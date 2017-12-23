package com.example.mike.mobileplayer.pager;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mike.mobileplayer.R;
import com.example.mike.mobileplayer.adapter.NetAudioPagerAdapter;
import com.example.mike.mobileplayer.base.BasePager;
import com.example.mike.mobileplayer.domain.NetAudioBean;
import com.example.mike.mobileplayer.utils.CacheUtils;
import com.example.mike.mobileplayer.utils.Constants;
import com.example.mike.mobileplayer.utils.LogUtil;
import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

/**
 * Created by 国鑫 on 2017/11/30.
 * 网络音乐页面
 */

public class NetAudioPager extends BasePager {

    @ViewInject(R.id.listView)
    private ListView mListView;

    @ViewInject(R.id.tv_noNet)
    private TextView mTextView;

    @ViewInject(R.id.pb_loading)
    private ProgressBar mProgressBar;
    //页面的数据
    private List<NetAudioBean.ListBean> datas;
    private NetAudioPagerAdapter adapter;


    public NetAudioPager(Context context) {
        super(context);
    }

    @Override
    public View initview() {
        LogUtil.e("网络音乐页面初始化");

        View view = View.inflate(context, R.layout.netaudio_pager, null);
        x.view().inject(this, view);

        return view;
    }

    @Override
    public void initData() {
        super.initData();
        LogUtil.e("网络音乐页面数据初始化");
        String saveJson = CacheUtils.getString(context, Constants.ALL_RES_URL);
        if (!TextUtils.isEmpty(saveJson)) {
            //解析数据
            processData(saveJson);
        }

        getDataFromNet();

    }

    /**
     * 解析json数据和显示数据
     *
     * @param json
     */
    private void processData(String json) {
        NetAudioBean netAudioBean = parsedJson(json);
        datas = netAudioBean.getList();
        if (datas != null && datas.size() > 0) {
            //有数据
            mTextView.setVisibility(View.GONE);
            //设置适配器
            adapter = new NetAudioPagerAdapter(context,datas);
            mListView.setAdapter(adapter);

        } else {
            mTextView.setText("没有找到数据……");

            //没有数据
            mTextView.setVisibility(View.VISIBLE);

        }
        mProgressBar.setVisibility(View.GONE);


    }

    /**
     * Gson解析数据
     *
     * @param json
     * @return
     */
    private NetAudioBean parsedJson(String json) {

        Gson gson = new Gson();
        return gson.fromJson(json, NetAudioBean.class);
    }

    private void getDataFromNet() {

        RequestParams params = new RequestParams(Constants.ALL_RES_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.e("请问数据成功==" + result);
                //保存数据
                CacheUtils.putString(context, Constants.ALL_RES_URL, result);
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("请求失败 == " + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });

    }
}
