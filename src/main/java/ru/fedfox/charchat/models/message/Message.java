package ru.fedfox.charchat.models.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.fedfox.charchat.enums.NotificationType;

import java.sql.Timestamp;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class Message {

    private String id;
    private String userId;
    private String chatId;
    private String content;
    private NotificationType type;
    private Timestamp timestamp;

}
