package com.example.mike.mobileplayer.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mike.mobileplayer.R;
import com.example.mike.mobileplayer.adapter.SearchAdapter;
import com.example.mike.mobileplayer.domain.SearchBean;
import com.example.mike.mobileplayer.utils.Constants;
import com.example.mike.mobileplayer.utils.JsonParser;
import com.example.mike.mobileplayer.utils.ToastUtils;
import com.google.gson.Gson;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by 国鑫 on 2017/12/18.
 * 作用：搜索界面
 */

public class SearchActivity extends Activity implements View.OnClickListener {

    private EditText tvSearchBox;
    private Button btSearch;
    private ListView listView;
    private ProgressBar progressBar;
    private TextView noFind;
    private ImageView ivSpeech;

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private String url;
    private List<SearchBean.ItemsBean> items;
    private SearchAdapter adapter;


    private void findViews() {
        tvSearchBox = (EditText) findViewById(R.id.tv_searchBox);
        btSearch = (Button) findViewById(R.id.bt_search);
        listView = (ListView) findViewById(R.id.listView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        noFind = (TextView) findViewById(R.id.noFind);
        ivSpeech = (ImageView) findViewById(R.id.iv_voice);


        btSearch.setOnClickListener(this);
        ivSpeech.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v == btSearch) {
            // Handle clicks for btSearch
            searchText();
        }else if (v == ivSpeech){
            // Handle clicks for btSearch
            ToastUtils.show(this,"语音输入");
            showDialog();

        }
    }

    private void searchText() {
        String text = tvSearchBox.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            if (items != null && items.size() > 0){
                items.clear();
            }
            try {
                text = URLEncoder.encode(text, "UTF-8");
                url = Constants.SEARCH_URL + text;
                getDataFromNet();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private void getDataFromNet() {
        progressBar.setVisibility(View.VISIBLE);
        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                progressData(result);


            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void progressData(String result) {

        SearchBean searchBean = parseJson(result);
        items = searchBean.getItems();


        if (items != null && items.size() > 0){
            //设置适配器
            adapter = new SearchAdapter(this,items);
            listView.setAdapter(adapter);
            noFind.setVisibility(View.GONE);

        }else{
            noFind.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }


    }

    /**
     * 解析json数据
     * @param result
     * @return
     */
    private SearchBean parseJson(String result) {

        Gson gson = new Gson();
        return gson.fromJson(result,SearchBean.class);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        findViews();
    }

    private void showDialog() {
        //1.创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(this, new MyInitListener());
        //2.设置accent、 language等参数
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");         //中文
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");        //普通话
        //若要将UI控件用于语义理解，必须添加以下参数设置，设置之后onResult回调返回将是语义理解
        //结果
        // mDialog.setParameter("asr_sch", "1");
        // mDialog.setParameter("nlp_version", "2.0");
        //3.设置回调接口
        mDialog.setListener(new MyRecognizerDialogListener());
        //4.显示dialog，接收语音输入
        mDialog.show();
    }

    class MyRecognizerDialogListener implements RecognizerDialogListener {

        /**
         * @param recognizerResult
         * @param b                是否说话结束
         */
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            String result = recognizerResult.getResultString();
            Log.e("MainActivity", "result ==" + result);
            String text = JsonParser.parseIatResult(result);
            //解析好的
            Log.e("MainActivity", "text ==" + text);

            String sn = null;
            // 读取json结果中的sn字段
            try {
                JSONObject resultJson = new JSONObject(recognizerResult.getResultString());
                sn = resultJson.optString("sn");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mIatResults.put(sn, text);

            StringBuffer resultBuffer = new StringBuffer();//拼成一句
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults.get(key));
            }

            tvSearchBox.setText(resultBuffer.toString());
            tvSearchBox.setSelection(tvSearchBox.length());

        }

        /**
         * 出错了
         *
         * @param speechError
         */
        @Override
        public void onError(SpeechError speechError) {
            Log.e("MainActivity", "onError ==" + speechError.getMessage());

        }
    }


    class MyInitListener implements InitListener {

        @Override
        public void onInit(int i) {
            if (i != ErrorCode.SUCCESS) {
                Toast.makeText(SearchActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
