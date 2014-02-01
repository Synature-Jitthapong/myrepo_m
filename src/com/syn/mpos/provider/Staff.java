package com.syn.mpos.provider;

import java.util.List;
import com.syn.pos.ShopData;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class Staff extends MPOSDatabase{
	public static final String TABLE_STAFF = "Staffs";
	public static final String COLUMN_STAFF_ID = "staff_id";
	public static final String COLUMN_STAFF_CODE = "staff_code";
	public static final String COLUMN_STAFF_NAME = "staff_name";
	public static final String COLUMN_STAFF_PASS = "staff_password";
	
	public static final String TABLE_STAFF_PERMISSION = "StaffPermission";
	public static final String COLUMN_STAFF_ROLE_ID = "staff_role_id";
	public static final String COLUMN_PERMMISSION_ITEM_ID = "permission_item_id";
	
	public Staff(SQLiteDatabase db) {
		super(db);
	}

	public ShopData.Staff getStaff(int staffId){
		ShopData.Staff s = null;
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + TABLE_STAFF +
				" WHERE " + COLUMN_STAFF_ID + "=?", new String[]{String.valueOf(staffId)});
		if(cursor.moveToFirst()){
			s = new ShopData.Staff();
			s.setStaffCode(cursor.getString(cursor.getColumnIndex(COLUMN_STAFF_CODE)));
			s.setStaffName(cursor.getString(cursor.getColumnIndex(COLUMN_STAFF_NAME)));
		}
		cursor.close();
		return s;
	}
	
	public void insertStaff(List<ShopData.Staff> staffLst) throws SQLException{
		mSqlite.delete(TABLE_STAFF, null, null);
		for(ShopData.Staff staff : staffLst){
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_STAFF_ID, staff.getStaffID());
			cv.put(COLUMN_STAFF_CODE, staff.getStaffCode());
			cv.put(COLUMN_STAFF_NAME, staff.getStaffName());
			cv.put(COLUMN_STAFF_PASS, staff.getStaffPassword());
			mSqlite.insertOrThrow(TABLE_STAFF, null, cv);
		}
	}
	
}
