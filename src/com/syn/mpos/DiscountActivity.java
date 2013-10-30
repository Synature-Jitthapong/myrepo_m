package com.syn.mpos;

import java.util.ArrayList;
import java.util.List;
import com.syn.mpos.R;
import com.syn.mpos.transaction.MPOSTransaction;
import com.syn.pos.OrderTransaction;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
import android.widget.TextView.OnEditorActionListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class DiscountActivity extends Activity implements OnEditorActionListener, OnCheckedChangeListener{
	private static final String TAG = "DiscountActivity";
	private static int mPosition = -1;
	private static int mDiscountType = 1;
	private static float mDiscount = 0.0f;
	
	private int mTransactionId;
	private int mComputerId;
	private Formatter mFormat;
	private MPOSTransaction mTrans;
	private DiscountAdapter mDisAdapter;
	private boolean mIsEdited = false;

	private OrderTransaction.OrderDetail mOrder;
	private List<OrderTransaction.OrderDetail> mOrderLst;
	private LinearLayout mLayoutVat;
	private EditText mTxtExcVat;
	private ListView mLvDiscount;
	private EditText mTxtSubTotal;
	private EditText mTxtTotalDiscount;
	private EditText mTxtTotalPrice;
	private EditText mTxtDiscount;
	private RadioGroup mRdoDiscountType;
	private TextView mTvItemName;
	private MenuItem mItemInput;

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
		switch(item.getItemId()){
		case R.id.itemCancel:
			cancel();
			return true;
		case R.id.itemConfirm:
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
		inflater.inflate(R.menu.activity_discount, menu);
		mItemInput = menu.findItem(R.id.itemDiscountInput);
		mTvItemName = (TextView) mItemInput.getActionView().findViewById(R.id.textView1);
		mTxtDiscount = (EditText) mItemInput.getActionView().findViewById(R.id.txtDiscount);
		mRdoDiscountType = (RadioGroup) mItemInput.getActionView().findViewById(R.id.rdoDisType);
		mTxtDiscount.setOnEditorActionListener(this);
		mRdoDiscountType.setOnCheckedChangeListener(this);
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
			mTrans.discountEatchProduct(mOrder.getOrderDetailId(), mTransactionId,
					mComputerId, mOrder.getVatType(), totalPriceAfterDiscount, mDiscount, mDiscountType);
	
			OrderTransaction.OrderDetail order = 
					mOrderLst.get(mPosition);
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
				mPosition = position;
				mOrder = 
						(OrderTransaction.OrderDetail) parent.getItemAtPosition(position);

				mItemInput.setVisible(true);
				mTvItemName.setText(mOrder.getProductName());
				mTxtDiscount.setText(mFormat.currencyFormat(mOrder.getPriceDiscount()));
				mTxtDiscount.selectAll();
				mTxtDiscount.requestFocus();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mTxtDiscount,
                        InputMethodManager.SHOW_IMPLICIT);
                
				if(mOrder.getDiscountType() == 2)
				{
					mTxtDiscount.setText(mFormat.currencyFormat(
							mOrder.getPriceDiscount() * 100 / mOrder.getTotalRetailPrice()));
				}
				mRdoDiscountType.check(mOrder.getDiscountType() == 1 ? R.id.rdoPrice : R.id.rdoPercent);
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
	
	void clearActionInput(){
		mTvItemName.setText(null);
		mTxtDiscount.setText(null);
		mItemInput.setVisible(false);
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
		if(EditorInfo.IME_ACTION_DONE == actionId){
			float discount = 0.0f;
			try {
				discount = Float.parseFloat(v.getText().toString());
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mDiscount = discount;
			updateDiscount();

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			
            clearActionInput();
			return true;
		}
		return false;
	}
}
