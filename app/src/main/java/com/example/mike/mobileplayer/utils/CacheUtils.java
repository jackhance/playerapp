package com.example.mike.mobileplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mike.mobileplayer.service.MusicPlayerService;

/**
 * Created by 国鑫 on 2017/12/14.
 * 作用：
 */


public class CacheUtils {

    /**
     * 保存数据
     * @param context
     * @param key
     * @param values
     */
    public static void putString(Context context, String key, String values) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("textcache", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(key, values).commit();

    }

    /**
     * 得到缓存的数据
     * @param context
     * @param key
     * @return
     */
    public static String getString(Context context, String key){

        SharedPreferences sharedPreferences = context.getSharedPreferences("textcache", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key,"");

    }

    /**
     * 保存播放模式
     * @param context
     * @param key
     * @param values
     */
    public static void putPlayMode(Context context, String key, int values){
        SharedPreferences sharedPreferences = context.getSharedPreferences("audioModeCache",Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(key,values).commit();

    }

    /**
     * 得到播放模式
     * @param context
     * @param key
     * @return
     */
    public static int getPlayMode(Context context,String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences("audioModeCache",Context.MODE_PRIVATE);
       return sharedPreferences.getInt(key, MusicPlayerService.REPREAT_NORMAL);
    }
}
