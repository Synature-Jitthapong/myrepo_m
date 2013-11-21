package com.syn.mpos.database;

import com.j1tth4.mobile.util.EncryptSHA1;
import com.j1tth4.mobile.util.Encryption;
import com.syn.pos.ShopData;
import android.content.Context;
import android.database.Cursor;

public class Login{
	private MPOSSQLiteHelper mSqlite;
	private String user;
	private String passEncrypt;
	
	public Login(Context context, String user, String pass) {
		mSqlite = new MPOSSQLiteHelper(context);
		this.user = user;
		Encryption encrypt = new EncryptSHA1();
		passEncrypt = encrypt.sha1(pass);
	}
	
	public boolean checkUser(){
		boolean isFound = false;
		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT " + Staff.COL_STAFF_CODE +
				" FROM " + Staff.TB_NAME +
				" WHERE " + Staff.COL_STAFF_CODE + "='" + user + "'");
		if(cursor.moveToFirst()){
			isFound = true;
		}
		cursor.close();
		mSqlite.close();
		return isFound;
	}
	
	public ShopData.Staff checkLogin() {
		ShopData.Staff s = null;
		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + Staff.TB_NAME +
				" WHERE " + Staff.COL_STAFF_CODE + "='" + user + "' " + 
				" AND " + Staff.COL_STAFF_PASS + "='" + passEncrypt + "'");
		if(cursor.moveToFirst()){
			s = new ShopData.Staff();
			s.setStaffID(cursor.getInt(cursor.getColumnIndex(Staff.COL_STAFF_ID)));
			s.setStaffCode(cursor.getString(cursor.getColumnIndex(Staff.COL_STAFF_CODE)));
			s.setStaffName(cursor.getString(cursor.getColumnIndex(Staff.COL_STAFF_NAME)));
			cursor.moveToNext();
		}
		cursor.close();
		mSqlite.close();
		return s;
	}
}
