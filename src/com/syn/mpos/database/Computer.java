package com.syn.mpos.database;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.syn.pos.ShopData;

public class Computer extends MPOSDatabase{

	public Computer(Context c) {
		super(c);
	}

	public boolean checkIsMainComputer(int computerId){
		boolean isMainComputer = false;
		Cursor cursor = mSqlite.query(ComputerTable.TABLE_COMPUTER, 
				new String[]{
				ComputerTable.COLUMN_IS_MAIN_COMPUTER
				}, ComputerTable.COLUMN_COMPUTER_ID + "=?", 
				new String[]{
					String.valueOf(computerId)
				}, null, null, null);
		if(cursor.moveToFirst()){
			if(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_IS_MAIN_COMPUTER)) != 0)
				isMainComputer = true;
		}
		cursor.close();
		return isMainComputer;
	}
	
	public ShopData.ComputerProperty getComputerProperty() {
		ShopData.ComputerProperty computer = 
				new ShopData.ComputerProperty();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + ComputerTable.TABLE_COMPUTER, null);
		if (cursor.moveToFirst()) {
			computer.setComputerID(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
			computer.setComputerName(cursor.getString(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_NAME)));
			computer.setDeviceCode(cursor.getString(cursor.getColumnIndex(ComputerTable.COLUMN_DEVICE_CODE)));
			computer.setRegistrationNumber(cursor.getString(cursor.getColumnIndex(ComputerTable.COLUMN_REGISTER_NUMBER)));
			cursor.moveToNext();
		}
		cursor.close();
		return computer;
	}

	public void insertComputer(List<ShopData.ComputerProperty> compLst) throws SQLException{
		mSqlite.beginTransaction();
		try {
			mSqlite.delete(ComputerTable.TABLE_COMPUTER, null, null);
			for (ShopData.ComputerProperty comp : compLst) {
				ContentValues cv = new ContentValues();
				cv.put(ComputerTable.COLUMN_COMPUTER_ID, comp.getComputerID());
				cv.put(ComputerTable.COLUMN_COMPUTER_NAME, comp.getComputerName());
				cv.put(ComputerTable.COLUMN_DEVICE_CODE, comp.getDeviceCode());
				cv.put(ComputerTable.COLUMN_REGISTER_NUMBER, comp.getRegistrationNumber());
				cv.put(ComputerTable.COLUMN_IS_MAIN_COMPUTER, comp.getIsMainComputer());
				mSqlite.insertOrThrow(ComputerTable.TABLE_COMPUTER, null, cv);
			}
			mSqlite.setTransactionSuccessful();
		} finally{
			mSqlite.endTransaction();
		}
	}
}
