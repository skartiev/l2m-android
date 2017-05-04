package com.zuzex.look2meet.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.io.File;

/**
 * Created by dgureev on 10/2/14.
 */
public class BitmapResizer {
    public static Bitmap scale(Context context, final File file) {
        Bitmap d = new BitmapDrawable(context.getResources(), file.getAbsolutePath()).getBitmap();
        int nh = (int) (d.getHeight() * (256.0 / d.getWidth()));
        Bitmap scaled = Bitmap.createScaledBitmap(d, 256, nh, true);
        return scaled;
    }
}
