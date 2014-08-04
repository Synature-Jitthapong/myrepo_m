package com.synature.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.synature.mpos.Utils;
import com.synature.mpos.database.table.BankTable;
import com.synature.mpos.database.table.ComputerTable;
import com.synature.mpos.database.table.CreditCardTable;
import com.synature.mpos.database.table.MenuCommentTable;
import com.synature.mpos.database.table.OrderCommentTable;
import com.synature.mpos.database.table.OrderDetailTable;
import com.synature.mpos.database.table.OrderSetTable;
import com.synature.mpos.database.table.OrderTransactionTable;
import com.synature.mpos.database.table.PayTypeTable;
import com.synature.mpos.database.table.PaymentDetailTable;
import com.synature.mpos.database.table.ProductComponentGroupTable;
import com.synature.mpos.database.table.ProductComponentTable;
import com.synature.mpos.database.table.ProductTable;
import com.synature.mpos.database.table.PromotionPriceGroupTable;
import com.synature.mpos.database.table.SessionDetailTable;
import com.synature.mpos.database.table.SessionTable;
import com.synature.mpos.database.table.ShopTable;

import android.content.Context;
import android.database.Cursor;

/*
 * This class to do generate SaleTransactionData
 * for send to HQ Server
 */
public class SaleTransaction extends MPOSDatabase{

	private Formater mFormat;
	
	private Shop mShop;
	
	/**
	 * for specific by transaction
	 */
	private int mTransactionId;
	
	/**
	 * session date for query transaction
	 */
	private String mSessionDate;
	
	/**
	 * List all transaction in session date?
	 */
	private boolean mIsAllTrans = false;
	
	public SaleTransaction(Context context, 
			String sessionDate, boolean isAllTrans) {
		super(context);
		mShop = new Shop(context);
		mFormat = new Formater(context);
		mSessionDate = sessionDate;
		mIsAllTrans = isAllTrans;
	}
	
	public SaleTransaction(Context context, String sessionDate, int transactionId){
		this(context, sessionDate, false);
		mTransactionId = transactionId;
	}
	
	public POSData_SaleTransaction listSaleTransaction() {
		POSData_SaleTransaction posSaleTrans = new POSData_SaleTransaction();
		SaleData_SessionInfo sessInfo = new SaleData_SessionInfo();

		sessInfo.setxTableSession(buildSessionObj());
		sessInfo.setxTableSessionEndDay(buildSessEnddayObj());
		posSaleTrans.setxArySaleTransaction(buildSaleTransLst());
		posSaleTrans.setxSessionInfo(sessInfo);
		return posSaleTrans;
	}

