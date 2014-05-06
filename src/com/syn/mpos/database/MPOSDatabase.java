package com.syn.mpos.database;

import java.util.UUID;

import com.syn.mpos.database.table.BankTable;
import com.syn.mpos.database.table.ComputerTable;
import com.syn.mpos.database.table.CreditCardTable;
import com.syn.mpos.database.table.GlobalPropertyTable;
import com.syn.mpos.database.table.HeaderFooterReceiptTable;
import com.syn.mpos.database.table.LanguageTable;
import com.syn.mpos.database.table.OrderDetailTable;
import com.syn.mpos.database.table.OrderSetTable;
import com.syn.mpos.database.table.OrderTransactionTable;
import com.syn.mpos.database.table.PayTypeTable;
import com.syn.mpos.database.table.PaymentButtonTable;
import com.syn.mpos.database.table.PaymentDetailTable;
import com.syn.mpos.database.table.PrintReceiptLogTable;
import com.syn.mpos.database.table.ProductComponentGroupTable;
import com.syn.mpos.database.table.ProductComponentTable;
import com.syn.mpos.database.table.ProductDeptTable;
import com.syn.mpos.database.table.ProductGroupTable;
import com.syn.mpos.database.table.ProductsTable;
import com.syn.mpos.database.table.SessionDetailTable;
import com.syn.mpos.database.table.SessionTable;
import com.syn.mpos.database.table.ShopTable;
import com.syn.mpos.database.table.StaffPermissionTable;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @author j1tth4
 * 
 */
public class MPOSDatabase {
	
	public static final int NOT_SEND = 0;
	public static final int ALREADY_SEND = 1;
	
	private MPOSOpenHelper mHelper;
	
	public MPOSDatabase(Context context){
		mHelper = MPOSOpenHelper.getInstance(context); 
	}
	
	public String getUUID(){
		return UUID.randomUUID().toString();
	}
	
	public SQLiteDatabase getWritableDatabase(){
		return mHelper.getWritableDatabase();
	}
	
	public SQLiteDatabase getReadableDatabase(){
		return mHelper.getReadableDatabase();
	}
	
	public static class MPOSOpenHelper extends SQLiteOpenHelper {
		
		private static final String DB_NAME = "mpos.db";
		private static final int DB_VERSION = 1;

		private static MPOSOpenHelper sHelper;

		/**
		 * @param context
		 * @return SQLiteOpenHelper instance This singleton pattern for only get
		 *         one SQLiteOpenHelper instance for thread save
		 */
		public static synchronized MPOSOpenHelper getInstance(Context context) {
			if (sHelper == null) {
				sHelper = new MPOSOpenHelper(context);
			}
			return sHelper;
		}

		private MPOSOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			BankTable.onCreate(db);
			ComputerTable.onCreate(db);
			CreditCardTable.onCreate(db);
			GlobalPropertyTable.onCreate(db);
			LanguageTable.onCreate(db);
			HeaderFooterReceiptTable.onCreate(db);
			OrderDetailTable.onCreate(db);
			OrderTransactionTable.onCreate(db);
			OrderSetTable.onCreate(db);
			PrintReceiptLogTable.onCreate(db);
			PaymentDetailTable.onCreate(db);
			PaymentButtonTable.onCreate(db);
			PayTypeTable.onCreate(db);
			ProductDeptTable.onCreate(db);
			ProductGroupTable.onCreate(db);
			ProductComponentGroupTable.onCreate(db);
			ProductComponentTable.onCreate(db);
			ProductsTable.onCreate(db);
			SessionTable.onCreate(db);
			SessionDetailTable.onCreate(db);
			ShopTable.onCreate(db);
			StaffPermissionTable.onCreate(db);
			StaffTable.onCreate(db);
		}

		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion,
				int newVersion) {

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
	}
}
