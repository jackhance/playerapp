package com.example.mike.mobileplayer.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.mike.mobileplayer.R;
import com.example.mike.mobileplayer.activity.SearchActivity;

/**
 * Created by 国鑫 on 2017/12/1.
 *  作用：主界面标题栏
 */

public class TitleBar extends LinearLayout implements View.OnClickListener {

    private View tv_search;
    private View iv_history;
    private Context context;

    /**
     * 在代码实例化该类的时候调用
     * @param context
     */
    public TitleBar(Context context) {
        this(context,null);
    }

    /**
     * 在布局文件使用该类的时候，系统实例化该类
     * @param context
     * @param attrs
     */
    public TitleBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    /**
     * 当使用样式的时候使用该类
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public TitleBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    /**
     * 当布局文件加载完成时，回调该方法
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //得到控件的实例
        tv_search = getChildAt(1);
        iv_history = getChildAt(2);

        //设置点击事件
        tv_search.setOnClickListener(this);
        iv_history.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_search:
                Intent intent = new Intent(context,SearchActivity.class);
                context.startActivity(intent);
                break;
            case R.id.iv_history:
                Toast.makeText(context,"播放记录", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
