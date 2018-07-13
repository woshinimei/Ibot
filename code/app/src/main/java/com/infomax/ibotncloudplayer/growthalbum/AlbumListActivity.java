package com.infomax.ibotncloudplayer.growthalbum;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.infomax.ibotncloudplayer.FullScreenActivity;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.growthalbum.bean.ChildInfo;
import com.infomax.ibotncloudplayer.growthalbum.utils.Filed;
import com.infomax.ibotncloudplayer.growthalbum.utils.OtherUtils;
import com.infomax.ibotncloudplayer.growthalbum.view.EmptyRecyclerView;
import com.infomax.ibotncloudplayer.utils.NetUtils;
import com.infomax.ibotncloudplayer.utils.ToastUtils;
import com.infomax.ibotncloudplayer.utils.Utils;
import com.xiaweizi.cornerslibrary.CornersProperty;
import com.xiaweizi.cornerslibrary.RoundCornersTransformation;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * @Author:create by jinlong.zou
 * @Date: 2018/3/8.
 * @Function:
 */

public class AlbumListActivity extends FullScreenActivity implements View.OnClickListener {
    private static final String TAG = AlbumListActivity.class.getSimpleName();
    private EmptyRecyclerView mRecyclerView;
    private ImageView emptyView;
    private LinearLayout headerBar;
    private List<ChildInfo.DataBean> dataBeans = new ArrayList<>();
    private AlbumRecyclerViewAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_list);
        headerBar = (LinearLayout) this.findViewById(R.id.header_bar);
        headerBar.setOnClickListener(this);

        mRecyclerView = (EmptyRecyclerView) this.findViewById(R.id.recyclerView);
        adapter = new AlbumRecyclerViewAdapter(this);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ChildInfo.DataBean bean = dataBeans.get(position);
                Intent intent = new Intent(AlbumListActivity.this,PhotoListActivity.class);
                intent.putExtra("faceId", bean.getFaceid());
                startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        emptyView = (ImageView) findViewById(R.id.empty_iv);
        ProgressBar loading = (ProgressBar) findViewById(R.id.pb_loading);
        mRecyclerView.setEmptyView(loading);
        fitData();
    }

    private void fitData() {
        getAlbumList();
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    private class AlbumRecyclerViewAdapter extends RecyclerView.Adapter<AlbumInfoHolder> implements View.OnClickListener {

        private OnItemClickListener mOnItemClickListener = null;
        private LayoutInflater inflater;
        private Context context;
        private RoundCornersTransformation transformation;
        public  AlbumRecyclerViewAdapter(Context context){
            this.context = context;
            inflater = LayoutInflater.from(context);
            transformation = new RoundCornersTransformation(context,
                    new CornersProperty().setCornersRadius(OtherUtils.dip2px(context, 10)));
        }

        @Override
        public AlbumInfoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_album, parent, false);
            AlbumInfoHolder holder = new AlbumInfoHolder(view);
            view.setOnClickListener(this);
            return holder;
        }

        @Override
        public void onBindViewHolder(AlbumInfoHolder holder, int position) {
            ChildInfo.DataBean bean = dataBeans.get(position);
            holder.itemView.setTag(position);
            Glide.with(context)
                    .load(bean.getPicture())
                    .placeholder(R.drawable.default_ibotn)
                    .error(R.drawable.default_ibotn)
                    .bitmapTransform(transformation)
                    .skipMemoryCache(true)
                    .into(holder.albumImage);
            holder.albumName.setText(bean.getChildname());
        }

        @Override
        public int getItemCount() {
            return dataBeans.size();
        }

        @Override
        public void onClick(View view) {
            if(mOnItemClickListener != null)
                mOnItemClickListener.onItemClick(view, (int) view.getTag());
        }

        public void setOnItemClickListener(OnItemClickListener listener){
            this.mOnItemClickListener = listener;
        }
    }

    private class AlbumInfoHolder extends RecyclerView.ViewHolder{

        private ImageView albumImage;
        private TextView albumName;
        public AlbumInfoHolder(View itemView) {
            super(itemView);
            albumImage = (ImageView) itemView.findViewById(R.id.iv_album);
            albumName = (TextView) itemView.findViewById(R.id.rb_album);
        }
    }

    private void getAlbumList(){
        if (!NetUtils.isNetworkConnected(this)) {//先检查网络
            ToastUtils.showCustomToast(getResources().getString(R.string.network_can_not_use));
            return;
        }
        Map<String, String> params = new HashMap<>();
        String terminalId = Utils.getDeviceSerial();
//        terminalId = "0016112909483";
        Log.i(TAG,"jlzou terminalId:" + terminalId);
        params.put("cmd", Filed.TEACHER_GET_CHILD_LIST_CMD);
        params.put("terminalid", terminalId);
        OkHttpUtils.post()
                .url(Filed.URL)
                .params(params)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG,"onError()>>>e.getMessage():" + e.getMessage());
                        ToastUtils.showCustomToast(getString(R.string.text_server_bus));
                        mRecyclerView.setEmptyView(emptyView);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.i(TAG,"jlzou response:" + response);
                        Gson gson = new Gson();
                        ChildInfo childInfo = gson.fromJson(response, ChildInfo.class);
                        if (childInfo != null && childInfo.getStatus() == 200){
                            List<ChildInfo.DataBean> data = childInfo.getData();
                            if(data != null){
                                dataBeans.addAll(childInfo.getData());
                                adapter.notifyDataSetChanged();
                            }
                        }else {
                            ToastUtils.showCustomToast(getString(R.string.text_server_bus));
                        }
                        mRecyclerView.setEmptyView(emptyView);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}
