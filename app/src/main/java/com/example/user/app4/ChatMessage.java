package com.example.user.app4;

public class ChatMessage {
    public String message;
    public String username;
    public String timeStamp;

    public ChatMessage(String username,String message) {
        super();
        this.message = message;
        this.username = username;
    }
}