package com.huantansheng.easyphotos.utils.media;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;

import com.huantansheng.easyphotos.EasyPhotos;
import com.huantansheng.easyphotos.constant.Type;
import com.huantansheng.easyphotos.models.album.entity.Photo;
import com.huantansheng.easyphotos.setting.Setting;
import com.huantansheng.easyphotos.utils.uri.UriUtils;
import com.huantansheng.easyphotos.utils.system.SystemUtils;

import java.io.File;

public class MediaUtils {

    /**
     * 获取时长
     *
     * @param path path
     * @return duration
     */
    public static long getDuration(String path) {
        MediaMetadataRetriever mmr = null;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(path);
            return Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception e) {
            Log.e("DurationUtils", e.toString());
        } finally {
            if (mmr != null) {
                mmr.release();
            }
        }
        return 0;
    }

    /**
     * 格式化时长（不足一秒则显示为一秒）
     *
     * @param duration duration
     * @return "MM:SS" or "H:MM:SS"
     */
    public static String format(long duration) {
        double seconds = duration / 1000.0;
        return DateUtils.formatElapsedTime((long) (seconds + 0.5));
    }

    /**
     * 创建一条图片地址uri,用于保存拍照后的照片
     */
    @Nullable
    public static Uri createImageUri(final Context context) {
        String status = Environment.getExternalStorageState();
        String time = String.valueOf(System.currentTimeMillis());
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + time);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_TAKEN, time);
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera");
            return context.getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            return context.getContentResolver()
                    .insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
        }
    }


    /**
     * 创建一条视频地址uri,用于保存录制的视频
     */
    @Nullable
    public static Uri createVideoUri(final Context context) {
        String status = Environment.getExternalStorageState();
        String time = String.valueOf(System.currentTimeMillis());
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, "VID_" + time);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATE_TAKEN, time);
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            values.put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/Camera");
            return context.getContentResolver()
                    .insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            return context.getContentResolver()
                    .insert(MediaStore.Video.Media.INTERNAL_CONTENT_URI, values);
        }
    }

    @SuppressLint("Range")
    @Nullable
    public static Pair<String, Photo> getPhoto(File file) {
        Uri uri = UriUtils.getUriByPath(file.getPath());
        if (uri == null) {
            return null;
        }
        Cursor cursor = EasyPhotos.getApp().getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return null;
        }
        Pair<String, Photo> pair = null;

        if (cursor.moveToFirst()) {
            final long id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            final String type = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
            final long size = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE));
            final int width = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH));
            final int height = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT));
            final String name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
            final long dateTime = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED));
            //final long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
            final long duration = getDuration(file.getAbsolutePath());
            final String bucketName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME));
            Photo photo = new Photo(name, path, uri, dateTime, width, height, size, duration, type);
            pair = new Pair<>(bucketName, photo);
        }
        cursor.close();
        return pair;
    }
}
