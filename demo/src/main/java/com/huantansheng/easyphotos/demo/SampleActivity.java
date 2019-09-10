package com.huantansheng.easyphotos.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huantansheng.easyphotos.EasyPhotos;
import com.huantansheng.easyphotos.callback.PuzzleCallback;
import com.huantansheng.easyphotos.callback.SelectCallback;
import com.huantansheng.easyphotos.constant.Type;
import com.huantansheng.easyphotos.models.album.entity.Photo;
import com.huantansheng.easyphotos.setting.Setting;

import java.util.ArrayList;

public class SampleActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * 选择的图片集
     */
    private ArrayList<Photo> selectedPhotoList = new ArrayList<>();
    private ArrayList<String> selectedPhotoPathList = new ArrayList<>();
    private MainAdapter adapter;
    private RecyclerView rvImage;

    /**
     * 图片列表和专辑项目列表的广告view
     */
    private RelativeLayout photosAdView, albumItemsAdView;

    /**
     * 广告是否加载完成
     */
    private boolean photosAdLoaded = false, albumItemsAdLoaded = false;

    /**
     * 展示bitmap功能的
     */
    private Bitmap bitmap = null;
    private ImageView bitmapView = null;
    private DrawerLayout drawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        initView();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
        drawer.clearAnimation();
        drawer.setAnimation(null);
        drawer.setLayoutAnimation(null);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.clearAnimation();
        navigationView.setAnimation(null);
        navigationView.setLayoutAnimation(null);

        bitmapView = findViewById(R.id.iv_image);
        bitmapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmapView.setVisibility(View.GONE);
            }
        });

        rvImage = (RecyclerView) findViewById(R.id.rv_image);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        adapter = new MainAdapter(this, selectedPhotoList);
        rvImage.setLayoutManager(linearLayoutManager);
        rvImage.setAdapter(adapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvImage);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SampleFragments.class);
            startActivity(intent);
        }
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    private SelectCallback callback = new SelectCallback() {
        @Override
        public void onResult(ArrayList<Photo> photos, ArrayList<String> paths, boolean isOriginal) {
            selectedPhotoList.clear();
            selectedPhotoList.addAll(photos);
            selectedPhotoPathList.clear();
            selectedPhotoPathList.addAll(paths);
            adapter.notifyDataSetChanged();
            rvImage.smoothScrollToPosition(0);
        }
    };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        bitmapView.setVisibility(View.GONE);

        int id = item.getItemId();
        switch (id) {
            case R.id.camera://单独使用相机
                EasyPhotos.createCamera(this)
                        .start(callback);
                break;
            case R.id.camera_cover://单独使用相机带覆盖层
                EasyPhotos.createCamera(this)
                        .setCameraCoverView(View.inflate(this, R.layout.layout_shoot_bg, null))
                        .start(callback);
                break;
            case R.id.album_single://相册单选，无相机功能
                EasyPhotos.createAlbum(this, false, GlideEngine.getInstance())
                        .start(callback);
                break;

            case R.id.album_multi://相册多选，无相机功能
                EasyPhotos.createAlbum(this, false, GlideEngine.getInstance())
                        .setCount(9)
                        .start(callback);
                break;

            case R.id.album_camera_single://相册单选，有相机功能，带裁剪
                EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                        .isCrop(true)
                        //.enableSingleCheckedBack(true)
                        .setFreeStyleCropEnabled(true)
                        .start(callback);
                break;

            case R.id.album_camera_multi://相册多选，有相机功能
                EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                        .setCount(22)
                        .start(callback);
                break;

            case R.id.album_ad://相册中包含广告
                // 需要在启动前创建广告view
                // 广告view不能有父布局
                // 广告view可以包含子布局
                // 广告View的数据可以在任何时候绑定
                initAdViews();
                //启动方法，装载广告view
                EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                        .setCount(9)
                        .setCameraLocation(Setting.LIST_FIRST)
                        .setAdView(photosAdView, photosAdLoaded, albumItemsAdView, albumItemsAdLoaded)
                        .start(callback);
                break;

            case R.id.album_size://只显示限制尺寸或限制文件大小以上的图片
                EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                        .setCount(9)
                        .setMinWidth(500)
                        .setMinHeight(500)
                        .setMinFileSize(1024 * 10)
                        .setMaxFileSize(1024 * 100)
                        .start(callback);
                break;

            case R.id.album_original_usable://显示原图按钮，并且默认选中，按钮可用
                EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                        .setCount(9)
                        .setOriginalMenu(true, true, null)
                        .start(callback);
                break;

            case R.id.album_original_unusable://显示原图按钮，并且默认不选中，按钮不可用。使用场景举例：仅VIP可以上传原图
                boolean isVip = false;//假设获取用户信息发现该用户不是vip
                EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                        .setCount(9)
                        .setOriginalMenu(false, isVip, "该功能为VIP会员特权功能")
                        .start(callback);
                break;

            case R.id.album_has_video_gif://相册中显示视频和gif图
                EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                        .setCount(9)
                        .filter(Type.all())
                        .setGif(true)
                        .setSelectMutualExclusion(true) //选择结果互斥（不能同时选择图片或视频）
                        .isCompress(true)
                        .setCompressEngine(LubanCompressEngine.getInstance())
                        .start(callback);
                break;

            case R.id.album_only_video://相册中只选择视频(相册只有视频 会禁用拼图 裁剪 拍照)
                EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                        .setCount(9)
                        .filter(Type.video())
                        .start(callback);
                break;


            case R.id.album_only_gif://相册中只选择视频(相册只有动图 会禁用拼图 裁剪 相机)
                EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                        .setGif(true) //注意需要显示gif
                        .filter(Type.gif())
                        .start(callback);
                break;

            case R.id.album_no_menu://相册中不显示底部的编辑图标按钮
                EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                        .setCount(9)
                        .setPuzzleMenu(false)
                        .setCleanMenu(false)
                        .start(callback);
                break;

            case R.id.album_selected://相册中包含默认勾选图片
                EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                        .setPuzzleMenu(false)
                        .setCount(9)
                        //推荐使用这种方式
                        .setSelectedPhotos(selectedPhotoList)
//                        .setSelectedPhotoPaths(selectedPhotoPathList)//两种方式参数类型不同，根据情况任选
                        .start(callback);
                break;

            case R.id.addWatermark: //给图片添加水印
                if (selectedPhotoList.isEmpty()) {
                    Toast.makeText(this, "没选图片", Toast.LENGTH_SHORT).show();
                    return true;
                }

                //这一步如果图大的话会耗时，但耗时不长，你可以在异步操作。另外copy出来的bitmap在确定不用的时候记得回收，如果你用Glide操作过copy出来的bitmap那就不要回收了，否则Glide会报错。
                Bitmap watermark = BitmapFactory.decodeResource(getResources(), R.drawable.watermark).copy(Bitmap.Config.RGB_565, true);
                bitmap = BitmapFactory.decodeFile(selectedPhotoList.get(0).path).copy(Bitmap.Config.ARGB_8888, true);

                //给图片添加水印的api
                EasyPhotos.addWatermark(watermark, bitmap, 1080, 20, 20, true);

//                EasyPhotos.addWatermarkWithText(watermark, bitmap, 1080, "aaa\r\n这一步如果图大的话会耗时，但耗时不长，你可以在异步操作。另外copy出来的bitmap在确定不用的时候记得回收，如果你用Glide操作过copy出来的bitmap那就不要回收了，否则Glide会报错。", 20, 20, true);

                bitmapView.setVisibility(View.VISIBLE);
                bitmapView.setImageBitmap(bitmap);
                Toast.makeText(SampleActivity.this, "水印在左下角", Toast.LENGTH_SHORT).show();
                break;

            case R.id.puzzle:
                EasyPhotos.createAlbum(this, false, GlideEngine.getInstance())
                        .setCount(9)
                        .setPuzzleMenu(false)
                        .start(new SelectCallback() {
                            @Override
                            public void onResult(ArrayList<Photo> photos, ArrayList<String> paths, boolean isOriginal) {
                                EasyPhotos.startPuzzleWithPhotos(SampleActivity.this, photos, Environment.getExternalStorageDirectory().getAbsolutePath(), "AlbumBuilder", false, GlideEngine.getInstance(), new PuzzleCallback() {
                                    @Override
                                    public void onResult(Photo photo, String path) {
                                        selectedPhotoList.clear();
                                        selectedPhotoList.add(photo);
                                        adapter.notifyDataSetChanged();
                                        rvImage.smoothScrollToPosition(0);
                                    }
                                });
                            }
                        });
                break;

            case R.id.previewPictures:
//                ArrayList<Photo> photos = new ArrayList<>();
//                photos.add(new Photo("1", "/storage/emulated/0/DCIM/hschefu/picture/IMG20190723_092507_cropped1563845115003.jpg", "image/jpg"));
//                photos.add(new Photo("2", "/storage/emulated/0/DCIM/hschefu/picture/IMG20190723_092507.jpg", "image/png"));
//                photos.add(new Photo("3", "/storage/emulated/0/DCIM/hschefu/picture/IMG20190722_154809_cropped1563781696014.jpg", "image/jpg"));
//                photos.add(new Photo("4", "/storage/emulated/0/DCIM/EasyPhotosDemo/VIDEO_20190715_11:01:59.mp4", "video/mp4"));
//                photos.add(new Photo("5", "/storage/emulated/0/DCIM/Screenshots/Screenshot_20190522-175523_-TS_cropped1558518949108_cropped1558669931684_cropped1559025812762_cropped1559112789402_cropped1559271958299_cropped1559629722074_cropped1562830607837_cropped1563781429432.jpg", "image/jpg"));
//                EasyPhotos.startPreviewPhotos(this, GlideEngine.getInstance(), photos,false);
                ArrayList<String> paths = new ArrayList<>();
                paths.add("https://www.baidu.com/img/bd_logo1.png?where=super");
                paths.add("data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCADcANwDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwDxqiiivSOIKKKKACiiigAop8MMlxMkMMbSSOQqooyST2Ar0vQfhM80KT63dNCWGfs8GNw+rHj8h+NZzqRgryY0m9jzGivc/wDhV/hny9vkXGf73nnP+FYGsfCJfLaTR75tw5ENzjn6MP8ACso4um3YfIzyuirWo6beaTePaX1u8Ey9VYdfcHuPeqtdCd9USFFFFMAooooAKKKVVZ2CqCWPAAHJoASiugsPBHiPUVDwaXMqHo02Ix/49ipbzwD4msYy76W8ijqYWWQ/kDmo9pC9rhZnNUU6SN4nZJEZHU4KsMEU2rAKKKKACiiigAooooAKKKKACiiigAooooA9a+FfhqNLRteuUDSyEpbZH3VHBb6k5H4e9emVm+HrZbPw3ptugwEtowfrtGf1rSrxqs3ObbN0rIKK8t8X/E2aC7ksNCKARkq92QGye4QHjHuf/r1wreLvETS+Ydavt3oJiB+XStYYScld6EuaR7n4j8NWPiXTzbXaBZFBMUyj5oz7e3qK8C1rR7vQtUlsLxNskZ4YdHXsw9jXX6D8U9UspUj1YC9tuhcALIo9scH8fzrsPFWk2PjnwuuoaW6zXMKl4HXq396M+h9vWtabnQlyz2YnaWx4hRSkEEgjBHUGkrvMwoorqPBHhN/E+qnzty2EGGnccbvRR7n+VTKSiuZglcXwl4IvvE8nnEm3sFOHnYfe9lHc/oK9l0TwvpGgRBbG0QSYwZnG6Rvx/oOKtT3Om6BpyedLBZ2kS7UBIUADsB3rj9Q+LOi2zFLO3uLwj+LHlqfz5/SvNnOrWfurQ1SUTvqK8/0/4taPcyBL21uLTP8AHxIo+uOf0rubO9ttQtUubSdJoHGVdDkGsZ05Q+JFJpmT4k8Kab4ktGS5iVLkD93cKPnQ/wBR7GvAdU0240jU7iwul2zQvtbHQ+hHsRzX03XjXxctki8RWlwoAaa3w/uVY8/kf0rpwlR83I9iJrS559RRRXomYUUUUAFFFFABRRRQAUUUUAFFFFAH0l4bulvfDOmXCnO+2TP1AAP6g1Q8dao+k+EL6eFiszqIUI6gscE/lmue+E+tLc6PNpMjfvbVi8YPeNj/AEOfzFaXxPgebwVOyjPlSxu30zj+teTyctblfc2v7tzwuiiivWMQrpfBviubwzqqszM1jMQLiL2/vD3H61zVFTKKkrME7HZfEXRYrDWU1Oz2tY6ivnIy/d3dTj65B/GuNrt9Huv+Eh8C6hoUx3XWnr9rsyepUfeX8ifz9q4iopXS5X0G+4qgswVQSScACvaVvrL4b+C7aCRVk1CVd/lA8vIepP8AsjgZ9q868CaamoeKbd58C2tAbqZj0CpyM/jiqXibXJfEOu3F9IT5ZO2FD/Ag6D+v1JqKkfaSUei3GnZXK+sa3f67etd387Sufur0VB6KOwrPoordJJWRIV23wz16bTvEcens5NpenYyE8B8fKw9+Mfj7VxNdJ4Cs3vfGunKo4ik81j6BRn+eB+NRVScHca3PoKvD/ilqC3ni4wIcraQrEf8AePzH+YH4V7Hq2pQ6PpVzqFwcRwIWx6nsPxOBXzbeXct/ez3c7bpZpDI59yc1xYOF5ORc30IKKKK9EzCiiigAooooAKKKKACiiigAooooA1PD2tTeH9at9QhyfLOHTP30PUV7/ILLxN4ddY5BJaXsBAYdsj+YP6ivmyu08BeNG8PXX2K9Zm02ZsnuYW/vD29R+P15cRRclzR3RUZW0Zyl/ZT6bqE9lcrtmgcow9x/Sq1evfEbwqusWaeINKCzSpGDKI+fNjxwwx1IH6fSvIa1pVFUjcTVmFFFFaiNTw7qZ0fX7O9/5ZpIBIPVDww/ImodZsv7N1q9sx92GZlU+q54P5YqjWv4gb7RLY3vU3NnGzH1ZMxn9Uz+NRa0rh0NnTXGjfDvUbz7txqkwtIj38tRlj+PIrj66TxRKYbDQtLXhbaxWVh6PKd5/QrXN0qa0cu42FFFFaCCvXfhPoLW1jPrU6Ye4/dw5/uA8n8SP/Ha4zwZ4OuPE16JZQ0emxN+9l6bv9lff+VdF438cxRQHQPD7LHbxr5Us8fTA42J7eprlrNz/dx+ZUdNWUviT4uXVrsaTYybrO3bMjqeJJP8B/P8K4Ciit6cFCPKiW7sKKKKsAooooAKKKKACiiigAooooAKKKKACiiigDpvC/jfUvDTCJD9osSctbyHge6n+E/p7VrajpOheLXa98O3MdpqD8yadcEJvb/YPTPt/KuDo6Vk6avzR0Y79Czfafeabctb3ttLBKOqyLj8vWq1bdr4q1KG3FrcmLULQf8ALC8TzAPoTyv4GlZvDt+c7LrS5T2X9/F+uGH61XM1uhGHWw8TXnhzTygzJFdSW/4MFZf130v/AAjlxNzp11aX47LBLh/++Gw35Ctrwro+ofansbuwuYR9ptrhfNiZRlJQp6j+7Ix/CpnNWv2GkZHjCRX8V6gqH5IXEC/RAE/9lrDrd1DQ9ZvdWvJ49MvGWSd23eS2OWJ64qNfDc8fN/eWNivfzpwzf98Jlv0pxlFRSuFjGrq/C/g2XV1/tHUpPsWjxfM88h27x6Ln+dQwXfhvRSHht5dYu16PcL5cCn12clvxxVDWfEmqa84N7cExL9yBBtjT6KKTcpaR08w0R0/ijxzE9iND8Np9l0yNdjSKNrSD0HcA9z1P8+CooqoQUFZCbuFFFFWAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAV1fhI3jLfX8lzcfYdPh8x0ErBWYnCrjP1P4VyleqNpP9hfBq7Z123F2I5ZPX5nUAf984/Wsa0kkl3HFHG+M9GutC1x7eSeWa2lHmW8jsW3Ie31HT/wDXXO1674nsR4i+GGn6og3XNpAkpPcjAVx+mfwryKijPmjrugkrMKKKK2EFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQBqeG9M/tjxFYWOMrLKN/wDuDlv0Br2H4mkR+BrhAAAZI1AH+8D/AErjvhHp4n1y8v2GRbQhF9mY/wCAP511vxTOPBj+9xH/AFrhrSvXjHsaRXukfw2kTUvAbWcvzJHJLAwP91uf/Zq8Yu7drS8ntn+9FIyH6g4r1j4PuTo+ox9luFP5r/8AWrzzxhCIPGGrIBgfaXb8zn+tXR0qziKXwoxKKKK6yAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKAPZPhFbhPDt5cY+aS5259lUf4mrvxSGfBbn0njP8AOovhOwPhCQDqLt8/98rWh8R4TN4Gv8DJjMb/AJOuf0zXlyf+0fM1+yY/wigKeHLycj/WXRA+gUf4mvPvHq7fHGqD/poD/wCOivVvhrCIvA1kw6yNI5/77I/kBXmfxJgMPji9YjiRY3H/AHwB/St6Mr15Ey+FHJUUUV2kBRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAex/CGTPh++j/u3WfzUf4V1fiuEXHhLVo/+nSRh9QpP9K5D4PoRouoP2NwAPwUf413GuY/4R/Us9Pssv8A6Aa8mtpWfqbR+E534Y3S3Hgq3jB+a3lkjb/vrd/JhXO/F3SG3WOrxrkY+zyn06lf/Zv0qp8I9W8nU7vS3b5bhPNjB/vL1/MH9K9N13SYtb0S606XAEyEKx/hbqp/A4q5P2Ve4lrE+aqKkuIJLW5lt5lKSxOUdT2IOCKjr0zIKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiprW3kvLuG2iGZJnWNR7k4FAHuPw0sDZeDLd2GGuXaY/QnA/QCtfxXOLbwlq0hOP9FkUfUqQP51o2VrHY2FvaRDEcEaxr9AMVynxPvfsvguaIHDXMqRD89x/9Brx0/aVb92bbI8h8L3x07xRpt0DgLOob/dJwf0Jr6Qr5etgxuogv3t4x9c19QjpXRjVqmTTPDvihpgsPFjXCLhLyMS8f3hwf5A/jXFV6t8YoQYNJnxyGlTP1Cn+leU11YeXNTTIluFFFFbCCiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACus+G+ni/8aWrMMpbK05/AYH6kVydelfB6ENqepzY5SFFH4kn/wBlrKu7U2xx3PXK8q+MN9mXTLBT0V5mH1wB/Jq9Vrwr4nXRuPG1xHniCKOMflu/9mrz8JG9T0NJ7GL4Vs/t/irTLfGQ1wpYewOT+gNfR9eG/C2ATeNI3P8Ayxgkcfov/s1e5VeMd5pBDY8v+McwEWkQZ5LSufw2j+teU13vxauzN4ogtwflgtl492JJ/TFcFXXh1akjOW4UUUVuIKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAK9N+DrAXuqr3McZ/Vv8a8yr0L4QzbfEV7D/AH7Ut+TL/jWGIX7pjjueyV8//EEEeOtUB/vp/wCgLX0BXhPxNhMXje5fH+tjjcf98gf0rjwb99+hpPYsfCmQJ4wZT1e1dR+an+le3V87eDdRGl+LdOuXbEfm+W59Aw25/XNfRNGMVp3CGx4P8S93/Cc3uemyPH02CuRr0b4uaY0OsWmpKv7u4i8tj6Mp/qCPyrzmu6g700Zy3CiiitRBRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABXYfDG48jxtbpnHnRSR/+O7v/AGWuPqzp9/Ppl7HeWrbJ487G9CQRn9aiceaLiCdmek6z8SbjT/G0i2/77TIP3EkQ/jIPzMD6g8fhWN8S7q01W90zV7GUSQXNsUBHUFWJIPoRuFcOSSSSck9SaNzbAm47QcgZ4zURoRi010G5NiZwcivpDwzqX9reGtPvScvJCN5/2hw36g18316t4R8V2vh/4ctPcOGljuJI4Ic8u2A2Pp83JrPFQcoq25UHZnZ+L9CHiDw5c2YAM4HmQH0cdPz5H4187spRirAhgcEHsa9D8O/FO9trt01sfaLaVy3mIoDRZ9B3X261y/i9bM+Jru40+ZJbS5InjZDx8wyR7c54pYeM6bcJBJp6ow6KKK6yAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACjJxjPFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFAH//2Q==");
                paths.add("https://www.baidu.com/img/bd_logo1.png?where=super");
                paths.add("data:image/jpeg;base64,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");
                paths.add("https://www.baidu.com/img/bd_logo1.png?where=super");
                EasyPhotos.startPreviewPaths(this, GlideEngine.getInstance(), paths, false);
                break;
            default:
                break;

        }

        return true;
    }

    /**
     * 需要在启动前创建广告view
     * 广告view不能有父布局
     * 广告view可以包含子布局
     * 为了确保广告view地址不变，设置final会更安全
     */

    private void initAdViews() {

        //模拟启动EasyPhotos前广告已经装载完毕
        initPhotosAd();

        //模拟不确定启动EasyPhotos前广告是否装载完毕
        initAlbumItemsAd();

    }

    /**
     * 模拟启动EasyPhotos前广告已经装载完毕
     */
    private void initPhotosAd() {
        photosAdView = (RelativeLayout) getLayoutInflater().inflate(R.layout.ad_photos, null, false);//不可以有父布局，所以inflate第二个参数必须为null，并且布局文件必须独立
        ((TextView) photosAdView.findViewById(R.id.tv_title)).setText("photosAd广告");
        ((TextView) photosAdView.findViewById(R.id.tv_content)).setText("github上star一下了解EasyPhotos的最新动态,这个布局和数据都是由你定制的");
        photosAdLoaded = true;
    }

    /**
     * 模拟不确定启动EasyPhotos前广告是否装载完毕
     * 模拟5秒后网络回调
     */
    private void initAlbumItemsAd() {
        albumItemsAdView = (RelativeLayout) getLayoutInflater().inflate(R.layout.ad_album_items, null, false);//不可以有父布局，所以inflate第二个参数必须为null，并且布局文件必须独立

        //模拟5秒后网络回调
        rvImage.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((ImageView) albumItemsAdView.findViewById(R.id.iv_image)).setImageResource(R.mipmap.ad);
                ((TextView) albumItemsAdView.findViewById(R.id.tv_title)).setText("albumItemsAd广告");
                photosAdLoaded = true;//正常情况可能不知道是先启动EasyPhotos还是数据先回来，所以这里加个标识，如果是后启动EasyPhotos，那么EasyPhotos会直接加载广告
                EasyPhotos.notifyAlbumItemsAdLoaded();
            }
        }, 5000);
    }

}
