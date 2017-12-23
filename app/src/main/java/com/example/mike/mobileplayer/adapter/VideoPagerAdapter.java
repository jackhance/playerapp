package com.example.mike.mobileplayer.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mike.mobileplayer.R;
import com.example.mike.mobileplayer.domain.MediaItem;
import com.example.mike.mobileplayer.utils.Utils;

import java.util.ArrayList;

/**
 * Created by mike on 2017/12/2.
 * 功能：videopager适配器
 */

public class VideoPagerAdapter extends BaseAdapter {

    private Context context;
    private final ArrayList<MediaItem> mediaItems;
    private Utils utils;
    private final boolean isVideo;


    public VideoPagerAdapter(Context context, ArrayList<MediaItem> mediaItems, boolean isVideo) {

        this.context = context;
        this.mediaItems = mediaItems;
        this.isVideo = isVideo;
        utils = new Utils();

    }


    @Override
    public int getCount() {
        return mediaItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHoder viewHoder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_vedio_pager, null);
            viewHoder = new ViewHoder();

            viewHoder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHoder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHoder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
            viewHoder.tv_duration = (TextView) convertView.findViewById(R.id.tv_duration);

            convertView.setTag(viewHoder);

        } else {
            viewHoder = (ViewHoder) convertView.getTag();

        }

        //根据position得到对应的数据
        MediaItem mediaItem = mediaItems.get(position);
        viewHoder.tv_name.setText(mediaItem.getName());
        viewHoder.tv_size.setText(Formatter.formatFileSize(context, mediaItem.getSize()));
        viewHoder.tv_duration.setText(utils.stringForTime((int) mediaItem.getDuration()));


        if (!isVideo) {
            //音频页面显示
            viewHoder.iv_icon.setImageResource(R.drawable.icon_audio_default);
        }

        return convertView;
    }

    static class ViewHoder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_size;
        TextView tv_duration;

    }
}


