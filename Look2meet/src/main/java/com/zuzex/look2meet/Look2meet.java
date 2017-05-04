package com.zuzex.look2meet;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.api.Look2meetApi;
import com.zuzex.look2meet.maps.LocationService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;


/**
 * Created by dgureev on 7/2/14.
 */
public class Look2meet extends Application {
	private static Context sContext;
	public static Context getContext() {
		return sContext;
	}
    public Look2meetApi api;
    public static LocationService locationService;
    public static boolean checkGps = true;

    @Override
    public void onCreate() {
        super.onCreate();
        DisplayImageOptions options = new DisplayImageOptions.Builder()
            .cacheOnDisk(true)
            .showImageOnLoading(R.drawable.no_image)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .imageScaleType(ImageScaleType.EXACTLY)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(options)
                .imageDownloader(new CustomImageDownaloder(getApplicationContext()))
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .memoryCache(new WeakMemoryCache())
                .denyCacheImageMultipleSizesInMemory()
                .memoryCacheSize(1048576 * 10) //10 mb
                        .build();
        ImageLoader.getInstance().init(config);

	    //access to app context
	    sContext = getApplicationContext();
        locationService = new LocationService(sContext);
        YandexMetrica.initialize(getApplicationContext(), getResources().getString(R.string.yandex_metrica_key));
        YandexMetrica.setReportCrashesEnabled(true);
        YandexMetrica.setReportsEnabled(true);
        YandexMetrica.setTrackLocationEnabled(true);
    }

    private class CustomImageDownaloder extends BaseImageDownloader {

        public CustomImageDownaloder(Context context) {
            super(context);
        }

        @Override
        protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
            HttpURLConnection conn = super.createConnection(url, extra);
            Map<String, String> headers = (Map<String, String>) extra;
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    conn.setRequestProperty(header.getKey(), header.getValue());
                }
            }
            return conn;
        }
    }


}
