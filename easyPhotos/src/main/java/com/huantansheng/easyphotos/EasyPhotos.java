package com.huantansheng.easyphotos;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.huantansheng.easyphotos.Builder.AlbumBuilder;
import com.huantansheng.easyphotos.callback.PuzzleCallback;
import com.huantansheng.easyphotos.engine.ImageEngine;
import com.huantansheng.easyphotos.models.ad.AdListener;
import com.huantansheng.easyphotos.models.album.entity.Photo;
import com.huantansheng.easyphotos.models.sticker.StickerModel;
import com.huantansheng.easyphotos.models.sticker.entity.TextStickerData;
import com.huantansheng.easyphotos.setting.Setting;
import com.huantansheng.easyphotos.ui.PreviewActivity;
import com.huantansheng.easyphotos.utils.bitmap.BitmapUtils;
import com.huantansheng.easyphotos.utils.bitmap.SaveBitmapCallBack;
import com.huantansheng.easyphotos.utils.media.MediaScannerConnectionUtils;
import com.huantansheng.easyphotos.utils.result.EasyResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * EasyPhotos的启动管理器
 * Created by huan on 2017/10/18.
 */
public class EasyPhotos {

    //easyPhotos的返回数据Key
    public static final String RESULT_PHOTOS = "keyOfEasyPhotosResult";
    public static final String RESULT_PATHS = "keyOfEasyPhotosResultPaths";
    public static final String RESULT_SELECTED_ORIGINAL = "keyOfEasyPhotosResultSelectedOriginal";

    /**
     * 创建相机
     *
     * @param activity 上下文
     * @return AlbumBuilder
     */
    public static AlbumBuilder createCamera(FragmentActivity activity) {
        return AlbumBuilder.createCamera(activity);
    }

    public static AlbumBuilder createCamera(Fragment fragmentV) {
        return AlbumBuilder.createCamera(fragmentV);
    }

    /**
     * 创建相册
     *
     * @param activity     上下文
     * @param isShowCamera 是否显示相机按钮
     * @param imageEngine  图片加载引擎的具体实现
     * @return
     */
    public static AlbumBuilder createAlbum(FragmentActivity activity, boolean isShowCamera, @NonNull ImageEngine imageEngine) {
        return AlbumBuilder.createAlbum(activity, isShowCamera, imageEngine);
    }

    public static AlbumBuilder createAlbum(Fragment fragmentV, boolean isShowCamera, @NonNull ImageEngine imageEngine) {
        return AlbumBuilder.createAlbum(fragmentV, isShowCamera, imageEngine);
    }


//*********************AD************************************


    /**
     * 设置广告监听
     * 内部使用，无需关心
     *
     * @param adListener 广告监听
     */
    public static void setAdListener(AdListener adListener) {
        AlbumBuilder.setAdListener(adListener);
    }

    /**
     * 刷新图片列表广告数据
     */
    public static void notifyPhotosAdLoaded() {
        AlbumBuilder.notifyPhotosAdLoaded();
    }

    /**
     * 刷新专辑项目列表广告
     */
    public static void notifyAlbumItemsAdLoaded() {
        AlbumBuilder.notifyAlbumItemsAdLoaded();
    }


//*************************bitmap功能***********************************/

    /**
     * 回收bitmap
     *
     * @param bitmap 要回收的bitmap
     */
    public static void recycle(Bitmap bitmap) {
        BitmapUtils.recycle(bitmap);
    }

    /**
     * 回收bitmap数组中的所有图片
     *
     * @param bitmaps 要回收的bitmap数组
     */
    public static void recycle(Bitmap... bitmaps) {
        BitmapUtils.recycle(bitmaps);
    }

    /**
     * 回收bitmap集合中的所有图片
     *
     * @param bitmaps 要回收的bitmap集合
     */
    public static void recycle(List<Bitmap> bitmaps) {
        BitmapUtils.recycle(bitmaps);
    }

    /**
     * 给图片添加水印，水印会根据图片宽高自动缩放处理
     *
     * @param watermark     水印
     * @param image         添加水印的图片
     * @param srcImageWidth 水印对应的原图片宽度,即ui制作水印时参考的要添加水印的图片的宽度
     * @param offsetX       添加水印的X轴偏移量
     * @param offsetY       添加水印的Y轴偏移量
     * @param addInLeft     true 在左下角添加水印，false 在右下角添加水印
     */
    public static void addWatermark(Bitmap watermark, Bitmap image, int srcImageWidth, int offsetX, int offsetY, boolean addInLeft) {
        BitmapUtils.addWatermark(watermark, image, srcImageWidth, offsetX, offsetY, addInLeft);
    }

