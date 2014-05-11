package com.syn.mpos.dao;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.syn.pos.ShopData;

public class ComputerDao extends MPOSDatabase{

	public ComputerDao(Context context) {
		super(context);
	}

	public int getComputerId(){
		return getComputerProperty().getComputerID();
	}
	
	public boolean checkIsMainComputer(int computerId){
		boolean isMainComputer = false;
		if(getComputerProperty().getIsMainComputer() != 0)
			isMainComputer = true;
		return isMainComputer;
	}
	
	public ShopData.ComputerProperty getComputerProperty() {
		ShopData.ComputerProperty computer = 
				new ShopData.ComputerProperty();
		Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + ComputerTable.TABLE_COMPUTER, null);
		if (cursor.moveToFirst()) {
			computer.setComputerID(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
			computer.setComputerName(cursor.getString(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_NAME)));
			computer.setDeviceCode(cursor.getString(cursor.getColumnIndex(ComputerTable.COLUMN_DEVICE_CODE)));
			computer.setIsMainComputer(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_IS_MAIN_COMPUTER)));
			computer.setRegistrationNumber(cursor.getString(cursor.getColumnIndex(ComputerTable.COLUMN_REGISTER_NUMBER)));
			cursor.moveToNext();
		}
		cursor.close();
		return computer;
	}

	public void insertComputer(List<ShopData.ComputerProperty> compLst) throws SQLException{
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(ComputerTable.TABLE_COMPUTER, null, null);
			for (ShopData.ComputerProperty comp : compLst) {
				ContentValues cv = new ContentValues();
				cv.put(ComputerTable.COLUMN_COMPUTER_ID, comp.getComputerID());
				cv.put(ComputerTable.COLUMN_COMPUTER_NAME, comp.getComputerName());
				cv.put(ComputerTable.COLUMN_DEVICE_CODE, comp.getDeviceCode());
				cv.put(ComputerTable.COLUMN_REGISTER_NUMBER, comp.getRegistrationNumber());
				cv.put(ComputerTable.COLUMN_IS_MAIN_COMPUTER, comp.getIsMainComputer());
				getWritableDatabase().insertOrThrow(ComputerTable.TABLE_COMPUTER, null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally {
			getWritableDatabase().endTransaction();
		}
	}
	
	public static class ComputerTable{
		
		public static final String TABLE_COMPUTER = "Computer";
		public static final String COLUMN_COMPUTER_ID = "computer_id";
		public static final String COLUMN_COMPUTER_NAME = "computer_name";
		public static final String COLUMN_DEVICE_CODE = "device_code";
		public static final String COLUMN_REGISTER_NUMBER = "register_number";
		public static final String COLUMN_IS_MAIN_COMPUTER = "ismain_computer";
		
		private static final String SQL_CREATE =
				"CREATE TABLE " + TABLE_COMPUTER + " ( " +
				COLUMN_COMPUTER_ID + " INTEGER, " +
				COLUMN_COMPUTER_NAME + " TEXT, " +
				COLUMN_DEVICE_CODE + " TEXT, " +
				COLUMN_REGISTER_NUMBER + " TEXT, " +
				COLUMN_IS_MAIN_COMPUTER + " INTEGER DEFAULT 0, " +
				"PRIMARY KEY (" + COLUMN_COMPUTER_ID + ") );";
		
		public static void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE);
		}

		public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
	}
}
