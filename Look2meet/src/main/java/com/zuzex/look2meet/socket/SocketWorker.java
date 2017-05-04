package com.zuzex.look2meet.socket;

/**
 * Created by romanabashin on 10.07.14.
 */

import android.content.Intent;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.zuzex.look2meet.DataModel.Organization;
import com.zuzex.look2meet.DataModel.UserProfile;
import com.zuzex.look2meet.Look2meet;
import com.zuzex.look2meet.api.Look2meetApi;
import com.zuzex.look2meet.chat.ChatMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import io.socket.IOAcknowledge;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

public class SocketWorker implements SocketThreadCallbackAdapter, Action1 {

    private static String TAG = "SocketWorker";

    private volatile static SocketWorker instance;

    public boolean isConnected = false;

    private String idUser;
    private String authToken;

    /** Returns singleton class instance */
    public static SocketWorker getInstance() {
        if (instance == null) {
            synchronized (SocketWorker.class) {
                if (instance == null) {
                    instance = new SocketWorker();
                }
            }
        }
        return instance;
    }

    //========== ========== ========== socket.io ========== ========== ==========

    public SocketThread socketThread;
    private Subscription subscription;
    private SocketThreadCallbackAdapter adapter;

    public void connectToSocket(int idUser, String authToken) {
        connectToSocket(idUser, authToken, null);
    }

    public void setSocketCallBackAdapter(SocketThreadCallbackAdapter adapter) {
        this.adapter = adapter;
    }

    public void connectToSocket(Integer idUser, String authToken, SocketThreadCallbackAdapter adapter) {
        Log.w(TAG, "Connect "+idUser+" "+authToken);
        this.idUser = idUser.toString();
        this.authToken = authToken;
        this.adapter = adapter;
   		if (socketThread == null) {
            socketThread = new SocketThread(this);
            socketThread.idUser = this.idUser;
            socketThread.authToken = this.authToken;
            socketThread.start();
        } else {
		    socketThread.idUser = this.idUser;
		    socketThread.authToken = this.authToken;
		    socketThread.reconnect();
        }
    }

    public void disconnect() {
        if(socketThread != null) {
            socketThread.disconnect();
        }
    }

    SocketThread getSocketThread() {
        if(socketThread == null) {
            socketThread = new SocketThread(this);
            socketThread.idUser = idUser.toString();
            socketThread.authToken = authToken;
            socketThread.start();
        }
        return socketThread;
    }

	public void getDialogHistory(Integer dialogId, IOAcknowledge ack) {
		JSONObject json = new JSONObject();
		try {
			json.accumulate("dialogId",dialogId.toString());
			json.accumulate("getAll","1");
            getSocketThread().sendEvent("getDialog", json, ack);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	public void sendMessageToChat(Integer dialogId, String text, JSONArray files, String giftUrl, IOAcknowledge ack) {
        JSONObject json = new JSONObject();
        try {
            json.accumulate("dialogId",dialogId.toString());
			json.accumulate("date",System.currentTimeMillis());
			json.accumulate("text",text);
			json.accumulate("isAdd", "0");
            if (files != null) {
                json.accumulate("files", files);
            }
            if (giftUrl != null && giftUrl.length() > 0) {
                json.accumulate("giftUrl", giftUrl);
            }
            getSocketThread().sendEvent("message",json,ack);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCoordsUpdateWithCallBack(double latCoord, double longCoord,IOAcknowledge ack) {
        JSONObject json = new JSONObject();
        try {
            json.accumulate("lat",Double.toString(latCoord));
            json.accumulate("long",Double.toString(longCoord));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getSocketThread().sendEvent("message",json,ack);
//        Log.w("Coordinates recieved","Recieved");
    }

    public void exitDialog(final int dialogId) {
//        {"args":[{"id":10}],"name":"exitDialog"}
        JSONObject body = new JSONObject();
        try {
            body.accumulate("id", dialogId);
//            Log.w("CHAT", "exit dialog " + body.toString());
            getSocketThread().sendEvent("exitDialog", body, new IOAcknowledge() {
                @Override
                public void ack(Object... args) {

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void on(String event, JSONObject obj) {
        //D/on﹕ message | {"files":[],"text":" testo","toUser":"7","event":"dialog","isAdd":0,"idUser":"6","date":1.404919088452575E12,"success":1,"dialogId":4}
//        Log.i("socket.io-res","on " + event + " | " + obj.toString());
        if ("message".equalsIgnoreCase(event)) {
	        ChatMessage message = new ChatMessage(obj);
            Intent intent;
            intent = new Intent("reciveChatMessage");
            intent.putExtra("message", message);
            LocalBroadcastManager.getInstance(Look2meet.getContext()).sendBroadcast(intent);
        }
    }

    @Override
    public void callback(JSONArray data) throws JSONException {
        if(adapter != null)
            adapter.callback(data);
    }

    @Override
    public void onMessage(String message) {
    }

    @Override
    public void onMessage(JSONObject json) {
        try {
            JSONObject orgJson = json.getJSONObject("response");
            Organization org = new Organization();
            org.UpdateFromJson(orgJson);
            Intent intent = new Intent("sendObjectAnons");
            intent.putExtra("organization", org);
            LocalBroadcastManager.getInstance(Look2meet.getContext()).sendBroadcast(intent);
        } catch (JSONException e) {
        }
    }

    @Override
    public void onConnect() {
        if(adapter != null)
            adapter.onConnect();
        if(Look2meet.locationService != null)
            try {
                subscription = Look2meet.locationService.subscribeToLocation().subscribe(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        isConnected = true;
    }

    @Override
    public void onDisconnect() {
        if(adapter != null)
            adapter.onDisconnect();
        Log.w(TAG, "disconnect");
        if(subscription != null) {
            subscription.unsubscribe();
        }
        isConnected = false;
    }

    @Override
    public void onConnectFailure() {
        Log.e(TAG, "CONNECTION FAILURE");
        if(adapter != null)
            adapter.onConnectFailure();
        isConnected = false;
        final Scheduler.Worker worker;
        worker = AndroidSchedulers.mainThread().createWorker();
        worker.schedule(new Action0() {
            @Override
            public void call() {
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        connectToSocket(UserProfile.getInstance().id, Look2meetApi.getInstance().getApiToken());
                        worker.unsubscribe();
                    }
                }, 5000);
            }
        });
    }

    @Override
    public void call(Object o) {
        Location location = (Location) o;
//        Log.w("Socket updated", "LOCATION");
        if(location != null) {
            sendCoordsUpdateWithCallBack(location.getLatitude(), location.getLongitude(), new IOAcknowledge() {
                @Override
                public void ack(Object... args) {

                }
            });
        }
    }


}
