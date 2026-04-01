package ru.fedfox.charchat.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.fedfox.charchat.enums.UserStatus;
import ru.fedfox.charchat.models.user.User;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new User(
                rs.getString("id"),
                rs.getString("wsid"),
                rs.getString("tag"),
                rs.getString("displayName"),
                rs.getString("password"),
                rs.getString("mail"),
                UserStatus.ONLINE
        );
    }

}
