package ru.fedfox.charchat.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.fedfox.charchat.enums.NotificationType;
import ru.fedfox.charchat.enums.UserStatus;
import ru.fedfox.charchat.models.message.Message;
import ru.fedfox.charchat.models.user.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Component
public class MessageMapper implements RowMapper<Message> {

    @Override
    public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Message(
                rs.getString("id"),
                rs.getString("sender"),
                rs.getString("chat"),
                rs.getString("content"),
                getTypeByString(rs.getInt("type")),
                rs.getTimestamp("time")
        );
    }

    private NotificationType getTypeByString(int id) {
        return switch (id) {
            case 1 -> NotificationType.INFO;
            case 2 -> NotificationType.NEW_CHAT;
            default -> NotificationType.MESSAGE;
        };

    }

}
