package com.syn.mpos.database;

import com.syn.mpos.database.Shop.ShopEntry;
import com.syn.mpos.database.Staff.StaffEntry;
import com.syn.mpos.database.Staff.StaffPermissionEntry;
import com.syn.mpos.database.StockDocument.DocumentTypeEntry;

public class MPOSSQL {
	public static final String BANK_SQL =
			"CREATE TABLE " + Bank.TABLE_BANK + " ( " +
			Bank.COLUMN_BANK_ID + " INTEGER, " +
			Bank.COLUMN_BANK_NAME + " TEXT, " +
			"PRIMARY KEY (" + Bank.COLUMN_BANK_ID + ") );";
	
	public static final String COMPUTER_SQL =
			"CREATE TABLE " + Computer.TABLE_COMPUTER + " ( " +
			Computer.COLUMN_COMPUTER_ID + " INTEGER, " +
			Computer.COLUMN_COMPUTER_NAME + " TEXT, " +
			Computer.COLUMN_DEVICE_CODE + " TEXT, " +
			Computer.COLUMN_REGISTER_NUMBER + " TEXT, " +
			Computer.COLUMN_IS_MAIN_COMPUTER + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + Computer.COLUMN_COMPUTER_ID + ") );";
	
	public static final String CREDIT_CARD_TYPE_SQL =
			"CREATE TABLE " + CreditCard.TABLE_CREDITCARD_TYPE + " ( " +
			CreditCard.COLUMN_CREDITCARD_TYPE_ID + " INTEGER, " +
			CreditCard.COLUMN_CREDITCARD_TYPE_NAME + " TEXT, " +
			" PRIMARY KEY (" + CreditCard.COLUMN_CREDITCARD_TYPE_ID + ") );";
	
	public static final String GLOBAL_PROPERTY_SQL =
			"CREATE TABLE " + GlobalProperty.TABLE_GLOBAL_PROPERTY + " ( " +
			GlobalProperty.COLUMN_CURRENCY_SYMBOL + " TEXT DEFAULT '$', " +
			GlobalProperty.COLUMN_CURRENCY_CODE + " TEXT DEFAULT 'USD', " +
			GlobalProperty.COLUMN_CURRENCY_NAME + " TEXT, " +
			GlobalProperty.COLUMN_CURRENCY_FORMAT + " TEXT DEFAULT '#,##0.00', " +
			GlobalProperty.COLUMN_QTY_FORMAT + " TEXT DEFAULT '#,##0', " +
			GlobalProperty.COLUMN_DATE_FORMAT + " TEXT DEFAULT 'd MMMM yyyy', " +
			GlobalProperty.COLUMN_TIME_FORMAT + " TEXT DEFAULT 'HH:mm:ss' );";
	
	public static final String LANGUAGE_SQL =
			"CREATE TABLE " + Language.TABLE_LANGUAGE + " ( " +
			Language.COLUMN_LANG_ID + " INTEGER DEFAULT 1, " +
			Language.COLUMN_LANG_NAME + " TEXT, " +
			Language.COLUMN_LANG_CODE + " TEXT DEFAULT 'en', " +
			"PRIMARY KEY (" + Language.COLUMN_LANG_ID + ") );";
	
	public static final String ORDER_SQL =
			"CREATE TABLE " + Transaction.TABLE_ORDER + " ( " +
			Transaction.COLUMN_ORDER_ID + " INTEGER, " +
			Transaction.COLUMN_TRANSACTION_ID + " INTEGER, " +
			Computer.COLUMN_COMPUTER_ID + " INTEGER, " +
			Products.COLUMN_PRODUCT_ID + " INTEGER, " +
			Products.COLUMN_PRODUCT_TYPE_ID + " INTEGER DEFAULT 1, " +
			Transaction.COLUMN_ORDER_QTY + " REAL DEFAULT 1, " +
			Products.COLUMN_PRODUCT_PRICE + " REAL DEFAULT 0, " +
			Transaction.COLUMN_DISCOUNT_TYPE + " INTEGER DEFAULT 2, " +
			Products.COLUMN_VAT_TYPE + " INTEGER DEFAULT 1, " +
			Transaction.COLUMN_TOTAL_VAT + " REAL DEFAULT 0, " +
			Transaction.COLUMN_TOTAL_VAT_EXCLUDE + " REAL DEFAULT 0, " +
			Transaction.COLUMN_MEMBER_DISCOUNT + " REAL DEFAULT 0, " +
			Transaction.COLUMN_PRICE_DISCOUNT + " REAL DEFAULT 0, " +
			Transaction.COLUMN_TOTAL_RETAIL_PRICE + " REAL DEFAULT 0, " +
			Transaction.COLUMN_TOTAL_SALE_PRICE + " REAL DEFAULT 0, " +
			Products.COLUMN_SALE_MODE + " INTEGER DEFAULT 1, " +
			"PRIMARY KEY (" + Transaction.COLUMN_ORDER_ID + " ASC, " +
			Transaction.COLUMN_TRANSACTION_ID + " ASC, " + Computer.COLUMN_COMPUTER_ID + " ASC) );";
	
