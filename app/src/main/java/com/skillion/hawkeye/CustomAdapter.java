package com.skillion.hawkeye;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.R;

/***
 * This class is being used by UploadVideo and SaveVideo classes - used for
 * applying the adapter design pattern in creating list view
 */
public class CustomAdapter extends BaseAdapter {
    Context context;
    String timeStampList[];
    int imageVideo[];
    LayoutInflater inflter;

    public CustomAdapter(Context applicationContext, String[] timeStampList, int[] imageVideo) {
        this.context = context;
        this.timeStampList = timeStampList;
        this.imageVideo = imageVideo;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return timeStampList.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.sample_video_view, null);
        TextView country = (TextView) view.findViewById(R.id.firstLine);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        country.setText(timeStampList[i]);
        icon.setImageResource(imageVideo[i]);
        return view;
    }
}