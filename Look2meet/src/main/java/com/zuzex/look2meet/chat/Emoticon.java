package com.zuzex.look2meet.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

public class Emoticon {
    public String text;
    public String url;
    public Drawable drawable;
    private static ArrayList<Emoticon> emoticonsObjects;

    public Emoticon(String text, String url) {
        this.text = text;
        this.url = url;
        drawable = null;
    }

    private static void loadImages(final Context context, ArrayList<Emoticon> emoticons) {
        for(final Emoticon emoticon: emoticons) {
            ImageLoader.getInstance().loadImage(emoticon.url, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }
                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    emoticon.drawable = new BitmapDrawable(context.getResources(),loadedImage);
                    float density = context.getResources().getDisplayMetrics().density;
                    int size = (int)density*20;
                    emoticon.drawable.setBounds(0, 0, size, size);
                }
                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
        }
    }

    public static void setup(final Context context) {
        emoticonsObjects = new ArrayList<Emoticon>();
        emoticonsObjects.add(new Emoticon(":-)", "http://look2meet.com/assets/img/smileset1/face-smile.png"));
        emoticonsObjects.add(new Emoticon(":-(", "http://look2meet.com/assets/img/smileset1/face-sad.png"));
        emoticonsObjects.add(new Emoticon(";-)", "http://look2meet.com/assets/img/smileset1/face-wink.png"));
        emoticonsObjects.add(new Emoticon(":D", "http://look2meet.com/assets/img/smileset1/face-grin.png"));
        emoticonsObjects.add(new Emoticon(":O", "http://look2meet.com/assets/img/smileset1/face-surprise.png"));
        emoticonsObjects.add(new Emoticon("(6)", "http://look2meet.com/assets/img/smileset1/face-devilish.png"));
        emoticonsObjects.add(new Emoticon("(A)", "http://look2meet.com/assets/img/smileset1/face-angel.png"));
        emoticonsObjects.add(new Emoticon(":|", "http://look2meet.com/assets/img/smileset1/face-plain.png"));
        emoticonsObjects.add(new Emoticon(":o)", "http://look2meet.com/assets/img/smileset1/face-smile-big.png"));
        emoticonsObjects.add(new Emoticon("8)", "http://look2meet.com/assets/img/smileset1/face-glasses.png"));
        emoticonsObjects.add(new Emoticon("(K)", "http://look2meet.com/assets/img/smileset1/face-kiss.png"));
        emoticonsObjects.add(new Emoticon("(M)", "http://look2meet.com/assets/img/smileset1/face-monkey.png"));
        loadImages(context, emoticonsObjects);
    }

    public static ArrayList<Emoticon> getEmoticons() {
        return emoticonsObjects;
    }

    public static Emoticon findEmoticonWithUrl(String url) {
        for (Emoticon emoticon: emoticonsObjects) {
            if(emoticon.url.equals(url)) {
                return emoticon;
            }
        }
        return null;
    }
}