	public static final String ORDER_TMP_SQL =
			"CREATE TABLE " + Transaction.TABLE_ORDER_TMP + " ( " +
			Transaction.COLUMN_ORDER_ID + " INTEGER, " +
			Transaction.COLUMN_TRANSACTION_ID + " INTEGER, " +
			Computer.COLUMN_COMPUTER_ID + " INTEGER, " +
			Products.COLUMN_PRODUCT_ID + " INTEGER, " +
			Products.COLUMN_PRODUCT_TYPE_ID + " INTEGER DEFAULT 1, " +
			Transaction.COLUMN_ORDER_QTY + " REAL DEFAULT 1, " +
			Products.COLUMN_PRODUCT_PRICE + " REAL DEFAULT 0, " +
			Transaction.COLUMN_DISCOUNT_TYPE + " INTEGER DEFAULT 2, " +
			Products.COLUMN_VAT_TYPE + " INTEGER DEFAULT 1, " +
			Transaction.COLUMN_TOTAL_VAT + " REAL DEFAULT 0, " +
			Transaction.COLUMN_TOTAL_VAT_EXCLUDE + " REAL DEFAULT 0, " +
			Transaction.COLUMN_MEMBER_DISCOUNT + " REAL DEFAULT 0, " +
			Transaction.COLUMN_PRICE_DISCOUNT + " REAL DEFAULT 0, " +
			Transaction.COLUMN_TOTAL_RETAIL_PRICE + " REAL DEFAULT 0, " +
			Transaction.COLUMN_TOTAL_SALE_PRICE + " REAL DEFAULT 0, " +
			Products.COLUMN_SALE_MODE + " INTEGER DEFAULT 1, " +
			"PRIMARY KEY (" + Transaction.COLUMN_ORDER_ID + " ASC, " +
			Transaction.COLUMN_TRANSACTION_ID + " ASC, " + Computer.COLUMN_COMPUTER_ID + " ASC) );";
	
	public static final String TRANSACTION_SQL =
			"CREATE TABLE " + Transaction.TABLE_TRANSACTION + " ( " +
			MPOSDatabase.COLUMN_UUID + " TEXT, " +
			Transaction.COLUMN_TRANSACTION_ID + " INTEGER, " +
			Computer.COLUMN_COMPUTER_ID + " INTEGER, " +
			ShopEntry.COLUMN_SHOP_ID + " INTEGER, " +
			Transaction.COLUMN_OPEN_TIME + " TEXT, " +
			Transaction.COLUMN_OPEN_STAFF + " INTEGER, " +
			Transaction.COLUMN_PAID_TIME + " TEXT, " +
			Transaction.COLUMN_PAID_STAFF_ID + " INTEGER, " +
			Transaction.COLUMN_CLOSE_TIME + " TEXT, " +
			Transaction.COLUMN_CLOSE_STAFF + " INTEGER, " +
			Transaction.COLUMN_STATUS_ID + " INTEGER DEFAULT 1, " +
			DocumentTypeEntry.COLUMN_DOC_TYPE + " INTEGER DEFAULT 8, " +
			Transaction.COLUMN_RECEIPT_YEAR + " INTEGER, " +
			Transaction.COLUMN_RECEIPT_MONTH + " INTEGER, " +
			Transaction.COLUMN_RECEIPT_ID + " INTEGER, " +
			Transaction.COLUMN_RECEIPT_NO + " TEXT, " +
			Transaction.COLUMN_SALE_DATE + " TEXT, " +
			Session.COLUMN_SESS_ID + " INTEGER, " +
			Transaction.COLUMN_VOID_STAFF_ID + " INTEGER, " +
			Transaction.COLUMN_VOID_REASON + " TEXT, " +
			Transaction.COLUMN_VOID_TIME + " TEXT, " +
			Transaction.COLUMN_MEMBER_ID + " INTEGER, " +
			Transaction.COLUMN_TRANS_VAT + " REAL DEFAULT 0, " +
			Transaction.COLUMN_TRANS_EXCLUDE_VAT + " REAL DEFAULT 0, " +
			Transaction.COLUMN_TRANS_VATABLE + " REAL DEFAULT 0, " +
			Transaction.COLUMN_TRANS_NOTE + " TEXT, " +
			Transaction.COLUMN_OTHER_DISCOUNT + " REAL DEFAULT 0, " +
			MPOSDatabase.COLUMN_SEND_STATUS + " INTEGER DEFAULT 0, " +
			Products.COLUMN_SALE_MODE + " INTEGER DEFAULT 1, " +
			"PRIMARY KEY (" + Transaction.COLUMN_TRANSACTION_ID + " ASC, " + Computer.COLUMN_COMPUTER_ID + " ASC) ); ";
	
