package com.example.user.app4;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by User on 21-Jun-16.
 */
public class HashMapParser {
    private HashMap<String,String> map;
    /*c'tor*/
    public HashMapParser() {
        this.map = new HashMap<String,String>();
    }
    /*regular put implemantation*/
    public void put(String key,String val) {
        this.map.put(key,val);
    }

    /*
    name parse
    dsc: the func takes the map and return a string readable to the network
     */
    public String Parse() {
        String s = "";
        for (Map.Entry<String, String> entry : map.entrySet())
        {
            s += entry.getKey()+"="+entry.getValue()+"&";
        }
        s = s.substring(0,s.length() -1);
        return s;
    }
}
