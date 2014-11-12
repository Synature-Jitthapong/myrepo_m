package com.synature.mpos;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.synature.mpos.database.SessionDao;
import com.synature.mpos.database.TransactionDao;
import com.synature.mpos.database.table.OrderTransTable;
import com.synature.mpos.database.table.SessionDetailTable;
import com.synature.mpos.database.table.SessionTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class WrongTimeZoneResolve {
	
	public static final String TAG = WrongTimeZoneResolve.class.getSimpleName();
	
	public static void resolveSession(Context context){
		SessionDao session = new SessionDao(context);
		SQLiteDatabase db = session.getWritableDatabase();
		Cursor cursor = null;
		db.beginTransaction();
		try {
			Log.i(TAG, "Begin resolve session date");
			cursor = db.rawQuery("SELECT " + SessionTable.COLUMN_SESS_DATE
					+ " FROM " + SessionTable.TABLE_SESSION, null);
			if(cursor.moveToFirst()){
				do{
					ContentValues cv = new ContentValues();
					String sessDate = cursor.getString(0);
					String newTime = getNewTime(sessDate);
					cv.put(SessionTable.COLUMN_SESS_DATE, newTime);
					db.update(SessionTable.TABLE_SESSION, cv, SessionTable.COLUMN_SESS_DATE + "=?", new String[]{sessDate});
					Log.i(TAG, "From: " + sessDate + " To: " + newTime);
				}while(cursor.moveToNext());
			}
			db.setTransactionSuccessful();
			Log.i(TAG, "Success resolve session date");
		} finally {
			db.endTransaction();
			if(cursor != null)
				cursor.close();
		}
	}
	
	public static void resolveSessionDetail(Context context){
		SessionDao session = new SessionDao(context);
		SQLiteDatabase db = session.getWritableDatabase();
		Cursor cursor = null;
		db.beginTransaction();
		try {
			Log.i(TAG, "Begin resolve session date");
			cursor = db.rawQuery("SELECT " + SessionTable.COLUMN_SESS_DATE
					+ " FROM " + SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL, null);
			if(cursor.moveToFirst()){
				do{
					ContentValues cv = new ContentValues();
					String sessDate = cursor.getString(0);
					String newTime = getNewTime(sessDate);
					cv.put(SessionTable.COLUMN_SESS_DATE, newTime);
					db.update(SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL, cv, SessionTable.COLUMN_SESS_DATE + "=?", new String[]{sessDate});
					Log.i(TAG, "From: " + sessDate + " To: " + newTime);
				}while(cursor.moveToNext());
			}
			db.setTransactionSuccessful();
			Log.i(TAG, "Success resolve session date");
		} finally {
			db.endTransaction();
			if(cursor != null)
				cursor.close();
		}
	}
	
	public static void resolveTransaction(Context context){
		TransactionDao trans = new TransactionDao(context);
		SQLiteDatabase db = trans.getWritableDatabase();
		Cursor cursor = null;
		db.beginTransaction();
		try {
			Log.i(TAG, "Begin resolve sale date");
			cursor = db.rawQuery("SELECT " + OrderTransTable.COLUMN_TRANS_ID + ", " 
					+ OrderTransTable.COLUMN_SALE_DATE
					+ " FROM " + OrderTransTable.TABLE_ORDER_TRANS, null);
			if(cursor.moveToFirst()){
				do{
					ContentValues cv = new ContentValues();
					int transId = cursor.getInt(0);
					String saleDate = cursor.getString(1);
					String newTime = getNewTime(saleDate);
					cv.put(OrderTransTable.COLUMN_SALE_DATE, newTime);
					db.update(OrderTransTable.TABLE_ORDER_TRANS, cv, SessionTable.COLUMN_SESS_DATE + "=?", 
							new String[]{String.valueOf(transId)});
					Log.i(TAG, "From: " + saleDate + " To: " + newTime);
				}while(cursor.moveToNext());
			}
			db.setTransactionSuccessful();
			Log.i(TAG, "Success resolve sale date");
		} finally {
			db.endTransaction();
			if(cursor != null)
				cursor.close();
		}
	}
	
	public static String getNewTime(String time){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(Long.parseLong(time));
		String newTime = String.valueOf(new GregorianCalendar(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).getTimeInMillis());
		return newTime;
	}
}
