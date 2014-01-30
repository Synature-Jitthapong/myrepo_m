package com.syn.mpos.database;

import com.syn.mpos.MPOSApplication;
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
	public static final String DB_NAME = MPOSApplication.DB_NAME;
	public static final int DB_VERSION = MPOSApplication.DB_VERSION;

	private static final String[] sqlCreateTables = {
		MPOSSQL.BANK_SQL,
		MPOSSQL.COMPUTER_SQL,
		MPOSSQL.CREDIT_CARD_TYPE_SQL,
		MPOSSQL.DOC_DETAIL_SQL,
		MPOSSQL.DOCUMENT_SQL,
		MPOSSQL.DOCUMENT_TYPE_SQL,
		MPOSSQL.GLOBAL_PROPERTY_SQL,
		MPOSSQL.LANGUAGE_SQL,
		MPOSSQL.ORDER_SQL,
		MPOSSQL.ORDER_TMP_SQL,
		MPOSSQL.TRANSACTION_SQL,
		MPOSSQL.PAYMENT_SQL,
		MPOSSQL.PAY_TYPE_SQL,
		MPOSSQL.PRODUCT_DEPT_SQL,
		MPOSSQL.PRODUCT_GROUP_SQL,
		MPOSSQL.PCOMP_SET_SQL,
		MPOSSQL.PRODUCT_SQL,
		MPOSSQL.PROVINCE_SQL,
		MPOSSQL.SESSION_SQL,
		MPOSSQL.SESSION_DETAIL_SQL,
		MPOSSQL.SHOP_SQL,
		MPOSSQL.STAFF_PERMISSION_SQL,
		MPOSSQL.STAFF_SQL,
		MPOSSQL.SYNC_TRANSACTION_LOG_SQL,
		MPOSSQL.HEAD_FOOD_RECEIPT_SQL
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
		"INSERT INTO DocumentType VALUES (7, '', 'Monthly Stock Card', 0);",
		"INSERT INTO DocumentType VALUES (10, '', 'Transfer Stock', 1);",
		"INSERT INTO DocumentType VALUES (18, '', 'Add From Daily Stock', 1);",
		"INSERT INTO DocumentType VALUES (19, '', 'Reduce From Daily Stock', -1);",
		"INSERT INTO DocumentType VALUES (20, '', 'Material From Sale Document', -1);",
		"INSERT INTO DocumentType VALUES (21, '', 'Material From Void Document', 1);",
		"INSERT INTO DocumentType VALUES (22, '', 'Add From Monthly Stock', 1);",
		"INSERT INTO DocumentType VALUES (23, '', 'Reduce From Monthly Stock', -1);",
		"INSERT INTO DocumentType VALUES (24, '', 'Daily Stock Card', 0);",
		"INSERT INTO DocumentType VALUES (39, '', 'Direct Receive Order', 1);",
		"INSERT INTO DocumentType VALUES (8, '', 'Receipt', 0);",
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
