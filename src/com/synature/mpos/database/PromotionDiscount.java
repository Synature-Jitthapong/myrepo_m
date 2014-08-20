package com.synature.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.synature.mpos.database.table.ProductTable;
import com.synature.mpos.database.table.PromotionPriceGroupTable;
import com.synature.mpos.database.table.PromotionProductDiscountTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class PromotionDiscount extends MPOSDatabase{

	public static final int PROMOTION_TYPE_COUPON = 4;
	public static final int PROMOTION_TYPE_VOUCHER = 5;
	
	public PromotionDiscount(Context context) {
		super(context);
	}

	/**
	 * List product discount
	 * @param priceGroupId
	 * @return List<com.synature.pos.PromotionProduct>
	 */
	public List<com.synature.pos.PromotionProduct> listPromotionProduct(int priceGroupId){
		List<com.synature.pos.PromotionProduct> productLst = 
				new ArrayList<com.synature.pos.PromotionProduct>();
		Cursor cursor = getReadableDatabase().query(PromotionProductDiscountTable.TABLE_PROMOTION_PRODUCT_DISCOUNT, 
				new String[]{
					ProductTable.COLUMN_PRODUCT_ID,
					PromotionProductDiscountTable.COLUMN_DISCOUNT_AMOUNT,
					PromotionProductDiscountTable.COLUMN_DISCOUNT_PERCENT,
					PromotionProductDiscountTable.COLUMN_AMOUNT_OR_PERCENT
				}, PromotionPriceGroupTable.COLUMN_PRICE_GROUP_ID + "=?", 
				new String[]{
					String.valueOf(priceGroupId)
				}, null, null, null);
		if(cursor.moveToFirst()){
			do{
				com.synature.pos.PromotionProduct product = new com.synature.pos.PromotionProduct();
				product.setProductID(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID)));
				product.setDiscountAmount(cursor.getDouble(cursor.getColumnIndex(PromotionProductDiscountTable.COLUMN_DISCOUNT_AMOUNT)));
				product.setDiscountPercent(cursor.getDouble(cursor.getColumnIndex(PromotionProductDiscountTable.COLUMN_DISCOUNT_PERCENT)));
				product.setAmountOrPercent(cursor.getInt(cursor.getColumnIndex(PromotionProductDiscountTable.COLUMN_AMOUNT_OR_PERCENT)));
				productLst.add(product);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return productLst;
	}
	
	/**
	 * List PromotionPriceGroup
	 * @return t<com.synature.pos.PromotionPrice>
	 */
	public List<com.synature.pos.PromotionPrice> listPromotionPriceGroup(){
		List<com.synature.pos.PromotionPrice> promoLst = 
				new ArrayList<com.synature.pos.PromotionPrice>();
		Cursor cursor = getReadableDatabase().query(PromotionPriceGroupTable.TABLE_PROMOTION_PRICE_GROUP, 
				new String[]{
				 	PromotionPriceGroupTable.COLUMN_PRICE_GROUP_ID,
				 	PromotionPriceGroupTable.COLUMN_PROMOTION_TYPE_ID,
				 	PromotionPriceGroupTable.COLUMN_PROMOTION_CODE,
				 	PromotionPriceGroupTable.COLUMN_PROMOTION_NAME,
				 	PromotionPriceGroupTable.COLUMN_BUTTON_NAME,
				 	PromotionPriceGroupTable.COLUMN_COUPON_HEADER,
				 	PromotionPriceGroupTable.COLUMN_PRICE_FROM_DATE,
				 	PromotionPriceGroupTable.COLUMN_PRICE_FROM_TIME,
				 	PromotionPriceGroupTable.COLUMN_PRICE_TO_DATE,
				 	PromotionPriceGroupTable.COLUMN_PRICE_TO_TIME,
				 	PromotionPriceGroupTable.COLUMN_PROMOTION_WEEKLY,
				 	PromotionPriceGroupTable.COLUMN_PROMOTION_MONTHLY,
				 	PromotionPriceGroupTable.COLUMN_IS_ALLOW_USE_OTHER_PROMOTION,
				 	PromotionPriceGroupTable.COLUMN_VOUCHER_AMOUNT,
				 	PromotionPriceGroupTable.COLUMN_OVER_PRICE,
				 	PromotionPriceGroupTable.COLUMN_PROMOTION_AMOUNT_TYPE
				},
				PromotionPriceGroupTable.COLUMN_PROMOTION_TYPE_ID + "=?", 
				new String[]{
					String.valueOf(PROMOTION_TYPE_COUPON)
				}, null, null, PromotionPriceGroupTable.COLUMN_PRICE_GROUP_ID);
		if(cursor.moveToFirst()){
			do{
				com.synature.pos.PromotionPrice promo = new com.synature.pos.PromotionPrice();
				promo.setPriceGroupID(cursor.getInt(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_PRICE_GROUP_ID)));
				promo.setPromotionTypeID(cursor.getInt(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_PROMOTION_TYPE_ID)));
				promo.setPromotionCode(cursor.getString(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_PROMOTION_CODE)));
				promo.setPromotionName(cursor.getString(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_PROMOTION_NAME)));
				promo.setButtonName(cursor.getString(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_BUTTON_NAME)));
				promo.setCouponHeader(cursor.getString(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_COUPON_HEADER)));
				promo.setPriceFromDate(cursor.getString(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_PRICE_FROM_DATE)));
				promo.setPriceFromTime(cursor.getString(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_PRICE_FROM_TIME)));
				promo.setPriceToDate(cursor.getString(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_PRICE_TO_DATE)));
				promo.setPriceToTime(cursor.getString(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_PRICE_TO_TIME)));
				promo.setPromotionWeekly(cursor.getString(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_PROMOTION_WEEKLY)));
				promo.setPromotionMonthly(cursor.getString(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_PROMOTION_MONTHLY)));
				promo.setIsAllowUseOtherPromotion(cursor.getInt(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_IS_ALLOW_USE_OTHER_PROMOTION)));
				promo.setVoucherAmount(cursor.getDouble(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_VOUCHER_AMOUNT)));
				promo.setOverPrice(cursor.getDouble(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_OVER_PRICE)));
				promo.setPromotionAmountType(cursor.getInt(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_PROMOTION_AMOUNT_TYPE)));
				promoLst.add(promo);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return promoLst;
	}
	
	/**
	 * Insert PromotionProductDiscount
	 * @param promoProductLst
	 */
	public void insertPromotionProductDiscount(List<com.synature.pos.PromotionProduct> promoProductLst){
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(PromotionProductDiscountTable.TABLE_PROMOTION_PRODUCT_DISCOUNT, null, null);
			for(com.synature.pos.PromotionProduct promoProduct : promoProductLst){
				ContentValues cv = new ContentValues();
				cv.put(PromotionPriceGroupTable.COLUMN_PRICE_GROUP_ID, promoProduct.getPriceGroupID());
				cv.put(ProductTable.COLUMN_PRODUCT_ID, promoProduct.getProductID());
				cv.put(ProductTable.COLUMN_SALE_MODE, promoProduct.getSaleMode());
				cv.put(PromotionProductDiscountTable.COLUMN_DISCOUNT_AMOUNT, promoProduct.getDiscountAmount());
				cv.put(PromotionProductDiscountTable.COLUMN_DISCOUNT_PERCENT, promoProduct.getDiscountPercent());
				cv.put(PromotionProductDiscountTable.COLUMN_AMOUNT_OR_PERCENT, promoProduct.getAmountOrPercent());
				getWritableDatabase().insert(PromotionProductDiscountTable.TABLE_PROMOTION_PRODUCT_DISCOUNT, null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally {
			getWritableDatabase().endTransaction();
		}
		
	}
	
	/**
	 * Insert PromotionPriceGroup
	 * @param promoLst
	 */
	public void insertPromotionPriceGroup(List<com.synature.pos.PromotionPrice> promoLst){
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(PromotionPriceGroupTable.TABLE_PROMOTION_PRICE_GROUP, null, null);
			for(com.synature.pos.PromotionPrice promoPrice : promoLst){
				ContentValues cv = new ContentValues();
				cv.put(PromotionPriceGroupTable.COLUMN_PRICE_GROUP_ID, promoPrice.getPriceGroupID());
				cv.put(PromotionPriceGroupTable.COLUMN_PROMOTION_TYPE_ID, promoPrice.getPromotionTypeID());
				cv.put(PromotionPriceGroupTable.COLUMN_PROMOTION_CODE, promoPrice.getPromotionCode());
				cv.put(PromotionPriceGroupTable.COLUMN_PROMOTION_NAME, promoPrice.getPromotionName());
				cv.put(PromotionPriceGroupTable.COLUMN_BUTTON_NAME, promoPrice.getButtonName());
				cv.put(PromotionPriceGroupTable.COLUMN_COUPON_HEADER, promoPrice.getCouponHeader());
				cv.put(PromotionPriceGroupTable.COLUMN_PRICE_FROM_DATE, promoPrice.getPriceFromDate());
				cv.put(PromotionPriceGroupTable.COLUMN_PRICE_FROM_TIME, promoPrice.getPriceFromTime());
				cv.put(PromotionPriceGroupTable.COLUMN_PRICE_TO_DATE, promoPrice.getPriceToDate());
				cv.put(PromotionPriceGroupTable.COLUMN_PRICE_TO_TIME, promoPrice.getPriceToTime());
				cv.put(PromotionPriceGroupTable.COLUMN_PROMOTION_WEEKLY, promoPrice.getPromotionWeekly());
				cv.put(PromotionPriceGroupTable.COLUMN_PROMOTION_MONTHLY, promoPrice.getPromotionMonthly());
				cv.put(PromotionPriceGroupTable.COLUMN_IS_ALLOW_USE_OTHER_PROMOTION, promoPrice.getIsAllowUseOtherPromotion());
				cv.put(PromotionPriceGroupTable.COLUMN_VOUCHER_AMOUNT, promoPrice.getVoucherAmount());
				cv.put(PromotionPriceGroupTable.COLUMN_OVER_PRICE, promoPrice.getOverPrice());
				cv.put(PromotionPriceGroupTable.COLUMN_PROMOTION_AMOUNT_TYPE, promoPrice.getPromotionAmountType());
				getWritableDatabase().insert(PromotionPriceGroupTable.TABLE_PROMOTION_PRICE_GROUP, null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally{
			getWritableDatabase().endTransaction();
		}
	}
}
