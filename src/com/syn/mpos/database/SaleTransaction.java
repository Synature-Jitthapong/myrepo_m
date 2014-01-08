package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.database.inventory.StockDocument;
import com.syn.mpos.database.transaction.PaymentDetail;
import com.syn.mpos.database.transaction.Session;
import com.syn.mpos.database.transaction.Transaction;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SaleTransaction {
	private Context mContext;
	private String mSessionDate;
	
	public SaleTransaction(Context c, String sessionDate){
		mContext = c;
		mSessionDate = sessionDate;
	}
	
	public POSData_SaleTransaction listSaleTransaction(){
		POSData_SaleTransaction posSaleTrans = new POSData_SaleTransaction();
		SaleData_SessionInfo sessInfo = new SaleData_SessionInfo();
		
		sessInfo.setxTableSession(buildSessionObj());
		sessInfo.setxTableSessionEndDay(buildSessEnddayObj());
		posSaleTrans.setxArySaleTransaction(buildSaleTransLst());
		posSaleTrans.setxSessionInfo(sessInfo);
		return posSaleTrans;
	}
	
	/*
	 * SaleData_SaleTransaction{
	 *  SaleTable_OrderTransaction{
	 *    [SaleTable_OrderDetail]
	 *    [SaleTable_OrderPromotion]
	 *    [SaleTable_PaymentDetail]
	 *   }
	 * }
	 */
	private List<SaleData_SaleTransaction> buildSaleTransLst(){
		List<SaleData_SaleTransaction> saleTransLst = 
				new ArrayList<SaleData_SaleTransaction>();;
		Transaction trans = new Transaction(mContext);
		trans.open();
		Cursor cursor = queryTransaction(trans.mSqlite);
		if(cursor != null){
			if(cursor.moveToFirst()){
				do{
					SaleData_SaleTransaction saleTrans = new SaleData_SaleTransaction();
					SaleTable_OrderTransaction orderTrans = new SaleTable_OrderTransaction();
					
					orderTrans.setSzUDID(cursor.getString(cursor.getColumnIndex(MPOSDatabase.COL_UUID)));
					orderTrans.setiTransactionID(cursor.getInt(cursor.getColumnIndex(Transaction.COL_TRANS_ID)));
					orderTrans.setiComputerID(cursor.getInt(cursor.getColumnIndex(Computer.COL_COMPUTER_ID)));
					orderTrans.setiShopID(cursor.getInt(cursor.getColumnIndex(Shop.COL_SHOP_ID)));
					orderTrans.setiOpenStaffID(cursor.getInt(cursor.getColumnIndex(Transaction.COL_OPEN_STAFF)));
					orderTrans.setDtOpenTime(
							Util.dateTimeFormat(cursor.getString(
									cursor.getColumnIndex(Transaction.COL_OPEN_TIME)), "yyyy-MM-dd HH:mm:ss"));
					orderTrans.setDtCloseTime(
							Util.dateTimeFormat(cursor.getString(
									cursor.getColumnIndex(Transaction.COL_CLOSE_TIME)), "yyyy-MM-dd HH:mm:ss"));
					orderTrans.setiDocType(cursor.getInt(cursor.getColumnIndex(StockDocument.COL_DOC_TYPE)));
					orderTrans.setiTransactionStatusID(cursor.getInt(cursor.getColumnIndex(Transaction.COL_STATUS_ID)));
					orderTrans.setiReceiptYear(cursor.getInt(cursor.getColumnIndex(Transaction.COL_RECEIPT_YEAR)));
					orderTrans.setiReceiptMonth(cursor.getInt(cursor.getColumnIndex(Transaction.COL_RECEIPT_MONTH)));
					orderTrans.setiReceiptID(cursor.getInt(cursor.getColumnIndex(Transaction.COL_RECEIPT_ID)));
					orderTrans.setSzReceiptNo(cursor.getString(cursor.getColumnIndex(Transaction.COL_RECEIPT_NO)));
					orderTrans.setDtSaleDate(
							Util.dateTimeFormat(cursor.getString(
									cursor.getColumnIndex(Transaction.COL_SALE_DATE)), "yyyy-MM-dd"));
					orderTrans.setfTransVAT(cursor.getDouble(cursor.getColumnIndex(Transaction.COL_TRANS_VAT)));
					orderTrans.setfTransactionVatable(cursor.getDouble(cursor.getColumnIndex(Transaction.COL_TRANS_VATABLE)));
					orderTrans.setiSessionID(cursor.getInt(cursor.getColumnIndex(Session.COL_SESS_ID)));
					orderTrans.setiVoidStaffID(cursor.getInt(cursor.getColumnIndex(Transaction.COL_VOID_STAFF_ID)));
					orderTrans.setSzVoidReason(cursor.getString(cursor.getColumnIndex(Transaction.COL_VOID_REASON)));
					orderTrans.setDtVoidTime(
							Util.dateTimeFormat(cursor.getString(
									cursor.getColumnIndex(Transaction.COL_VOID_TIME)), "yyyy-MM-dd HH:mm:ss"));
					orderTrans.setSzTransactionNote(cursor.getString(cursor.getColumnIndex(Transaction.COL_TRANS_NOTE)));
					
					saleTrans.setxOrderTransaction(orderTrans);
					saleTrans.setxAryOrderDetail(buildOrderDetailLst(trans.mSqlite, orderTrans.getiTransactionID()));
					saleTrans.setxAryPaymentDetail(buildPaymentDetailLst(trans.mSqlite, orderTrans.getiTransactionID()));
					saleTrans.setxAryOrderPromotion(new ArrayList<SaleTable_OrderPromotion>());
					saleTransLst.add(saleTrans);
				}while(cursor.moveToNext());
			}
			cursor.close();
		}
		trans.close();
		return saleTransLst;
	}
	
	// build PaymentDetailLst
	public List<SaleTable_PaymentDetail> buildPaymentDetailLst(SQLiteDatabase sqlite, int transId){
		List<SaleTable_PaymentDetail> paymentDetailLst = new ArrayList<SaleTable_PaymentDetail>();
		Cursor cursor = queryPaymentDetail(sqlite, transId);
		if(cursor != null){
			if(cursor.moveToFirst()){
				do{
					SaleTable_PaymentDetail payment = new SaleTable_PaymentDetail();
					payment.setiPaymentDetailID(cursor.getInt(cursor.getColumnIndex(PaymentDetail.COL_PAY_ID)));
					payment.setiTransactionID(transId);
					payment.setiComputerID(cursor.getInt(cursor.getColumnIndex(Computer.COL_COMPUTER_ID)));
					//payment.setiShopID(cursor.getInt(cursor.getColumnIndex(Shop.COL_SHOP_ID)));
					payment.setiPayTypeID(cursor.getInt(cursor.getColumnIndex(PaymentDetail.COL_PAY_TYPE_ID)));
					payment.setfPayAmount(cursor.getDouble(cursor.getColumnIndex(PaymentDetail.COL_PAY_AMOUNT)));
					payment.setSzCreditCardNo(cursor.getString(cursor.getColumnIndex(CreditCard.COL_CREDIT_CARD_NO)));
					payment.setiExpireMonth(cursor.getInt(cursor.getColumnIndex(CreditCard.COL_EXP_MONTH)));
					payment.setiExpireYear(cursor.getInt(cursor.getColumnIndex(CreditCard.COL_EXP_YEAR)));
					payment.setSzRemark(cursor.getString(cursor.getColumnIndex(PaymentDetail.COL_REMARK)));
					paymentDetailLst.add(payment);
				}while(cursor.moveToNext());
			}
			cursor.close();
		}
		return paymentDetailLst;
	}
	
	// build OrderDetailLst
	public List<SaleTable_OrderDetail> buildOrderDetailLst(SQLiteDatabase sqlite, int transId){
		List<SaleTable_OrderDetail> orderDetailLst = new ArrayList<SaleTable_OrderDetail>();
		Cursor cursor = queryOrderDetail(sqlite, transId);
		if(cursor != null){
			if(cursor.moveToFirst()){
				do{
					SaleTable_OrderDetail order = new SaleTable_OrderDetail();
					order.setiOrderDetailID(cursor.getInt(cursor.getColumnIndex(Transaction.COL_ORDER_ID)));
					order.setiTransactionID(cursor.getInt(cursor.getColumnIndex(Transaction.COL_TRANS_ID)));
					order.setiComputerID(cursor.getInt(cursor.getColumnIndex(Computer.COL_COMPUTER_ID)));
					//order.setiShopID(cursor.getInt(cursor.getColumnIndex(Shop.COL_SHOP_ID)));
					order.setiProductID(cursor.getInt(cursor.getColumnIndex(Products.COL_PRODUCT_ID)));
					order.setiProductTypeID(cursor.getInt(cursor.getColumnIndex(Products.COL_PRODUCT_TYPE_ID)));
					order.setfQty(cursor.getDouble(cursor.getColumnIndex(Transaction.COL_ORDER_QTY)));
					order.setfPricePerUnit(cursor.getDouble(cursor.getColumnIndex(Products.COL_PRODUCT_PRICE)));
					order.setfRetailPrice(cursor.getDouble(cursor.getColumnIndex(Transaction.COL_TOTAL_RETAIL_PRICE)));
					order.setfSalePrice(cursor.getDouble(cursor.getColumnIndex(Transaction.COL_TOTAL_SALE_PRICE)));
					order.setfTotalVatAmount(cursor.getDouble(cursor.getColumnIndex(Transaction.COL_TOTAL_VAT)));
					order.setfPriceDiscountAmount(cursor.getDouble(cursor.getColumnIndex(Transaction.COL_PRICE_DISCOUNT)));
					orderDetailLst.add(order);
				}while(cursor.moveToNext());
			}
			cursor.close();
		}
		return orderDetailLst;
	}
	
	public SaleTable_SessionEndDay buildSessEnddayObj(){
		SaleTable_SessionEndDay saleSessEnd = new SaleTable_SessionEndDay();
		Session sess = new Session(mContext);
		sess.open();
		Cursor cursor = querySessionEndday(sess.mSqlite);
		if(cursor != null){
			if(cursor.moveToFirst()){
				do{
					saleSessEnd.setDtSessionDate(
							Util.dateTimeFormat(cursor.getString(
									cursor.getColumnIndex(Session.COL_SESS_DATE)), "yyyy-MM-dd"));
					saleSessEnd.setDtEndDayDateTime(
							Util.dateTimeFormat(cursor.getString(
									cursor.getColumnIndex(Session.COL_ENDDAY_DATE)), "yyyy-MM-dd HH:mm:ss"));
					saleSessEnd.setfTotalAmountReceipt(cursor.getDouble(cursor.getColumnIndex(Session.COL_TOTAL_AMOUNT_RECEIPT)));
					saleSessEnd.setiTotalQtyReceipt(cursor.getInt(cursor.getColumnIndex(Session.COL_TOTAL_QTY_RECEIPT)));
				}while(cursor.moveToNext());
			}
			cursor.close();
		}
		sess.close();
		return saleSessEnd;
	}
	
	public SaleTable_Session buildSessionObj(){
		SaleTable_Session saleSess = new SaleTable_Session();
		Session sess = new Session(mContext);
		sess.open();
		Cursor cursor = querySession(sess.mSqlite);
		if(cursor != null){
			if(cursor.moveToFirst()){
				do{
					saleSess.setiSessionID(cursor.getInt(cursor.getColumnIndex(Session.COL_SESS_ID)));
					saleSess.setiComputerID(cursor.getInt(cursor.getColumnIndex(Computer.COL_COMPUTER_ID)));
					saleSess.setiShopID(cursor.getInt(cursor.getColumnIndex(Shop.COL_SHOP_ID)));
					saleSess.setDtCloseSessionDateTime(
							Util.dateTimeFormat(cursor.getString(
									cursor.getColumnIndex(Session.COL_SESS_DATE)), "yyyy-MM-dd HH:mm:ss"));
					saleSess.setDtOpenSessionDateTime(
							Util.dateTimeFormat(cursor.getString(
									cursor.getColumnIndex(Session.COL_OPEN_DATE)), "yyyy-MM-dd HH:mm:ss"));
					saleSess.setDtCloseSessionDateTime(
							Util.dateTimeFormat(cursor.getString(
									cursor.getColumnIndex(Session.COL_CLOSE_DATE)), "yyyy-MM-dd HH:mm:ss"));
					saleSess.setDtSessionDate(
							Util.dateTimeFormat(cursor.getString(
									cursor.getColumnIndex(Session.COL_SESS_DATE)), "yyyy-MM-dd"));
					saleSess.setfOpenSessionAmount(cursor.getDouble(cursor.getColumnIndex(Session.COL_OPEN_AMOUNT)));
					saleSess.setfCloseSessionAmount(cursor.getDouble(cursor.getColumnIndex(Session.COL_CLOSE_AMOUNT)));
					saleSess.setiIsEndDaySession(cursor.getInt(cursor.getColumnIndex(Session.COL_IS_ENDDAY)));
				}while(cursor.moveToNext());
			}
			cursor.close();
		}
		sess.close();
		return saleSess;
	}
	
	public Cursor queryPaymentDetail(SQLiteDatabase sqlite, int transId){
		return sqlite.rawQuery("SELECT * "
				+ " FROM " + PaymentDetail.TB_PAYMENT
				+ " WHERE " + Transaction.COL_TRANS_ID + "=?", 
				new String[]{String.valueOf(transId)});
	}
	
	public Cursor queryOrderDetail(SQLiteDatabase sqlite, int transId){
		return sqlite.rawQuery("SELECT * "
				+ " FROM " + Transaction.TB_ORDER
				+ " WHERE " + Transaction.COL_TRANS_ID + "=?", 
				new String[]{String.valueOf(transId)});
	}
	
	public Cursor queryTransaction(SQLiteDatabase sqlite){
		return sqlite.rawQuery("SELECT * "
				+ " FROM " + Transaction.TB_TRANS
				+ " WHERE " + Transaction.COL_SALE_DATE + "=?"
				+ " AND " + Transaction.COL_STATUS_ID + " IN(?,?) "
				+ " AND " + MPOSDatabase.COL_SEND_STATUS + "=?", 
				new String[]{String.valueOf(mSessionDate), 
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						String.valueOf(Transaction.TRANS_STATUS_VOID),
						String.valueOf(MPOSDatabase.NOT_SEND)});
	}
	
	public Cursor querySessionEndday(SQLiteDatabase sqlite){
		return sqlite.rawQuery("SELECT * "
				+ " FROM " + Session.TB_SESSION_DETAIL
				+ " WHERE " + Session.COL_SESS_DATE
				+ "=?", new String[]{String.valueOf(mSessionDate)});
	}
	
	public Cursor querySession(SQLiteDatabase sqlite){
		return sqlite.rawQuery("SELECT * "
				+ " FROM " + Session.TB_SESSION
				+ " WHERE " + Session.COL_SESS_DATE
				+ "=?", new String[]{String.valueOf(mSessionDate)});
	}
	
	public Cursor querySyncSaleLog(SQLiteDatabase sqlite){
		return sqlite.rawQuery("SELECT * "
				+ " FROM " + SyncSaleLog.TB_SYNC_SALE_LOG
				+ " WHERE " + SyncSaleLog.COL_SYNC_STATUS 
				+ "=?", 
				new String[]{String.valueOf(SyncSaleLog.SYNC_FAIL)});
	}
	
    public static class POSData_SaleTransaction
    {
        private SaleData_SessionInfo xSessionInfo;
        private List<SaleData_SaleTransaction> xArySaleTransaction;
	
        public SaleData_SessionInfo getxSessionInfo() {
			return xSessionInfo;
		}
		public void setxSessionInfo(SaleData_SessionInfo xSessionInfo) {
			this.xSessionInfo = xSessionInfo;
		}
		public List<SaleData_SaleTransaction> getxArySaleTransaction() {
			return xArySaleTransaction;
		}
		public void setxArySaleTransaction(
				List<SaleData_SaleTransaction> xArySaleTransaction) {
			this.xArySaleTransaction = xArySaleTransaction;
		}
    }

    public static class SaleData_SessionInfo
    {
        private SaleTable_Session xTableSession;
        private SaleTable_SessionEndDay xTableSessionEndDay;
		
        public SaleTable_Session getxTableSession() {
			return xTableSession;
		}
		public void setxTableSession(SaleTable_Session xTableSession) {
			this.xTableSession = xTableSession;
		}
		public SaleTable_SessionEndDay getxTableSessionEndDay() {
			return xTableSessionEndDay;
		}
		public void setxTableSessionEndDay(SaleTable_SessionEndDay xTableSessionEndDay) {
			this.xTableSessionEndDay = xTableSessionEndDay;
		}
    }

    public static class SaleTable_Session
    {
        private int iSessionID;
        private int iComputerID;
        private int iShopID;
        private String dtSessionDate;
        private String dtOpenSessionDateTime;
        private String dtCloseSessionDateTime;
        private Double fOpenSessionAmount;
        private Double fCloseSessionAmount;
        private int iIsEndDaySession;
		
        public int getiSessionID() {
			return iSessionID;
		}
		public void setiSessionID(int iSessionID) {
			this.iSessionID = iSessionID;
		}
		public int getiComputerID() {
			return iComputerID;
		}
		public void setiComputerID(int iComputerID) {
			this.iComputerID = iComputerID;
		}
		public int getiShopID() {
			return iShopID;
		}
		public void setiShopID(int iShopID) {
			this.iShopID = iShopID;
		}
		public String getDtSessionDate() {
			return dtSessionDate;
		}
		public void setDtSessionDate(String dtSessionDate) {
			this.dtSessionDate = dtSessionDate;
		}
		public String getDtOpenSessionDateTime() {
			return dtOpenSessionDateTime;
		}
		public void setDtOpenSessionDateTime(String dtOpenSessionDateTime) {
			this.dtOpenSessionDateTime = dtOpenSessionDateTime;
		}
		public String getDtCloseSessionDateTime() {
			return dtCloseSessionDateTime;
		}
		public void setDtCloseSessionDateTime(String dtCloseSessionDateTime) {
			this.dtCloseSessionDateTime = dtCloseSessionDateTime;
		}
		public Double getfOpenSessionAmount() {
			return fOpenSessionAmount;
		}
		public void setfOpenSessionAmount(Double fOpenSessionAmount) {
			this.fOpenSessionAmount = fOpenSessionAmount;
		}
		public Double getfCloseSessionAmount() {
			return fCloseSessionAmount;
		}
		public void setfCloseSessionAmount(Double fCloseSessionAmount) {
			this.fCloseSessionAmount = fCloseSessionAmount;
		}
		public int getiIsEndDaySession() {
			return iIsEndDaySession;
		}
		public void setiIsEndDaySession(int iIsEndDaySession) {
			this.iIsEndDaySession = iIsEndDaySession;
		}
    }

    public static class SaleTable_SessionEndDay
    {
        private String dtSessionDate;
        private String dtEndDayDateTime;
        private int iTotalQtyReceipt;
        private Double fTotalAmountReceipt;
		
        public String getDtSessionDate() {
			return dtSessionDate;
		}
		public void setDtSessionDate(String dtSessionDate) {
			this.dtSessionDate = dtSessionDate;
		}
		public String getDtEndDayDateTime() {
			return dtEndDayDateTime;
		}
		public void setDtEndDayDateTime(String dtEndDayDateTime) {
			this.dtEndDayDateTime = dtEndDayDateTime;
		}
		public int getiTotalQtyReceipt() {
			return iTotalQtyReceipt;
		}
		public void setiTotalQtyReceipt(int iTotalQtyReceipt) {
			this.iTotalQtyReceipt = iTotalQtyReceipt;
		}
		public Double getfTotalAmountReceipt() {
			return fTotalAmountReceipt;
		}
		public void setfTotalAmountReceipt(Double fTotalAmountReceipt) {
			this.fTotalAmountReceipt = fTotalAmountReceipt;
		}
    }
    
    public static class SaleData_SaleTransaction
    {
        private SaleTable_OrderTransaction xOrderTransaction;
        private List<SaleTable_OrderDetail> xAryOrderDetail;
        private List<SaleTable_OrderPromotion> xAryOrderPromotion;
        private List<SaleTable_PaymentDetail> xAryPaymentDetail;
        
		public SaleTable_OrderTransaction getxOrderTransaction() {
			return xOrderTransaction;
		}
		public void setxOrderTransaction(SaleTable_OrderTransaction xOrderTransaction) {
			this.xOrderTransaction = xOrderTransaction;
		}
		public List<SaleTable_OrderDetail> getxAryOrderDetail() {
			return xAryOrderDetail;
		}
		public void setxAryOrderDetail(List<SaleTable_OrderDetail> xAryOrderDetail) {
			this.xAryOrderDetail = xAryOrderDetail;
		}
		public List<SaleTable_OrderPromotion> getxAryOrderPromotion() {
			return xAryOrderPromotion;
		}
		public void setxAryOrderPromotion(
				List<SaleTable_OrderPromotion> xAryOrderPromotion) {
			this.xAryOrderPromotion = xAryOrderPromotion;
		}
		public List<SaleTable_PaymentDetail> getxAryPaymentDetail() {
			return xAryPaymentDetail;
		}
		public void setxAryPaymentDetail(List<SaleTable_PaymentDetail> xAryPaymentDetail) {
			this.xAryPaymentDetail = xAryPaymentDetail;
		}
    }

    public static class SaleTable_OrderTransaction
    {
    	private String szUDID;
        private int iTransactionID;
        private int iComputerID;
        private int iShopID;
        private int iOpenStaffID;
        private String dtOpenTime;
        private String dtCloseTime;
        private int iSaleMode;
        private String szQueueName;
        private int iNoCust;
        private int iDocType;
        private int iTransactionStatusID;
        private int iReceiptYear;
        private int iReceiptMonth;
        private int iReceiptID;
        private String szReceiptNo;
        private String dtSaleDate;
        private Double fTransVAT;
        private Double fServiceCharge;
        private Double fServiceChargeVAT;
        private Double fTransactionVatable;
        private Double fVatPercent;
        private Double fServiceChargePercent;
        private int iIsCalcServiceCharge;
        private int iSessionID;
        private int iVoidStaffID;
        private String szVoidReason;
        private String dtVoidTime;
        private int iMemberID;
        private String szTransactionNote;
        
		public String getSzUDID() {
			return szUDID;
		}
		public void setSzUDID(String szUDID) {
			this.szUDID = szUDID;
		}
		public int getiTransactionID() {
			return iTransactionID;
		}
		public void setiTransactionID(int iTransactionID) {
			this.iTransactionID = iTransactionID;
		}
		public int getiComputerID() {
			return iComputerID;
		}
		public void setiComputerID(int iComputerID) {
			this.iComputerID = iComputerID;
		}
		public int getiShopID() {
			return iShopID;
		}
		public void setiShopID(int iShopID) {
			this.iShopID = iShopID;
		}
		public int getiOpenStaffID() {
			return iOpenStaffID;
		}
		public void setiOpenStaffID(int iOpenStaffID) {
			this.iOpenStaffID = iOpenStaffID;
		}
		public String getDtOpenTime() {
			return dtOpenTime;
		}
		public void setDtOpenTime(String dtOpenTime) {
			this.dtOpenTime = dtOpenTime;
		}
		public String getDtCloseTime() {
			return dtCloseTime;
		}
		public void setDtCloseTime(String dtCloseTime) {
			this.dtCloseTime = dtCloseTime;
		}
		public int getiSaleMode() {
			return iSaleMode;
		}
		public void setiSaleMode(int iSaleMode) {
			this.iSaleMode = iSaleMode;
		}
		public String getSzQueueName() {
			return szQueueName;
		}
		public void setSzQueueName(String szQueueName) {
			this.szQueueName = szQueueName;
		}
		public int getiNoCust() {
			return iNoCust;
		}
		public void setiNoCust(int iNoCust) {
			this.iNoCust = iNoCust;
		}
		public int getiDocType() {
			return iDocType;
		}
		public void setiDocType(int iDocType) {
			this.iDocType = iDocType;
		}
		public int getiTransactionStatusID() {
			return iTransactionStatusID;
		}
		public void setiTransactionStatusID(int iTransactionStatusID) {
			this.iTransactionStatusID = iTransactionStatusID;
		}
		public int getiReceiptYear() {
			return iReceiptYear;
		}
		public void setiReceiptYear(int iReceiptYear) {
			this.iReceiptYear = iReceiptYear;
		}
		public int getiReceiptMonth() {
			return iReceiptMonth;
		}
		public void setiReceiptMonth(int iReceiptMonth) {
			this.iReceiptMonth = iReceiptMonth;
		}
		public int getiReceiptID() {
			return iReceiptID;
		}
		public void setiReceiptID(int iReceiptID) {
			this.iReceiptID = iReceiptID;
		}
		public String getSzReceiptNo() {
			return szReceiptNo;
		}
		public void setSzReceiptNo(String szReceiptNo) {
			this.szReceiptNo = szReceiptNo;
		}
		public String getDtSaleDate() {
			return dtSaleDate;
		}
		public void setDtSaleDate(String dtSaleDate) {
			this.dtSaleDate = dtSaleDate;
		}
		public Double getfTransVAT() {
			return fTransVAT;
		}
		public void setfTransVAT(Double fTransVAT) {
			this.fTransVAT = fTransVAT;
		}
		public Double getfServiceCharge() {
			return fServiceCharge;
		}
		public void setfServiceCharge(Double fServiceCharge) {
			this.fServiceCharge = fServiceCharge;
		}
		public Double getfServiceChargeVAT() {
			return fServiceChargeVAT;
		}
		public void setfServiceChargeVAT(Double fServiceChargeVAT) {
			this.fServiceChargeVAT = fServiceChargeVAT;
		}
		public Double getfTransactionVatable() {
			return fTransactionVatable;
		}
		public void setfTransactionVatable(Double fTransactionVatable) {
			this.fTransactionVatable = fTransactionVatable;
		}
		public Double getfVatPercent() {
			return fVatPercent;
		}
		public void setfVatPercent(Double fVatPercent) {
			this.fVatPercent = fVatPercent;
		}
		public Double getfServiceChargePercent() {
			return fServiceChargePercent;
		}
		public void setfServiceChargePercent(Double fServiceChargePercent) {
			this.fServiceChargePercent = fServiceChargePercent;
		}
		public int getiIsCalcServiceCharge() {
			return iIsCalcServiceCharge;
		}
		public void setiIsCalcServiceCharge(int iIsCalcServiceCharge) {
			this.iIsCalcServiceCharge = iIsCalcServiceCharge;
		}
		public int getiSessionID() {
			return iSessionID;
		}
		public void setiSessionID(int iSessionID) {
			this.iSessionID = iSessionID;
		}
		public int getiVoidStaffID() {
			return iVoidStaffID;
		}
		public void setiVoidStaffID(int iVoidStaffID) {
			this.iVoidStaffID = iVoidStaffID;
		}
		public String getSzVoidReason() {
			return szVoidReason;
		}
		public void setSzVoidReason(String szVoidReason) {
			this.szVoidReason = szVoidReason;
		}
		public String getDtVoidTime() {
			return dtVoidTime;
		}
		public void setDtVoidTime(String dtVoidTime) {
			this.dtVoidTime = dtVoidTime;
		}
		public int getiMemberID() {
			return iMemberID;
		}
		public void setiMemberID(int iMemberID) {
			this.iMemberID = iMemberID;
		}
		public String getSzTransactionNote() {
			return szTransactionNote;
		}
		public void setSzTransactionNote(String szTransactionNote) {
			this.szTransactionNote = szTransactionNote;
		}
    }
    
    public static class SaleTable_OrderDetail
    {
        private int iOrderDetailID;
        private int iTransactionID;
        private int iComputerID;
        private int iShopID;
        private int iProductID;
        private int iProductTypeID;
        private int iSaleMode;
        private Double fQty;
        private Double fPricePerUnit;
        private Double fRetailPrice;
        private Double fSalePrice;
        private Double fTotalVatAmount;
        private Double fMemberDiscountAmount;
        private Double fPriceDiscountAmount;
        private int iParentOrderDetailID;
        
		public int getiOrderDetailID() {
			return iOrderDetailID;
		}
		public void setiOrderDetailID(int iOrderDetailID) {
			this.iOrderDetailID = iOrderDetailID;
		}
		public int getiTransactionID() {
			return iTransactionID;
		}
		public void setiTransactionID(int iTransactionID) {
			this.iTransactionID = iTransactionID;
		}
		public int getiComputerID() {
			return iComputerID;
		}
		public void setiComputerID(int iComputerID) {
			this.iComputerID = iComputerID;
		}
		public int getiShopID() {
			return iShopID;
		}
		public void setiShopID(int iShopID) {
			this.iShopID = iShopID;
		}
		public int getiProductID() {
			return iProductID;
		}
		public void setiProductID(int iProductID) {
			this.iProductID = iProductID;
		}
		public int getiProductTypeID() {
			return iProductTypeID;
		}
		public void setiProductTypeID(int iProductTypeID) {
			this.iProductTypeID = iProductTypeID;
		}
		public int getiSaleMode() {
			return iSaleMode;
		}
		public void setiSaleMode(int iSaleMode) {
			this.iSaleMode = iSaleMode;
		}
		public Double getfQty() {
			return fQty;
		}
		public void setfQty(Double fQty) {
			this.fQty = fQty;
		}
		public Double getfPricePerUnit() {
			return fPricePerUnit;
		}
		public void setfPricePerUnit(Double fPricePerUnit) {
			this.fPricePerUnit = fPricePerUnit;
		}
		public Double getfRetailPrice() {
			return fRetailPrice;
		}
		public void setfRetailPrice(Double fRetailPrice) {
			this.fRetailPrice = fRetailPrice;
		}
		public Double getfSalePrice() {
			return fSalePrice;
		}
		public void setfSalePrice(Double fSalePrice) {
			this.fSalePrice = fSalePrice;
		}
		public Double getfTotalVatAmount() {
			return fTotalVatAmount;
		}
		public void setfTotalVatAmount(Double fTotalVatAmount) {
			this.fTotalVatAmount = fTotalVatAmount;
		}
		public Double getfMemberDiscountAmount() {
			return fMemberDiscountAmount;
		}
		public void setfMemberDiscountAmount(Double fMemberDiscountAmount) {
			this.fMemberDiscountAmount = fMemberDiscountAmount;
		}
		public Double getfPriceDiscountAmount() {
			return fPriceDiscountAmount;
		}
		public void setfPriceDiscountAmount(Double fPriceDiscountAmount) {
			this.fPriceDiscountAmount = fPriceDiscountAmount;
		}
		public int getiParentOrderDetailID() {
			return iParentOrderDetailID;
		}
		public void setiParentOrderDetailID(int iParentOrderDetailID) {
			this.iParentOrderDetailID = iParentOrderDetailID;
		}
    }

    public static class SaleTable_OrderPromotion
    {
        private int iOrderDetailID;
        private int iTransactionID;
        private int iComputerID;
        private int iShopID;
        private int iDiscountTypeID;
        private int iPromotionID;
        private Double fDiscountPrice;
        private Double fPriceAfterDiscount;
        
		public int getiOrderDetailID() {
			return iOrderDetailID;
		}
		public void setiOrderDetailID(int iOrderDetailID) {
			this.iOrderDetailID = iOrderDetailID;
		}
		public int getiTransactionID() {
			return iTransactionID;
		}
		public void setiTransactionID(int iTransactionID) {
			this.iTransactionID = iTransactionID;
		}
		public int getiComputerID() {
			return iComputerID;
		}
		public void setiComputerID(int iComputerID) {
			this.iComputerID = iComputerID;
		}
		public int getiShopID() {
			return iShopID;
		}
		public void setiShopID(int iShopID) {
			this.iShopID = iShopID;
		}
		public int getiDiscountTypeID() {
			return iDiscountTypeID;
		}
		public void setiDiscountTypeID(int iDiscountTypeID) {
			this.iDiscountTypeID = iDiscountTypeID;
		}
		public int getiPromotionID() {
			return iPromotionID;
		}
		public void setiPromotionID(int iPromotionID) {
			this.iPromotionID = iPromotionID;
		}
		public Double getfDiscountPrice() {
			return fDiscountPrice;
		}
		public void setfDiscountPrice(Double fDiscountPrice) {
			this.fDiscountPrice = fDiscountPrice;
		}
		public Double getfPriceAfterDiscount() {
			return fPriceAfterDiscount;
		}
		public void setfPriceAfterDiscount(Double fPriceAfterDiscount) {
			this.fPriceAfterDiscount = fPriceAfterDiscount;
		}
    }

    public static class SaleTable_PaymentDetail
    {
        private int iPaymentDetailID;
        private int iTransactionID;
        private int iComputerID;
        private int iShopID;
        private int iPayTypeID;
        private Double fPayAmount;
        private String szCreditCardNo;
        private int iExpireMonth;
        private int iExpireYear;
        private int iBankNameID;
        private int iCreditCardType;
        private Double fPaymentVat;
        private String szRemark;
        
		public int getiPaymentDetailID() {
			return iPaymentDetailID;
		}
		public void setiPaymentDetailID(int iPaymentDetailID) {
			this.iPaymentDetailID = iPaymentDetailID;
		}
		public int getiTransactionID() {
			return iTransactionID;
		}
		public void setiTransactionID(int iTransactionID) {
			this.iTransactionID = iTransactionID;
		}
		public int getiComputerID() {
			return iComputerID;
		}
		public void setiComputerID(int iComputerID) {
			this.iComputerID = iComputerID;
		}
		public int getiShopID() {
			return iShopID;
		}
		public void setiShopID(int iShopID) {
			this.iShopID = iShopID;
		}
		public int getiPayTypeID() {
			return iPayTypeID;
		}
		public void setiPayTypeID(int iPayTypeID) {
			this.iPayTypeID = iPayTypeID;
		}
		public Double getfPayAmount() {
			return fPayAmount;
		}
		public void setfPayAmount(Double fPayAmount) {
			this.fPayAmount = fPayAmount;
		}
		public String getSzCreditCardNo() {
			return szCreditCardNo;
		}
		public void setSzCreditCardNo(String szCreditCardNo) {
			this.szCreditCardNo = szCreditCardNo;
		}
		public int getiExpireMonth() {
			return iExpireMonth;
		}
		public void setiExpireMonth(int iExpireMonth) {
			this.iExpireMonth = iExpireMonth;
		}
		public int getiExpireYear() {
			return iExpireYear;
		}
		public void setiExpireYear(int iExpireYear) {
			this.iExpireYear = iExpireYear;
		}
		public int getiBankNameID() {
			return iBankNameID;
		}
		public void setiBankNameID(int iBankNameID) {
			this.iBankNameID = iBankNameID;
		}
		public int getiCreditCardType() {
			return iCreditCardType;
		}
		public void setiCreditCardType(int iCreditCardType) {
			this.iCreditCardType = iCreditCardType;
		}
		public Double getfPaymentVat() {
			return fPaymentVat;
		}
		public void setfPaymentVat(Double fPaymentVat) {
			this.fPaymentVat = fPaymentVat;
		}
		public String getSzRemark() {
			return szRemark;
		}
		public void setSzRemark(String szRemark) {
			this.szRemark = szRemark;
		}
    }
}
