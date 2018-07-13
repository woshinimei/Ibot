package com.infomax.ibotncloudplayer.activity.base;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import java.util.List;

/**
 * jy
 *  Android的Fragment中onActivityResult不被调用的解决方案
 *  然后我们继承这个BaseFragmentActivity即可，但是要注意，在Fragment中启动Activity时，一定要调用根Fragment的启动方法，如下注释代码：
 */
public class BaseFragmentActiviy extends /*FullScreenFragmentActivity*/ FragmentActivity {
 private static final String TAG = "BaseFragmentActiviy";

 @Override
 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  FragmentManager fm = getSupportFragmentManager();
  int index = requestCode >> 16;
  if (index != 0) {
   index--;
   if (fm.getFragments() == null || index < 0
           || index >= fm.getFragments().size()) {
    Log.w(TAG, "Activity result fragment index out of range: 0x"
            + Integer.toHexString(requestCode));
    return;
   }
   Fragment frag = fm.getFragments().get(index);
   if (frag == null) {
    Log.w(TAG, "Activity result no fragment exists for index: 0x"
            + Integer.toHexString(requestCode));
   } else {
    handleResult(frag, requestCode, resultCode, data);
   }
   return;
  }

 }

 /**
  * 递归调用，对所有子Fragement生效
  *
  * @param frag
  * @param requestCode
  * @param resultCode
  * @param data
  */
 private void handleResult(Fragment frag, int requestCode, int resultCode,
                           Intent data) {
  frag.onActivityResult(requestCode & 0xffff, resultCode, data);
  List<Fragment> frags = frag.getChildFragmentManager().getFragments();
  if (frags != null) {
   for (Fragment f : frags) {
    if (f != null)
     handleResult(f, requestCode, resultCode, data);
   }
  }
 }
}

///*
///**
//  * 得到根Fragment
//  *
//  * @return
//  */
//private Fragment getRootFragment() {
// Fragment fragment = getParentFragment();
// while (fragment.getParentFragment() != null) {
//  fragment = fragment.getParentFragment();
// }
// return fragment;
//
//}
//
// /**
//  * 启动Activity
//  */
// private void onClickTextViewRemindAdvancetime() {
//  Intent intent = new Intent();
//  intent.setClass(getActivity(), YourActivity.class);
//  intent.putExtra("TAG","TEST");
//  getRootFragment().startActivityForResult(intent, 1001);
// }
// */