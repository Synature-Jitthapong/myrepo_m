package com.syn.mpos.database;

import java.util.List;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.syn.pos.ShopData;

public class Computer extends MPOSDatabase{
	
	public static final String TABLE_COMPUTER = "Computer";
	public static final String COLUMN_COMPUTER_ID = "computer_id";
	public static final String COLUMN_COMPUTER_NAME = "computer_name";
	public static final String COLUMN_DEVICE_CODE = "device_code";
	public static final String COLUMN_REGISTER_NUMBER = "register_number";
	public static final String COLUMN_IS_MAIN_COMPUTER = "ismain_computer";
	
	public Computer(SQLiteDatabase db) {
		super(db);
	}

	public boolean checkIsMainComputer(int computerId){
		boolean isMainComputer = false;
		Cursor cursor = mSqlite.query(TABLE_COMPUTER, 
				new String[]{
				COLUMN_IS_MAIN_COMPUTER
				}, COLUMN_COMPUTER_ID + "=?", 
				new String[]{
					String.valueOf(computerId)
				}, null, null, null);
		if(cursor.moveToFirst()){
			if(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_MAIN_COMPUTER)) != 0)
				isMainComputer = true;
		}
		cursor.close();
		return isMainComputer;
	}
	
	public ShopData.ComputerProperty getComputerProperty() {
		ShopData.ComputerProperty computer = 
				new ShopData.ComputerProperty();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + TABLE_COMPUTER, null);
		if (cursor.moveToFirst()) {
			computer.setComputerID(cursor.getInt(cursor.getColumnIndex(COLUMN_COMPUTER_ID)));
			computer.setComputerName(cursor.getString(cursor.getColumnIndex(COLUMN_COMPUTER_NAME)));
			computer.setDeviceCode(cursor.getString(cursor.getColumnIndex(COLUMN_DEVICE_CODE)));
			computer.setRegistrationNumber(cursor.getString(cursor.getColumnIndex(COLUMN_REGISTER_NUMBER)));
			cursor.moveToNext();
		}
		cursor.close();
		return computer;
	}

	public void insertComputer(List<ShopData.ComputerProperty> compLst) throws SQLException{
		mSqlite.delete(TABLE_COMPUTER, null, null);
		for (ShopData.ComputerProperty comp : compLst) {
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_COMPUTER_ID, comp.getComputerID());
			cv.put(COLUMN_COMPUTER_NAME, comp.getComputerName());
			cv.put(COLUMN_DEVICE_CODE, comp.getDeviceCode());
			cv.put(COLUMN_REGISTER_NUMBER, comp.getRegistrationNumber());
			cv.put(COLUMN_IS_MAIN_COMPUTER, comp.getIsMainComputer());
			mSqlite.insertOrThrow(TABLE_COMPUTER, null, cv);
		}
	}
}
