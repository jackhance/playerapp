package com.example.mike.mobileplayer.pager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mike.mobileplayer.R;
import com.example.mike.mobileplayer.activity.SystemVideoPlayer;
import com.example.mike.mobileplayer.adapter.NetVideoPagerAdapter;
import com.example.mike.mobileplayer.base.BasePager;
import com.example.mike.mobileplayer.domain.MediaItem;
import com.example.mike.mobileplayer.utils.CacheUtils;
import com.example.mike.mobileplayer.utils.Constants;
import com.example.mike.mobileplayer.utils.LogUtil;
import com.example.mike.mobileplayer.view.XListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.vov.vitamio.utils.Log;


/**
 * Created by 国鑫 on 2017/11/30.
 * 网络视频页面
 */

public class NetVideoPager extends BasePager {

    @ViewInject(R.id.listView)
    private XListView mListView;

    @ViewInject(R.id.tv_noNet)
    private TextView mtv_noNet;

    @ViewInject(R.id.pb_loading)
    private ProgressBar mProgressBar;

    private ArrayList<MediaItem> mediaItems;
    private NetVideoPagerAdapter netVideoPageradpter;
    //是否加载更多
    private boolean isLoadMore = false;


    public NetVideoPager(Context context) {
        super(context);
    }

    @Override
    public View initview() {

        View view = View.inflate(context, R.layout.netvedio_pager, null);
        //写注解，第一个参数是this，第二个是布局
        x.view().inject(this, view);

        //设置listViewItem的点击事件
        mListView.setOnItemClickListener(new MyOnItemClickListener());
        mListView.setPullLoadEnable(true);
        mListView.setXListViewListener(new MyXListViewListener());
        return view;

    }

    class MyXListViewListener implements XListView.IXListViewListener {

        @Override
        public void onRefresh() {
            getDataFromNet();
        }

        @Override
        public void onLoadMore() {
            getMoreDataFromNet();
        }
    }

    private void getMoreDataFromNet() {
        //联网请求
        //视频内容
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                isLoadMore = true;
                //主线程解析数据
                processData(result);
                Log.e("联网成功" + result);

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e("联网失败" + ex.getMessage());
                isLoadMore = false;
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.e("onCancelled == " + cex.getMessage());
                isLoadMore = false;
            }

            @Override
            public void onFinished() {
                Log.e("Finish");
                isLoadMore = false;
            }
        });
    }

    class MyOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            MediaItem mediaItem = mediaItems.get(position);


            //传递列表数据--对象--序列化
            Intent intent = new Intent(context, SystemVideoPlayer.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", position-1);
            context.startActivity(intent);

        }
    }

    @Override
    public void initData() {
        super.initData();
        LogUtil.e("网络视频页面数据初始化");

        //保存缓存数据
        String saveJson = CacheUtils.getString(context,Constants.NET_URL);
        if (!TextUtils.isEmpty(saveJson)){
            processData(saveJson);
        }
        getDataFromNet();


    }

    private void getDataFromNet() {
        //联网请求
        //视频内容
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {

                //缓存数据
                CacheUtils.putString(context,Constants.NET_URL,result);

                //主线程解析数据
                processData(result);
                Log.e("联网成功" + result);

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e("联网失败" + ex.getMessage());
                showData();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.e("onCancelled == " + cex.getMessage());
            }

            @Override
            public void onFinished() {
                Log.e("Finish");

            }
        });
    }


    private void processData(String json) {

        if (!isLoadMore){
            mediaItems = parse(json);
            showData();

        }else {
            //加载更多
            isLoadMore = false;
            mediaItems.addAll(parse(json));
            //刷新适配器
            netVideoPageradpter.notifyDataSetChanged();
            onLoad();

        }


    }

    private void showData() {
        //设置适配器
        if (mediaItems != null && mediaItems.size() > 0) {

            netVideoPageradpter = new NetVideoPagerAdapter(context, mediaItems);
            mListView.setAdapter(netVideoPageradpter);
            onLoad();
            mtv_noNet.setVisibility(View.GONE);
        } else {

            mtv_noNet.setVisibility(View.VISIBLE);
        }

        mProgressBar.setVisibility(View.GONE);
    }

    private void onLoad() {
        mListView.stopRefresh();
        mListView.stopLoadMore();
        mListView.setRefreshTime("" + getSystemTime());
    }

    /**
     * 得到系统时间
     *
     * @return
     */
    private String getSystemTime() {

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    /**
     * 解析json数据
     * 1.用系统接口解析
     * 2.第三方工具（Gson， fastjson）
     *
     * @param json
     */
    private ArrayList<MediaItem> parse(String json) {
        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.optJSONArray("trailers");
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObjectItem = (JSONObject) jsonArray.get(i);
                    if (jsonObjectItem != null) {

                        MediaItem mediaItem = new MediaItem();
                        String movieName = jsonObjectItem.optString("movieName");
                        mediaItem.setName(movieName);

                        String videoTitle = jsonObjectItem.optString("videoTitle");
                        mediaItem.setDesc(videoTitle);

                        String imgUrl = jsonObjectItem.optString("coverImg");
                        mediaItem.setImageUrl(imgUrl);

                        String hightUrl = jsonObjectItem.optString("hightUrl");
                        mediaItem.setData(hightUrl);

                        //把数据添加到集合中
                        mediaItems.add(mediaItem);

                    }


                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mediaItems;
    }
}
