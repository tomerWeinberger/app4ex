package com.example.user.app4;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {

    private TextView chatText;
    private TextView chatText2;
    private List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
    private Context context;

    @Override
    public void add(ChatMessage object) {
        chatMessageList.add(object);
        super.add(object);
        if(getCount() > 10){
            remove(getItem(0));
        }
    }
    @Override
    public void remove(ChatMessage object) {
        chatMessageList.remove(object);
        super.remove(object);
    }
    public ChatArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }
    @Override
    public int getCount() {
        return this.chatMessageList.size();
    }

    public ChatMessage getItem(int index) {
        return this.chatMessageList.get(index);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessageObj = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        row = inflater.inflate(R.layout.msg, parent, false);
        chatText = (TextView) row.findViewById(R.id.msgr);
        chatText2 = (TextView) row.findViewById(R.id.umsgr);
        chatText.setText(chatMessageObj.message);
        chatText2.setText(chatMessageObj.username);
        return row;
    }

}