	public static final String PRINT_RECEIPT_LOG_SQL = 
			"CREATE TABLE " + PrintReceiptLog.TABLE_PRINT_RECEIPT_LOG + "( " +
			PrintReceiptLog.COLUMN_PRINT_RECEIPT_LOG_ID + " INTEGER, " + 
			Transaction.COLUMN_TRANSACTION_ID + " INTEGER, " +
			Computer.COLUMN_COMPUTER_ID + " INTEGER, " +
			StaffEntry.COLUMN_STAFF_ID + " INTEGER, " +
			PrintReceiptLog.COLUMN_PRINT_RECEIPT_LOG_TIME + " TEXT, " +
			PrintReceiptLog.COLUMN_PRINT_RECEIPT_LOG_STATUS + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + PrintReceiptLog.COLUMN_PRINT_RECEIPT_LOG_ID + " AUTOINCREMENT ) );";
	
	public static final String PAYMENT_BUTTON_SQL = 
			"CREATE TABLE " + PaymentAmountButton.TABLE_PAYMENT_AMOUNT + "( " +
			PaymentAmountButton.COLUMN_PAYMENT_AMOUNT_ID + " INTEGER, " +
			PaymentAmountButton.COLUMN_PAYMENT_AMOUNT + " REAL DEFAULT 0, " +
			PaymentAmountButton.COLUMN_ORDERING + " INTEGER DEFAULT 0 );";
	
	public static final String PAYMENT_SQL =
			"CREATE TABLE " + PaymentDetail.TABLE_PAYMENT + " ( " +
					PaymentDetail.COLUMN_PAY_ID + " INTEGER, " +
			Transaction.COLUMN_TRANSACTION_ID + " INTEGER, " +
			Computer.COLUMN_COMPUTER_ID + " INTEGER, " +
			PaymentDetail.COLUMN_PAY_TYPE_ID + " INTEGER DEFAULT 1, " +
			PaymentDetail.COLUMN_PAY_AMOUNT + " REAL DEFAULT 0, " +
			PaymentDetail.COLUMN_PAID + " REAL DEFAULT 0, " +
			CreditCard.COLUMN_CREDITCARD_NO + " TEXT, " +
			CreditCard.COLUMN_EXP_MONTH + " INTEGER, " +
			CreditCard.COLUMN_EXP_YEAR + " INTEGER, " +
			Bank.COLUMN_BANK_ID + " INTEGER, " +
			CreditCard.COLUMN_CREDITCARD_TYPE_ID + " INTEGER, " +
			PaymentDetail.COLUMN_REMARK + " TEXT, " +
			"PRIMARY KEY (" + PaymentDetail.COLUMN_PAY_ID + " ASC, " + 
			Transaction.COLUMN_TRANSACTION_ID + " ASC, " + Computer.COLUMN_COMPUTER_ID + " ASC) );";
	
	public static final String PAY_TYPE_SQL =
			"CREATE TABLE " + PaymentDetail.TABLE_PAY_TYPE + " ( " +
			PaymentDetail.COLUMN_PAY_TYPE_ID + " INTEGER, " +
			PaymentDetail.COLUMN_PAY_TYPE_CODE + " TEXT, " +
			PaymentDetail.COLUMN_PAY_TYPE_NAME + " TEXT, " +
			PaymentDetail.COLUMN_ORDERING + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + PaymentDetail.COLUMN_PAY_TYPE_ID + ") );";
	
	public static final String PRODUCT_DEPT_SQL =
			"CREATE TABLE " + Products.TABLE_PRODUCT_DEPT + " ( " +
			Products.COLUMN_PRODUCT_DEPT_ID + " INTEGER, " +
			Products.COLUMN_PRODUCT_GROUP_ID + " INTEGER, " +
			Products.COLUMN_PRODUCT_DEPT_CODE + " TEXT, " +
			Products.COLUMN_PRODUCT_DEPT_NAME + " TEXT, " +
			Products.COLUMN_ACTIVATE + " INTEGER DEFAULT 0, " +
			Products.COLUMN_ORDERING + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + Products.COLUMN_PRODUCT_DEPT_ID + "));";
	
