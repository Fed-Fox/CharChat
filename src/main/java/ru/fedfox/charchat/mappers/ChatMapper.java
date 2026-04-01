package ru.fedfox.charchat.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.fedfox.charchat.models.chat.Chat;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ChatMapper implements RowMapper<Chat> {

    @Override
    public Chat mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Chat(
                rs.getString("id"),
                rs.getString("starter"),
                rs.getString("ender")
        );
    }

}
