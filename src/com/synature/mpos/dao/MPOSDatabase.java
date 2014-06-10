package com.synature.mpos.dao;

import java.util.UUID;

import com.synature.mpos.dao.Bank.BankTable;
import com.synature.mpos.dao.Computer.ComputerTable;
import com.synature.mpos.dao.CreditCard.CreditCardTable;
import com.synature.mpos.dao.Formater.GlobalPropertyTable;
import com.synature.mpos.dao.HeaderFooterReceipt.HeaderFooterReceiptTable;
import com.synature.mpos.dao.Language.LanguageTable;
import com.synature.mpos.dao.MenuComment.MenuCommentGroupTable;
import com.synature.mpos.dao.MenuComment.MenuCommentTable;
import com.synature.mpos.dao.MenuComment.MenuFixCommentTable;
import com.synature.mpos.dao.PaymentAmountButton.PaymentButtonTable;
import com.synature.mpos.dao.PaymentDetail.PayTypeTable;
import com.synature.mpos.dao.PaymentDetail.PaymentDetailTable;
import com.synature.mpos.dao.PrintReceiptLog.PrintReceiptLogTable;
import com.synature.mpos.dao.Products.ProductComponentGroupTable;
import com.synature.mpos.dao.Products.ProductComponentTable;
import com.synature.mpos.dao.Products.ProductDeptTable;
import com.synature.mpos.dao.Products.ProductGroupTable;
import com.synature.mpos.dao.Products.ProductsTable;
import com.synature.mpos.dao.Session.SessionDetailTable;
import com.synature.mpos.dao.Session.SessionTable;
import com.synature.mpos.dao.Shop.ShopTable;
import com.synature.mpos.dao.Staffs.StaffPermissionTable;
import com.synature.mpos.dao.Staffs.StaffTable;
import com.synature.mpos.dao.Transaction.OrderCommentTable;
import com.synature.mpos.dao.Transaction.OrderDetailTable;
import com.synature.mpos.dao.Transaction.OrderSetTable;
import com.synature.mpos.dao.Transaction.OrderTransactionTable;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @author j1tth4
 * 
 */
public class MPOSDatabase extends BaseColumn{
	
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
			MenuCommentTable.onCreate(db);
			MenuCommentGroupTable.onCreate(db);
			MenuFixCommentTable.onCreate(db);
			OrderDetailTable.onCreate(db);
			OrderTransactionTable.onCreate(db);
			OrderSetTable.onCreate(db);
			OrderCommentTable.onCreate(db);
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
