package com.huantansheng.easyphotos.models.album;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.huantansheng.easyphotos.R;
import com.huantansheng.easyphotos.constant.Type;
import com.huantansheng.easyphotos.models.album.entity.Album;
import com.huantansheng.easyphotos.models.album.entity.AlbumItem;
import com.huantansheng.easyphotos.models.album.entity.Photo;
import com.huantansheng.easyphotos.setting.Setting;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

/**
 * 专辑模型
 * Created by huan on 2017/10/20.
 * <p>
 * Modified by Eagle on 2018/08/31.
 * 修改内容：将AlbumModel的实例化与数据查询分开
 */
public class AlbumModel {
    private static final String TAG = "AlbumModel";
    public static AlbumModel instance;
    public Album album;
    private boolean isQuery = false;

    private static final String IS_GIF = "=='image/gif'";

    private static final String NOT_GIF = "!='image/gif'";

    private static final Uri CONTENT_URI = MediaStore.Files.getContentUri("external");

    private static final String[] PROJECTIONS = new String[]{
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.SIZE,
            MediaStore.Video.Media.DURATION,

            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT
    };

    private static final String SORT_ORDER = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC";

    private static final String[] SELECTION_ALL_ARGS = {
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
    };

    private AlbumModel() {
        album = new Album();
    }

    public static AlbumModel getInstance() {
        if (null == instance) {
            synchronized (AlbumModel.class) {
                if (null == instance) {
                    instance = new AlbumModel();
                }
            }
        }
        return instance;
    }

