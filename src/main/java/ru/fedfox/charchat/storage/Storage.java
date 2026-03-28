package ru.fedfox.charchat.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.fedfox.charchat.enums.NotificationType;
import ru.fedfox.charchat.enums.UserStatus;
import ru.fedfox.charchat.models.chat.Chat;
import ru.fedfox.charchat.models.chat.ChatNotification;
import ru.fedfox.charchat.models.message.Message;
import ru.fedfox.charchat.models.message.MessageNotification;
import ru.fedfox.charchat.models.user.User;
import ru.fedfox.charchat.utils.KeysGeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class Storage {

    private HashMap<String, Chat> chats = new HashMap<>();
    private HashMap<String, Message> messages = new HashMap<>();
    private HashMap<String, User> users = new HashMap<>();

    public Chat addChat(Chat chat) {
        chat.setId(KeysGeneration.generateWithRandomSimvols(64));

        chats.put(chat.getId(), chat);

        users.get(chats.get(chat.getId()).getStarter()).getChats().add(chat.getId());

        users.get(chats.get(chat.getId()).getEnder()).getChats().add(chat.getId());

        return chat;
    }

    public void addMessage(Message message) {
        message.setId(KeysGeneration.generateWithRandomSimvols(64));
        message.setUserName(users.get(message.getUserId()).getDisplayName());
        messages.put(message.getId(), message);
        chats.get(message.getChatId()).getMessages().add(message.getId());
    }

    public String addUser(User user) {
        log.info(String.format("Добавлен пользователь %s (%s)", user.getTag(), user.getDisplayName()));
        String id = KeysGeneration.generateWithRandomSimvols(64);
        user.setId(id);
        user.setChats(new ArrayList<>());
        user.setStatus(UserStatus.ONLINE);
        users.put(id, user);
        return id;
    }


    public User getUser(String id) {
        return users.get(id);
    }

    public ArrayList<User> getUsersByChat(String chatId) {
        ArrayList<User> list = new ArrayList<>();

        list.add(users.get(chats.get(chatId).getStarter()));

        list.add(users.get(chats.get(chatId).getEnder()));

        return list;
    }

    public ArrayList<ChatNotification> getListChatsByUser(String user) {
        ArrayList<ChatNotification> list = new ArrayList<>();

        for (String id : users.get(user).getChats()) {
            User userSoE = getSoEChatMember(id, user);

            List<MessageNotification> listMessages = new ArrayList<>();

            for (String messId : chats.get(id).getMessages()) {
                listMessages.add(new MessageNotification(messages.get(messId)));
            }

            listMessages = listMessages.subList(0, Math.min(listMessages.size(), 100));

            list.add(
                    new ChatNotification(
                            id,
                            userSoE.getDisplayName(),
                            userSoE.getStatus(),
                            NotificationType.NEW_CHAT,
                            listMessages
                    )
            );
        }

        return list;
    }

    public User getSoEChatMember(String chatId, String user) {
        Chat chat = chats.get(chatId);

        if (chat.getStarter().equals(user)) {
            return users.get(chat.getEnder());
        } else {
            return users.get(chat.getStarter());
        }
    }

    public ArrayList<Message> getMessagesByChat(String id) {
        ArrayList<Message> list = new ArrayList<>();

        for (String messId : chats.get(id).getMessages()) {
            list.add(messages.get(messId));
        }

        return list;
    }

    public ArrayList<MessageNotification> getMessageNotificationsByChat(String id) {
        ArrayList<MessageNotification> list = new ArrayList<>();

        for (String messId : chats.get(id).getMessages()) {
            list.add(new MessageNotification(messages.get(messId)));
        }

        return list;
    }

    public User getUserByTag(String tag) {
        for (User user : users.values()) {
            if (user.getTag().equals(tag)) {
                return user;
            }
        }

        return null;
    }

    public User getUserByEmail(String mail) {
        for (User user : users.values()) {
            if (user.getMail().equals(mail)) {
                return user;
            }
        }

        return null;
    }



    public boolean containsChat(String chatId) {
        return chats.containsKey(chatId);
    }

    public boolean containsCookies(String data) {
        return users.containsKey(data);
    }

    public void genUserWsId(String session) {
        users.get(session).setWsid(KeysGeneration.generateWithRandomSimvols(64));
    }

    public boolean hasChatWithUsers(User fromUser, User toUser) {
        for (Chat chat : chats.values()) {
            if (chat.getEnder().equals(toUser.getId()) && chat.getStarter().equals(fromUser.getId())) {
                return true;
            }
        }

        return false;
    }

    public boolean containsTag(String tag) {
        for (User user : users.values()) {
            if (user.getTag().equals(tag)) {
                return true;
            }
        }

        return false;
    }

    public boolean containsEmail(String mail) {
        for (User user : users.values()) {
            if (user.getMail().equals(mail)) {
                return true;
            }
        }

        return false;
    }

    public Chat getChat(String chatId) {
        return chats.get(chatId);
    }
}
