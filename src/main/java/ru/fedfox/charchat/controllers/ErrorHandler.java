package ru.fedfox.charchat.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.fedfox.charchat.exeptions.ExistExeption;
import ru.fedfox.charchat.exeptions.NotFoundExeption;
import ru.fedfox.charchat.exeptions.ValidationExeption;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> validationError(ValidationExeption e) {
        return Map.of(
                "error", "Ошибка валидации",
                "message", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> nullPointer(NullPointerException e) {
        return Map.of(
                "error", "Ошибка ввода",
                "message", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> notFoundError(NotFoundExeption e) {
        return Map.of(
                "error", "Объект не найден",
                "message", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FOUND)
    public Map<String, String> existError(ExistExeption e) {
        return Map.of(
                "error", "Объект уже существует",
                "message", e.getMessage()
        );
    }

}
