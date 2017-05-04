package com.zuzex.look2meet.DataModel;

import org.json.JSONObject;

/**
 * Created by dgureev on 9/30/14.
 */
public interface IUpdateStatus {
    /** Called when a response is received. */
    public void onUpdateSuccess(JSONObject response);
    public void onUpdateError();
}
