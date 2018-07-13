package com.infomax.ibotncloudplayer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.bean.CloudFolderBean;

import java.util.List;

/**
 * Created by jy on 2016/10/22.
 * 云端 文件夹-PUBLIC-VIDEO， Listview，adapter
 */
public class CloudFolderAdapter extends CommonAdapter<CloudFolderBean>{
    public CloudFolderAdapter(Context context, List<CloudFolderBean> datas, int layoutId) {
        super(context, datas, layoutId);
    }

    @Override
    public void setData(List<CloudFolderBean> data) {
        mDatas = data;
        notifyDataSetChanged();
    }

    @Override
    public void convert(ViewHolder holder, CloudFolderBean bean) {

        holder.setText(R.id.tv_name, bean.name);

        View convertView =  holder.getConvertView();

        if (bean.selected){//选中
            convertView.setSelected(true);
            convertView.setBackgroundColor(Color.parseColor("#5566CDAA"));//浅绿色
        }else {
            convertView.setSelected(false);
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

    }
}
