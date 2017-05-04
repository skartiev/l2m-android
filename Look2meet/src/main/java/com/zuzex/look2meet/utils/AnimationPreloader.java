package com.zuzex.look2meet.utils;

/**
 * Created by dgureev on 6/2/14.
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

import com.zuzex.look2meet.R;

public class AnimationPreloader {
    private ProgressDialog progressDialog;
    Context context;
    private Handler handler;
    private AlertDialog alertDialog;

    public AnimationPreloader(Context context, ProgressDialog.OnCancelListener cancelListener) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(true);
        progressDialog.setMessage(createMessage());
        progressDialog.setOnCancelListener(cancelListener);
        handler = createHandler();
        alertDialog = null;
    }

    public AnimationPreloader(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(createMessage());
        progressDialog.setCancelable(false);
        handler = createHandler();
    }

    public AnimationPreloader(Context context, int STYLE) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(STYLE);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        progressDialog.setMessage(createMessage());
        handler = createHandler();

    }

    public Handler createHandler() {
        return new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                int progress = message.what;
                if(progressDialog.getProgress() < progressDialog.getMax()) {
                    progressDialog.setProgress(progress);
                } else  {
                    done();
                }
                return false;
            }
        });
    }

    public SpannableString createMessage() {
        SpannableString msg = new SpannableString(context.getString(R.string.please_wait));
        msg.setSpan(new RelativeSizeSpan(1.5f), 0, msg.length(), 0);
        return  msg;
    }

    public void launch() {
        if(!progressDialog.isShowing()) {
            progressDialog.show();
            progressDialog.setMessage(createMessage());
        }
    };



    public void done() {
        if(progressDialog!=null) {
            progressDialog.setProgress(0);
            progressDialog.setMessage(createMessage());
            progressDialog.dismiss();
            progressDialog.hide();
        }
    }

    public void close() {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
        if(alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    public void cancel(String headerMessage, String bodyMessage) {
        done();
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                alertBuilder.setTitle(headerMessage);
                alertBuilder.setMessage(bodyMessage);
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    public void cancel(String headerMessage, String bodyMessage, DialogInterface.OnDismissListener dismissListener) {
        progressDialog.dismiss();
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setTitle(headerMessage);
        alertBuilder.setMessage(bodyMessage);
        AlertDialog alert = alertBuilder.create();
        alert.setOnDismissListener(dismissListener);
        alert.show();
    }

    public void setStyle(final int STYLE) {
        progressDialog.setProgressStyle(STYLE);
    }

    public void setProgress(int percent) {
        handler.sendEmptyMessage(percent);
    }
}
