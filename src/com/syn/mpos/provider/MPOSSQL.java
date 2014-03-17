package com.syn.mpos.provider;

import com.syn.mpos.provider.Computer.ComputerEntry;
import com.syn.mpos.provider.PrintReceiptLog.PrintLogEntry;
import com.syn.mpos.provider.Products.ComponentGroupEntry;
import com.syn.mpos.provider.Products.ComponentSetEntry;
import com.syn.mpos.provider.Products.ProductDeptEntry;
import com.syn.mpos.provider.Products.ProductEntry;
import com.syn.mpos.provider.Products.ProductGroupEntry;
import com.syn.mpos.provider.Session.SessionEntry;
import com.syn.mpos.provider.Transaction.OrderDetailEntry;
import com.syn.mpos.provider.Transaction.TransactionEntry;

public class MPOSSQL {
	public static final String BANK_SQL =
			"CREATE TABLE " + Bank.TABLE_BANK + " ( " +
			Bank.COLUMN_BANK_ID + " INTEGER, " +
			Bank.COLUMN_BANK_NAME + " TEXT, " +
			"PRIMARY KEY (" + Bank.COLUMN_BANK_ID + ") );";
	
	public static final String COMPUTER_SQL =
			"CREATE TABLE " + ComputerEntry.TABLE_COMPUTER + " ( " +
			ComputerEntry.COLUMN_COMPUTER_ID + " INTEGER, " +
			ComputerEntry.COLUMN_COMPUTER_NAME + " TEXT, " +
			ComputerEntry.COLUMN_DEVICE_CODE + " TEXT, " +
			ComputerEntry.COLUMN_REGISTER_NUMBER + " TEXT, " +
			ComputerEntry.COLUMN_IS_MAIN_COMPUTER + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + ComputerEntry.COLUMN_COMPUTER_ID + ") );";
	
	public static final String CREDIT_CARD_TYPE_SQL =
			"CREATE TABLE " + CreditCard.TABLE_CREDITCARD_TYPE + " ( " +
			CreditCard.COLUMN_CREDITCARD_TYPE_ID + " INTEGER, " +
			CreditCard.COLUMN_CREDITCARD_TYPE_NAME + " TEXT, " +
			" PRIMARY KEY (" + CreditCard.COLUMN_CREDITCARD_TYPE_ID + ") );";
	
	public static final String DOC_DETAIL_SQL =
			"CREATE TABLE " + StockDocument.TABLE_DOC_DETAIL + " ( " +
			StockDocument.COLUMN_DOC_DETAIL_ID + " INTEGER, " +
			StockDocument.COLUMN_DOC_ID + " INTEGER, " +
			Shop.COLUMN_SHOP_ID + " INTEGER, " +
			ProductEntry.COLUMN_PRODUCT_ID + " INTEGER, " +
			StockDocument.COLUMN_PRODUCT_AMOUNT + " REAL DEFAULT 0, " +
			ProductEntry.COLUMN_PRODUCT_UNIT_NAME + " TEXT, " + 
			ProductEntry.COLUMN_PRODUCT_PRICE + " REAL DEFAULT 0, " +
			"PRIMARY KEY (" + StockDocument.COLUMN_DOC_DETAIL_ID + " ASC, " + 
			StockDocument.COLUMN_DOC_ID + " ASC, " + Shop.COLUMN_SHOP_ID + " ASC) );";
	
	public static final String DOCUMENT_SQL =
			"CREATE TABLE " + StockDocument.TABLE_DOCUMENT + " ( " +
			StockDocument.COLUMN_DOC_ID + " INTEGER, " +
			Shop.COLUMN_SHOP_ID + " INTEGER, " +
			StockDocument.COLUMN_REF_DOC_ID + " INTEGER, " +
			StockDocument.COLUMN_REF_SHOP_ID + " INTEGER, " +
			StockDocument.COLUMN_DOC_TYPE + " INTEGER, " +
			StockDocument.COLUMN_DOC_NO + " TEXT, " +
			StockDocument.COLUMN_DOC_DATE + " TEXT, " +
			StockDocument.COLUMN_DOC_YEAR + " INTEGER, " +
			StockDocument.COLUMN_DOC_MONTH + " INTEGER, " +
			StockDocument.COLUMN_UPDATE_BY + " INTEGER, " +
			StockDocument.COLUMN_UPDATE_DATE + " TEXT, " +
			StockDocument.COLUMN_DOC_STATUS + " INTEGER DEFAULT 1, " +
			StockDocument.COLUMN_REMARK + " TEXT, " +
			StockDocument.COLUMN_IS_SEND_TO_HQ + " INTEGER DEFAULT 0, " +
			StockDocument.COLUMN_IS_SEND_TO_HQ_DATE + " TEXT, " +
			"PRIMARY KEY (" + StockDocument.COLUMN_DOC_ID + " ASC, " + Shop.COLUMN_SHOP_ID + " ASC) );"; 
	
