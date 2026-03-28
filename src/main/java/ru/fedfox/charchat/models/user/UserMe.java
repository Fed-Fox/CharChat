package ru.fedfox.charchat.models.user;

import lombok.Data;
import ru.fedfox.charchat.enums.UserStatus;

@Data
public class UserMe {

    private String id;
    private String wsid;
    private String tag;
    private String displayName;
    private UserStatus status;

    public UserMe(User user) {
        this.id = user.getId();
        this.displayName = user.getDisplayName();
        this.tag = user.getTag();
        this.status = user.getStatus();
        this.wsid = user.getWsid();
    }

}
