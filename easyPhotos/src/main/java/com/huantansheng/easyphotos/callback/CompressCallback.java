package com.huantansheng.easyphotos.callback;

import com.huantansheng.easyphotos.models.album.entity.Photo;

import java.util.ArrayList;

public interface CompressCallback {
    /**
     * 压缩开始
     */
    void onStart();

    /**
     * 压缩成功
     *
     * @param photos 压缩结果
     */
    void onSuccess(ArrayList<Photo> photos);

    /**
     * 压缩失败
     *
     * @param photos  压缩结果
     * @param message 压缩失败原因
     */
    void onFailed(ArrayList<Photo> photos, String message);
}
