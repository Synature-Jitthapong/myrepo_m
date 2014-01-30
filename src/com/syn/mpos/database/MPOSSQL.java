package com.syn.mpos.database;

import com.syn.mpos.database.inventory.StockDocument;
import com.syn.mpos.database.transaction.PaymentDetail;
import com.syn.mpos.database.transaction.Session;
import com.syn.mpos.database.transaction.Transaction;

public class MPOSSQL {
	public static final String BANK_SQL =
			"CREATE TABLE " + Bank.TB_BANK + " ( " +
			Bank.COL_BANK_ID + " INTEGER, " +
			Bank.COL_BANK_NAME + " TEXT, " +
			"PRIMARY KEY (" + Bank.COL_BANK_ID + ") );";
	
	public static final String COMPUTER_SQL =
			"CREATE TABLE " + Computer.TB_COMPUTER + " ( " +
			Computer.COL_COMPUTER_ID + " INTEGER, " +
			Computer.COL_COMPUTER_NAME + " TEXT, " +
			Computer.COL_DEVICE_CODE + " TEXT, " +
			Computer.COL_REGISTER_NUMBER + " TEXT, " +
			Computer.COL_IS_MAIN_COMPUTER + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + Computer.COL_COMPUTER_ID + ") );";
	
	public static final String CREDIT_CARD_TYPE_SQL =
			"CREATE TABLE " + CreditCard.TB_CREDIT_CARD_TYPE + " ( " +
			CreditCard.COL_CREDIT_CARD_TYPE_ID + " INTEGER, " +
			CreditCard.COL_CREDIT_CARD_TYPE_NAME + " TEXT, " +
			" PRIMARY KEY (" + CreditCard.COL_CREDIT_CARD_TYPE_ID + ") );";
	
	public static final String DOC_DETAIL_SQL =
			"CREATE TABLE " + StockDocument.TB_DOC_DETAIL + " ( " +
			StockDocument.COL_DOC_DETAIL_ID + " INTEGER, " +
			StockDocument.COL_DOC_ID + " INTEGER, " +
			Shop.COL_SHOP_ID + " INTEGER, " +
			Products.COL_PRODUCT_ID + " INTEGER, " +
			StockDocument.COL_PRODUCT_AMOUNT + " REAL DEFAULT 0, " +
			Products.COL_PRODUCT_UNIT_NAME + " TEXT, " + 
			Products.COL_PRODUCT_PRICE + " REAL DEFAULT 0, " +
			"PRIMARY KEY (" + StockDocument.COL_DOC_DETAIL_ID + " ASC, " + 
			StockDocument.COL_DOC_ID + " ASC, " + Shop.COL_SHOP_ID + " ASC) );";
	
	public static final String DOCUMENT_SQL =
			"CREATE TABLE " + StockDocument.TB_DOCUMENT + " ( " +
			StockDocument.COL_DOC_ID + " INTEGER, " +
			Shop.COL_SHOP_ID + " INTEGER, " +
			StockDocument.COL_REF_DOC_ID + " INTEGER, " +
			StockDocument.COL_REF_SHOP_ID + " INTEGER, " +
			StockDocument.COL_DOC_TYPE + " INTEGER, " +
			StockDocument.COL_DOC_NO + " TEXT, " +
			StockDocument.COL_DOC_DATE + " TEXT, " +
			StockDocument.COL_DOC_YEAR + " INTEGER, " +
			StockDocument.COL_DOC_MONTH + " INTEGER, " +
			StockDocument.COL_UPDATE_BY + " INTEGER, " +
			StockDocument.COL_UPDATE_DATE + " TEXT, " +
			StockDocument.COL_DOC_STATUS + " INTEGER DEFAULT 1, " +
			StockDocument.COL_REMARK + " TEXT, " +
			StockDocument.COL_IS_SEND_TO_HQ + " INTEGER DEFAULT 0, " +
			StockDocument.COL_IS_SEND_TO_HQ_DATE + " TEXT, " +
			"PRIMARY KEY (" + StockDocument.COL_DOC_ID + " ASC, " + Shop.COL_SHOP_ID + " ASC) );"; 
	
	public static final String DOCUMENT_TYPE_SQL =
			"CREATE TABLE " + StockDocument.TB_DOCUMENT_TYPE + " ( " +
			StockDocument.COL_DOC_TYPE + " INTEGER, " +
			StockDocument.COL_DOC_TYPE_HEADER + " TEXT, " +
			StockDocument.COL_DOC_TYPE_NAME + " TEXT, " +
			StockDocument.COL_MOVE_MENT + " INTEGER DEFAULT 0 );";
	
