package com.syn.mpos.database;

import com.j1tth4.mobile.sqlite.SQLiteHelper;
import com.j1tth4.mobile.util.EncryptSHA1;
import com.j1tth4.mobile.util.Encryption;
import com.syn.pos.ShopData;

import android.content.Context;
import android.database.Cursor;

public class Login{

	private SQLiteHelper dbHelper;
	private String user;
	private String passEncrypt;
	
	public Login(Context context, String user, String pass) {
		dbHelper = new MPOSSQLiteHelper(context);
		this.user = user;
		Encryption encrypt = new EncryptSHA1();
		passEncrypt = encrypt.sha1(pass);
	}
	
	public boolean checkUser(){
		boolean isFound = false;
		
		String strSql = "SELECT staff_code " +
				" FROM staffs " +
				" WHERE staff_code='" + user + "'";
		
		dbHelper.open();
		
		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			isFound = true;
		}
		cursor.close();
		
		dbHelper.close();
		
		return isFound;
	}
	
	public ShopData.Staff checkLogin() {
		ShopData.Staff s = null;
		
		String strSql = "SELECT * FROM staffs " +
				" WHERE staff_code='" + user + "' " + 
				" AND staff_password='" + passEncrypt + "'";
		
		dbHelper.open();

		Cursor cursor = dbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			s = new ShopData.Staff();
			s.setStaffID(cursor.getInt(cursor.getColumnIndex("staff_id")));
			s.setStaffCode(cursor.getString(cursor.getColumnIndex("staff_code")));
			s.setStaffName(cursor.getString(cursor.getColumnIndex("staff_name")));
			
			cursor.moveToNext();
		}
		cursor.close();
		
		dbHelper.close();
		return s;
	}
}
