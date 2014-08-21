package com.synature.mpos;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.synature.mpos.database.Formater;
import com.synature.mpos.database.MPOSOrderTransaction;
import com.synature.mpos.database.Products;
import com.synature.mpos.database.Transaction;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
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
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class DiscountActivity extends Activity implements OnItemClickListener, 
	OnCheckedChangeListener, OnClickListener, OnEditorActionListener{
	
	public static final int PRICE_DISCOUNT_TYPE = 1;
	public static final int PERCENT_DISCOUNT_TYPE = 2;
	public static final int OTHER_DISCOUNT_TYPE = 6;

	private Formater mFormat;
	private Transaction mTrans;
	private Products mProduct;
	private MPOSOrderTransaction.MPOSOrderDetail mOrder;
	private DiscountAdapter mDisAdapter;
	private List<MPOSOrderTransaction.MPOSOrderDetail> mOrderLst;
	
	private int mTransactionId;
	private int mPosition = -1;
	private int mDisAllType = PERCENT_DISCOUNT_TYPE;	// default is percent discount
	
	private LinearLayout mSummaryContainer;
	private ListView mLvDiscount;
	private RadioGroup mRdoDisType;
	private EditText mTxtDisAll;
	private Button mBtnApplyDisAll;
	private MenuItem mItemConfirm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		/**
		 * Register ExceptinHandler for catch error when application crash.
		 */
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, 
				Utils.LOG_PATH, Utils.LOG_FILE_NAME));
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		setContentView(R.layout.activity_discount);
		mLvDiscount = (ListView) findViewById(R.id.lvOrder);
		mSummaryContainer = (LinearLayout) findViewById(R.id.summaryContainer);

		Intent intent = getIntent();
		mTransactionId = intent.getIntExtra("transactionId", 0);	
		mTrans = new Transaction(this);
		mProduct = new Products(this);
		mFormat = new Formater(this);
		mOrder = new MPOSOrderTransaction.MPOSOrderDetail();

		mTrans.prepareDiscount(mTransactionId);
		setupCustomView();
		setupDiscountListView();
		loadOrder();
	}
	
	private void setupDiscountListView(){
		mOrderLst = new ArrayList<MPOSOrderTransaction.MPOSOrderDetail>();
		mDisAdapter = new DiscountAdapter();
		mLvDiscount.setAdapter(mDisAdapter);
		mLvDiscount.setOnItemClickListener(this);	
	}
	
	private void setupCustomView(){
		LayoutInflater inflater = getLayoutInflater();
		LinearLayout disAllView = (LinearLayout) inflater.inflate(R.layout.action_input_discount, null, false);
		mBtnApplyDisAll = (Button) disAllView.findViewById(R.id.btnApply);
		getActionBar().setCustomView(disAllView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		getActionBar().setDisplayShowCustomEnabled(true);
		mRdoDisType = (RadioGroup) disAllView.findViewById(R.id.rdoDiscountType);
		mTxtDisAll = (EditText) disAllView.findViewById(R.id.txtDiscount);
		mTxtDisAll.clearFocus();
		mBtnApplyDisAll.setOnClickListener(this);
		mTxtDisAll.setOnEditorActionListener(this);
		mRdoDisType.setOnCheckedChangeListener(this);
	}
	
	public static class DiscountDialogFragment extends DialogFragment{
		
		private String mProductName;
		private double mDiscount;
		private double mTotalRetailPrice;
		private int mPriceOrPercent;
		
		public static DiscountDialogFragment newInstance(String productName, 
				double discount, double totalRetailPrice, int priceOrPercent){
			DiscountDialogFragment f = new DiscountDialogFragment();
			Bundle b = new Bundle();
			b.putString("title", productName);
			b.putDouble("discount", discount);
			b.putDouble("totalRetailPrice", totalRetailPrice);
			b.putInt("priceOrPercent", priceOrPercent);
			f.setArguments(b);
			return f;
		}
		
		private void enterDiscount(EditText editText){
			double discount = mDiscount;
			try {
				discount = Utils.stringToDouble(editText.getText().toString());
				DiscountActivity host = (DiscountActivity) getActivity();
				host.updateDiscount(discount, mPriceOrPercent);
				host.mItemConfirm.setVisible(true);
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
			mPriceOrPercent = getArguments().getInt("priceOrPercent");
			if(mPriceOrPercent == 0)
				mPriceOrPercent = PERCENT_DISCOUNT_TYPE;
			
			LayoutInflater inflater = (LayoutInflater)
					getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.input_discount_layout, null, false);
			final EditText txtDiscount = (EditText) v.findViewById(R.id.txtDiscount);
			final RadioGroup rdoDiscountType = (RadioGroup) v.findViewById(R.id.rdoDiscountType);
			if(mPriceOrPercent == PERCENT_DISCOUNT_TYPE)
				((RadioButton)rdoDiscountType.findViewById(R.id.rdoPercent)).setChecked(true);
			else if(mPriceOrPercent == PRICE_DISCOUNT_TYPE)
				((RadioButton)rdoDiscountType.findViewById(R.id.rdoPrice)).setChecked(true);
			if(mPriceOrPercent == PERCENT_DISCOUNT_TYPE)
				txtDiscount.setText(((DiscountActivity) getActivity()).mFormat.currencyFormat(mDiscount * 100 / mTotalRetailPrice));
			else
				txtDiscount.setText(((DiscountActivity) getActivity()).mFormat.currencyFormat(mDiscount));
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
							mPriceOrPercent = PRICE_DISCOUNT_TYPE;
						break;
					case R.id.rdoPercent:
						if(rdo.isChecked())
							mPriceOrPercent = PERCENT_DISCOUNT_TYPE;
						break;
					}
				}
				
			});
		
			return new AlertDialog.Builder(getActivity())
				.setTitle(mProductName)
				.setCancelable(false)
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
					}
				}).create();
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			View focusView = getActivity().getCurrentFocus();
			if(focusView instanceof EditText){
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
					      Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(((EditText) focusView).getWindowToken(), 0);
			}
			super.onDismiss(dialog);
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			cancel();
			return true;
		case R.id.itemConfirm:
			mTrans.confirmDiscount(mTransactionId);
			mTrans.updateTransactionPromotion(mTransactionId, 0);
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

	/*
	 * this following by 
	 * if discount = 200
	 * A 100 (100 / 1000) * 200 = x
	 * B 200 (200 / 1000) * 200 = y
	 * C 300 (300 / 1000) * 200 = z
	 * D 400 = 200 - (x + y + z)
	 * Total 1000
	 */
	private void discountAll(){
		if(!TextUtils.isEmpty(mTxtDisAll.getText())){
			InputMethodManager imm = (InputMethodManager)getSystemService(
				      Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mTxtDisAll.getWindowToken(), 0);
			// clear discount first
			clearDiscount();
			try {
				Products product = new Products(this);
				double discountAll = Utils.stringToDouble(mTxtDisAll.getText().toString());
				double maxTotalRetailPrice = mTrans.getMaxTotalRetailPrice(mTransactionId);
				double totalDiscount = 0.0d;
				MPOSOrderTransaction.MPOSOrderDetail summOrder = 
						mTrans.getSummaryOrderForDiscount(mTransactionId);
				double totalPrice = summOrder.getTotalRetailPrice();
				if(discountAll <= summOrder.getTotalRetailPrice()){
					mTxtDisAll.setText(null);
					List<MPOSOrderTransaction.MPOSOrderDetail> orderLst = mOrderLst;
					for(MPOSOrderTransaction.MPOSOrderDetail order : orderLst){
						if(product.getProduct(order.getProductId()).getDiscountAllow() == 1){
							double totalRetailPrice = order.getTotalRetailPrice();
							if(mDisAllType == PRICE_DISCOUNT_TYPE){
								if(totalRetailPrice < maxTotalRetailPrice){
									double discount = (totalRetailPrice / totalPrice) * discountAll;
									BigDecimal big = new BigDecimal(discount);
									big = big.setScale(0, BigDecimal.ROUND_FLOOR);
									discount = big.doubleValue();
									totalDiscount += discount;
									double totalPriceAfterDiscount = totalRetailPrice - discount;
									mTrans.discountEatchProduct(mTransactionId, order.getOrderDetailId(),
											order.getVatType(), mProduct.getVatRate(order.getProductId()), 
											totalPriceAfterDiscount, discount, PRICE_DISCOUNT_TYPE, 0, 
											OTHER_DISCOUNT_TYPE, "");
								}
							}else if(mDisAllType == PERCENT_DISCOUNT_TYPE){
								double discount = calculateDiscount(order.getTotalRetailPrice(), 
										discountAll, PERCENT_DISCOUNT_TYPE);
								double totalPriceAfterDiscount = totalRetailPrice - discount;
								mTrans.discountEatchProduct(mTransactionId, order.getOrderDetailId(),
										order.getVatType(), mProduct.getVatRate(order.getProductId()), 
										totalPriceAfterDiscount, discount, PERCENT_DISCOUNT_TYPE, 0, 
										OTHER_DISCOUNT_TYPE, "");
							}
						}
					}
					if(mDisAllType == PRICE_DISCOUNT_TYPE){
						Iterator<MPOSOrderTransaction.MPOSOrderDetail> it = orderLst.iterator();
						while(it.hasNext()){
							MPOSOrderTransaction.MPOSOrderDetail order = it.next();
							if(order.getTotalRetailPrice() == maxTotalRetailPrice){
								double totalRetailPrice = order.getTotalRetailPrice();
								double discount = discountAll - totalDiscount;
								BigDecimal big = new BigDecimal(discount);
								big = big.setScale(0, BigDecimal.ROUND_FLOOR);
								discount = big.doubleValue();
								if(discount > order.getTotalRetailPrice())
									discount = order.getTotalRetailPrice();
								totalDiscount += discount;
								double totalPriceAfterDiscount = totalRetailPrice - discount;
								mTrans.discountEatchProduct(mTransactionId, order.getOrderDetailId(),
										order.getVatType(), mProduct.getVatRate(order.getProductId()), 
										totalPriceAfterDiscount, discount, PRICE_DISCOUNT_TYPE, 0, 
										OTHER_DISCOUNT_TYPE, "");
							}
						}
					}
					loadOrder();
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void clearDiscount(){
		mTrans.cancelDiscount(mTransactionId);
		mTrans.prepareDiscount(mTransactionId);
	}
	
	private void updateDiscount(double discount, int priceOrPercent) {
		discount = calculateDiscount(mOrder.getTotalRetailPrice(), discount, priceOrPercent);
		double totalPriceAfterDiscount = mOrder.getTotalRetailPrice() - discount;
		mTrans.discountEatchProduct(mTransactionId, 
				mOrder.getOrderDetailId(), mOrder.getVatType(),
				mProduct.getVatRate(mOrder.getProductId()), 
				totalPriceAfterDiscount, discount, priceOrPercent, 0, 
				OTHER_DISCOUNT_TYPE, "");
		
		MPOSOrderTransaction.MPOSOrderDetail order = mOrderLst.get(mPosition);
		order.setPriceDiscount(discount);
		order.setTotalSalePrice(totalPriceAfterDiscount);
		order.setPriceOrPercent(priceOrPercent);
		
		mOrderLst.set(mPosition, order);
		mDisAdapter.notifyDataSetChanged();
	}
	
	/**
	 * @param totalRetailPrice
	 * @param discount
	 * @param priceOrPercent
	 * @return 0 if not success
	 */
	public static double calculateDiscount(double totalRetailPrice, double discount, int priceOrPercent){
		if(discount < 0)
			return 0;
		
		double totalDiscount = discount;
		if(priceOrPercent == PRICE_DISCOUNT_TYPE){
			if(totalRetailPrice < discount)
				totalDiscount = 0;
		}else if(priceOrPercent == PERCENT_DISCOUNT_TYPE){
			if(discount > 100)
				totalDiscount = 0;
			else
				totalDiscount = totalRetailPrice * discount / 100;
		}
		BigDecimal big = new BigDecimal(totalDiscount);
		big = big.setScale(0, BigDecimal.ROUND_FLOOR);
		return big.doubleValue();
	}
	
	private void cancel(){
//		if (mIsEdited) {
//			new AlertDialog.Builder(this)
//					.setTitle(R.string.discount)
//					.setIcon(android.R.drawable.ic_dialog_info)
//					.setMessage(R.string.confirm_cancel)
//					.setNegativeButton(R.string.no,
//							new DialogInterface.OnClickListener() {
//
//								@Override
//								public void onClick(DialogInterface dialog,
//										int which) {
//								}
//							})
//					.setPositiveButton(R.string.yes,
//							new DialogInterface.OnClickListener() {
//
//								@Override
//								public void onClick(DialogInterface dialog,
//										int which) {
//									mTrans.cancelDiscount(mTransactionId);
//									finish();
//								}
//							}).show();
//		} else {
			finish();
//}	
	}
	
	private void summary() {
		MPOSOrderTransaction.MPOSOrderDetail summ = 
				mTrans.getSummaryOrderForDiscount(mTransactionId);
		TextView[] tvs = {
				SaleReportActivity.createTextViewSummary(this, getString(R.string.summary), Utils.getLinHorParams(1.2f)),
				SaleReportActivity.createTextViewSummary(this, mFormat.qtyFormat(summ.getQty()), Utils.getLinHorParams(0.5f)),
				SaleReportActivity.createTextViewSummary(this, mFormat.currencyFormat(summ.getPricePerUnit()), Utils.getLinHorParams(0.7f)),
				SaleReportActivity.createTextViewSummary(this, mFormat.currencyFormat(summ.getTotalRetailPrice()), Utils.getLinHorParams(0.7f)),
				SaleReportActivity.createTextViewSummary(this, mFormat.currencyFormat(summ.getPriceDiscount()), Utils.getLinHorParams(0.7f)),
				SaleReportActivity.createTextViewSummary(this, mFormat.currencyFormat(summ.getTotalSalePrice()), Utils.getLinHorParams(0.7f))
		};
		if(mSummaryContainer.getChildCount() > 0)
			mSummaryContainer.removeAllViews();

		LinearLayout rowSummary = SaleReportActivity.createRowSummary(this, tvs);
		rowSummary.setDividerDrawable(getResources().getDrawable(android.R.drawable.divider_horizontal_bright));
		rowSummary.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
		mSummaryContainer.addView(rowSummary);
	}
	
	private void loadOrder() {
		mOrderLst = mTrans.listAllOrderForDiscount(mTransactionId);
		mDisAdapter.notifyDataSetChanged();
	}
	
	/**
	 * @author j1tth4
	 * Discount list adapter
	 */
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
			LayoutInflater inflater = (LayoutInflater)
					getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = convertView;
			rowView = inflater.inflate(R.layout.discount_template, null, false);
			TextView tvNo = (TextView) rowView.findViewById(R.id.tvNo);
			TextView tvName = (TextView) rowView.findViewById(R.id.tvName);
			TextView tvQty = (TextView) rowView.findViewById(R.id.tvQty);
			TextView tvUnitPrice = (TextView) rowView.findViewById(R.id.tvPrice);
			TextView tvTotalPrice = (TextView) rowView.findViewById(R.id.tvTotalPrice);
			TextView tvDiscount = (TextView) rowView.findViewById(R.id.tvDiscount);
			final TextView tvSalePrice = (TextView) rowView.findViewById(R.id.tvSalePrice);

			final MPOSOrderTransaction.MPOSOrderDetail order =
					mOrderLst.get(position);
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

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		RadioButton rdo = (RadioButton) group.findViewById(checkedId);
		switch(checkedId){
		case R.id.rdoPrice:
			if(rdo.isChecked())
				mDisAllType = PRICE_DISCOUNT_TYPE;
			break;
		case R.id.rdoPercent:
			if(rdo.isChecked())
				mDisAllType = PERCENT_DISCOUNT_TYPE;
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		MPOSOrderTransaction.MPOSOrderDetail order = 
				(MPOSOrderTransaction.MPOSOrderDetail) parent.getItemAtPosition(position);
		
		Products p = new Products(this);
		if(p.isAllowDiscount(order.getProductId())){
			mPosition = position;
			mOrder = order;
			DiscountDialogFragment discount = 
					DiscountDialogFragment.newInstance(mOrder.getProductName(), 
							mOrder.getPriceDiscount(), mOrder.getTotalRetailPrice(), 
							mOrder.getPriceOrPercent());
			discount.show(getFragmentManager(), "DiscountDialog");
		}else{
			new AlertDialog.Builder(this)
			.setTitle(R.string.discount)
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

	@Override
	public void onClick(View v) {
		discountAll();
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if(actionId == EditorInfo.IME_ACTION_DONE){
			discountAll();
			return true;
		}
		return false;
	}
}
