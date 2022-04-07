package com.huantansheng.easyphotos.utils.provider;

import android.app.Application;

import androidx.core.content.FileProvider;

import com.huantansheng.easyphotos.EasyPhotos;

public class EasyPhotosFileProvider extends FileProvider {
    @Override
    public boolean onCreate() {
        EasyPhotos.init((Application) getContext().getApplicationContext());
        return true;
    }
}
