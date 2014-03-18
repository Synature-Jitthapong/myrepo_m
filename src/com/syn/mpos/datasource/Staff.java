package com.syn.mpos.datasource;

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
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + StaffEntry.TABLE_STAFF +
				" WHERE " + StaffEntry.COLUMN_STAFF_ID + "=?", new String[]{String.valueOf(staffId)});
		if(cursor.moveToFirst()){
			s = new ShopData.Staff();
			s.setStaffCode(cursor.getString(cursor.getColumnIndex(StaffEntry.COLUMN_STAFF_CODE)));
			s.setStaffName(cursor.getString(cursor.getColumnIndex(StaffEntry.COLUMN_STAFF_NAME)));
		}
		cursor.close();
		return s;
	}
	
	public void insertStaff(List<ShopData.Staff> staffLst) throws SQLException{
		mSqlite.delete(StaffEntry.TABLE_STAFF, null, null);
		for(ShopData.Staff staff : staffLst){
			ContentValues cv = new ContentValues();
			cv.put(StaffEntry.COLUMN_STAFF_ID, staff.getStaffID());
			cv.put(StaffEntry.COLUMN_STAFF_CODE, staff.getStaffCode());
			cv.put(StaffEntry.COLUMN_STAFF_NAME, staff.getStaffName());
			cv.put(StaffEntry.COLUMN_STAFF_PASS, staff.getStaffPassword());
			mSqlite.insertOrThrow(StaffEntry.TABLE_STAFF, null, cv);
		}
	}
	
	public static abstract class StaffPermissionEntry{
		public static final String TABLE_STAFF_PERMISSION = "StaffPermission";
		public static final String COLUMN_STAFF_ROLE_ID = "staff_role_id";
		public static final String COLUMN_PERMMISSION_ITEM_ID = "permission_item_id";
	}
	
	public static abstract class StaffEntry{
		public static final String TABLE_STAFF = "Staffs";
		public static final String COLUMN_STAFF_ID = "staff_id";
		public static final String COLUMN_STAFF_CODE = "staff_code";
		public static final String COLUMN_STAFF_NAME = "staff_name";
		public static final String COLUMN_STAFF_PASS = "staff_password";
	}
}
