package com.zuzex.look2meet.Log;

import android.app.ActivityManager;
import android.os.Build;

/**
 * Created by sanchirkartiev on 31.07.14.
 */
public class SystemInfo {
    String osVersion;
    Long freeMemory;
    Long freeStorageMemory;
    Integer id;
    String message;

    public SystemInfo(Integer id, String message)
    {
        osVersion = "Android: " + Build.VERSION.RELEASE;
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        freeMemory = memoryInfo.availMem;
        Runtime runtime = Runtime.getRuntime();
        freeStorageMemory = runtime.freeMemory();
        this.id = id;
        this.message = message;
    }
    public String getString()
    {
       return osVersion + " " +freeMemory.toString()+" "+freeStorageMemory.toString()+" "+id.toString()+" "+message;
    }
}
