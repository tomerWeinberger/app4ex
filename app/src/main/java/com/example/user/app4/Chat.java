package com.example.user.app4;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class Chat extends Activity {

    private static final String TAG = "ChatActivity";
    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private String username;
    private MsgTask mAuthTask;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private final long interval = 300000;
    private SensorManager mSensorManager;
    private ShakeIt mSensorListener;


    /*
    name:onCreate
    desc:initialize important vars
    */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get the service for shake option
        //create the view
        setContentView(R.layout.activity_chat);
        //initialize vars
        Bundle extras = getIntent().getExtras();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        username = settings.getString("username", "");
        buttonSend = (Button) findViewById(R.id.send);
        listView = (ListView) findViewById(R.id.msgview);
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.msg);
        listView.setAdapter(chatArrayAdapter);
        //initialize send btn and his function
        chatText = (EditText) findViewById(R.id.msg);
        //defined action if i press send ot "enter"
        chatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });
        //initiate refresh action
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage();
            }
        });
        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeIt();
        //define shake action
        mSensorListener.setOnShakeListener(new ShakeIt.OnShakeListener() {

            public void onShake() {
                Toast.makeText(Chat.this, getString(R.string.first_msg), Toast.LENGTH_LONG).show();
                updateMessages("shake");
            }
        });
        //ask for the last tem msg
        //get current time
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
        //ask for msg up to this time
        mAuthTask = new MsgTask("to", this.username, "msg", currentTimestamp.toString());
        mAuthTask.execute();
        setNotifcat();
    }

    /*
    name:onResume
    desc:this func activates once you resume activity
     */
    @Override
    protected void onResume() {
        super.onResume();
        //register to shake listener
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
        //initialize list le
        chatArrayAdapter.initializetoSee();
        //initialize swipe down refresh func
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTenMore();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /*
    name:onpause
    desc: pre pause the program
     */
    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

    /*
    * name updateMessages
    * desc: the func asks for an update of new msgs
    * */
    public void updateMessages(String choice) {
        //get current time
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
        //ask for msg from this time
        mAuthTask = new MsgTask("from", this.username, chatText.getText().toString(), currentTimestamp.toString());
        //if action was asked not by user(but by time defs)
        if (choice.equals("time"))
            mAuthTask.setNotify(true);
        mAuthTask.execute();
    }

    /*
    name loadTenMore
    dsc : the func asks for ten older msg
     */
    public void loadTenMore() {
        //get current time
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
        chatArrayAdapter.addTenTolist();
        //ask for msg up to this time
        mAuthTask = new MsgTask("to", this.username, chatText.getText().toString(), currentTimestamp.toString());
        mAuthTask.execute();
    }

    /*
    name sendChatMessage
    desc the func send the msg you sent
     */
    private boolean sendChatMessage() {
        //get current time
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
        //put msg in my display(b4 on everyones elses)
        ChatMessage cm = new ChatMessage(this.username, chatText.getText().toString(), currentTimestamp.toString());
        chatArrayAdapter.add(cm);
        //ask for server to save the msg in DB
        mAuthTask = new MsgTask("save", this.username, chatText.getText().toString(), currentTimestamp.toString());
        mAuthTask.execute();
        chatText.setText(" ");
        return true;
    }

    /*
    this class is in charch on the communication woth the server
     */
    public class MsgTask extends AsyncTask<Void, Void, JSONObject> {
        private String action;
        private boolean notify = false;
        private HashMapParser map;

        /*
        c'tor
         */
        MsgTask(String action, String sender, String msg, String t) {
            this.action = action;
            this.map = new HashMapParser();
            this.map.put("action", action);
            this.map.put("sender", sender);
            this.map.put("time", t);
            this.map.put("msg", msg);
        }

        public void setNotify(boolean toNot) {
            this.notify = toNot;
        }

        /*
        namedoInBackground
        desc:send the post request!!and return the json
         */
        @Override
        protected JSONObject doInBackground(Void... params) {
            PostMsg pm = new PostMsg(User.address + "MsgController", this.map);
            return pm.sendPostMsg();
        }

        /*
        name onPostExecute
        desc: take the json and act according to json answer
         */
        @Override
        protected void onPostExecute(final JSONObject json) {
            try {
                String s = json.getString("msgCtrl_result");
                //if a SELECT FROM MSG was activated
                if (s.equals("list")) {
                    //parse the json object to lst of msg & send to our chatArrayAdapter
                    JSONArray arr = (JSONArray) json.getJSONArray("list");
                    Timestamp firstMsgTime;
                    //adapt list - if its an empty list before
                    if (chatArrayAdapter.getList().size() == 0) {
                        Calendar calendar = Calendar.getInstance();
                        firstMsgTime = new java.sql.Timestamp(calendar.getTime().getTime());
                    } else {
                        firstMsgTime = chatArrayAdapter.getList().get(0).time;
                    }
                    //add msgs
                    List<ChatMessage> chatMessageList = chatArrayAdapter.ConvertJsonToList(arr,action,firstMsgTime);
                    boolean differ = false;
                    if(notify) {
                        List<ChatMessage> copy = chatArrayAdapter.Copy();
                        chatArrayAdapter.setList(chatMessageList,action);
                        //set a variable to know if a change was made in the list
                        differ = chatArrayAdapter.Differ(copy);
                    } else {
                        chatArrayAdapter.setList(chatMessageList, action);
                    }
                    //if i was aked to notify,and there are new msgs-or the list was changed
                    if (notify && differ) {
                        //set notifications properties
                        NotyAlram();
                        //if i was asked to notify but there are no new msg to load
                    } else if(notify) {
                        setNotifcat();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    /*
    name setNotifcat
     desc create an notification requset in "interval" milisecond
     */
    public void setNotifcat() {
        NotificationUpd.chat = Chat.this;
        AlarmManager alramMg = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Long timeTo = new GregorianCalendar().getTimeInMillis() + this.interval;
        Intent in = new Intent(this, NotificationUpd.class);
        alramMg.set(AlarmManager.RTC_WAKEUP, timeTo, PendingIntent.getBroadcast(
                                this, 0, in, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public void NotyAlram() {
        Context context = getApplicationContext();
        NotificationCompat.Builder mbuild = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("new meseges")
                .setContentText("new one")
                .setAutoCancel(true);
        Uri alarnSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mbuild.setSound(alarnSound);
        int notificationId = 001;
        Intent intent = new Intent(context, Chat.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
        mbuild.setContentIntent(pIntent);
        NotificationManager notMgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notMgr.notify(notificationId, mbuild.build());
        //start again the count untill next notification
        setNotifcat();
    }
}