	public static final String DOCUMENT_TYPE_SQL =
			"CREATE TABLE " + StockDocument.TABLE_DOCUMENT_TYPE + " ( " +
			StockDocument.COLUMN_DOC_TYPE + " INTEGER, " +
			StockDocument.COLUMN_DOC_TYPE_HEADER + " TEXT, " +
			StockDocument.COLUMN_DOC_TYPE_NAME + " TEXT, " +
			StockDocument.COLUMN_MOVE_MENT + " INTEGER DEFAULT 0 );";
	
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
			"CREATE TABLE " + OrderDetailEntry.TABLE_ORDER + " ( " +
			OrderDetailEntry.COLUMN_ORDER_ID + " INTEGER, " +
			TransactionEntry.COLUMN_TRANSACTION_ID + " INTEGER, " +
			ComputerEntry.COLUMN_COMPUTER_ID + " INTEGER, " +
			ProductEntry.COLUMN_PRODUCT_ID + " INTEGER, " +
			ProductEntry.COLUMN_PRODUCT_TYPE_ID + " INTEGER DEFAULT 1, " +
			OrderDetailEntry.COLUMN_ORDER_QTY + " REAL DEFAULT 1, " +
			ProductEntry.COLUMN_PRODUCT_PRICE + " REAL DEFAULT 0, " +
			OrderDetailEntry.COLUMN_DISCOUNT_TYPE + " INTEGER DEFAULT 2, " +
			ProductEntry.COLUMN_VAT_TYPE + " INTEGER DEFAULT 1, " +
			OrderDetailEntry.COLUMN_TOTAL_VAT + " REAL DEFAULT 0, " +
			OrderDetailEntry.COLUMN_TOTAL_VAT_EXCLUDE + " REAL DEFAULT 0, " +
			OrderDetailEntry.COLUMN_MEMBER_DISCOUNT + " REAL DEFAULT 0, " +
			OrderDetailEntry.COLUMN_PRICE_DISCOUNT + " REAL DEFAULT 0, " +
			OrderDetailEntry.COLUMN_TOTAL_RETAIL_PRICE + " REAL DEFAULT 0, " +
			OrderDetailEntry.COLUMN_TOTAL_SALE_PRICE + " REAL DEFAULT 0, " +
			OrderDetailEntry.COLUMN_SALE_MODE + " INTEGER DEFAULT 1, " +
			"PRIMARY KEY (" + OrderDetailEntry.COLUMN_ORDER_ID + " ASC, " +
			TransactionEntry.COLUMN_TRANSACTION_ID + " ASC, " + ComputerEntry.COLUMN_COMPUTER_ID + " ASC) );";
	
