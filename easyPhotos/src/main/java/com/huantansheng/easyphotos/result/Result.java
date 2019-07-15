package com.huantansheng.easyphotos.result;

import android.text.TextUtils;

import com.huantansheng.easyphotos.constant.Type;
import com.huantansheng.easyphotos.models.album.entity.Photo;
import com.huantansheng.easyphotos.setting.Setting;

import java.io.File;
import java.util.ArrayList;

/**
 * 存储的返回图片集
 * Created by huan on 2017/10/24.
 */

public class Result {
    public static ArrayList<Photo> photos = new ArrayList<>();

    /**
     * @return 0：添加成功 -4：选择结果互斥 -3：文件不存在 -2：超过视频选择数 -1：超过图片选择数
     */
    public static int addPhoto(Photo photo) {
        final String path = photo.path;
        if (TextUtils.isEmpty(path)) {
            return -3;
        }
        final File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            return -3;
        }
        if (Setting.selectMutualExclusion && photos.size() > 0) {
            if (photo.type.contains(Type.VIDEO) != photos.get(0).type.contains(Type.VIDEO)) {
                return -4;
            }
        }
        if (Setting.videoCount != -1 || Setting.pictureCount != -1) {
            int number = getVideoNumber();
            if (photo.type.contains(Type.VIDEO) && number >= Setting.videoCount) {
                return -2;
            }
            number = photos.size() - number;
            if ((!photo.type.contains(Type.VIDEO)) && number >= Setting.pictureCount) {
                return -1;
            }
        }
        photos.add(photo);
        return 0;
    }

    public static void removePhoto(Photo photo) {
        photos.remove(photo);
    }

    public static void removePhoto(int photoIndex) {
        removePhoto(photos.get(photoIndex));
    }

    public static void removeAll() {
        int size = photos.size();
        for (int i = 0; i < size; i++) {
            removePhoto(0);
        }
    }

    private static int getVideoNumber() {
        int count = 0;
        for (Photo p : photos) {
            if (p.type.contains(Type.VIDEO)) {
                count += 1;
            }
        }
        return count;
    }

    public static void processOriginal() {
        if (Setting.showOriginalMenu) {
            if (Setting.originalMenuUsable) {
                for (Photo photo : photos) {
                    photo.selectedOriginal = Setting.selectedOriginal;
                }
            }
        }
    }

    public static void clear() {
        photos.clear();
    }

    public static boolean isEmpty() {
        return photos.isEmpty();
    }

    public static int count() {
        return photos.size();
    }

    /**
     * 获取选择器应该显示的数字
     *
     * @param photo 当前图片
     * @return 选择器应该显示的数字
     */
    public static String getSelectorNumber(Photo photo) {
        return String.valueOf(photos.indexOf(photo) + 1);
    }

    public static String getPhotoPath(int position) {
        return photos.get(position).path;
    }

    public static String getPhotoType(int position) {
        return photos.get(position).type;
    }

    public static long getPhotoDuration(int position) {
        return photos.get(position).duration;
    }

    public static boolean isSelected(Photo photo) {
        for (Photo p : Result.photos) {
            if (p.path != null && p.path.equals(photo.path)) return true;
        }
        return false;
    }
}
