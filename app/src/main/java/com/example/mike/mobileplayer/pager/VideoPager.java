package com.example.mike.mobileplayer.pager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mike.mobileplayer.R;
import com.example.mike.mobileplayer.activity.SystemVideoPlayer;
import com.example.mike.mobileplayer.adapter.VideoPagerAdapter;
import com.example.mike.mobileplayer.base.BasePager;
import com.example.mike.mobileplayer.domain.MediaItem;
import com.example.mike.mobileplayer.utils.LogUtil;

import java.util.ArrayList;

/**
 * Created by 国鑫 on 2017/11/30.
 * 本地视频页面
 */

public class VideoPager extends BasePager {


    private ListView listView;
    private TextView tv_noMedia;
    private ProgressBar pb_loading;
    private VideoPagerAdapter videoPagerAdapter;


    /**
     * 装媒体数据集合
     */
    private ArrayList<MediaItem> mediaItems;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (mediaItems != null && mediaItems.size() > 0) {
                //有数据，设置适配器，隐藏文本

                videoPagerAdapter = new VideoPagerAdapter(context, mediaItems, true);
                listView.setAdapter(videoPagerAdapter);
                tv_noMedia.setVisibility(View.GONE);

            } else {
                //没有数据，显示文本
                tv_noMedia.setVisibility(View.VISIBLE);

            }
            //progress隐藏
            pb_loading.setVisibility(View.GONE);
        }
    };


    public VideoPager(Context context) {
        super(context);
    }

    @Override
    public View initview() {

        View view = View.inflate(context, R.layout.vedio_pager, null);
        listView = (ListView) view.findViewById(R.id.listView);
        tv_noMedia = (TextView) view.findViewById(R.id.tv_noMedia);
        pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);

        //设置listViewItem的点击事件
        listView.setOnItemClickListener(new MyOnItemClickListener());

        return view;
    }

    class MyOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            MediaItem mediaItem = mediaItems.get(position);


            //调用自己写的播放器
           /* Intent intent = new Intent(context,SystemVideoPlayer.class);
            intent.setDataAndType(Uri.parse(mediaItem.getData()),"video*//*");
            context.startActivity(intent);*/


            //传递列表数据--对象--序列化
            Intent intent = new Intent(context, SystemVideoPlayer.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", position);
            context.startActivity(intent);

        }
    }

    @Override
    public void initData() {
        super.initData();
        LogUtil.e("本地视频页面数据初始化");
        //加载本地的视频数据
        getDataFromLocal();


    }


    //从本地的sd卡中得到数据
    //1.遍历sd卡，获取后缀名
    //2.从内容提供者中获取视频
    //6.0之后的系统要动态获取读取sd卡的权限


    private void getDataFromLocal() {

        new Thread() {
            @Override
            public void run() {
                super.run();

                //回调读取sd卡权限函数


                mediaItems = new ArrayList<>();
                ContentResolver resolver = context.getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;



                String[] obj = {

                        MediaStore.Video.Media.DISPLAY_NAME,    //视频在sd卡的名称
                        MediaStore.Video.Media.DURATION,    //视频的总时长
                        MediaStore.Video.Media.SIZE,    //视频的大小
                        MediaStore.Video.Media.DATA,    //视频的绝对地址
                        MediaStore.Video.Media.ARTIST,  //媒体艺术家


                };

                Cursor cursor = resolver.query(uri, obj, null, null, null);
                if (cursor != null) {

                    while (cursor.moveToNext()) {
                        MediaItem mediaItem = new MediaItem();
                        mediaItems.add(mediaItem);


                        String name = cursor.getString(0);
                        mediaItem.setName(name);

                        long duration = cursor.getLong(1);
                        mediaItem.setDuration(duration);

                        long size = cursor.getLong(2);
                        mediaItem.setSize(size);

                        String data = cursor.getString(3);
                        mediaItem.setData(data);

                        String artist = cursor.getString(4);
                        mediaItem.setArtist(artist);

                    }
                    cursor.close();
                }
                System.out.println(mediaItems + "\n");

                //发消息

                handler.sendEmptyMessage(10);
            }


        }.start();

    }



}

