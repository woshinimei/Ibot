package com.infomax.ibotncloudplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

//避免焦点冲突的方法
//1) xml中控件添加 focusable="false"
//2) xml 中外层布局中添加 descendFocusability="blocksDescendants"
//listview 复用导致内容错乱问题解决
// checkBox
// 在bean中添加一个变量，

/**
 * 通用的列表适配器
 * @author juying
 * 适用于 数据源 是List<T>
 * 
 * @param <T>
 */
public abstract class CommonAdapter<T> extends BaseAdapter {

	protected Context mContext;
	protected List<T> mDatas;
	protected LayoutInflater mInflater;
	protected int layoutId;

	public CommonAdapter(Context context, List<T> datas, int layoutId) {
		mContext = context;
		mDatas = datas;
		this.layoutId = layoutId;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		if (mDatas == null) {
			return 0;
		}
		return mDatas.size();
	}

	@Override
	public T getItem(int position) {

		return (mDatas == null ? null :  (T) mDatas.get(position));
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = ViewHolder.get(mContext, convertView, parent, layoutId, position);

		convert(holder, getItem(position));

		return holder.getConvertView();
	}
	/*** 设置数据 */
	public abstract void setData(List<T> data);

	/**
	 * 控件赋值
	 * 
	 * @param holder
	 * @param bean
	 */
	public abstract void convert(ViewHolder holder, T bean);
}