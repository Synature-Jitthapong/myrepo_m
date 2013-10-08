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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class DiscountActivity extends Activity implements OnConfirmClickListener{
	//private static final String TAG = "DiscountActivity";
	private Context mContext;
	private int mTransactionId;
	private int mComputerId;
	private Formatter mFormat;
	private MPOSTransaction mTrans;
	private DiscountAdapter mDisAdapter;
	private boolean mIsEdited = false;

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
		mContext = DiscountActivity.this;

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
		case R.id.itemCancel:
			onCancelClick(item.getActionView());
			return true;
		case R.id.itemConfirm:
			onConfirmClick(item.getActionView());
			return true;
		default:
		return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_confirm, menu);
		menu.findItem(R.id.itemClose).setVisible(false);
		return true;
	}
	
	private boolean updateDiscount(int position, int orderDetailId, int vatType, 
			float totalPrice, float discount, int disType, TextView tvSalePrice) {
		
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
			
			tvSalePrice.setText(mFormat.currencyFormat(totalPriceAfterDiscount));
			summary();
			mIsEdited = true;
			
			return true;
		}
		
		return false;
	}

	private class DiscountAdapter extends BaseAdapter{
		
		private LayoutInflater inflater;
		
		public DiscountAdapter (){
			inflater = LayoutInflater.from(mContext);
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
			final EditText txtDiscount = (EditText) rowView.findViewById(R.id.txtDisPrice);
			RadioGroup rgDisType = (RadioGroup) rowView.findViewById(R.id.rdoDisType);
			txtDiscount.setSelectAllOnFocus(true);
			final TextView tvSalePrice = (TextView) rowView.findViewById(R.id.tvSalePrice);
			
			tvNo.setText(Integer.toString(position + 1) + ".");
			tvName.setText(order.getProductName());
			tvQty.setText(mFormat.qtyFormat(order.getQty()));
			tvUnitPrice.setText(mFormat.currencyFormat(order.getPricePerUnit()));
			tvTotalPrice.setText(mFormat.currencyFormat(order.getTotalRetailPrice()));
			txtDiscount.setText(mFormat.currencyFormat(order.getPriceDiscount()));
			tvSalePrice.setText(mFormat.currencyFormat(order.getTotalSalePrice()));

			switch(order.getDiscountType()){
			case 1:
				rgDisType.check(R.id.rdoPrice);
				break;
				
			case 2:
				rgDisType.check(R.id.rdoPercent);
				txtDiscount.setText(mFormat.currencyFormat(order.getPriceDiscount() * 100 / order.getTotalRetailPrice()));
				break;
			}
			
			rgDisType.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					RadioButton rdoDisType;
					
					switch(checkedId){
					case R.id.rdoPrice:
						rdoDisType = (RadioButton) group.findViewById(checkedId);
						if(rdoDisType.isChecked())
							order.setDiscountType(1);
						
						break;
					case R.id.rdoPercent:
						rdoDisType = (RadioButton) group.findViewById(checkedId);
						if(rdoDisType.isChecked())
							order.setDiscountType(2);
							
						break;
					}
					
					float discount = 0.0f;
					try {
						discount = Float.parseFloat(txtDiscount.getText().toString());
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					
					if (updateDiscount(position, order.getOrderDetailId(),
							order.getVatType(), order.getTotalRetailPrice(),
							discount, order.getDiscountType(),
							tvSalePrice)) {

					} else {
						Toast toast = Toast
								.makeText(mContext,
										R.string.not_allow_discount,
										Toast.LENGTH_SHORT);
						toast.show();
						txtDiscount.setText(mFormat.currencyFormat(order.getPriceDiscount()));
					}
				}
				
			});
			
			txtDiscount.setOnFocusChangeListener(new OnFocusChangeListener(){

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus) {
						EditText txtDisPrice = (EditText) v;
						float discount = 0.0f;

						try {
							discount = Float.parseFloat(txtDisPrice.getText()
									.toString());
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}

						if (updateDiscount(position, order.getOrderDetailId(),
								order.getVatType(),
								order.getTotalRetailPrice(), discount,
								order.getDiscountType(), tvSalePrice)) {

						} else {
							Toast toast = Toast.makeText(mContext,
									R.string.not_allow_discount,
									Toast.LENGTH_SHORT);
							toast.show();
							txtDiscount.setText(mFormat.currencyFormat(order.getPriceDiscount()));
						}
					}
				}
			});

			if(position % 2 == 0)
				rowView.setBackgroundResource(R.color.smoke_white);
			else
				rowView.setBackgroundResource(R.color.light_gray);
			
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
		mFormat = new Formatter(mContext);
		mTrans = new MPOSTransaction(mContext);
		mOrderLst = new ArrayList<OrderTransaction.OrderDetail>();
		mDisAdapter = new DiscountAdapter();
		mLvDiscount.setAdapter(mDisAdapter);
		mLvDiscount.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				clearFocus(v);
				return false;
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

	@Override
	public void onSaveClick(View v){
		
	}
	
	@Override
	public void onConfirmClick(View v) {
		if (mTrans.confirmDiscount(mTransactionId, mComputerId))
			finish();
	}

	@Override
	public void onCancelClick(View v) {
		if (mIsEdited) {
			new AlertDialog.Builder(mContext)
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
			onCancelClick(null);
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
}
