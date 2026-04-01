package ru.fedfox.charchat.enums;

import lombok.Getter;

@Getter
public enum NotificationType {

    MESSAGE(0),
    INFO(1),
    NEW_CHAT(2);

    int type = 0;

    NotificationType(int type) {
        this.type = type;
    }

}
