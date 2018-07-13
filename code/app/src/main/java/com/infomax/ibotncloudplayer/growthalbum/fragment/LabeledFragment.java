package com.infomax.ibotncloudplayer.growthalbum.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.growthalbum.CommonSimpleResponseBean;
import com.infomax.ibotncloudplayer.growthalbum.ImagePagerActivity;
import com.infomax.ibotncloudplayer.growthalbum.PhotoListActivity;
import com.infomax.ibotncloudplayer.growthalbum.bean.AlbumInfo;
import com.infomax.ibotncloudplayer.growthalbum.utils.Filed;
import com.infomax.ibotncloudplayer.growthalbum.utils.PictureConfig;
import com.infomax.ibotncloudplayer.growthalbum.view.EmptyRecyclerView;
import com.infomax.ibotncloudplayer.utils.NetUtils;
import com.infomax.ibotncloudplayer.utils.ToastUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * @Author:create by jinlong.zou
 * @Date: 2018/3/7.
 * @Function:
 */

public class LabeledFragment extends Fragment implements View.OnClickListener {

    private static final String STATE_LIST = "STATE_LIST";
    private static final String TAG = LabeledFragment.class.getSimpleName();
    private EmptyRecyclerView mRecyclerView;
    private TextView sendBtn;
    private TextView editBtn;
    private ImageView markAllBtn;
    private ImageView emptyView;
    private PhotoRecyclerViewAdapter adapter;
    private List<Integer> selectedItemList = new ArrayList<>();
    private List<AlbumInfo.DataBean> dataBeans = new ArrayList<>();
    private boolean isFlush;
    private boolean isEditStatus = false;
    private String faceId;
    private int currentPage = 1;
    private String maxId = "0";
    public static LabeledFragment newInstance(boolean isflush, String faceId) {
        final LabeledFragment labeledFragment = new LabeledFragment();

        final Bundle args = new Bundle();
        args.putBoolean("isflush", isflush);
        args.putString("faceId", faceId);
        labeledFragment.setArguments(args);

        return labeledFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFlush = getArguments() != null ? getArguments().getBoolean("isflush") : null;
        faceId = getArguments() != null ? getArguments().getString("faceId") : null;
        if (savedInstanceState != null) {
//            selectItemList = savedInstanceState.getIntegerArrayList(STATE_LIST);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_unlabeled_list, container, false);
        mRecyclerView = (EmptyRecyclerView) view.findViewById(R.id.recyclerView);
        sendBtn = (TextView) view.findViewById(R.id.btn_send);
        editBtn = (TextView) view.findViewById(R.id.btn_edit);
        markAllBtn = (ImageView) view.findViewById(R.id.mark_all);
        sendBtn.setOnClickListener(this);
        editBtn.setOnClickListener(this);
        markAllBtn.setOnClickListener(this);
        editBtn.setSelected(false);
        editBtn.setText(getResources().getString(R.string.edit));
        sendBtn.setText(getResources().getString(R.string.cancel_label));
        adapter = new PhotoRecyclerViewAdapter(getActivity(), mRecyclerView);
        adapter.setOnClickItemListener(new OnClickItemListener() {
            @Override
            public void onItemClick(View view, int position) {
                ArrayList<String> photoList = new ArrayList<>();
                ArrayList<String> thumbnail = new ArrayList<>();
                for(AlbumInfo.DataBean bean : dataBeans){
                    photoList.add(bean.getUrl());
                    thumbnail.add(bean.getThumbnail());
                }
                PictureConfig config = new PictureConfig.Builder()
                        .setListData(photoList)	//图片数据List<String> list
                        .setThumbnailListData(thumbnail)
                        .setPosition(position)	//图片下标（从第position张图片开始浏览）
                        .setDownloadPath("ibotn")	//图片下载文件夹地址
                        .setIsShowNumber(true)//是否显示数字下标
                        .needDownload(true)	//是否支持图片下载
                        .setPlacrHolder(R.drawable.default_ibotn)	//占位符图片
                        .build();
                ImagePagerActivity.startActivity(getActivity(), config);
            }
        });
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),5));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setOtherItemNum(1);
        emptyView = (ImageView) view.findViewById(R.id.empty_iv);
        ProgressBar loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        mRecyclerView.setEmptyView(loading);
        //加载更多
        adapter.setLoadingMore(new OnLoadingMore() {
            @Override
            public void onLoadMore() {
                getPhotoList();
            }
        });
        fitDatas();
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putIntegerArrayList(STATE_LIST,selectItemList);
    }

    private void fitDatas() {
//        photoList = Data.get().get(0).getPictureList();
        getPhotoList();
    }

    public interface OnClickItemListener{
        void onItemClick(View view, int position);
    }

    private class PhotoRecyclerViewAdapter extends RecyclerView.Adapter<PhotoInfoHolder> implements View.OnClickListener {
        private Context context;
        private LayoutInflater inflater;
        private OnClickItemListener onClickItemListener;

        private final int TYPE_LOAD_MORE = 100;
        private final int TYPE_NORMAL = 101;

        private boolean isLoading;
        private int visibleThreshold = 6;
        private boolean canLoadMore = true;
        OnLoadingMore loadingMore;

        public PhotoRecyclerViewAdapter(Context context, RecyclerView recyclerView){
            this.context = context;
            inflater = LayoutInflater.from(context);

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                    int itemCount = layoutManager.getItemCount();
                    int lastPosition = layoutManager.findLastVisibleItemPosition();
                    Log.i("lastPosition --> " ,   lastPosition + "");
                    Log.i("itemCount  --> " ,  itemCount + " ");

                    if (canLoadMore && !isLoading && (lastPosition >= (itemCount - visibleThreshold))) {
                        if (loadingMore != null) {
                            isLoading = true;
                            Log.i(TAG,"jlzou labeled onLoadMore");
                            loadingMore.onLoadMore();
                        }
                    }
                }
            });
        }

        @Override
        public PhotoInfoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_LOAD_MORE) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more, parent, false);
                PhotoInfoHolder holder = new PhotoInfoHolder(view);
                ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.pb_loading);
                progressBar.setInterpolator(new AccelerateInterpolator(2));
                progressBar.setIndeterminate(true);
                return holder;
            }else {
                View view = inflater.inflate(R.layout.item_photo, parent, false);
                PhotoInfoHolder holder = new PhotoInfoHolder(view);
                view.setOnClickListener(this);
                return holder;
            }
        }

        @Override
        public void onBindViewHolder(PhotoInfoHolder holder, int position) {

        }

        @Override
        public void onBindViewHolder(final PhotoInfoHolder holder, final int position, List payloads) {
            if (getItemViewType(position) == TYPE_LOAD_MORE) {
                View itemView = holder.itemView;
                if (canLoadMore && isLoading) {
                    if (itemView.getVisibility() != View.VISIBLE) {
                        itemView.setVisibility(View.VISIBLE);
                    }
                } else if (itemView.getVisibility() == View.VISIBLE) {
                    itemView.setVisibility(View.GONE);
                }
            }else {
                final AlbumInfo.DataBean bean = dataBeans.get(position);
                if(payloads.isEmpty()){
                    holder.itemView.setTag(position);
                    holder.markImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            boolean isSelected = view.isSelected();
                            view.setSelected(!isSelected);
                            bean.setMark(!isSelected);
                            if (!isSelected) {
                                selectedItemList.add(Integer.valueOf(position));
                            } else {
                                selectedItemList.remove(Integer.valueOf(position));
                            }
                        }
                    });
                    if (!isEditStatus) {
                        holder.markImage.setVisibility(View.GONE);
                        bean.setMark(false);
                    } else {
                        holder.markImage.setVisibility(View.VISIBLE);
                    }