	public static final String ORDER_TMP_SQL =
			"CREATE TABLE " + OrderDetailEntry.TABLE_ORDER_TMP + " ( " +
			OrderDetailEntry.COLUMN_ORDER_ID + " INTEGER, " +
			TransactionEntry.COLUMN_TRANSACTION_ID + " INTEGER, " +
			ComputerEntry.COLUMN_COMPUTER_ID + " INTEGER, " +
			ProductEntry.COLUMN_PRODUCT_ID + " INTEGER, " +
			ProductEntry.COLUMN_PRODUCT_TYPE_ID + " INTEGER DEFAULT 1, " +
			OrderDetailEntry.COLUMN_ORDER_QTY + " REAL DEFAULT 1, " +
			ProductEntry.COLUMN_PRODUCT_PRICE + " REAL DEFAULT 0, " +
			OrderDetailEntry.COLUMN_DISCOUNT_TYPE + " INTEGER DEFAULT 2, " +
			ProductEntry.COLUMN_VAT_TYPE + " INTEGER DEFAULT 1, " +
			OrderDetailEntry.COLUMN_TOTAL_VAT + " REAL DEFAULT 0, " +
			OrderDetailEntry.COLUMN_TOTAL_VAT_EXCLUDE + " REAL DEFAULT 0, " +
			OrderDetailEntry.COLUMN_MEMBER_DISCOUNT + " REAL DEFAULT 0, " +
			OrderDetailEntry.COLUMN_PRICE_DISCOUNT + " REAL DEFAULT 0, " +
			OrderDetailEntry.COLUMN_TOTAL_RETAIL_PRICE + " REAL DEFAULT 0, " +
			OrderDetailEntry.COLUMN_TOTAL_SALE_PRICE + " REAL DEFAULT 0, " +
			OrderDetailEntry.COLUMN_SALE_MODE + " INTEGER DEFAULT 1, " +
			"PRIMARY KEY (" + OrderDetailEntry.COLUMN_ORDER_ID + " ASC, " +
			TransactionEntry.COLUMN_TRANSACTION_ID + " ASC, " + ComputerEntry.COLUMN_COMPUTER_ID + " ASC) );";
	
	public static final String TRANSACTION_SQL =
			"CREATE TABLE " + TransactionEntry.TABLE_TRANSACTION + " ( " +
			MPOSDatabase.COLUMN_UUID + " TEXT, " +
			TransactionEntry.COLUMN_TRANSACTION_ID + " INTEGER, " +
			ComputerEntry.COLUMN_COMPUTER_ID + " INTEGER, " +
			Shop.COLUMN_SHOP_ID + " INTEGER, " +
			TransactionEntry.COLUMN_OPEN_TIME + " TEXT, " +
			TransactionEntry.COLUMN_OPEN_STAFF + " INTEGER, " +
			TransactionEntry.COLUMN_PAID_TIME + " TEXT, " +
			TransactionEntry.COLUMN_PAID_STAFF_ID + " INTEGER, " +
			TransactionEntry.COLUMN_CLOSE_TIME + " TEXT, " +
			TransactionEntry.COLUMN_CLOSE_STAFF + " INTEGER, " +
			TransactionEntry.COLUMN_STATUS_ID + " INTEGER DEFAULT 1, " +
			StockDocument.COLUMN_DOC_TYPE + " INTEGER DEFAULT 8, " +
			TransactionEntry.COLUMN_RECEIPT_YEAR + " INTEGER, " +
			TransactionEntry.COLUMN_RECEIPT_MONTH + " INTEGER, " +
			TransactionEntry.COLUMN_RECEIPT_ID + " INTEGER, " +
			TransactionEntry.COLUMN_RECEIPT_NO + " TEXT, " +
			TransactionEntry.COLUMN_SALE_DATE + " TEXT, " +
			SessionEntry.COLUMN_SESS_ID + " INTEGER, " +
			TransactionEntry.COLUMN_VOID_STAFF_ID + " INTEGER, " +
			TransactionEntry.COLUMN_VOID_REASON + " TEXT, " +
			TransactionEntry.COLUMN_VOID_TIME + " TEXT, " +
			TransactionEntry.COLUMN_MEMBER_ID + " INTEGER, " +
			TransactionEntry.COLUMN_TRANS_VAT + " REAL DEFAULT 0, " +
			TransactionEntry.COLUMN_TRANS_EXCLUDE_VAT + " REAL DEFAULT 0, " +
			TransactionEntry.COLUMN_TRANS_VATABLE + " REAL DEFAULT 0, " +
			TransactionEntry.COLUMN_TRANS_NOTE + " TEXT, " +
			TransactionEntry.COLUMN_OTHER_DISCOUNT + " REAL DEFAULT 0, " +
			MPOSDatabase.COLUMN_SEND_STATUS + " INTEGER DEFAULT 0, " +
			OrderDetailEntry.COLUMN_SALE_MODE + " INTEGER DEFAULT 1, " +
			"PRIMARY KEY (" + TransactionEntry.COLUMN_TRANSACTION_ID + " ASC, " + ComputerEntry.COLUMN_COMPUTER_ID + " ASC) ); ";
	
