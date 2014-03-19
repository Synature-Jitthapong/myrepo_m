package com.syn.mpos.datasource;

import com.syn.mpos.datasource.Bank.BankEntry;
import com.syn.mpos.datasource.Computer.ComputerEntry;
import com.syn.mpos.datasource.CreditCard.CreditCardEntry;
import com.syn.mpos.datasource.GlobalProperty.GlobalEntry;
import com.syn.mpos.datasource.HeaderFooterReceipt.HeaderFooterEntry;
import com.syn.mpos.datasource.Language.LanguageEntry;
import com.syn.mpos.datasource.PaymentAmountButton.PaymentButtonEntry;
import com.syn.mpos.datasource.PaymentDetail.PayTypeEntry;
import com.syn.mpos.datasource.PaymentDetail.PaymentDetailEntry;
import com.syn.mpos.datasource.PrintReceiptLog.PrintLogEntry;
import com.syn.mpos.datasource.Products.ProductComponentGroupEntry;
import com.syn.mpos.datasource.Products.ProductComponentEntry;
import com.syn.mpos.datasource.Products.ProductDeptEntry;
import com.syn.mpos.datasource.Products.ProductEntry;
import com.syn.mpos.datasource.Products.ProductGroupEntry;
import com.syn.mpos.datasource.Session.SessionEntry;
import com.syn.mpos.datasource.Shop.ShopEntry;
import com.syn.mpos.datasource.Staff.StaffEntry;
import com.syn.mpos.datasource.Staff.StaffPermissionEntry;
import com.syn.mpos.datasource.StockDocument.DocumentTypeEntry;
import com.syn.mpos.datasource.SyncSaleLog.SyncSaleLogEntry;
import com.syn.mpos.datasource.Transaction.OrderDetailEntry;
import com.syn.mpos.datasource.Transaction.TransactionEntry;

public class MPOSSQL {
	public static final String BANK_SQL =
			"CREATE TABLE " + BankEntry.TABLE_BANK + " ( " +
			BankEntry.COLUMN_BANK_ID + " INTEGER, " +
			BankEntry.COLUMN_BANK_NAME + " TEXT, " +
			"PRIMARY KEY (" + BankEntry.COLUMN_BANK_ID + ") );";
	
	public static final String COMPUTER_SQL =
			"CREATE TABLE " + ComputerEntry.TABLE_COMPUTER + " ( " +
			ComputerEntry.COLUMN_COMPUTER_ID + " INTEGER, " +
			ComputerEntry.COLUMN_COMPUTER_NAME + " TEXT, " +
			ComputerEntry.COLUMN_DEVICE_CODE + " TEXT, " +
			ComputerEntry.COLUMN_REGISTER_NUMBER + " TEXT, " +
			ComputerEntry.COLUMN_IS_MAIN_COMPUTER + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + ComputerEntry.COLUMN_COMPUTER_ID + ") );";
	
	public static final String CREDIT_CARD_TYPE_SQL =
			"CREATE TABLE " + CreditCardEntry.TABLE_CREDITCARD_TYPE + " ( " +
			CreditCardEntry.COLUMN_CREDITCARD_TYPE_ID + " INTEGER, " +
			CreditCardEntry.COLUMN_CREDITCARD_TYPE_NAME + " TEXT, " +
			" PRIMARY KEY (" + CreditCardEntry.COLUMN_CREDITCARD_TYPE_ID + ") );";
	
	public static final String GLOBAL_PROPERTY_SQL =
			"CREATE TABLE " + GlobalEntry.TABLE_GLOBAL_PROPERTY + " ( " +
			GlobalEntry.COLUMN_CURRENCY_SYMBOL + " TEXT DEFAULT '$', " +
			GlobalEntry.COLUMN_CURRENCY_CODE + " TEXT DEFAULT 'USD', " +
			GlobalEntry.COLUMN_CURRENCY_NAME + " TEXT, " +
			GlobalEntry.COLUMN_CURRENCY_FORMAT + " TEXT DEFAULT '#,##0.00', " +
			GlobalEntry.COLUMN_QTY_FORMAT + " TEXT DEFAULT '#,##0', " +
			GlobalEntry.COLUMN_DATE_FORMAT + " TEXT DEFAULT 'd MMMM yyyy', " +
			GlobalEntry.COLUMN_TIME_FORMAT + " TEXT DEFAULT 'HH:mm:ss' );";
	
