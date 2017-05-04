package com.zuzex.look2meet.media;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.zuzex.look2meet.utils.StorageHelper;

import java.io.File;

/**
 * Created by dgureev on 9/30/14.
 */

enum CONTENT_TYPE {
    VIDEO,
    PHOTO
}

public abstract class CaptureMedia extends Activity{
    private static String PROJECT_NANE = "Look2meet";
    protected CONTENT_TYPE content_type;
    protected static final int REQUEST_IMAGE_FROM_CAMERA = 1;
    protected static final int REQUEST_VIDEO_FROM_CAMERA = 2;
    protected static final int REQUEST_IMAGE_FROM_GALLERY = 3;
    protected static final int REQUEST_VIDEO_FROM_GALLERY = 4;
    protected File mFile = null;

    abstract protected void onActivityResult(int requestCode, int resultCode, Intent data);

    public void init(CONTENT_TYPE contentType) {
        this.content_type = contentType;
    }

    // capture from camera
    //========== ========== ========== ========== ========== ========== ========== ========== ========== ==========

    public void captureFromCamera() {
        switch (content_type) {
            case VIDEO:
                captureVideoFromCamera();
                break;
            case PHOTO:
                capturePhotoFromCamera();
                break;
            default:
                return;
        }
    }

    protected void captureVideoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        mFile = StorageHelper.createMediaFile(this, PROJECT_NANE, ".mp4");
        if(mFile == null)
            return;
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFile));
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, REQUEST_VIDEO_FROM_CAMERA);
    }

    protected void capturePhotoFromCamera() {
        mFile = StorageHelper.createMediaFile(this, PROJECT_NANE, ".jpg");
        if(mFile == null)
            return;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFile));
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_FROM_CAMERA);
        }
    }

    // capture from gallery
    //========== ========== ========== ========== ========== ========== ========== ========== ========== ==========
    protected void captureFromGallery() {
        switch (content_type) {
            case VIDEO:
                captureVideoFromGallery();
                break;
            case PHOTO:
                capturePhotoFromGallery();
                break;
            default:
                return;
        }
    }

    protected void capturePhotoFromGallery() {
        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_IMAGE_FROM_GALLERY);
    }

    protected void captureVideoFromGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("video/*");
        startActivityForResult(i, REQUEST_VIDEO_FROM_GALLERY);
    }
}
