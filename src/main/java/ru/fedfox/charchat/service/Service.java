package ru.fedfox.charchat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import ru.fedfox.charchat.enums.NotificationType;
import ru.fedfox.charchat.enums.UserStatus;
import ru.fedfox.charchat.exeptions.ExistExeption;
import ru.fedfox.charchat.exeptions.NotFoundExeption;
import ru.fedfox.charchat.exeptions.ValidationExeption;
import ru.fedfox.charchat.models.chat.Chat;
import ru.fedfox.charchat.models.chat.ChatNotification;
import ru.fedfox.charchat.models.message.Message;
import ru.fedfox.charchat.models.message.MessageNotification;
import ru.fedfox.charchat.models.user.Profile;
import ru.fedfox.charchat.models.user.User;
import ru.fedfox.charchat.models.user.UserMe;
import ru.fedfox.charchat.storage.Storage;
import ru.fedfox.charchat.utils.KeysGeneration;
import ru.fedfox.charchat.utils.TextValidator;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@org.springframework.stereotype.Service
public class Service {

    private Storage storage;

    public Service(Storage storage) {
        this.storage = storage;

        storage.addUser(new User(
                "",
                "",
                "fedfox",
                "FedFox",
                "FedFox3000",
                "foxyfedor3000@gmail.com",
                UserStatus.ONLINE,
                new ArrayList<>()
        ));

        storage.addUser(new User(
                "",
                "",
                "tester",
                "Testировщик",
                "FedFox3000",
                "test@gmail.com",
                UserStatus.ONLINE,
                new ArrayList<>()
        ));
    }

    public ResponseEntity<?> wsMessageResponse(Message message, SimpMessagingTemplate messagingTemplate) {
        if (TextValidator.textValidationMessage(message.getContent())) {
            throw new ValidationExeption("Некорректный ввод");
        }

        if (!storage.containsCookies(message.getUserId())) {
            throw new NotFoundExeption("Сессия не найдена!");
        }

        if (!storage.containsChat(message.getChatId())) {
            throw new NotFoundExeption("Сессия не найдена!");
        }

        message.setType(NotificationType.MESSAGE);

        storage.addMessage(message);

        User starterUser = storage.getUser(storage.getChat(message.getChatId()).getStarter());

        User enderUser = storage.getUser(storage.getChat(message.getChatId()).getEnder());

        User sender = storage.getUser(message.getUserId());

        messagingTemplate.convertAndSend("/topic/user/" + starterUser.getWsid(),
                new MessageNotification(
                        message.getChatId(),
                        sender.getDisplayName(),
                        message.getContent(),
                        message.getTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        message.getType()
                )
        );

        messagingTemplate.convertAndSend("/topic/user/" + enderUser.getWsid(),
                new MessageNotification(
                        message.getChatId(),
                        sender.getDisplayName(),
                        message.getContent(),
                        message.getTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        message.getType()
                )
        );

        return ResponseEntity.ok().build();
    }

    public List<ChatNotification> getChatsByUser(String data) {
        if (storage.containsCookies(data)) {
            return storage.getListChatsByUser(data);
        }

        throw new NotFoundExeption("Сессия не найдена!");
    }

    public ArrayList<MessageNotification> getMessagesFromChat(String session, String chatId) {
        if (storage.containsCookies(session)) {
            ArrayList<MessageNotification> list = new ArrayList<>();

            for (Message message : storage.getMessagesByChat(chatId)) {
                list.add(new MessageNotification(message));
            }

            return list;
        }

        throw new NotFoundExeption("Сессия не найдена!");
    }

    public Map<String, Object> validateCookiesAndGetMe(String session) {
        Map<String, Object> map = new HashMap<>();

        if (storage.containsCookies(session)) {
            storage.genUserWsId(session);
            map.put("user", new UserMe(storage.getUser(session)));
            map.put("validate", true);
        } else {
            map.put("validate", false);
        }

        return map;
    }

    public Map<String, Object> validateLoginCookies(String session) {
        Map<String, Object> map = new HashMap<>();

        if (storage.containsCookies(session)) {
            map.put("validate", true);
        } else {
            map.put("validate", false);
        }

        return map;
    }

    public Profile getUserProfile(String session, String chatId) {
        if (!storage.containsCookies(session)) {
            throw new NotFoundExeption("Сессия не найдена!");
        }

        if (!storage.containsCookies(session)) {
            throw new NotFoundExeption("Сессия не найдена!");
        }

        return new Profile(
                storage.getSoEChatMember(chatId, session)
        );
    }

    public Map<String, Object> createChatWithUser(String session, String tag, SimpMessagingTemplate messagingTemplate) {
        if (!storage.containsCookies(session)) {
            throw new NotFoundExeption("Сессия не найдена!");
        }

        User fromUser = storage.getUser(session);

        User toUser = storage.getUserByTag(tag);

        if (toUser == null) {
            throw new NotFoundExeption("Тег не найден");
        }

        if (storage.hasChatWithUsers(fromUser, toUser)) {
            throw new ExistExeption("Чат уже существует");
        }

        Chat chat = storage.addChat(new Chat(
                "",
                new ArrayList<>(),
                fromUser.getId(),
                toUser.getId()
        ));

        List<MessageNotification> messages = storage.getMessageNotificationsByChat(chat.getId());

        messages = messages.subList(0, Math.min(messages.size(), 100));

        messagingTemplate.convertAndSend("/topic/user/" + fromUser.getWsid(),
                new ChatNotification(
                        chat.getId(),
                        toUser.getDisplayName(),
                        toUser.getStatus(),
                        NotificationType.NEW_CHAT,
                        messages
                )
        );

        messagingTemplate.convertAndSend("/topic/user/" + toUser.getWsid(),
                new ChatNotification(
                        chat.getId(),
                        fromUser.getDisplayName(),
                        fromUser.getStatus(),
                        NotificationType.NEW_CHAT,
                        messages
                )
        );

        Map<String, Object> body = new HashMap<>();

        body.put("success", true);

        return body;
    }

    public ResponseEntity<?> validateLogin(User user) {
        User userbd = storage.getUserByEmail(user.getMail());

        if (userbd == null || !userbd.getPassword().equals(user.getPassword())) {
            throw new NotFoundExeption("Ошибка логина или пароля");
        }

        if (!TextValidator.textValidationPassword(user.getPassword()) || !TextValidator.textValidationEmail(user.getMail())) {
            throw new ValidationExeption("Некоректные данные");
        }

        ResponseCookie rc = ResponseCookie.from("session", userbd.getId())
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(365 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        Map<String, Object> body = new HashMap<>();

        body.put("success", true);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, rc.toString()).body(body);
    }

    public ResponseEntity<?> validateReg(User user) {
        if (storage.containsTag(user.getTag())) {
            throw new ExistExeption("Такой тег уже существует!");
        }

        if (storage.containsEmail(user.getMail())) {
            throw new ExistExeption("Такая почта уже зарегестрирована!");
        }

        if (!TextValidator.textValidationPassword(user.getPassword())
                || !TextValidator.textValidationEmail(user.getMail())
                || !TextValidator.textValidationTag(user.getTag())
                || !TextValidator.textValidationDisplayName(user.getDisplayName())) {
            throw new ValidationExeption("Некоректные данные");
        }

        ResponseCookie rc = ResponseCookie.from("session", storage.addUser(user))
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(365 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        Map<String, Object> body = new HashMap<>();

        body.put("success", true);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, rc.toString()).body(body);
    }
}
