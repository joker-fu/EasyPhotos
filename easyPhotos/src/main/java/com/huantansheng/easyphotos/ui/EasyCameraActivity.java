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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.huantansheng.cameralibrary.JCameraView;
import com.huantansheng.cameralibrary.listener.ClickListener;
import com.huantansheng.cameralibrary.listener.ErrorListener;
import com.huantansheng.cameralibrary.listener.JCameraListener;
import com.huantansheng.cameralibrary.listener.JCameraPreViewListener;
import com.huantansheng.cameralibrary.util.FileUtil;
import com.huantansheng.easyphotos.R;
import com.huantansheng.easyphotos.constant.Capture;
import com.huantansheng.easyphotos.constant.Code;
import com.huantansheng.easyphotos.constant.Key;
import com.huantansheng.easyphotos.setting.Setting;
import com.huantansheng.easyphotos.utils.Future;
import com.huantansheng.easyphotos.utils.media.MediaUtils;
import com.huantansheng.easyphotos.utils.system.SystemUtils;

import java.io.File;
import java.io.IOException;

public class EasyCameraActivity extends AppCompatActivity {

    private JCameraView jCameraView;
    private ProgressBar pbProgress;
    private RelativeLayout rlCoverView;

    private String applicationName = "";
    private Uri imageUri = null;

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
        if (cameraIntent.resolveActivity(getPackageManager()) != null ||
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            if (SystemUtils.beforeAndroidTen()) {
                final File tempFile = createCameraTempFile("IMG", ".jpg");
                if (tempFile != null && tempFile.exists()) {
                    if (SystemUtils.beforeAndroidN()) {
                        imageUri = Uri.fromFile(tempFile);
                    } else {
                        //通过FileProvider创建一个content类型的Uri
                        imageUri = FileProvider.getUriForFile(this, Setting.fileProviderAuthority, tempFile);
                    }
//                    cameraPath = tempFile.getAbsolutePath();
                } else {
                    Toast.makeText(this, R.string.camera_temp_file_error_easy_photos, Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                imageUri = MediaUtils.createImageUri(getApplicationContext());
                if (imageUri == null) {
                    Toast.makeText(this, R.string.camera_temp_file_error_easy_photos, Toast.LENGTH_SHORT).show();
                    return;
                }
//                cameraPath = imageUri.toString();
            }
            //对目标应用临时授权该Uri所代表的文件
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //将拍取的照片保存到指定URI
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(cameraIntent, Code.REQUEST_CAMERA);
        } else {
            Toast.makeText(this, R.string.msg_no_camera_easy_photos, Toast.LENGTH_SHORT).show();
        }
//        系统相机录制功能
//        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
//            createCameraTempFile("VIDEO", ".mp4");
//            if (mTempImageFile != null && mTempImageFile.exists()) {
//                Uri videoUri;
//                if (Build.VERSION.SDK_INT >= 24) {
//                    //通过FileProvider创建一个content类型的Uri
//                    videoUri = FileProvider.getUriForFile(this, Setting.fileProviderAuthority, mTempImageFile);
//                } else {
//                    videoUri = Uri.fromFile(mTempImageFile);
//                }
//                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
//                cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, Setting.recordDuration / 1000);
//                cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
//                startActivityForResult(cameraIntent, Code.REQUEST_CAMERA);
//            }
//        } else {
//            Toast.makeText(this, R.string.msg_no_camera_easy_photos, Toast.LENGTH_SHORT).show();
//        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File createCameraTempFile(String prefix, String suffix) {
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
            return File.createTempFile(prefix, suffix, dir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
        setContentView(R.layout.activity_camera_easy_photos);
        pbProgress = findViewById(R.id.pb_progress);
        jCameraView = findViewById(R.id.jCameraView);
        jCameraView.enableCameraTip(Setting.enableCameraTip);
        if (Setting.cameraCoverView != null) {
            View coverView = Setting.cameraCoverView;
            rlCoverView = findViewById(R.id.rl_cover_view);
            coverView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            rlCoverView.addView(coverView);
        }
        initCustomCamera();
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

    private void initCustomCamera() {
        //视频存储路径
        if (SystemUtils.beforeAndroidTen()) {
            // /storage/emulated/0/DCIM/EasyPhotosDemo
            jCameraView.setSaveVideoPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + applicationName);
        } else {
            // /storage/emulated/0/Android/data/com.huantansheng.easyphotos.demo2/files/DCIM/EasyPhotosDemo
            jCameraView.setSaveVideoPath(getExternalFilesDir(Environment.DIRECTORY_DCIM) + File.separator + applicationName);
        }
        jCameraView.setFeatures(getFeature());
        jCameraView.setMediaQuality(Setting.RECORDING_BIT_RATE);
        //fixme 录像时间+800ms 修复录像时间少1s问题
        jCameraView.setDuration(Setting.recordDuration + 800);
        jCameraView.setErrorListener(new ErrorListener() {
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
        if (Setting.cameraCoverView != null) {
            jCameraView.setPreViewListener(new JCameraPreViewListener() {

                @Override
                public void start(int type) {
                    rlCoverView.setVisibility(View.GONE);
                }

                @Override
                public void stop(int type) {
                    rlCoverView.setVisibility(View.VISIBLE);
                }
            });
        }
        //JCameraView监听
        jCameraView.setJCameraListener(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                //获取图片bitmap
                if (SystemUtils.beforeAndroidTen()) {
                    String path = FileUtil.saveBitmap(applicationName, bitmap);
                    File file = new File(path);
                    if (SystemUtils.beforeAndroidN()) {
                        imageUri = Uri.fromFile(file);
                    } else {
                        //通过FileProvider创建一个content类型的Uri
                        imageUri = FileProvider.getUriForFile(EasyCameraActivity.this, Setting.fileProviderAuthority, file);
                    }
                } else {
                    String path = FileUtil.saveBitmapAndroidQ(EasyCameraActivity.this, applicationName, bitmap);
                    imageUri = Uri.parse(path);
                }

                Intent intent = new Intent();
                intent.putExtra(Key.EXTRA_RESULT_CAPTURE_IMAGE_PATH, imageUri);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void recordSuccess(final String url, Bitmap firstFrame) {
                //获取视频路径
                if (SystemUtils.beforeAndroidTen()) {
                    Intent intent = new Intent();
                    File file = new File(url);
                    if (SystemUtils.beforeAndroidN()) {
                        imageUri = Uri.fromFile(file);
                    } else {
                        //通过FileProvider创建一个content类型的Uri
                        imageUri = FileProvider.getUriForFile(EasyCameraActivity.this, Setting.fileProviderAuthority, file);
                    }
                    intent.putExtra(Key.EXTRA_RESULT_CAPTURE_VIDEO_PATH, imageUri);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    pbProgress.setVisibility(View.VISIBLE);
                    Future.runAsync(() -> {
                        String path = FileUtil.copy2DCIMAndroidQ(EasyCameraActivity.this, url, applicationName);
                        return Uri.parse(path);
                    }).onSuccess(imageUri -> {
                        pbProgress.setVisibility(View.GONE);
                        if (!isFinishing()) {
                            Intent intent = new Intent();
                            intent.putExtra(Key.EXTRA_RESULT_CAPTURE_VIDEO_PATH, imageUri);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });
                }
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
        if (resultCode == RESULT_OK && Code.REQUEST_CAMERA == requestCode && imageUri != null) {
            Intent intent = new Intent();
            intent.putExtra(Key.EXTRA_RESULT_CAPTURE_IMAGE_PATH, imageUri);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
