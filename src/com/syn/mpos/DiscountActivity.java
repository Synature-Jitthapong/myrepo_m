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
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
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
	private ListView mLvDiscount;
	private EditText mTxtSubTotal;
	private EditText mTxtTotalDiscount;
	private EditText mTxtTotalPrice;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_discount);
		mContext = DiscountActivity.this;

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
			exit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
	
	private float calculateDiscount(int orderDetailId, int vatType, float totalPrice, float discount) {
		mIsEdited = true;

		float totalPriceAfterDiscount = totalPrice - discount;
		mTrans.discountEatchProduct(orderDetailId, mTransactionId,
				mComputerId, vatType, totalPrice, discount);

		summary();
		return totalPriceAfterDiscount;
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
		public View getView(int position, View convertView, ViewGroup parent) {
			final OrderTransaction.OrderDetail order =
					mOrderLst.get(position);
			final ViewHolder holder;
			
			if(convertView == null){
				convertView = inflater.inflate(R.layout.discount_template, null);
				holder = new ViewHolder();
				holder.tvNo = (TextView) convertView.findViewById(R.id.tvNo);
				holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
				holder.tvQty = (TextView) convertView.findViewById(R.id.tvQty);
				holder.tvUnitPrice = (TextView) convertView.findViewById(R.id.tvPrice);
				holder.tvTotalPrice = (TextView) convertView.findViewById(R.id.tvTotalPrice);
				holder.txtDiscount = (EditText) convertView.findViewById(R.id.txtDisPrice);
				holder.tvSalePrice = (TextView) convertView.findViewById(R.id.tvSalePrice);
				holder.txtDiscount.setSelectAllOnFocus(true);
				
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tvNo.setText(Integer.toString(position + 1));
			holder.tvName.setText(order.getProductName());
			holder.tvQty.setText(mFormat.qtyFormat(order.getQty()));
			holder.tvUnitPrice.setText(mFormat.currencyFormat(order.getPricePerUnit()));
			holder.tvTotalPrice.setText(mFormat.currencyFormat(order.getTotalRetailPrice()));
			holder.txtDiscount.setText(mFormat.currencyFormat(order.getPriceDiscount()));
			holder.tvSalePrice.setText(mFormat.currencyFormat(order.getTotalSalePrice()));
			holder.txtDiscount.clearFocus();
			
			holder.txtDiscount.setOnFocusChangeListener(new OnFocusChangeListener(){

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					EditText txtDisPrice = (EditText) v;
					if(!hasFocus){
						float discount = 0.0f;
						
						try {
							discount = Float.parseFloat(txtDisPrice
									.getText().toString());
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}

						if (discount >= 0
								&& order.getTotalRetailPrice() >= discount) {
							float salePrice = calculateDiscount(
									order.getOrderDetailId(),
									order.getVatType(),
									order.getTotalRetailPrice(),
									discount);

							holder.tvSalePrice.setText(mFormat
									.currencyFormat(salePrice));
						} else {
							txtDisPrice.setText(mFormat.currencyFormat(order.getPriceDiscount()));
						}	
					}
				}
				
			});

			if(position % 2 == 0)
				convertView.setBackgroundResource(R.color.smoke_white);
			else
				convertView.setBackgroundResource(R.color.grey_light);
			
			
			return convertView;
		}
		
		public class ViewHolder{
			TextView tvNo;
			TextView tvName;
			TextView tvQty;
			TextView tvUnitPrice;
			TextView tvTotalPrice;
			EditText txtDiscount;
			TextView tvSalePrice;
		}
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
				if(event.getAction() == KeyEvent.ACTION_DOWN){
					EditText txtDisCount = (EditText) v.findViewById(R.id.txtDisPrice);
					if(txtDisCount != null){
						txtDisCount.clearFocus();
					}
				}
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

		mTxtSubTotal.setText(mFormat.currencyFormat(subTotal));
		mTxtTotalDiscount.setText(mFormat.currencyFormat(totalDiscount));
		mTxtTotalPrice.setText(mFormat.currencyFormat(totalSalePrice));
	}

	private void exit() {
		DiscountActivity.this.finish();
	}

	@Override
	public void onSaveClick(View v){
		
	}
	
	@Override
	public void onConfirmClick(View v) {
		if (mTrans.confirmDiscount(mTransactionId, mComputerId))
			exit();
	}

	@Override
	public void onCancelClick(View v) {
		if (mIsEdited) {
			new AlertDialog.Builder(mContext)
					.setTitle(R.string.information)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setMessage(R.string.confirm_cancel)
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mTrans.cancelDiscount(mTransactionId,
											mComputerId);
									exit();
								}
							})
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mTrans.confirmDiscount(mTransactionId,
											mComputerId);
									exit();
								}
							}).show();
		} else {
			exit();
		}
	}
}
