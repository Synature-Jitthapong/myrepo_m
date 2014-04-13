package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.syn.mpos.MPOSApplication;
import com.syn.mpos.database.StockDocument.DocumentTypeEntry;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SaleTransaction {
	private Shop mShop;
	private SQLiteDatabase mSqlite;
	private String mSessionDate;
	private int mTransactionId;
	
	public SaleTransaction(SQLiteDatabase db, String sessionDate, int transactionId){
		mSqlite = db;
		mShop = new Shop(db);
		mTransactionId = transactionId;
		mSessionDate = sessionDate;
	}

	public SaleTransaction(SQLiteDatabase db, String sessionDate) {
		mSqlite = db;
		mShop = new Shop(db);
		mSessionDate = sessionDate;
	}

	public POSData_SaleTransaction listSaleSaleTransactionByTransactionId() {
		POSData_SaleTransaction posSaleTrans = new POSData_SaleTransaction();
		SaleData_SessionInfo sessInfo = new SaleData_SessionInfo();

		sessInfo.setxTableSession(buildSessionObj());
		sessInfo.setxTableSessionEndDay(buildSessEnddayObj());
		posSaleTrans.setxArySaleTransaction(buildSaleTransLst(false, true));
		posSaleTrans.setxSessionInfo(sessInfo);
		return posSaleTrans;
	}
	
	public POSData_SaleTransaction listAllSaleTransactionInSaleDate() {
		POSData_SaleTransaction posSaleTrans = new POSData_SaleTransaction();
		SaleData_SessionInfo sessInfo = new SaleData_SessionInfo();

		sessInfo.setxTableSession(buildSessionObj());
		sessInfo.setxTableSessionEndDay(buildSessEnddayObj());
		posSaleTrans.setxArySaleTransaction(buildSaleTransLst(true, false));
		posSaleTrans.setxSessionInfo(sessInfo);
		return posSaleTrans;
	}
	
	public POSData_SaleTransaction listSaleTransaction() {
		POSData_SaleTransaction posSaleTrans = new POSData_SaleTransaction();
		SaleData_SessionInfo sessInfo = new SaleData_SessionInfo();

		sessInfo.setxTableSession(buildSessionObj());
		sessInfo.setxTableSessionEndDay(buildSessEnddayObj());
		posSaleTrans.setxArySaleTransaction(buildSaleTransLst(false, false));
		posSaleTrans.setxSessionInfo(sessInfo);
		return posSaleTrans;
	}

	/*
	 * SaleData_SaleTransaction{ SaleTable_OrderTransaction{
	 * [SaleTable_OrderDetail] [SaleTable_OrderPromotion]
	 * [SaleTable_PaymentDetail] } }
	 */
	public List<SaleData_SaleTransaction> buildSaleTransLst(boolean listAll, boolean listByTransId) {
		List<SaleData_SaleTransaction> saleTransLst = new ArrayList<SaleData_SaleTransaction>();
		Cursor cursor = queryTransaction();
		
		if(listAll)
			cursor = queryAllTransactionInSaleDate();
		else if(listByTransId)
			cursor = queryTransactionByTransactionId();
		else 
			cursor = queryTransaction();
		
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					SaleData_SaleTransaction saleTrans = new SaleData_SaleTransaction();
					SaleTable_OrderTransaction orderTrans = new SaleTable_OrderTransaction();
					
					orderTrans.setSzUDID(
							cursor.getString(cursor.getColumnIndex(MPOSDatabase.COLUMN_UUID)));
					orderTrans.setiTransactionID(
							cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANSACTION_ID)));
					orderTrans.setiComputerID(cursor.getInt(
							cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
					orderTrans.setiShopID(cursor.getInt(
							cursor.getColumnIndex(ShopTable.COLUMN_SHOP_ID)));
					orderTrans.setiOpenStaffID(cursor.getInt(
							cursor.getColumnIndex(OrderTransactionTable.COLUMN_OPEN_STAFF)));
					orderTrans.setDtOpenTime(Util.dateTimeFormat(
							cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_OPEN_TIME)), "yyyy-MM-dd HH:mm:ss"));
					orderTrans.setDtCloseTime(Util.dateTimeFormat(
							cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_CLOSE_TIME)), "yyyy-MM-dd HH:mm:ss"));
					orderTrans.setiDocType(
							cursor.getInt(cursor.getColumnIndex(DocumentTypeEntry.COLUMN_DOC_TYPE)));
					orderTrans.setiTransactionStatusID(
							cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_STATUS_ID)));
					orderTrans.setiReceiptYear(
							cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_YEAR)));
					orderTrans.setiReceiptMonth(
							cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_MONTH)));
					orderTrans.setiReceiptID(
							cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_ID)));
					orderTrans.setSzReceiptNo(
							cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_NO)));
					orderTrans.setDtSaleDate(Util.dateTimeFormat(
							cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_SALE_DATE)),"yyyy-MM-dd"));
					orderTrans.setfTransVAT(
							MPOSApplication.fixesDigitLength(4, cursor.getDouble(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_VAT))));
					orderTrans.setfTransactionVatable(
							MPOSApplication.fixesDigitLength(4, cursor.getDouble(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_VATABLE))));
					orderTrans.setiSessionID(
							cursor.getInt(cursor.getColumnIndex(SessionTable.COLUMN_SESS_ID)));
					orderTrans.setiVoidStaffID(cursor.getInt(cursor
							.getColumnIndex(OrderTransactionTable.COLUMN_VOID_STAFF_ID)));
					orderTrans.setSzVoidReason(
							cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_VOID_REASON)));
					orderTrans.setDtVoidTime(Util.dateTimeFormat(
							cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_VOID_TIME)), "yyyy-MM-dd HH:mm:ss"));
					orderTrans.setSzTransactionNote(
							cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_NOTE)));
					orderTrans.setiSaleMode(cursor.getInt(
							cursor.getColumnIndex(ProductsTable.COLUMN_SALE_MODE)));
					orderTrans.setfVatPercent(MPOSApplication.fixesDigitLength(4,
							mShop.getCompanyVatRate()));
					orderTrans.setfTransactionExcludeVAT(MPOSApplication.fixesDigitLength(4,cursor.getDouble(
							cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT))));
					orderTrans.setiNoCust(1);
					
					saleTrans.setxOrderTransaction(orderTrans);
					Cursor orderDetailCursor = queryOrderDetail(orderTrans.getiTransactionID());
					saleTrans.setxAryOrderDetail(buildOrderDetailLst(orderDetailCursor));
					saleTrans.setxAryOrderPromotion(builderOrderPromotionLst(orderDetailCursor));
					orderDetailCursor.close();
					saleTrans.setxAryPaymentDetail(buildPaymentDetailLst(orderTrans.getiTransactionID()));
					saleTransLst.add(saleTrans);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return saleTransLst;
	}

	// build PaymentDetailLst
	public List<SaleTable_PaymentDetail> buildPaymentDetailLst(int transId) {
		List<SaleTable_PaymentDetail> paymentDetailLst = new ArrayList<SaleTable_PaymentDetail>();
		Cursor cursor = queryPaymentDetail(transId);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					SaleTable_PaymentDetail payment = new SaleTable_PaymentDetail();
					payment.setiPaymentDetailID(cursor.getInt(cursor
							.getColumnIndex(PaymentDetailTable.COLUMN_PAY_ID)));
					payment.setiTransactionID(transId);
					payment.setiComputerID(cursor.getInt(cursor
							.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
					// payment.setiShopID(cursor.getInt(cursor.getColumnIndex(Shop.COL_SHOP_ID)));
					payment.setiPayTypeID(cursor.getInt(cursor
							.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_ID)));
					payment.setfPayAmount(
							MPOSApplication.fixesDigitLength(4, cursor.getDouble(cursor
									.getColumnIndex(PaymentDetailTable.COLUMN_PAY_AMOUNT))));
					payment.setSzCreditCardNo(cursor.getString(cursor
							.getColumnIndex(CreditCardTable.COLUMN_CREDITCARD_NO)));
					payment.setiExpireMonth(cursor.getInt(cursor
							.getColumnIndex(CreditCardTable.COLUMN_EXP_MONTH)));
					payment.setiExpireYear(cursor.getInt(cursor
							.getColumnIndex(CreditCardTable.COLUMN_EXP_YEAR)));
					payment.setSzRemark(cursor.getString(cursor
							.getColumnIndex(PaymentDetailTable.COLUMN_REMARK)));
					paymentDetailLst.add(payment);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return paymentDetailLst;
	}

	// builder OrderPromotionLst
	public List<SaleTable_OrderPromotion> builderOrderPromotionLst(Cursor cursor){
		List<SaleTable_OrderPromotion> orderPromotionLst = new ArrayList<SaleTable_OrderPromotion>();
		if(cursor != null){
			if(cursor.moveToFirst()){
				do{
					// order promotion
					if(cursor.getDouble(cursor
							.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT)) > 0){

						SaleTable_OrderPromotion promotion = new SaleTable_OrderPromotion();
						promotion.setiTransactionID(cursor.getInt(cursor
							.getColumnIndex(OrderTransactionTable.COLUMN_TRANSACTION_ID)));
						promotion.setiComputerID(cursor.getInt(cursor
							.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
						promotion.setiOrderDetailID(cursor.getInt(cursor
							.getColumnIndex(OrderDetailTable.COLUMN_ORDER_ID)));
						promotion.setfDiscountPrice(
								MPOSApplication.fixesDigitLength(4, cursor.getDouble(cursor
									.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT))));
						promotion.setfPriceAfterDiscount(
								MPOSApplication.fixesDigitLength(4, cursor.getDouble(cursor
									.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE))));
						promotion.setiDiscountTypeID(6);
						promotion.setiPromotionID(0);
						orderPromotionLst.add(promotion);
					}
				}while(cursor.moveToNext());
			}
		}
		return orderPromotionLst;
	}
	
	// build OrderDetailLst
	public List<SaleTable_OrderDetail> buildOrderDetailLst(Cursor cursor) {
		List<SaleTable_OrderDetail> orderDetailLst = new ArrayList<SaleTable_OrderDetail>();
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					SaleTable_OrderDetail order = new SaleTable_OrderDetail();
					order.setiOrderDetailID(cursor.getInt(cursor
							.getColumnIndex(OrderDetailTable.COLUMN_ORDER_ID)));
					order.setiTransactionID(cursor.getInt(cursor
							.getColumnIndex(OrderTransactionTable.COLUMN_TRANSACTION_ID)));
					order.setiComputerID(cursor.getInt(cursor
							.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
					// order.setiShopID(cursor.getInt(cursor.getColumnIndex(Shop.COL_SHOP_ID)));
					order.setiVatType(cursor.getInt(
							cursor.getColumnIndex(ProductsTable.COLUMN_VAT_TYPE)));
					order.setiProductID(cursor.getInt(cursor
							.getColumnIndex(ProductsTable.COLUMN_PRODUCT_ID)));
					order.setiProductTypeID(cursor.getInt(cursor
							.getColumnIndex(ProductsTable.COLUMN_PRODUCT_TYPE_ID)));
					order.setfQty(
							MPOSApplication.fixesDigitLength(4,cursor.getDouble(cursor
									.getColumnIndex(OrderDetailTable.COLUMN_ORDER_QTY))));
					order.setfPricePerUnit(
							MPOSApplication.fixesDigitLength(4, cursor.getDouble(
									cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_PRICE))));
					order.setfRetailPrice(
							MPOSApplication.fixesDigitLength(4, cursor.getDouble(
									cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE))));
					order.setfSalePrice(
							MPOSApplication.fixesDigitLength(4, cursor.getDouble(
									cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE))));
					order.setfTotalVatAmount(
							MPOSApplication.fixesDigitLength(4, cursor.getDouble(
									cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_VAT))));
					order.setfPriceDiscountAmount(
							MPOSApplication.fixesDigitLength(4, cursor.getDouble(
									cursor.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT))));
					order.setiSaleMode(cursor.getInt(
							cursor.getColumnIndex(ProductsTable.COLUMN_SALE_MODE)));
					order.setiProductTypeID(cursor.getInt(
							cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_TYPE_ID)));
					
					orderDetailLst.add(order);
				} while (cursor.moveToNext());
			}
		}
		return orderDetailLst;
	}

	public SaleTable_SessionEndDay buildSessEnddayObj() {
		SaleTable_SessionEndDay saleSessEnd = new SaleTable_SessionEndDay();
		Cursor cursor = querySessionEndday();
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					saleSessEnd.setDtSessionDate(Util.dateTimeFormat(cursor
							.getString(cursor
									.getColumnIndex(SessionTable.COLUMN_SESS_DATE)),
							"yyyy-MM-dd"));
					saleSessEnd
							.setDtEndDayDateTime(Util.dateTimeFormat(
									cursor.getString(cursor
											.getColumnIndex(SessionTable.COLUMN_ENDDAY_DATE)),
									"yyyy-MM-dd HH:mm:ss"));
					saleSessEnd
							.setfTotalAmountReceipt(MPOSApplication.fixesDigitLength(
									4,
									cursor.getDouble(cursor
											.getColumnIndex(SessionTable.COLUMN_TOTAL_AMOUNT_RECEIPT))));
					saleSessEnd.setiTotalQtyReceipt(cursor.getInt(cursor
							.getColumnIndex(SessionTable.COLUMN_TOTAL_QTY_RECEIPT)));
				} while (cursor.moveToNext());
			} else {
				Calendar c = Calendar.getInstance();
				saleSessEnd.setDtSessionDate(Util.dateTimeFormat(
						String.valueOf(c.getTimeInMillis()), "yyyy-MM-dd"));
				saleSessEnd.setDtEndDayDateTime(Util.dateTimeFormat(
						String.valueOf(c.getTimeInMillis()),
						"yyyy-MM-dd HH:mm:ss"));
				saleSessEnd.setfTotalAmountReceipt(MPOSApplication
						.fixesDigitLength(4, 0.0d));
				saleSessEnd.setiTotalQtyReceipt(0);
			}
			cursor.close();
		}
		return saleSessEnd;
	}

	public SaleTable_Session buildSessionObj() {
		SaleTable_Session saleSess = new SaleTable_Session();
		Cursor cursor = querySession();
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					saleSess.setiSessionID(cursor.getInt(cursor
							.getColumnIndex(SessionTable.COLUMN_SESS_ID)));
					saleSess.setiComputerID(cursor.getInt(cursor
							.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
					saleSess.setiShopID(cursor.getInt(cursor
							.getColumnIndex(ShopTable.COLUMN_SHOP_ID)));
					saleSess.setDtCloseSessionDateTime(Util.dateTimeFormat(
							cursor.getString(cursor
									.getColumnIndex(SessionTable.COLUMN_SESS_DATE)),
							"yyyy-MM-dd HH:mm:ss"));
					saleSess.setDtOpenSessionDateTime(Util.dateTimeFormat(
							cursor.getString(cursor
									.getColumnIndex(SessionTable.COLUMN_OPEN_DATE)),
							"yyyy-MM-dd HH:mm:ss"));
					saleSess.setDtCloseSessionDateTime(Util.dateTimeFormat(
							cursor.getString(cursor
									.getColumnIndex(SessionTable.COLUMN_CLOSE_DATE)),
							"yyyy-MM-dd HH:mm:ss"));
					saleSess.setDtSessionDate(Util.dateTimeFormat(cursor
							.getString(cursor
									.getColumnIndex(SessionTable.COLUMN_SESS_DATE)),
							"yyyy-MM-dd"));
					saleSess.setfOpenSessionAmount(MPOSApplication.fixesDigitLength(
							4,
							cursor.getDouble(cursor
									.getColumnIndex(SessionTable.COLUMN_OPEN_AMOUNT))));
					saleSess.setfCloseSessionAmount(MPOSApplication.fixesDigitLength(
							4,
							cursor.getDouble(cursor
									.getColumnIndex(SessionTable.COLUMN_CLOSE_AMOUNT))));
					saleSess.setiIsEndDaySession(cursor.getInt(cursor
							.getColumnIndex(SessionTable.COLUMN_IS_ENDDAY)));
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return saleSess;
	}

	public Cursor queryPaymentDetail(int transId) {
		return mSqlite.rawQuery(
				"SELECT " + PaymentDetailTable.COLUMN_PAY_ID + ", " +
				ComputerTable.COLUMN_COMPUTER_ID + ", " +
				PayTypeTable.COLUMN_PAY_TYPE_ID + ", " +
				CreditCardTable.COLUMN_CREDITCARD_NO + ", " +
				CreditCardTable.COLUMN_EXP_MONTH + ", " +
				CreditCardTable.COLUMN_EXP_YEAR + ", " +
				PaymentDetailTable.COLUMN_REMARK + ", " +
				" SUM(" + PaymentDetailTable.COLUMN_PAY_AMOUNT + ") AS " + 
				PaymentDetailTable.COLUMN_PAY_AMOUNT +
				" FROM " + PaymentDetailTable.TABLE_NAME + 
				" WHERE " + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?" +
				" GROUP BY " + PayTypeTable.COLUMN_PAY_TYPE_ID,
				new String[] { String.valueOf(transId) });
	}

	public Cursor queryOrderDetail(int transId) {
		return mSqlite.rawQuery(
				"SELECT * " + 
				" FROM " + OrderDetailTable.TABLE_ORDER + 
				" WHERE " + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?",
				new String[] { String.valueOf(transId) });
	}

	public Cursor queryTransactionByTransactionId() {
		return mSqlite.rawQuery(
				"SELECT * " + 
				" FROM " + OrderTransactionTable.TABLE_NAME + 
				" WHERE " + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=?" + 
				" AND " + OrderTransactionTable.COLUMN_STATUS_ID + " IN(?,?) ",
				new String[] {
						String.valueOf(mTransactionId),
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						String.valueOf(Transaction.TRANS_STATUS_VOID)});
	}
	
	public Cursor queryAllTransactionInSaleDate() {
		return mSqlite.rawQuery(
				"SELECT * " + 
				" FROM " + OrderTransactionTable.TABLE_NAME + 
				" WHERE " + OrderTransactionTable.COLUMN_SALE_DATE + "=?" + 
				" AND " + OrderTransactionTable.COLUMN_STATUS_ID + " IN(?,?) ",
				new String[] {
						mSessionDate,
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						String.valueOf(Transaction.TRANS_STATUS_VOID)});
	}
	
	public Cursor queryTransaction() {
		return mSqlite.rawQuery(
				"SELECT * " + 
				" FROM " + OrderTransactionTable.TABLE_NAME + 
				" WHERE " + OrderTransactionTable.COLUMN_SALE_DATE + "=?" + 
				" AND " + OrderTransactionTable.COLUMN_STATUS_ID + " IN(?,?) " + 
				" AND " + MPOSDatabase.COLUMN_SEND_STATUS + "=?",
				new String[] {
						mSessionDate,
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						String.valueOf(Transaction.TRANS_STATUS_VOID),
						String.valueOf(MPOSDatabase.NOT_SEND) });
	}

	public Cursor querySessionEndday() {
		return mSqlite.rawQuery(
				"SELECT * " + 
				" FROM " + SessionTable.TABLE_NAME + 
				" WHERE " + SessionTable.COLUMN_SESS_DATE + "=?",
				new String[] {mSessionDate});
	}

	public Cursor querySession() {
		return mSqlite.rawQuery(
				"SELECT * " + 
				" FROM " + SessionTable.TABLE_NAME + 
				" WHERE " + SessionTable.COLUMN_SESS_DATE + "=?",
				new String[] {mSessionDate});
	}

	public Cursor querySyncSaleLog() {
		return mSqlite.rawQuery(
				"SELECT * " + 
				" FROM " + SyncSaleLogTable.TABLE_NAME + 
				" WHERE " + SyncSaleLogTable.COLUMN_SYNC_STATUS + "=?",
				new String[] { String.valueOf(SyncSaleLog.SYNC_FAIL) });
	}

	public static class POSData_SaleTransaction {
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

	public static class SaleData_SessionInfo {
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

		public void setxTableSessionEndDay(
				SaleTable_SessionEndDay xTableSessionEndDay) {
			this.xTableSessionEndDay = xTableSessionEndDay;
		}
	}

	public static class SaleTable_Session {
		private int iSessionID;
		private int iComputerID;
		private int iShopID;
		private String dtSessionDate;
		private String dtOpenSessionDateTime;
		private String dtCloseSessionDateTime;
		private String fOpenSessionAmount;
		private String fCloseSessionAmount;
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

		public String getfOpenSessionAmount() {
			return fOpenSessionAmount;
		}

		public void setfOpenSessionAmount(String fOpenSessionAmount) {
			this.fOpenSessionAmount = fOpenSessionAmount;
		}

		public String getfCloseSessionAmount() {
			return fCloseSessionAmount;
		}

		public void setfCloseSessionAmount(String fCloseSessionAmount) {
			this.fCloseSessionAmount = fCloseSessionAmount;
		}

		public int getiIsEndDaySession() {
			return iIsEndDaySession;
		}

		public void setiIsEndDaySession(int iIsEndDaySession) {
			this.iIsEndDaySession = iIsEndDaySession;
		}
	}

	public static class SaleTable_SessionEndDay {
		private String dtSessionDate;
		private String dtEndDayDateTime;
		private int iTotalQtyReceipt;
		private String fTotalAmountReceipt;

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

		public String getfTotalAmountReceipt() {
			return fTotalAmountReceipt;
		}

		public void setfTotalAmountReceipt(String fTotalAmountReceipt) {
			this.fTotalAmountReceipt = fTotalAmountReceipt;
		}
	}

	public static class SaleData_SaleTransaction {
		private SaleTable_OrderTransaction xOrderTransaction;
		private List<SaleTable_OrderDetail> xAryOrderDetail;
		private List<SaleTable_OrderPromotion> xAryOrderPromotion;
		private List<SaleTable_PaymentDetail> xAryPaymentDetail;

		public SaleTable_OrderTransaction getxOrderTransaction() {
			return xOrderTransaction;
		}

		public void setxOrderTransaction(
				SaleTable_OrderTransaction xOrderTransaction) {
			this.xOrderTransaction = xOrderTransaction;
		}

		public List<SaleTable_OrderDetail> getxAryOrderDetail() {
			return xAryOrderDetail;
		}

		public void setxAryOrderDetail(
				List<SaleTable_OrderDetail> xAryOrderDetail) {
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

		public void setxAryPaymentDetail(
				List<SaleTable_PaymentDetail> xAryPaymentDetail) {
			this.xAryPaymentDetail = xAryPaymentDetail;
		}
	}

	public static class SaleTable_OrderTransaction {
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
		private String fTransVAT;
		private String fServiceCharge;
		private String fServiceChargeVAT;
		private String fTransactionVatable;
		private String fVatPercent;
		private String fServiceChargePercent;
		private int iIsCalcServiceCharge;
		private int iSessionID;
		private int iVoidStaffID;
		private String szVoidReason;
		private String dtVoidTime;
		private int iMemberID;
		private String szTransactionNote;
        private String fTransactionExcludeVAT;

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

		public String getfTransVAT() {
			return fTransVAT;
		}

		public void setfTransVAT(String fTransVAT) {
			this.fTransVAT = fTransVAT;
		}

		public String getfServiceCharge() {
			return fServiceCharge;
		}

		public void setfServiceCharge(String fServiceCharge) {
			this.fServiceCharge = fServiceCharge;
		}

		public String getfServiceChargeVAT() {
			return fServiceChargeVAT;
		}

		public void setfServiceChargeVAT(String fServiceChargeVAT) {
			this.fServiceChargeVAT = fServiceChargeVAT;
		}

		public String getfTransactionVatable() {
			return fTransactionVatable;
		}

		public void setfTransactionVatable(String fTransactionVatable) {
			this.fTransactionVatable = fTransactionVatable;
		}

		public String getfVatPercent() {
			return fVatPercent;
		}

		public void setfVatPercent(String fVatPercent) {
			this.fVatPercent = fVatPercent;
		}

		public String getfServiceChargePercent() {
			return fServiceChargePercent;
		}

		public void setfServiceChargePercent(String fServiceChargePercent) {
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

		public String getfTransactionExcludeVAT() {
			return fTransactionExcludeVAT;
		}

		public void setfTransactionExcludeVAT(String fTransactionExcludeVAT) {
			this.fTransactionExcludeVAT = fTransactionExcludeVAT;
		}
		
	}

	public static class SaleTable_OrderDetail {
		private int iOrderDetailID;
		private int iTransactionID;
		private int iComputerID;
		private int iShopID;
		private int iProductID;
		private int iProductTypeID;
		private int iSaleMode;
		private String fQty;
		private String fPricePerUnit;
		private String fRetailPrice;
		private String fSalePrice;
		private String fTotalVatAmount;
		private String fMemberDiscountAmount;
		private String fPriceDiscountAmount;
		private int iParentOrderDetailID;
        private int iVatType;

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

		public String getfQty() {
			return fQty;
		}

		public void setfQty(String fQty) {
			this.fQty = fQty;
		}

		public String getfPricePerUnit() {
			return fPricePerUnit;
		}

		public void setfPricePerUnit(String fPricePerUnit) {
			this.fPricePerUnit = fPricePerUnit;
		}

		public String getfRetailPrice() {
			return fRetailPrice;
		}

		public void setfRetailPrice(String fRetailPrice) {
			this.fRetailPrice = fRetailPrice;
		}

		public String getfSalePrice() {
			return fSalePrice;
		}

		public void setfSalePrice(String fSalePrice) {
			this.fSalePrice = fSalePrice;
		}

		public String getfTotalVatAmount() {
			return fTotalVatAmount;
		}

		public void setfTotalVatAmount(String fTotalVatAmount) {
			this.fTotalVatAmount = fTotalVatAmount;
		}

		public String getfMemberDiscountAmount() {
			return fMemberDiscountAmount;
		}

		public void setfMemberDiscountAmount(String fMemberDiscountAmount) {
			this.fMemberDiscountAmount = fMemberDiscountAmount;
		}

		public String getfPriceDiscountAmount() {
			return fPriceDiscountAmount;
		}

		public void setfPriceDiscountAmount(String fPriceDiscountAmount) {
			this.fPriceDiscountAmount = fPriceDiscountAmount;
		}

		public int getiParentOrderDetailID() {
			return iParentOrderDetailID;
		}

		public void setiParentOrderDetailID(int iParentOrderDetailID) {
			this.iParentOrderDetailID = iParentOrderDetailID;
		}

		public int getiVatType() {
			return iVatType;
		}

		public void setiVatType(int iVatType) {
			this.iVatType = iVatType;
		}
		
	}

	public static class SaleTable_OrderPromotion {
		private int iOrderDetailID;
		private int iTransactionID;
		private int iComputerID;
		private int iShopID;
		private int iDiscountTypeID;
		private int iPromotionID;
		private String fDiscountPrice;
		private String fPriceAfterDiscount;

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

		public String getfDiscountPrice() {
			return fDiscountPrice;
		}

		public void setfDiscountPrice(String fDiscountPrice) {
			this.fDiscountPrice = fDiscountPrice;
		}

		public String getfPriceAfterDiscount() {
			return fPriceAfterDiscount;
		}

		public void setfPriceAfterDiscount(String fPriceAfterDiscount) {
			this.fPriceAfterDiscount = fPriceAfterDiscount;
		}
	}

	public static class SaleTable_PaymentDetail {
		private int iPaymentDetailID;
		private int iTransactionID;
		private int iComputerID;
		private int iShopID;
		private int iPayTypeID;
		private String fPayAmount;
		private String szCreditCardNo;
		private int iExpireMonth;
		private int iExpireYear;
		private int iBankNameID;
		private int iCreditCardType;
		private String fPaymentVat;
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

		public String getfPayAmount() {
			return fPayAmount;
		}

		public void setfPayAmount(String fPayAmount) {
			this.fPayAmount = fPayAmount;
		}

		public void setfPaymentVat(String fPaymentVat) {
			this.fPaymentVat = fPaymentVat;
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

		public String getfPaymentVat() {
			return fPaymentVat;
		}

		public String getSzRemark() {
			return szRemark;
		}

		public void setSzRemark(String szRemark) {
			this.szRemark = szRemark;
		}
	}
}
