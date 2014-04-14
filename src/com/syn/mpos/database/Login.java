package com.syn.mpos.database;

import com.j1tth4.mobile.util.EncryptSHA1;
import com.j1tth4.mobile.util.Encryption;
import com.syn.pos.ShopData;

import android.content.Context;
import android.database.Cursor;

public class Login extends MPOSDatabase{
	private String user;
	private String passEncrypt;
	
	public Login(Context c, String user, String pass) {
		super(c);
		this.user = user;
		Encryption encrypt = new EncryptSHA1();
		passEncrypt = encrypt.sha1(pass);
	}
	
	public boolean checkUser(){
		boolean isFound = false;
		Cursor cursor = mSqlite.query(StaffTable.TABLE_NAME, 
				new String[]{StaffTable.COLUMN_STAFF_CODE}, 
				StaffTable.COLUMN_STAFF_CODE + "=?", 
				new String[]{user}, null, null, null);
		if(cursor.moveToFirst()){
			isFound = true;
		}
		cursor.close();
		return isFound;
	}
	
	public ShopData.Staff checkLogin() {
		ShopData.Staff s = null;
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " 
				+ StaffTable.TABLE_NAME
				+ " WHERE " + StaffTable.COLUMN_STAFF_CODE + "=?" 
				+ " AND " + StaffTable.COLUMN_STAFF_PASS + "=?", 
				new String[]{user, passEncrypt});
		if(cursor.moveToFirst()){
			s = new ShopData.Staff();
			s.setStaffID(cursor.getInt(cursor.getColumnIndex(StaffTable.COLUMN_STAFF_ID)));
			s.setStaffCode(cursor.getString(cursor.getColumnIndex(StaffTable.COLUMN_STAFF_CODE)));
			s.setStaffName(cursor.getString(cursor.getColumnIndex(StaffTable.COLUMN_STAFF_NAME)));
			cursor.moveToNext();
		}
		cursor.close();
		return s;
	}
}