	public static final String GLOBAL_PROPERTY_SQL =
			"CREATE TABLE " + GlobalProperty.TB_GLOBAL_PROPERTY + " ( " +
			GlobalProperty.COL_CURRENCY_SYMBOL + " TEXT DEFAULT '$', " +
			GlobalProperty.COL_CURRENCY_CODE + " TEXT DEFAULT 'USD', " +
			GlobalProperty.COL_CURRENCY_NAME + " TEXT, " +
			GlobalProperty.COL_CURRENCY_FORMAT + " TEXT DEFAULT '#,##0.00', " +
			GlobalProperty.COL_QTY_FORMAT + " TEXT DEFAULT '#,##0', " +
			GlobalProperty.COL_DATE_FORMAT + " TEXT DEFAULT 'd MMMM yyyy', " +
			GlobalProperty.COL_TIME_FORMAT + " TEXT DEFAULT 'HH:mm:ss' );";
	
	public static final String LANGUAGE_SQL =
			"CREATE TABLE " + Language.TB_LANGUAGE + " ( " +
			Language.COL_LANG_ID + " INTEGER DEFAULT 1, " +
			Language.COL_LANG_NAME + " TEXT, " +
			Language.COL_LANG_CODE + " TEXT DEFAULT 'en', " +
			"PRIMARY KEY (" + Language.COL_LANG_ID + ") );";
	
	public static final String ORDER_SQL =
			"CREATE TABLE " + Transaction.TB_ORDER + " ( " +
			Transaction.COL_ORDER_ID + " INTEGER, " +
			Transaction.COL_TRANS_ID + " INTEGER, " +
			Computer.COL_COMPUTER_ID + " INTEGER, " +
			Products.COL_PRODUCT_ID + " INTEGER, " +
			Products.COL_PRODUCT_TYPE_ID + " INTEGER DEFAULT 1, " +
			Transaction.COL_ORDER_QTY + " REAL DEFAULT 1, " +
			Products.COL_PRODUCT_PRICE + " REAL DEFAULT 0, " +
			Transaction.COL_DISCOUNT_TYPE + " INTEGER DEFAULT 1, " +
			Products.COL_VAT_TYPE + " INTEGER DEFAULT 1, " +
			Transaction.COL_TOTAL_VAT + " REAL DEFAULT 0, " +
			Transaction.COL_TOTAL_VAT_EXCLUDE + " REAL DEFAULT 0, " +
			Transaction.COL_MEMBER_DISCOUNT + " REAL DEFAULT 0, " +
			Transaction.COL_PRICE_DISCOUNT + " REAL DEFAULT 0, " +
			Transaction.COL_TOTAL_RETAIL_PRICE + " REAL DEFAULT 0, " +
			Transaction.COL_TOTAL_SALE_PRICE + " REAL DEFAULT 0, " +
			"PRIMARY KEY (" + Transaction.COL_ORDER_ID + " ASC, " +
			Transaction.COL_TRANS_ID + " ASC, " + Computer.COL_COMPUTER_ID + " ASC) );";
	
	public static final String ORDER_TMP_SQL =
			"CREATE TABLE " + Transaction.TB_ORDER_TMP + " ( " +
			Transaction.COL_ORDER_ID + " INTEGER, " +
			Transaction.COL_TRANS_ID + " INTEGER, " +
			Computer.COL_COMPUTER_ID + " INTEGER, " +
			Products.COL_PRODUCT_ID + " INTEGER, " +
			Products.COL_PRODUCT_TYPE_ID + " INTEGER DEFAULT 1, " +
			Transaction.COL_ORDER_QTY + " REAL DEFAULT 1, " +
			Products.COL_PRODUCT_PRICE + " REAL DEFAULT 0, " +
			Transaction.COL_DISCOUNT_TYPE + " INTEGER DEFAULT 1, " +
			Products.COL_VAT_TYPE + " INTEGER DEFAULT 1, " +
			Transaction.COL_TOTAL_VAT + " REAL DEFAULT 0, " +
			Transaction.COL_TOTAL_VAT_EXCLUDE + " REAL DEFAULT 0, " +
			Transaction.COL_MEMBER_DISCOUNT + " REAL DEFAULT 0, " +
			Transaction.COL_PRICE_DISCOUNT + " REAL DEFAULT 0, " +
			Transaction.COL_TOTAL_RETAIL_PRICE + " REAL DEFAULT 0, " +
			Transaction.COL_TOTAL_SALE_PRICE + " REAL DEFAULT 0, " +
			"PRIMARY KEY (" + Transaction.COL_ORDER_ID + " ASC, " +
			Transaction.COL_TRANS_ID + " ASC, " + Computer.COL_COMPUTER_ID + " ASC) );";
	
