package com.example.mike.mobileplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mike.mobileplayer.R;
import com.example.mike.mobileplayer.domain.SearchBean;

import org.xutils.x;

import java.util.List;

/**
 * Created by mike on 2017/12/2.
 * 功能：SearchAdapter适配器
 */

public class SearchAdapter extends BaseAdapter {

    private Context context;
    private List<SearchBean.ItemsBean> items;


    public SearchAdapter(Context context, List<SearchBean.ItemsBean> items) {

        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
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
        SearchBean.ItemsBean itemsBean = items.get(position);
        viewHoder.tv_name.setText(itemsBean.getItemTitle());
        viewHoder.tv_desc.setText(itemsBean.getKeywords());
        x.image().bind(viewHoder.iv_icon,itemsBean.getItemImage().getImgUrl1());




        return convertView;
    }

    static class ViewHoder{
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_desc;
    }
}


