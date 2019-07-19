package com.huantansheng.easyphotos.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.huantansheng.easyphotos.R;
import com.huantansheng.easyphotos.constant.Type;
import com.huantansheng.easyphotos.models.album.entity.Photo;
import com.huantansheng.easyphotos.setting.Setting;
import com.huantansheng.easyphotos.ui.widget.imagezoom.ImageViewTouch;
import com.huantansheng.easyphotos.ui.widget.imagezoom.ImageViewTouchBase;

import java.io.File;
import java.util.ArrayList;


/**
 * 大图预览界面图片集合的适配器
 * Created by huan on 2017/10/26.
 */
public class PreviewPhotosAdapter extends PagerAdapter {

    private ArrayList<Photo> photos;
    private OnClickListener listener;
    private LayoutInflater inflater;

    public interface OnClickListener {
        void onPhotoClick();

        void onPhotoScaleChanged();
    }


    public PreviewPhotosAdapter(Context cxt, ArrayList<Photo> photos, OnClickListener listener) {
        this.photos = photos;
        this.inflater = LayoutInflater.from(cxt);
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        return bindItem(container, position);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    private Object bindItem(ViewGroup container, int position) {
        View view = inflater.inflate(R.layout.item_preview_photo_easy_photos, container, false);
        PreviewPhotosViewHolder holder = new PreviewPhotosViewHolder(view);

        final String path = photos.get(position).path;
        final String type = photos.get(position).type;

        holder.ivPlay.setVisibility(View.GONE);
        if (type.contains(Type.VIDEO)) {
            Setting.imageEngine.loadPhoto(holder.ivPhoto.getContext(), path, holder.ivPhoto);
            holder.ivPlay.setVisibility(View.VISIBLE);
            holder.ivPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toPlayVideo(v, path, type);
                }
            });
        } else if (path.endsWith(Type.GIF) || type.endsWith(Type.GIF)) {
            Setting.imageEngine.loadGif(holder.ivPhoto.getContext(), path, holder.ivPhoto);
        } else {
            Setting.imageEngine.loadPhoto(holder.ivPhoto.getContext(), path, holder.ivPhoto);
        }
        holder.ivPhoto.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
        holder.ivPhoto.setOnScaleChangeListener(new ImageViewTouchBase.OnScaleChangeListener() {
            @Override
            public void onScaleChanged(Float scale, float centerX, float centerY) {
                //暂时去掉
                listener.onPhotoScaleChanged();
            }
        });
        holder.ivPhoto.setSingleTapListener(new ImageViewTouch.OnImageViewTouchSingleTapListener() {
            @Override
            public void onSingleTapConfirmed() {
                listener.onPhotoClick();
            }
        });

        container.addView(view);

        return view;
    }

    private void toPlayVideo(View v, String path, String type) {
        Context context = v.getContext();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = getUri(context, path, intent);
        intent.setDataAndType(uri, type);
        context.startActivity(intent);
    }

    private Uri getUri(Context context, String path, Intent intent) {
        File file = new File(path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            return FileProvider.getUriForFile(context, Setting.fileProviderAuthority, file);
        } else {
            return Uri.fromFile(file);
        }
    }


    public class PreviewPhotosViewHolder {

        public ImageViewTouch ivPhoto;
        ImageView ivPlay;

        PreviewPhotosViewHolder(View itemView) {
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            ivPlay = itemView.findViewById(R.id.iv_play);
        }
    }
}