	public List<SaleData_SaleTransaction> buildSaleTransLst() {
		List<SaleData_SaleTransaction> saleTransLst = new ArrayList<SaleData_SaleTransaction>();
		Cursor cursor = queryTransactionNotSend();

		if(mTransactionId != 0)
			cursor = queryTransaction();
		else if(mIsAllTrans)
			cursor = queryAllTransactionInSessionDate();
		else
			cursor = queryTransactionNotSend();
		
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					SaleData_SaleTransaction saleTrans = new SaleData_SaleTransaction();
					SaleTable_OrderTransaction orderTrans = new SaleTable_OrderTransaction();
					
					orderTrans.setSzUDID(cursor.getString(cursor.getColumnIndex(COLUMN_UUID)));
					orderTrans.setiTransactionID(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_ID)));
					orderTrans.setiComputerID(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
					orderTrans.setiShopID(cursor.getInt(cursor.getColumnIndex(ShopTable.COLUMN_SHOP_ID)));
					orderTrans.setiOpenStaffID(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_OPEN_STAFF)));
					orderTrans.setDtOpenTime(mFormat.dateTimeFormat(cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_OPEN_TIME)), "yyyy-MM-dd HH:mm:ss"));
					orderTrans.setDtCloseTime(mFormat.dateTimeFormat(cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_CLOSE_TIME)), "yyyy-MM-dd HH:mm:ss"));
					orderTrans.setiDocType(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_DOC_TYPE_ID)));
					orderTrans.setiTransactionStatusID(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_STATUS_ID)));
					orderTrans.setiReceiptYear(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_YEAR)));
					orderTrans.setiReceiptMonth(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_MONTH)));
					orderTrans.setiReceiptID(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_ID)));
					orderTrans.setSzReceiptNo(cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_NO)));
					orderTrans.setDtSaleDate(mFormat.dateTimeFormat(cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_SALE_DATE)),"yyyy-MM-dd"));
					orderTrans.setfTransVAT(Utils.fixesDigitLength(mFormat, 4, cursor.getDouble(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_VAT))));
					orderTrans.setfTransactionVatable(Utils.fixesDigitLength(mFormat, 4, cursor.getDouble(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_VATABLE))));
					orderTrans.setiSessionID(cursor.getInt(cursor.getColumnIndex(SessionTable.COLUMN_SESS_ID)));
					orderTrans.setiVoidStaffID(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_VOID_STAFF_ID)));
					orderTrans.setSzVoidReason(cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_VOID_REASON)));
					orderTrans.setDtVoidTime(mFormat.dateTimeFormat(cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_VOID_TIME)), "yyyy-MM-dd HH:mm:ss"));
					orderTrans.setSzTransactionNote(cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_NOTE)));
					orderTrans.setiSaleMode(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_SALE_MODE)));
					orderTrans.setfVatPercent(Utils.fixesDigitLength(mFormat, 4,  cursor.getDouble(cursor.getColumnIndex(ProductTable.COLUMN_VAT_RATE))));
					orderTrans.setfTransactionExcludeVAT(Utils.fixesDigitLength(mFormat, 4, cursor.getDouble(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT))));
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
					payment.setiPaymentDetailID(cursor.getInt(cursor.getColumnIndex(PaymentDetailTable.COLUMN_PAY_ID)));
					payment.setiTransactionID(transId);
					payment.setiComputerID(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
					payment.setiShopID(mShop.getShopId());
					payment.setiPayTypeID(cursor.getInt(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_ID)));
					payment.setfPayAmount(Utils.fixesDigitLength(mFormat, 4, cursor.getDouble(cursor.getColumnIndex(PaymentDetailTable.COLUMN_PAY_AMOUNT))));
					payment.setSzCreditCardNo(cursor.getString(cursor.getColumnIndex(CreditCardTable.COLUMN_CREDITCARD_NO)));
					payment.setiExpireMonth(cursor.getInt(cursor.getColumnIndex(CreditCardTable.COLUMN_EXP_MONTH)));
					payment.setiExpireYear(cursor.getInt(cursor.getColumnIndex(CreditCardTable.COLUMN_EXP_YEAR)));
					payment.setiBankNameID(cursor.getInt(cursor.getColumnIndex(BankTable.COLUMN_BANK_ID)));
					payment.setiCreditCardType(cursor.getInt(cursor.getColumnIndex(CreditCardTable.COLUMN_CREDITCARD_TYPE_ID)));
					payment.setSzRemark(cursor.getString(cursor.getColumnIndex(PaymentDetailTable.COLUMN_REMARK)));
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
					if(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT)) > 0){

						SaleTable_OrderPromotion promotion = new SaleTable_OrderPromotion();
						promotion.setiTransactionID(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_ID)));
						promotion.setiComputerID(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
						promotion.setiShopID(mShop.getShopId());
						promotion.setiOrderDetailID(cursor.getInt(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_ID)));
						promotion.setfDiscountPrice(Utils.fixesDigitLength(mFormat, 4, cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT))));
						promotion.setfPriceAfterDiscount(Utils.fixesDigitLength(mFormat, 4, cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE))));
						promotion.setiDiscountTypeID(cursor.getInt(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_PROMOTION_TYPE_ID))); // 6 = other discount
						promotion.setiPromotionID(cursor.getInt(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_PRICE_GROUP_ID)));
						promotion.setSzCouponHeader(cursor.getString(cursor.getColumnIndex(PromotionPriceGroupTable.COLUMN_COUPON_HEADER)));
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
					int productTypeId = cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_TYPE_ID));
					if(productTypeId != Products.COMMENT_HAVE_PRICE && 
							productTypeId != Products.CHILD_OF_SET_HAVE_PRICE){
						SaleTable_OrderDetail order = new SaleTable_OrderDetail();
						order.setiOrderDetailID(cursor.getInt(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_ID)));
						order.setiTransactionID(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_ID)));
						order.setiComputerID(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
						order.setiShopID(mShop.getShopId());
						order.setiVatType(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_VAT_TYPE)));
						order.setiProductID(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID)));
						order.setiProductTypeID(productTypeId);
						order.setfQty(Utils.fixesDigitLength(mFormat, 4, cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_QTY))));
						order.setfPricePerUnit(Utils.fixesDigitLength(mFormat, 4, cursor.getDouble(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_PRICE))));
						order.setfRetailPrice(Utils.fixesDigitLength(mFormat, 4, cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE))));
						order.setfSalePrice(Utils.fixesDigitLength(mFormat, 4, cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE))));
						order.setfTotalVatAmount(Utils.fixesDigitLength(mFormat, 4, cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_VAT))));
						order.setfPriceDiscountAmount(Utils.fixesDigitLength(mFormat, 4, cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT))));
						order.setiSaleMode(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_SALE_MODE)));
						// order comment
						Cursor curOrderComm = queryOrderComment(order.getiTransactionID(), order.getiOrderDetailID());
						if(curOrderComm.moveToFirst()){
							do{
								SaleTable_CommentInfo comment = new SaleTable_CommentInfo();
								/**
								 * use parent_order_id of OrderCommentTable if have discount
								 * because discount of comment stored in OrderDetail
								 */
								int orderId = curOrderComm.getInt(curOrderComm.getColumnIndex(OrderDetailTable.COLUMN_ORDER_ID));
								double orderCommentDiscount = curOrderComm.getDouble(curOrderComm.getColumnIndex(OrderCommentTable.COLUMN_ORDER_COMMENT_PRICE_DISCOUNT));
								if(orderCommentDiscount > 0){
									orderId = curOrderComm.getInt(curOrderComm.getColumnIndex(OrderCommentTable.COLUMN_SELF_ORDER_ID));
								}
								comment.setiOrderID(orderId);
								comment.setiCommentID(curOrderComm.getInt(curOrderComm.getColumnIndex(MenuCommentTable.COLUMN_COMMENT_ID)));
								comment.setfCommentQty(Utils.fixesDigitLength(mFormat, 4, curOrderComm.getDouble(curOrderComm.getColumnIndex(OrderCommentTable.COLUMN_ORDER_COMMENT_QTY))));
								comment.setfCommentPrice(Utils.fixesDigitLength(mFormat, 4, curOrderComm.getDouble(curOrderComm.getColumnIndex(OrderCommentTable.COLUMN_ORDER_COMMENT_PRICE))));
								comment.setfDiscountPrice(Utils.fixesDigitLength(mFormat, 4, orderCommentDiscount));
								order.getxListCommentInfo().add(comment);
							}while(curOrderComm.moveToNext());
						}
						curOrderComm.close();
						// order set
						Cursor curOrderSet = queryOrderSet(order.getiTransactionID(), order.getiOrderDetailID());
						if(curOrderSet.moveToFirst()){
							do{
								SaleTable_ChildOrderType7 orderSet = 
										new SaleTable_ChildOrderType7();
								/**
								 * use parent_order_id of OrderSetTable if have discount
								 * because discount of OrderSet stored in OrderDetail
								 */
								int orderId = curOrderSet.getInt(curOrderSet.getColumnIndex(OrderDetailTable.COLUMN_ORDER_ID));
								double orderSetDiscount = curOrderSet.getDouble(curOrderSet.getColumnIndex(OrderSetTable.COLUMN_ORDER_SET_PRICE_DISCOUNT));
								if(orderSetDiscount > 0){
									orderId = curOrderSet.getInt(curOrderSet.getColumnIndex(OrderCommentTable.COLUMN_SELF_ORDER_ID));
								}
								orderSet.setiOrderID(orderId);
								orderSet.setiPGroupID(curOrderSet.getInt(curOrderSet.getColumnIndex(ProductComponentTable.COLUMN_PGROUP_ID)));
								orderSet.setiSetGroupNo(curOrderSet.getInt(curOrderSet.getColumnIndex(ProductComponentGroupTable.COLUMN_SET_GROUP_NO)));
								orderSet.setiProductID(curOrderSet.getInt(curOrderSet.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID)));
								orderSet.setfProductQty(Utils.fixesDigitLength(mFormat, 4, curOrderSet.getDouble(curOrderSet.getColumnIndex(OrderSetTable.COLUMN_ORDER_SET_QTY))));
								orderSet.setfProductPrice(Utils.fixesDigitLength(mFormat, 4, curOrderSet.getDouble(curOrderSet.getColumnIndex(OrderSetTable.COLUMN_ORDER_SET_PRICE))));
								orderSet.setfDiscountPrice(Utils.fixesDigitLength(mFormat, 4, orderSetDiscount));
								order.getxListChildOrderSetLinkType7().add(orderSet);
							}while(curOrderSet.moveToNext());
						}
						curOrderSet.close();
						orderDetailLst.add(order);
					}
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
					saleSessEnd.setDtSessionDate(mFormat.dateTimeFormat(cursor.getString(cursor.getColumnIndex(SessionTable.COLUMN_SESS_DATE)),"yyyy-MM-dd"));
					saleSessEnd.setDtEndDayDateTime(mFormat.dateTimeFormat(cursor.getString(cursor.getColumnIndex(SessionDetailTable.COLUMN_ENDDAY_DATE)),"yyyy-MM-dd HH:mm:ss"));
					saleSessEnd.setfTotalAmountReceipt(Utils.fixesDigitLength(mFormat, 4, cursor.getDouble(cursor.getColumnIndex(SessionDetailTable.COLUMN_TOTAL_AMOUNT_RECEIPT))));
					saleSessEnd.setiTotalQtyReceipt(cursor.getInt(cursor.getColumnIndex(SessionDetailTable.COLUMN_TOTAL_QTY_RECEIPT)));
				} while (cursor.moveToNext());
			} else {
				saleSessEnd.setDtSessionDate(mFormat.dateTimeFormat(mSessionDate, "yyyy-MM-dd"));
				saleSessEnd.setDtEndDayDateTime(mFormat.dateTimeFormat(mSessionDate, "yyyy-MM-dd HH:mm:ss"));
				saleSessEnd.setfTotalAmountReceipt(Utils.fixesDigitLength(mFormat, 4, 0.0d));
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
					saleSess.setiSessionID(cursor.getInt(cursor.getColumnIndex(SessionTable.COLUMN_SESS_ID)));
					saleSess.setiComputerID(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
					saleSess.setiShopID(cursor.getInt(cursor.getColumnIndex(ShopTable.COLUMN_SHOP_ID)));
					saleSess.setDtCloseSessionDateTime(mFormat.dateTimeFormat(cursor.getString(cursor.getColumnIndex(SessionTable.COLUMN_SESS_DATE)),"yyyy-MM-dd HH:mm:ss"));
					saleSess.setDtOpenSessionDateTime(mFormat.dateTimeFormat(cursor.getString(cursor.getColumnIndex(SessionTable.COLUMN_OPEN_DATE)),"yyyy-MM-dd HH:mm:ss"));
					saleSess.setDtCloseSessionDateTime(mFormat.dateTimeFormat(cursor.getString(cursor.getColumnIndex(SessionTable.COLUMN_CLOSE_DATE)),"yyyy-MM-dd HH:mm:ss"));
					saleSess.setDtSessionDate(mFormat.dateTimeFormat(cursor.getString(cursor.getColumnIndex(SessionTable.COLUMN_SESS_DATE)),"yyyy-MM-dd"));
					saleSess.setfOpenSessionAmount(Utils.fixesDigitLength(mFormat, 4,cursor.getDouble(cursor.getColumnIndex(SessionTable.COLUMN_OPEN_AMOUNT))));
					saleSess.setfCloseSessionAmount(Utils.fixesDigitLength(mFormat, 4,cursor.getDouble(cursor.getColumnIndex(SessionTable.COLUMN_CLOSE_AMOUNT))));
					saleSess.setiIsEndDaySession(cursor.getInt(cursor.getColumnIndex(SessionTable.COLUMN_IS_ENDDAY)));
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return saleSess;
	}

	public Cursor queryPaymentDetail(int transId) {
		return getReadableDatabase().query(
				PaymentDetailTable.TABLE_PAYMENT_DETAIL, 
				PaymentDetail.ALL_PAYMENT_DETAIL_COLUMNS, 
				OrderTransactionTable.COLUMN_TRANS_ID + "=?", 
				new String[]{
					String.valueOf(transId)
				}, null, null, null);
	}

	public Cursor queryOrderSet(int transactionId, int orderDetailId){
		return getReadableDatabase().rawQuery(
				"SELECT a." + OrderSetTable.COLUMN_ORDER_SET_ID + ","
				+ " a." + OrderDetailTable.COLUMN_ORDER_ID + ", "
				+ " a." + ProductTable.COLUMN_PRODUCT_ID + ", "
				+ " a." + OrderSetTable.COLUMN_ORDER_SET_QTY + ", "
				+ " a." + OrderSetTable.COLUMN_ORDER_SET_PRICE + ","
				+ " a." + ProductComponentTable.COLUMN_PGROUP_ID + ", "
				+ " a." + OrderSetTable.COLUMN_ORDER_SET_PRICE_DISCOUNT + ","
				+ " a." + OrderCommentTable.COLUMN_SELF_ORDER_ID + ", "
				+ " b." + ProductComponentGroupTable.COLUMN_SET_GROUP_NO
				+ " FROM " + OrderSetTable.TABLE_ORDER_SET + " a "
				+ " LEFT JOIN " + ProductComponentGroupTable.TABLE_PCOMPONENT_GROUP + " b "
				+ " ON a." + ProductComponentTable.COLUMN_PGROUP_ID + "=b." +  ProductComponentTable.COLUMN_PGROUP_ID
				+ " WHERE a." + OrderTransactionTable.COLUMN_TRANS_ID + "=?"
				+ " AND a." + OrderDetailTable.COLUMN_ORDER_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(orderDetailId)
				});
	}
	
	public Cursor queryOrderComment(int transactionId, int orderDetailId){
		return getReadableDatabase().query(
				OrderCommentTable.TABLE_ORDER_COMMENT, 
				Transaction.ALL_ORDER_COMMENT_COLUMNS, 
				OrderTransactionTable.COLUMN_TRANS_ID + "=?"
				+ " and " + OrderDetailTable.COLUMN_ORDER_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(orderDetailId)
				}, null, null, null);
	}
	
	public Cursor queryOrderDetail(int transId) {
		return getReadableDatabase().query(
				OrderDetailTable.TABLE_ORDER, 
				Transaction.ALL_ORDER_COLUMNS, 
				OrderTransactionTable.COLUMN_TRANS_ID + "=?", 
				new String[]{
					String.valueOf(transId)
				}, null, null, null);
	}
	
	public Cursor queryAllTransactionInSessionDate() {
		return getReadableDatabase().query(
				OrderTransactionTable.TABLE_ORDER_TRANS, 
				Transaction.ALL_TRANS_COLUMNS, 
				OrderTransactionTable.COLUMN_SALE_DATE + "=?" 
				+ " AND " + OrderTransactionTable.COLUMN_STATUS_ID + " IN(?,?) ",
				new String[] {
						mSessionDate,
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						String.valueOf(Transaction.TRANS_STATUS_VOID)
				}, null, null, null);
	}
	
	public Cursor queryTransactionNotSend() {
		return getReadableDatabase().query(
				OrderTransactionTable.TABLE_ORDER_TRANS, 
				Transaction.ALL_TRANS_COLUMNS, 
				OrderTransactionTable.COLUMN_SALE_DATE + "=?" + 
						" AND " + OrderTransactionTable.COLUMN_STATUS_ID + " IN(?,?) " + 
						" AND " + COLUMN_SEND_STATUS + "=?", 
				new String[] {
						mSessionDate,
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						String.valueOf(Transaction.TRANS_STATUS_VOID),
						String.valueOf(MPOSDatabase.NOT_SEND) 
				}, 
				null, null, null);
	}
	
	public Cursor queryTransaction() {
		return getReadableDatabase().query(
				OrderTransactionTable.TABLE_ORDER_TRANS, 
				Transaction.ALL_TRANS_COLUMNS, 
				OrderTransactionTable.COLUMN_TRANS_ID + "=?" + 
				" AND " + OrderTransactionTable.COLUMN_STATUS_ID + " IN(?,?) " + 
				" AND " + COLUMN_SEND_STATUS + "=?", 
				new String[] {
						String.valueOf(mTransactionId),
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						String.valueOf(Transaction.TRANS_STATUS_VOID),
						String.valueOf(MPOSDatabase.NOT_SEND) 
				}, 
				null, null, null);
	}

	public Cursor querySessionEndday() {
		return getReadableDatabase().query(
				SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL, 
				Session.ALL_SESS_ENDDAY_COLUMNS, 
				SessionTable.COLUMN_SESS_DATE + "=?", 
				new String[] {
					mSessionDate
				}, 
				null, null, null);
	}

	public Cursor querySession() {
		return getReadableDatabase().query(
				SessionTable.TABLE_SESSION, 
				Session.ALL_SESS_COLUMNS, 
				SessionTable.COLUMN_SESS_DATE + "=?", 
				new String[] {
					mSessionDate
				}, 
				null, null, null);
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
		private List<SaleTable_ChildOrderType7> xListChildOrderSetLinkType7 
			= new ArrayList<SaleTable_ChildOrderType7>();
		private List<SaleTable_CommentInfo> xListCommentInfo = 
				new ArrayList<SaleTable_CommentInfo>();
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

		public List<SaleTable_ChildOrderType7> getxListChildOrderSetLinkType7() {
			return xListChildOrderSetLinkType7;
		}

		public List<SaleTable_CommentInfo> getxListCommentInfo() {
			return xListCommentInfo;
		}

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

	public static class SaleTable_ChildOrderType7 {
		private int iOrderID;
		private int iProductID;
		private String fProductPrice;
		private String fDiscountPrice;
		private String fProductQty;
		private String szOrderComment;
		private int iPGroupID; // For child type 7
		private int iSetGroupNo;
		public int getiOrderID() {
			return iOrderID;
		}
		public void setiOrderID(int iOrderID) {
			this.iOrderID = iOrderID;
		}
		public int getiProductID() {
			return iProductID;
		}
		public void setiProductID(int iProductID) {
			this.iProductID = iProductID;
		}
		public String getfProductPrice() {
			return fProductPrice;
		}
		public void setfProductPrice(String fProductPrice) {
			this.fProductPrice = fProductPrice;
		}
		public String getfDiscountPrice() {
			return fDiscountPrice;
		}
		public void setfDiscountPrice(String fDiscountPrice) {
			this.fDiscountPrice = fDiscountPrice;
		}
		public String getfProductQty() {
			return fProductQty;
		}
		public void setfProductQty(String fProductQty) {
			this.fProductQty = fProductQty;
		}
		public String getSzOrderComment() {
			return szOrderComment;
		}
		public void setSzOrderComment(String szOrderComment) {
			this.szOrderComment = szOrderComment;
		}
		public int getiPGroupID() {
			return iPGroupID;
		}
		public void setiPGroupID(int iPGroupID) {
			this.iPGroupID = iPGroupID;
		}
		public int getiSetGroupNo() {
			return iSetGroupNo;
		}
		public void setiSetGroupNo(int iSetGroupNo) {
			this.iSetGroupNo = iSetGroupNo;
		}
	}
	
	public static class SaleTable_CommentInfo{
		private int iCommentID;
		private int iOrderID;
		private String fCommentQty;
		private String fCommentPrice;
		private String fDiscountPrice;
		public int getiCommentID() {
			return iCommentID;
		}
		public void setiCommentID(int iCommentID) {
			this.iCommentID = iCommentID;
		}
		public int getiOrderID() {
			return iOrderID;
		}
		public void setiOrderID(int iOrderID) {
			this.iOrderID = iOrderID;
		}
		public String getfCommentQty() {
			return fCommentQty;
		}
		public void setfCommentQty(String fCommentQty) {
			this.fCommentQty = fCommentQty;
		}
		public String getfCommentPrice() {
			return fCommentPrice;
		}
		public void setfCommentPrice(String fCommentPrice) {
			this.fCommentPrice = fCommentPrice;
		}
		public String getfDiscountPrice() {
			return fDiscountPrice;
		}
		public void setfDiscountPrice(String fDiscountPrice) {
			this.fDiscountPrice = fDiscountPrice;
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
		private String szCouponHeader;

		public String getSzCouponHeader() {
			return szCouponHeader;
		}

		public void setSzCouponHeader(String szCouponHeader) {
			this.szCouponHeader = szCouponHeader;
		}

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
