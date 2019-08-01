package com.huantansheng.easyphotos.ui.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.huantansheng.easyphotos.ui.widget.imagezoom.ImageViewTouch;

/**
 * 图片预览 ViewPager
 */
public class PreviewViewPager extends ViewPager {


    public PreviewViewPager(@NonNull Context context) {
        super(context);
    }

    public PreviewViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof ImageViewTouch) {
            return ((ImageViewTouch) v).canScroll(dx) || super.canScroll(v, checkV, dx, x, y);
        }
        return super.canScroll(v, checkV, dx, x, y);
    }

}