	public static final String PRINT_RECEIPT_LOG_SQL = 
			"CREATE TABLE " + PrintLogEntry.TABLE_PRINT_RECEIPT_LOG + "( " +
			PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_ID + " INTEGER, " + 
			TransactionEntry.COLUMN_TRANSACTION_ID + " INTEGER, " +
			ComputerEntry.COLUMN_COMPUTER_ID + " INTEGER, " +
			Staff.COLUMN_STAFF_ID + " INTEGER, " +
			PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_TIME + " TEXT, " +
			PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_STATUS + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_ID + " AUTOINCREMENT ) );";
	
	public static final String PAYMENT_BUTTON_SQL = 
			"CREATE TABLE " + PaymentAmountButton.TABLE_PAYMENT_AMOUNT + "( " +
			PaymentAmountButton.COLUMN_PAYMENT_AMOUNT_ID + " INTEGER, " +
			PaymentAmountButton.COLUMN_PAYMENT_AMOUNT + " REAL DEFAULT 0, " +
			PaymentAmountButton.COLUMN_ORDERING + " INTEGER DEFAULT 0 );";
	
	public static final String PAYMENT_SQL =
			"CREATE TABLE " + PaymentDetail.TABLE_PAYMENT + " ( " +
			PaymentDetail.COLUMN_PAY_ID + " INTEGER, " +
			TransactionEntry.COLUMN_TRANSACTION_ID + " INTEGER, " +
			ComputerEntry.COLUMN_COMPUTER_ID + " INTEGER, " +
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
			TransactionEntry.COLUMN_TRANSACTION_ID + " ASC, " + ComputerEntry.COLUMN_COMPUTER_ID + " ASC) );";
	
	public static final String PAY_TYPE_SQL =
			"CREATE TABLE " + PaymentDetail.TABLE_PAY_TYPE + " ( " +
			PaymentDetail.COLUMN_PAY_TYPE_ID + " INTEGER, " +
			PaymentDetail.COLUMN_PAY_TYPE_CODE + " TEXT, " +
			PaymentDetail.COLUMN_PAY_TYPE_NAME + " TEXT, " +
			PaymentDetail.COLUMN_ORDERING + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + PaymentDetail.COLUMN_PAY_TYPE_ID + ") );";
	
	public static final String PRODUCT_DEPT_SQL =
			"CREATE TABLE " + ProductDeptEntry.TABLE_PRODUCT_DEPT + " ( " +
			ProductEntry.COLUMN_PRODUCT_DEPT_ID + " INTEGER, " +
			ProductEntry.COLUMN_PRODUCT_GROUP_ID + " INTEGER, " +
			ProductDeptEntry.COLUMN_PRODUCT_DEPT_CODE + " TEXT, " +
			ProductDeptEntry.COLUMN_PRODUCT_DEPT_NAME + " TEXT, " +
			ProductEntry.COLUMN_ACTIVATE + " INTEGER DEFAULT 0, " +
			ProductEntry.COLUMN_ORDERING + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + ProductEntry.COLUMN_PRODUCT_DEPT_ID + "));";
	
	public static final String PRODUCT_GROUP_SQL =
			"CREATE TABLE " + ProductGroupEntry.TABLE_PRODUCT_GROUP + " ( " +
			ProductEntry.COLUMN_PRODUCT_GROUP_ID + " INTEGER, " +
			ProductGroupEntry.COLUMN_PRODUCT_GROUP_CODE + " TEXT, " +
			ProductGroupEntry.COLUMN_PRODUCT_GROUP_NAME + " TEXT, " +
			ProductGroupEntry.COLUMN_PRODUCT_GROUP_TYPE + " INTEGER DEFAULT 0, " +
			ProductGroupEntry.COLUMN_IS_COMMENT + " INTEGER DEFAULT 0, " +
			ProductEntry.COLUMN_ACTIVATE + " INTEGER DEFAULT 0, " +
			ProductEntry.COLUMN_ORDERING + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + ProductEntry.COLUMN_PRODUCT_GROUP_ID + "));";
	