	public static final String TRANSACTION_SQL =
			"CREATE TABLE " + Transaction.TB_TRANS + " ( " +
			MPOSDatabase.COL_UUID + " TEXT, " +
			Transaction.COL_TRANS_ID + " INTEGER, " +
			Computer.COL_COMPUTER_ID + " INTEGER, " +
			Shop.COL_SHOP_ID + " INTEGER, " +
			Transaction.COL_OPEN_TIME + " TEXT, " +
			Transaction.COL_OPEN_STAFF + " INTEGER, " +
			Transaction.COL_PAID_TIME + " TEXT, " +
			Transaction.COL_PAID_STAFF_ID + " INTEGER, " +
			Transaction.COL_CLOSE_TIME + " TEXT, " +
			Transaction.COL_CLOSE_STAFF + " INTEGER, " +
			Transaction.COL_STATUS_ID + " INTEGER DEFAULT 1, " +
			StockDocument.COL_DOC_TYPE + " INTEGER DEFAULT 8, " +
			Transaction.COL_RECEIPT_YEAR + " INTEGER, " +
			Transaction.COL_RECEIPT_MONTH + " INTEGER, " +
			Transaction.COL_RECEIPT_ID + " INTEGER, " +
			Transaction.COL_RECEIPT_NO + " TEXT, " +
			Transaction.COL_SALE_DATE + " TEXT, " +
			Session.COL_SESS_ID + " INTEGER, " +
			Transaction.COL_VOID_STAFF_ID + " INTEGER, " +
			Transaction.COL_VOID_REASON + " TEXT, " +
			Transaction.COL_VOID_TIME + " TEXT, " +
			Transaction.COL_MEMBER_ID + " INTEGER, " +
			Transaction.COL_TRANS_VAT + " REAL DEFAULT 0, " +
			Transaction.COL_TRANS_EXCLUDE_VAT + " REAL DEFAULT 0, " +
			Transaction.COL_TRANS_VATABLE + " REAL DEFAULT 0, " +
			Transaction.COL_TRANS_NOTE + " TEXT, " +
			Transaction.COL_OTHER_DISCOUNT + " REAL DEFAULT 0, " +
			MPOSDatabase.COL_SEND_STATUS + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + Transaction.COL_TRANS_ID + " ASC, " + Computer.COL_COMPUTER_ID + " ASC) ); ";
	
	public static final String PAYMENT_SQL =
			"CREATE TABLE " + PaymentDetail.TB_PAYMENT + " ( " +
			PaymentDetail.COL_PAY_ID + " INTEGER, " +
			Transaction.COL_TRANS_ID + " INTEGER, " +
			Computer.COL_COMPUTER_ID + " INTEGER, " +
			PaymentDetail.COL_PAY_TYPE_ID + " INTEGER DEFAULT 1, " +
			PaymentDetail.COL_PAY_AMOUNT + " REAL DEFAULT 0, " +
			CreditCard.COL_CREDIT_CARD_NO + " TEXT, " +
			CreditCard.COL_EXP_MONTH + " INTEGER, " +
			CreditCard.COL_EXP_YEAR + " INTEGER, " +
			Bank.COL_BANK_ID + " INTEGER, " +
			CreditCard.COL_CREDIT_CARD_TYPE_ID + " INTEGER, " +
			PaymentDetail.COL_REMARK + " TEXT, " +
			"PRIMARY KEY (" + PaymentDetail.COL_PAY_ID + " ASC, " + 
			Transaction.COL_TRANS_ID + " ASC, " + Computer.COL_COMPUTER_ID + " ASC) );";
	
	public static final String PAY_TYPE_SQL =
			"CREATE TABLE " + PaymentDetail.TB_PAY_TYPE + " ( " +
			PaymentDetail.COL_PAY_TYPE_ID + " INTEGER, " +
			PaymentDetail.COL_PAY_TYPE_CODE + " TEXT, " +
			PaymentDetail.COL_PAY_TYPE_NAME + " TEXT, " +
			"PRIMARY KEY (" + PaymentDetail.COL_PAY_TYPE_ID + ") );";
	
