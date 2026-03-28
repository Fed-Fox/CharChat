package ru.fedfox.charchat.models.message;

import lombok.Data;
import ru.fedfox.charchat.enums.NotificationType;

import java.time.LocalDateTime;

@Data
public class Message {

    private String id;
    private String userId;
    private String userName;
    private String chatId;
    private String content;
    private NotificationType type;
    private LocalDateTime time;

}
