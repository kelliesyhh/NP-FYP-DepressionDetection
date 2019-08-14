package com.example.depressionanalysis;

import android.app.Application;

public class PredictiveIndex extends Application {

    public static String SAVED_FILE;
    public static String SESSION_ID;
    public static String USER;

    public static String getSAVED_FILE() {
        return SAVED_FILE;
    }

    public static void setSAVED_FILE(String filename) {
        SAVED_FILE = filename;
    }

    public static void setSESSION_ID() {
        SESSION_ID = randomAlphaNumeric(10);
    }

    public static String getSESSION_ID(){
        return SESSION_ID;
    }

    public static void setUSER(String username) {
        USER = username;
    }

    public static String getUSER(){
        return USER;
    }

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
}