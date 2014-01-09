package com.syn.mpos;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.R;
import com.syn.mpos.database.GlobalProperty;
import com.syn.mpos.database.Products;
import com.syn.mpos.database.transaction.Transaction;
import com.syn.pos.OrderTransaction;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class DiscountActivity extends Activity implements OnEditorActionListener, 
	OnCheckedChangeListener, OnFocusChangeListener, OnClickListener{
	private static final String TAG = "DiscountActivity";
	
	private int mTransactionId;
	private int mComputerId;
	private int mPosition = -1;
	private int mDiscountType = 1;
	private float mDiscount = 0.0f;
	private float mTotalPrice = 0.0f;

	private DiscountAdapter mDisAdapter;
	private boolean mIsEdited = false;

	private GlobalProperty mGlobalProp;
	private Transaction mTransaction;
	private Products mProduct;
	private OrderTransaction.OrderDetail mOrder;
	private List<OrderTransaction.OrderDetail> mOrderLst;
	private LinearLayout mLayoutVat;
	private ListView mLvDiscount;
	private TextView mTvExcVat;
	private TextView mTvSubTotal;
	private TextView mTvTotalDiscount;
	private TextView mTvTotalPrice;
	private EditText mTxtDiscount;
	private RadioGroup mRdoDiscountType;
	private Button mBtnDone;
	private TextView mTvItemName;
	private MenuItem mItemInput;
	private MenuItem mItemClose;
	private MenuItem mItemConfirm;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_discount);
		mLayoutVat = (LinearLayout) findViewById(R.id.layoutVat);
		mTvExcVat = (TextView) findViewById(R.id.tvVatExclude);
		mLvDiscount = (ListView) findViewById(R.id.lvOrder);
		mTvSubTotal = (TextView) findViewById(R.id.tvSubTotal);
		mTvTotalDiscount = (TextView) findViewById(R.id.tvTotalDiscount);
		mTvTotalPrice = (TextView) findViewById(R.id.tvTotalPrice);
		mTvTotalDiscount.setOnEditorActionListener(this);
		
		Intent intent = getIntent();
		mTransactionId = intent.getIntExtra("transactionId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);
		init();
	}

	private void init(){
		mGlobalProp = new GlobalProperty(this);
		mTransaction = new Transaction(this);
		mProduct = new Products(this);
		mOrderLst = new ArrayList<OrderTransaction.OrderDetail>();
		mDisAdapter = new DiscountAdapter();
		mLvDiscount.setAdapter(mDisAdapter);
		mLvDiscount.setOnItemClickListener(new OnItemClickListener(){
	
			@Override
			public void onItemClick(AdapterView<?> parent, View v, final int position,
					final long id) {
				mPosition = position;
				mOrder = (OrderTransaction.OrderDetail) parent.getItemAtPosition(position);
				
				mTvItemName.setText(mOrder.getProductName());
				mTxtDiscount.setText(mGlobalProp.currencyFormat(mOrder.getPriceDiscount()));
				if(mOrder.getDiscountType() == 2){
					mTxtDiscount.setText(mGlobalProp.currencyFormat(
							mOrder.getPriceDiscount() * 100 / mOrder.getTotalRetailPrice()));
				}
				mRdoDiscountType.check(mOrder.getDiscountType() == 1 ? R.id.rdoPrice : R.id.rdoPercent);
				mTxtDiscount.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mTxtDiscount, InputMethodManager.SHOW_IMPLICIT);
                
				mItemInput.setVisible(true);
				mItemClose.setVisible(false);
				mItemConfirm.setVisible(false);
			}
		});
		loadOrder();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.itemCancel:
			cancel();
			return true;
		case R.id.itemConfirm:
			if (mTransaction.confirmDiscount(mTransactionId, mComputerId))
				finish();
			return true;
		default:
		return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_discount, menu);
		mItemInput = menu.findItem(R.id.itemDiscountInput);
		mItemClose = menu.findItem(R.id.itemCancel);
		mItemConfirm = menu.findItem(R.id.itemConfirm);
		
		mTvItemName = (TextView) mItemInput.getActionView().findViewById(R.id.textView1);
		mTxtDiscount = (EditText) mItemInput.getActionView().findViewById(R.id.txtDiscount);
		mRdoDiscountType = (RadioGroup) mItemInput.getActionView().findViewById(R.id.rdoDisType);
		mBtnDone = (Button) mItemInput.getActionView().findViewById(R.id.btnDone);
		
		mTxtDiscount.setOnEditorActionListener(this);
		mTxtDiscount.setSelectAllOnFocus(true);
		mTxtDiscount.setOnFocusChangeListener(this);
		mRdoDiscountType.setOnCheckedChangeListener(this);
		mBtnDone.setOnClickListener(this);
		return true;
	}
	
	private boolean updateDiscount() {
		if(mDiscount >= 0){
			if(mDiscountType == 1){
				if(mOrder.getTotalRetailPrice() < mDiscount)
					return false;
			}else if(mDiscountType==2){
				if(mDiscount > 100){
					return false;
				}
				mDiscount = mOrder.getTotalRetailPrice() * mDiscount / 100;
			}
				
			float totalPriceAfterDiscount = mOrder.getTotalRetailPrice() - mDiscount;
			
			mTransaction.discountEatchProduct(mOrder.getOrderDetailId(), 
					mTransactionId, mComputerId, 
					mProduct.getVatRate(mOrder.getProductId()), 
					totalPriceAfterDiscount, mDiscount, mDiscountType);
			
			OrderTransaction.OrderDetail order = mOrderLst.get(mPosition);
			order.setPriceDiscount(mDiscount);
			order.setTotalSalePrice(totalPriceAfterDiscount);
			order.setDiscountType(mDiscountType);
			
			mOrderLst.set(mPosition, order);
			mDisAdapter.notifyDataSetChanged();
			mIsEdited = true;
			
			return true;
		}
		
		return false;
	}

	private class DiscountAdapter extends BaseAdapter{
		
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
			
			LayoutInflater inflater = (LayoutInflater)
					DiscountActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			tvQty.setText(mGlobalProp.qtyFormat(order.getQty()));
			tvUnitPrice.setText(mGlobalProp.currencyFormat(order.getPricePerUnit()));
			tvTotalPrice.setText(mGlobalProp.currencyFormat(order.getTotalRetailPrice()));
			tvDiscount.setText(mGlobalProp.currencyFormat(order.getPriceDiscount()));
			tvSalePrice.setText(mGlobalProp.currencyFormat(order.getTotalSalePrice()));
			
			return rowView;
		}
	}
	
	private void loadOrder() {
		if (mTransaction.copyOrderToTmp(mTransactionId, mComputerId)) {
			mOrderLst = mTransaction.listAllOrderTmp(mTransactionId, mComputerId);
			mDisAdapter.notifyDataSetChanged();
		}
	}

	private void summary() {
		float subTotal = mTransaction.getTotalRetailPrice(mTransactionId, mComputerId, true);
		float totalVatExclude = mTransaction.getTotalVatExclude(mTransactionId, mComputerId, true);
		float totalDiscount = mTransaction.getPriceDiscount(mTransactionId, mComputerId, true); 
				
		mTotalPrice = mTransaction.getTotalSalePrice(mTransactionId, mComputerId, true) + 
				totalVatExclude;
		
		if(totalVatExclude > 0)
			mLayoutVat.setVisibility(View.VISIBLE);
		else
			mLayoutVat.setVisibility(View.GONE);
		
		mTvExcVat.setText(mGlobalProp.currencyFormat(totalVatExclude));
		mTvSubTotal.setText(mGlobalProp.currencyFormat(subTotal));
		mTvTotalDiscount.setText(mGlobalProp.currencyFormat(totalDiscount));
		mTvTotalPrice.setText(mGlobalProp.currencyFormat(mTotalPrice));
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
									mTransaction.cancelDiscount(mTransactionId, 
											mComputerId);
									finish();
								}
							}).show();
		} else {
			finish();
		}	
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

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
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
	
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (EditorInfo.IME_ACTION_DONE == actionId) {
			float discount = 0.0f;
			try {
				discount = Float.parseFloat(v.getText().toString());
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			switch (v.getId()) {
			case R.id.txtDiscount:
				mDiscount = discount;
				if(updateDiscount()){
					mItemInput.setVisible(false);
					mItemClose.setVisible(true);
					mItemConfirm.setVisible(true);
				}else{
					popupNotAllowDiscount();
				}
				return true;
			}
		}
		return false;
	}

	private void popupNotAllowDiscount(){
		new AlertDialog.Builder(this)
		.setMessage(R.string.not_allow_discount)
		.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		})
		.show();
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnDone:
			mTxtDiscount.onEditorAction(EditorInfo.IME_ACTION_DONE);
			break;
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(v.getId() == R.id.txtDiscount){
			if(!hasFocus){
				InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}
		}
	}
}
