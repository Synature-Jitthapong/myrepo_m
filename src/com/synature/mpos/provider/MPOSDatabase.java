package com.synature.mpos.provider;

import java.util.UUID;

import com.synature.mpos.provider.Bank.BankTable;
import com.synature.mpos.provider.Computer.ComputerTable;
import com.synature.mpos.provider.CreditCard.CreditCardTable;
import com.synature.mpos.provider.Formater.GlobalPropertyTable;
import com.synature.mpos.provider.HeaderFooterReceipt.HeaderFooterReceiptTable;
import com.synature.mpos.provider.Language.LanguageTable;
import com.synature.mpos.provider.MenuComment.MenuCommentGroupTable;
import com.synature.mpos.provider.MenuComment.MenuCommentTable;
import com.synature.mpos.provider.MenuComment.MenuFixCommentTable;
import com.synature.mpos.provider.PaymentAmountButton.PaymentButtonTable;
import com.synature.mpos.provider.PaymentDetail.PayTypeTable;
import com.synature.mpos.provider.PaymentDetail.PaymentDetailTable;
import com.synature.mpos.provider.PrintReceiptLog.PrintReceiptLogTable;
import com.synature.mpos.provider.Products.ProductComponentGroupTable;
import com.synature.mpos.provider.Products.ProductComponentTable;
import com.synature.mpos.provider.Products.ProductDeptTable;
import com.synature.mpos.provider.Products.ProductGroupTable;
import com.synature.mpos.provider.Products.ProductsTable;
import com.synature.mpos.provider.Session.SessionDetailTable;
import com.synature.mpos.provider.Session.SessionTable;
import com.synature.mpos.provider.Shop.ShopTable;
import com.synature.mpos.provider.Staffs.StaffPermissionTable;
import com.synature.mpos.provider.Staffs.StaffTable;
import com.synature.mpos.provider.Transaction.OrderCommentTable;
import com.synature.mpos.provider.Transaction.OrderDetailTable;
import com.synature.mpos.provider.Transaction.OrderSetTable;
import com.synature.mpos.provider.Transaction.OrderTransactionTable;

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
				sHelper = new MPOSOpenHelper(context.getApplicationContext());
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
