package com.example.mike.mobileplayer.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mike.mobileplayer.R;
import com.example.mike.mobileplayer.domain.MediaItem;
import com.example.mike.mobileplayer.utils.LogUtil;
import com.example.mike.mobileplayer.utils.Utils;
import com.example.mike.mobileplayer.view.VitamioVideoView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;

/**
 * Created by 国鑫 on 2017/12/3.
 * 作用：自己定义的播放器
 */

public class VitamioVideoPlayer extends Activity implements View.OnClickListener {

    private static final int PROGRESS = 1;
    private static final int HIDE_MEDIACONTROLLER = 2;
    private static final int FULL_SCREEN = 1;
    private static final int DEFAULT_SCREEN = 2;
    private static final int SHOW_SPEED = 3;
    private VitamioVideoView videoview;
    private Uri uri;


    private LinearLayout llTop;
    private Button btnExit;
    private TextView tvName;
    private ImageView ivBattery;
    private TextView tvSystemTime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private LinearLayout llBottom;
    private RelativeLayout media_controller;
    private TextView tvCurrentTime;
    private SeekBar seekBarVideo;
    private TextView tvDuration;
    private Button btnSwitchPlayer;
    private Button btnVideoPre;
    private Button btnVideoStartPause;
    private Button btnVideoNext;
    private Button btnVideoSwitchScreen;
    private LinearLayout ll_buffer;
    private TextView tv_netSpeed;
    private LinearLayout ll_loading;
    private TextView tvLoadingNetSpeed;


    private Utils utils;
    //监听电量变化的广播
    private MyReceiver receiver;
    //传进来的视频列表
    private ArrayList<MediaItem> mediaItems;
    //要播放的列表中的具体位置
    private int position;
    //手势识别器
    private GestureDetector gestureDetector;
    //控制面板
    private boolean isshowMediaController = false;
    //全屏与否
    private boolean isFullScreen = false;
    //屏幕的宽、高
    private int screenWidth = 0;
    private int screenHeight = 0;
    private int videoWidth;
    private int videoHeight;

    //调节声音
    private AudioManager am;
    private int currentVoice;
    private int maxVoice;   //0-15个等级
    //是否静音
    private boolean isMute = false;
    //是否是网络uri
    private boolean isNetUri;
    //是否用系统的卡顿判断
    //    private boolean isUseSystem = false;
    //上一次的播放进度
    private int prePosition;


    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2017-12-05 21:50:55 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        Vitamio.isInitialized(this);    //初始化vitamio库
        setContentView(R.layout.activity_vitamio_video_player);
        media_controller = (RelativeLayout) findViewById(R.id.media_controller);
        videoview = (VitamioVideoView) findViewById(R.id.videoview);

        //顶部布局
        llTop = (LinearLayout) findViewById(R.id.ll_top);
        btnExit = (Button) findViewById(R.id.btn_exit);
        tvName = (TextView) findViewById(R.id.tv_name);
        ivBattery = (ImageView) findViewById(R.id.iv_battery);
        tvSystemTime = (TextView) findViewById(R.id.tv_system_time);

        //音量布局
        btnVoice = (Button) findViewById(R.id.btn_voice);
        seekbarVoice = (SeekBar) findViewById(R.id.seekbar_voice);

        //缓冲加载提示
        ll_buffer = (LinearLayout) findViewById(R.id.ll_buffer);
        tv_netSpeed = (TextView) findViewById(R.id.tv_netSpeed);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
        tvLoadingNetSpeed = (TextView) findViewById(R.id.tv_loading_netSpeed);

        //进度条布局
        llBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        tvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        seekBarVideo = (SeekBar) findViewById(R.id.seekBar_video);
        tvDuration = (TextView) findViewById(R.id.tv_duration);

        //播放按钮布局
        btnSwitchPlayer = (Button) findViewById(R.id.btn_switch_player);
        btnVideoPre = (Button) findViewById(R.id.btn_video_pre);
        btnVideoStartPause = (Button) findViewById(R.id.btn_video_start_pause);
        btnVideoNext = (Button) findViewById(R.id.btn_video_next);
        btnVideoSwitchScreen = (Button) findViewById(R.id.btn_video_switch_screen);


