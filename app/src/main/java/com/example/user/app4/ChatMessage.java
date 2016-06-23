package com.example.user.app4;

import java.sql.Timestamp;

public class ChatMessage implements Comparable  {
    public String msg;
    public String sender;
    public java.sql.Timestamp time;
    public long msgNum;
    /*
    c'tor
     */
    public ChatMessage(String username,String message,String t) {
        super();
        this.msg = message;
        this.sender = username;
        this.time = Timestamp.valueOf(t);
    }


    @Override
    public int compareTo(Object another) {
        ChatMessage next  = (ChatMessage) another;
         if(this.time.before(next.time)){
             return -1;
         }
        return 1;
    }
}