package com.huantansheng.easyphotos.constant;

/**
 * Created by huan on 2018/1/9.
 */

public class Type {

    public static final String IMAGE = "image";
    public static final String VIDEO = "video";
    public static final String GIF = "gif";

    public static String[] all() {
        return new String[]{IMAGE, VIDEO};
    }

    public static String[] image() {
        return new String[]{IMAGE};
    }

    public static String[] gif() {
        return new String[]{GIF};
    }

    public static String[] video() {
        return new String[]{VIDEO};
    }
}
