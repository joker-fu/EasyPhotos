package com.huantansheng.easyphotos.models.album.entity;

import java.util.ArrayList;

/**
 * 专辑项目实体类
 * Created by huan on 2017/10/20.
 */

public class AlbumItem {
    public String name;
    public String coverImagePath;
    public ArrayList<Photo> photos;

    AlbumItem(String name, String coverImagePath) {
        this.name = name;
        this.coverImagePath = coverImagePath;
        this.photos = new ArrayList<>();
    }

    public void addImageItem(Photo imageItem) {
        this.photos.add(imageItem);
    }

    public void addImageItem(int index, Photo imageItem) {
        this.coverImagePath = imageItem.path;
        this.photos.add(index, imageItem);
    }
}
