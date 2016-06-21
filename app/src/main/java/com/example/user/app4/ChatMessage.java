package com.example.user.app4;

public class ChatMessage {
    public String msg;
    public String sender;
    public java.sql.Timestamp time;
    public long msgNum;
    public ChatMessage(String username,String message,java.sql.Timestamp t,long msgNum) {
        super();
        this.msg = message;
        this.sender = username;
        this.time = t;
        this.msgNum = msgNum;
    }

}