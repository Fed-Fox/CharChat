package ru.fedfox.charchat.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.fedfox.charchat.models.user.User;
import ru.fedfox.charchat.service.Service;

@Slf4j
@Controller()
public class LoginController {

    @Autowired
    private Service service;


    @RequestMapping("/login")
    public String login() {
        return "login";
    }


    @PostMapping("/login")
    public ResponseEntity<?> userLogin(@RequestBody User user) {
        return service.validateLogin(user);
    }

    @GetMapping("/login-cookies")
    public ResponseEntity<?> validateCookies(@CookieValue(value = "session") String session) {
        return new ResponseEntity<>(service.validateLoginCookies(session), HttpStatus.OK);
    }

}
