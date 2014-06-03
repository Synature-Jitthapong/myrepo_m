package com.syn.mpos.dao;

import java.util.UUID;

import com.syn.mpos.dao.Bank.BankTable;
import com.syn.mpos.dao.Computer.ComputerTable;
import com.syn.mpos.dao.CreditCard.CreditCardTable;
import com.syn.mpos.dao.Formater.GlobalPropertyTable;
import com.syn.mpos.dao.HeaderFooterReceipt.HeaderFooterReceiptTable;
import com.syn.mpos.dao.Language.LanguageTable;
import com.syn.mpos.dao.PaymentAmountButton.PaymentButtonTable;
import com.syn.mpos.dao.PaymentDetail.PayTypeTable;
import com.syn.mpos.dao.PaymentDetail.PaymentDetailTable;
import com.syn.mpos.dao.PrintReceiptLog.PrintReceiptLogTable;
import com.syn.mpos.dao.Products.ProductComponentGroupTable;
import com.syn.mpos.dao.Products.ProductComponentTable;
import com.syn.mpos.dao.Products.ProductDeptTable;
import com.syn.mpos.dao.Products.ProductGroupTable;
import com.syn.mpos.dao.Products.ProductsTable;
import com.syn.mpos.dao.Session.SessionDetailTable;
import com.syn.mpos.dao.Session.SessionTable;
import com.syn.mpos.dao.Shop.ShopTable;
import com.syn.mpos.dao.Staffs.StaffPermissionTable;
import com.syn.mpos.dao.Staffs.StaffTable;
import com.syn.mpos.dao.Transaction.OrderDetailTable;
import com.syn.mpos.dao.Transaction.OrderSetTable;
import com.syn.mpos.dao.Transaction.OrderTransactionTable;

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