	public static final String PCOMP_SET_SQL =
			"CREATE TABLE " + ComponentSetEntry.TABLE_PCOMP_SET + " ( " +
					ComponentSetEntry.COLUMN_PGROUP_ID + " INTEGER, " +
			ProductEntry.COLUMN_PRODUCT_ID + " INTEGER, " +
			ComponentSetEntry.COLUMN_CHILD_PRODUCT_ID + " INTEGER, " +
			ComponentSetEntry.COLUMN_CHILD_PRODUCT_AMOUNT + " REAL, " +
			ComponentSetEntry.COLUMN_FLEXIBLE_PRODUCT_PRICE + " REAL DEFAULT 0, " +
			ComponentSetEntry.COLUMN_FLEXIBLE_INCLUDE_PRICE + " INTEGER DEFAULT 0 " +
			");";
	
	public static final String PCOMP_GROUP_SQL =
			"CREATE TABLE " + ComponentGroupEntry.TABLE_PCOMP_GROUP + " ( " +
			ComponentSetEntry.COLUMN_PGROUP_ID + " INTEGER, " +
			ProductEntry.COLUMN_PRODUCT_ID + " INTEGER, " +
			ComponentGroupEntry.COL_SET_GROUP_NO + " TEXT, " +
			ComponentGroupEntry.COL_SET_GROUP_NAME + " TEXT, " +
			ComponentGroupEntry.COL_REQ_AMOUNT + " REAL DEFAULT 0 " +
			");";
	
	public static final String PRODUCT_SQL =
			"CREATE TABLE " + ProductEntry.TABLE_PRODUCT + " ( " +
			ProductEntry.COLUMN_PRODUCT_ID + " INTEGER, " +
			ProductEntry.COLUMN_PRODUCT_DEPT_ID + " INTEGER, " +
			ProductEntry.COLUMN_PRODUCT_CODE + " TEXT, " +
			ProductEntry.COLUMN_PRODUCT_BAR_CODE + " TEXT, " +
			ProductEntry.COLUMN_PRODUCT_NAME + " TEXT, " +
			ProductEntry.COLUMN_PRODUCT_DESC + " TEXT, " +
			ProductEntry.COLUMN_PRODUCT_TYPE_ID + " INTEGER DEFAULT 0, " +
			ProductEntry.COLUMN_PRODUCT_PRICE + " REAL DEFAULT 0, " +
			ProductEntry.COLUMN_PRODUCT_UNIT_NAME + " TEXT, " +
			ProductEntry.COLUMN_DISCOUNT_ALLOW + " INTEGER DEFAULT 1, " +
			ProductEntry.COLUMN_VAT_TYPE + " INTEGER DEFAULT 1, " +
			ProductEntry.COLUMN_VAT_RATE + " REAL DEFAULT 0, " +
			ProductEntry.COLUMN_ISOUTOF_STOCK + " INTEGER DEFAULT 0, " +
			ProductEntry.COLUMN_IMG_URL + " TEXT, " +
			ProductEntry.COLUMN_ACTIVATE + " INTEGER DEFAULT 0, " +
			ProductEntry.COLUMN_ORDERING + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + ProductEntry.COLUMN_PRODUCT_ID + " ASC));";
	
	public static final String PROVINCE_SQL =
			"CREATE TABLE " + Province.TABLE_PROVINCE + " ( " +
			Province.COLUMN_PROVINCE_ID + " INTEGER, " +
			Province.COLUMN_PROVINCE_NAME + " TEXT, " +
			"PRIMARY KEY (" + Province.COLUMN_PROVINCE_ID + "));";
	
	public static final String SESSION_SQL =
			"CREATE TABLE " + SessionEntry.TABLE_SESSION + " ( " +
			SessionEntry.COLUMN_SESS_ID + " INTEGER, " +
			ComputerEntry.COLUMN_COMPUTER_ID + " INTEGER, " +
			Shop.COLUMN_SHOP_ID + " INTEGER, " +
			TransactionEntry.COLUMN_OPEN_STAFF + " INTEGER, " +
			TransactionEntry.COLUMN_CLOSE_STAFF + " INTEGER, " +
			SessionEntry.COLUMN_SESS_DATE + " TEXT, " +
			SessionEntry.COLUMN_OPEN_DATE + " TEXT, " +
			SessionEntry.COLUMN_CLOSE_DATE + " TEXT, " +
			SessionEntry.COLUMN_OPEN_AMOUNT + " REAL, " +
			SessionEntry.COLUMN_CLOSE_AMOUNT + " REAL, " +
			SessionEntry.COLUMN_IS_ENDDAY + " INTEGER, " +
			"PRIMARY KEY (" + SessionEntry.COLUMN_SESS_ID + ", " + ComputerEntry.COLUMN_COMPUTER_ID + "));";
	
