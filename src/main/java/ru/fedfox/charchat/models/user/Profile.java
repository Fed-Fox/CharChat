package ru.fedfox.charchat.models.user;

import lombok.Data;
import ru.fedfox.charchat.enums.UserStatus;

@Data
public class Profile {

    private String tag;
    private String displayName;
    private UserStatus status;

    public Profile(User user) {
        this.displayName = user.getDisplayName();
        this.tag = user.getTag();
        this.status = user.getStatus();
    }

}
