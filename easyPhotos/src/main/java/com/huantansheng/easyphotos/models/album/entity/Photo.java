package com.huantansheng.easyphotos.models.album.entity;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.huantansheng.easyphotos.utils.uri.UriUtils;

/**
 * 图片item实体类
 * Created by huan on 2017/10/20.
 */

public class Photo implements Parcelable {
    private static final String TAG = "Photo";
    public String name;//图片名称
    public String filePath;//原图片路径
    public Uri fileUri;//原图片uri
    public String cropPath;//图片裁剪路径
    public String compressPath; //图片压缩路径
    public String type;//图片类型
    public int width;//图片宽度
    public int height;//图片高度
    public long size;//图片文件大小，单位：Bytes
    public long duration;//视频时长，单位：毫秒
    public long time;//图片拍摄的时间戳,单位：毫秒
    public boolean selectedOriginal;//用户选择时是否选择了原图选项

    public Photo(String name, String path, Uri uri, long time, int width, int height, long size, long duration, String type) {
        this.name = name;
        this.filePath = path;
        this.fileUri = uri;
        this.time = time;
        this.width = width;
        this.height = height;
        this.type = type;
        this.size = size;
        this.duration = duration;
        this.selectedOriginal = false;
    }

    public String getAvailablePath() {
        String path;
        if (!TextUtils.isEmpty(compressPath)) {
            path = compressPath;
        } else if (!TextUtils.isEmpty(cropPath)) {
            path = cropPath;
        } else {
            path = filePath;
        }
        return path;
    }

    public Uri getAvailableUri() {
        String path = getAvailablePath();
        return UriUtils.getUriByPath(path);
    }

    @Override
    public String toString() {
        return "Photo{" +
                "name='" + name + '\'' +
                ", path='" + filePath + '\'' +
                ", uri='" + fileUri + '\'' +
                ", cropPath='" + cropPath + '\'' +
                ", compressPath='" + compressPath + '\'' +
                ", time=" + time + '\'' +
                ", minWidth=" + width + '\'' +
                ", minHeight=" + height +
                '}';
    }

    protected Photo(Parcel in) {
        name = in.readString();
        filePath = in.readString();
        fileUri = in.readParcelable(Uri.class.getClassLoader());
        cropPath = in.readString();
        compressPath = in.readString();
        type = in.readString();
        width = in.readInt();
        height = in.readInt();
        size = in.readLong();
        duration = in.readLong();
        time = in.readLong();
        selectedOriginal = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(filePath);
        dest.writeParcelable(fileUri, flags);
        dest.writeString(cropPath);
        dest.writeString(compressPath);
        dest.writeString(type);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeLong(size);
        dest.writeLong(duration);
        dest.writeLong(time);
        dest.writeByte((byte) (selectedOriginal ? 1 : 0));
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        try {
            Photo other = (Photo) o;
            return this.filePath != null && this.filePath.equals(other.filePath);
        } catch (ClassCastException e) {
            Log.e(TAG, "equals: " + Log.getStackTraceString(e));
        }
        return super.equals(o);
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
