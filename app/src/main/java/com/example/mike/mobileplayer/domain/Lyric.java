package com.example.mike.mobileplayer.domain;

/**
 * Created by 国鑫 on 2017/12/17.
 * 作用：歌词类
 *
 */

public class Lyric {
    /**
     * 歌词内容
     */
    private String content;

    /**
     * 时间轴
     */
    private long timePoint;

    /**
     * 停顿歌词时间
     */
    private long sleepTime;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimePoint() {
        return timePoint;
    }

    public void setTimePoint(long timePoint) {
        this.timePoint = timePoint;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Override
    public String toString() {
        return "Lyric{" +
                "content='" + content + '\'' +
                ", timePoint=" + timePoint +
                ", sleepTime=" + sleepTime +
                '}';
    }
}
