package com.example.user.app4;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {

    private TextView chatText;
    private TextView chatText2;
    private List<ChatMessage> chatMessageList = new ArrayList<>();
    private Context context;
    private int toSee;

    public ChatArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
        this.toSee = 10;
    }

    public List<ChatMessage> getList() {
        return chatMessageList;
    }
    public List<ChatMessage> sort(List<ChatMessage> c) {
        //sort list

        Collections.sort(c);
        return c;
    }
    public boolean Differ(List<ChatMessage> l) {
        int min = (this.chatMessageList.size() <l.size() ? this.chatMessageList.size():l.size());
        for(int i=0;i<min;i++){
            ChatMessage one = l.get(i);
            ChatMessage two = this.chatMessageList.get(i);
            if(!one.time.equals(two.time) || !one.sender.equals(two.sender) ||
                    !one.msg.equals(two.msg))
                return  true;
        }
        return false;
    }
    public void setList(List<ChatMessage> l,String action) {
        if(action.equals("from")) {
            this.chatMessageList.clear();
            super.clear();
        }
        for(int i=0;i<l.size();i++)
            this.add(l.get(i));
        this.chatMessageList = this.sort(this.chatMessageList);
        super.notifyDataSetChanged();
    }

    public List<ChatMessage> Copy() {
        List<ChatMessage> copy = new ArrayList<>();
        for(int i=0;i<this.chatMessageList.size();i++)
            copy.add(this.chatMessageList.get(i));
        return copy;
    }
    /*
    name add
    dsc the func add an object and if there are too much on the display
        area-it delets the latest
     */
    @Override
    public void add(ChatMessage object) {
        chatMessageList.add(object);
        super.add(object);
        //if there too much
        if(getCount() > toSee){
            remove(getItem(0));
        }
    }

    /*
    remove an object from list
     */
    @Override
    public void remove(ChatMessage object) {
        chatMessageList.remove(object);
        super.remove(object);
    }

    //initialize first list length to 10
    public void initializetoSee(){
        this.toSee=10;
    }
    //add tem more if necceccarry
    public void addTenTolist(){
        this.toSee = this.chatMessageList.size()+10;
    }
    @Override
    //get size of list
    public int getCount() { return this.chatMessageList.size(); }
    //get ith item
    public ChatMessage getItem(int index) {
        return this.chatMessageList.get(index);
    }

    /*
    the func present the msg
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessageObj = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        row = inflater.inflate(R.layout.msg, parent, false);
        chatText = (TextView) row.findViewById(R.id.msgr);
        chatText2 = (TextView) row.findViewById(R.id.umsgr);
        chatText.setText(chatMessageObj.msg);
        Timestamp t = chatMessageObj.time;
        String time = String.valueOf(t.getHours())+":"+String.valueOf(t.getMinutes());
        chatText2.setText(chatMessageObj.sender+" "+time);
        return row;
    }
}


