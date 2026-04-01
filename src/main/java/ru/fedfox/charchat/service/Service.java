package ru.fedfox.charchat.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import ru.fedfox.charchat.dal.Repository;
import ru.fedfox.charchat.enums.NotificationType;
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
import ru.fedfox.charchat.utils.TextValidator;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@org.springframework.stereotype.Service
@AllArgsConstructor
public class Service {

    private Repository repository;

    public ResponseEntity<?> wsMessageResponse(Message message, SimpMessagingTemplate messagingTemplate) {
        if (TextValidator.textValidationMessage(message.getContent())) {
            throw new ValidationExeption("Некорректный ввод");
        }

        Optional<User> sender = repository.getUser(message.getUserId());

        Optional<Chat> chat = repository.getChat(message.getChatId());

        if (sender.isEmpty()) {
            throw new NotFoundExeption("Сессия не найдена!");
        }

        if (chat.isEmpty()) {
            throw new NotFoundExeption("Чат не найден!");
        }

        if (!repository.userHasChat(message.getChatId(), message.getUserId())) {
            throw new ValidationExeption("Чат не найден у пользователя!");
        }

        Optional<User> soeUser = repository.getUser(repository.getSoEChatMember(chat.get(), sender.get().getId()));

        if (soeUser.isEmpty()) {
            throw new NotFoundExeption("Сессия не найдена");
        }

        message.setType(NotificationType.MESSAGE);

        message.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));

        repository.addMessage(message);

        messagingTemplate.convertAndSend("/topic/user/" + soeUser.get().getWsid(),
                new MessageNotification(
                        message.getChatId(),
                        sender.get().getDisplayName(),
                        message.getContent(),
                        message.getTimestamp().toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        message.getType()
                )
        );

        messagingTemplate.convertAndSend("/topic/user/" + sender.get().getWsid(),
                new MessageNotification(
                        message.getChatId(),
                        sender.get().getDisplayName(),
                        message.getContent(),
                        message.getTimestamp().toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        message.getType()
                )
        );

        return ResponseEntity.ok().build();
    }

    public List<ChatNotification> getChatsByUser(String session) {
        if (repository.getUser(session).isPresent()) {
            return repository.getListChatsByUser(session);
        }

        throw new NotFoundExeption("Сессия не найдена!");
    }

    /* В НАСТОЯЩИЙ МОМЕНТ НЕ ИСПОЛЬЗУЕТЬСЯ ИЗ-ЗА ПОЛЬЗОВАТЕЛЬСКОГО СОХРАНЕНРИЯ СООБЩЕНИЙ

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


     */

    public Map<String, Object> validateCookiesAndGetMe(String session) {
        Map<String, Object> map = new HashMap<>();

        Optional<User> user = repository.getUser(session);

        if (user.isPresent()) {
            user.get().setWsid(repository.genUserWsId(session));
            map.put("user", new UserMe(user.get()));
            map.put("validate", true);

            log.info(String.format("Пользователь %s вошёл в свой аккаунт по cookies", user.get().getTag()));
        } else {
            map.put("validate", false);
        }

        return map;
    }

    public Map<String, Object> validateLoginCookies(String session) {
        Map<String, Object> map = new HashMap<>();

        Optional<User> user = repository.getUser(session);

        if (user.isPresent()) {
            map.put("validate", true);

            log.info(String.format("Пользователь %s вошёл в свой аккаунт по cookies", user.get().getTag()));
        } else {
            map.put("validate", false);
        }

        return map;
    }

    public Profile getUserProfile(String session, String chatId) {
        Optional<User> sender = repository.getUser(session);

        Optional<Chat> chat = repository.getChat(chatId);

        if (sender.isEmpty()) {
            throw new NotFoundExeption("Сессия не найдена!");
        }

        if (chat.isEmpty()) {
            throw new NotFoundExeption("Чат не найден!");
        }

        return new Profile(
                repository.getUser(repository.getSoEChatMember(chat.get(), session)).get()
        );
    }

    public Map<String, Object> createChatWithUser(String session, String tag, SimpMessagingTemplate messagingTemplate) {
        Optional<User> starter = repository.getUser(session);

        if (starter.isEmpty()) {
            throw new NotFoundExeption("Сессия не найдена!");
        }

        Optional<User> ender = repository.getUserByTag(tag);

        if (ender.isEmpty()) {
            throw new NotFoundExeption("Тег не найден");
        }

        if (repository.hasChatWithUsers(starter.get(), ender.get())) {
            throw new ExistExeption("Чат уже существует");
        }

        Chat chat = repository.addChat(new Chat(
                "",
                starter.get().getId(),
                ender.get().getId()
        ));

        List<MessageNotification> messages = new ArrayList<>();

        messagingTemplate.convertAndSend("/topic/user/" + starter.get().getWsid(),
                new ChatNotification(
                        chat.getId(),
                        ender.get().getDisplayName(),
                        ender.get().getStatus(),
                        NotificationType.NEW_CHAT,
                        messages
                )
        );

        messagingTemplate.convertAndSend("/topic/user/" + ender.get().getWsid(),
                new ChatNotification(
                        chat.getId(),
                        starter.get().getDisplayName(),
                        starter.get().getStatus(),
                        NotificationType.NEW_CHAT,
                        messages
                )
        );

        Map<String, Object> body = new HashMap<>();

        body.put("success", true);

        log.info(String.format("Создан новый чат между пользователями %s и %s", starter.get().getTag(), ender.get().getTag()));

        return body;
    }

    public ResponseEntity<?> validateLogin(User user) {
        Optional<User> userbd = repository.getUserByMail(user.getMail());

        if (userbd.isEmpty() || !userbd.get().getPassword().equals(user.getPassword())) {
            throw new NotFoundExeption("Ошибка логина или пароля");
        }

        if (!TextValidator.textValidationPassword(user.getPassword()) || !TextValidator.textValidationEmail(user.getMail())) {
            throw new ValidationExeption("Некоректные данные");
        }

        ResponseCookie rc = ResponseCookie.from("session", userbd.get().getId())
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(365 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        Map<String, Object> body = new HashMap<>();

        body.put("success", true);

        log.info(String.format("Пользователь %s вошёл в свой аккаунт", userbd.get().getTag()));

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, rc.toString()).body(body);
    }

    public ResponseEntity<?> validateReg(User user) {
        if (repository.containsTag(user.getTag())) {
            throw new ExistExeption("Такой тег уже существует!");
        }

        if (repository.containsEmail(user.getMail())) {
            throw new ExistExeption("Такая почта уже зарегестрирована!");
        }

        if (!TextValidator.textValidationPassword(user.getPassword())
                || !TextValidator.textValidationEmail(user.getMail())
                || !TextValidator.textValidationTag(user.getTag())
                || !TextValidator.textValidationDisplayName(user.getDisplayName())) {
            throw new ValidationExeption("Некоректные данные");
        }

        User userbd = repository.addUser(user);

        ResponseCookie rc = ResponseCookie.from("session", user.getId())
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(365 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        Map<String, Object> body = new HashMap<>();

        body.put("success", true);

        log.info(String.format("Пользователь %s зарегистрировал в свой аккаунт", userbd.getTag()));

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, rc.toString()).body(body);
    }
}
