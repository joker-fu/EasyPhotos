package com.huantansheng.easyphotos.engine;

import android.content.Context;

import com.huantansheng.easyphotos.callback.CompressCallback;
import com.huantansheng.easyphotos.models.album.entity.Photo;

import java.util.ArrayList;

/**
 * 图片压缩方式
 * Created by joker on 2019/8/1.
 */
public interface CompressEngine {
    /**
     * 压缩处理
     *
     * @param photos 选择的图片
     */
    void compress(Context context, ArrayList<Photo> photos, CompressCallback callback);
}
