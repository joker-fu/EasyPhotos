package com.huantansheng.easyphotos.callback;

import android.view.View;

/**
 * SelectCallback
 *
 * @author joker
 * @date 2019/7/29.
 */
public abstract class VideoPreviewCallback {
    /**
     * 选择结果回调
     *
     * @param v    返回预览View
     * @param path 返回图片地址
     * @param type 返回图片类型
     */
    public abstract void callback(View v, String path, String type);
}
