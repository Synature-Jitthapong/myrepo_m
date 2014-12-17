package com.synature.mpos.database;

import android.content.Context;
import android.database.SQLException;

public class WasteDataSource extends MPOSDatabase{

	public WasteDataSource(Context context) {
		super(context);
	}

	public void confirmWaste(int transactionId){
		getWritableDatabase().beginTransaction();
		try{
			OrderTransWasteDao trans = new OrderTransWasteDao(getWritableDatabase());
			trans.moveToRealTable(transactionId);
			
			PaymentDetailWasteDao payment = new PaymentDetailWasteDao(getWritableDatabase());
			payment.moveToRealTable(transactionId);
			getWritableDatabase().setTransactionSuccessful();
		}finally{
			getWritableDatabase().endTransaction();
		}
	}
	
	public void deletePaymentWast(int transactionId){
		PaymentDetailWasteDao payment = 
				new PaymentDetailWasteDao(getWritableDatabase());
		payment.deletePaymentDetailWaste(transactionId);
	}
	
	public void addPaymentWaste(int transactionId, int computerId, 
			int payTypeId, double payAmount, String remark) throws SQLException{
		PaymentDetailWasteDao payment = 
				new PaymentDetailWasteDao(getWritableDatabase());
		payment.addPaymentDetailWaste(transactionId, computerId, 
				payTypeId, payAmount, remark);
	}
}
