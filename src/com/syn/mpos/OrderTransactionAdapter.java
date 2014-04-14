package com.syn.mpos;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import com.syn.pos.OrderTransaction;

public abstract class OrderTransactionAdapter extends BaseAdapter {
	
	protected List<OrderTransaction> mTransLst;
	protected LayoutInflater mInflater;

	public OrderTransactionAdapter(Context c, List<OrderTransaction> transLst) {
		mTransLst = transLst;
		mInflater = (LayoutInflater) c
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mTransLst != null ? mTransLst.size() : 0;
	}

	@Override
	public OrderTransaction getItem(int position) {
		return mTransLst.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
