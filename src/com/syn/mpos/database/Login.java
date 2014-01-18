package com.syn.mpos.database;

import com.j1tth4.mobile.util.EncryptSHA1;
import com.j1tth4.mobile.util.Encryption;
import com.syn.pos.ShopData;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Login extends MPOSDatabase{
	private String user;
	private String passEncrypt;
	
	public Login(SQLiteDatabase db, String user, String pass) {
		super(db);
		this.user = user;
		Encryption encrypt = new EncryptSHA1();
		passEncrypt = encrypt.sha1(pass);
	}
	
	public boolean checkUser(){
		boolean isFound = false;
		Cursor cursor = mSqlite.query(Staff.TB_STAFF, 
				new String[]{Staff.COL_STAFF_CODE}, 
				Staff.COL_STAFF_CODE + "=?", 
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
				+ Staff.TB_STAFF
				+ " WHERE " + Staff.COL_STAFF_CODE + "=?" 
				+ " AND " + Staff.COL_STAFF_PASS + "=?", 
				new String[]{user, passEncrypt});
		if(cursor.moveToFirst()){
			s = new ShopData.Staff();
			s.setStaffID(cursor.getInt(cursor.getColumnIndex(Staff.COL_STAFF_ID)));
			s.setStaffCode(cursor.getString(cursor.getColumnIndex(Staff.COL_STAFF_CODE)));
			s.setStaffName(cursor.getString(cursor.getColumnIndex(Staff.COL_STAFF_NAME)));
			cursor.moveToNext();
		}
		cursor.close();
		return s;
	}
}
