package com.syn.mpos;

import java.util.ArrayList;
import java.util.List;
import com.syn.mpos.R;
import com.syn.mpos.transaction.MPOSTransaction;
import com.syn.pos.OrderTransaction;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class DiscountActivity extends Activity{
	//private static final String TAG = "DiscountActivity";
	private int mTransactionId;
	private int mComputerId;
	private Formatter mFormat;
	private MPOSTransaction mTrans;
	private DiscountAdapter mDisAdapter;
	private boolean mIsEdited = false;
	private DiscountPopup mDiscountPopup;
	private int mDiscountType = 1; // 1 = price, 2 = percent

	private List<OrderTransaction.OrderDetail> mOrderLst;
	private LinearLayout mLayoutVat;
	private EditText mTxtExcVat;
	private ListView mLvDiscount;
	private EditText mTxtSubTotal;
	private EditText mTxtTotalDiscount;
	private EditText mTxtTotalPrice;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_discount);

		mLayoutVat = (LinearLayout) findViewById(R.id.layoutVat);
		mTxtExcVat = (EditText) findViewById(R.id.txtExcVat);
		mLvDiscount = (ListView) findViewById(R.id.listView1);
		mTxtSubTotal = (EditText) findViewById(R.id.txtSubTotal);
		mTxtTotalDiscount = (EditText) findViewById(R.id.txtTotalDiscount);
		mTxtTotalPrice = (EditText) findViewById(R.id.txtTotalPrice);

		Intent intent = getIntent();
		mTransactionId = intent.getIntExtra("transactionId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);

		if (mTransactionId != 0 && mComputerId != 0) {
			init();
		} else {
			finish();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		clearFocus(getCurrentFocus());
		
		switch(item.getItemId()){
		case R.id.action_cancel:
			cancel();
			return true;
		case R.id.action_confirm:
			if (mTrans.confirmDiscount(mTransactionId, mComputerId))
				finish();
			return true;
		default:
		return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.discount_activity, menu);
		return true;
	}
	
	private boolean updateDiscount(int position, int orderDetailId, int vatType, 
			float totalPrice, float discount, int disType) {
		
		if(discount >= 0){
			if(disType == 1){
				if(totalPrice < discount)
					return false;
			}else if(disType==2){
				if(discount > 100){
					return false;
				}
				discount = totalPrice * discount / 100;
			}
				
			float totalPriceAfterDiscount = totalPrice - discount;
			mTrans.discountEatchProduct(orderDetailId, mTransactionId,
					mComputerId, vatType, totalPriceAfterDiscount, discount, disType);
	
			OrderTransaction.OrderDetail order = 
					mOrderLst.get(position);
			order.setPriceDiscount(discount);
			order.setTotalSalePrice(totalPriceAfterDiscount);
			mOrderLst.set(position, order);
			
			summary();
			mIsEdited = true;
			
			return true;
		}
		
		return false;
	}

	private class DiscountAdapter extends BaseAdapter{
		
		private LayoutInflater inflater;
		
		public DiscountAdapter (){
			inflater = LayoutInflater.from(DiscountActivity.this);
		}
		
		@Override
		public int getCount() {
			return mOrderLst != null ? mOrderLst.size() : 0;
		}

		@Override
		public OrderTransaction.OrderDetail getItem(int position) {
			return mOrderLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public void notifyDataSetChanged() {
			summary();
			super.notifyDataSetChanged();
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final OrderTransaction.OrderDetail order =
					mOrderLst.get(position);
			
			View rowView = convertView;
			rowView = inflater.inflate(R.layout.discount_template, null);
			TextView tvNo = (TextView) rowView.findViewById(R.id.tvNo);
			TextView tvName = (TextView) rowView.findViewById(R.id.tvName);
			TextView tvQty = (TextView) rowView.findViewById(R.id.tvQty);
			TextView tvUnitPrice = (TextView) rowView.findViewById(R.id.tvPrice);
			TextView tvTotalPrice = (TextView) rowView.findViewById(R.id.tvTotalPrice);
			TextView tvDiscount = (TextView) rowView.findViewById(R.id.tvDiscount);
			final TextView tvSalePrice = (TextView) rowView.findViewById(R.id.tvSalePrice);
			
			tvNo.setText(Integer.toString(position + 1) + ".");
			tvName.setText(order.getProductName());
			tvQty.setText(mFormat.qtyFormat(order.getQty()));
			tvUnitPrice.setText(mFormat.currencyFormat(order.getPricePerUnit()));
			tvTotalPrice.setText(mFormat.currencyFormat(order.getTotalRetailPrice()));
			tvDiscount.setText(mFormat.currencyFormat(order.getPriceDiscount()));
			tvSalePrice.setText(mFormat.currencyFormat(order.getTotalSalePrice()));
			
			return rowView;
		}
	}
	
	private void clearFocus(View v){
		v.clearFocus();
		hideKeyboard();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			View v = getCurrentFocus();
			clearFocus(v);
		}
		return super.onTouchEvent(event);
	}
	
	private void init(){
		mFormat = new Formatter(DiscountActivity.this);
		mTrans = new MPOSTransaction(DiscountActivity.this);
		mOrderLst = new ArrayList<OrderTransaction.OrderDetail>();
		mDisAdapter = new DiscountAdapter();
		mLvDiscount.setAdapter(mDisAdapter);
		mLvDiscount.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View v, final int position,
					final long id) {
				
				v.setSelected(true);
				
				final OrderTransaction.OrderDetail order = 
						(OrderTransaction.OrderDetail) parent.getItemAtPosition(position);

				mDiscountType = order.getDiscountType();
				
				if(mDiscountPopup != null)
					mDiscountPopup = null;
				
				mDiscountPopup = 
						DiscountPopup.newInstance(mFormat.currencyFormat(order.getPriceDiscount()),
							mDiscountType, new OnKeyListener(){
	
								@Override
								public boolean onKey(View v, int keyCode, KeyEvent event) {
									float discount = 0.0f;
									try {
										discount = Float.parseFloat(((EditText) 
												v.findViewById(R.id.txtDiscount)).getText().toString());
									} catch (NumberFormatException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
										updateDiscount(position, order.getOrderDetailId(), 
												order.getVatType(), order.getTotalRetailPrice(), 
												discount, mDiscountType);
										mDiscountPopup.dismiss();
										
										mDisAdapter.notifyDataSetChanged();
										return true;
									}
									return false;
								}
							
							}, new OnCheckedChangeListener(){
				
								@Override
								public void onCheckedChanged(RadioGroup group,
										int checkedId) {
									RadioButton rdo = (RadioButton) group.findViewById(checkedId);
									switch(checkedId){
									case R.id.rdoPrice:
										if(rdo.isChecked())
											mDiscountType = 1;
										break;
									case R.id.rdoPercent:
										if(rdo.isChecked())
											mDiscountType = 2;
										break;
									}
								}
								
							});
				mDiscountPopup.show(getFragmentManager(), "DiscountPopup");
			}
		});
		loadOrder();
	}

	private void loadOrder() {
		if (mTrans.copyOrderToTmp(mTransactionId, mComputerId)) {
			mOrderLst = mTrans.listAllOrdersTmp(mTransactionId, mComputerId);
			mDisAdapter.notifyDataSetChanged();
		}
	}

	private void summary() {
		OrderTransaction.OrderDetail orderDetail = 
				mTrans.getSummaryTmp(mTransactionId, mComputerId);

		float subTotal = orderDetail.getTotalRetailPrice();
		float vat = orderDetail.getVat();
		float totalSalePrice = orderDetail.getTotalSalePrice() + vat;
		float totalDiscount = orderDetail.getPriceDiscount() + orderDetail.getMemberDiscount();

		if(vat > 0)
			mLayoutVat.setVisibility(View.VISIBLE);
		else
			mLayoutVat.setVisibility(View.GONE);
		
		mTxtExcVat.setText(mFormat.currencyFormat(vat));
		mTxtSubTotal.setText(mFormat.currencyFormat(subTotal));
		mTxtTotalDiscount.setText(mFormat.currencyFormat(totalDiscount));
		mTxtTotalPrice.setText(mFormat.currencyFormat(totalSalePrice));
	}

	private void cancel(){
		if (mIsEdited) {
			new AlertDialog.Builder(DiscountActivity.this)
					.setTitle(R.string.information)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setMessage(R.string.confirm_cancel)
					.setNegativeButton(R.string.no,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							})
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mTrans.cancelDiscount(mTransactionId,
											mComputerId);
									finish();
								}
							}).show();
		} else {
			finish();
		}	
	}
	
	private void hideKeyboard(){
		getWindow().setSoftInputMode(
			      WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			cancel();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
}
