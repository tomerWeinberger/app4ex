package com.example.user.app4;

/**
 * Created by user on 13/06/2016.
 */

import android.app.Activity;
import android.app.AlarmManager;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.google.android.gms.common.api.GoogleApiClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
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


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(listener,accelometer,
                SensorManager.SENSOR_DELAY_GAME);
        mLastFirstVisibleItem = listView.getFirstVisiblePosition();
        chatArrayAdapter.initializetoSee();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(listener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        setContentView(R.layout.activity_chat);
        Bundle extras = getIntent().getExtras();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        username = settings.getString("username", "");
        buttonSend = (Button) findViewById(R.id.send);
        listView = (ListView) findViewById(R.id.msgview);
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.msg);
        listView.setAdapter(chatArrayAdapter);
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

        //listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        //listView.setAdapter(chatArrayAdapter);

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
                return;
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
        setNotifcat();
    }



    public void updateMessages(String choice){
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
        mAuthTask = new MsgTask("from",this.username, chatText.getText().toString(),currentTimestamp.toString());
        if(choice.equals("time"))
            mAuthTask.setNotify(true);
        mAuthTask.execute();
    }

    public void loadTenMore(){
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
        mAuthTask = new MsgTask("to",this.username, chatText.getText().toString(),currentTimestamp.toString());
        mAuthTask.execute();
    }

    private boolean sendChatMessage() {
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
        mAuthTask = new MsgTask("save",this.username, chatText.getText().toString(),currentTimestamp.toString());
        mAuthTask.execute();
        chatText.setText(" ");
        return true;
    }

    public class MsgTask extends AsyncTask<Void, Void, JSONObject> {
        private String sender;
        private String msg;
        private String action;
        private String time;

        private boolean notify = false;
        private HashMapParser map;
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
        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                URL url = new URL("http://172.18.13.47:8080/Server/MsgController");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                try {
                    //send the POST out
                    PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                    out.print(this.map.Parse());
                    out.close();

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    StringBuilder responseStrBuilder = new StringBuilder();
                    User.cookie = urlConnection.getHeaderField("Set-Cookie");
                    String inputStr;
                    while ((inputStr = streamReader.readLine()) != null)
                        responseStrBuilder.append(inputStr);
                    return new JSONObject(responseStrBuilder.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(final JSONObject json) {
            try {
                String s = json.getString("msgCtrl_result");
                if(s.equals("list")){
                    JSONArray arr = (JSONArray) json.getJSONArray("list");
                    chatArrayAdapter.addTenTolist();
                    chatArrayAdapter.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject j = arr.getJSONObject(i);
                        String msg = j.getString("msg");
                        String sender = j.getString("sender");
                        String time = j.getString("time");
                        chatArrayAdapter.add(new ChatMessage(sender,msg,time));
                    }
                    if(notify == true){
                        Context context = getApplicationContext();
                        NotificationCompat.Builder mbuild = new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.livechat)
                                .setContentTitle("new meseges")
                                .setContentText("new one");
                        int notificationId = 001;
                        NotificationManager notMgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
                        notMgr.notify(notificationId,mbuild.build());
                        setNotifcat();
                    }
                } else if (s.equals("success")) {
                    ChatMessage cm = new ChatMessage(sender, msg, time);
                    chatArrayAdapter.add(cm);
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
    public void setNotifcat(){
        NotificationUpd.chat = this;
        AlarmManager alramMg = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Long timeTo = new GregorianCalendar().getTimeInMillis() + 5000;
        Intent in = new Intent(this,NotificationUpd.class);
        alramMg.set(AlarmManager.RTC_WAKEUP,timeTo, PendingIntent.getBroadcast(this,0,in,PendingIntent.FLAG_UPDATE_CURRENT));
    }
}