	public static final String PRODUCT_DEPT_SQL =
			"CREATE TABLE " + Products.TB_PRODUCT_DEPT + " ( " +
			Products.COL_PRODUCT_DEPT_ID + " INTEGER, " +
			Products.COL_PRODUCT_GROUP_ID + " INTEGER, " +
			Products.COL_PRODUCT_DEPT_CODE + " TEXT, " +
			Products.COL_PRODUCT_DEPT_NAME + " TEXT, " +
			Products.COL_ACTIVATE + " INTEGER DEFAULT 0, " +
			Products.COL_ORDERING + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + Products.COL_PRODUCT_DEPT_ID + "));";
	
	public static final String PRODUCT_GROUP_SQL =
			"CREATE TABLE " + Products.TB_PRODUCT_GROUP + " ( " +
			Products.COL_PRODUCT_GROUP_ID + " INTEGER, " +
			Products.COL_PRODUCT_GROUP_CODE + " TEXT, " +
			Products.COL_PRODUCT_GROUP_NAME + " TEXT, " +
			Products.COL_PRODUCT_GROUP_TYPE + " INTEGER DEFAULT 0, " +
			Products.COL_IS_COMMENT + " INTEGER DEFAULT 0, " +
			Products.COL_ACTIVATE + " INTEGER DEFAULT 0, " +
			Products.COL_ORDERING + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + Products.COL_PRODUCT_GROUP_ID + "));";
	
	public static final String PCOMP_SET_SQL =
			"CREATE TABLE " + Products.TB_PCOMP_SET + " ( " +
			Products.COL_PGROUP_ID + " INTEGER, " +
			Products.COL_PRODUCT_ID + " INTEGER, " +
			Products.COL_CHILD_PRODUCT_ID + " INTEGER, " +
			Products.COL_CHILD_PRODUCT_AMOUNT + " REAL, " +
			Products.COL_FLEXIBLE_PRODUCT_PRICE + " REAL DEFAULT 0, " +
			Products.COL_FLEXIBLE_INCLUDE_PRICE + " INTEGER DEFAULT 0 " +
			");";
	
	public static final String PRODUCT_SQL =
			"CREATE TABLE " + Products.TB_PRODUCT + " ( " +
			Products.COL_PRODUCT_ID + " INTEGER, " +
			Products.COL_PRODUCT_DEPT_ID + " INTEGER, " +
			Products.COL_PRODUCT_GROUP_ID + " INTEGER, " +
			Products.COL_PRODUCT_CODE + " TEXT, " +
			Products.COL_PRODUCT_BAR_CODE + " TEXT, " +
			Products.COL_PRODUCT_NAME + " TEXT, " +
			Products.COL_PRODUCT_DESC + " TEXT, " +
			Products.COL_PRODUCT_TYPE_ID + " INTEGER DEFAULT 0, " +
			Products.COL_PRODUCT_PRICE + " REAL DEFAULT 0, " +
			Products.COL_PRODUCT_UNIT_NAME + " TEXT, " +
			Products.COL_DISCOUNT_ALLOW + " INTEGER DEFAULT 1, " +
			Products.COL_VAT_TYPE + " INTEGER DEFAULT 1, " +
			Products.COL_VAT_RATE + " REAL DEFAULT 0, " +
			Products.COL_IS_OUTOF_STOCK + " INTEGER DEFAULT 0, " +
			Products.COL_IMG_URL + " TEXT, " +
			Products.COL_ACTIVATE + " INTEGER DEFAULT 0, " +
			Products.COL_ORDERING + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + Products.COL_PRODUCT_ID + " ASC));";
	
	public static final String PROVINCE_SQL =
			"CREATE TABLE " + Province.TB_PROVINCE + " ( " +
			Province.COL_PROVINCE_ID + " INTEGER, " +
			Province.COL_PROVINCE_NAME + " TEXT, " +
			"PRIMARY KEY (" + Province.COL_PROVINCE_ID + "));";
	
