package com.example.mike.mobileplayer.utils;

import com.example.mike.mobileplayer.domain.Lyric;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by 国鑫 on 2017/12/18.
 * 作用：解析歌词工具
 */

public class LyricUtils {

    //是否存在歌词
    private boolean isHaveLyric = false;

    public boolean isHaveLyric() {
        return isHaveLyric;
    }

    //得到歌词集合
    private ArrayList<Lyric> lyrics;

    public ArrayList<Lyric> getLyrics() {
        return lyrics;
    }



    /**
     * 读取歌词文件
     *
     * @param file
     */
    public void readLyricFile(File file) {
        if (file == null || !file.exists()) {
            lyrics = null;
            isHaveLyric = false;
        } else {
            //在这解析
            //1.解析
            lyrics = new ArrayList<>();
            isHaveLyric = true;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),getCharset(file)));

                String line = "";
                while ((line = reader.readLine()) != null) {
                    line = parsedLyric(line);

                }
                reader.close();


            } catch (Exception e) {
                e.printStackTrace();
            }

            //2.排序
            Collections.sort(lyrics, new Comparator<Lyric>() {
                @Override
                public int compare(Lyric lhs, Lyric rhs) {
                    if (lhs.getTimePoint() < rhs.getTimePoint()) {
                        return -1;
                    } else if (lhs.getTimePoint() > rhs.getTimePoint()) {
                        return 1;
                    } else {
                        return 0;
                    }

                }
            });


            //3.计算每句的高亮时间
            for (int i = 0; i < lyrics.size(); i++) {
                Lyric firstLyric = lyrics.get(i);
                if (i + 1 < lyrics.size()){
                    Lyric secondLyric = lyrics.get(i + 1);
                    firstLyric.setSleepTime(secondLyric.getSleepTime() - firstLyric.getSleepTime());

                }
            }
        }
    }


    /**
     * 判断文件编码
     * @param file 文件
     * @return 编码：GBK,UTF-8,UTF-16LE
     */
    public String getCharset(File file) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8";
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF)
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF)
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }

    /**
     * 解析一句歌词
     *
     * @param line
     * @return
     */
    private String parsedLyric(String line) {
        int post1 = line.indexOf("[");  //默认0，没有就返回-1
        int post2 = line.indexOf("]");  //默认9，没有就返回-1
        if (post1 == 0 && post2 != -1) {

            //装时间
            long[] times = new long[getCount(line)];
            String strTime = line.substring(post1 + 1, post2);   //得到【时间】
            times[0] = strTime2LongTime(strTime);

            String content = line;
            int i = 1;
            while (post1 == 0 && post2 != -1) {

                content = content.substring(post2 + 1);
                post1 = content.indexOf("[");
                post2 = content.indexOf("]");
                if (post2 != -1) {
                    strTime = content.substring(post1 + 1, post2);
                    times[i] = strTime2LongTime(strTime);

                    if (times[i] == -1) {
                        return "";
                    }
                    i++;
                }
            }

            //把时间数组和文本关联，并添加到集合
            Lyric lyric = new Lyric();
            for (int j = 0; j < times.length; j++) {
                if (times[j] != 0) {
                    lyric.setContent(content);
                    lyric.setTimePoint(times[j]);
                    //添加到集合中
                    lyrics.add(lyric);
                    lyric = new Lyric();

                }
            }
            return content;
        }
        return "";

    }

    /**
     * 把string类型的time转换称long类型
     *
     * @param strTime
     * @return
     */
    private long strTime2LongTime(String strTime) {

        long result = -1;
        try {
            //1.按：切割
            String[] str1 = strTime.split(":");
            //2.按小数点切割
            String[] str2 = str1[1].split("\\.");

            //分
            long min = Long.parseLong(str1[0]);

            //秒
            long second = Long.parseLong(str2[0]);

            //毫秒
            long mil = Long.parseLong(str2[1]);

            result = min * 60 * 1000 + second * 1000 + mil * 10;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        return result;
    }

    private int getCount(String line) {
        int result = -1;
        String[] left = line.split("\\[");
        String[] right = line.split("\\]");
        if (left.length == 0 && right.length == 0) {
            result = 1;
        } else if (left.length > right.length) {
            result = left.length;
        } else {
            result = right.length;
        }
        return result;

    }
}
