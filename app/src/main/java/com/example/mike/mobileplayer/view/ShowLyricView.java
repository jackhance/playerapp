package com.example.mike.mobileplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.example.mike.mobileplayer.domain.Lyric;
import com.example.mike.mobileplayer.utils.DensityUtils;
import com.example.mike.mobileplayer.utils.LogUtil;

import java.util.ArrayList;

/**
 * Created by 国鑫 on 2017/12/17.
 * 作用：自定义歌词控件
 */

public class ShowLyricView extends TextView {

    /**
     * 歌词列表
     */
    private ArrayList<Lyric> lyrics;
    private int width;
    private int height;
    //画笔
    private Paint paint;
    private Paint grayPaint;
    //歌词中的索引
    private int index;
    //每行的高
    private float textHeight;
    //当前播放进度
    private float currentPosition;
    //当前歌词显示时间
    private float sleepTime;
    //当前歌词高亮时间戳
    private float timePoint;

    //设置歌词列表
    public void setLyrics(ArrayList<Lyric> lyrics) {
        this.lyrics = lyrics;
    }

    public ShowLyricView(Context context) {
        this(context, null);
    }

    public ShowLyricView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShowLyricView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    private void initView(Context context) {

        textHeight = DensityUtils.dip2px(context, 50);
        LogUtil.e("textHeight==" + textHeight);
        //绘制画笔
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(DensityUtils.dip2px(context, 17));
        paint.setAntiAlias(true);
        //设置居中对齐
        paint.setTextAlign(Paint.Align.CENTER);


        //绘制画笔
        grayPaint = new Paint();
        grayPaint.setColor(Color.BLUE);
        grayPaint.setTextSize(DensityUtils.dip2px(context, 17));
        grayPaint.setAntiAlias(true);
        //设置居中对齐
        grayPaint.setTextAlign(Paint.Align.CENTER);


      /*  lyrics = new ArrayList<>();
        Lyric lyric = new Lyric();
        for (int i = 0; i < 1000; i++) {
            lyric.setTimePoint(1000 * i);
            lyric.setSleepTime(1500 + i);
            lyric.setContent("偶尔抬起头来还好有个月亮可赏" + i);
            //把歌词添加到集合中
            lyrics.add(lyric);
            lyric = new Lyric();
        }*/
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (lyrics != null && lyrics.size() > 0) {

            //绘制歌词
            //往上推移
/*
            float plush = 0;
            if(sleepTime ==0){
                plush = 0;
            }else{
                //平移
                //这一句所花的时间 ：休眠时间 = 移动的距离 ： 总距离（行高）
                //移动的距离 =  (这一句所花的时间 ：休眠时间)* 总距离（行高）
                //                float delta = ((currentPositon-timePoint)/sleepTime )*textHeight;

                //屏幕的的坐标 = 行高 + 移动的距离
                plush = textHeight + ((currentPosition-timePoint)/sleepTime )*textHeight;
            }
            canvas.translate(0,-plush);*/


            //绘制当前句
            String currentLyric = lyrics.get(index).getContent();
            canvas.drawText(currentLyric, width / 2, height / 2, paint);
            //绘制前面的歌词
            float tempY = height / 2;   //Y轴的中间坐标
            for (int i = index - 1; i >= 0; i--) {
                //每一句歌词
                String preLyric = lyrics.get(i).getContent();
                tempY = tempY - textHeight;
                if (tempY < 0) {
                    break;
                }
                canvas.drawText(preLyric, width / 2, tempY, grayPaint);
            }
            //绘制后面的歌词
            tempY = height / 2;   //Y轴的中间坐标
            for (int i = index + 1; i < lyrics.size(); i++) {
                //每一句歌词
                String nextLyric = lyrics.get(i).getContent();
                tempY = tempY + textHeight;
                if (tempY > height) {
                    break;
                }
                canvas.drawText(nextLyric, width / 2, tempY, grayPaint);


            }


        } else {
            //没有歌词
            canvas.drawText("没有找到歌词", width / 2, height / 2, paint);
        }
    }

    public void setShowNextLyric(int currentPosition) {
        this.currentPosition = currentPosition;
        if (lyrics == null || lyrics.size() == 0)
            return;

        for (int i = 1; i < lyrics.size(); i++) {
            if (currentPosition < lyrics.get(i).getTimePoint()) {
                int tempIndex = i - 1;
                if (currentPosition >= lyrics.get(tempIndex).getTimePoint()) {
                    //当前歌词
                    index = tempIndex;
                    sleepTime = lyrics.get(index).getSleepTime();
                    timePoint = lyrics.get(index).getTimePoint();

                }
            }

        }
        //重新绘制
        invalidate();   //在主线程中执行
    }
}
