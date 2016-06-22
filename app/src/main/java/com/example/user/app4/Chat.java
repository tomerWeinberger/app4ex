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
import com.google.android.gms.common.api.GoogleApiClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Chat extends Activity {

    private static final String TAG = "ChatActivity";
    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private String username;
    private SensorManager sensorManager;
    private SensorEventListener listener;
    private Sensor accelometer;
    private long lastUpdate;
    private float last_x,last_y,last_z;
    private static final int SHAKE_THRESHOLD = 800;
    private int mLastFirstVisibleItem;
    private boolean update;
    private MsgTask mAuthTask;
    private GoogleApiClient client;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private final long interval=5000;//300000;

    /*
    name:onResume
    desc:this func activates once you resume activity
     */
    @Override
    protected void onResume() {
        super.onResume();
        //register to shake listener
        sensorManager.registerListener(listener,accelometer,
                SensorManager.SENSOR_DELAY_GAME);
        mLastFirstVisibleItem = listView.getFirstVisiblePosition();//dont need if refres works
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
        super.onPause();
        sensorManager.unregisterListener(listener);
    }

    /*
    name:onCreate
    desc:initialize important vars
    */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get the service for shake option
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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
        chatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });
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

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            int currentFirstVisibleItem;
            int currentVisibleItemCount;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int currentFirstVisibleItem = listView.getFirstVisiblePosition();
                if (currentFirstVisibleItem <= mLastFirstVisibleItem) {
                    if (currentVisibleItemCount > 0 && scrollState == SCROLL_STATE_IDLE) {
                        if (currentFirstVisibleItem == 0) {
                            if(update)
                                loadTenMore(); //write what you want to do when you scroll up to the end of listview.
                            else
                                update=true;
                        }
                    } else {
                        listView.scrollBy(0,10);
                    }
                } else {
                    update = false;
                }
                mLastFirstVisibleItem = currentFirstVisibleItem;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                currentFirstVisibleItem = firstVisibleItem;
                currentVisibleItemCount = visibleItemCount;
            }
        });

        listener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                //i dont need this function
                long curTime = System.currentTimeMillis();
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() != SensorManager.SENSOR_ACCELEROMETER) return;
                    long curTime = System.currentTimeMillis();
                    // only allow one update every 100ms.
                    if ((curTime - lastUpdate) > 100) {
                        long diffTime = (curTime - lastUpdate);
                        lastUpdate = curTime;

                        float x = event.values[SensorManager.DATA_X];
                        float y = event.values[SensorManager.DATA_Y];
                        float z = event.values[SensorManager.DATA_Z];

                        float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;

                        if (speed > SHAKE_THRESHOLD) {
                            updateMessages("shake");
                        }
                        last_x = x;
                        last_y = y;
                        last_z = z;
                    }
            }
        };
        mSwipeRefreshLayout= (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        //get current time
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
        //ask for msg up to this time
        mAuthTask = new MsgTask("to",this.username, " ",currentTimestamp.toString());
        mAuthTask.execute();
        setNotifcat();
    }


    /*
    * name updateMessages
    * desc: the func asks for an update of new msgs
    * */
    public void updateMessages(String choice){
        //get current time
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
        //ask for msg from this time
        mAuthTask = new MsgTask("from",this.username, chatText.getText().toString(),currentTimestamp.toString());
        //if action was asked not by user(but by time defs)
        if(choice.equals("time"))
            mAuthTask.setNotify(true);
        mAuthTask.execute();
    }

    /*
    name loadTenMore
    dsc : the func asks for ten older msg
     */
    public void loadTenMore(){
        //get current time
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
        //ask for msg up to this time
        mAuthTask = new MsgTask("to",this.username, chatText.getText().toString(),currentTimestamp.toString());
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
        ChatMessage cm = new ChatMessage(this.username, chatText.getText().toString(),currentTimestamp.toString());
        chatArrayAdapter.add(cm);
        //ask for server to save the msg in DB
        mAuthTask = new MsgTask("save",this.username, chatText.getText().toString(),currentTimestamp.toString());
        mAuthTask.execute();
        chatText.setText(" ");
        return true;
    }

    /*
    this class is in charch on the communication woth the server
     */
    public class MsgTask extends AsyncTask<Void, Void, JSONObject> {
        private String sender;
        private String msg;
        private String action;
        private String time;
        private boolean notify = false;
        private HashMapParser map;
        /*
        c'tor
         */
        MsgTask(String action, String sender, String msg, String t) {
            this.map = new HashMapParser();
            this.map.put("action", action);
            this.map.put("sender", sender);
            this.map.put("time", t);
            this.map.put("msg", msg);
            this.sender = sender;
            this.time=t;
            this.msg=msg;
        }

        public void setNotify(boolean toNot){
            this.notify = toNot;
        }

        /*
        namedoInBackground
        desc:send the post request!!and return the json
         */
        @Override
        protected JSONObject doInBackground(Void... params) {
            PostMsg pm = new PostMsg(User.address+"MsgController",this.map);
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
                int prevSize = chatArrayAdapter.getCount();
                //if a SELECT FROM MSG was activated
                if(s.equals("list")){
                    //parse the json object to lst of msg & send to our chatArrayAdapter
                    JSONArray arr = (JSONArray) json.getJSONArray("list");
                    chatArrayAdapter.addTenTolist();
                    chatArrayAdapter.clear();
                    //add msgs
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject j = arr.getJSONObject(i);
                        String msg = j.getString("msg");
                        String sender = j.getString("sender");
                        String time = j.getString("time");
                        chatArrayAdapter.add(new ChatMessage(sender,msg,time));
                    }
                    //if i was aked to notify,and there are new msgs
                    if(notify && chatArrayAdapter.getCount() > prevSize) {
                        //set notifications properties
                        NotyAlram();
                    }
                    //if INSERT TO MSG was activated
                } else if (s.equals("success")) {
                    ;
                }
            }catch(Exception e){
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
    public void setNotifcat(){
        NotificationUpd.chat = Chat.this;
        AlarmManager alramMg = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Long timeTo = new GregorianCalendar().getTimeInMillis() + this.interval;
        Intent in = new Intent(this,NotificationUpd.class);
        alramMg.set(AlarmManager.RTC_WAKEUP,timeTo, PendingIntent.getBroadcast(this,0,in,PendingIntent.FLAG_UPDATE_CURRENT));
    }
    public void NotyAlram(){
        Context context = getApplicationContext();
        NotificationCompat.Builder mbuild = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("new meseges")
                .setContentText("new one")
                .setAutoCancel(true);
        Uri alarnSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mbuild.setSound(alarnSound);
        int notificationId = 001;
        Intent intent = new Intent(context,Chat.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(context,0,intent,0);
        mbuild.setContentIntent(pIntent);
        NotificationManager notMgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notMgr.notify(notificationId, mbuild.build());
        //start again the count untill next notification
        setNotifcat();
    }
}
