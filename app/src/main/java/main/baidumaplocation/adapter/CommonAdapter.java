package main.baidumaplocation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


import java.util.List;

import main.baidumaplocation.holder.ViewHolder;

/**
 * 公共适配器
 *
 * @author PS
 * @version 1.0
 * @date 2016年4月25日
 */
public abstract class CommonAdapter<T> extends BaseAdapter {

	protected LayoutInflater mInflater;

	protected Context mContext;

	protected List<T> mDatas;

	protected final int mItemLayoutId;

	private int mCurPosition = 0;

	public int getCurPosition() {
		return mCurPosition;
	}

	public void setCurPosition(int mCurPosition) {
		this.mCurPosition = mCurPosition;
	}

	public CommonAdapter(Context context, List<T> mDatas, int itemLayoutId) {
		this.mContext = context;
		this.mInflater = LayoutInflater.from(mContext);
		this.mDatas = mDatas;
		this.mItemLayoutId = itemLayoutId;
	}

	@Override
	public int getCount() {
		return mDatas.size();
	}

	@Override
	public T getItem(int position) {
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder = getViewHolder(position, convertView,
				parent);
		convert(viewHolder, getItem(position), position);
		return viewHolder.getConvertView();
	}

	public abstract void convert(ViewHolder mHolder, T item, int position);

	private ViewHolder getViewHolder(int position, View convertView,
									 ViewGroup parent) {
		return ViewHolder.get(mContext, convertView, parent, mItemLayoutId,
				position);
	}
}
