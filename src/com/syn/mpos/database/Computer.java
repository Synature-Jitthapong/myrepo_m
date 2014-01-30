package com.syn.mpos.database;

import java.util.List;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.syn.pos.ShopData;

public class Computer extends MPOSDatabase{
	public static final String TB_COMPUTER = "Computer";
	public static final String COL_COMPUTER_ID = "ComputerId";
	public static final String COL_COMPUTER_NAME = "ComputerName";
	public static final String COL_DEVICE_CODE = "DeviceCode";
	public static final String COL_REGISTER_NUMBER = "RegisterNumber";
	public static final String COL_IS_MAIN_COMPUTER = "IsMainComputer";
		
	public Computer(SQLiteDatabase db) {
		super(db);
	}

	public boolean checkIsMainComputer(int computerId){
		boolean isMainComputer = false;
		Cursor cursor = mSqlite.query(TB_COMPUTER, 
				new String[]{
					COL_IS_MAIN_COMPUTER
				}, COL_COMPUTER_ID + "=?", 
				new String[]{
					String.valueOf(computerId)
				}, null, null, null);
		if(cursor.moveToFirst()){
			if(cursor.getInt(cursor.getColumnIndex(COL_IS_MAIN_COMPUTER)) != 0)
				isMainComputer = true;
		}
		cursor.close();
		return isMainComputer;
	}
	
	public ShopData.ComputerProperty getComputerProperty() {
		ShopData.ComputerProperty computer = 
				new ShopData.ComputerProperty();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + TB_COMPUTER, null);
		if (cursor.moveToFirst()) {
			computer.setComputerID(cursor.getInt(cursor.getColumnIndex(COL_COMPUTER_ID)));
			computer.setComputerName(cursor.getString(cursor.getColumnIndex(COL_COMPUTER_NAME)));
			computer.setDeviceCode(cursor.getString(cursor.getColumnIndex(COL_DEVICE_CODE)));
			computer.setRegistrationNumber(cursor.getString(cursor.getColumnIndex(COL_REGISTER_NUMBER)));
			cursor.moveToNext();
		}
		cursor.close();
		return computer;
	}

	public void insertComputer(List<ShopData.ComputerProperty> compLst) throws SQLException{
		mSqlite.execSQL("DELETE FROM " + TB_COMPUTER);
		for (ShopData.ComputerProperty comp : compLst) {
			ContentValues cv = new ContentValues();
			cv.put(COL_COMPUTER_ID, comp.getComputerID());
			cv.put(COL_COMPUTER_NAME, comp.getComputerName());
			cv.put(COL_DEVICE_CODE, comp.getDeviceCode());
			cv.put(COL_REGISTER_NUMBER, comp.getRegistrationNumber());
			mSqlite.insertOrThrow(TB_COMPUTER, null, cv);
		}
	}
}
