package com.huantansheng.easyphotos.demo;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.huantansheng.easyphotos.callback.CompressCallback;
import com.huantansheng.easyphotos.engine.CompressEngine;
import com.huantansheng.easyphotos.models.album.entity.Photo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class LubanCompressEngine implements CompressEngine {
    //单例
    private static LubanCompressEngine instance = null;

    //单例模式，私有构造方法
    private LubanCompressEngine() {
    }

    //获取单例
    public static LubanCompressEngine getInstance() {
        if (null == instance) {
            synchronized (LubanCompressEngine.class) {
                if (null == instance) {
                    instance = new LubanCompressEngine();
                }
            }
        }
        return instance;
    }

    private String getPath() {
        String path = Environment.getExternalStorageDirectory() + "/Luban/image/";
        File file = new File(path);
        if (file.mkdirs()) {
            return path;
        }
        return path;
    }

    @Override
    public void compress(final Context context, final ArrayList<Photo> photos, final CompressCallback callback) {
        //TODO demo 演示使用，根据实际使用情况修改
        callback.onStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<String> paths = new ArrayList<>();
                    for (Photo photo : photos) {
                        if (TextUtils.isEmpty(photo.cropPath)) {
                            paths.add(photo.path);
                        } else {
                            paths.add(photo.cropPath);
                        }
                    }

                    List<File> files = Luban.with(context).load(paths)
                            .ignoreBy(100)
                            .setTargetDir(getPath())
                            .filter(new CompressionPredicate() {
                                @Override
                                public boolean apply(String path) {
                                    return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif") || path.toLowerCase().endsWith(".mp4"));
                                }
                            }).get();
                    for (int i = 0, j = photos.size(); i < j; i++) {
                        Photo photo = photos.get(i);
                        photo.compressPath = files.get(i).getPath();
                    }
                    callback.onSuccess(photos);
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onFailed(photos, e.getMessage());
                }
            }
        }).start();
    }
}