	public static final String SESSION_DETAIL_SQL =
			"CREATE TABLE " + SessionEntry.TABLE_SESSION_DETAIL + " ( " +
			SessionEntry.COLUMN_SESS_DATE + " TEXT, " +
			SessionEntry.COLUMN_ENDDAY_DATE + " TEXT, " +
			SessionEntry.COLUMN_TOTAL_QTY_RECEIPT + " INTEGER, " +
			SessionEntry.COLUMN_TOTAL_AMOUNT_RECEIPT + " REAL, " +
			SessionEntry.COLUMN_IS_SEND_TO_HQ + " INTEGER, " +
			SessionEntry.COLUMN_SEND_TO_HQ_DATE + " TEXT, " +
			"PRIMARY KEY (" + SessionEntry.COLUMN_SESS_DATE + "));";
	
	public static final String SHOP_SQL =
			"CREATE TABLE " + Shop.TABLE_SHOP + " ( " +
			Shop.COLUMN_SHOP_ID + " INTEGER, " +
			Shop.COLUMN_SHOP_CODE + " TEXT, " +
			Shop.COLUMN_SHOP_NAME + " TEXT, " +
			Shop.COLUMN_SHOP_TYPE + " INTEGER, " +
			Shop.COLUMN_VAT_TYPE + " INTEGER, " +
			Shop.COLUMN_OPEN_HOUR + " TEXT, " +
			Shop.COLUMN_CLOSE_HOUR + " TEXT, " +
			Shop.COLUMN_COMPANY + " TEXT, " +
			Shop.COLUMN_ADDR1 + " TEXT, " +
			Shop.COLUMN_ADDR2 + " TEXT, " +
			Shop.COLUMN_CITY + " TEXT, " +
			Province.COLUMN_PROVINCE_ID + " INTEGER, " +
			Shop.COLUMN_ZIPCODE + " TEXT, " +
			Shop.COLUMN_TELEPHONE + " TEXT, " +
			Shop.COLUMN_FAX + " TEXT, " +
			Shop.COLUMN_TAX_ID + " TEXT, " +
			Shop.COLUMN_REGISTER_ID + " TEXT, " +
			Shop.COLUMN_VAT + " REAL );";
	
	public static final String STAFF_PERMISSION_SQL =
			"CREATE TABLE " + Staff.TABLE_STAFF_PERMISSION + " ( " +
			Staff.COLUMN_STAFF_ROLE_ID + " INTEGER DEFAULT 0, " +
			Staff.COLUMN_PERMMISSION_ITEM_ID + " INTEGER DEFAULT 0);";
	
	public static final String STAFF_SQL =
			"CREATE TABLE " + Staff.TABLE_STAFF + " ( " +
			Staff.COLUMN_STAFF_ID + " INTEGER, " +
			Staff.COLUMN_STAFF_CODE + " TEXT, " +
			Staff.COLUMN_STAFF_NAME + " TEXT, " +
			Staff.COLUMN_STAFF_PASS + " TEXT, " +
			"PRIMARY KEY (" + Staff.COLUMN_STAFF_ID + "));";
	
	public static final String SYNC_TRANSACTION_LOG_SQL =
			"CREATE TABLE " + SyncSaleLog.TABLE_SYNC_SALE_LOG + " ( " +
			SessionEntry.COLUMN_SESS_DATE + " TEXT, " + 
			SyncSaleLog.COLUMN_SYNC_STATUS + " INTEGER DEFAULT 0, " + 
			" PRIMARY KEY (" + SessionEntry.COLUMN_SESS_DATE + ")" + " );";
	
	public static final String HEAD_FOOD_RECEIPT_SQL =
			"CREATE TABLE " + HeaderFooterReceipt.TABLE_HEADER_FOOTER_RECEIPT + " ( " +
			HeaderFooterReceipt.COLUMN_TEXT_IN_LINE + " TEXT, " +
			HeaderFooterReceipt.COLUMN_LINE_TYPE + " INTEGER, " +
			HeaderFooterReceipt.COLUMN_LINE_ORDER + " INTEGER );";
}
