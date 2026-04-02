package ru.fedfox.charchat.models.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.fedfox.charchat.enums.NotificationType;
import ru.fedfox.charchat.models.Notification;

import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
public class MessageNotification implements Notification {

    private String chatId;
    private String sender;
    private String content;
    private String time;
    private NotificationType type;

    public MessageNotification(Message message, String sender) {
        this.chatId = message.getChatId();
        this.content = message.getContent();
        this.time = message.getTimestamp().toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        this.type = message.getType();
        this.sender = sender;
    }

}
