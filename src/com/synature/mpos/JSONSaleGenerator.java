package com.synature.mpos;

import java.lang.reflect.Type;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.synature.mpos.database.SaleTransaction;
import com.synature.mpos.database.SaleTransaction.POSData_EndDaySaleTransaction;
import com.synature.mpos.database.SaleTransaction.POSData_SaleTransaction;
import com.synature.util.Logger;

public class JSONSaleGenerator{
	public static final String TAG = JSONSaleGenerator.class.getSimpleName();
	
	private Context mContext;
	private SaleTransaction mSaleTrans;
	
	public JSONSaleGenerator(Context context){
		mContext = context;
		mSaleTrans = new SaleTransaction(context);
	}
	
	public String generateSale(String sessionDate){
		String json = null;
		try {
			Gson gson = new Gson();
			Type type = new TypeToken<POSData_SaleTransaction>() {}.getType();
			json = gson.toJson(mSaleTrans.getTransaction(sessionDate), type);
		} catch (Exception e) {
			Logger.appendLog(mContext, MPOSApplication.LOG_PATH, MPOSApplication.LOG_FILE_NAME,
					" Error at generate json sale : " + e.getMessage());
		}
		return json;
	}
	
	public String generateEnddayUnSendSale(String sessionDate){
		String json = null;
		try {
			Gson gson = new Gson();
			Type type = new TypeToken<POSData_EndDaySaleTransaction>() {}.getType();
			json = gson.toJson(mSaleTrans.getEndDayUnSendTransaction(sessionDate), type);
		} catch (Exception e) {
			Logger.appendLog(mContext, MPOSApplication.LOG_PATH, MPOSApplication.LOG_FILE_NAME,
					" Error at generate json unsend end day : " + e.getMessage());
		}
		return json;
	}
	
	public String generateEnddaySale(String sessionDate){
		String json = null;
		try {
			Gson gson = new Gson();
			Type type = new TypeToken<POSData_EndDaySaleTransaction>() {}.getType();
			json = gson.toJson(mSaleTrans.getEndDayTransaction(sessionDate), type);
		} catch (Exception e) {
			Logger.appendLog(mContext, MPOSApplication.LOG_PATH, MPOSApplication.LOG_FILE_NAME,
					" Error at generate json end day : " + e.getMessage());
		}
		return json;
	}
}
