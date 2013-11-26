package com.syn.mpos.database;

import java.util.List;
import com.syn.pos.ShopData;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class Staff extends MPOSSQLiteHelper{
	public static final String TB_STAFF = "Staff";
	public static final String COL_STAFF_ID = "StaffID";
	public static final String COL_STAFF_CODE = "StaffCode";
	public static final String COL_STAFF_NAME = "StaffName";
	public static final String COL_STAFF_PASS = "StaffPassword";
	
	public Staff(Context c) {
		super(c);
	}

	public ShopData.Staff getStaff(int staffId){
		ShopData.Staff s = null;
		open();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + TB_STAFF +
				" WHERE " + COL_STAFF_ID + "=" + staffId, null);
		if(cursor.moveToFirst()){
			s = new ShopData.Staff();
			s.setStaffCode(cursor.getString(cursor.getColumnIndex(COL_STAFF_CODE)));
			s.setStaffName(cursor.getString(cursor.getColumnIndex(COL_STAFF_NAME)));
		}
		cursor.close();
		close();
		return s;
	}
	
	public void addStaff(List<ShopData.Staff> staffLst) throws SQLException{
		open();
		mSqlite.execSQL("DELETE FROM " + TB_STAFF);
		for(ShopData.Staff staff : staffLst){
			ContentValues cv = new ContentValues();
			cv.put(COL_STAFF_ID, staff.getStaffID());
			cv.put(COL_STAFF_CODE, staff.getStaffCode());
			cv.put(COL_STAFF_NAME, staff.getStaffName());
			cv.put(COL_STAFF_PASS, staff.getStaffPassword());
			mSqlite.insertOrThrow(TB_STAFF, null, cv);
		}
		close();
	}
	
}
