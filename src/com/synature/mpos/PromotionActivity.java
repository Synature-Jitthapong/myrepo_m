package com.synature.mpos;

import java.util.List;

import com.synature.mpos.database.Formater;
import com.synature.mpos.database.MPOSOrderTransaction.MPOSOrderDetail;
import com.synature.mpos.database.Products;
import com.synature.mpos.database.PromotionDiscount;
import com.synature.mpos.database.Transaction;
import com.synature.pos.ProductGroups;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class PromotionActivity extends Activity {

	public static final String TAG = PromotionActivity.class.getSimpleName();
	
	private Transaction mTrans;
	private PromotionDiscount mPromotion;
	private Formater mFormat;
	private List<MPOSOrderDetail> mOrderLst;
	private List<ProductGroups.PromotionPrice> mPromoLst;
	private OrderDiscountAdapter mOrderAdapter;
	
	private int mTransactionId;
	
	private EditText mTxtTotalPrice;
	private LinearLayout mPromoButtonContainer;
	private LinearLayout mSummaryContainer;
	private ListView mLvOrderDiscount; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_promotion);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		mPromoButtonContainer = (LinearLayout) findViewById(R.id.promoButtonContainer);
		mLvOrderDiscount = (ListView) findViewById(R.id.lvOrderDiscount);
		mTxtTotalPrice = (EditText) findViewById(R.id.txtTotalPrice);
		mSummaryContainer = (LinearLayout) findViewById(R.id.summaryContainer);
		
		mTrans = new Transaction(this);
		mFormat = new Formater(this);
		mPromotion = new PromotionDiscount(this);
		
		Intent intent = getIntent();
		mTransactionId = intent.getIntExtra("transactionId", 0);
		
		mTrans.prepareDiscount(mTransactionId);
		loadOrderDetail();
		setupPromotionButton();
		summary();
	}

	private void setupPromotionButton(){
		mPromoLst = mPromotion.listPromotionPriceGroup();
		for(ProductGroups.PromotionPrice promo : mPromoLst){
			LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
			Button btn = (Button) inflater.inflate(R.layout.button_template, null);
			btn.setId(promo.getPriceGroupID());
			btn.setText(promo.getPromotionName().equals("") ? promo.getButtonName() : promo.getPromotionName());
			btn.setMinWidth(128);
			btn.setMinHeight(64);
			btn.setOnClickListener(new OnPromotionButtonClickListener(promo.getPriceGroupID(), 
					promo.getPromotionTypeID(), promo.getCouponHeader(), promo.getPromotionName()));
			mPromoButtonContainer.addView(btn, getHorizontalParams());
		}

		try {
			for(MPOSOrderDetail detail : mOrderLst){
				for(int i = 0; i < mPromoButtonContainer.getChildCount(); i++){
					View child = mPromoButtonContainer.getChildAt(i);
					if(detail.getPromotionPriceGroupId() == child.getId()){
						child.setSelected(true);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadOrderDetail(){
		mOrderLst = mTrans.listAllOrderForDiscount(mTransactionId);
		if(mOrderAdapter == null){
			mOrderAdapter = new OrderDiscountAdapter();
			mLvOrderDiscount.setAdapter(mOrderAdapter);
		}else{
			mOrderAdapter.notifyDataSetChanged();
		}
	}
	
	private void confirmDiscount(){
		mTrans.confirmDiscount(mTransactionId);
	}
	
	private void clearDiscount(){
		new AlertDialog.Builder(this)
		.setTitle(R.string.discount)
		.setMessage(R.string.confirm_clear_discount)
		.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		})
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// reset discount
				Products p = new Products(PromotionActivity.this);
				for(MPOSOrderDetail detail : mOrderLst){
					double totalRetailPrice = detail.getTotalRetailPrice();
					double discount = DiscountActivity.calculateDiscount(totalRetailPrice, 
							0, DiscountActivity.PRICE_DISCOUNT_TYPE);
					double priceAfterDiscount = totalRetailPrice - discount;
					
					mTrans.discountEatchProduct(mTransactionId, detail.getOrderDetailId(), 
							p.getVatType(detail.getProductId()), p.getVatRate(detail.getProductId()), 
							priceAfterDiscount, discount, DiscountActivity.PRICE_DISCOUNT_TYPE, 
							0, 0, "", "");
				}
				loadOrderDetail();
				summary();
				for(int i = 0; i < mPromoLst.size(); i++){
					View child = mPromoButtonContainer.getChildAt(i);
					if(child.isSelected())
						child.setSelected(false);
				}
			}
		}).show();
	}
	
	private void summary(){
		MPOSOrderDetail summ = mTrans.getSummaryOrderForDiscount(mTransactionId);
		mTxtTotalPrice.setText(mFormat.currencyFormat(summ.getTotalSalePrice()));

		if(mSummaryContainer.getChildCount() > 0)
			mSummaryContainer.removeAllViews();
		TextView[] tvs = {
				SaleReportActivity.createTextViewSummary(this, getString(R.string.summary), Utils.getLinHorParams(1.2f)),
				SaleReportActivity.createTextViewSummary(this, mFormat.qtyFormat(summ.getQty()), Utils.getLinHorParams(0.5f)),
				SaleReportActivity.createTextViewSummary(this, mFormat.currencyFormat(summ.getPricePerUnit()), Utils.getLinHorParams(0.7f)),
				SaleReportActivity.createTextViewSummary(this, mFormat.currencyFormat(summ.getTotalRetailPrice()), Utils.getLinHorParams(0.7f)),
				SaleReportActivity.createTextViewSummary(this, mFormat.currencyFormat(summ.getPriceDiscount()), Utils.getLinHorParams(0.7f)),
				SaleReportActivity.createTextViewSummary(this, mFormat.currencyFormat(summ.getTotalSalePrice()), Utils.getLinHorParams(0.7f))
		};
		LinearLayout rowSummary = SaleReportActivity.createRowSummary(this, tvs);
		rowSummary.setDividerDrawable(getResources().getDrawable(android.R.drawable.divider_horizontal_bright));
		rowSummary.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
		mSummaryContainer.addView(rowSummary);
	}
	
	private LinearLayout.LayoutParams getHorizontalParams(){
		return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
	}
	
	private class OnPromotionButtonClickListener implements OnClickListener{

		private int mPriceGroupId;
		private int mPromotionTypeId;
		private String mCouponHeader;
		private String mPromotionName;
		
		public OnPromotionButtonClickListener(int priceGroupId, int promotionTypeId, 
				String couponHeader, String promotionName){
			mPriceGroupId = priceGroupId;
			mPromotionTypeId = promotionTypeId;
			mCouponHeader = couponHeader;
			mPromotionName = promotionName;
		}
		
		@Override
		public void onClick(View view) {
			if(view.isSelected()){
				clearDiscount();
			}else{
				List<ProductGroups.PromotionProduct> productLst = 
						mPromotion.listPromotionProduct(mPriceGroupId);
				if(productLst.size() > 0){
					if(discount(productLst)){
						view.setSelected(true);
						for(int i = 0; i < mPromoLst.size(); i++){
							View child = mPromoButtonContainer.getChildAt(i);
							if(child.getId() != view.getId()){
								if(child.isSelected())
									child.setSelected(false);
							}
						}
					}
				}
			}
		}
		
		/**
		 * @param productLst
		 * @return true if can discount false is not
		 */
		private boolean discount(List<ProductGroups.PromotionProduct> productLst){
			boolean canDiscount = false;
			Products p = new Products(PromotionActivity.this);
			for(MPOSOrderDetail detail : mOrderLst){
				for(ProductGroups.PromotionProduct product : productLst){
					if(detail.getProductId() == product.getProductID()){
						if(p.isAllowDiscount(product.getProductID())){
							canDiscount = true;
							int orderDetailId = detail.getOrderDetailId();
							int vatType = p.getVatType(product.getProductID());
							double vatRate = p.getVatRate(product.getProductID());
							double totalRetailPrice = detail.getTotalRetailPrice();
							double discount = 0.0d;
							double priceAfterDiscount = 0.0d;
							double discountAmount = 0.0d;
							if(product.getDiscountAmount() > 0){
								// discount amount
								discountAmount = product.getDiscountAmount() * detail.getQty();
								discount = DiscountActivity.calculateDiscount(totalRetailPrice, 
										discountAmount, DiscountActivity.PRICE_DISCOUNT_TYPE);
								priceAfterDiscount = totalRetailPrice - discount;
								
								mTrans.discountEatchProduct(mTransactionId, orderDetailId, vatType, vatRate, 
										priceAfterDiscount, discount, DiscountActivity.PRICE_DISCOUNT_TYPE, 
										mPriceGroupId, mPromotionTypeId, mCouponHeader, mPromotionName);
							}else if(product.getDiscountPercent() > 0){
								// discount percent
								discount = DiscountActivity.calculateDiscount(totalRetailPrice, 
										product.getDiscountPercent(), DiscountActivity.PERCENT_DISCOUNT_TYPE);
								priceAfterDiscount = totalRetailPrice - discount;
								
								mTrans.discountEatchProduct(mTransactionId, orderDetailId, vatType, vatRate, 
										priceAfterDiscount, discount, DiscountActivity.PERCENT_DISCOUNT_TYPE, 
										mPriceGroupId, mPromotionTypeId, mCouponHeader, mPromotionName);
							}
						}else{
							Log.d(TAG, "ProductID:" + detail.getProductId() + " Not allow discount");
						}
					}
				}
			}
			summary();
			loadOrderDetail();
			return canDiscount;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.promotion, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		case R.id.itemConfirm:
			confirmDiscount();
			finish();
			return true;
		default:	
		return super.onOptionsItemSelected(item);
		}
	}
	
	private class OrderDiscountAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mOrderLst.size();
		}

		@Override
		public Object getItem(int position) {
			return mOrderLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			DiscountViewHolder holder;
			if(convertView == null){
				LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
				convertView = inflater.inflate(R.layout.discount_template, parent, false);
				holder = new DiscountViewHolder();
				holder.tvNo = (TextView) convertView.findViewById(R.id.tvNo);
				holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
				holder.tvQty = (TextView) convertView.findViewById(R.id.tvQty);
				holder.tvUnitPrice = (TextView) convertView.findViewById(R.id.tvPrice);
				holder.tvTotalPrice = (TextView) convertView.findViewById(R.id.tvTotalPrice);
				holder.tvDiscount = (TextView) convertView.findViewById(R.id.tvDiscount);
				holder.tvSalePrice = (TextView) convertView.findViewById(R.id.tvSalePrice);
				convertView.setTag(holder);
			}else{
				holder = (DiscountViewHolder) convertView.getTag();
			}
			MPOSOrderDetail detail = mOrderLst.get(position);
			holder.tvNo.setText(Integer.toString(position + 1) + ".");
			holder.tvName.setText(detail.getProductName());
			holder.tvQty.setText(mFormat.qtyFormat(detail.getQty()));
			holder.tvUnitPrice.setText(mFormat.currencyFormat(detail.getPricePerUnit()));
			holder.tvTotalPrice.setText(mFormat.currencyFormat(detail.getTotalRetailPrice()));
			holder.tvDiscount.setText(mFormat.currencyFormat(detail.getPriceDiscount()));
			holder.tvSalePrice.setText(mFormat.currencyFormat(detail.getTotalSalePrice()));
			return convertView;
		}
		
	}
	
	public static class DiscountViewHolder{
		TextView tvNo;
		TextView tvName;
		TextView tvQty;
		TextView tvUnitPrice;
		TextView tvTotalPrice;
		TextView tvDiscount;
		TextView tvSalePrice;
	}
}
