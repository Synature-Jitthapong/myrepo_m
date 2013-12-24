package com.syn.mpos.database;

import com.syn.mpos.GlobalVar;
import com.syn.mpos.database.inventory.StockDocument;
import com.syn.mpos.database.transaction.PaymentDetail;
import com.syn.mpos.database.transaction.Session;
import com.syn.mpos.database.transaction.Transaction;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @author j1tth4
 *
 */
public class MPOSSQLiteHelper extends SQLiteOpenHelper{
	public static final String DB_NAME = GlobalVar.DB_NAME;
	public static final int DB_VERSION = GlobalVar.DB_VERSION;

	private static final String[] sqlCreateTables = {
		// tb bankname
		"CREATE TABLE " + Bank.TB_BANK + " ( " +
		Bank.COL_BANK_ID + " INTEGER, " +
		Bank.COL_BANK_NAME + " TEXT, " +
		"PRIMARY KEY (" + Bank.COL_BANK_ID + ") );",
		
		// tb computer
		"CREATE TABLE " + Computer.TB_COMPUTER + " ( " +
		Computer.COL_COMPUTER_ID + " INTEGER, " +
		Computer.COL_COMPUTER_NAME + " TEXT, " +
		Computer.COL_DEVICE_CODE + " TEXT, " +
		Computer.COL_REGISTER_NUMBER + " TEXT, " +
		"PRIMARY KEY (" + Computer.COL_COMPUTER_ID + ") );",
		
		// tb creditcard type
		"CREATE TABLE " + CreditCard.TB_CREDIT_CARD_TYPE + " ( " +
		CreditCard.COL_CREDIT_CARD_TYPE_ID + " INTEGER, " +
		CreditCard.COL_CREDIT_CARD_TYPE_NAME + " TEXT, " +
		" PRIMARY KEY (" + CreditCard.COL_CREDIT_CARD_TYPE_ID + ") );",
		
		// tb docdetail
		"CREATE TABLE " + StockDocument.TB_DOC_DETAIL + " ( " +
		StockDocument.COL_DOC_DETAIL_ID + " INTEGER, " +
		StockDocument.COL_DOC_ID + " INTEGER, " +
		Shop.COL_SHOP_ID + " INTEGER, " +
		Products.COL_PRODUCT_ID + " INTEGER, " +
		StockDocument.COL_PRODUCT_AMOUNT + " REAL DEFAULT 0, " +
		Products.COL_PRODUCT_UNIT_NAME + " TEXT, " + 
		Products.COL_PRODUCT_PRICE + " REAL DEFAULT 0, " +
		"PRIMARY KEY (" + StockDocument.COL_DOC_DETAIL_ID + " ASC, " + 
		StockDocument.COL_DOC_ID + " ASC, " + Shop.COL_SHOP_ID + " ASC) );",
		
		// tb document
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
		"PRIMARY KEY (" + StockDocument.COL_DOC_ID + " ASC, " + Shop.COL_SHOP_ID + " ASC) );", 
		
		// tb documenttype
		"CREATE TABLE " + StockDocument.TB_DOCUMENT_TYPE + " ( " +
		StockDocument.COL_DOC_TYPE + " INTEGER, " +
		StockDocument.COL_DOC_TYPE_HEADER + " TEXT, " +
		StockDocument.COL_DOC_TYPE_NAME + " TEXT, " +
		StockDocument.COL_MOVE_MENT + " INTEGER DEFAULT 0 );",
		
		// tb globalproperty
		"CREATE TABLE " + GlobalProperty.TB_GLOBAL_PROPERTY + " ( " +
		GlobalProperty.COL_CURRENCY_SYMBOL + " TEXT DEFAULT '$', " +
		GlobalProperty.COL_CURRENCY_CODE + " TEXT DEFAULT 'USD', " +
		GlobalProperty.COL_CURRENCY_NAME + " TEXT, " +
		GlobalProperty.COL_CURRENCY_FORMAT + " TEXT DEFAULT '#,##0.##', " +
		GlobalProperty.COL_QTY_FORMAT + " TEXT DEFAULT '#,##0.##', " +
		GlobalProperty.COL_DATE_FORMAT + " TEXT DEFAULT 'yyyy/MM/dd', " +
		GlobalProperty.COL_TIME_FORMAT + " TEXT DEFAULT 'HH:mm:ss' );",
		
		// tb language
		"CREATE TABLE " + Language.TB_LANGUAGE + " ( " +
		Language.COL_LANG_ID + " INTEGER DEFAULT 1, " +
		Language.COL_LANG_NAME + " TEXT, " +
		Language.COL_LANG_CODE + " TEXT DEFAULT 'en', " +
		"PRIMARY KEY (" + Language.COL_LANG_ID + ") );", 
		
		// tb orderdetail
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
		Transaction.COL_TOTAL_VAT + " REAL DEFAULT 1, " +
		Transaction.COL_MEMBER_DISCOUNT + " REAL DEFAULT 0, " +
		Transaction.COL_PRICE_DISCOUNT + " REAL DEFAULT 0, " +
		Transaction.COL_TOTAL_RETAIL_PRICE + " REAL DEFAULT 0, " +
		Transaction.COL_TOTAL_SALE_PRICE + " REAL DEFAULT 0, " +
		"PRIMARY KEY (" + Transaction.COL_ORDER_ID + " ASC, " +
		Transaction.COL_TRANS_ID + " ASC, " + Computer.COL_COMPUTER_ID + " ASC) );",

		// tb orderdetailtmp
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
		Transaction.COL_TOTAL_VAT + " REAL DEFAULT 1, " +
		Transaction.COL_MEMBER_DISCOUNT + " REAL DEFAULT 0, " +
		Transaction.COL_PRICE_DISCOUNT + " REAL DEFAULT 0, " +
		Transaction.COL_TOTAL_RETAIL_PRICE + " REAL DEFAULT 0, " +
		Transaction.COL_TOTAL_SALE_PRICE + " REAL DEFAULT 0, " +
		"PRIMARY KEY (" + Transaction.COL_ORDER_ID + " ASC, " +
		Transaction.COL_TRANS_ID + " ASC, " + Computer.COL_COMPUTER_ID + " ASC) );",
		
		// tb ordertransaction
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
		Transaction.COL_STATUS_ID + " INTEGER DEFAULT 1, " +
		StockDocument.COL_DOC_TYPE + " INTEGER DEFAULT 8, " +
		Transaction.COL_RECEIPT_YEAR + " INTEGER, " +
		Transaction.COL_RECEIPT_MONTH + " INTEGER, " +
		Transaction.COL_RECEIPT_ID + " INTEGER, " +
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
		"PRIMARY KEY (" + Transaction.COL_TRANS_ID + " ASC, " + Computer.COL_COMPUTER_ID + " ASC) ); ",
		
		// tb paydetail
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
		Transaction.COL_TRANS_ID + " ASC, " + Computer.COL_COMPUTER_ID + " ASC) );",
		
		// tb paytype
		"CREATE TABLE " + PaymentDetail.TB_PAY_TYPE + " ( " +
		PaymentDetail.COL_PAY_TYPE_ID + " INTEGER, " +
		PaymentDetail.COL_PAY_TYPE_CODE + " TEXT, " +
		PaymentDetail.COL_PAY_TYPE_NAME + " TEXT, " +
		"PRIMARY KEY (" + PaymentDetail.COL_PAY_TYPE_ID + ") );", 
		
		// tb product dept
		"CREATE TABLE " + Products.TB_PRODUCT_DEPT + " ( " +
		Products.COL_PRODUCT_DEPT_ID + " INTEGER, " +
		Products.COL_PRODUCT_GROUP_ID + " INTEGER, " +
		Products.COL_PRODUCT_DEPT_CODE + " TEXT, " +
		Products.COL_PRODUCT_DEPT_NAME + " TEXT, " +
		"PRIMARY KEY (" + Products.COL_PRODUCT_DEPT_ID + "));",
		
		// tb product group
		"CREATE TABLE " + Products.TB_PRODUCT_GROUP + " ( " +
		Products.COL_PRODUCT_GROUP_ID + " INTEGER, " +
		Products.COL_PRODUCT_GROUP_CODE + " TEXT, " +
		Products.COL_PRODUCT_GROUP_NAME + " TEXT, " +
		Products.COL_PRODUCT_GROUP_TYPE + " INTEGER DEFAULT 0, " +
		Products.COL_IS_COMMENT + " INTEGER DEFAULT 0, " +
		"PRIMARY KEY (" + Products.COL_PRODUCT_GROUP_ID + "));",
		
		// tb products
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
		"PRIMARY KEY (" + Products.COL_PRODUCT_ID + " ASC));",
		
		// tb province
		"CREATE TABLE " + Province.TB_PROVINCE + " ( " +
		Province.COL_PROVINCE_ID + " INTEGER, " +
		Province.COL_PROVINCE_NAME + " TEXT, " +
		"PRIMARY KEY (" + Province.COL_PROVINCE_ID + "));",
		
		// tb session
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
		"PRIMARY KEY (" + Session.COL_SESS_ID + ", " + Computer.COL_COMPUTER_ID + "));",
		
		// tb session enddaydetail
		"CREATE TABLE " + Session.TB_SESSION_DETAIL + " ( " +
		Session.COL_SESS_DATE + " TEXT, " +
		Session.COL_ENDDAY_DATE + " TEXT, " +
		Session.COL_TOTAL_QTY_RECEIPT + " INTEGER, " +
		Session.COL_TOTAL_AMOUNT_RECEIPT + " REAL, " +
		Session.COL_IS_SEND_TO_HQ + " INTEGER, " +
		Session.COL_SEND_TO_HQ_DATE + " TEXT, " +
		"PRIMARY KEY (" + Session.COL_SESS_DATE + "));",
		
		// tb shop
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
		Shop.COL_VAT + " REAL );",
		
		// tb staff permission
		"CREATE TABLE " + Staff.TB_STAFF_PERMISSION + " ( " +
		Staff.COL_STAFF_ROLE_ID + " INTEGER DEFAULT 0, " +
		Staff.COL_PERMMISSION_ITEM_ID + " INTEGER DEFAULT 0);",
		
		// tb staffs
		"CREATE TABLE " + Staff.TB_STAFF + " ( " +
		Staff.COL_STAFF_ID + " INTEGER, " +
		Staff.COL_STAFF_CODE + " TEXT, " +
		Staff.COL_STAFF_NAME + " TEXT, " +
		Staff.COL_STAFF_PASS + " TEXT, " +
		"PRIMARY KEY (" + Staff.COL_STAFF_ID + "));"
	};
	
