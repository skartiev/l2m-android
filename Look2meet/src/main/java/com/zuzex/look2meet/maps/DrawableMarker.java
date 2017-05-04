package com.zuzex.look2meet.maps;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by dgureev on 6/26/14.
 */
public class DrawableMarker {
    Context context;
    int textSize;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    Rect bounds;

    public DrawableMarker (Context context) {
        this.context = context;
        this.textSize = 0;
        this.bounds = new Rect();
    }

    public Bitmap drawCheckinsCount(Context context, int gResId, String male_count, String female_count) {
        female_count = fixOneSymbol(female_count);
        this.bounds = new Rect();
        Resources resources = context.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap_local = BitmapFactory.decodeResource(resources, gResId);
        android.graphics.Bitmap.Config bitmapConfig =
                bitmap_local.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap_local.copy(bitmapConfig, true);
        canvas = new Canvas(bitmap);
        // new antialised Paint
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(61, 61, 61));
        // text size in pixels

        calcTextSize(female_count);
        paint.setTextSize((int) (textSize * scale));
        int x = bitmap.getWidth()/3 - bounds.width()/2;
        int y = bitmap.getHeight()/2 + bounds.height()/4;
        canvas.drawText(female_count, x, y, paint);

        int x_male = bitmap.getWidth()/2 + bitmap.getWidth()/20;
        int y_male = bitmap.getHeight()/2 + bounds.height()/4;
        canvas.drawText(male_count, x_male, y_male, paint);
        return bitmap;
    }

    public Bitmap drawMultiObjectCount(Context context, int resourceId, String text) {
        text = fixOneSymbol(text);
        this.bounds = new Rect();
        Resources resources = context.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap_local = BitmapFactory.decodeResource(resources, resourceId);
        android.graphics.Bitmap.Config bitmapConfig = bitmap_local.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap_local.copy(bitmapConfig, true);
        canvas = new Canvas(bitmap);
        // new antialised Paint
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(255, 255, 255));
        // text size in pixels
        textSize = 16;
        paint.getTextBounds(text, 0, text.length(), bounds);
        if(bounds.width() >= bitmap.getWidth()/2) {
            while (bounds.width() >= bitmap.getWidth()/2) {
                textSize -=1;
                paint.setTextSize(textSize);
                paint.getTextBounds(text, 0, text.length(), bounds);
            }
        }
        paint.setTextSize((int) (textSize * scale));

        int x = bitmap.getWidth()/2 - bounds.width()/5;
        int y = bitmap.getHeight()/2 + bounds.height();
        canvas.drawText(text, x, y, paint);
        return bitmap;
    }

    void calcTextSize(String checkinsText) {
        textSize = 14;
        paint.getTextBounds(checkinsText, 0, checkinsText.length(), bounds);
        if(bounds.width() >= bitmap.getWidth()/3) {
            while (bounds.width() >= bitmap.getWidth()/3) {
                textSize = textSize - 1;
                paint.setTextSize(textSize);
                paint.getTextBounds(checkinsText, 0, checkinsText.length(), bounds);
            }
        }
    }

    String fixOneSymbol(String text) {
        if(text.length() == 1) {
            return " "+text;
        }
        return text;
    }
}