	public static final String PRODUCT_GROUP_SQL =
			"CREATE TABLE " + Products.TABLE_PRODUCT_GROUP + " ( " +
			Products.COLUMN_PRODUCT_GROUP_ID + " INTEGER, " +
			Products.COLUMN_PRODUCT_GROUP_CODE + " TEXT, " +
			Products.COLUMN_PRODUCT_GROUP_NAME + " TEXT, " +
			Products.COLUMN_PRODUCT_GROUP_TYPE + " INTEGER DEFAULT 0, " +
			Products.COLUMN_IS_COMMENT + " INTEGER DEFAULT 0, " +
			Products.COLUMN_ACTIVATE + " INTEGER DEFAULT 0, " +
			Products.COLUMN_ORDERING + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + Products.COLUMN_PRODUCT_GROUP_ID + "));";
	
	public static final String PCOMP_SQL =
			"CREATE TABLE " + Products.TABLE_PCOMP + " ( " +
					Products.COLUMN_PGROUP_ID + " INTEGER, " +
			Products.COLUMN_PRODUCT_ID + " INTEGER, " +
			Products.COLUMN_SALE_MODE + " INTEGER DEFAULT 0, " +
			Products.COLUMN_CHILD_PRODUCT_ID + " INTEGER, " +
			Products.COLUMN_CHILD_PRODUCT_AMOUNT + " REAL, " +
			Products.COLUMN_FLEXIBLE_PRODUCT_PRICE + " REAL DEFAULT 0, " +
			Products.COLUMN_FLEXIBLE_INCLUDE_PRICE + " INTEGER DEFAULT 0 " +
			");";
	
	public static final String PCOMP_GROUP_SQL =
			"CREATE TABLE " + Products.TABLE_PCOMP_GROUP + " ( " +
			Products.COLUMN_PGROUP_ID + " INTEGER, " +
			Products.COLUMN_PRODUCT_ID + " INTEGER, " +
			Products.COLUMN_SALE_MODE + " INTEGER DEFAULT 0, " +
			Products.COLUMN_SET_GROUP_NO + " TEXT, " +
			Products.COLUMN_SET_GROUP_NAME + " TEXT, " +
			Products.COLUMN_REQ_AMOUNT + " REAL DEFAULT 0 " +
			");";
	
	public static final String PRODUCT_SQL =
			"CREATE TABLE " + Products.TABLE_PRODUCT + " ( " +
			Products.COLUMN_PRODUCT_ID + " INTEGER, " +
			Products.COLUMN_PRODUCT_DEPT_ID + " INTEGER, " +
			Products.COLUMN_PRODUCT_CODE + " TEXT, " +
			Products.COLUMN_PRODUCT_BAR_CODE + " TEXT, " +
			Products.COLUMN_PRODUCT_NAME + " TEXT, " +
			Products.COLUMN_PRODUCT_DESC + " TEXT, " +
			Products.COLUMN_PRODUCT_TYPE_ID + " INTEGER DEFAULT 0, " +
			Products.COLUMN_PRODUCT_PRICE + " REAL DEFAULT 0, " +
			Products.COLUMN_PRODUCT_UNIT_NAME + " TEXT, " +
			Products.COLUMN_DISCOUNT_ALLOW + " INTEGER DEFAULT 1, " +
			Products.COLUMN_VAT_TYPE + " INTEGER DEFAULT 1, " +
			Products.COLUMN_VAT_RATE + " REAL DEFAULT 0, " +
			Products.COLUMN_ISOUTOF_STOCK + " INTEGER DEFAULT 0, " +
			Products.COLUMN_IMG_URL + " TEXT, " +
			Products.COLUMN_ACTIVATE + " INTEGER DEFAULT 0, " +
			Products.COLUMN_ORDERING + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + Products.COLUMN_PRODUCT_ID + " ASC));";
	
	public static final String SESSION_SQL =
			"CREATE TABLE " + Session.TABLE_SESSION + " ( " +
			Session.COLUMN_SESS_ID + " INTEGER, " +
			Computer.COLUMN_COMPUTER_ID + " INTEGER, " +
			ShopEntry.COLUMN_SHOP_ID + " INTEGER, " +
			Transaction.COLUMN_OPEN_STAFF + " INTEGER, " +
			Transaction.COLUMN_CLOSE_STAFF + " INTEGER, " +
			Session.COLUMN_SESS_DATE + " TEXT, " +
			Session.COLUMN_OPEN_DATE + " TEXT, " +
			Session.COLUMN_CLOSE_DATE + " TEXT, " +
			Session.COLUMN_OPEN_AMOUNT + " REAL, " +
			Session.COLUMN_CLOSE_AMOUNT + " REAL, " +
			Session.COLUMN_IS_ENDDAY + " INTEGER, " +
			"PRIMARY KEY (" + Session.COLUMN_SESS_ID + ", " + Computer.COLUMN_COMPUTER_ID + "));";
	
