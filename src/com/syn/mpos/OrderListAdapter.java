package com.syn.mpos;

import java.util.List;

import com.syn.mpos.model.OrderTransaction;
import com.syn.pos.mobile.mpos.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class OrderListAdapter extends BaseAdapter{
	private List<OrderTransaction.OrderDetail> orderDetailLst;
	private LayoutInflater inflater;
	private Formatter format;
	private ListButtonOnClickListener listener;
	private AdapterStateListener state;
	
	public OrderListAdapter (Context c, Formatter format, 
			List<OrderTransaction.OrderDetail> orderLst, ListButtonOnClickListener onClick, AdapterStateListener adapterState){
		this.orderDetailLst = orderLst;
		this.format = format;
		inflater = LayoutInflater.from(c);
		listener = onClick;
		state = adapterState;
	}

	@Override
	public int getCount() {
		return orderDetailLst != null ? orderDetailLst.size() : 0;
	}

	@Override
	public OrderTransaction.OrderDetail getItem(int position) {
		return orderDetailLst.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final OrderTransaction.OrderDetail orderDetail = 
				orderDetailLst.get(position);
		ViewHolder holder;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.order_list_template, null);
			holder = new ViewHolder();
			holder.tvOrderNo = (TextView) convertView.findViewById(R.id.textViewOrderNo);
			holder.tvOrderName = (TextView) convertView.findViewById(R.id.textViewOrderName);
			holder.txtOrderAmount = (EditText) convertView.findViewById(R.id.editTextOrderAmount);
			holder.tvOrderPrice = (TextView) convertView.findViewById(R.id.textViewOrderPrice);
			holder.btnMinus = (Button) convertView.findViewById(R.id.buttonOrderMinus);
			holder.btnPlus = (Button) convertView.findViewById(R.id.buttonOrderPlus);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.tvOrderNo.setText(Integer.toString(position + 1));
		holder.tvOrderName.setText(orderDetail.getProductName());
		holder.txtOrderAmount.setText(format.qtyFormat(orderDetail.getProductAmount()));
		holder.tvOrderPrice.setText(format.currencyFormat(orderDetail.getProductPrice()));
		
		holder.btnMinus.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				listener.onMinusClick(position);
			}
			
		});
		holder.btnPlus.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				listener.onPlusClick(position);
			}
			
		});
		
		return convertView;
	}
	
	@Override
	public void notifyDataSetChanged() {
		state.onNotify();
		super.notifyDataSetChanged();
	}

	private class ViewHolder{
		TextView tvOrderNo;
		TextView tvOrderName;
		EditText txtOrderAmount;
		TextView tvOrderPrice;
		Button btnMinus;
		Button btnPlus;
	}

}
