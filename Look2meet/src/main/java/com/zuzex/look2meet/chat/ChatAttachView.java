package com.zuzex.look2meet.chat;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.zuzex.look2meet.DataModel.MediaUploadResponse;
import com.zuzex.look2meet.R;
import com.zuzex.look2meet.api.GlobalHelper;
import com.zuzex.look2meet.api.Look2meetApi;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by dgureev on 10/1/14.
 */
public class ChatAttachView extends RelativeLayout {
    private static final String TAG = "ChatAttachView";
    public File currentFile;
    public ImageView imageView;
    public Button closeButton;
    public ProgressBar progressBar;
    private Handler handler;
    public MediaUploadResponse mediaUploadResponse;
    public boolean isUploaded = false;
    RelativeLayout.LayoutParams relParam;

    public ChatAttachView(Context context) {
        super(context);
        handler = new Handler();
        initView();
    }

    private void initView() {
        View view = inflate(getContext(), R.layout.activity_chat_attachment, null);
        addView(view);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        closeButton = (Button) view.findViewById(R.id.closeButton);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
    }

    public void startUpload(final File currentFile) {
        if (currentFile == null) {
            return;
        }
        progressBar.setProgress(0);
        progressBar.setVisibility(VISIBLE);
        isUploaded = false;
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                int progress = message.what;
                progressBar.setProgress(progress);
                return false;
            }
        });
        Look2meetApi.getInstance().upload2(getContext(), currentFile, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (GlobalHelper.successStatusFromJson(response, true, null)) {
                    progressBar.setProgress(100);
                    progressBar.setVisibility(GONE);
                    mediaUploadResponse = new MediaUploadResponse(response);
                    isUploaded = true;
                }
            }
            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                int progress = (int)((bytesWritten * 1.0 / totalSize) * 100);
                handler.sendEmptyMessage(progress);
            }
        });
    }

    public static ChatAttachView createChatAttachView(final Context context) {
        ChatAttachView holder = new ChatAttachView(context);
        holder.relParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        return holder;
    }
}