	private static final String[] sqlAddition = {
		"INSERT INTO BankName VALUES (1, 'กรุงเทพฯ');",
		"INSERT INTO BankName VALUES (2, 'กสิกรไทย');",
		"INSERT INTO BankName VALUES (3, 'ทหารไทย');",
		"INSERT INTO BankName VALUES (4, 'กรุงไทย');",
		"INSERT INTO BankName VALUES (5, 'ไทยพาณิชย์');",
		"INSERT INTO BankName VALUES (6, 'UOB');",
		"INSERT INTO BankName VALUES (7, 'ซิตี้แบงค์');",
		"INSERT INTO BankName VALUES (8, 'กรุงศรีอยุธยา');",
		"INSERT INTO BankName VALUES (9, 'นครหลวงไทย');",
		"INSERT INTO BankName VALUES (10, 'Standard Charter');",
		"INSERT INTO BankName VALUES (11, 'HSBC');",
		"INSERT INTO BankName VALUES (12, 'ออมสิน');",
		"INSERT INTO BankName VALUES (13, 'อิออน');",
		"INSERT INTO BankName VALUES (14, 'AIG');",
		"INSERT INTO BankName VALUES (15, 'GE Money');",
		"INSERT INTO BankName VALUES (16, 'แคปปิตอล โอเค');",
		"INSERT INTO BankName VALUES (17, 'BMB');",
		"INSERT INTO BankName VALUES (18, 'ASA');",
		"INSERT INTO BankName VALUES (19, 'PSCL');",
		"INSERT INTO BankName VALUES (20, 'อื่น ๆ');",
		"INSERT INTO BankName VALUES (21, 'TDB');",
		// credit type
		"INSERT INTO CreditCardType VALUES (1, 'VISA');",
		"INSERT INTO CreditCardType VALUES (2, 'Master');",
		"INSERT INTO CreditCardType VALUES (3, 'AMEX');",
		"INSERT INTO CreditCardType VALUES (4, 'DINERS');",
		"INSERT INTO CreditCardType VALUES (5, 'JCB');",
		"INSERT INTO CreditCardType VALUES (6, 'VS-GOLD');",
		"INSERT INTO CreditCardType VALUES (7, 'MC-GOLD');",
		// documenttype
		"INSERT INTO DocumentType VALUES (7, 'xxxST', 'Monthly Stock Card', 0);",
		"INSERT INTO DocumentType VALUES (10, 'xxxR', 'Transfer Stock', 1);",
		"INSERT INTO DocumentType VALUES (18, 'xxxDS', 'Add From Daily Stock', 1);",
		"INSERT INTO DocumentType VALUES (19, 'xxxDS', 'Reduce From Daily Stock', -1);",
		"INSERT INTO DocumentType VALUES (20, 'xxxSD', 'Material From Sale Document', -1);",
		"INSERT INTO DocumentType VALUES (21, 'xxxVD', 'Material From Void Document', 1);",
		"INSERT INTO DocumentType VALUES (22, 'xxxMS', 'Add From Monthly Stock', 1);",
		"INSERT INTO DocumentType VALUES (23, 'xxxMS', 'Reduce From Monthly Stock', -1);",
		"INSERT INTO DocumentType VALUES (24, 'xxxST', 'Daily Stock Card', 0);",
		"INSERT INTO DocumentType VALUES (39, 'xxxRO', 'Direct Receive Order', 1);",
		"INSERT INTO DocumentType VALUES (8, 'xxxRC', 'Receipt', 0);",
		// province
		"INSERT INTO Province VALUES (1, 'Chiang Mai');",
		"INSERT INTO Province VALUES (2, 'Chiang Rai');",
		"INSERT INTO Province VALUES (3, 'Kamphaeng Phet');",
		"INSERT INTO Province VALUES (4, 'Lampang');",
		"INSERT INTO Province VALUES (5, 'Lamphun');"
	};
	
	public MPOSSQLiteHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for(String sql : sqlCreateTables){
			db.execSQL(sql);
		}
		
		for(String sql : sqlAddition){
			db.execSQL(sql);
		}
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(newVersion > oldVersion){
			//db.execSQL("ALTER TABLE " + Transaction.TB_TRANS + " ADD COLUMN UUID TEXT;");
		}
	}
}
