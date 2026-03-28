package ru.fedfox.charchat.controllers;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.fedfox.charchat.models.chat.Chat;
import ru.fedfox.charchat.models.chat.ChatNotification;
import ru.fedfox.charchat.models.message.Message;
import ru.fedfox.charchat.models.message.MessageNotification;
import ru.fedfox.charchat.service.Service;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@Controller
public class ChatControllers {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private Service service;


    @RequestMapping("/")
    public String index() {
        return "index";
    }


    @MessageMapping("/chat/send-message")
    public ResponseEntity<?> processMessage(@Payload Message message) {
        return service.wsMessageResponse(message, messagingTemplate);
    }


    @GetMapping("/app/chats")
    public ResponseEntity<?> getChats(@CookieValue(value = "session") String data) {
        return ResponseEntity.ok(service.getChatsByUser(data));
    }

    @GetMapping("/app/messages/{chatId}")
    public ResponseEntity<?> getMessagesFromChat(@CookieValue(value = "session") String session, @PathVariable String chatId) {
        return ResponseEntity.ok(service.getMessagesFromChat(session, chatId));
    }

    @GetMapping("/app/validate-cookies/")
    public ResponseEntity<?> validateCookies(@CookieValue(value = "session") String session) {
        return new ResponseEntity<>(service.validateCookiesAndGetMe(session), HttpStatus.OK);
    }

    @GetMapping("/app/chat/create/{tag}")
    public ResponseEntity<?> createChatWithUser(@CookieValue(value = "session") String session, @PathVariable String tag) {
        return new ResponseEntity<>(service.createChatWithUser(session, tag, messagingTemplate), HttpStatus.OK);
    }

    @GetMapping("/app/chat/profile/{chatId}")
    public ResponseEntity<?> getUserProfile(@CookieValue(value = "session") String session, @PathVariable String chatId) {
        return new ResponseEntity<>(service.getUserProfile(session, chatId), HttpStatus.OK);
    }

}
