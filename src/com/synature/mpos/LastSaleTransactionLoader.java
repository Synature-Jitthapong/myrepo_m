package com.synature.mpos;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.synature.mpos.database.MPOSDatabase;
import com.synature.mpos.database.SaleTransaction;
import com.synature.mpos.database.table.OrderTransTable;
import com.synature.pos.WebServiceResult;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.text.TextUtils;

public class LastSaleTransactionLoader extends MPOSServiceBase{

	public static final String METHOD = "WSmPOS_GenerateAllSaleTransBackToMPos";
	
	public LastSaleTransactionLoader(Context context, ResultReceiver receiver) {
		super(context, METHOD, receiver);
	}

	@Override
	protected void onPostExecute(String result) {
		WebServiceResult ws;
		try {
			ws = toServiceObject(result);
			if(ws.getiResultID() == WebServiceResult.SUCCESS_STATUS){
				Gson gson = new Gson();
				BackSaleTransaction saleTrans = 
						gson.fromJson(ws.getSzResultData(), BackSaleTransaction.class);
				if(mReceiver != null){
					Bundle b = new Bundle();
				}
			}else{
				if(mReceiver != null){
					Bundle b = new Bundle();
					b.putString("msg", TextUtils.isEmpty(ws.getSzResultData()) ? result : ws.getSzResultData());
					mReceiver.send(RESULT_ERROR, b);
				}
			}
		} catch (JsonSyntaxException e) {
			if(mReceiver != null){
				Bundle b = new Bundle();
				b.putString("msg", TextUtils.isEmpty(result) ? e.getMessage() : result);
				mReceiver.send(RESULT_ERROR, b);
			}
		}
	}
	
	private int countTransaction(){
		int total = 0;
		MPOSDatabase helper = new MPOSDatabase(mContext);
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select count(" + OrderTransTable.COLUMN_TRANS_ID + ")"
				+ " from " + OrderTransTable.TABLE_ORDER_TRANS, null);
		if(cursor.moveToFirst()){
			total = cursor.getInt(0);
		}
		cursor.close();
		return total;
	}
	
	public static class BackSaleTransaction extends SaleTransaction.POSData_EndDaySaleTransaction implements Parcelable{

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
		}
		
	}
}
