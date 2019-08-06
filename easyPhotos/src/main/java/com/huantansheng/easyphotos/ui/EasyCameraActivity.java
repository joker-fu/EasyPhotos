package com.huantansheng.easyphotos.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.huantansheng.cameralibrary.JCameraView;
import com.huantansheng.cameralibrary.listener.ClickListener;
import com.huantansheng.cameralibrary.listener.ErrorListener;
import com.huantansheng.cameralibrary.listener.JCameraListener;
import com.huantansheng.cameralibrary.util.FileUtil;
import com.huantansheng.easyphotos.R;
import com.huantansheng.easyphotos.constant.Capture;
import com.huantansheng.easyphotos.constant.Code;
import com.huantansheng.easyphotos.constant.Key;
import com.huantansheng.easyphotos.setting.Setting;

import java.io.File;
import java.io.IOException;

public class EasyCameraActivity extends AppCompatActivity {

    private JCameraView jCameraView;
    private File mTempImageFile;
    private String applicationName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            applicationName = getString(R.string.app_name);
            PackageManager packageManager = getApplicationContext().getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (Setting.useSystemCamera) {
            toSystemCamera();
        } else {
            toCustomCamera();
        }
    }

    private void toSystemCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            createCameraTempImageFile();
            if (mTempImageFile != null && mTempImageFile.exists()) {
                Uri imageUri;
                if (Build.VERSION.SDK_INT >= 24) {
                    //通过FileProvider创建一个content类型的Uri
                    imageUri = FileProvider.getUriForFile(this, Setting.fileProviderAuthority, mTempImageFile);
                } else {
                    imageUri = Uri.fromFile(mTempImageFile);
                }
                //对目标应用临时授权该Uri所代表的文件
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                //将拍取的照片保存到指定URI
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, Code.REQUEST_CAMERA);
            } else {
                Toast.makeText(this, R.string.camera_temp_file_error_easy_photos, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.msg_no_camera_easy_photos, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createCameraTempImageFile() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + applicationName);
        if (!dir.exists() || !dir.isDirectory()) {
            if (!dir.mkdirs()) {
                dir = getExternalFilesDir(null);
                if (null == dir || !dir.exists()) {
                    dir = getFilesDir();
                    if (null == dir || !dir.exists()) {
                        String cacheDirPath = getExternalCacheDir() + File.separator;
                        dir = new File(cacheDirPath);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                    }
                }
            }
        }
        try {
            mTempImageFile = File.createTempFile("IMG", ".jpg", dir);
        } catch (IOException e) {
            e.printStackTrace();
            mTempImageFile = null;
        }

    }

    private void toCustomCamera() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            // 始终允许窗口延伸到屏幕短边上的刘海区域
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_camera);
        jCameraView = findViewById(R.id.jCameraView);
        jCameraView.enableCameraTip(Setting.enableCameraTip);
        if (Setting.cameraCoverView != null && Setting.cameraCoverView.get() != null) {
            View coverView = Setting.cameraCoverView.get();
            RelativeLayout rlCoverView = findViewById(R.id.rl_cover_view);
            coverView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            rlCoverView.addView(coverView);
        }
        init();
    }

    private int getFeature() {
        switch (Setting.captureType) {
            case Capture.ALL:
                return JCameraView.BUTTON_STATE_BOTH;
            case Capture.IMAGE:
                return JCameraView.BUTTON_STATE_ONLY_CAPTURE;
            default:
                return JCameraView.BUTTON_STATE_ONLY_RECORDER;
        }
    }

    private void init() {
        //视频存储路径
        jCameraView.setSaveVideoPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + applicationName);
        jCameraView.setFeatures(getFeature());
        jCameraView.setMediaQuality(Setting.RECORDING_BIT_RATE);
        //fixme 录像时间+800ms 修复录像时间少1s问题
        jCameraView.setDuration(Setting.recordDuration + 800);
        jCameraView.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                //错误监听
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }

            @Override
            public void AudioPermissionError() {
                Toast.makeText(EasyCameraActivity.this, getString(R.string.missing_audio_permission), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
        //JCameraView监听
        jCameraView.setJCameraListener(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                //获取图片bitmap
                String path = FileUtil.saveBitmap(applicationName, bitmap);
                Intent intent = new Intent();
                intent.putExtra(Key.EXTRA_RESULT_CAPTURE_IMAGE_PATH, path);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                //获取视频路径
                //String path = FileUtil.saveBitmap(applicationName, firstFrame);
                Intent intent = new Intent();
                //intent.putExtra(Key.EXTRA_RESULT_CAPTURE_IMAGE_PATH, path);
                intent.putExtra(Key.EXTRA_RESULT_CAPTURE_VIDEO_PATH, url);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        jCameraView.setLeftClickListener(new ClickListener() {
            @Override
            public void onClick() {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //全屏显示
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Setting.useSystemCamera) {
            jCameraView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!Setting.useSystemCamera) {
            jCameraView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        if (Setting.cameraCoverView != null) Setting.cameraCoverView.clear();
        Setting.cameraCoverView = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            finish();
            return;
        }
        if (resultCode == RESULT_OK && Code.REQUEST_CAMERA == requestCode && mTempImageFile != null) {
            //获取图片bitmap
            Intent intent = new Intent();
            intent.putExtra(Key.EXTRA_RESULT_CAPTURE_IMAGE_PATH, mTempImageFile.getPath());
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
