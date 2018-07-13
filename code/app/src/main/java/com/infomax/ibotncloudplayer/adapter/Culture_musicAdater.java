package com.infomax.ibotncloudplayer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.bean.MusicBean;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by hushaokun on 2018/6/8.
 */

public class Culture_musicAdater extends BaseAdapter {
    List<MusicBean> list;
    Context context;
    ViewHolder holder;

    public Culture_musicAdater(List<MusicBean> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
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

        if (view == null) {
            view = View.inflate(context, R.layout.item_lv_music_culture, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (i % 2 == 0) {
            holder.rlContent.setBackgroundResource(R.drawable.culture_lv_item_bg2);
        } else {
            holder.rlContent.setBackgroundResource(R.drawable.culture_lv_item_bg1);
        }
        MusicBean bean = list.get(i);
        boolean selected = bean.isSelected();
        holder.rlContent.setSelected(selected);
        long duration = bean.getDuration();//总时间
        long curDuration = bean.getCurDuration();//当前时间
        //毫秒转换成分秒
        SimpleDateFormat formatter = new SimpleDateFormat("mm:   ss");//格式化时间
        String hms = formatter.format(duration);
        holder.tvTime.setText(hms + "");

        String info = bean.getArtist();
        if (info != null && !info.contains("unknown")) {
            holder.tvInfo.setText(info + "");
        }
        String name = bean.getName();
        if (name != null) {
            name = name.replace(".mp3", "");
        }
        holder.tvName.setText(name + "");
        holder.bar.setMax((int) duration);
        holder.bar.setProgress((int) curDuration);

        return view;
    }



    static class ViewHolder {

        RelativeLayout rlContent;
        TextView tvPlay;
        TextView tvName;
        TextView tvInfo;
        TextView tvTime;
        ProgressBar bar;

        public ViewHolder(View view) {
            rlContent = (RelativeLayout) view.findViewById(R.id.rl_content);
            tvPlay = (TextView) view.findViewById(R.id.iv_play);
            tvName = (TextView) view.findViewById(R.id.tv_name);
            tvInfo = (TextView) view.findViewById(R.id.tv_info);
            tvTime = (TextView) view.findViewById(R.id.tv_time);
            bar = (ProgressBar) view.findViewById(R.id.pbar);
        }
    }
}
