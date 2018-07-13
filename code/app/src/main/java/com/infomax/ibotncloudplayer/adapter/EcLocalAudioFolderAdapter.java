package com.infomax.ibotncloudplayer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.bean.EcVideoFolderBean;

import java.util.List;

/**
 * jy
 * desc: 教育内容-本地音频文件夹列表设配器
 * modify:2016-12-12 修改为继承 CommonAdapter
 */
public class EcLocalAudioFolderAdapter extends /*BaseAdapter*/ CommonAdapter<EcVideoFolderBean> {

    public EcLocalAudioFolderAdapter(Context context, List<EcVideoFolderBean> datas, int layoutId) {
        super(context, datas, layoutId);
    }

    @Override
    public void setData(List<EcVideoFolderBean> data) {
        mDatas = data;
        notifyDataSetChanged();
    }

    @Override
    public void convert(ViewHolder holder, EcVideoFolderBean bean) {

        holder.setText(R.id.tv_name,bean.name);
        View convertView = holder.getConvertView();
            if (bean.selected){
                convertView.setSelected(true);
                convertView.setBackgroundColor(Color.parseColor("#5566CDAA"));//浅绿色
            }else {
                convertView.setSelected(false);
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }
    }
}