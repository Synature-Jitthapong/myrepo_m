package com.syn.mpos.database;

import java.util.List;

import com.syn.pos.ShopData;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class Staff extends MPOSDatabase{
	
	public Staff(SQLiteDatabase db) {
		super(db);
	}

	public ShopData.Staff getStaff(int staffId){
		ShopData.Staff s = null;
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + StaffTable.TABLE_NAME +
				" WHERE " + StaffTable.COLUMN_STAFF_ID + "=?", new String[]{String.valueOf(staffId)});
		if(cursor.moveToFirst()){
			s = new ShopData.Staff();
			s.setStaffCode(cursor.getString(cursor.getColumnIndex(StaffTable.COLUMN_STAFF_CODE)));
			s.setStaffName(cursor.getString(cursor.getColumnIndex(StaffTable.COLUMN_STAFF_NAME)));
		}
		cursor.close();
		return s;
	}
	
	public void insertStaff(List<ShopData.Staff> staffLst) throws SQLException{
		mSqlite.beginTransaction();
		try {
			mSqlite.delete(StaffTable.TABLE_NAME, null, null);
			for(ShopData.Staff staff : staffLst){
				ContentValues cv = new ContentValues();
				cv.put(StaffTable.COLUMN_STAFF_ID, staff.getStaffID());
				cv.put(StaffTable.COLUMN_STAFF_CODE, staff.getStaffCode());
				cv.put(StaffTable.COLUMN_STAFF_NAME, staff.getStaffName());
				cv.put(StaffTable.COLUMN_STAFF_PASS, staff.getStaffPassword());
				mSqlite.insertOrThrow(StaffTable.TABLE_NAME, null, cv);
			}
			mSqlite.setTransactionSuccessful();
		} finally{
			mSqlite.endTransaction();
		}
	}
}
