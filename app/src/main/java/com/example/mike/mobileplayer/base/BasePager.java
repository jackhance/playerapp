package com.example.mike.mobileplayer.base;

import android.content.Context;
import android.view.View;

/**
 * Created by 国鑫 on 2017/11/30.
 * 作用：基类、公共类
 * <p>
 *     VideoPager
 * </p>
 *      AudioPager
 * <p>
 *     NetVideoPager
 * </p>
 *      NetAudioPager
 *
 *      继承BasePager
 */

public abstract class BasePager {
    public final Context context;

    public View rootView;

    public boolean isInitData;

    public BasePager(Context context){
        this.context = context;
        rootView = initview();
    }



    /**
     * 强制子类实现特定效果
     * @return
     */
    public abstract View initview();

    /**
     * 当子页面需要初始化数据、联网请求数据、绑定数据时重写该方法
     */
    public void initData(){

    }


}
