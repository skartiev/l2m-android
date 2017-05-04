package com.zuzex.look2meet.utils;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dgureev on 9/25/14.
 */
public class EscapedJSONObject extends JSONObject {

    public EscapedJSONObject(String jsonString) throws JSONException {
        super(jsonString);
    }

    @Override
    public String optString(String name) {
      return StringEscapeUtils.unescapeHtml(super.optString(name));
    };

    @Override
    public String optString(String name, String fallback) {
        return StringEscapeUtils.unescapeHtml(super.optString(StringEscapeUtils.unescapeHtml(name), fallback));
    };
}
