package com.syn.mpos.database;

import java.util.List;

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
	private int mComputerId;
	private int mSessionId;
	
	private double mTotalVatExcluded;
	private double mTotalVat;
	private double mTotalSalePrice;
	private double mTotalPriceVatable;
	private double mTotalDiscount;
	private double mSubTotalPrice;
	
	public MPOSTransaction(Context context, int computerId){
		mContext = context;
		mComputerId = computerId;
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
		if(getCurrentTransaction(saleDate) == 0){
			MPOSShop ms = new MPOSShop(mContext);
			mTransactionId = mTransaction.openTransaction(ms.getShopId(), 
					ms.getComputerId(), sessionId, staffId, ms.getCompanyVatRate());
			
			SyncSaleLogDataSource syncLog = new SyncSaleLogDataSource(mContext);
			syncLog.addSyncSaleLog(saleDate);
		}
	}
	
	/**
	 * summary
	 */
	public void summary(){
		mSubTotalPrice = mTransaction.getTotalRetailPrice(mTransactionId);
		mTotalVatExcluded = mTransaction.getTotalVatExclude(mTransactionId);
		mTotalVat = mTransaction.getTotalVat(mTransactionId);
		mTotalDiscount = mTransaction.getTotalPriceDiscount(mTransactionId);
		mTotalSalePrice = mTransaction.getTotalSalePrice(mTransactionId);
		mTotalPriceVatable = mTransaction.getTransactionVatable(mTransactionId);
		mTransaction.updateTransactionVat(mTransactionId);
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
	 * @param productId
	 * @param productType
	 * @param vatType
	 * @param vatRate
	 * @param orderQty
	 * @param pricePerUnit
	 */
	public void addOrder(int productId, int productType, int vatType,
			double vatRate, double orderQty, double pricePerUnit){
		mTransaction.addOrderDetail(mTransactionId, mComputerId, 
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
	 * @param saleDate
	 * @return current transactionId
	 */
	public int getCurrentTransaction(String saleDate){
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

	public double getTotalDiscount() {
		return mTotalDiscount;
	}

	public void setTotalDiscount(double mTotalDiscount) {
		this.mTotalDiscount = mTotalDiscount;
	}

	public double getTotalSalePrice() {
		return mTotalSalePrice;
	}

	public void setTotalSalePrice(double mTotalSalePrice) {
		this.mTotalSalePrice = mTotalSalePrice;
	}

	public double getTotalVatExcluded() {
		return mTotalVatExcluded;
	}

	public void setTotalVatExcluded(double mTotalVatExcluded) {
		this.mTotalVatExcluded = mTotalVatExcluded;
	}

	public double getTotalVat() {
		return mTotalVat;
	}

	public void setTotalVat(double mTotalVat) {
		this.mTotalVat = mTotalVat;
	}

	public double getTotalPriceVatable() {
		return mTotalPriceVatable;
	}

	public void setTotalPriceVatable(double mTotalPriceVatable) {
		this.mTotalPriceVatable = mTotalPriceVatable;
	}

	public double getSubTotalPrice() {
		return mSubTotalPrice;
	}

	public void setSubTotalPrice(double mSubTotalPrice) {
		this.mSubTotalPrice = mSubTotalPrice;
	}
}
