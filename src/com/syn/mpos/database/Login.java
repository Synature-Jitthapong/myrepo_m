package com.syn.mpos.database;

import com.j1tth4.mobile.util.EncryptSHA1;
import com.j1tth4.mobile.util.Encryption;
import com.syn.pos.ShopData;

import android.content.Context;
import android.database.Cursor;

public class Login extends MPOSDatabase{
	
	private String mUser;
	private String mPassEncrypt;
	
	public Login(Context context, String user, String pass) {
		super(context);
		mUser = user;
		Encryption encrypt = new EncryptSHA1();
		mPassEncrypt = encrypt.sha1(pass);
	}
	
	protected boolean checkUser(){
		boolean isFound = false;
		Cursor cursor = getReadableDatabase().query(StaffTable.TABLE_NAME, 
				new String[]{StaffTable.COLUMN_STAFF_CODE}, 
				StaffTable.COLUMN_STAFF_CODE + "=?", 
				new String[]{mUser}, null, null, null);
		if(cursor.moveToFirst()){
			isFound = true;
		}
		cursor.close();
		return isFound;
	}
	
	protected ShopData.Staff checkLogin() {
		ShopData.Staff s = null;
		Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " 
				+ StaffTable.TABLE_NAME
				+ " WHERE " + StaffTable.COLUMN_STAFF_CODE + "=?" 
				+ " AND " + StaffTable.COLUMN_STAFF_PASS + "=?", 
				new String[]{mUser, mPassEncrypt});
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
