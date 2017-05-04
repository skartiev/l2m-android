package com.zuzex.look2meet.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Created by dgureev on 10/1/14.
 */
public class StorageHelper {
    // Storage states
    private static boolean externalStorageAvailable, externalStorageWriteable;
    /**
     * Checks the external storage's state and saves it in member attributes.
     */
    private static void checkStorage() {
        // Get the external storage's state
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            // Storage is available and writeable
            externalStorageAvailable = externalStorageWriteable = true;
        } else if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            // Storage is only readable
            externalStorageAvailable = true;
            externalStorageWriteable = false;
        } else {
            // Storage is neither readable nor writeable
            externalStorageAvailable = externalStorageWriteable = false;
        }
    }

    /**
     * Checks the state of the external storage.
     *
     * @return True if the external storage is available, false otherwise.
     */
    public static boolean isExternalStorageAvailable() {
        checkStorage();
        return externalStorageAvailable;
    }

    /**
     * Checks the state of the external storage.
     *
     * @return True if the external storage is writeable, false otherwise.
     */
    public static boolean isExternalStorageWriteable() {
        checkStorage();
        return externalStorageWriteable;
    }

    /**
     * Checks the state of the external storage.
     *
     * @return True if the external storage is available and writeable, false
     *         otherwise.
     */
    public static boolean isExternalStorageAvailableAndWriteable() {
        checkStorage();
        if (!externalStorageAvailable) {
            return false;
        } else if (!externalStorageWriteable) {
            return false;
        } else {
            return true;
        }
    }

    public static File createMediaFile(Context context, final String dir, final String ext) {
        File mediaStorageDir;
        if(StorageHelper.isExternalStorageAvailableAndWriteable()) {
            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), dir);
        } else {
            mediaStorageDir = context.getDir(dir, Context.MODE_PRIVATE);
        }
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // For unique file name appending current timeStamp with file name
        java.util.Date date= new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date.getTime());
        // For unique video file name appending current timeStamp with file name
        return new File(mediaStorageDir.getPath() + File.separator + dir+ timeStamp + ext);
    }



}