    /**
     * 给图片添加带文字和图片的水印，水印会根据图片宽高自动缩放处理
     *
     * @param watermark     水印图片
     * @param image         要加水印的图片
     * @param srcImageWidth 水印对应的原图片宽度,即ui制作水印时参考的要添加水印的图片的宽度
     * @param text          要添加的文字
     * @param offsetX       添加水印的X轴偏移量
     * @param offsetY       添加水印的Y轴偏移量
     * @param addInLeft     true 在左下角添加水印，false 在右下角添加水印
     * @return 是否成功
     */
    public static void addWatermarkWithText(Bitmap watermark, Bitmap image, int srcImageWidth, @NonNull String text, int offsetX, int offsetY, boolean addInLeft) {
        BitmapUtils.addWatermarkWithText(watermark, image, srcImageWidth, text, offsetX, offsetY, addInLeft);
    }

    /**
     * 保存Bitmap到指定文件夹
     *
     * @param act         上下文
     * @param dirPath     文件夹全路径
     * @param bitmap      bitmap
     * @param namePrefix  保存文件的前缀名，文件最终名称格式为：前缀名+自动生成的唯一数字字符+.png
     * @param notifyMedia 是否更新到媒体库
     * @param callBack    保存图片后的回调，回调已经处于UI线程
     */
    public static void saveBitmapToDir(Activity act, String dirPath, String namePrefix, Bitmap bitmap, boolean notifyMedia, SaveBitmapCallBack callBack) {
        BitmapUtils.saveBitmapToDir(act, dirPath, namePrefix, bitmap, notifyMedia, callBack);
    }


    /**
     * 把View画成Bitmap
     *
     * @param view 要处理的View
     * @return Bitmap
     */
    public static Bitmap createBitmapFromView(View view) {
        return BitmapUtils.createBitmapFromView(view);
    }

    /**
     * 启动拼图（最多对9张图片进行拼图）
     *
     * @param act                  上下文
     * @param photos               图片集合（最多对9张图片进行拼图）
     * @param puzzleSaveDirPath    拼图完成保存的文件夹全路径
     * @param puzzleSaveNamePrefix 拼图完成保存的文件名前缀，最终格式：前缀+默认生成唯一数字标识+.png
     * @param replaceCustom        单击替换拼图中的某张图片时，是否以startForResult的方式启动你的自定义界面，该界面与传进来的act为同一界面。false则在EasyPhotos内部完成，正常需求直接写false即可。 true的情况适用于：用于拼图的图片集合中包含网络图片，是在你的act界面中获取并下载的（也可以直接用网络地址，不用下载后的本地地址，也就是可以不下载下来），而非单纯本地相册。举例：你的act中有两个按钮，一个指向本地相册，一个指向网络相册，用户在该界面任意选择，选择好图片后跳转到拼图界面，用户在拼图界面点击替换按钮，将会启动一个新的act界面，这时，act只让用户在网络相册和本地相册选择一张图片，选择好执行
     *                             Intent intent = new Intent();
     *                             intent.putParcelableArrayListExtra(AlbumBuilder.RESULT_PHOTOS , photos);
     *                             act.setResult(RESULT_OK,intent); 并关闭act，回到拼图界面，完成替换。
     * @param imageEngine          图片加载引擎的具体实现
     * @param callback             拼图回调
     */
    public static void startPuzzleWithPhotos(FragmentActivity act, ArrayList<Photo> photos, String puzzleSaveDirPath, String puzzleSaveNamePrefix, boolean replaceCustom, @NonNull ImageEngine imageEngine, PuzzleCallback callback) {
        act.setResult(Activity.RESULT_OK);
        EasyResult.get(act).startPuzzleWithPhotos(photos, puzzleSaveDirPath, puzzleSaveNamePrefix, replaceCustom, imageEngine, callback);
    }

