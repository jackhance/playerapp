package com.example.mike.mobileplayer.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.mike.mobileplayer.IMusicPlayerService;
import com.example.mike.mobileplayer.R;
import com.example.mike.mobileplayer.domain.MediaItem;
import com.example.mike.mobileplayer.service.MusicPlayerService;
import com.example.mike.mobileplayer.utils.LyricUtils;
import com.example.mike.mobileplayer.utils.ToastUtils;
import com.example.mike.mobileplayer.utils.Utils;
import com.example.mike.mobileplayer.view.ShowLyricView;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by 国鑫 on 2017/12/15.
 * 作用：
 */

public class AudioPlayerActivity extends Activity implements View.OnClickListener {

    private static final int PROGRESS = 1;
    private static final int SHOW_LYRICS = 2;
    private int position;
    private IMusicPlayerService service;

    //调节声音
    private AudioManager am;
    private int currentVoice;
    private int maxVoice;   //0-15个等级


    private Button btBack;
    private TextView tvMusicName;
    private TextView tvMusicArtist;
    private SeekBar seekBarVoice;
    private LinearLayout llBottom;
    private TextView tvCurrentTime;
    private SeekBar seekBarAudio;
    private TextView tvDuration;
    private Button btnSwitchPlayMode;
    private Button btnAudioPre;
    private Button btnAudioStartPause;
    private Button btnAudioNext;
    private MyReciver reciver;
    private Utils utils;
    //true时，从状态栏进入，记住原来的播放状态
    //false时，从播放列表进入
    private boolean notification;

    private ShowLyricView tv_showLyricView;

    private ServiceConnection con = new ServiceConnection() {

        /**
         * 链接成功时回调此方法
         * @param name
         * @param iBinder
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            service = IMusicPlayerService.Stub.asInterface(iBinder);
            if (service != null) {
                try {
                    if (!notification) {
                        //从列表进入
                        service.openAudio(position);
                    } else {
                        //从状态栏进入
                        showViewData();


                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 断开连接回调此方法
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                if (service != null) {
                    service.stop();
                    service = null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }


        }
    };
    private boolean isMute = false;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2017-12-16 12:10:37 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {

        setContentView(R.layout.activity_audioplayer);
        btBack = (Button) findViewById(R.id.bt_back);
        tvMusicName = (TextView) findViewById(R.id.tv_musicName);
        tvMusicArtist = (TextView) findViewById(R.id.tv_musicArtist);
        seekBarVoice = (SeekBar) findViewById(R.id.seekbar_voice);

        tv_showLyricView = (ShowLyricView) findViewById(R.id.tv_showLyricView);


        llBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        tvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        tvDuration = (TextView) findViewById(R.id.tv_duration);
        seekBarAudio = (SeekBar) findViewById(R.id.seekBar_audio);


        btnSwitchPlayMode = (Button) findViewById(R.id.btn_switch_playMode);
        btnAudioPre = (Button) findViewById(R.id.btn_audio_pre);
        btnAudioStartPause = (Button) findViewById(R.id.btn_audio_start_pause);
        btnAudioNext = (Button) findViewById(R.id.btn_audio_next);

        btBack.setOnClickListener(this);
        btnSwitchPlayMode.setOnClickListener(this);
        btnAudioPre.setOnClickListener(this);
        btnAudioStartPause.setOnClickListener(this);
        btnAudioNext.setOnClickListener(this);



        //设置进度条seekbar的拖动
        seekBarAudio.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
        //设置音量seekbar的拖动
        seekBarVoice.setMax(maxVoice);
        seekBarVoice.setProgress(currentVoice);
        seekBarVoice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());

    }

   private class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {

                upDataVoice(progress);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {


        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    //设置音量的大小
    private void upDataVoice(int progress) {

            am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            seekBarVoice.setProgress(progress);
            currentVoice = progress;



    }

    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        /**
         * 用户引起的动作调这个
         *
         * @param seekBar
         * @param progress
         * @param fromUser
         */

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                try {
                    service.seekTo(progress);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }


        }

