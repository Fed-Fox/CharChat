package ru.fedfox.charchat.exeptions;

public class ValidationExeption extends RuntimeException {

    public ValidationExeption(String message) {
        super(message);
    }

}
