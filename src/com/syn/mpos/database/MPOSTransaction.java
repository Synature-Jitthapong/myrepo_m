package com.syn.mpos.database;

import java.util.List;

import com.syn.pos.OrderTransaction;
import com.syn.pos.Payment.PaymentDetail;

import android.content.Context;

/**
 * @author j1tth4
 * Manage transaction, order, payment...
 */
public class MPOSTransaction {
	
	private Context mContext;
	/**
	 * Session data source
	 */
	private SessionDataSource mSession;
	/**
	 * Transaction data source
	 */
	private OrderTransactionDataSource mTransaction;
	/**
	 * Payment data source
	 */
	private PaymentDetailDataSource mPayment;
	
	private int mTransactionId;
	private int mSessionId;
	
	public MPOSTransaction(Context context){
		mContext = context;
		mTransaction = new OrderTransactionDataSource(context.getApplicationContext());
		mSession = new SessionDataSource(context.getApplicationContext());
		mPayment = new PaymentDetailDataSource(context.getApplicationContext());
	}
	
	/**
	 * change transaction status to STATUS_NEW
	 */
	public void prepareTransaction(){
		mTransaction.prepareTransaction(mTransactionId);
	}
	
	/**
	 * close transaction
	 */
	public void closeTransaction(int staffId){
		mTransaction.successTransaction(mTransactionId, staffId);
	}
	
	/**
	 * open transaction
	 */
	public void openTransaction(int sessionId, int staffId){
		SessionDataSource session = new SessionDataSource(mContext);
		String saleDate = session.getSessionDate(sessionId);
		if(getCurrentTransactionId(saleDate) == 0){
			MPOSShop ms = new MPOSShop(mContext);
			mTransactionId = mTransaction.openTransaction(ms.getShopId(), 
					ms.getComputerId(), sessionId, staffId, ms.getCompanyVatRate());
			
			SyncSaleLogDataSource syncLog = new SyncSaleLogDataSource(mContext);
			syncLog.addSyncSaleLog(saleDate);
		}
	}

	/**
	 * @return List<MPOSOrderTransaction.OrderDetail>
	 */
	public List<MPOSOrderTransaction.OrderDetail> listAllOrder(){
		return mTransaction.listAllOrder(mTransactionId);
	}
	
	/**
	 * @param saleDate
	 * @return List<MPOSOrderTransaction>
	 */
	public List<MPOSOrderTransaction> listHoldOrder(String saleDate){
		return mTransaction.listHoldOrder(saleDate);
	}
	
	/**
	 * @return total transaction not send
	 */
	public int countTransNotSend(){
		return mTransaction.countTransNotSend();
	}
	
	/**
	 * @return total hold order
	 */
	public int countHoldOrder(){
		return mTransaction.countHoldOrder(getCurrentSessionDate());
	}
	
	/**
	 * @param note
	 */
	public void holdOrder(String note){
		mTransaction.holdTransaction(mTransactionId, note);
	}
	
	/**
	 * @param orderDetailId
	 */
	public void deleteOrder(int orderDetailId){
		mTransaction.deleteOrderDetail(mTransactionId, orderDetailId);
	}
	
	/**
	 * @param orderDetailId
	 * @param vatType
	 * @param vatRate
	 * @param orderQty
	 * @param pricePerUnit
	 */
	public void updateOrder(int orderDetailId, int vatType, double vatRate,
			double orderQty, double pricePerUnit){
		mTransaction.updateOrderDetail(mTransactionId, 
				orderDetailId, vatType, vatRate, orderQty, pricePerUnit);
	}
	
	/**
	 * @param computerId
	 * @param productId
	 * @param productType
	 * @param vatType
	 * @param vatRate
	 * @param orderQty
	 * @param pricePerUnit
	 */
	public void addOrder(int computerId, int productId, int productType, int vatType,
			double vatRate, double orderQty, double pricePerUnit){
		mTransaction.addOrderDetail(mTransactionId, computerId, 
				productId, productType, vatType, vatRate, orderQty, pricePerUnit);
	}
	
