package com.example.mike.mobileplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mike.mobileplayer.R;
import com.example.mike.mobileplayer.domain.MediaItem;

import org.xutils.x;

import java.util.ArrayList;

/**
 * Created by mike on 2017/12/2.
 * 功能：netvideopager适配器
 */

public class NetVideoPagerAdapter extends BaseAdapter {

    private Context context;
    private final ArrayList<MediaItem> mediaItems;


    public NetVideoPagerAdapter(Context context, ArrayList<MediaItem> mediaItems) {

        this.context = context;
        this.mediaItems = mediaItems;
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
        if(convertView == null){
            convertView = View.inflate(context,R.layout.item_netvedio_pager,null);
            viewHoder = new ViewHoder();
            viewHoder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHoder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHoder.tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);

            convertView.setTag(viewHoder);

        }else {
            viewHoder = (ViewHoder) convertView.getTag();

        }

        //根据position得到对应的数据
        MediaItem mediaItem = mediaItems.get(position);
        viewHoder.tv_name.setText(mediaItem.getName());
        viewHoder.tv_desc.setText(mediaItem.getDesc());
        x.image().bind(viewHoder.iv_icon,mediaItem.getImageUrl());




        return convertView;
    }

    static class ViewHoder{
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_desc;
    }
}