	public static final String LANGUAGE_SQL =
			"CREATE TABLE " + LanguageEntry.TABLE_LANGUAGE + " ( " +
			LanguageEntry.COLUMN_LANG_ID + " INTEGER DEFAULT 1, " +
			LanguageEntry.COLUMN_LANG_NAME + " TEXT, " +
			LanguageEntry.COLUMN_LANG_CODE + " TEXT DEFAULT 'en', " +
			"PRIMARY KEY (" + LanguageEntry.COLUMN_LANG_ID + ") );";
	
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
			ProductEntry.COLUMN_SALE_MODE + " INTEGER DEFAULT 1, " +
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
			ProductEntry.COLUMN_SALE_MODE + " INTEGER DEFAULT 1, " +
			"PRIMARY KEY (" + OrderDetailEntry.COLUMN_ORDER_ID + " ASC, " +
			TransactionEntry.COLUMN_TRANSACTION_ID + " ASC, " + ComputerEntry.COLUMN_COMPUTER_ID + " ASC) );";
	
	public static final String TRANSACTION_SQL =
			"CREATE TABLE " + TransactionEntry.TABLE_TRANSACTION + " ( " +
			MPOSDatabase.COLUMN_UUID + " TEXT, " +
			TransactionEntry.COLUMN_TRANSACTION_ID + " INTEGER, " +
			ComputerEntry.COLUMN_COMPUTER_ID + " INTEGER, " +
			ShopEntry.COLUMN_SHOP_ID + " INTEGER, " +
			TransactionEntry.COLUMN_OPEN_TIME + " TEXT, " +
			TransactionEntry.COLUMN_OPEN_STAFF + " INTEGER, " +
			TransactionEntry.COLUMN_PAID_TIME + " TEXT, " +
			TransactionEntry.COLUMN_PAID_STAFF_ID + " INTEGER, " +
			TransactionEntry.COLUMN_CLOSE_TIME + " TEXT, " +
			TransactionEntry.COLUMN_CLOSE_STAFF + " INTEGER, " +
			TransactionEntry.COLUMN_STATUS_ID + " INTEGER DEFAULT 1, " +
			DocumentTypeEntry.COLUMN_DOC_TYPE + " INTEGER DEFAULT 8, " +
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
			ProductEntry.COLUMN_SALE_MODE + " INTEGER DEFAULT 1, " +
			"PRIMARY KEY (" + TransactionEntry.COLUMN_TRANSACTION_ID + " ASC, " + ComputerEntry.COLUMN_COMPUTER_ID + " ASC) ); ";
	
	public static final String PRINT_RECEIPT_LOG_SQL = 
			"CREATE TABLE " + PrintLogEntry.TABLE_PRINT_RECEIPT_LOG + "( " +
			PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_ID + " INTEGER, " + 
			TransactionEntry.COLUMN_TRANSACTION_ID + " INTEGER, " +
			ComputerEntry.COLUMN_COMPUTER_ID + " INTEGER, " +
			StaffEntry.COLUMN_STAFF_ID + " INTEGER, " +
			PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_TIME + " TEXT, " +
			PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_STATUS + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + PrintLogEntry.COLUMN_PRINT_RECEIPT_LOG_ID + " AUTOINCREMENT ) );";
	
	public static final String PAYMENT_BUTTON_SQL = 
			"CREATE TABLE " + PaymentButtonEntry.TABLE_PAYMENT_AMOUNT + "( " +
			PaymentButtonEntry.COLUMN_PAYMENT_AMOUNT_ID + " INTEGER, " +
			PaymentButtonEntry.COLUMN_PAYMENT_AMOUNT + " REAL DEFAULT 0, " +
			PaymentButtonEntry.COLUMN_ORDERING + " INTEGER DEFAULT 0 );";
	
