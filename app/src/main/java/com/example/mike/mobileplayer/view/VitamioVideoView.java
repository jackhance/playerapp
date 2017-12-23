package com.example.mike.mobileplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by 国鑫 on 2017/12/9.
 * 作用：自定义VitamioVideoVIEW
 */

public class VitamioVideoView extends io.vov.vitamio.widget.VideoView {
    public VitamioVideoView(Context context) {
        super(context);
    }

    public VitamioVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VitamioVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
    }

    public void setVideoSize(int videoWidth,int videoHeight){
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = videoWidth;
        params.height = videoHeight;
        setLayoutParams(params);

    }

}
