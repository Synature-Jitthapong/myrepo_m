package com.syn.mpos.datasource;

import com.j1tth4.mobile.util.EncryptSHA1;
import com.j1tth4.mobile.util.Encryption;
import com.syn.mpos.datasource.Staff.StaffEntry;
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
		Cursor cursor = mSqlite.query(StaffEntry.TABLE_STAFF, 
				new String[]{StaffEntry.COLUMN_STAFF_CODE}, 
				StaffEntry.COLUMN_STAFF_CODE + "=?", 
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
				+ StaffEntry.TABLE_STAFF
				+ " WHERE " + StaffEntry.COLUMN_STAFF_CODE + "=?" 
				+ " AND " + StaffEntry.COLUMN_STAFF_PASS + "=?", 
				new String[]{user, passEncrypt});
		if(cursor.moveToFirst()){
			s = new ShopData.Staff();
			s.setStaffID(cursor.getInt(cursor.getColumnIndex(StaffEntry.COLUMN_STAFF_ID)));
			s.setStaffCode(cursor.getString(cursor.getColumnIndex(StaffEntry.COLUMN_STAFF_CODE)));
			s.setStaffName(cursor.getString(cursor.getColumnIndex(StaffEntry.COLUMN_STAFF_NAME)));
			cursor.moveToNext();
		}
		cursor.close();
		return s;
	}
}