	/**
	 * clear current transaction
	 */
	public void clearTransaction(){
		mTransaction.deleteOrderDetail(mTransactionId);
		mTransaction.deleteTransaction(mTransactionId);
		mPayment.deleteAllPaymentDetail(mTransactionId);
	}
	
	/**
	 * Update field OpenStaffId
	 * @param staffId
	 */
	public void updateTransaction(int staffId){
		mTransaction.updateTransaction(mTransactionId, staffId);
	}
	
	/**
	 * Update transaction vat
	 */
	public void updateTransactionVat(){
		mTransaction.updateTransactionVat(mTransactionId);
	}
	
	/**
	 * @return receipt number
	 */
	public String getReceiptNo(){
		return mTransaction.getReceiptNo(mTransactionId);
	}
	
	/**
	 * @return List<PaymentDetail>
	 */
	public List<PaymentDetail> listPaymentDetail(){
		return mPayment.listPaymentGroupByType(mTransactionId);
	}
	
	/**
	 * @return total order qty
	 */
	public int getOrderQty(){
		return mTransaction.getTotalOrderQty(mTransactionId);
	}
	
	/**
	 * @return total paid
	 */
	public double getTotalPaid(){
		return mPayment.getTotalPaid(mTransactionId);
	}
	
	/**
	 * @return subTotal
	 */
	public double getSubTotalPrice(){
		return mTransaction.getTotalRetailPrice(mTransactionId);	
	}
	
	/**
	 * @return totalVatExclude
	 */
	public double getTotalVatExclude(){
		return mTransaction.getTotalVatExclude(mTransactionId);
	}
	
	/**
	 * @return totalVat
	 */
	public double getTotalVat(){
		return mTransaction.getTotalVat(mTransactionId);
	}
	
	/**
	 * @return totalPriceDiscount
	 */
	public double getTotalPriceDiscount(){
		return mTransaction.getTotalPriceDiscount(mTransactionId);
	}
	
	/**
	 * @return totalSalePrice
	 */
	public double getTotalSalePrice(){
		return mTransaction.getTotalSalePrice(mTransactionId);
	}
	
	/**
	 * @return vatable
	 */
	public double getTransactionVatable(){
		return mTransaction.getTransactionVatable(mTransactionId);
	}
	
	/**
	 * @return transactionVat
	 */
	public double getTransactionVat(){
		return mTransaction.getTransactionVat(mTransactionId);
	}
	
	/**
	 * @return open staffId
	 */
	public int getOpenTransactionStaffId(){
		return getTransaction().getOpenStaffId();
	}
	
	/**
	 * @return MPOSOrderTransaction object
	 */
	public MPOSOrderTransaction getTransaction(){
		return mTransaction.getTransaction(mTransactionId);
	}
	/**
	 * @param saleDate
	 * @return current transactionId
	 */
	public int getCurrentTransactionId(String saleDate){
		return mTransaction.getCurrTransaction(saleDate);
	}

	/**
	 * @param shopId
	 * @param computerId
	 * @param openStaffId
	 * @param openAmount
	 * @return sessionId
	 */
	public void openSession(int shopId, int computerId, 
			int openStaffId, double openAmount){
		if(getCurrentSession(openStaffId) == 0){
			mSessionId = mSession.addSession(shopId, computerId, 
					openStaffId, openAmount);
		}
	}
	
	/**
	 * @param sessionId
	 * @return session date
	 */
	public String getCurrentSessionDate(){
		return mSession.getSessionDate(mSessionId);
	}
	
	/**
	 * @return session date string long millisecond pattern
	 */
	public String getLastSessionDate(){
		return mSession.getSessionDate();
	}
	
	/**
	 * @return sessionId
	 * 0 if not have session
	 */
	public int getCurrentSession(int staffId){
		return mSession.getCurrentSession(staffId);
	}
	
	public int getTransactionId() {
		return mTransactionId;
	}

	public void setTransactionId(int mTransactionId) {
		this.mTransactionId = mTransactionId;
	}
}