        /**
         * 当触碰的时候回调这个
         *
         * @param seekBar
         */

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }


        /**
         * 当触碰离开的时候回调这个
         *
         * @param seekBar
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }


    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2017-12-16 12:10:37 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == btBack) {
            // Handle clicks for btBack
            finish();
            btBack.setBackgroundResource(R.drawable.btn_audioplayer_back_selector);
        } else if (v == btnSwitchPlayMode) {
            // Handle clicks for btnSwitchPlayMode
            setPlayMode();
        } else if (v == btnAudioPre) {
            // Handle clicks for btnAudioPre
            if (service != null) {
                try {
                    service.playpre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnAudioStartPause) {
            // Handle clicks for btnAudioStartPause
            if (service != null) {
                try {
                    if (service.isPlaying()) {
                        //暂停时按钮设为播放
                        service.pause();
                        btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);

                    } else {
                        //播放时按钮设为暂停
                        service.start();
                        btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnAudioNext) {
            // Handle clicks for btnAudioNext
            if (service != null) {
                try {

                    service.playnext();


                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void setPlayMode() {
        try {
            int playMode = service.getPlayMode();

            if (playMode == MusicPlayerService.REPREAT_NORMAL) {
                playMode = MusicPlayerService.REPREAT_SINGLE;
            } else if (playMode == MusicPlayerService.REPREAT_SINGLE) {
                playMode = MusicPlayerService.REPREAT_RANDOM;
            } else if (playMode == MusicPlayerService.REPREAT_RANDOM) {
                playMode = MusicPlayerService.REPREAT_NORMAL;
            }

            //保存
            service.setPlayMode(playMode);

            //设置图片
            showPlayMode();

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void showPlayMode() {

        try {
            int playMode = service.getPlayMode();
            if (playMode == MusicPlayerService.REPREAT_NORMAL) {
                btnSwitchPlayMode.setBackgroundResource(R.drawable.btn_audio_listmode_selector);
                ToastUtils.show(this, "列表播放");
            } else if (playMode == MusicPlayerService.REPREAT_SINGLE) {
                btnSwitchPlayMode.setBackgroundResource(R.drawable.btn_audio_singlemode_selector);
                ToastUtils.show(this, "单曲循环");
            } else if (playMode == MusicPlayerService.REPREAT_RANDOM) {
                btnSwitchPlayMode.setBackgroundResource(R.drawable.btn_audio_randommode_selector);
                ToastUtils.show(this, "随机播放");
            } else {
                btnSwitchPlayMode.setBackgroundResource(R.drawable.btn_audio_listmode_selector);
                ToastUtils.show(this, "列表播放");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 校验播放状态
     */
    private void checkPlayMode() {

        try {
            int playMode = service.getPlayMode();
            if (playMode == MusicPlayerService.REPREAT_NORMAL) {
                btnSwitchPlayMode.setBackgroundResource(R.drawable.btn_audio_listmode_selector);
            } else if (playMode == MusicPlayerService.REPREAT_SINGLE) {
                btnSwitchPlayMode.setBackgroundResource(R.drawable.btn_audio_singlemode_selector);
            } else if (playMode == MusicPlayerService.REPREAT_RANDOM) {
                btnSwitchPlayMode.setBackgroundResource(R.drawable.btn_audio_randommode_selector);
            } else {
                btnSwitchPlayMode.setBackgroundResource(R.drawable.btn_audio_listmode_selector);
            }

            if (service.isPlaying()) {
                //暂停时按钮设为播放
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);

            } else {
                //播放时按钮设为暂停
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case SHOW_LYRICS:   //显示歌词

                    //1.得到当前的进度
                    try {
                        int currentPosition = service.getCurrentPosition();

                        //2.将进度传入控件ShowLyricView里，并计算显示当前歌词
                        tv_showLyricView.setShowNextLyric(currentPosition);

                        //3.实时发送消息
                        handler.removeMessages(SHOW_LYRICS);
                        handler.sendEmptyMessage(SHOW_LYRICS);

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;

                case PROGRESS:
                    try {

                        //得到当前进度
                        int currentPosition = service.getCurrentPosition();

                        //设置进度
                        seekBarAudio.setProgress(currentPosition);

                        //时间进度更新
                        tvCurrentTime.setText(utils.stringForTime(currentPosition));
                        tvDuration.setText(utils.stringForTime(service.getDuration()));

                        //每秒更新一次
                        handler.removeMessages(PROGRESS);
                        handler.sendEmptyMessageDelayed(PROGRESS, 1000);

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        findViews();
        getData();
        bindAndStartService();


    }

    private void initData() {

        utils = new Utils();

        //得到音量
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVoice = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);


         /*   //注册广播
            reciver = new MyReciver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(MusicPlayerService.OPENAUDIO);
            registerReceiver(reciver, intentFilter);*/


        //EventBus注册
        EventBus.getDefault().register(this);   //this是当前类


    }


    class MyReciver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            showData(null);

        }
    }

    //订阅方法 (不能私有)
//    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false, priority = 0)
    public void showData(MediaItem mediaItem) {

        //发消息开始歌词同步
        showLyric();
        showViewData();
        checkPlayMode();

    }
    public void onEventMainThread(MediaItem mediaItem){

        //发消息开始歌词同步
        showLyric();
        showViewData();
        checkPlayMode();
    }




    private void showLyric() {

        //解析歌词
        LyricUtils lyricUtils = new LyricUtils();
        //传歌词文件
        try {
            String path = service.getMusicPath();
            path = path.substring(0, path.lastIndexOf("."));
            File file = new File(path + ".lrc");
            if (!file.exists()) {
                file = new File(path + ".txt");
            }
            lyricUtils.readLyricFile(file);
            tv_showLyricView.setLyrics(lyricUtils.getLyrics());


        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (lyricUtils.isHaveLyric()) {

            handler.sendEmptyMessage(SHOW_LYRICS);
        }


    }

    private void showViewData() {
        try {
            tvMusicArtist.setText(service.getArtist());
            tvMusicName.setText(service.getName());
            seekBarAudio.setMax(service.getDuration());
            seekBarVoice.setMax(service.getMaxVoice());

            if (service.isPlaying()) {
                //暂停时按钮设为播放
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);

            } else {
                //播放时按钮设为暂停
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
            }

            //发消息
            handler.sendEmptyMessage(PROGRESS);


        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void bindAndStartService() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction("com.guoxin.mobilePlayer_openAudioPlayer");
        bindService(intent, con, Context.BIND_AUTO_CREATE);
        startService(intent);   //防止实例化多个服务

    }

    private void getData() {

        notification = getIntent().getBooleanExtra("notification", false);
        if (!notification) {
            //从列表进入
            position = getIntent().getIntExtra("position", 0);

        }

    }


    @Override
    protected void onDestroy() {

        handler.removeMessages(PROGRESS);

            /*//取消注册广播
            if (reciver != null) {
                unregisterReceiver(reciver);
                reciver = null; //方便垃圾回收机制优先回收
            }*/

        //取消EventBus注册
        EventBus.getDefault().unregister(this);


        //解绑服务
        if (con != null) {
            unbindService(con);
            con = null;
        }

        super.onDestroy();
    }
}
