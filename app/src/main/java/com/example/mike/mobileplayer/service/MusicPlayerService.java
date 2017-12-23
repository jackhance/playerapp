package com.example.mike.mobileplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.example.mike.mobileplayer.IMusicPlayerService;
import com.example.mike.mobileplayer.R;
import com.example.mike.mobileplayer.activity.AudioPlayerActivity;
import com.example.mike.mobileplayer.domain.MediaItem;
import com.example.mike.mobileplayer.utils.CacheUtils;

import java.io.IOException;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by 国鑫 on 2017/12/15.
 * 作用：
 */

public class MusicPlayerService extends Service {

    public static final String OPENAUDIO = "com.guoxin.mobilePlayer_OPENAUDIO";
    private ArrayList<MediaItem> mediaItems;
    private int position;
    private MediaItem mediaItem;
    private MediaPlayer mediaPlayer;
    private NotificationManager manager;
    private AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
    private int currentVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
    private int maxVoice = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);   //0-15个等级

    //顺序播放
    public static final int REPREAT_NORMAL = 1;
    //单曲播放
    public static final int REPREAT_SINGLE = 2;
    //随机播放
    public static final int REPREAT_RANDOM = 3;

    //播放模式
    private int playMode = REPREAT_NORMAL;


    @Override
    public void onCreate() {
        super.onCreate();

        playMode = CacheUtils.getPlayMode(this, "playMode");

        //加载音乐列表
        getDataFromLocal();
    }

    private void getDataFromLocal() {

        new Thread() {
            @Override
            public void run() {
                super.run();


                mediaItems = new ArrayList<>();
                ContentResolver resolver = getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] obj = {

                        MediaStore.Audio.Media.DISPLAY_NAME,    //视频在sd卡的名称
                        MediaStore.Audio.Media.DURATION,    //视频的总时长
                        MediaStore.Audio.Media.SIZE,    //视频的大小
                        MediaStore.Audio.Media.DATA,    //视频的绝对地址
                        MediaStore.Audio.Media.ARTIST,  //媒体艺术家

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


            }
        }.start();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    private IMusicPlayerService.Stub stub = new IMusicPlayerService.Stub() {

        MusicPlayerService service = MusicPlayerService.this;


        @Override
        public void openAudio(int position) throws RemoteException {
            service.openAudio(position);

        }

        @Override
        public void start() throws RemoteException {
            service.start();

        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public void stop() throws RemoteException {
            service.stop();

        }

        @Override
        public void playpre() throws RemoteException {
            service.playpre();

        }

        @Override
        public void playnext() throws RemoteException {
            service.playnext();

        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return service.getCurrentPosition();
        }

        @Override
        public int getDuration() throws RemoteException {
            return service.getDuration();
        }

        @Override
        public String getArtist() throws RemoteException {
            return service.getArtist();
        }

        @Override
        public String getName() throws RemoteException {
            return service.getName();
        }

        @Override
        public String getMusicPath() throws RemoteException {
            return service.getMusicPath();
        }

        @Override
        public void setPlayMode(int playMode) throws RemoteException {
            service.setPlayMode(playMode);

        }

        @Override
        public int getPlayMode() throws RemoteException {
            return service.getPlayMode();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return service.isPlaying();
        }

        @Override
        public void seekTo(int position) throws RemoteException {
            mediaPlayer.seekTo(position);
        }

        @Override
        public int getCurrentVoice() throws RemoteException {
            return service.getCurrentVoice();
        }

        @Override
        public int getMaxVoice() throws RemoteException {
            return service.getMaxVoice();
        }
    };


    /**
     * 根据位置打开音频文件
     *
     * @param position
     */
    private void openAudio(int position) {

        this.position = position;
        if (mediaItems != null && mediaItems.size() > 0) {
            mediaItem = mediaItems.get(position);

            if (mediaPlayer != null) {

                mediaPlayer.reset();
            }
            try {

                mediaPlayer = new MediaPlayer();
                //设置监听（同视频播放器）
                mediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
                mediaPlayer.setOnErrorListener(new MyOnErrorListener());
                mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
                mediaPlayer.setDataSource(mediaItem.getData());
                mediaPlayer.prepareAsync();


                if (playMode == MusicPlayerService.REPREAT_SINGLE) {
                    //单曲循环播放-不会触发播放完成的回调
                    mediaPlayer.setLooping(true);
                } else {
                    //不循环播放
                    mediaPlayer.setLooping(false);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {

        }

    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {

            //让activity获取信息
//                        notifyChange(OPENAUDIO);

            //用EventBus传递订阅信息
            EventBus.getDefault().post(mediaItem);

            start();
        }
    }

    /**
     * 根据动作发广播
     *
     * @param
     */
    private void notifyChange(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);


    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            playnext();
            return true;
        }
    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            playnext();

        }
    }


    /**
     * 播放音乐
     */
    private void start() {
        mediaPlayer.start();

        //播放歌曲时，状态栏显示正在播放，点击时，进入音乐播放界面
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.putExtra("notification", true);   //表示来自状态栏
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notification_music_playing)
                .setContentTitle("优益影音")
                .setContentText("正在播放" + getName())
                .setContentIntent(pendingIntent)
                .build();
        manager.notify(1, notification);

    }

    /**
     * 暂停音乐
     */
    private void pause() {
        mediaPlayer.pause();

        //歌曲暂停时，状态栏显示暂停播放，点击时，进入音乐播放界面
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.putExtra("notification", true);   //表示来自状态栏
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notification_music_playing)
                .setContentTitle("优益影音")
                .setContentText("暂停播放" + getName())
                .setContentIntent(pendingIntent)
                .build();
        manager.notify(1, notification);

        //        manager.cancel(1);
    }

    /**
     * 停止
     */
    private void stop() {
        mediaPlayer.stop();
        manager.cancel(1);
    }

    /**
     * 播放上一首
     */
    private void playpre() {
        //1.根据当前的播放模式设置上一个位置
        setPrePosition();

        //2.根据当前的播放模式和下标位置去播放音频
        openPreAudio();

    }

    private void openPreAudio() {
        int playMode = getPlayMode();
        if (playMode == MusicPlayerService.REPREAT_NORMAL) {
            openAudio(position);
        } else if (playMode == MusicPlayerService.REPREAT_SINGLE) {
            openAudio(position);

        } else if (playMode == MusicPlayerService.REPREAT_RANDOM) {
            openAudio(position);

        } else {
            openAudio(position);
        }
    }

    private void setPrePosition() {
        int playMode = getPlayMode();
        if (playMode == MusicPlayerService.REPREAT_NORMAL) {
            position--;
            if (position < 0) {
                position = mediaItems.size() - 1;
            }
        } else if (playMode == MusicPlayerService.REPREAT_SINGLE) {
            position--;
            if (position < 0) {
                position = mediaItems.size() - 1;
            }

        } else if (playMode == MusicPlayerService.REPREAT_RANDOM) {
            position--;
            position = (int) (Math.random() * mediaItems.size());


        } else {
            position--;
            if (position < 0) {
                position = mediaItems.size() - 1;
            }
        }
    }

    /**
     * 播放下一首
     */
    private void playnext() {
        //1.根据当前的播放模式设置下一个位置
        setNextPosition();

        //2.根据当前的播放模式和下标位置去播放音频
        openNextAudio();


    }

    private void openNextAudio() {
        int playMode = getPlayMode();
        if (playMode == MusicPlayerService.REPREAT_NORMAL) {
            openAudio(position);
        } else if (playMode == MusicPlayerService.REPREAT_SINGLE) {
            openAudio(position);

        } else if (playMode == MusicPlayerService.REPREAT_RANDOM) {
            openAudio(position);

        } else {
            openAudio(position);
        }
    }

    private void setNextPosition() {
        int playMode = getPlayMode();
        if (playMode == MusicPlayerService.REPREAT_NORMAL) {
            position++;
            if (position >= mediaItems.size()) {
                position = 0;
            }
        } else if (playMode == MusicPlayerService.REPREAT_SINGLE) {
            position++;
            if (position >= mediaItems.size()) {
                position = 0;
            }

        } else if (playMode == MusicPlayerService.REPREAT_RANDOM) {
            position++;
            position = (int) (Math.random() * mediaItems.size());


        } else {
            position++;
            if (position >= mediaItems.size()) {
                position = 0;
            }
        }

    }

    /**
     * 得到当前时间
     *
     * @return
     */
    private int getCurrentPosition() {

        return mediaPlayer.getCurrentPosition();
    }

    /**
     * 得到总时长
     *
     * @return
     */
    private int getDuration() {

        return mediaPlayer.getDuration();
    }

    /**
     * 得到歌手名
     *
     * @return
     */
    private String getArtist() {
        return mediaItem.getArtist();
    }

    /**
     * 得到歌曲名
     *
     * @return
     */
    private String getName() {
        return mediaItem.getName();
    }

    /**
     * 得到音乐路径
     *
     * @return
     */
    private String getMusicPath() {
        return mediaItem.getData();
    }

    /**
     * 设置播放模式
     */
    private void setPlayMode(int playMode) {
        CacheUtils.putPlayMode(this, "playMode", playMode);
        this.playMode = playMode;


        if (playMode == MusicPlayerService.REPREAT_SINGLE) {
            //单曲循环播放-不会触发播放完成的回调
            mediaPlayer.setLooping(true);
        } else {
            //不循环播放
            mediaPlayer.setLooping(false);
        }

    }

    /**
     * 得到播放模式
     *
     * @return
     */
    private int getPlayMode() {

        return playMode;
    }

    /**
     * 音频播放的状态判断
     *
     * @return
     */
    private boolean isPlaying() {

        return mediaPlayer.isPlaying();
    }
    private int getMaxVoice(){
        return am.getStreamMaxVolume(maxVoice);
    }
    private int getCurrentVoice(){
        return am.getStreamVolume(currentVoice);
    }


}