//                DrawableRequestBuilder<String> thumbnailRequest = Glide
//                        .with(context)
//                        .load(bean.getThumbnail());
                    Glide.with(context).load(bean.getThumbnail())
                            .placeholder(R.drawable.default_ibotn)
                            .error(R.drawable.default_ibotn)
                            .skipMemoryCache(true)
//                        .thumbnail(thumbnailRequest)
                            .into(holder.photoImage);
                    holder.markImage.setSelected(bean.isMark());
                }else{
                    if (!isEditStatus) {
                        holder.markImage.setVisibility(View.GONE);
                        bean.setMark(false);
                    } else {
                        holder.markImage.setVisibility(View.VISIBLE);
                    }
                    holder.markImage.setSelected(bean.isMark());
                }
            }
        }

        @Override
        public int getItemCount() {
            return dataBeans.size() + 1;
        }

        @Override
        public void onClick(View view) {
            if(onClickItemListener != null){
                onClickItemListener.onItemClick(view, (int) view.getTag());
            }
        }

        public void setOnClickItemListener(OnClickItemListener listener){
            onClickItemListener = listener;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return TYPE_LOAD_MORE;
            } else {
                return TYPE_NORMAL;
            }
        }

        public void setLoadingMore(OnLoadingMore loadingMore) {
            this.loadingMore = loadingMore;
        }

        public void setLoading(boolean loading) {
            isLoading = loading;
        }

        public void setCanLoadMore(boolean canLoadMore) {
            this.canLoadMore = canLoadMore;
        }
    }

    interface OnLoadingMore {
        void onLoadMore();
    }

    private class PhotoInfoHolder extends RecyclerView.ViewHolder{

        private ImageView photoImage;
        private View maskView;
        private ImageView markImage;
        public PhotoInfoHolder(View itemView) {
            super(itemView);
            photoImage = (ImageView) itemView.findViewById(R.id.iv_photo);
            maskView = itemView.findViewById(R.id.mask);
            markImage = (ImageView) itemView.findViewById(R.id.checkmark);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_send:
                sendUnlabeledList();
                break;
            case R.id.mark_all:
                markAllBtn.setSelected(!markAllBtn.isSelected());
                break;
            case R.id.btn_edit:
                boolean isSelected = editBtn.isSelected();
                editBtn.setSelected(!isSelected);
                if(!isSelected){
                    editBtn.setText(getResources().getString(R.string.cancel_edit));
                    isEditStatus = true;
//                    adapter.notifyDataSetChanged();
                }else {
                    editBtn.setText(getResources().getString(R.string.edit));
                    isEditStatus = false;
                    selectedItemList.clear();
//                    adapter.notifyDataSetChanged();
                }
                adapter.notifyItemRangeChanged(0, dataBeans.size(), new Object());
                break;
        }
    }

    public boolean getStatus(int position){
        return dataBeans.get(position).isMark();
    }

    public void setStatus(int position,boolean status){
        dataBeans.get(position).setMark(status);
        adapter.notifyItemChanged(position, new Object());
        if(status){
            selectedItemList.add(Integer.valueOf(position));
        }else{
            selectedItemList.remove(Integer.valueOf(position));
        }
    }

    public boolean getEditStatus(){
        return isEditStatus;
    }

    private void getPhotoList(){
        if (!NetUtils.isNetworkConnected(getActivity())) {//先检查网络
            ToastUtils.showCustomToast(getResources().getString(R.string.network_can_not_use));
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("cmd", Filed.TEACHER_GET_CHILD_PHOTO_LIST_CMD);
        params.put("page", String.valueOf(currentPage ++));
        params.put("num", String.valueOf(Filed.NUMOFPAGER));
        params.put("faceid",faceId);
        params.put("mark",  Filed.MARKED);
        params.put("maxid", maxId);
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
                        AlbumInfo albumInfo = gson.fromJson(response, AlbumInfo.class);
                        if(albumInfo != null && 200 == albumInfo.getStatus()) {
                            maxId = albumInfo.getMaxid();
                            if (albumInfo.getData() != null) {
                                int lastPostion = dataBeans.size();
                                Log.i(TAG, "jlzou lastPostion:" + lastPostion);
                                dataBeans.addAll(albumInfo.getData());
                                Log.i(TAG, "jlzou dataBeans size:" + dataBeans.size());
                                Log.i(TAG, "jlzou dataBeans getUrl:" + dataBeans.get(0).getUrl());
                                Log.i(TAG, "jlzou albumInfo size:" + albumInfo.getData().size());

//                                adapter.notifyDataSetChanged();
                                adapter.notifyItemInserted(lastPostion);
                                adapter.setLoading(false);
                            }
                            if (Integer.parseInt(albumInfo.getNum()) < Filed.NUMOFPAGER) {
                                adapter.setCanLoadMore(false);
                                Log.i(TAG, "jlzou labeled no more");
                            }
                        }else{
                            ToastUtils.showCustomToast(getString(R.string.text_server_bus));
                        }
                        mRecyclerView.setEmptyView(emptyView);
                    }
                });
    }

    private void sendUnlabeledList() {
        if (!NetUtils.isNetworkConnected(getActivity())) {//先检查网络
            ToastUtils.showCustomToast(getResources().getString(R.string.network_can_not_use));
            return;
        }
        List<String> ids = new ArrayList<>();
        for(int position : selectedItemList){
            ids.add(dataBeans.get(position).getId());
        }
        if(ids.size() <= 0){
            return;
        }
        Log.i(TAG,"jlzou ids:" + ids.toString());
        Map<String, String> params = new HashMap<>();
        params.put("cmd",Filed.TEACHER_CANCEL_LABELED_PICTURE_CMD);
        params.put("id", ids.toString());
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
                        Log.i(TAG,"jlzou labeled response:" + response);
                        Gson gson = new Gson();
                        CommonSimpleResponseBean responseBean = gson.fromJson(response, CommonSimpleResponseBean.class);
                        if(responseBean != null && 200 == responseBean.Status){
                            //刷新当前列表
                            List<AlbumInfo.DataBean> unlabeledList = new ArrayList<>();
                            Collections.sort(selectedItemList, new Comparator<Integer>() {
                                @Override
                                public int compare(Integer o1, Integer o2) {
                                    return o2 - o1;
                                }
                            });
                            for(int position : selectedItemList){
                                AlbumInfo.DataBean bean = dataBeans.get(position);
                                unlabeledList.add(bean);
                                bean.setMark(false);
                                dataBeans.remove(position);
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position , dataBeans.size() - position);
                            }

                            ((PhotoListActivity)getActivity()).refers(unlabeledList,false);
                            selectedItemList.clear();
                        }else{
                            ToastUtils.showCustomToast(getString(R.string.text_server_bus));
                        }
                        mRecyclerView.setEmptyView(emptyView);
                    }
                });
    }

    public void refers(List<AlbumInfo.DataBean> list){
        dataBeans.addAll(0,list);
        adapter.notifyDataSetChanged();
//        mRecyclerView.scrollToPosition(0);
    }
}
