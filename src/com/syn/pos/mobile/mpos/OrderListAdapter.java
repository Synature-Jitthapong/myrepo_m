package com.syn.pos.mobile.mpos;

import com.syn.pos.mobile.model.OrderTransaction;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class OrderListAdapter extends BaseAdapter{
	private OrderTransaction orderTrans;
	private LayoutInflater inflater;
	private Formatter format;
	
	public OrderListAdapter (Context c, Formatter format, OrderTransaction trans){
		this.orderTrans = trans;
		this.format = format;
		inflater = LayoutInflater.from(c);
	}

	@Override
	public int getCount() {
		return orderTrans.orderDetailLst != null ? orderTrans.orderDetailLst.size() : 0;
	}

	@Override
	public OrderTransaction.OrderDetail getItem(int position) {
		return orderTrans.orderDetailLst.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		OrderTransaction.OrderDetail orderDetail = 
				orderTrans.orderDetailLst.get(position);
		ViewHolder holder;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.order_list_template, null);
			holder = new ViewHolder();
			holder.tvOrderNo = (TextView) convertView.findViewById(R.id.textViewOrderNo);
			holder.tvOrderName = (TextView) convertView.findViewById(R.id.textViewOrderName);
			holder.tvOrderAmount = (TextView) convertView.findViewById(R.id.textViewOrderAmount);
			holder.tvOrderPrice = (TextView) convertView.findViewById(R.id.textViewOrderPrice);
			holder.btnMinus = (Button) convertView.findViewById(R.id.buttonOrderMinus);
			holder.btnPlus = (Button) convertView.findViewById(R.id.buttonOrderPlus);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.tvOrderNo.setText(Integer.toString(position + 1));
		holder.tvOrderName.setText(orderDetail.getProductName());
		holder.tvOrderAmount.setText(format.qtyFormat(orderDetail.getProductAmount()));
		holder.tvOrderPrice.setText(format.currencyFormat(orderDetail.getProductPrice()));
		
		return convertView;
	}
	
	private class ViewHolder{
		TextView tvOrderNo;
		TextView tvOrderName;
		TextView tvOrderAmount;
		TextView tvOrderPrice;
		Button btnMinus;
		Button btnPlus;
	}
}
