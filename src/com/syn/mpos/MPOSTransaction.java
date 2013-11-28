package com.syn.mpos;

import java.util.List;

import com.syn.mpos.database.inventory.SaleStock;
import com.syn.mpos.database.transaction.Transaction;
import com.syn.pos.OrderTransaction;

import android.content.Context;

public class MPOSTransaction {
	public static int mShopId;
	public static int mStaffId;
	public static int mSessionId;
	public static int mTransactionId;
	public static int mComputerId;
	
	public static String mErr;
	private Context mContext;
	private Transaction mTrans;
	
	public MPOSTransaction(Context c, int shopId, int computerId){
		mContext = c;
		mShopId = shopId;
		mComputerId = computerId;
	}
	
	public MPOSTransaction(Context c, int shopId, int computerId, int sessionId){
		mContext = c;
		mShopId = shopId;
		mComputerId = computerId;
		mSessionId = sessionId;
		
		mTrans = new Transaction(c);
		openTransaction();
	}
	
	public float getMemberDiscount(boolean tempTable){
		return mTrans.getMemberDiscount(mTransactionId, mComputerId, tempTable);
	}
	
	public float getPriceDiscount(boolean tempTable){
		return mTrans.getPriceDiscount(mTransactionId, mComputerId, tempTable);
	}
	
	public float getTotalVatExclude(boolean tempTable){
		return mTrans.getTotalVatExclude(mTransactionId, mComputerId, tempTable);
	}
	
	public float getTotalVatIncluded(boolean tempTable){
		return mTrans.getTotalVatIncluded(mTransactionId, mComputerId, tempTable);
	}
	
	public float getTotalRetailPrice(boolean tempTable){
		return mTrans.getTotalRetailPrice(mTransactionId, mComputerId, tempTable);
	}
	
	public float getTotalSalePrice(boolean tempTable){
		return mTrans.getTotalSalePrice(mTransactionId, mComputerId, tempTable);
	}
	
	public List<OrderTransaction> listTransaction(long saleDate){
		return mTrans.listTransaction(saleDate);
	}
	
	public List<OrderTransaction> listHoldOrder(){
		return mTrans.listHoldOrder(mComputerId);
	}
	
	public List<OrderTransaction.OrderDetail> listOrderTmp(){
		return mTrans.listAllOrderTmp(mTransactionId, mComputerId);
	}
	
	public List<OrderTransaction.OrderDetail> listOrder(){
		return mTrans.listAllOrder(mTransactionId, mComputerId);
	}
	
	public OrderTransaction.OrderDetail getOrder(int orderDetailId){
		return mTrans.getOrder(mTransactionId, mComputerId, orderDetailId);
	}
	
	public boolean cancelDiscount(){
		return mTrans.cancelDiscount(mTransactionId, mComputerId);
	}
	
	public boolean confirmDiscount(){
		return mTrans.confirmDiscount(mTransactionId, mComputerId);
	}
	
	public boolean discountEatchProduct(int orderDetailId, float vatRate, 
			float salePrice, float discount){
		return mTrans.discountEatchProduct(orderDetailId, mTransactionId, 
				mComputerId, vatRate, salePrice, discount);
	}
	
	public boolean copyOrderToTmp(){
		return mTrans.copyOrderToTmp(mTransactionId, mComputerId);
	}
	
	public boolean updateOrder(int orderDetailId, float vatRate, float orderQty, float price){
		return mTrans.updateOrderDetail(mTransactionId, mComputerId, 
				orderDetailId, vatRate, orderQty, price);
	}
	
	public boolean deleteOrder(int orderDetailId){
		return mTrans.deleteOrderDetail(mTransactionId, mComputerId, orderDetailId);
	}
	
	public int addOrder(int productId, int productType, 
			int vatType, float vatRate, float orderQty, float price){
		return mTrans.addOrderDetail(mTransactionId, mComputerId, productId, 
				productType, vatType, vatRate, orderQty, price);
	}
	
	public void openTransaction(){
		mTransactionId = mTrans.getCurrTransaction(mComputerId);
		if(mTransactionId == 0){
			mTransactionId = mTrans.openTransaction(mComputerId, 
					mShopId, mSessionId, mStaffId);
		}
	}
	
	public int countHoldOrder(){
		return mTrans.countHoldOrder(mComputerId);
	}
	
	public boolean clearTransaction(){
		boolean isCleared = 
				mTrans.deleteTransaction(mTransactionId, mComputerId);
		if(isCleared){
			// do clear order
			mTrans.deleteOrderDetail(mTransactionId, mComputerId);
			
			// do clear payment
			
			openTransaction();
		}
		return isCleared;
	}
	
	public OrderTransaction getTransaction(){
		return mTrans.getTransaction(mTransactionId, mComputerId);
	}
	
	public boolean successTransaction(){
		boolean isSuccess = 
				mTrans.successTransaction(mTransactionId, mComputerId, mStaffId);
		
		if(isSuccess){
			SaleStock stock = new SaleStock(mContext);
			isSuccess = stock.createSaleDocument(mShopId, mStaffId, 
					mTrans.listAllOrder(mTransactionId, mComputerId));
			
			if(isSuccess)
				openTransaction();
			else
				mErr = "Cannot create sale document.";
		}else{
			mErr = "Cannot success transaction.";
		}
		return isSuccess;
	}
	
	public boolean prepareTransaction(){
		return mTrans.prepareTransaction(mTransactionId, mComputerId);
	}
	
	public boolean holdTransaction(String note){
		boolean isHolded = 
				mTrans.holdTransaction(mTransactionId, mComputerId, note);
		if(isHolded){
			openTransaction();
		}else{
			mErr = "Cannot hold transaction.";
		}
		return isHolded;
	}
	
	public boolean voidTransaction(String reason){
		boolean isVoided = 
				mTrans.voidTransaction(mTransactionId, mComputerId, mStaffId, reason);
		if(isVoided){
			SaleStock stock = new SaleStock(mContext);
			isVoided = stock.createVoidDocument(mShopId, mStaffId, 
					mTrans.listAllOrder(mTransactionId, mComputerId), reason);
			if(isVoided)
				openTransaction();
			else
				mErr = "Cannot create void document.";
		}else{
			mErr = "Cannot void transaction.";
		}
		return isVoided;
	}
}
