package com.example.user.app4;

/**
 * Created by user on 13/06/2016.
 */

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.codehaus.jackson.type.TypeReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
        username = extras.getString("name");
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
                                updateMessages(); //write what you want to do when you scroll up to the end of listview.
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
                            load();
                        }
                        last_x = x;
                        last_y = y;
                        last_z = z;
                    }
            }
        };
    }

    private void updateMessages(){
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
        mAuthTask = new MsgTask("from",this.username, chatText.getText().toString(),currentTimestamp);
    }

    private void load(){
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
        mAuthTask = new MsgTask("to",this.username, chatText.getText().toString(),currentTimestamp);
    }

    private boolean sendChatMessage() {
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
        mAuthTask = new MsgTask("save",this.username, chatText.getText().toString(),currentTimestamp);
        chatText.setText(" ");
        return true;
    }

    public class MsgTask extends AsyncTask<Void, Void, JSONObject> {
        private final String action;
        private final String sender;
        private final String msg;
        private final java.sql.Timestamp t;

        MsgTask(String action, String u, String m, java.sql.Timestamp t) {
            this.action = action;
            this.sender = u;
            this.t = t;
            this.msg = m;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                URL url = new URL("http://10.0.0.1:8080/Server/MsgController");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setReadTimeout(100000);
                urlConnection.setConnectTimeout(150000);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("action", this.action);
                urlConnection.setRequestProperty("sender", this.sender);
                urlConnection.setRequestProperty("time", this.t.toString());
                urlConnection.setRequestProperty("msg", this.msg);
                try {
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
                if (json.getString("msgCtrl_result") == "success") {
                    ChatMessage cm = new ChatMessage(this.sender, this.msg,this.t);
                    chatArrayAdapter.add(cm);
                } else {
                    try{
                        ObjectMapper jsonMapper = new ObjectMapper();
                        List<ChatMessage> l = jsonMapper.readValue(json.toString(), new TypeReference<List<ChatMessage>>(){});
                        chatArrayAdapter.clear();
                        chatArrayAdapter.addAll(l);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;

        }
    }
}
