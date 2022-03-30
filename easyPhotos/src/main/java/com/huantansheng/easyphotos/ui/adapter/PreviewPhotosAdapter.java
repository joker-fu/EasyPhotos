package com.huantansheng.easyphotos.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.chrisbanes.photoview.PhotoView;
import com.huantansheng.easyphotos.R;
import com.huantansheng.easyphotos.constant.Type;
import com.huantansheng.easyphotos.models.album.entity.Photo;
import com.huantansheng.easyphotos.setting.Setting;
import com.huantansheng.easyphotos.ui.widget.subscaleview.ImageSource;
import com.huantansheng.easyphotos.ui.widget.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.util.ArrayList;


/**
 * 大图预览界面图片集合的适配器
 * Created by huan on 2017/10/26.
 */
public class PreviewPhotosAdapter extends RecyclerView.Adapter<PreviewPhotosAdapter.PreviewPhotosViewHolder> {
    private final ArrayList<Photo> photos;
    private final OnClickListener listener;
    private final LayoutInflater inflater;

    public interface OnClickListener {
        void onPhotoClick();

        void onPhotoScaleChanged();
    }

    public PreviewPhotosAdapter(Context cxt, ArrayList<Photo> photos, OnClickListener listener) {
        this.photos = photos;
        this.inflater = LayoutInflater.from(cxt);
        this.listener = listener;
    }

    @NonNull
    @Override
    public PreviewPhotosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PreviewPhotosViewHolder(inflater.inflate(R.layout.item_preview_photo_easy_photos, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final PreviewPhotosViewHolder holder, int position) {
        final String path = photos.get(position).filePath;
        final String type = photos.get(position).type;
        final double ratio = (double) photos.get(position).height / (double) photos.get(position).width;

        holder.ivPlay.setVisibility(View.GONE);
        holder.ivPhoto.setVisibility(View.GONE);
        holder.ivBigPhoto.setVisibility(View.GONE);

        if (type.contains(Type.VIDEO)) {
            holder.ivPhoto.setVisibility(View.VISIBLE);
            Setting.imageEngine.loadPhoto(holder.ivPhoto.getContext(), path, holder.ivPhoto);
            holder.ivPlay.setVisibility(View.VISIBLE);
            holder.ivPlay.setOnClickListener(v -> toPlayVideo(v, path, type));
        } else if (path.endsWith(Type.GIF) || type.endsWith(Type.GIF)) {
            holder.ivPhoto.setVisibility(View.VISIBLE);
            Setting.imageEngine.loadGif(holder.ivPhoto.getContext(), path, holder.ivPhoto);
        } else {
            if (ratio > 3 || ratio < 0.34) {
                holder.ivBigPhoto.setVisibility(View.VISIBLE);
                holder.ivBigPhoto.setImage(ImageSource.uri(path));
            } else {
                holder.ivPhoto.setVisibility(View.VISIBLE);
                Setting.imageEngine.loadPhoto(holder.ivPhoto.getContext(), path, holder.ivPhoto);
            }
        }

        holder.ivBigPhoto.setOnClickListener(v -> listener.onPhotoClick());
        holder.ivBigPhoto.setOnStateChangedListener(new SubsamplingScaleImageView.OnStateChangedListener() {
            @Override
            public void onScaleChanged(float newScale, int origin) {
                listener.onPhotoScaleChanged();
            }

            @Override
            public void onCenterChanged(PointF newCenter, int origin) {

            }
        });
        holder.ivPhoto.setScale(1f);
        holder.ivPhoto.setOnViewTapListener((view, x, y) -> listener.onPhotoClick());
        holder.ivPhoto.setOnScaleChangeListener((scaleFactor, focusX, focusY) -> listener.onPhotoScaleChanged());
    }

    private void toPlayVideo(View v, String path, String type) {
        if (Setting.videoPreviewCallback != null) {
            Setting.videoPreviewCallback.callback(v, path, type);
        } else {
            Context context = v.getContext();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            final Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = Uri.parse(path);
            } else {
                uri = Uri.fromFile(new File(path));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
            }
            intent.setDataAndType(uri, type);
            context.startActivity(intent);
        }
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class PreviewPhotosViewHolder extends RecyclerView.ViewHolder {
        public PhotoView ivPhoto;
        public SubsamplingScaleImageView ivBigPhoto;
        ImageView ivPlay;

        PreviewPhotosViewHolder(View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            ivBigPhoto = itemView.findViewById(R.id.iv_big_photo);
            ivPlay = itemView.findViewById(R.id.iv_play);
            ivPhoto.setMaximumScale(5f);
            ivPhoto.setMediumScale(3f);
            ivPhoto.setMinimumScale(1f);

            ivBigPhoto.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START);
            ivBigPhoto.setMaxScale(5f);
            ivBigPhoto.setMinScale(0.8f);
        }
    }
}