	public static final String SESSION_SQL =
			"CREATE TABLE " + Session.TB_SESSION + " ( " +
			Session.COL_SESS_ID + " INTEGER, " +
			Computer.COL_COMPUTER_ID + " INTEGER, " +
			Shop.COL_SHOP_ID + " INTEGER, " +
			Session.COL_OPEN_STAFF + " INTEGER, " +
			Session.COL_CLOSE_STAFF + " INTEGER, " +
			Session.COL_SESS_DATE + " TEXT, " +
			Session.COL_OPEN_DATE + " TEXT, " +
			Session.COL_CLOSE_DATE + " TEXT, " +
			Session.COL_OPEN_AMOUNT + " REAL, " +
			Session.COL_CLOSE_AMOUNT + " REAL, " +
			Session.COL_IS_ENDDAY + " INTEGER, " +
			"PRIMARY KEY (" + Session.COL_SESS_ID + ", " + Computer.COL_COMPUTER_ID + "));";
	
	public static final String SESSION_DETAIL_SQL =
			"CREATE TABLE " + Session.TB_SESSION_DETAIL + " ( " +
			Session.COL_SESS_DATE + " TEXT, " +
			Session.COL_ENDDAY_DATE + " TEXT, " +
			Session.COL_TOTAL_QTY_RECEIPT + " INTEGER, " +
			Session.COL_TOTAL_AMOUNT_RECEIPT + " REAL, " +
			Session.COL_IS_SEND_TO_HQ + " INTEGER, " +
			Session.COL_SEND_TO_HQ_DATE + " TEXT, " +
			"PRIMARY KEY (" + Session.COL_SESS_DATE + "));";
	
	public static final String SHOP_SQL =
			"CREATE TABLE " + Shop.TB_SHOP + " ( " +
			Shop.COL_SHOP_ID + " INTEGER, " +
			Shop.COL_SHOP_CODE + " TEXT, " +
			Shop.COL_SHOP_NAME + " TEXT, " +
			Shop.COL_SHOP_TYPE + " INTEGER, " +
			Shop.COL_VAT_TYPE + " INTEGER, " +
			Shop.COL_OPEN_HOUR + " TEXT, " +
			Shop.COL_CLOSE_HOUR + " TEXT, " +
			Shop.COL_COMPANY + " TEXT, " +
			Shop.COL_ADDR1 + " TEXT, " +
			Shop.COL_ADDR2 + " TEXT, " +
			Shop.COL_CITY + " TEXT, " +
			Province.COL_PROVINCE_ID + " INTEGER, " +
			Shop.COL_ZIPCODE + " TEXT, " +
			Shop.COL_TELEPHONE + " TEXT, " +
			Shop.COL_FAX + " TEXT, " +
			Shop.COL_TAX_ID + " TEXT, " +
			Shop.COL_REGISTER_ID + " TEXT, " +
			Shop.COL_VAT + " REAL );";
	
	public static final String STAFF_PERMISSION_SQL =
			"CREATE TABLE " + Staff.TB_STAFF_PERMISSION + " ( " +
			Staff.COL_STAFF_ROLE_ID + " INTEGER DEFAULT 0, " +
			Staff.COL_PERMMISSION_ITEM_ID + " INTEGER DEFAULT 0);";
	
	public static final String STAFF_SQL =
			"CREATE TABLE " + Staff.TB_STAFF + " ( " +
			Staff.COL_STAFF_ID + " INTEGER, " +
			Staff.COL_STAFF_CODE + " TEXT, " +
			Staff.COL_STAFF_NAME + " TEXT, " +
			Staff.COL_STAFF_PASS + " TEXT, " +
			"PRIMARY KEY (" + Staff.COL_STAFF_ID + "));";
	
	public static final String SYNC_TRANSACTION_LOG_SQL =
			"CREATE TABLE " + SyncSaleLog.TB_SYNC_SALE_LOG + " ( " +
			Session.COL_SESS_DATE + " TEXT, " + 
			SyncSaleLog.COL_SYNC_STATUS + " INTEGER DEFAULT 0, " + 
			" PRIMARY KEY (" + Session.COL_SESS_DATE + ")" + " );";
	
	public static final String HEAD_FOOD_RECEIPT_SQL =
			"CREATE TABLE " + HeaderFooterReceipt.TB_HEADER_FOOTER_RECEIPT + " ( " +
			HeaderFooterReceipt.COL_TEXT_IN_LINE + " TEXT, " +
			HeaderFooterReceipt.COL_LINE_TYPE + " INTEGER, " +
			HeaderFooterReceipt.COL_LINE_ORDER + " INTEGER );";
}
