package com.syn.mpos.database;

import java.util.List;

import com.syn.pos.OrderTransaction.OrderDetail;
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
	
	public MPOSTransaction(Context context){
		mContext = context;
		mTransaction = new OrderTransactionDataSource(context.getApplicationContext());
		mSession = new SessionDataSource(context.getApplicationContext());
		mPayment = new PaymentDetailDataSource(context.getApplicationContext());
	}
	
	/**
	 * @param orderDetailId
	 */
	public void deleteOrder(int orderDetailId){
		mTransaction.deleteOrderDetail(getCurrentTransactionId(), orderDetailId);
	}

	/**
	 * @param payTypeId
	 */
	public void deletePayment(int payTypeId){
		mPayment.deletePaymentDetail(payTypeId);
	}

	/**
	 * delete all payment of current transactionId
	 */
	public void deleteAllPayment(){
		mPayment.deleteAllPaymentDetail(getCurrentTransactionId());
	}

	/**
	 * clear current transaction
	 */
	public void clearTransaction(){
		int transactionId = getCurrentTransactionId();
		mTransaction.deleteOrderDetail(transactionId);
		mTransaction.deleteTransaction(transactionId);
		mPayment.deleteAllPaymentDetail(transactionId);
	}

	/**
	 * update field send status
	 * @param saleDate
	 */
	public void updateTransactionSendStatus(String saleDate){
		mTransaction.updateTransactionSendStatus(saleDate);
	}
	
	/**
	 * update field send status
	 */
	public void updateTransactionSendStatus(){
		mTransaction.updateTransactionSendStatus(getCurrentTransactionId());
	}
	
	/**
	 * Update field OpenStaffId
	 * @param staffId
	 */
	public void updateTransaction(int staffId){
		mTransaction.updateTransaction(getCurrentTransactionId(), staffId);
	}
	
	/**
	 * Update transaction vat
	 */
	public void updateTransactionVat(){
		mTransaction.updateTransactionVat(getCurrentTransactionId());
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
		mTransaction.updateOrderDetail(getCurrentTransactionId(), 
				orderDetailId, vatType, vatRate, orderQty, pricePerUnit);
	}

	/**
	 * @param note
	 */
	public void holdOrder(String note){
		mTransaction.holdTransaction(getCurrentTransactionId(), note);
	}

	/**
	 * Confirm discount
	 */
	public void confirmDiscount(){
		mTransaction.confirmDiscount(getCurrentTransactionId());
	}
	
	/**
	 * @param orderDetailId
	 * @param vatType
	 * @param vatRate
	 * @param salePrice
	 * @param discount
	 * @param discountType
	 */
	public void discountEatchProduct(int orderDetailId, int vatType, double vatRate,
			double salePrice, double discount, int discountType){
		mTransaction.discountEatchProduct(orderDetailId, 
				getCurrentTransactionId(), vatType, vatRate, salePrice, discount, discountType);
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
		mTransaction.addOrderDetail(getCurrentTransactionId(), computerId, 
				productId, productType, vatType, vatRate, orderQty, pricePerUnit);
	}

	/**
	 * @param computerId
	 * @param payTypeId
	 * @param paid
	 * @param pay
	 * @param cardNo
	 * @param expMonth
	 * @param expYear
	 * @param bankId
	 * @param cardTypeId
	 * @param remark
	 */
	public void addPayment(int computerId, int payTypeId, double paid, double pay, 
			String cardNo, int expMonth, int expYear, int bankId, int cardTypeId, String remark){
		mPayment.addPaymentDetail(getCurrentTransactionId(), computerId, payTypeId, 
				paid, pay, cardNo, expMonth, expYear, bankId, cardTypeId, remark);
	}

	/**
	 * @param saleDate
	 * @return List<MPOSOrderTransaction>
	 */
	public List<MPOSOrderTransaction> listHoldOrder(String saleDate){
		return mTransaction.listHoldOrder(saleDate);
	}

	/**
	 * @return List<MPOSOrderTransaction.OrderDetail>
	 */
	public List<MPOSOrderTransaction.OrderDetail> listOrderForDiscunt(){
		return mTransaction.listAllOrderTmp(getCurrentTransactionId());
	}
	
	/**
	 * @return List<MPOSOrderTransaction.OrderDetail>
	 */
	public List<MPOSOrderTransaction.OrderDetail> listAllOrder(){
		return mTransaction.listAllOrder(getCurrentTransactionId());
	}

	/**
	 * @return List<PaymentDetail>
	 */
	public List<PaymentDetail> listPayment(){
		return mPayment.listPayment(getCurrentTransactionId());
	}
	
	/**
	 * @return List<PaymentDetail>
	 */
	public List<PaymentDetail> listPaymentDetail(){
		return mPayment.listPaymentGroupByType(getCurrentTransactionId());
	}
	
	/**
	 * Prepare order for discount
	 */
	public void prepareDiscount(){
		mTransaction.copyOrderToTmp(getCurrentTransactionId());
	}
	
	/**
	 * change transaction status to STATUS_NEW
	 */
	public void prepareTransaction(){
		mTransaction.prepareTransaction(getCurrentTransactionId());
	}

	/**
	 * close transaction
	 */
	public void closeTransaction(int staffId){
		mTransaction.successTransaction(getCurrentTransactionId(), staffId);
	}

	/**
	 * @param staffId
	 * @return current transactionId
	 */
	public int openTransaction(int staffId){
		int transactionId = 0;
		if(getCurrentTransactionId() == 0){
			MPOSShop ms = new MPOSShop(mContext);
			int sessionId = getCurrentSessionId(staffId);
			transactionId = mTransaction.openTransaction(ms.getShopId(), 
					ms.getComputerId(), sessionId, staffId, ms.getCompanyVatRate());
			
			SyncSaleLogDataSource syncLog = new SyncSaleLogDataSource(mContext);
			syncLog.addSyncSaleLog(getLastSessionDate());
		}
		return transactionId;
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
		return mTransaction.countHoldOrder(getLastSessionDate());
	}

	/**
	 * @return receipt number
	 */
	public String getReceiptNo(){
		return mTransaction.getReceiptNo(getCurrentTransactionId());
	}

	/**
	 * @return total paid
	 */
	public double getTotalPaid(){
		return mPayment.getTotalPaid(getCurrentTransactionId());
	}
	
	/**
	 * @return total order qty
	 */
	public int getOrderQty(){
		return mTransaction.getTotalOrderQty(getCurrentTransactionId());
	}

	
	/**
	 * @return subTotal
	 */
	public double getSubTotalPrice(){
		return mTransaction.getTotalRetailPrice(getCurrentTransactionId());	
	}
	
	/**
	 * @return totalVatExclude
	 */
	public double getTotalVatExclude(){
		return mTransaction.getTotalVatExclude(getCurrentTransactionId());
	}
	
	/**
	 * @return totalVatExclude
	 */
	public double getTmpTotalVatExclude(){
		return mTransaction.getTmpTotalVatExclude(getCurrentTransactionId());
	}
	
	/**
	 * @return totalVat
	 */
	public double getTotalVat(){
		return mTransaction.getTotalVat(getCurrentTransactionId());
	}
	
	/**
	 * @return totalDiscount
	 */
	public double getTmpTotalDisocunt(){
		return mTransaction.getTmpTotalDiscount(getCurrentTransactionId()) + getTotalVatExclude();
	}
	
	/**
	 * @return totalDiscount
	 */
	public double getTotalDiscount(){
		return mTransaction.getTotalDiscount(getCurrentTransactionId());
	}
	
	/**
	 * @return totalSalePrice
	 */
	public double getTotalSalePrice(){
		return mTransaction.getTotalSalePrice(getCurrentTransactionId());
	}
	
	/**
	 * @return vatable
	 */
	public double getTransactionVatable(){
		return mTransaction.getTransactionVatable(getCurrentTransactionId());
	}
	
	/**
	 * @return transactionVat
	 */
	public double getTransactionVat(){
		return mTransaction.getTransactionVat(getCurrentTransactionId());
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
		return mTransaction.getTransaction(getCurrentTransactionId());
	}

	/**
	 * Get current transactionId identify by sessionDate
	 * @return current transactionId
	 */
	public int getCurrentTransactionId(){
		String sessionDate = getLastSessionDate();
		return mTransaction.getCurrTransaction(sessionDate);
	}

	/**
	 * @param shopId
	 * @param computerId
	 * @param openStaffId
	 * @param openAmount
	 * @return sessionId
	 */
	public int openSession(int shopId, int computerId, 
			int openStaffId, double openAmount){
		int sessionId = getCurrentSessionId(openStaffId);
		if( sessionId == 0){
			sessionId = mSession.addSession(shopId, computerId, 
					openStaffId, openAmount);
		}
		return sessionId;
	}
	
	/**
	 * @return session date string long millisecond pattern
	 */
	public String getLastSessionDate(){
		return mSession.getSessionDate();
	}
	
	/**
	 * Get current sessionId identify by staffId
	 * @return sessionId
	 * 0 if not have session
	 */
	public int getCurrentSessionId(int staffId){
		return mSession.getCurrentSessionId(staffId);
	}
}
