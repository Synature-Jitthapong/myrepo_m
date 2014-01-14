package com.syn.mpos.database;

import java.util.List;
import com.syn.pos.ShopData;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class Staff extends MPOSDatabase{
	public static final String TB_STAFF = "Staffs";
	public static final String COL_STAFF_ID = "StaffID";
	public static final String COL_STAFF_CODE = "StaffCode";
	public static final String COL_STAFF_NAME = "StaffName";
	public static final String COL_STAFF_PASS = "StaffPassword";
	
	public static final String TB_STAFF_PERMISSION = "StaffPermission";
	public static final String COL_STAFF_ROLE_ID = "StaffRoleId";
	public static final String COL_PERMMISSION_ITEM_ID = "PermissionItemId";
	
	public Staff(SQLiteDatabase db) {
		super(db);
	}

	public ShopData.Staff getStaff(int staffId){
		ShopData.Staff s = null;
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + TB_STAFF +
				" WHERE " + COL_STAFF_ID + "=" + staffId, null);
		if(cursor.moveToFirst()){
			s = new ShopData.Staff();
			s.setStaffCode(cursor.getString(cursor.getColumnIndex(COL_STAFF_CODE)));
			s.setStaffName(cursor.getString(cursor.getColumnIndex(COL_STAFF_NAME)));
		}
		cursor.close();
		return s;
	}
	
	public void insertStaff(List<ShopData.Staff> staffLst) throws SQLException{
		mSqlite.execSQL("DELETE FROM " + TB_STAFF);
		for(ShopData.Staff staff : staffLst){
			ContentValues cv = new ContentValues();
			cv.put(COL_STAFF_ID, staff.getStaffID());
			cv.put(COL_STAFF_CODE, staff.getStaffCode());
			cv.put(COL_STAFF_NAME, staff.getStaffName());
			cv.put(COL_STAFF_PASS, staff.getStaffPassword());
			mSqlite.insertOrThrow(TB_STAFF, null, cv);
		}
	}
	
}
