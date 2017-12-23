package com.example.mike.mobileplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;

import com.example.mike.mobileplayer.R;

/**
 *作用：过渡页面
 */

public class SplashActivity extends AppCompatActivity {
    private Handler handler = new Handler();

    private static final String TAG = SplashActivity.class.getSimpleName();//名称能动态改变！此时为“SplashActivity”

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //两秒后执行此处
                //执行在主线程中

                startMainActivity();
                Log.e(TAG,"当前线程的名称 == " + Thread.currentThread());
            }
        },2000);
    }





    /**
     * 跳转到主界面，并把当前页面关闭
     */
    private void startMainActivity() {

            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
    }

    /**
     *触摸事件，点击加载界面直接进入主界面
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        startMainActivity();
        return super.onTouchEvent(event);
    }

    /**
     * 防止主界面被重启
     */
    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
