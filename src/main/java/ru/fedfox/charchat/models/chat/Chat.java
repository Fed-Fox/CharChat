package ru.fedfox.charchat.models.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;

@Data
@AllArgsConstructor
public class Chat {

    private String id;
    private ArrayList<String> messages;
    private String starter;
    private String ender;

}
