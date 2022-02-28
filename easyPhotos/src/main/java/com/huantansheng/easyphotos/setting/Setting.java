package com.huantansheng.easyphotos.setting;

import android.view.View;

import androidx.annotation.IntDef;

import com.huantansheng.cameralibrary.JCameraView;
import com.huantansheng.easyphotos.callback.VideoPreviewCallback;
import com.huantansheng.easyphotos.constant.Capture;
import com.huantansheng.easyphotos.constant.Type;
import com.huantansheng.easyphotos.engine.CompressEngine;
import com.huantansheng.easyphotos.engine.ImageEngine;
import com.huantansheng.easyphotos.models.album.entity.Photo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * EasyPhotos的设置值
 * Created by huan on 2017/10/24.
 */

public class Setting {
    public static int minWidth = 1;
    public static int minHeight = 1;
    public static long minSize = 1;
    public static long maxSize = Long.MAX_VALUE;
    public static boolean selectMutualExclusion = false;
    public static boolean distinguishCount = true;
    public static int count = 1;
    public static int pictureCount = -1;
    public static int videoCount = -1;
    public static WeakReference<View> photosAdView = null;
    public static WeakReference<View> albumItemsAdView = null;
    public static boolean photoAdIsOk = false;
    public static boolean albumItemsAdIsOk = false;
    public static ArrayList<Photo> selectedPhotos = new ArrayList<>();
    public static boolean showOriginalMenu = false;
    public static boolean originalMenuUsable = false;
    public static String originalMenuUnusableHint = "";
    public static boolean selectedOriginal = false;
    public static String fileProviderAuthority = null;
    public static boolean isShowCamera = false;
    public static boolean onlyStartCamera = false;
    public static boolean showPuzzleMenu = true;
    public static List<String> filterTypes = new ArrayList<>(Arrays.asList(Type.image()));
    public static boolean showGif = false;
    public static boolean showCleanMenu = true;
    public static long videoMinSecond = 0L;
    public static long videoMaxSecond = Long.MAX_VALUE;
    public static ImageEngine imageEngine = null;
    public static CompressEngine compressEngine = null;
    public static boolean isCompress = false;
    public static VideoPreviewCallback videoPreviewCallback;
    public static boolean singleCheckedBack = false;
    // 相机按钮位置
    public static final int LIST_FIRST = 0;
    public static final int BOTTOM_RIGHT = 1;
    public static int cameraLocation = BOTTOM_RIGHT;
    // 相机功能
    public static boolean useSystemCamera = true;
    public static String captureType = Capture.ALL;
    public static int recordDuration = 15000;
    public static View cameraCoverView = null;
    public static boolean enableCameraTip = true;
    public static int RECORDING_BIT_RATE = JCameraView.MEDIA_QUALITY_MIDDLE;
    // 裁剪相关参数
    public static boolean isCrop = false;
    public static int compressQuality = 90;
    public static boolean isCircle = false;
    public static boolean isShowCropCropFrame = true;
    public static boolean isShowCropGrid = true;
    public static boolean isFreeStyleCrop = false;
    public static boolean isHideUCropControls = false;
    public static float[] aspectRatio = new float[]{1, 1};

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {LIST_FIRST, BOTTOM_RIGHT})
    public @interface Location {

    }

    public static void clear() {
        minWidth = 1;
        minHeight = 1;
        minSize = 1;
        maxSize = Long.MAX_VALUE;
        selectMutualExclusion = false;
        distinguishCount = true;
        count = 1;
        pictureCount = -1;
        videoCount = -1;
        if (photosAdView != null) photosAdView.clear();
        photosAdView = null;
        if (albumItemsAdView != null) albumItemsAdView.clear();
        albumItemsAdView = null;
        photoAdIsOk = false;
        albumItemsAdIsOk = false;
        selectedPhotos.clear();
        showOriginalMenu = false;
        originalMenuUsable = false;
        originalMenuUnusableHint = "";
        selectedOriginal = false;
        compressEngine = null;
        isCompress = false;
        videoPreviewCallback = null;
        singleCheckedBack = false;
        cameraLocation = BOTTOM_RIGHT;
        isShowCamera = false;
        onlyStartCamera = false;
        showPuzzleMenu = true;
        filterTypes = new ArrayList<>(Arrays.asList(Type.image()));
        showGif = false;
        showCleanMenu = true;
        videoMinSecond = 0L;
        videoMaxSecond = Long.MAX_VALUE;
        useSystemCamera = true;
        captureType = Capture.ALL;
        recordDuration = 15000;
        cameraCoverView = null;
        enableCameraTip = true;
        RECORDING_BIT_RATE = JCameraView.MEDIA_QUALITY_MIDDLE;
        isCrop = false;
        compressQuality = 90;
        isCircle = false;
        isShowCropCropFrame = true;
        isShowCropGrid = true;
        isFreeStyleCrop = false;
        isHideUCropControls = false;
        aspectRatio = new float[]{1, 1};
    }

    public static boolean isOnlyGif() {
        //Setting.filterTypes.containsAll(Arrays.asList(Type.image()))
        return Arrays.asList(Type.gif()).containsAll(Setting.filterTypes);
    }

    public static boolean isOnlyImage() {
        //Setting.filterTypes.containsAll(Arrays.asList(Type.image()))
        return Arrays.asList(Type.image()).containsAll(Setting.filterTypes);
    }

    public static boolean isOnlyVideo() {
        //Setting.filterTypes.containsAll(Arrays.asList(Type.video()))
        return Arrays.asList(Type.video()).containsAll(Setting.filterTypes);
    }

    public static boolean isAll() {
        //Setting.filterTypes.containsAll(Arrays.asList(Type.all()))
        return Arrays.asList(Type.all()).containsAll(Setting.filterTypes);
    }

    public static boolean showVideo() {
        return !isOnlyImage();
    }

    public static boolean hasPhotosAd() {
        return photosAdView != null && photosAdView.get() != null;
    }

    public static boolean hasAlbumItemsAd() {
        return albumItemsAdView != null && albumItemsAdView.get() != null;
    }

    public static boolean isBottomRightCamera() {
        return cameraLocation == BOTTOM_RIGHT;
    }
}
