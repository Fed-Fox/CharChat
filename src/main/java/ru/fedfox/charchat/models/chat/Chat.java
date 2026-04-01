package ru.fedfox.charchat.models.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Chat {

    private String id;
    private String starter;
    private String ender;

}
