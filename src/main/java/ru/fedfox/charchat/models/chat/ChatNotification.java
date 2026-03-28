package ru.fedfox.charchat.models.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.fedfox.charchat.enums.NotificationType;
import ru.fedfox.charchat.enums.UserStatus;
import ru.fedfox.charchat.models.Notification;
import ru.fedfox.charchat.models.message.MessageNotification;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatNotification implements Notification {

    private String id;
    private String name;
    private UserStatus status;
    private NotificationType type;
    private List<MessageNotification> messages;

    public ChatNotification(Chat chat, UserStatus status, String name, List<MessageNotification> messages) {
        this.id = chat.getId();
        this.status = status;
        this.name = name;
        this.type = NotificationType.NEW_CHAT;
        this.messages = messages;
    }

}