    /**
     * 启动拼图（最多对9张图片进行拼图）
     *
     * @param act                  上下文
     * @param paths                图片地址集合（最多对9张图片进行拼图）
     * @param puzzleSaveDirPath    拼图完成保存的文件夹全路径
     * @param puzzleSaveNamePrefix 拼图完成保存的文件名前缀，最终格式：前缀+默认生成唯一数字标识+.png
     * @param replaceCustom        单击替换拼图中的某张图片时，是否以startForResult的方式启动你的自定义界面，该界面与传进来的act为同一界面。false则在EasyPhotos内部完成，正常需求直接写false即可。 true的情况适用于：用于拼图的图片集合中包含网络图片，是在你的act界面中获取并下载的（也可以直接用网络地址，不用下载后的本地地址，也就是可以不下载下来），而非单纯本地相册。举例：你的act中有两个按钮，一个指向本地相册，一个指向网络相册，用户在该界面任意选择，选择好图片后跳转到拼图界面，用户在拼图界面点击替换按钮，将会启动一个新的act界面，这时，act只让用户在网络相册和本地相册选择一张图片，选择好执行
     *                             Intent intent = new Intent();
     *                             intent.putStringArrayListExtra(AlbumBuilder.RESULT_PATHS , paths);
     *                             act.setResult(RESULT_OK,intent); 并关闭act，回到拼图界面，完成替换。
     * @param imageEngine          图片加载引擎的具体实现
     * @param callback             拼图回调
     */
    public static void startPuzzleWithPaths(FragmentActivity act, ArrayList<String> paths, String puzzleSaveDirPath, String puzzleSaveNamePrefix, boolean replaceCustom, @NonNull ImageEngine imageEngine, PuzzleCallback callback) {
        EasyResult.get(act).startPuzzleWithPaths(paths, puzzleSaveDirPath, puzzleSaveNamePrefix, replaceCustom, imageEngine, callback);
    }

    /**
     * 提供外部预览图片（网络图片请开启网络权限）
     *
     * @param act           上下文
     * @param imageEngine   图片加载引擎
     * @param photos        图片Photo集合
     * @param bottomPreview 是否显示底部预览效果
     */
    public static void startPreviewPhotos(FragmentActivity act, @NonNull ImageEngine imageEngine, @NonNull ArrayList<Photo> photos, boolean bottomPreview) {
        Setting.imageEngine = imageEngine;
        PreviewActivity.start(act, photos, bottomPreview);
    }

    /**
     * 提供外部预览图片（网络图片请开启网络权限）
     *
     * @param act           上下文
     * @param imageEngine   图片加载引擎
     * @param paths         图片路径集合
     * @param bottomPreview 是否显示底部预览效果
     */
    public static void startPreviewPaths(FragmentActivity act, @NonNull ImageEngine imageEngine, @NonNull ArrayList<String> paths, boolean bottomPreview) {
        Setting.imageEngine = imageEngine;
        ArrayList<Photo> photos = new ArrayList<>();
        for (String path : paths) {
            Photo photo = new Photo(null, path, 0, 0, 0, 0, 0, "");
            photos.add(photo);
        }
        PreviewActivity.start(act, photos, bottomPreview);
    }

    //**************更新媒体库***********************

    /**
     * 更新媒体文件到媒体库
     *
     * @param cxt       上下文
     * @param filePaths 更新的文件地址
     */
    public static void notifyMedia(Context cxt, String... filePaths) {
        MediaScannerConnectionUtils.refresh(cxt, filePaths);
    }

    /**
     * 更新媒体文件到媒体库
     *
     * @param cxt   上下文
     * @param files 更新的文件
     */
    public static void notifyMedia(Context cxt, File... files) {
        MediaScannerConnectionUtils.refresh(cxt, files);
    }

    /**
     * 更新媒体文件到媒体库
     *
     * @param cxt      上下文
     * @param fileList 更新的文件地址集合
     */
    public static void notifyMedia(Context cxt, List<String> fileList) {
        MediaScannerConnectionUtils.refresh(cxt, fileList);
    }


    //*********************************贴纸***************************


    /**
     * 添加文字贴纸的文字数据
     *
     * @param textStickerData 文字贴纸的文字数据
     */
    public static void addTextStickerData(TextStickerData... textStickerData) {
        StickerModel.textDataList.addAll(Arrays.asList(textStickerData));
    }

    /**
     * 清空文字贴纸的数据
     */
    public static void clearTextStickerDataList() {
        StickerModel.textDataList.clear();
    }
}
