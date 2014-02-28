package com.syn.mpos.provider;

import com.syn.mpos.MPOSApplication;
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
		MPOSSQL.PRINT_RECEIPT_LOG_SQL,
		MPOSSQL.PAYMENT_SQL,
		MPOSSQL.PAYMENT_BUTTON_SQL,
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
	
	public MPOSSQLiteHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for(String sql : sqlCreateTables){
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
