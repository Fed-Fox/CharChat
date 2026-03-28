package ru.fedfox.charchat.models.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import ru.fedfox.charchat.enums.UserStatus;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@ToString
public class User {

    private String id;
    private String wsid;
    private String tag;
    private String displayName;
    private String password;
    private String mail;
    private UserStatus status;
    private ArrayList<String> chats;

}
