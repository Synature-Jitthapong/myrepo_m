package com.syn.mpos.database;

import java.util.List;

import com.syn.pos.ShopData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class StaffDataSource extends MPOSDatabase{
	
	public StaffDataSource(Context context) {
		super(context);
	}

	/**
	 * @param staffId
	 * @return ShopData.Staff
	 */
	public ShopData.Staff getStaff(int staffId){
		ShopData.Staff s = null;
		Cursor cursor = getReadableDatabase().query(StaffTable.TABLE_NAME, 
				new String[]{StaffTable.COLUMN_STAFF_CODE, 
				StaffTable.COLUMN_STAFF_NAME}, 
				StaffTable.COLUMN_STAFF_ID + "=?", 
				new String[]{String.valueOf(staffId)}, null, null, null);
		if(cursor.moveToFirst()){
			s = new ShopData.Staff();
			s.setStaffCode(cursor.getString(cursor.getColumnIndex(StaffTable.COLUMN_STAFF_CODE)));
			s.setStaffName(cursor.getString(cursor.getColumnIndex(StaffTable.COLUMN_STAFF_NAME)));
		}
		cursor.close();
		return s;
	}
	
	/**
	 * @param staffLst
	 * @throws SQLException
	 */
	public void insertStaff(List<ShopData.Staff> staffLst) throws SQLException{
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(StaffTable.TABLE_NAME, null, null);
			for(ShopData.Staff staff : staffLst){
				ContentValues cv = new ContentValues();
				cv.put(StaffTable.COLUMN_STAFF_ID, staff.getStaffID());
				cv.put(StaffTable.COLUMN_STAFF_CODE, staff.getStaffCode());
				cv.put(StaffTable.COLUMN_STAFF_NAME, staff.getStaffName());
				cv.put(StaffTable.COLUMN_STAFF_PASS, staff.getStaffPassword());
				getWritableDatabase().insertOrThrow(StaffTable.TABLE_NAME, null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally{
			getWritableDatabase().endTransaction();
		}
	}
}
