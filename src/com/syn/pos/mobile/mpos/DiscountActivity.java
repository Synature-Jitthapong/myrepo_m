package com.syn.pos.mobile.mpos;

import java.util.List;

import com.syn.pos.mobile.model.OrderTransaction;
import com.syn.pos.mobile.mpos.dao.MPOSTransaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;

public class DiscountActivity extends Activity {
	private static final String TAG = "DiscountActivity";
	private Formatter format;
	private MPOSTransaction mposTrans;
	private OrderTransaction orderTrans;
	
	private List<OrderTransaction.OrderDetail> orderLst;
	private ListView lvDiscount;
	private TextView tvSubTotal;
	private TextView tvTotalDiscount;
	private TextView tvTotalVat;
	private TextView tvTotalPrice;
	
	private int transactionId;
	private int computerId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_discount);
	
		lvDiscount = (ListView) findViewById(R.id.listViewDiscount);
		tvSubTotal = (TextView) findViewById(R.id.textViewSubTotal);
		tvTotalDiscount = (TextView) findViewById(R.id.textViewDiscount);
		tvTotalPrice = (TextView) findViewById(R.id.textViewTotalPrice);
		
		Intent intent = getIntent();
		transactionId = intent.getIntExtra("transactionId", 0);
		computerId = intent.getIntExtra("computerId", 0);
		
		if(transactionId != 0 && computerId != 0){
			format = new Formatter(DiscountActivity.this);
			mposTrans = new MPOSTransaction(DiscountActivity.this, format);	
			
			loadOrder();
			summaryPrice();
		}else{
			exit();
		}
	}
	
	private void loadOrder(){
		orderLst = mposTrans.listAllOrders(transactionId, computerId);
		DiscountAdapter adapter = new DiscountAdapter();
		lvDiscount.setAdapter(adapter);	
	}
	
	private void summaryPrice(){
		OrderTransaction.OrderDetail orderDetail = 
				mposTrans.getSummary(transactionId);

		tvSubTotal.setText(format.currencyFormat(orderDetail.getProductPrice()));
		tvTotalDiscount.setText(format.currencyFormat(orderDetail.getEachProductDiscount()));
		tvTotalPrice.setText(format.currencyFormat(orderDetail.getProductPrice() - orderDetail.getEachProductDiscount()));
	}
	
	private void exit(){
		DiscountActivity.this.finish();	
	}
	
	public void okClicked(final View v){
		exit();
	}
	
	public void cancelClicked(final View v){
		exit();
	}
	
	private class DiscountAdapter extends BaseAdapter{
		private LayoutInflater inflater;
		
		public DiscountAdapter(){
			inflater = LayoutInflater.from(DiscountActivity.this);
		}
		
		@Override
		public int getCount() {
			return orderLst != null ? orderLst.size() : 0;
		}

		@Override
		public OrderTransaction.OrderDetail getItem(int position) {
			return orderLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			OrderTransaction.OrderDetail orderDetail = 
					orderLst.get(position);
			ViewHolder holder;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.discount_template, null);
				holder = new ViewHolder();
				holder.tvDiscountNo = (TextView) convertView.findViewById(R.id.textViewDisNo);
				holder.tvProductName = (TextView) convertView.findViewById(R.id.textViewDisProName);
				holder.tvProductAmount = (TextView) convertView.findViewById(R.id.textViewDisProAmount);
				holder.tvProductPrice = (TextView) convertView.findViewById(R.id.textViewDisProPrice);
				holder.tvTotalDiscount = (TextView) convertView.findViewById(R.id.textViewDisTotalPrice);
				holder.txtDiscount = (EditText) convertView.findViewById(R.id.editTextDisPrice);
				
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tvDiscountNo.setText(Integer.toString(position + 1));
			holder.tvProductName.setText(orderDetail.getProductName());
			holder.tvProductAmount.setText(format.qtyFormat(orderDetail.getProductAmount()));
			holder.tvProductPrice.setText(format.currencyFormat(orderDetail.getProductPrice()));
			holder.tvTotalDiscount.setText(format.currencyFormat(orderDetail.getEachProductDiscount()));
			return convertView;
		}
		
		private class ViewHolder{
			TextView tvDiscountNo;
			TextView tvProductName;
			TextView tvProductAmount;
			TextView tvProductPrice;
			TextView tvTotalDiscount;
			EditText txtDiscount;
		}
		
	}
}
