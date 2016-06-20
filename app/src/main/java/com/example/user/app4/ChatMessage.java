package com.example.user.app4;

public class ChatMessage {
    public String message;
    public String username;
    public java.sql.Timestamp timeStamp;

    public ChatMessage(String username,String message,java.sql.Timestamp t) {
        super();
        this.message = message;
        this.username = username;
        this.timeStamp = t;
    }
}