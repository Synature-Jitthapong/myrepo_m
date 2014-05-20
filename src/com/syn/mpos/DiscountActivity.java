package com.syn.mpos;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.j1tth4.exceptionhandler.ExceptionHandler;
import com.syn.mpos.dao.GlobalPropertyDao;
import com.syn.mpos.dao.MPOSOrderTransaction;
import com.syn.mpos.dao.ProductsDao;
import com.syn.mpos.dao.TransactionDao;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class DiscountActivity extends Activity{
	
	public static final int PRICE_DISCOUNT_TYPE = 1;
	public static final int PERCENT_DISCOUNT_TYPE = 2;
	public static final String DISCOUNT_FRAGMENT_TAG = "DiscountDialog";
	
	private static GlobalPropertyDao sGlobal;
	
	private TransactionDao mTransaction;
	private ProductsDao mProduct;
	
	private MPOSOrderTransaction.MPOSOrderDetail mOrder;
	
	private int mTransactionId;
	private int mPosition = -1;
	private double mTotalPrice = 0.0f;
	private boolean mIsEdited = false;
	
	private DiscountAdapter mDisAdapter;
	private List<MPOSOrderTransaction.MPOSOrderDetail> mOrderLst;
	private LinearLayout mLayoutVat;
	private ListView mLvDiscount;
	private EditText mTxtTotalVatExc;
	private EditText mTxtSubTotal;
	private EditText mTxtTotalDiscount;
	private EditText mTxtTotalPrice;
	private MenuItem mItemConfirm;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		/**
		 * Register ExceptinHandler for catch error when application crash.
		 */
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, 
				MPOSApplication.LOG_DIR, MPOSApplication.LOG_FILE_NAME));
		
		setContentView(R.layout.activity_discount);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		mLayoutVat = (LinearLayout) findViewById(R.id.layoutVat);
		mTxtTotalVatExc = (EditText) findViewById(R.id.txtTotalVatExclude);
		mLvDiscount = (ListView) findViewById(R.id.lvOrder);
		mTxtSubTotal = (EditText) findViewById(R.id.txtSubTotal);
		mTxtTotalDiscount = (EditText) findViewById(R.id.txtTotalDiscount);
		mTxtTotalPrice = (EditText) findViewById(R.id.txtTotalPrice);
		
		Intent intent = getIntent();
		mTransactionId = intent.getIntExtra("transactionId", 0);
		mTransaction = new TransactionDao(getApplicationContext());
		mProduct = new ProductsDao(getApplicationContext());
		sGlobal = new GlobalPropertyDao(getApplicationContext());
		
		mOrderLst = new ArrayList<MPOSOrderTransaction.MPOSOrderDetail>();
		mDisAdapter = new DiscountAdapter();
		mLvDiscount.setAdapter(mDisAdapter);
		mLvDiscount.setOnItemClickListener(new OnItemClickListener(){
	
			@Override
			public void onItemClick(AdapterView<?> parent, View v, final int position,
					final long id) {
				MPOSOrderTransaction.MPOSOrderDetail order = 
						(MPOSOrderTransaction.MPOSOrderDetail) parent.getItemAtPosition(position);
				
				ProductsDao p = new ProductsDao(getApplicationContext());
				if(p.getProduct(order.getProductId()).getDiscountAllow() == 1){
					mPosition = position;
					mOrder = order;
					DiscountDialogFragment discount = 
							DiscountDialogFragment.newInstance(
									mOrder.getProductName(), mOrder.getPriceDiscount(), 
									mOrder.getTotalRetailPrice(), mOrder.getDiscountType());
					discount.show(getFragmentManager(), DISCOUNT_FRAGMENT_TAG);
				}else{
					new AlertDialog.Builder(DiscountActivity.this)
					.setMessage(R.string.not_allow_discount)
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mLvDiscount.setItemChecked(mPosition, true);
						}
					})
					.show();
				}
			}
		});
		loadOrder();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			cancel();
			return true;
		case R.id.itemConfirm:
			mTransaction.confirmDiscount(mTransactionId);
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
		mItemConfirm = menu.findItem(R.id.itemConfirm);
		return true;
	}

	private boolean updateDiscount(double discount, int discountType) {
		if(discount >= 0){
			if(discountType == 1){
				if(mOrder.getTotalRetailPrice() < discount)
					return false;
			}else if(discountType==2){
				if(discount > 100){
					return false;
				}
				discount = mOrder.getTotalRetailPrice() * discount / 100;
			}	
			discount = MPOSUtil.roundingPrice(discount);
			double totalPriceAfterDiscount = MPOSUtil.roundingPrice(mOrder.getTotalRetailPrice() - discount);
			mTransaction.discountEatchProduct(mTransactionId, 
					mOrder.getOrderDetailId(), mOrder.getVatType(),
					mProduct.getVatRate(mOrder.getProductId()), 
					totalPriceAfterDiscount, discount, discountType);
			
			MPOSOrderTransaction.MPOSOrderDetail order = mOrderLst.get(mPosition);
			order.setPriceDiscount(discount);
			order.setTotalSalePrice(totalPriceAfterDiscount);
			order.setDiscountType(discountType);
			
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
		public MPOSOrderTransaction.MPOSOrderDetail getItem(int position) {
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
			final MPOSOrderTransaction.MPOSOrderDetail order =
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
			tvQty.setText(sGlobal.qtyFormat(order.getQty()));
			tvUnitPrice.setText(sGlobal.currencyFormat(order.getPricePerUnit()));
			tvTotalPrice.setText(sGlobal.currencyFormat(order.getTotalRetailPrice()));
			tvDiscount.setText(sGlobal.currencyFormat(order.getPriceDiscount()));
			tvSalePrice.setText(sGlobal.currencyFormat(order.getTotalSalePrice()));
			
			return rowView;
		}
	}
	
	private void loadOrder() {
		if(mTransaction.prepareDiscount(mTransactionId)){
			mOrderLst = mTransaction.listAllOrderForDiscount(mTransactionId);
			mDisAdapter.notifyDataSetChanged();
		}
	}

	private void summary() {
		MPOSOrderTransaction.MPOSOrderDetail summOrder = 
				mTransaction.getSummaryOrderForDiscount(mTransactionId);
		double totalVatExcluded = summOrder.getVatExclude();
		if(totalVatExcluded > 0)
			mLayoutVat.setVisibility(View.VISIBLE);
		else
			mLayoutVat.setVisibility(View.GONE);
		mTotalPrice = summOrder.getTotalSalePrice() + summOrder.getVatExclude();
		mTxtTotalVatExc.setText(sGlobal.currencyFormat(totalVatExcluded));
		mTxtSubTotal.setText(sGlobal.currencyFormat(summOrder.getTotalRetailPrice()));
		mTxtTotalDiscount.setText(sGlobal.currencyFormat(summOrder.getPriceDiscount()));
		mTxtTotalPrice.setText(sGlobal.currencyFormat(mTotalPrice));
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
									mTransaction.cancelDiscount(mTransactionId);
									finish();
								}
							}).show();
		} else {
			finish();
		}	
	}

	public void doPositiveClick(double discount, int discountType){
		if(updateDiscount(discount, discountType)){
			mItemConfirm.setVisible(true);
		}else{
			popupNotAllowDiscount();
		}
	}
	
	public void doNegativeClick(){
		
	}
	
	public static class DiscountDialogFragment extends DialogFragment{
		private String mProductName;
		private double mDiscount;
		private double mTotalRetailPrice;
		private int mDiscountType;
		
		public static DiscountDialogFragment newInstance(String productName, 
				double discount, double totalRetailPrice, int discountType){
			DiscountDialogFragment f = new DiscountDialogFragment();
			Bundle b = new Bundle();
			b.putString("title", productName);
			b.putDouble("discount", discount);
			b.putDouble("totalRetailPrice", totalRetailPrice);
			b.putInt("discountType", discountType);
			f.setArguments(b);
			return f;
		}
		
		private void enterDiscount(EditText editText){
			double discount = mDiscount;
			try {
				discount = MPOSUtil.stringToDouble(editText.getText().toString());
				((DiscountActivity)getActivity()).doPositiveClick(discount, mDiscountType);
				getDialog().dismiss();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			mProductName = getArguments().getString("title");
			mDiscount = getArguments().getDouble("discount");
			mTotalRetailPrice = getArguments().getDouble("totalRetailPrice");
			mDiscountType = getArguments().getInt("discountType");
			if(mDiscountType == 0)
				mDiscountType = PERCENT_DISCOUNT_TYPE;
			
			LayoutInflater inflater = (LayoutInflater)
					getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.discount_dialog, null);
			final EditText txtDiscount = (EditText) v.findViewById(R.id.txtDiscount);
			final RadioGroup rdoDiscountType = (RadioGroup) v.findViewById(R.id.rdoDiscountType);
			if(mDiscountType == PERCENT_DISCOUNT_TYPE)
				((RadioButton)rdoDiscountType.findViewById(R.id.rdoPercent)).setChecked(true);
			else if(mDiscountType == PRICE_DISCOUNT_TYPE)
				((RadioButton)rdoDiscountType.findViewById(R.id.rdoPrice)).setChecked(true);
			if(mDiscountType == PERCENT_DISCOUNT_TYPE)
				txtDiscount.setText(sGlobal.currencyFormat(mDiscount * 100 / mTotalRetailPrice));
			else
				txtDiscount.setText(sGlobal.currencyFormat(mDiscount));
			txtDiscount.setSelectAllOnFocus(true);
			txtDiscount.requestFocus();
			txtDiscount.setOnEditorActionListener(new OnEditorActionListener(){

				@Override
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {
					if(actionId == EditorInfo.IME_ACTION_DONE){
						enterDiscount((EditText)v);
						return true;
					}
					return false;
				}
				
			});
			rdoDiscountType.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					RadioButton rdo = (RadioButton) group.findViewById(checkedId);
					switch(checkedId){
					case R.id.rdoPrice:
						if(rdo.isChecked())
							mDiscountType = PRICE_DISCOUNT_TYPE;
						break;
					case R.id.rdoPercent:
						if(rdo.isChecked())
							mDiscountType = PERCENT_DISCOUNT_TYPE;
						break;
					}
				}
				
			});
		
			return new AlertDialog.Builder(getActivity())
				.setTitle(mProductName)
				.setView(v)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						enterDiscount(txtDiscount);
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						((DiscountActivity)getActivity()).doNegativeClick();
					}
				}).create();
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
	
	private void popupNotAllowDiscount(){
		new AlertDialog.Builder(this)
		.setMessage(R.string.not_allow_discount_price)
		.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		})
		.show();
	}
}
