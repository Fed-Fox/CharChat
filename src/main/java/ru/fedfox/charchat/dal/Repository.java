package ru.fedfox.charchat.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.fedfox.charchat.enums.NotificationType;
import ru.fedfox.charchat.enums.UserStatus;
import ru.fedfox.charchat.exeptions.NotFoundExeption;
import ru.fedfox.charchat.mappers.ChatMapper;
import ru.fedfox.charchat.mappers.MessageMapper;
import ru.fedfox.charchat.mappers.UserMapper;
import ru.fedfox.charchat.models.chat.Chat;
import ru.fedfox.charchat.models.chat.ChatNotification;
import ru.fedfox.charchat.models.message.Message;
import ru.fedfox.charchat.models.message.MessageNotification;
import ru.fedfox.charchat.models.user.User;
import ru.fedfox.charchat.utils.KeysGeneration;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@org.springframework.stereotype.Repository
public class Repository {


    private final JdbcTemplate jdbc;
    private final UserMapper userMapper;
    private final ChatMapper chatMapper;
    private final MessageMapper messageMapper;



    public Chat addChat(Chat chat) {
        chat.setId(KeysGeneration.generateWithRandomSimvols(64));

        jdbc.update(
                "INSERT INTO chats(id, starter, ender) VALUES (?, ?, ?)",
                chat.getId(), chat.getStarter(), chat.getEnder()
        );

        return chat;
    }

    public Message addMessage(Message message) {
        message.setId(KeysGeneration.generateWithRandomSimvols(64));

        jdbc.update(
                "INSERT INTO messages(id, sender, chat, content, time, type) VALUES (?, ?, ?, ?, ?, ?)",
                message.getId(), message.getUserId(), message.getChatId(), message.getContent(), message.getTimestamp(), message.getType().getType()
        );

        return message;
    }

    public User addUser(User user) {
        user.setId(KeysGeneration.generateWithRandomSimvols(64));
        user.setStatus(UserStatus.ONLINE);

        jdbc.update(
                "INSERT INTO users(id, wsid, mail, tag, displayName, password) VALUES (?, ?, ?, ?, ?, ?)",
                user.getId(), "", user.getMail(), user.getTag(), user.getDisplayName(), user.getPassword()
        );

        return user;
    }


    public Optional<User> getUser(String id) {
        try {
            User result = jdbc.queryForObject(
                    "SELECT * FROM users WHERE id = ?",
                    userMapper,
                    id
            );

            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public Optional<Chat> getChat(String id) {
        try {
            Chat result = jdbc.queryForObject(
                    "SELECT * FROM chats WHERE id = ?",
                    chatMapper,
                    id
            );

            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public List<ChatNotification> getListChatsByUser(String userId) {
        List<Chat> result = jdbc.query(
                "SELECT * FROM chats WHERE starter = ? OR ender = ?",
                chatMapper,
                userId, userId
        );

        Optional<User> user = getUser(userId);

        if (user.isEmpty()) {
            throw new NotFoundExeption("Индефикатор пользователя не найден");
        }

        return result.stream()
                .map(chat -> {
                    Optional<User> soeUser = getUser(getSoEChatMember(chat, userId));

                    if (soeUser.isEmpty()) {
                        throw new NotFoundExeption("Индефикатор пользователя не найден");
                    }

                    return new ChatNotification(
                            chat.getId(),
                            soeUser.get().getDisplayName(),
                            soeUser.get().getStatus(),
                            NotificationType.NEW_CHAT,
                            getMessagesByChat(chat.getId())
                                    .stream()
                                    .map(MessageNotification::new)
                                    .toList()
                    );
                })
                .toList();
    }

    public String getSoEChatMember(Chat chat, String user) {
        if (chat.getStarter().equals(user)) {
            return chat.getEnder();
        } else if (chat.getEnder().equals(user)) {
            return chat.getStarter();
        } else {
            throw new NotFoundExeption("У пользователя нет такого чата");
        }
    }

    public List<Message> getMessagesByChat(String id) {
        return jdbc.query(
                "SELECT * FROM messages WHERE chat = ?",
                messageMapper,
                id
        );
    }

    public Optional<User> getUserByTag(String tag) {
        try {
            User result = jdbc.queryForObject(
                    "SELECT * FROM users WHERE tag = ?",
                    userMapper,
                    tag
            );

            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public Optional<User> getUserByMail(String mail) {
        try {
            User result = jdbc.queryForObject(
                    "SELECT * FROM users WHERE mail = ?",
                    userMapper,
                    mail
            );

            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public String genUserWsId(String session) {
        String wsid = KeysGeneration.generateWithRandomSimvols(64);

        jdbc.update(
                "UPDATE users SET wsid = ? WHERE id = ?",
                wsid,
                session
        );

        return wsid;
    }

    public boolean hasChatWithUsers(User fromUser, User toUser) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM chats WHERE (starter = ? AND ender = ?) OR (starter = ? AND ender = ?))",
                Boolean.class,
                fromUser.getId(), toUser.getId(),
                toUser.getId(), fromUser.getId()
        ));
    }

    public boolean userHasChat(String chatId, String userId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM chats WHERE id = ? AND (starter = ? OR ender = ?))",
                Boolean.class,
                chatId, userId, userId
        ));
    }

    public boolean containsTag(String tag) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM users WHERE tag = ?)",
                Boolean.class,
                tag
        ));
    }

    public boolean containsEmail(String mail) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM users WHERE mail = ?)",
                Boolean.class,
                mail
        ));
    }

}
