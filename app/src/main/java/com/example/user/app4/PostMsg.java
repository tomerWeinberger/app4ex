package com.example.user.app4;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostMsg {
    private String myUrl;
    private HashMapParser hmp;
    /*c'tor*/
    public PostMsg(String url,HashMapParser hmp) {
        this.hmp=hmp;
        this.myUrl=url;
    }
    /*
    name sendPostMsg
    desc : the func sends the posr request to server and return his answer
     */
    public JSONObject sendPostMsg() {
        HttpURLConnection urlConnection=null;
        try {
            //create url connection
            URL url = new URL(this.myUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            //send the POST out
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(this.hmp.Parse());
                out.close();
            //ask for the incoming answer
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();
                User.cookie = urlConnection.getHeaderField("Set-Cookie");
                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);
            //return answer as json
                return new JSONObject(responseStrBuilder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(urlConnection!=null)
                urlConnection.disconnect();
        }
        return null;
    }
}