	public static final String PAYMENT_SQL =
			"CREATE TABLE " + PaymentDetailEntry.TABLE_PAYMENT + " ( " +
					PaymentDetailEntry.COLUMN_PAY_ID + " INTEGER, " +
			TransactionEntry.COLUMN_TRANSACTION_ID + " INTEGER, " +
			ComputerEntry.COLUMN_COMPUTER_ID + " INTEGER, " +
			PayTypeEntry.COLUMN_PAY_TYPE_ID + " INTEGER DEFAULT 1, " +
			PaymentDetailEntry.COLUMN_PAY_AMOUNT + " REAL DEFAULT 0, " +
			PaymentDetailEntry.COLUMN_PAID + " REAL DEFAULT 0, " +
			CreditCardEntry.COLUMN_CREDITCARD_NO + " TEXT, " +
			CreditCardEntry.COLUMN_EXP_MONTH + " INTEGER, " +
			CreditCardEntry.COLUMN_EXP_YEAR + " INTEGER, " +
			BankEntry.COLUMN_BANK_ID + " INTEGER, " +
			CreditCardEntry.COLUMN_CREDITCARD_TYPE_ID + " INTEGER, " +
			PaymentDetailEntry.COLUMN_REMARK + " TEXT, " +
			"PRIMARY KEY (" + PaymentDetailEntry.COLUMN_PAY_ID + " ASC, " + 
			TransactionEntry.COLUMN_TRANSACTION_ID + " ASC, " + ComputerEntry.COLUMN_COMPUTER_ID + " ASC) );";
	
	public static final String PAY_TYPE_SQL =
			"CREATE TABLE " + PayTypeEntry.TABLE_PAY_TYPE + " ( " +
			PayTypeEntry.COLUMN_PAY_TYPE_ID + " INTEGER, " +
			PayTypeEntry.COLUMN_PAY_TYPE_CODE + " TEXT, " +
			PayTypeEntry.COLUMN_PAY_TYPE_NAME + " TEXT, " +
			PayTypeEntry.COLUMN_ORDERING + " INTEGER DEFAULT 0, " +
			"PRIMARY KEY (" + PayTypeEntry.COLUMN_PAY_TYPE_ID + ") );";
	
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
	
	public static final String PCOMP_SQL =
			"CREATE TABLE " + ProductComponentEntry.TABLE_PCOMP + " ( " +
					ProductComponentEntry.COLUMN_PGROUP_ID + " INTEGER, " +
			ProductEntry.COLUMN_PRODUCT_ID + " INTEGER, " +
			ProductEntry.COLUMN_SALE_MODE + " INTEGER DEFAULT 0, " +
			ProductComponentEntry.COLUMN_CHILD_PRODUCT_ID + " INTEGER, " +
			ProductComponentEntry.COLUMN_CHILD_PRODUCT_AMOUNT + " REAL, " +
			ProductComponentEntry.COLUMN_FLEXIBLE_PRODUCT_PRICE + " REAL DEFAULT 0, " +
			ProductComponentEntry.COLUMN_FLEXIBLE_INCLUDE_PRICE + " INTEGER DEFAULT 0 " +
			");";
	
	public static final String PCOMP_GROUP_SQL =
			"CREATE TABLE " + ProductComponentGroupEntry.TABLE_PCOMP_GROUP + " ( " +
			ProductComponentEntry.COLUMN_PGROUP_ID + " INTEGER, " +
			ProductEntry.COLUMN_PRODUCT_ID + " INTEGER, " +
			ProductEntry.COLUMN_SALE_MODE + " INTEGER DEFAULT 0, " +
			ProductComponentGroupEntry.COLUMN_SET_GROUP_NO + " TEXT, " +
			ProductComponentGroupEntry.COLUMN_SET_GROUP_NAME + " TEXT, " +
			ProductComponentGroupEntry.COLUMN_REQ_AMOUNT + " REAL DEFAULT 0 " +
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
	
	public static final String SESSION_SQL =
			"CREATE TABLE " + SessionEntry.TABLE_SESSION + " ( " +
			SessionEntry.COLUMN_SESS_ID + " INTEGER, " +
			ComputerEntry.COLUMN_COMPUTER_ID + " INTEGER, " +
			ShopEntry.COLUMN_SHOP_ID + " INTEGER, " +
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
			"CREATE TABLE " + SyncSaleLogEntry.TABLE_SYNC_SALE_LOG + " ( " +
			SessionEntry.COLUMN_SESS_DATE + " TEXT, " + 
			SyncSaleLogEntry.COLUMN_SYNC_STATUS + " INTEGER DEFAULT 0, " + 
			" PRIMARY KEY (" + SessionEntry.COLUMN_SESS_DATE + ")" + " );";
	
	public static final String HEAD_FOOD_RECEIPT_SQL =
			"CREATE TABLE " + HeaderFooterEntry.TABLE_HEADER_FOOTER_RECEIPT + " ( " +
			HeaderFooterEntry.COLUMN_TEXT_IN_LINE + " TEXT, " +
			HeaderFooterEntry.COLUMN_LINE_TYPE + " INTEGER, " +
			HeaderFooterEntry.COLUMN_LINE_ORDER + " INTEGER );";
}