        //设置按钮监听
        btnExit.setOnClickListener(this);
        btnVoice.setOnClickListener(this);
        btnSwitchPlayer.setOnClickListener(this);
        btnVideoPre.setOnClickListener(this);
        btnVideoStartPause.setOnClickListener(this);
        btnVideoNext.setOnClickListener(this);
        btnVideoSwitchScreen.setOnClickListener(this);

        //关联seekbar跟最大音量
        seekbarVoice.setMax(maxVoice);
        //设置当前音量关联当前进度
        seekbarVoice.setProgress(currentVoice);

        //开始更新网速
        handler.sendEmptyMessage(SHOW_SPEED);
    }

    /**
     * Handle button click events<br />
     * <br />
     */
    @Override
    public void onClick(View v) {
        if (v == btnExit) {
            // Handle clicks for btnExit
            finish();
        } else if (v == btnVoice) {
            // Handle clicks for btnVoice
            isMute = !isMute;
            upDataVoice(currentVoice, isMute);

        } else if (v == btnSwitchPlayer) {
            // Handle clicks for btnSwitchPlayer
            showSystemPlayerDialog();
        } else if (v == btnVideoPre) {
            // Handle clicks for btnVideoPre
            //播放上一个视频按钮设置
            playPreVideo();
        } else if (v == btnVideoStartPause) {
            // Handle clicks for btnVideoStartPause
            startAndPause();
        } else if (v == btnVideoNext) {
            // Handle clicks for btnVideoNext
            //播放下一个视频按钮设置
            playNextVideo();
        } else if (v == btnVideoSwitchScreen) {
            // Handle clicks for btnVideoSwitchScreen
            setFullScreenAndDefault();
        }
        handler.removeMessages(HIDE_MEDIACONTROLLER);
        handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
    }

    private void showSystemPlayerDialog() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("视频花屏？切换原始播放器修复试试吧！点击确定切换")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startSystemPlayer();
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);


    }

    private void startSystemPlayer() {
        if (videoview != null) {
            videoview.stopPlayback();

        }
        Intent intent = new Intent(this, SystemVideoPlayer.class);
        if (mediaItems != null && mediaItems.size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", position);

        } else if (uri != null) {
            intent.setData(uri);
        }
        startActivity(intent);
        finish();   //关闭播放器
    }

    private void startAndPause() {
        if (videoview.isPlaying()) {

            //视频在播放-设置暂停
            videoview.pause();
            //按钮状态设置播放
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_start_selector);
        } else {
            //视频播放
            videoview.start();
            //按钮状态设置暂停
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        }
    }

    /**
     * 播放上一个视频
     */

    private void playPreVideo() {
        if (mediaItems != null && mediaItems.size() > 0) {
            position--;
            if (position >= 0) {
                ll_loading.setVisibility(View.VISIBLE);
                MediaItem mediaItem = mediaItems.get(position);
                tvName.setText(mediaItem.getName());
                isNetUri = utils.isNetUri(mediaItem.getData());
                videoview.setVideoPath(mediaItem.getData());

                //设置按钮状态
                setButtonState();

            } else if (uri != null) {
                //上一个和下一个按钮设置不可点击
                setButtonState();

            }
        }
    }

    /**
     * 播放下一个视频
     */

    private void playNextVideo() {
        if (mediaItems != null && mediaItems.size() > 0) {
            position++;
            if (position < mediaItems.size()) {
                ll_loading.setVisibility(View.VISIBLE);
                MediaItem mediaItem = mediaItems.get(position);
                tvName.setText(mediaItem.getName());
                isNetUri = utils.isNetUri(mediaItem.getData());
                videoview.setVideoPath(mediaItem.getData());

                //设置按钮状态
                setButtonState();

            } else if (uri != null) {
                //上一个和下一个按钮设置不可点击
                setButtonState();

            }
        }
    }

    private void setButtonState() {
        if (mediaItems != null && mediaItems.size() > 0) {
            if (mediaItems.size() == 1) {
                //当只有一个视频的时候，两个按钮设置为不可点击
                btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                btnVideoPre.setEnabled(false);
                btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                btnVideoNext.setEnabled(false);
            } else if (mediaItems.size() == 2) {
                //有两个视频的时候
                if (position == 0) {
                    //当视频处于第一个，上一个按钮不能点击
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);

                    btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                    btnVideoNext.setEnabled(true);
                } else if (position == mediaItems.size() - 1) {
                    //当视频处于最后一个，下一个按钮不能点击,上一个可以点击
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);

                    btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
                    btnVideoPre.setEnabled(true);
                }
            } else {
                if (position == 0) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);
                } else if (position == mediaItems.size() - 1) {
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);
                } else {
                    //其他情况都可以点击
                    btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
                    btnVideoPre.setEnabled(true);

                    btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                    btnVideoNext.setEnabled(true);
                }
            }
        } else if (uri != null) {
            //两个按钮设置为不可点击
            btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            btnVideoPre.setEnabled(false);
            btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            btnVideoNext.setEnabled(false);

        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_SPEED:    //显示网速
                    String netSpeed = utils.getNetSpeed(VitamioVideoPlayer.this);
                    tvLoadingNetSpeed.setText("玩命加载中……" + netSpeed);
                    tv_netSpeed.setText(netSpeed);
                    //每两秒更新一次
                    handler.removeMessages(SHOW_SPEED);
                    handler.sendEmptyMessageDelayed(SHOW_SPEED, 2000);
                    break;
                case HIDE_MEDIACONTROLLER:
                    //隐藏控制面板
                    hideMediaController();
                    break;
                case PROGRESS:

                    //1.得到当前的视频播放进程
                    int currentPosition = (int) videoview.getCurrentPosition();

                    //2.SeekBar.setProgress(当前进度);
                    seekBarVideo.setProgress(currentPosition);


                    //更新文本播放进度
                    tvCurrentTime.setText(utils.stringForTime(currentPosition));

                    //设置系统时间
                    tvSystemTime.setText(getSystemTime());


                    //3.每秒更新一次
                    handler.removeMessages(PROGRESS);
                    handler.sendEmptyMessageDelayed(PROGRESS, 1000);

                    //网络资源缓冲进度更新
                    if (isNetUri) {
                        //网络资源
                        int buffer = videoview.getBufferPercentage();   //数值是0-100
                        int totalBuffer = buffer * seekBarVideo.getMax();
                        int secondaryProgress = totalBuffer / 100;
                        seekBarVideo.setSecondaryProgress(secondaryProgress);

                    } else {
                        //本地资源
                        seekBarVideo.setSecondaryProgress(0);

                    }

                    //自定义监听卡顿
                   /* if (!isUseSystem && videoview.isPlaying()){
                        int buffer = currentPosition - prePosition;
                        if (buffer < 500){
                            ll_buffer.setVisibility(View.VISIBLE);
                        }else{
                            ll_buffer.setVisibility(View.GONE);
                        }

                    }
                    prePosition = currentPosition;*/
                    break;
            }
        }
    };


    /**
     * 得到系统时间
     *
     * @return
     */
    private String getSystemTime() {

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();

        findViews();

        setListener();

        getDate();

        setDate();

        setButtonState();

        Window _window;
        /**
         * 全屏下隐藏状态栏
         */
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(attrs);
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


            /**

             * 隐藏pad底部虚拟键

             */
            _window = getWindow();

            WindowManager.LayoutParams params = _window.getAttributes();
            params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
            _window.setAttributes(params);


        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            //竖屏操作：
            /*WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attrs);
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            _window = getWindow();

            WindowManager.LayoutParams params = _window.getAttributes();
            params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
            _window.setAttributes(params);*/

        }

    }

    private void setDate() {
        if (mediaItems != null && mediaItems.size() > 0) {
            MediaItem mediaItem = mediaItems.get(position);
            tvName.setText(mediaItem.getName());    //设置视频名称
            isNetUri = utils.isNetUri(mediaItem.getData());
            videoview.setVideoPath(mediaItem.getData());

        } else if (uri != null) {       //接收从别的地方过来的视频
            tvName.setText(uri.toString());     //设置视频名称
            isNetUri = utils.isNetUri(uri.toString());
            videoview.setVideoURI(uri);
        } else {
            Toast.makeText(this, "没有收到本地数据", Toast.LENGTH_SHORT).show();

        }
    }

    private void getDate() {

        //得到播放地址
        uri = getIntent().getData();

        //接收列表数据
        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);


    }


    private void initData() {

        utils = new Utils();
        //注册电量广播
        receiver = new MyReceiver();

        IntentFilter intentFilter = new IntentFilter();
        //电量变化时发出广播
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, intentFilter);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            //实例化手势识别器

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                startAndPause();
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {

                setFullScreenAndDefault();
                return super.onDoubleTap(e);

            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isshowMediaController) {
                    //隐藏
                    hideMediaController();
                    //移除隐藏消息
                    handler.removeMessages(HIDE_MEDIACONTROLLER);

                } else {
                    //显示
                    showMediaController();
                    //发消息隐藏
                    handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
                }
                return super.onSingleTapConfirmed(e);
            }
        });

        //得到屏幕的宽、高
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        //得到音量
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVoice = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);


    }

    private void setFullScreenAndDefault() {
        if (isFullScreen) {
            //默认
            setVideoType(DEFAULT_SCREEN);
        } else {
            //全屏
            setVideoType(FULL_SCREEN);
        }
    }

    private void setVideoType(int defaultScreen) {
        switch (defaultScreen) {
            case FULL_SCREEN:   //全屏
                //视频画面的大小
                videoview.setVideoSize(screenWidth, screenHeight);
                //按钮的状态
                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_video_switch_screen_default_selector);
                isFullScreen = true;
                break;
            case DEFAULT_SCREEN:    //默认效果
                //视频真实的宽和高
                int mVideoWidth = videoWidth;
                int mVideoHeight = videoHeight;

                //屏幕的宽和高
                int width = screenWidth;
                int height = screenHeight;

                if (mVideoWidth * height < width * mVideoHeight) {

                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {

                    height = width * mVideoHeight / mVideoWidth;
                }
                videoview.setVideoSize(width, height);

                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_video_switch_screen_full_selector);
                isFullScreen = false;
                break;
        }
    }

    /**
     * 监听电量变化的广播
     */
    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            setBattery(level);

        }
    }

    private void setBattery(int level) {

        //设置电量显示状态
        if (level <= 0) {
            ivBattery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            ivBattery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 25) {
            ivBattery.setImageResource(R.drawable.ic_battery_25);
        } else if (level <= 50) {
            ivBattery.setImageResource(R.drawable.ic_battery_50);
        } else if (level <= 75) {
            ivBattery.setImageResource(R.drawable.ic_battery_75);
        } else if (level <= 100) {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        } else {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }

    }

    private void setListener() {
        //准备阶段监听
        videoview.setOnPreparedListener(new MyOnPreparedListener());

        //播放出错监听

        videoview.setOnErrorListener(new MyOnErrorListener());

        //播放完成监听

        videoview.setOnCompletionListener(new MyOnCompletionListener());

        //seekBar状态变化的监听
        seekBarVideo.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());

        seekbarVoice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());

        //网络卡顿监听
        //        if(isUseSystem){
        //系统的监听
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            videoview.setOnInfoListener(new NetOnInfoListener());
            //            }

        }

    }

    class NetOnInfoListener implements MediaPlayer.OnInfoListener {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:    //视频卡顿
                    ll_buffer.setVisibility(View.VISIBLE);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:     //卡顿结束
                    ll_buffer.setVisibility(View.GONE);
                    break;
            }
            return false;
        }
    }

    class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if (progress > 0) {
                    isMute = false;
                } else {
                    isMute = true;
                }
                upDataVoice(progress, isMute);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
        }
    }

    //设置音量的大小
    private void upDataVoice(int progress, boolean isMute) {
        if (isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            seekbarVoice.setProgress(0);
        } else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            seekbarVoice.setProgress(progress);
            currentVoice = progress;
        }


    }

    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        /**
         * 手指滑动时引起seekbar的进度变化的回调方法
         *
         * @param seekBar
         * @param progress
         * @param fromUser 行为由用户引起时为turn，非用户引起为false
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoview.seekTo(progress);
            }

        }

        /**
         * 当触碰的时候回调这个
         *
         * @param seekBar
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDE_MEDIACONTROLLER);

        }

        /**
         * 当触碰离开的时候回调这个
         *
         * @param seekBar
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
        }
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {

        //当底层解码准备完成
        @Override
        public void onPrepared(MediaPlayer mp) {

            videoWidth = mp.getVideoWidth();
            videoHeight = mp.getVideoHeight();

            videoview.start();     //开始播放
            //1.得到视频的总时长，赋给进度条
            int duration = (int) videoview.getDuration();
            seekBarVideo.setMax(duration);
            tvDuration.setText(utils.stringForTime(duration));

            hideMediaController();  //默认隐藏控制面板

            //2.发消息
            handler.sendEmptyMessage(PROGRESS);

            videoview.setVideoSize(mp.getVideoWidth(), mp.getVideoHeight());

            //默认播放
            setVideoType(DEFAULT_SCREEN);
            //默认没有加载画面
            ll_loading.setVisibility(View.GONE);


        }
    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        //当播放内容出错时

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Toast.makeText(VitamioVideoPlayer.this, "播放出错了！", Toast.LENGTH_SHORT).show();
            showErrorDialog();
            return true;
        }
    }

    private void showErrorDialog() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("很抱歉，无法播放此视频！")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GRAY);


    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        //当播放内容完成时
        @Override
        public void onCompletion(MediaPlayer mp) {

            //播放完成后自动播放下一个
            playNextVideo();

            if (mediaItems != null && position == mediaItems.size()) {
                Toast.makeText(VitamioVideoPlayer.this, "视频列表已完全播放完", Toast.LENGTH_LONG).show();
                finish();
            } else if (uri != null) {
                finish();
            }

        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LogUtil.e("onRestart--");
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.e("onStart--");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.e("onResume--");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.e("onPause--");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.e("onStop--");
    }

    @Override
    protected void onDestroy() {

        //移除所有消息
        handler.removeCallbacksAndMessages(null);


        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
        LogUtil.e("onDestroy--");
    }

    private float startY;
    private float startX;
    //屏幕的高
    private float touchRang;
    //按一下的时候的当前音量
    private int mVol;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //将事件传递给手势识别器
        gestureDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:   //按下
                //按下时记录值
                startY = event.getY();
                startX = event.getX();
                mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                touchRang = Math.min(screenHeight, screenWidth); //取得screenHeight
                handler.removeMessages(HIDE_MEDIACONTROLLER);
                break;
            case MotionEvent.ACTION_MOVE:   //移动
                showMediaController();
                //移动的记录相关值
                float endY = event.getY();
                float endX = event.getX();
                float distanceY = startY - endY;

                if (endX < screenWidth / 2) {

                    //左边屏幕-调节亮度
                    final double FLING_MIN_DISTANCE = 0.5;
                    final double FLING_MIN_VELOCITY = 0.5;
                    if (distanceY > FLING_MIN_DISTANCE
                            && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(10);
                    }
                    if (distanceY < FLING_MIN_DISTANCE
                            && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(-10);

                    }
                } else {

                    //改变的音量 = （滑动屏幕的距离 ： 总距离）* 音量最大值
                    float delta = (distanceY / touchRang) * maxVoice;
                    //最终音量 = 原来的音量 + 改变的音量
                    int voice = (int) Math.min(Math.max(mVol + delta, 0), maxVoice);
                    if (delta != 0) {
                        isMute = false;
                        upDataVoice(voice, isMute);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:     //离开
                handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
                break;

        }
        return super.onTouchEvent(event);
    }

    /**
     * 设置屏幕亮度 lp = 0 全暗 ，lp= -1,根据系统设置， lp = 1; 最亮
     */

    public void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();

        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;

        } else if (lp.screenBrightness < 0.2) {
            lp.screenBrightness = (float) 0.2;

        }
        getWindow().setAttributes(lp);
    }

    /**
     * 显示控制面板
     */
    private void showMediaController() {
        media_controller.setVisibility(View.VISIBLE);
        isshowMediaController = true;
    }

    /**
     * 隐藏控制面板
     */
    private void hideMediaController() {
        media_controller.setVisibility(View.GONE);
        isshowMediaController = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVoice--;
            upDataVoice(currentVoice, false);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVoice++;
            upDataVoice(currentVoice, false);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}