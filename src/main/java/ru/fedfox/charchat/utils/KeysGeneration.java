package ru.fedfox.charchat.utils;

import java.util.Random;

public class KeysGeneration {

    public static String generateWithRandomSimvols(int len) {
        return generateWithRandomSimvols(len, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMOPQRSTUVWXYZ0123456789");
    }

    public static String generateWithRandomSimvols(int len, String alp) {
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();

        for (int i = 0 ; i < len ; i++) {
            sb.append(alp.charAt(rand.nextInt(0, alp.length())));
        }

        return sb.toString();
    }

}