	public static final String SESSION_DETAIL_SQL =
			"CREATE TABLE " + Session.TABLE_SESSION_DETAIL + " ( " +
			Session.COLUMN_SESS_DATE + " TEXT, " +
			Session.COLUMN_ENDDAY_DATE + " TEXT, " +
			Session.COLUMN_TOTAL_QTY_RECEIPT + " INTEGER, " +
			Session.COLUMN_TOTAL_AMOUNT_RECEIPT + " REAL, " +
			Session.COLUMN_IS_SEND_TO_HQ + " INTEGER, " +
			Session.COLUMN_SEND_TO_HQ_DATE + " TEXT, " +
			"PRIMARY KEY (" + Session.COLUMN_SESS_DATE + "));";
	
	public static final String SHOP_SQL =
			"CREATE TABLE " + ShopEntry.TABLE_SHOP + " ( " +
			ShopEntry.COLUMN_SHOP_ID + " INTEGER, " +
			ShopEntry.COLUMN_SHOP_CODE + " TEXT, " +
			ShopEntry.COLUMN_SHOP_NAME + " TEXT, " +
			ShopEntry.COLUMN_SHOP_TYPE + " INTEGER, " +
			ShopEntry.COLUMN_VAT_TYPE + " INTEGER, " +
			ShopEntry.COLUMN_OPEN_HOUR + " TEXT, " +
			ShopEntry.COLUMN_CLOSE_HOUR + " TEXT, " +
			ShopEntry.COLUMN_COMPANY + " TEXT, " +
			ShopEntry.COLUMN_ADDR1 + " TEXT, " +
			ShopEntry.COLUMN_ADDR2 + " TEXT, " +
			ShopEntry.COLUMN_CITY + " TEXT, " +
			ShopEntry.COLUMN_PROVINCE_ID + " INTEGER, " +
			ShopEntry.COLUMN_ZIPCODE + " TEXT, " +
			ShopEntry.COLUMN_TELEPHONE + " TEXT, " +
			ShopEntry.COLUMN_FAX + " TEXT, " +
			ShopEntry.COLUMN_TAX_ID + " TEXT, " +
			ShopEntry.COLUMN_REGISTER_ID + " TEXT, " +
			ShopEntry.COLUMN_VAT + " REAL );";
	
	public static final String STAFF_PERMISSION_SQL =
			"CREATE TABLE " + StaffPermissionEntry.TABLE_STAFF_PERMISSION + " ( " +
			StaffPermissionEntry.COLUMN_STAFF_ROLE_ID + " INTEGER DEFAULT 0, " +
			StaffPermissionEntry.COLUMN_PERMMISSION_ITEM_ID + " INTEGER DEFAULT 0);";
	
	public static final String STAFF_SQL =
			"CREATE TABLE " + StaffEntry.TABLE_STAFF + " ( " +
			StaffEntry.COLUMN_STAFF_ID + " INTEGER, " +
			StaffEntry.COLUMN_STAFF_CODE + " TEXT, " +
			StaffEntry.COLUMN_STAFF_NAME + " TEXT, " +
			StaffEntry.COLUMN_STAFF_PASS + " TEXT, " +
			"PRIMARY KEY (" + StaffEntry.COLUMN_STAFF_ID + "));";
	
	public static final String SYNC_TRANSACTION_LOG_SQL =
			"CREATE TABLE " + SyncSaleLog.TABLE_SYNC_SALE_LOG + " ( " +
			Session.COLUMN_SESS_DATE + " TEXT, " + 
			SyncSaleLog.COLUMN_SYNC_STATUS + " INTEGER DEFAULT 0, " + 
			" PRIMARY KEY (" + Session.COLUMN_SESS_DATE + ")" + " );";
	
	public static final String HEAD_FOOD_RECEIPT_SQL =
			"CREATE TABLE " + HeaderFooterReceipt.TABLE_HEADER_FOOTER_RECEIPT + " ( " +
			HeaderFooterReceipt.COLUMN_TEXT_IN_LINE + " TEXT, " +
			HeaderFooterReceipt.COLUMN_LINE_TYPE + " INTEGER, " +
			HeaderFooterReceipt.COLUMN_LINE_ORDER + " INTEGER );";
}
