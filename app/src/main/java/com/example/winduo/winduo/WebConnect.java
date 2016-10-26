package com.example.winduo.winduo;

import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class WebConnect extends AsyncTask<String, Void, MatrixCursor> {

    @Override
    protected MatrixCursor doInBackground(String... params){
        if (params[0].equals("1")) {//get all main chats
            return getAllMainChats(); //filter out subchats

        } else if (params[0].equals("2")) { //get all main messages
            return getAllMainMessages(params[1]);

        } else if (params[0].equals("3")) { //get all subchats
            return getAllSubChats(); //filter in only subchats of a main chat

        } else if (params[0].equals("4")) { //get all subchatmessages
            return  getAllSubChatMessages(params[1]);
        } else {
            throw new Error("incorrect parameters for doInBackground");
        }
    }

    private MatrixCursor getAllSubChatMessages(String subchatName) {
        String builder = "http://chat.wintonhackathon.com/rooms/";
        builder = builder.concat(subchatName);
        ArrayList<JSONObject> list = getInfo(builder);

        String[] columns = new String[] {"_id", "user", "message", "time", "chatname"};

        MatrixCursor cursor = new MatrixCursor(columns);
        if (list != null) {
            for (int i = 0; i < list.size() ; i++) {
                cursor.addRow(new Object[] {i, list.get(i+1).toString()} );
            }
        }
        return cursor;
    }

    private MatrixCursor getAllSubChats() {
        ArrayList<JSONObject> list = getInfo("http://chat.wintonhackathon.com/rooms");

        String[] columns = new String[] {"_id", "user", "message", "time", "chatname"};

        MatrixCursor cursor = new MatrixCursor(columns);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                try {
                    if (list.get(i).getJSONArray("room").toString().startsWith("sub_")) {
                        cursor.addRow(new Object[]{i, list.get(i + 1).toString()});
                    }
                } catch (JSONException e) {
                    //do nothing
                }
            }
        }
        return cursor;
    }

    private MatrixCursor getAllMainMessages(String chatName) {
        String builder = "http://chat.wintonhackathon.com/rooms/";
        builder = builder.concat(chatName);
        ArrayList<JSONObject> list = getInfo(builder);

        String[] columns = new String[] {"_id", "user", "message", "time", "chatname"};

        MatrixCursor cursor = new MatrixCursor(columns);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                cursor.addRow(new Object[]{i, list.get(i + 1).toString()});
            }
        }
        return cursor;
    }

    private MatrixCursor getAllMainChats() {
        ArrayList<JSONObject> list = getInfo("http://chat.wintonhackathon.com/rooms");

        String[] columns = new String[] {"_id", "groupchat_name"};

        MatrixCursor cursor = new MatrixCursor(columns);
        if (list != null) {

            for (int i = 0; i < list.size(); i++) {
                try {
                    if (!list.get(i).getJSONArray("room").toString().startsWith("sub_")) {
                        cursor.addRow(new Object[]{i, list.get(i + 1).toString()});
                    }
                } catch (JSONException e) {
                    //do nothing
                }
            }
        }
        return cursor;
    }

    private ArrayList<JSONObject> getInfo(String urlString) {
        JSONArray jsonArray = new JSONArray();
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            InputStream inStream;
            inStream = connection.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
            String temp, response = "";

            while ((temp = bReader.readLine()) != null) {
                response += temp;
            }

            bReader.close();
            inStream.close();
            connection.disconnect();

            jsonArray = new JSONArray(response.substring(response.indexOf("["), response.lastIndexOf("]") + 1));


            ArrayList<JSONObject> list = new ArrayList<>();
            if (jsonArray != null) {
                int len = jsonArray.length();
                for (int i=0;i<len;i++){
                    list.add(jsonArray.getJSONObject(i));
                }
                Log.d("TEST:", jsonArray.toString());
            }
            return list;
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }
    
    private void convertObjToArr(JSONArray obj, ArrayList<JSONObject> list) {
        for (int i = 0; i < obj.length() ; i++) {
            try {
                list.add(obj.getJSONObject(i));
            } catch (Exception e) {
                Log.e("couldnt convert!",e.getMessage(), e);
            }
        }
    }
}
