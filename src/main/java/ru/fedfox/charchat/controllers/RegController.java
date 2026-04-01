package ru.fedfox.charchat.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.fedfox.charchat.models.user.User;
import ru.fedfox.charchat.service.Service;

@Controller
@AllArgsConstructor
public class RegController {

    private Service service;


    @RequestMapping("/reg")
    public String reg() {
        return "reg";
    }


    @PostMapping("/reg")
    public ResponseEntity<?> userRegister(@RequestBody User user) {
        return service.validateReg(user);
    }

}
