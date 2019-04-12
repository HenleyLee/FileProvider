package com.henley.fileprovider.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.henley.fileprovider.Android7Helper;
import com.henley.fileprovider.ImageUriHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_PERMISSION = 0x100;
    private static final int REQUEST_CODE_CROP = 0x110;
    private static final int REQUEST_CODE_CAPTURE = 0x111;
    private static final int REQUEST_CODE_CAPTURE_CROP = 0x112;
    private static final int REQUEST_CODE_GALLERY = 0x113;
    private static final int REQUEST_CODE_GALLERY_CROP = 0x114;
    private static final int REQUEST_CODE_DOCUMENTS = 0x115;
    private static final int REQUEST_CODE_DOCUMENTS_CROP = 0x116;

    private Toast toast;
    private String mPhotoPath;
    private ImageView ivDisplay;
    private View mCurrentView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_installapk).setOnClickListener(this);
        findViewById(R.id.btn_capture).setOnClickListener(this);
        findViewById(R.id.btn_capture_crop).setOnClickListener(this);
        findViewById(R.id.btn_gallery).setOnClickListener(this);
        findViewById(R.id.btn_gallery_crop).setOnClickListener(this);
        findViewById(R.id.btn_documents).setOnClickListener(this);
        findViewById(R.id.btn_documents_crop).setOnClickListener(this);
        ivDisplay = (ImageView) findViewById(R.id.iv_display_result);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            boolean success = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    success = false;
                }
            }
            if (success) {
                mCurrentView.performClick();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent;
        Log.i("TAG", "data = " + (data == null ? null : data.toString()));
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_CROP:
                    setImageBitmap();
                    break;
                case REQUEST_CODE_CAPTURE:
                    setImageBitmap();
                    break;
                case REQUEST_CODE_CAPTURE_CROP:
                    Uri inputUri = Android7Helper.getUriForFile(this, new File(mPhotoPath));
                    mPhotoPath = getFilePath();
                    intent = Android7Helper.getIntentWithCrop(this, inputUri, new File(mPhotoPath));
                    startActivityForResult(intent, REQUEST_CODE_CROP);
                    break;
                case REQUEST_CODE_GALLERY:
                    if (data == null || data.getData() == null) {
                        showToast("获取图片失败");
                        return;
                    }
                    mPhotoPath = ImageUriHelper.getFilePathFromUri(this, data.getData());
                    setImageBitmap();
                    break;
                case REQUEST_CODE_GALLERY_CROP:
                    if (data == null || data.getData() == null) {
                        showToast("获取图片失败");
                        return;
                    }
                    mPhotoPath = getFilePath();
                    intent = Android7Helper.getIntentWithCrop(this, data.getData(), new File(mPhotoPath));
                    startActivityForResult(intent, REQUEST_CODE_CROP);
                    break;
                case REQUEST_CODE_DOCUMENTS:
                    if (data == null || data.getData() == null) {
                        showToast("获取图片失败");
                        return;
                    }
                    mPhotoPath = ImageUriHelper.getFilePathFromUri(this, data.getData());
                    setImageBitmap();
                    break;
                case REQUEST_CODE_DOCUMENTS_CROP:
                    if (data == null || data.getData() == null) {
                        showToast("获取图片失败");
                        return;
                    }
                    mPhotoPath = getFilePath();
                    intent = Android7Helper.getIntentWithCrop(this, data.getData(), new File(mPhotoPath));
                    startActivityForResult(intent, REQUEST_CODE_CROP);
                    break;
            }
        }

    }

    private void setImageBitmap() {
        Bitmap bitmap = BitmapFactory.decodeFile(mPhotoPath);
        ivDisplay.setImageBitmap(bitmap);
    }

    @Override
    public void onClick(View v) {
        this.mCurrentView = v;
        boolean checkPermission = checkPermission();
        if (!checkPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    REQUEST_CODE_PERMISSION);
            return;
        }
        Intent intent;
        switch (v.getId()) {
            case R.id.btn_installapk:
                intent = Android7Helper.getIntentWithInstallApk(this, new File(getPackageResourcePath()));
                startActivity(intent);
                break;
            case R.id.btn_capture:
                mPhotoPath = getFilePath();
                intent = Android7Helper.getIntentWithCapture(this, new File(mPhotoPath));
                startActivityForResult(intent, REQUEST_CODE_CAPTURE);
                break;
            case R.id.btn_capture_crop:
                mPhotoPath = getFilePath();
                intent = Android7Helper.getIntentWithCapture(this, new File(mPhotoPath));
                startActivityForResult(intent, REQUEST_CODE_CAPTURE_CROP);
                break;
            case R.id.btn_gallery:
                intent = Android7Helper.getIntentWithGallery();
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
                break;
            case R.id.btn_gallery_crop:
                intent = Android7Helper.getIntentWithGallery();
                startActivityForResult(intent, REQUEST_CODE_GALLERY_CROP);
                break;
            case R.id.btn_documents:
                intent = Android7Helper.getIntentWithDocuments();
                startActivityForResult(intent, REQUEST_CODE_DOCUMENTS);
                break;
            case R.id.btn_documents_crop:
                intent = Android7Helper.getIntentWithDocuments();
                startActivityForResult(intent, REQUEST_CODE_DOCUMENTS_CROP);
                break;
        }
    }

    private boolean checkPermission(){
        int granted1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int granted2 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (granted1 == PackageManager.PERMISSION_GRANTED && granted2 == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    @NonNull
    private String getFilePath() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String fileName = "IMG_" + formatter.format(new Date()) + ".jpg";
        return new File(getExternalFilesDir("images"), fileName).getAbsolutePath();
    }

    public void showToast(CharSequence message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        if (toast == null) {
            toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        toast.show();
    }
}
