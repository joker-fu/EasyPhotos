package com.huantansheng.easyphotos.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.huantansheng.easyphotos.R;
import com.huantansheng.easyphotos.constant.Type;
import com.huantansheng.easyphotos.models.album.entity.Photo;
import com.huantansheng.easyphotos.setting.Setting;
import com.huantansheng.easyphotos.ui.widget.subscaleview.ImageSource;
import com.huantansheng.easyphotos.ui.widget.subscaleview.SubsamplingScaleImageView;

import java.util.ArrayList;


/**
 * 大图预览界面图片集合的适配器
 * Created by huan on 2017/10/26.
 */
public class PreviewPhotosAdapter extends RecyclerView.Adapter<PreviewPhotosAdapter.PreviewViewHolder> {

    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_IMAGE = 1;

    private final ArrayList<Photo> photos;
    private final OnClickListener listener;
    private final LayoutInflater inflater;
    private final ArrayList<PreviewViewHolder> holders = new ArrayList<>();

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
    public void onViewAttachedToWindow(@NonNull PreviewViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.onViewAttachedToWindow();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull PreviewViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onViewDetachedFromWindow();
    }

    @NonNull
    @Override
    public PreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PreviewPhotosAdapter.PreviewViewHolder holder;
        if (viewType == TYPE_VIDEO) {
            holder = new PreviewVideosViewHolder(inflater.inflate(R.layout.item_preview_video_easy_photos, parent, false));
        } else {
            holder = new PreviewPhotosViewHolder(inflater.inflate(R.layout.item_preview_photo_easy_photos, parent, false));
        }
        holder.setOnClickListener(listener);
        holders.add(holder);
        return holder;
    }

    @Override
    public int getItemViewType(int position) {
        final String type = photos.get(position).type;
        if (type.contains(Type.VIDEO)) {
            return TYPE_VIDEO;
        }
        return TYPE_IMAGE;
    }

    @Override
    public void onBindViewHolder(@NonNull final PreviewViewHolder holder, int position) {
        holder.onBind(photos.get(position), position);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public void destroy() {
        for (PreviewViewHolder holder : holders) {
            holder.destroy();
        }
        holders.clear();
    }

    public abstract static class PreviewViewHolder extends RecyclerView.ViewHolder {
        protected OnClickListener listener;
        protected PhotoView ivPhoto;

        public PreviewViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public abstract void onBind(Photo photo, int position);

        public void setOnClickListener(OnClickListener listener) {
            this.listener = listener;
        }

        public void onViewAttachedToWindow() {

        }

        public void onViewDetachedFromWindow() {

        }

        public void reset() {
            if (ivPhoto != null && ivPhoto.getScale() != 1f) {
                ivPhoto.setScale(1f, true);
            }
        }

        public void destroy() {

        }
    }

    public static class PreviewPhotosViewHolder extends PreviewViewHolder {
        private final SubsamplingScaleImageView ivBigPhoto;

        PreviewPhotosViewHolder(View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            ivBigPhoto = itemView.findViewById(R.id.iv_big_photo);
            ivPhoto.setMaximumScale(5f);
            ivPhoto.setMediumScale(3f);
            ivPhoto.setMinimumScale(1f);

            ivBigPhoto.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START);
            ivBigPhoto.setMaxScale(5f);
            ivBigPhoto.setMinScale(0.8f);
        }

        @Override
        public void onBind(Photo photo, int position) {
            final String path = photo.getAvailablePath();
            final String type = photo.type;
            final double ratio = (double) photo.height / (double) photo.width;

            ivPhoto.setVisibility(View.GONE);
            ivBigPhoto.setVisibility(View.GONE);

            if (path.endsWith(Type.GIF) || type.endsWith(Type.GIF)) {
                ivPhoto.setVisibility(View.VISIBLE);
                Setting.imageEngine.loadGif(ivPhoto.getContext(), path, ivPhoto);
            } else {
                if (ratio > 3 || ratio < 0.34) {
                    ivBigPhoto.setVisibility(View.VISIBLE);
                    ivBigPhoto.setImage(ImageSource.uri(path));
                } else {
                    ivPhoto.setVisibility(View.VISIBLE);
                    Setting.imageEngine.loadPhoto(ivPhoto.getContext(), path, ivPhoto);
                }
            }

            ivBigPhoto.setOnClickListener(v -> listener.onPhotoClick());
            ivBigPhoto.setOnStateChangedListener(new SubsamplingScaleImageView.OnStateChangedListener() {
                @Override
                public void onScaleChanged(float newScale, int origin) {
                    listener.onPhotoScaleChanged();
                }

                @Override
                public void onCenterChanged(PointF newCenter, int origin) {

                }
            });
            ivPhoto.setScale(1f);
            ivPhoto.setOnViewTapListener((view, x, y) -> listener.onPhotoClick());
            ivPhoto.setOnScaleChangeListener((scaleFactor, focusX, focusY) -> listener.onPhotoScaleChanged());
        }

        @Override
        public void reset() {
            super.reset();
            if (ivBigPhoto != null && ivBigPhoto.getScale() != 1f) {
                ivBigPhoto.resetScaleAndTop();
            }
        }
    }

    public static class PreviewVideosViewHolder extends PreviewViewHolder {
        private StyledPlayerView playerView;
        private final ProgressBar progressBar;
        private final ImageView ivPlay;

        private final Player.Listener playerListener = new Player.Listener() {

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying)
                    playingUI();
                else
                    resetUI();
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                resetUI();
            }

        };

        PreviewVideosViewHolder(View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            ivPlay = itemView.findViewById(R.id.iv_play);
            playerView = itemView.findViewById(R.id.player_view);
            progressBar = itemView.findViewById(R.id.progress_bar);
            playerView.setUseController(false);
            ivPhoto.setMaximumScale(5f);
            ivPhoto.setMediumScale(3f);
            ivPhoto.setMinimumScale(1f);
        }

        @Override
        public void onBind(Photo photo, int position) {
            Setting.imageEngine.loadPhoto(ivPhoto.getContext(), photo.getAvailablePath(), ivPhoto);

            ivPhoto.setVisibility(View.VISIBLE);
            ivPlay.setVisibility(View.VISIBLE);
            ivPlay.setOnClickListener(v -> toPlayVideo(v, photo));

            ivPhoto.setScale(1f);
            ivPhoto.setOnViewTapListener((view, x, y) -> listener.onPhotoClick());
            ivPhoto.setOnScaleChangeListener((scaleFactor, focusX, focusY) -> listener.onPhotoScaleChanged());
        }

        private void toPlayVideo(View v, Photo photo) {
            if (Setting.videoPreviewCallback != null) {
                Setting.videoPreviewCallback.callback(v, photo.getAvailablePath(), photo.type);
            } else {
                Player player = playerView.getPlayer();
                if (player != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    ivPlay.setVisibility(View.GONE);
                    MediaItem mediaItem = MediaItem.fromUri(photo.getAvailableUri());
                    player.setMediaItem(mediaItem);
                    player.prepare();
                    player.play();
                } else {
                    Context context = v.getContext();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.putExtra(Intent.EXTRA_STREAM, photo.getAvailableUri());
                    }
                    intent.setDataAndType(photo.getAvailableUri(), photo.type);
                    context.startActivity(intent);
                }
            }
        }

        private void resetUI() {
            ivPlay.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            ivPhoto.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.GONE);
        }

        private void playingUI() {
            if (progressBar.getVisibility() == View.VISIBLE) {
                progressBar.setVisibility(View.GONE);
            }
            if (ivPlay.getVisibility() == View.VISIBLE) {
                ivPlay.setVisibility(View.GONE);
            }
            if (playerView.getVisibility() == View.GONE) {
                playerView.setVisibility(View.VISIBLE);
            }
            if (ivPhoto.getVisibility() == View.VISIBLE) {
                ivPhoto.setVisibility(View.GONE);
            }
        }

        @Override
        public void onViewAttachedToWindow() {
            Player player = new ExoPlayer.Builder(itemView.getContext()).build();
            playerView.setPlayer(player);
            player.addListener(playerListener);
        }

        @Override
        public void onViewDetachedFromWindow() {
            Player player = playerView.getPlayer();
            if (player != null) {
                player.removeListener(playerListener);
                player.release();
                playerView.setPlayer(null);
                resetUI();
            }
        }

        public void destroy() {
            Player player = playerView.getPlayer();
            if (player != null) {
                player.removeListener(playerListener);
                player.release();
                playerView = null;
            }
        }
    }
}