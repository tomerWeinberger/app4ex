package com.example.user.app4;

import java.sql.Timestamp;

public class ChatMessage {
    public String msg;
    public String sender;
    public java.sql.Timestamp time;
    public long msgNum;
    public ChatMessage(String username,String message,String t) {
        super();
        this.msg = message;
        this.sender = username;
        this.time = Timestamp.valueOf(t);
    }

}