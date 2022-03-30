package com.huantansheng.easyphotos.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.huantansheng.easyphotos.EasyPhotos;
import com.huantansheng.easyphotos.R;
import com.huantansheng.easyphotos.callback.CompressCallback;
import com.huantansheng.easyphotos.constant.Capture;
import com.huantansheng.easyphotos.constant.Code;
import com.huantansheng.easyphotos.constant.Key;
import com.huantansheng.easyphotos.constant.Type;
import com.huantansheng.easyphotos.models.ad.AdListener;
import com.huantansheng.easyphotos.models.album.AlbumModel;
import com.huantansheng.easyphotos.models.album.entity.Photo;
import com.huantansheng.easyphotos.result.Result;
import com.huantansheng.easyphotos.setting.Setting;
import com.huantansheng.easyphotos.ui.adapter.AlbumItemsAdapter;
import com.huantansheng.easyphotos.ui.adapter.PhotosAdapter;
import com.huantansheng.easyphotos.ui.widget.PressedTextView;
import com.huantansheng.easyphotos.utils.color.ColorUtils;
import com.huantansheng.easyphotos.utils.media.MediaScannerConnectionUtils;
import com.huantansheng.easyphotos.utils.media.MediaUtils;
import com.huantansheng.easyphotos.utils.permission.PermissionUtil;
import com.huantansheng.easyphotos.utils.settings.SettingsUtils;
import com.huantansheng.easyphotos.utils.system.SystemUtils;
import com.huantansheng.easyphotos.utils.uri.UriUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EasyPhotosActivity extends AppCompatActivity implements AlbumItemsAdapter
        .OnClickListener, PhotosAdapter.OnClickListener, AdListener, View.OnClickListener {

    private AlbumModel albumModel;
    private final ArrayList<Object> photoList = new ArrayList<>();
    private final ArrayList<Object> albumItemList = new ArrayList<>();

    private final ArrayList<Photo> resultList = new ArrayList<>();

    private RecyclerView rvPhotos;
    private PhotosAdapter photosAdapter;
    private GridLayoutManager gridLayoutManager;

    private RecyclerView rvAlbumItems;
    private AlbumItemsAdapter albumItemsAdapter;
    private RelativeLayout rootViewAlbumItems;

    private PressedTextView tvAlbumItems, tvDone, tvPreview;
    private TextView tvOriginal;
    private AnimatorSet setHide;
    private AnimatorSet setShow;

    private int currAlbumItemIndex = 0;

    private ImageView ivCamera;

    private LinearLayout mSecondMenus;

    private RelativeLayout permissionView;
    private TextView tvPermission;
    private View mBottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_photos);
        hideActionBar();
        adaptationStatusBar();
        if (!Setting.onlyStartCamera && null == Setting.imageEngine) {
            finish();
            return;
        }
        initSomeViews();
        if (PermissionUtil.checkAndRequestPermissionsInActivity(this, getNeedPermissions())) {
            hasPermissions();
        } else {
            permissionView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        Setting.clear();
        if (albumModel != null) {
            albumModel.stopQuery();
        }
        super.onDestroy();
    }

    public static void start(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, EasyPhotosActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void start(Fragment fragment, int requestCode) {
        Intent intent = new Intent(fragment.getActivity(), EasyPhotosActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void start(androidx.fragment.app.Fragment fragment, int requestCode) {
        Intent intent = new Intent(fragment.getContext(), EasyPhotosActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }

    private void adaptationStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int statusColor = getWindow().getStatusBarColor();
            if (statusColor == Color.TRANSPARENT) {
                statusColor = ContextCompat.getColor(this, R.color.colorPrimaryDark);
            }
            if (ColorUtils.isWhiteColor(statusColor)) {
                SystemUtils.getInstance().setStatusDark(this, true);
            }
        }
    }

    private void initSomeViews() {
        mBottomBar = findViewById(R.id.m_bottom_bar);
        permissionView = findViewById(R.id.rl_permissions_view);
        tvPermission = findViewById(R.id.tv_permission);
        rootViewAlbumItems = findViewById(R.id.root_view_album_items);
        findViewById(R.id.iv_second_menu).setVisibility(Setting.showPuzzleMenu || Setting.showCleanMenu || Setting.showOriginalMenu ? View.VISIBLE : View.GONE);
        if (Setting.isOnlyVideo()) {
            ((TextView) findViewById(R.id.tv_title)).setText(R.string.video_selection_easy_photos);
        }
        setClick(R.id.iv_back);
    }

    private void hasPermissions() {
        Setting.fileProviderAuthority = getPackageName() + ".provider";
        permissionView.setVisibility(View.GONE);
        if (Setting.onlyStartCamera) {
            launchCamera(Code.REQUEST_CAMERA);
            return;
        }
        if (Setting.selectedPhotos.size() > Setting.count) {
            throw new RuntimeException("AlbumBuilder: 默认勾选的图片张数不能大于设置的选择数！" + "|默认勾选张数：" + Setting.selectedPhotos.size() + "|设置的选择数：" + Setting.count);
        }
        AlbumModel.CallBack albumModelCallBack = () -> runOnUiThread(() -> {
            if (photosAdapter != null && albumItemsAdapter != null) {
                int start = photoList.size();
                photoList.clear();
                photoList.addAll(albumModel.getCurrAlbumItemPhotos(currAlbumItemIndex));
                photosAdapter.notifyItemChanged(start, photoList.size());
                albumItemList.clear();
                albumItemList.addAll(albumModel.getAlbumItems());
                albumItemsAdapter.notifyDataSetChanged();
                return;
            }
            onAlbumWorkedDo();
            showProgress(false);
        });
        albumModel = AlbumModel.getInstance();
        showProgress(true);
        albumModel.query(this, albumModelCallBack);
        if (!Setting.selectedPhotos.isEmpty()) {
            for (Photo selectedPhoto : Setting.selectedPhotos) {
                selectedPhoto.selectedOriginal = Setting.selectedOriginal;
                Result.addPhoto(selectedPhoto);
            }
        }
    }

    protected String[] getNeedPermissions() {
        if (Setting.isShowCamera) {
            if (Setting.captureType.equals(Capture.IMAGE)) {
                return new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            } else {
                return new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            }
        } else {
            return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionUtil.onPermissionResult(this, permissions, grantResults, new PermissionUtil.PermissionCallBack() {
            @Override
            public void onSuccess() {
                hasPermissions();
            }

            @Override
            public void onShouldShow() {
                tvPermission.setText(R.string.permissions_again_easy_photos);
                permissionView.setOnClickListener(view -> {
                    if (PermissionUtil.checkAndRequestPermissionsInActivity(EasyPhotosActivity.this, getNeedPermissions())) {
                        hasPermissions();
                    } else {
                        permissionView.setVisibility(View.VISIBLE);
                    }
                });

            }

            @Override
            public void onFailed() {
                tvPermission.setText(R.string.permissions_die_easy_photos);
                permissionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SettingsUtils.startMyApplicationDetailsForResult(EasyPhotosActivity.this, getPackageName());
                    }
                });
            }
        });
    }


    /**
     * 启动相机
     *
     * @param requestCode startActivityForResult的请求码
     */
    private void launchCamera(int requestCode) {
        if (!cameraIsCanUse()) {
            permissionView.setVisibility(View.VISIBLE);
            tvPermission.setText(R.string.permissions_die_easy_photos);
            permissionView.setOnClickListener(view ->
                    SettingsUtils.startMyApplicationDetailsForResult(this, getPackageName())
            );
            return;
        }
        Intent intent = new Intent(this, EasyCameraActivity.class);
        startActivityForResult(intent, requestCode);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Code.REQUEST_SETTING_APP_DETAILS) {
            if (PermissionUtil.checkAndRequestPermissionsInActivity(this, getNeedPermissions())) {
                hasPermissions();
            } else {
                permissionView.setVisibility(View.VISIBLE);
            }
            return;
        }
        switch (resultCode) {
            case RESULT_OK:
                if (data == null) return;
                if (Code.REQUEST_CAMERA == requestCode) {
                    Uri uri;
                    Uri videoUri = data.getParcelableExtra(Key.EXTRA_RESULT_CAPTURE_VIDEO_PATH);
                    Uri imageUri = data.getParcelableExtra(Key.EXTRA_RESULT_CAPTURE_IMAGE_PATH);
                    if (videoUri != null) {
                        uri = videoUri;
                    } else {
                        uri = imageUri;
                    }
                    String path = UriUtils.getPathByUri(uri);
                    File tempFile = null;
                    if (path != null) tempFile = new File(path);
                    if (tempFile == null || !tempFile.exists()) {
                        throw new RuntimeException("EasyPhotos拍照保存的图片不存在");
                    }
                    onCameraResult(tempFile);
                    return;
                }
                if (Code.REQUEST_PREVIEW_ACTIVITY == requestCode) {
                    photosAdapter.change();
                    processOriginalMenu();
                    shouldShowMenuDone();
                    if (data.getBooleanExtra(Key.PREVIEW_CLICK_DONE, false)) {
                        done();
                    }
                    return;
                }
                if (Code.REQUEST_PUZZLE_SELECTOR == requestCode) {
                    try {
                        PackageManager packageManager = getApplicationContext().getPackageManager();
                        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
                        final String applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
                        Photo puzzlePhoto = data.getParcelableExtra(EasyPhotos.RESULT_PHOTOS);
                        addNewPhoto(applicationName, puzzlePhoto);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                if (UCrop.REQUEST_CROP == requestCode) {
                    final Uri resultUri = UCrop.getOutput(data);
                    if (resultUri != null) {
                        resultList.get(0).cropPath = resultUri.getPath();
                        done();
                    }
                    return;
                }
                break;
            case RESULT_CANCELED:
                if (Code.REQUEST_CAMERA == requestCode) {
                    if (Setting.onlyStartCamera) {
                        finish();
                    }
                    return;
                }

                if (Code.REQUEST_PREVIEW_ACTIVITY == requestCode) {
                    processOriginalMenu();
                    return;
                }

                if (UCrop.REQUEST_CROP == requestCode) {
                    if (Setting.onlyStartCamera) {
                        finish();
                    }
                }
                break;
            case UCrop.RESULT_ERROR:
                if (data != null) {
                    Log.e("EasyPhotos", "ucrop occur error: " + UCrop.getError(data));
                }
                break;
            default:
                break;
        }
    }

    private void startCrop(AppCompatActivity context, Photo photo, Intent data) {

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photo.filePath, bitmapOptions);
        if (bitmapOptions.outWidth == -1 || bitmapOptions.outHeight == -1) {
            setResult(RESULT_OK, data);
            finish();
            Log.e("EasyPhotos", "该类型不支持裁剪！");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH:mm:ss", Locale.getDefault());

        String imageName = "IMG_CROP_%s" + getImageSuffix(photo.type);
        String destinationFileName = String.format(imageName, dateFormat.format(new Date()));

        UCrop.Options options = new UCrop.Options();
        //设置相关颜色
        int statusBarColor = ContextCompat.getColor(this, R.color.easy_photos_status_bar);
        if (ColorUtils.isWhiteColor(statusBarColor)) {
            statusBarColor = Color.LTGRAY;
        }
        options.setStatusBarColor(statusBarColor);
        int barColor = ContextCompat.getColor(this, R.color.easy_photos_bar_primary);
        options.setToolbarColor(barColor);
        int widgetColor = ContextCompat.getColor(this, R.color.easy_photos_fg_primary);
        options.setToolbarWidgetColor(widgetColor);
        //options.setLogoColor(Color.TRANSPARENT);
        //设置裁剪质量
        options.setCompressionQuality(Setting.compressQuality);
        //是否圆形裁剪
        options.setCircleDimmedLayer(Setting.isCircle);
        //设置网格相关
        options.setShowCropFrame(Setting.isShowCropCropFrame);
        options.setShowCropGrid(Setting.isShowCropGrid);
        //是否自由裁剪
        options.setFreeStyleCropEnabled(Setting.isFreeStyleCrop);
        //设置title
        options.setToolbarTitle(getString(R.string.ucrop_activity_title));
        //隐藏底部控制栏
        options.setHideBottomControls(Setting.isHideUCropControls);
        //toolbar
        options.setToolbarCancelDrawable(R.drawable.ic_arrow_back_easy_photos);

//        Uri uri;
//        if (SystemUtils.beforeAndroidTen()) {
//            uri = Uri.fromFile(new File(source));
//        } else {
//            uri = Uri.parse(source);
//        }

        File cacheFile = new File(context.getCacheDir(), destinationFileName);
        UCrop.of(photo.fileUri, Uri.fromFile(cacheFile))
                .withAspectRatio(Setting.aspectRatio[0], Setting.aspectRatio[1])
                .withOptions(options)
                .start(context);
    }

    private String getImageSuffix(String type) {
        String defaultSuffix = ".png";
        try {
            int index = type.lastIndexOf("/") + 1;
            if (index > 0) {
                return "." + type.substring(index);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return defaultSuffix;
        }
        return defaultSuffix;
    }

    private void addNewPhoto(String albumName, Photo photo) {
        MediaScannerConnectionUtils.refresh(this, photo.filePath);
        photo.selectedOriginal = Setting.selectedOriginal;

        String albumItem_all_name = albumModel.getAllAlbumName(this);
        albumModel.album.getAlbumItem(albumItem_all_name).addImageItem(0, photo);
        if (albumName == null) {
            final File parentFile = new File(photo.filePath).getParentFile();
            albumName = parentFile.getName();
        }
        albumModel.album.addAlbumItem(albumName, photo.fileUri, photo.filePath);
        albumModel.album.getAlbumItem(albumName).addImageItem(0, photo);

        albumItemList.clear();
        albumItemList.addAll(albumModel.getAlbumItems());
        if (Setting.hasAlbumItemsAd()) {
            int albumItemsAdIndex = 2;
            if (albumItemList.size() < albumItemsAdIndex + 1) {
                albumItemsAdIndex = albumItemList.size() - 1;
            }
            albumItemList.add(albumItemsAdIndex, Setting.albumItemsAdView);
        }
        albumItemsAdapter.notifyDataSetChanged();

        if (Setting.count == 1) {
            Result.clear();
            int res = Result.addPhoto(photo);
            onSelectError(res);
        } else {
            if (Result.count() >= Setting.count) {
                onSelectError(null);
            } else {
                int res = Result.addPhoto(photo);
                onSelectError(res);
            }
        }
        rvAlbumItems.scrollToPosition(0);
        albumItemsAdapter.setSelectedPosition(0);
        if (Setting.singleCheckedBack) {
            tvDone.performClick();
        } else {
            shouldShowMenuDone();
        }
    }

    private void onCameraResult(File file) {

        final Pair<String, Photo> pair = MediaUtils.getPhoto(file);
        if (pair == null || pair.second == null) {
            throw new RuntimeException("EasyPhotos拍照保存的图片不存在");
        }

        String bucketName = pair.first;
        Photo photo = pair.second;
        if (Setting.onlyStartCamera || albumModel.getAlbumItems().isEmpty()) {
            MediaScannerConnectionUtils.refresh(this, file);// 更新媒体库

            photo.selectedOriginal = Setting.selectedOriginal;
            Result.addPhoto(photo);
            done();
            return;
        }
        addNewPhoto(bucketName, photo);
    }

    private void onAlbumWorkedDo() {
        initView();
    }

    private void initView() {

        if (albumModel.getAlbumItems().isEmpty()) {
            Toast.makeText(this, R.string.no_photos_easy_photos, Toast.LENGTH_LONG).show();
            if (Setting.isShowCamera) {
                launchCamera(Code.REQUEST_CAMERA);
            } else {
                finish();
            }
            return;
        }

        EasyPhotos.setAdListener(this);
        if (Setting.hasPhotosAd()) {
            findViewById(R.id.m_tool_bar_bottom_line).setVisibility(View.GONE);
        }
        ivCamera = findViewById(R.id.fab_camera);
        if (Setting.isShowCamera && Setting.isBottomRightCamera()) {
            ivCamera.setVisibility(View.VISIBLE);
        }
        if (!Setting.showPuzzleMenu) {
            findViewById(R.id.tv_puzzle).setVisibility(View.GONE);
        }
        mSecondMenus = findViewById(R.id.m_second_level_menu);
        int columns = getResources().getInteger(R.integer.photos_columns_easy_photos);
        tvAlbumItems = findViewById(R.id.tv_album_items);
        tvAlbumItems.setText(albumModel.getAlbumItems().get(0).name);
        tvDone = findViewById(R.id.tv_done);
        rvPhotos = findViewById(R.id.rv_photos);
        ((SimpleItemAnimator) rvPhotos.getItemAnimator()).setSupportsChangeAnimations(false);
        //去除item更新的闪光
        photoList.clear();
        photoList.addAll(albumModel.getCurrAlbumItemPhotos(0));
        int index = 0;
        if (Setting.hasPhotosAd()) {
            photoList.add(index, Setting.photosAdView);
        }
        if (Setting.isShowCamera && !Setting.isBottomRightCamera()) {
            if (Setting.hasPhotosAd()) index = 1;
            photoList.add(index, null);
        }
        photosAdapter = new PhotosAdapter(this, photoList, this);

        gridLayoutManager = new GridLayoutManager(this, columns);
        if (Setting.hasPhotosAd()) {
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (position == 0) {
                        return gridLayoutManager.getSpanCount();//独占一行
                    } else {
                        return 1;//只占一行中的一列
                    }
                }
            });
        }
        rvPhotos.setLayoutManager(gridLayoutManager);
        rvPhotos.setAdapter(photosAdapter);
        tvOriginal = findViewById(R.id.tv_original);
        if (Setting.showOriginalMenu) {
            processOriginalMenu();
        } else {
            tvOriginal.setVisibility(View.GONE);
        }
        tvPreview = findViewById(R.id.tv_preview);

        initAlbumItems();
        shouldShowMenuDone();
        setClick(R.id.iv_album_items, R.id.tv_clear, R.id.iv_second_menu, R.id.tv_puzzle);
        setClick(tvAlbumItems, rootViewAlbumItems, tvDone, tvOriginal, tvPreview, ivCamera);
    }

    private void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initAlbumItems() {
        rvAlbumItems = findViewById(R.id.rv_album_items);
        albumItemList.clear();
        albumItemList.addAll(albumModel.getAlbumItems());

        if (Setting.hasAlbumItemsAd()) {
            int albumItemsAdIndex = 2;
            if (albumItemList.size() < albumItemsAdIndex + 1) {
                albumItemsAdIndex = albumItemList.size() - 1;
            }
            albumItemList.add(albumItemsAdIndex, Setting.albumItemsAdView);
        }
        albumItemsAdapter = new AlbumItemsAdapter(this, albumItemList, 0, this);
        rvAlbumItems.setLayoutManager(new LinearLayoutManager(this));
        rvAlbumItems.setAdapter(albumItemsAdapter);
        rvAlbumItems.setOnTouchListener(new View.OnTouchListener() {
            float lastY;
            boolean canClose;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        float curY = event.getY();
                        if (!rvAlbumItems.canScrollVertically(-1)) {
                            float dy = lastY == 0 ? 0 : curY - lastY;
                            if (dy > ViewConfiguration.get(EasyPhotosActivity.this).getScaledTouchSlop()) {
                                lastY = 0;
                                canClose = true;
                            }
                        } else {
                            canClose = false;
                        }
                        lastY = curY;
                        break;
                    case MotionEvent.ACTION_UP:
                        if (canClose) {
                            canClose = false;
                            showAlbumItems(false);
                            return true;
                        }
                        break;
                    default:
                        lastY = 0;
                        canClose = false;
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.tv_album_items == id || R.id.iv_album_items == id) {
            showAlbumItems(View.GONE == rootViewAlbumItems.getVisibility());
        } else if (R.id.root_view_album_items == id) {
            showAlbumItems(false);
        } else if (R.id.iv_back == id) {
            setResult(RESULT_CANCELED);
            finish();
        } else if (R.id.tv_done == id) {
            done();
        } else if (R.id.tv_clear == id) {
            if (Result.isEmpty()) {
                processSecondMenu();
                return;
            }
            Result.removeAll();
            photosAdapter.change();
            shouldShowMenuDone();
            processSecondMenu();
        } else if (R.id.tv_original == id) {
            if (!Setting.originalMenuUsable) {
                Toast.makeText(this, Setting.originalMenuUnusableHint, Toast.LENGTH_SHORT).show();
                return;
            }
            Setting.selectedOriginal = !Setting.selectedOriginal;
            processOriginalMenu();
            processSecondMenu();
        } else if (R.id.tv_preview == id) {
            PreviewActivity.start(EasyPhotosActivity.this, -1, 0);
        } else if (R.id.fab_camera == id) {
            launchCamera(Code.REQUEST_CAMERA);
        } else if (R.id.iv_second_menu == id) {
            processSecondMenu();
        } else if (R.id.tv_puzzle == id) {
            processSecondMenu();
            PuzzleSelectorActivity.start(this);
        }
    }

    public void processSecondMenu() {
        if (mSecondMenus == null) {
            return;
        }
        if (View.VISIBLE == mSecondMenus.getVisibility()) {
            mSecondMenus.setVisibility(View.INVISIBLE);
            if (Setting.isShowCamera && Setting.isBottomRightCamera()) {
                ivCamera.setVisibility(View.VISIBLE);
            }
        } else {
            mSecondMenus.setVisibility(View.VISIBLE);
            if (Setting.isShowCamera && Setting.isBottomRightCamera()) {
                ivCamera.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void done() {
        Intent intent = new Intent();
        Result.processOriginal();
        resultList.clear();
        resultList.addAll(Result.photos);
        intent.putParcelableArrayListExtra(EasyPhotos.RESULT_PHOTOS, resultList);
        ArrayList<String> resultPaths = new ArrayList<>();
        for (Photo photo : resultList) {
            resultPaths.add(photo.getAvailablePath());
        }
        intent.putStringArrayListExtra(EasyPhotos.RESULT_PATHS, resultPaths);
        intent.putExtra(EasyPhotos.RESULT_SELECTED_ORIGINAL, Setting.selectedOriginal);
        if (Setting.isCrop && TextUtils.isEmpty(resultList.get(0).cropPath)) {
            startCrop(this, resultList.get(0), intent);
        } else if (!isCompressed && Setting.compressEngine != null && Setting.isCompress) {
            isCompressed = true;
            compress();
        } else {
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private boolean isCompressed = false;

    private void compress() {
        Setting.compressEngine.compress(this, resultList, new CompressCallback() {
            @Override
            public void onStart() {
                showProgress(true, EasyPhotosActivity.this.getString(R.string.compressing_picture));
            }

            @Override
            public void onSuccess(ArrayList<Photo> photos) {
                showProgress(false);
                done();
            }

            @Override
            public void onFailed(ArrayList<Photo> photos, final String message) {
                showProgress(false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(EasyPhotosActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void processOriginalMenu() {
        if (!Setting.showOriginalMenu) return;
        if (Setting.selectedOriginal) {
            tvOriginal.setTextColor(ContextCompat.getColor(this, R.color.easy_photos_fg_accent));
        } else {
            if (Setting.originalMenuUsable) {
                tvOriginal.setTextColor(ContextCompat.getColor(this, R.color.easy_photos_fg_primary));
            } else {
                tvOriginal.setTextColor(ContextCompat.getColor(this, R.color.easy_photos_fg_primary_dark));
            }
        }
    }

    private void showAlbumItems(boolean isShow) {
        if (null == setShow) {
            newAnimators();
        }
        if (isShow) {
            rootViewAlbumItems.setVisibility(View.VISIBLE);
            setShow.start();
        } else {
            setHide.start();
        }

    }

    private void newAnimators() {
        newHideAnim();
        newShowAnim();
    }

    private void newShowAnim() {
        ObjectAnimator translationShow = ObjectAnimator.ofFloat(rvAlbumItems, "translationY",
                mBottomBar.getTop(), 0);
        ObjectAnimator alphaShow = ObjectAnimator.ofFloat(rootViewAlbumItems, "alpha", 0.0f, 1.0f);
        translationShow.setDuration(300);
        setShow = new AnimatorSet();
        setShow.setInterpolator(new AccelerateDecelerateInterpolator());
        setShow.play(translationShow).with(alphaShow);
    }

    private void newHideAnim() {
        ObjectAnimator translationHide = ObjectAnimator.ofFloat(rvAlbumItems, "translationY", 0,
                mBottomBar.getTop());
        ObjectAnimator alphaHide = ObjectAnimator.ofFloat(rootViewAlbumItems, "alpha", 1.0f, 0.0f);
        translationHide.setDuration(300);
        setHide = new AnimatorSet();
        setHide.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                rootViewAlbumItems.setVisibility(View.GONE);
            }
        });
        setHide.setInterpolator(new AccelerateInterpolator());
        setHide.play(translationHide).with(alphaHide);
    }

    @Override
    public void onAlbumItemClick(int position, int realPosition) {
        updatePhotos(realPosition);
        showAlbumItems(false);
        tvAlbumItems.setText(albumModel.getAlbumItems().get(realPosition).name);
    }

    private void updatePhotos(int currAlbumItemIndex) {
        this.currAlbumItemIndex = currAlbumItemIndex;
        photoList.clear();
        photoList.addAll(albumModel.getCurrAlbumItemPhotos(currAlbumItemIndex));
        int index = 0;
        if (Setting.hasPhotosAd()) {
            photoList.add(index, Setting.photosAdView);
        }
        if (Setting.isShowCamera && !Setting.isBottomRightCamera()) {
            if (Setting.hasPhotosAd()) index = 1;
            photoList.add(index, null);
        }
        photosAdapter.change();
        rvPhotos.scrollToPosition(0);
    }

    private void shouldShowMenuDone() {
        if (Result.isEmpty()) {
            if (View.VISIBLE == tvDone.getVisibility()) {
                ScaleAnimation scaleHide = new ScaleAnimation(1f, 0f, 1f, 0f);
                scaleHide.setDuration(200);
                tvDone.startAnimation(scaleHide);
            }
            tvDone.setVisibility(View.INVISIBLE);
            tvPreview.setVisibility(View.INVISIBLE);
        } else {
            if (View.INVISIBLE == tvDone.getVisibility()) {
                ScaleAnimation scaleShow = new ScaleAnimation(0f, 1f, 0f, 1f);
                scaleShow.setDuration(200);
                tvDone.startAnimation(scaleShow);
            }
            tvDone.setVisibility(View.VISIBLE);
            tvPreview.setVisibility(View.VISIBLE);
        }
        if (Setting.distinguishCount && Setting.selectMutualExclusion && Result.photos.size() > 0) {
            final String photoType = Result.photos.get(0).type;
            if (photoType.contains(Type.VIDEO) && Setting.videoCount != -1) {
                tvDone.setText(getString(R.string.selector_action_done_easy_photos, Result.count(), Setting.videoCount));
            } else if (photoType.contains(Type.IMAGE) && Setting.pictureCount != -1) {
                tvDone.setText(getString(R.string.selector_action_done_easy_photos, Result.count(), Setting.pictureCount));
            } else {
                tvDone.setText(getString(R.string.selector_action_done_easy_photos, Result.count(), Setting.count));
            }
        } else {
            tvDone.setText(getString(R.string.selector_action_done_easy_photos, Result.count(), Setting.count));
        }
    }

    @Override
    public void onCameraClick() {
        launchCamera(Code.REQUEST_CAMERA);
    }

    @Override
    public void onPhotoClick(int position, int realPosition) {
        PreviewActivity.start(EasyPhotosActivity.this, currAlbumItemIndex, realPosition);
    }

    @Override
    public void onSelectError(@Nullable Integer result) {
        if (result == null) {
            Toast.makeText(this, getString(R.string.selector_reach_max_hint_easy_photos, Setting.count), Toast.LENGTH_SHORT).show();
            return;
        }
        switch (result) {
            case -1:
                Toast.makeText(this, getString(R.string.selector_reach_max_image_hint_easy_photos, Setting.pictureCount), Toast.LENGTH_SHORT).show();
                break;
            case -2:
                Toast.makeText(this, getString(R.string.selector_reach_max_video_hint_easy_photos, Setting.videoCount), Toast.LENGTH_SHORT).show();
                break;
            case -3:
                Toast.makeText(this, getString(R.string.msg_no_file_easy_photos), Toast.LENGTH_SHORT).show();
                break;
            case -4:
                Toast.makeText(this, getString(R.string.selector_mutual_exclusion_easy_photos), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onSelectorChanged() {
        if (Setting.singleCheckedBack) {
            tvDone.performClick();
        } else {
            shouldShowMenuDone();
        }
    }


    @Override
    public void onBackPressed() {

        if (null != rootViewAlbumItems && rootViewAlbumItems.getVisibility() == View.VISIBLE) {
            showAlbumItems(false);
            return;
        }

        if (null != mSecondMenus && View.VISIBLE == mSecondMenus.getVisibility()) {
            processSecondMenu();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onPhotosAdLoaded() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (photosAdapter != null)
                    photosAdapter.change();
            }
        });
    }

    @Override
    public void onAlbumItemsAdLoaded() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (albumItemsAdapter != null)
                    albumItemsAdapter.notifyDataSetChanged();
            }
        });
    }


    private void setClick(@IdRes int... ids) {
        for (int id : ids) {
            findViewById(id).setOnClickListener(this);
        }
    }

    private void setClick(View... views) {
        for (View v : views) {
            v.setOnClickListener(this);
        }
    }

    /**
     * 返回true 表示可以使用  返回false表示不可以使用
     */
    public boolean cameraIsCanUse() {
        boolean isCanUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters(); //针对魅族手机
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            isCanUse = false;
        }

        if (mCamera != null) {
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
                return isCanUse;
            }
        }
        return isCanUse;
    }

    private void showProgress(final boolean show, final String... msgs) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final FrameLayout flProgress = findViewById(R.id.frame_progress);
                if (show) {
                    flProgress.setOnClickListener(EasyPhotosActivity.this);
                    flProgress.setVisibility(View.VISIBLE);
                    final TextView tvMessage = findViewById(R.id.tv_progress_message);
                    if (msgs == null || msgs.length == 0) {
                        tvMessage.setVisibility(View.GONE);
                    } else {
                        tvMessage.setText(msgs[0]);
                        tvMessage.setVisibility(View.VISIBLE);
                    }
                } else {
                    flProgress.setOnClickListener(null);
                    flProgress.setVisibility(View.GONE);
                }
            }
        });
    }
}
