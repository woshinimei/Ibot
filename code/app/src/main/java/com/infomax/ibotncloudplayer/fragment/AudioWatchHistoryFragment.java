package com.infomax.ibotncloudplayer.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.activity.EcAudioActivity;
import com.infomax.ibotncloudplayer.adapter.LocalAudioLVAdapter;
import com.infomax.ibotncloudplayer.bean.LearnTrajectoryBean;
import com.infomax.ibotncloudplayer.bean.LocalAudioBean;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.GlideUtils;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.SharedPreferenceUtils;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by juying on 2016-12-12  <br/>
 * 1.教育内容-本地音频-播放历史记录 fragment <br/>
 */
public class AudioWatchHistoryFragment extends Fragment {

    private final String TAG = AudioWatchHistoryFragment.class.getSimpleName();

    private EcAudioActivity mActivity;
    private ListView lv_local;

    private RelativeLayout rl_nodata;

    private SharedPreferences sp;
    private LocalAudioLVAdapter localAudioLvAdapter;

    private int localHistorySize;


    private ArrayList<LearnTrajectoryBean> trajectoryList = new ArrayList<LearnTrajectoryBean>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (EcAudioActivity) getActivity();

        View view = View.inflate(mActivity, R.layout.fragment_watch_historyl_audio, null);

        initViews(view);

        registListener();

        initData();

        return view;
    }

    private void initViews(View view) {

        lv_local = (ListView) view.findViewById(R.id.lv_list_local);

        rl_nodata = (RelativeLayout) view.findViewById(R.id.rl_nodata);

    }



    private void registListener() {

    }
    public void initData() {

        localHistorySize = 0;

        /************本地历史记录********************/
        //保存bean到sp
        sp = mActivity.getSharedPreferences(Constant.MySharedPreference.SP_NAME, Context.MODE_PRIVATE);
//        sp.edit().clear().commit();

        String localList = sp.getString(Constant.MySharedPreference.SP_KEY_LOCAL_AUDIO_LIST, "");

        MyLog.d(TAG, "---->>initData--->>local-->>:" + localList);
        if (TextUtils.isEmpty(localList))
        {
            localHistorySize = 0;
        }else
        {
            try {

                LinkedList<LocalAudioBean> tempList = (LinkedList<LocalAudioBean>) SharedPreferenceUtils.string2Object(localList);
                if (localAudioLvAdapter == null) {
                    localAudioLvAdapter = new LocalAudioLVAdapter(mActivity, tempList, lv_local, mOnRequestVideoListener, mActivity.getTrajectoryHolder());
                    lv_local.setAdapter(localAudioLvAdapter);
                }else
                {
                    localAudioLvAdapter.setData(tempList);
                }

                MyLog.d(TAG, "---->>local--->>tempList.size():"+tempList.size());

                localHistorySize = tempList.size();

            } catch (Exception e) {
                MyLog.d(TAG, "---->>>>Exception:" + e.getMessage());
                localHistorySize = 0;
            }
        }

        changeViewState(localHistorySize);

    }

    private LocalAudioLVAdapter.OnRequestVideoListener mOnRequestVideoListener = new LocalAudioLVAdapter.OnRequestVideoListener() {
        @Override
        public void onRequestImage(String imgPath, ImageView iv, int tagId) {
            GlideUtils.load(mActivity, imgPath, iv);

//            if (tagId == Integer.valueOf(iv.getTag().toString()))
//            {
//
//            }
        }
    };

    /**
     * 切换显示列表，没有数据提示界面
     * @param localHistorySize
     */
    private void changeViewState(int localHistorySize)
    {
            if (localHistorySize > 0 )
            {
                lv_local.setVisibility(View.VISIBLE);
                rl_nodata.setVisibility(View.GONE);
            }else
            {
                lv_local.setVisibility(View.GONE);
                rl_nodata.setVisibility(View.VISIBLE);
            }
    }

    @Override
    public void onResume() {
        super.onResume();
        MyLog.d(TAG, "---->>>>onResume:");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
