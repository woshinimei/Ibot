package com.infomax.ibotncloudplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.bean.Icon;

import java.util.List;

/**
 * Created by hushaokun on 2018/6/6.
 */

public class IconGvAdater extends BaseAdapter {
    List<Icon> list;
    Context context;

    public IconGvAdater(List<Icon> list, Context context) {
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
        ViewHoldel holdel;
        if (view == null) {
            view = View.inflate(context, R.layout.icon_gv_item, null);
            holdel = new ViewHoldel(view);
            view.setTag(holdel);
        } else {
            holdel = (ViewHoldel) view.getTag();
        }
        Icon icon = list.get(i);
        holdel.img.setImageResource(icon.getUrl());
        holdel.tvName.setText(icon.getName() + "");

        return view;
    }

    static class ViewHoldel {
        TextView tvName;
        ImageView img;
        ViewHoldel(View view) {
            tvName = (TextView) view.findViewById(R.id.tv_name);
            img = (ImageView) view.findViewById(R.id.iv_img);

        }
    }

}