    /**
     * 专辑查询
     *
     * @param context  调用查询方法的context
     * @param callBack 查询完成后的回调
     */
    public void query(final Context context, final CallBack callBack) {
        isQuery = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                album.clear();
                initAlbum(context);
                if (null != callBack) callBack.onAlbumWorkedCallBack();
            }
        }).start();
    }

    public void stopQuery() {
        isQuery = false;
        instance = null;
    }

    /**
     * 获取视频(最长或最小时间)
     */
    private String getDurationCondition() {
        return String.format(Locale.getDefault(), "%d <%s duration and duration <= %d",
                Setting.videoMinSecond, Setting.videoMinSecond == 0 ? "" : "=", Setting.videoMaxSecond);
    }

    private void initAlbum(Context context) {
        String selection;
        String[] selectionArgs;
        if (Setting.isOnlyGif() && Setting.showGif) {
            //进gif
            selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + "> " + Setting.minSize
                    + " AND " + MediaStore.MediaColumns.SIZE + "< " + Setting.maxSize
                    + " AND " + MediaStore.MediaColumns.MIME_TYPE + IS_GIF;
            selectionArgs = new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)};
        } else if (Setting.isOnlyImage()) {
            //仅图片
            if (Setting.showGif) {
                selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                        + " AND " + MediaStore.MediaColumns.SIZE + "> " + Setting.minSize
                        + " AND " + MediaStore.MediaColumns.SIZE + "< " + Setting.maxSize;
            } else {
                selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                        + " AND " + MediaStore.MediaColumns.SIZE + "> " + Setting.minSize
                        + " AND " + MediaStore.MediaColumns.SIZE + "< " + Setting.maxSize
                        + " AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF;
            }
            selectionArgs = new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)};
        } else if (Setting.isOnlyVideo()) {
            //仅视频
            selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + "> " + Setting.minSize
                    + " AND " + MediaStore.MediaColumns.SIZE + "< " + Setting.maxSize
                    + " AND " + getDurationCondition();
            selectionArgs = new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)};
        } else if (Setting.isAll()) {
            //全部
            selection = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + (Setting.showGif ? "" : " AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF)
                    + " OR "
                    + (MediaStore.Files.FileColumns.MEDIA_TYPE + "=? AND " + getDurationCondition()) + ")"
                    + " AND " + MediaStore.MediaColumns.SIZE + "> " + Setting.minSize
                    + " AND " + MediaStore.MediaColumns.SIZE + "< " + Setting.maxSize;
            selectionArgs = SELECTION_ALL_ARGS;
        } else {
            throw new RuntimeException("filter types error, please check your filter method! ");
        }

        final ContentResolver contentResolver = context.getContentResolver();
        final Cursor cursor = contentResolver.query(CONTENT_URI, PROJECTIONS, selection, selectionArgs, SORT_ORDER);

        //System.out.println("-----》 " + System.currentTimeMillis());
        if (cursor == null) {
//            Log.d(TAG, "call: " + "Empty photos");
        } else if (cursor.moveToFirst()) {
            final String albumItem_all_name = getAllAlbumName(context);
            final String albumItem_video_name = context.getString(R.string.selector_folder_video_easy_photos);
            final int pathCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
            final int nameCol = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
            final int dateCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED);
            final int mimeTypeCol = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE);
            final int sizeCol = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE);
            final int durationCol = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
            final int widthCol = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH);
            final int heightCol = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT);

            boolean equalsAlbumName = albumItem_video_name.equals(albumItem_all_name);
            do {
                final String path = cursor.getString(pathCol);
                final String type = cursor.getString(mimeTypeCol);
                final long size = cursor.getLong(sizeCol);
                final int width = cursor.getInt(widthCol);
                final int height = cursor.getInt(heightCol);
                final String name = cursor.getString(nameCol);
                final long dateTime = cursor.getLong(dateCol);
                final long duration = cursor.getLong(durationCol);
                final Photo imageItem = new Photo(name, path, dateTime, width, height, size, duration, type);
                // 把图片全部放进“全部”专辑
                album.addAlbumItem(albumItem_all_name, "", path);
                album.getAlbumItem(albumItem_all_name).addImageItem(imageItem);
                if (type.contains(Type.VIDEO)) {
                    // 把视频全部放进“所有视频”专辑
                    if (Setting.showVideo() && !equalsAlbumName) {
                        album.addAlbumItem(albumItem_video_name, "", path, 1);
                        album.getAlbumItem(albumItem_video_name).addImageItem(imageItem);
                    }
                } else if (width < Setting.minWidth || height < Setting.minHeight) {
                    continue;
                }

                // 添加当前图片的专辑到专辑模型实体中
                final File parentFile = new File(path).getParentFile();
                if (parentFile == null) continue;
                final String folderPath = parentFile.getAbsolutePath();
                final String albumName = parentFile.getName();
                album.addAlbumItem(albumName, folderPath, path);
                album.getAlbumItem(albumName).addImageItem(imageItem);
            } while (isQuery && cursor.moveToNext());
            cursor.close();
        }
        //System.out.println("-----》 " + System.currentTimeMillis());
    }

    /**
     * 获取全部专辑名
     *
     * @return 专辑名
     */
    public String getAllAlbumName(Context context) {
        String albumItem_all_name = context.getString(R.string.selector_folder_all_video_photo_easy_photos);
        if (Setting.isOnlyVideo()) {
            albumItem_all_name = context.getString(R.string.selector_folder_video_easy_photos);
        } else if (Setting.isOnlyImage() || Setting.isOnlyGif()) {
            //不显示视频
            albumItem_all_name = context.getString(R.string.selector_folder_all_easy_photos);
        }
        return albumItem_all_name;
    }

    /**
     * 获取当前专辑项目的图片集
     *
     * @return 当前专辑项目的图片集
     */
    public ArrayList<Photo> getCurrAlbumItemPhotos(int currAlbumItemIndex) {
        return album.getAlbumItem(currAlbumItemIndex).photos;
    }

    /**
     * 获取专辑项目集
     *
     * @return 专辑项目集
     */
    public ArrayList<AlbumItem> getAlbumItems() {
        return album.albumItems;
    }

    public interface CallBack {
        void onAlbumWorkedCallBack();
    }

    public void fillPhoto(Context context, Photo photo) {
        String filePath = photo.path;
        final File file = new File(photo.path);
        if (!file.exists()) return;
        String where;
        String[] selectionArgs = null;
        if (filePath.startsWith("content://media/")) {
            where = null;
        } else {
            where = MediaStore.MediaColumns.DATA + "=?";
            selectionArgs = new String[]{filePath};
        }
        Cursor cursor = context.getContentResolver().query(CONTENT_URI, PROJECTIONS, where, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            final int pathCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
            final int nameCol = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
            final int dateCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED);
            final int mimeTypeCol = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE);
            final int sizeCol = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE);
            final int durationCol = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
            final int widthCol = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH);
            final int heightCol = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT);
            photo.path = cursor.getString(pathCol);
            photo.type = cursor.getString(mimeTypeCol);
            photo.size = cursor.getLong(sizeCol);
            photo.width = cursor.getInt(widthCol);
            photo.height = cursor.getInt(heightCol);
            photo.name = cursor.getString(nameCol);
            photo.time = cursor.getLong(dateCol);
            photo.duration = cursor.getLong(durationCol);
        }
    }
}

