package com.syn.mpos;

import java.util.List;

import com.syn.mpos.data.MPOSTransaction;
import com.syn.mpos.model.OrderTransaction;
import com.syn.pos.mobile.mpos.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class DiscountActivity extends Activity {
	private static final String TAG = "DiscountActivity";
	private Context context;
	private Formatter format;
	private MPOSTransaction mposTrans;
	private OrderTransaction orderTrans;
	
	private List<OrderTransaction.OrderDetail> orderLst;
	private TableLayout tbLayoutDiscount;
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
	
		context = DiscountActivity.this;
		
		tbLayoutDiscount = (TableLayout) findViewById(R.id.tableLayoutDiscount);
		tvSubTotal = (TextView) findViewById(R.id.textViewDisSubTotal);
		tvTotalDiscount = (TextView) findViewById(R.id.textViewDisDiscount);
		tvTotalPrice = (TextView) findViewById(R.id.textViewDisTotal);
		
		Intent intent = getIntent();
		transactionId = intent.getIntExtra("transactionId", 0);
		computerId = intent.getIntExtra("computerId", 0);
		
		if(transactionId != 0 && computerId != 0){
			format = new Formatter(DiscountActivity.this);
			mposTrans = new MPOSTransaction(DiscountActivity.this);	
			
			loadOrder();
			summaryPrice();
		}else{
			exit();
		}
	}
	
	private void loadOrder(){
		orderLst = mposTrans.listAllOrders(transactionId, computerId);
		
		LayoutInflater inflater = LayoutInflater.from(context);
		for(int i = 0; i < orderLst.size(); i++){
			View v = inflater.inflate(R.layout.discount_template, null);
			TextView tvDiscountNo = (TextView) v.findViewById(R.id.textViewDisNo);
			TextView tvDiscountProName = (TextView) v.findViewById(R.id.textViewDisProName);
			TextView tvDiscountProAmount = (TextView) v.findViewById(R.id.textViewDisProAmount);
			TextView tvDiscountProPrice = (TextView) v.findViewById(R.id.textViewDisProPrice);
			EditText txtDisPrice = (EditText) v.findViewById(R.id.editTextDisPrice);
			TextView tvDisTotalPrice = (TextView) v.findViewById(R.id.textViewDisTotalPrice);
			
			OrderTransaction.OrderDetail order = 
					orderLst.get(i);
			
			tvDiscountNo.setText(Integer.toString(i + 1));
			tvDiscountProName.setText(order.getProductName());
			tvDiscountProAmount.setText(Float.toString(order.getProductAmount()));
			tvDiscountProPrice.setText(Float.toString(order.getProductPrice()));
			tvDisTotalPrice.setText(Float.toString(order.getProductPrice()));
			
			tbLayoutDiscount.addView(v);
		}
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
}
