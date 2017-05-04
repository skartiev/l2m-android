package com.zuzex.look2meet.socket;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import io.socket.IOAcknowledge;
import io.socket.SocketIO;


public class SocketThread extends Thread {
    private SocketIO socket;
    private SocketThreadCallback callback;

    public  String authToken;
    public  String idUser;

    public SocketThread(SocketThreadCallbackAdapter callback) {
        this.callback = new SocketThreadCallback(callback);
    }
    
    @Override
    public void run() {
        connect();
    }

    public void disconnect() {
        socket.disconnect();
    }

    private void connect() {
        if (socket == null || !socket.isConnected()) {
            try {
                URL socketUrl = new URL("http://look2meet.com:80/");
                socket = new SocketIO(socketUrl);
                socket.addHeader("authToken",authToken);
                socket.addHeader("idUser",idUser);
                socket.connect(callback);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void reconnect() {
        if (socket != null && socket.isConnected()) {
            socket.disconnect();
            socket = null;
        }
        connect();
    }

    public void sendEvent(String eventName, JSONObject json, IOAcknowledge ack) {
//	    ack.ack("[[{\"userFromId\":\"6\"}]]");
        socket.emit(eventName, ack, new Object[]{json});
//        socket.emit(eventName, ack, json.toString());
    }


//    public void disconnect() {
//        socket.disconnect();
//    }

//    public void sendMessage(String message) {
//        try {
//            JSONObject json = new JSONObject();
//            json.putOpt("message", message);
//            socket.emit("user message", json);
//        } catch (JSONException ex) {
//            ex.printStackTrace();
//        }
//    }
    
//    public void join(String nickname) {
//        try {
//            JSONObject json = new JSONObject();
//            json.putOpt("nickname", nickname);
//            socket.emit("nickname", callback, json);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
}
