package com.example.mike.mobileplayer.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.example.mike.mobileplayer.R;
import com.example.mike.mobileplayer.base.BasePager;
import com.example.mike.mobileplayer.pager.AudioPager;
import com.example.mike.mobileplayer.pager.NetAudioPager;
import com.example.mike.mobileplayer.pager.NetVideoPager;
import com.example.mike.mobileplayer.pager.VideoPager;
import com.example.mike.mobileplayer.utils.ToastUtils;

import java.util.ArrayList;


/**
 * Created by 国鑫 on 2017/11/29.
 * <p>
 * 作用：主页面
 */

public class MainActivity extends FragmentActivity {

    private RadioGroup rb_bottom_tag;
    private ArrayList<BasePager> basePagers;

    private int position;       //选中的位置
    private boolean isExit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rb_bottom_tag = (RadioGroup) findViewById(R.id.rg_bottom_tag);


        //实例化基类页面，添加四个页面
        basePagers = new ArrayList<>();
        basePagers.add(new VideoPager(this));    //position=0
        basePagers.add(new AudioPager(this));        //position=1
        basePagers.add(new NetVideoPager(this));     //position=2
        basePagers.add(new NetAudioPager(this));     //position=3

        //设置RadioGroup的监听
        rb_bottom_tag.setOnCheckedChangeListener(new MyOnCheckedChangeListener());
        rb_bottom_tag.check(R.id.rb_video);//默认选中首页





    }


    class MyOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                //根据position得到不同的实例
                default:
                    isGrantExternalRW(MainActivity.this);
                    position = 0;
                    break;
                case R.id.rb_audio:
                    position = 1;
                    break;
                case R.id.rb_net_video:
                    position = 2;
                    break;
                case R.id.rb_netaudio:
                    position = 3;
                    break;

            }
            setFragment();
        }
    }

    private void setFragment() {
        //1.得到FragmentManger
        FragmentManager manager = getSupportFragmentManager();
        //2.开启事务
        FragmentTransaction ft = manager.beginTransaction();
        //3.替换
        ft.replace(R.id.fl_main_content, new ReplaceFragment(getBasePager()));
        //4.提交事务
        ft.commit();
    }

    public static class ReplaceFragment extends Fragment {

        private BasePager currPager;

        public ReplaceFragment(BasePager pager) {
            this.currPager = pager;
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return currPager.rootView;
        }
    }


    /**
     * 根据位置得到对应的页面
     *
     * @return
     */
    private BasePager getBasePager() {
        BasePager basePager = basePagers.get(position);
        if (basePager != null && !basePager.isInitData) {
            basePager.initData();   //联网请求、绑定数据

            basePager.isInitData = true;    //屏蔽各页面再次初始化


        }
        return basePager;
    }

    /**
     * 获取读取sd卡的权限
     *
     * @param activity
     * @return
     */
    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
            return false;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (position != 0) {
                position = 0;
                rb_bottom_tag.check(R.id.rb_video); //首页
                return true;
            } else if (!isExit) {
                isExit = true;
                ToastUtils.show(this, "再按一次退出");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExit = false;
                    }
                }, 2000);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}


