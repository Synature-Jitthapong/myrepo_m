package com.syn.mpos.dao;

import java.util.List;

import com.syn.pos.ShopData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class StaffDao extends MPOSDatabase{
	
	public StaffDao(Context context) {
		super(context);
	}

	public int countStaffs(){
		int totalStaff = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT COUNT(*) FROM " + StaffTable.TABLE_STAFF, 
				null);
		if(cursor.moveToFirst()){
			totalStaff = cursor.getInt(0);
		}
		return totalStaff;
	}
	
	/**
	 * @param staffId
	 * @return ShopData.Staff
	 */
	public ShopData.Staff getStaff(int staffId){
		ShopData.Staff s = null;
		Cursor cursor = getReadableDatabase().query(StaffTable.TABLE_STAFF, 
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
			getWritableDatabase().delete(StaffTable.TABLE_STAFF, null, null);
			for(ShopData.Staff staff : staffLst){
				ContentValues cv = new ContentValues();
				cv.put(StaffTable.COLUMN_STAFF_ID, staff.getStaffID());
				cv.put(StaffTable.COLUMN_STAFF_CODE, staff.getStaffCode());
				cv.put(StaffTable.COLUMN_STAFF_NAME, staff.getStaffName());
				cv.put(StaffTable.COLUMN_STAFF_PASS, staff.getStaffPassword());
				getWritableDatabase().insertOrThrow(StaffTable.TABLE_STAFF, null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally{
			getWritableDatabase().endTransaction();
		}
	}
	
	public static class StaffPermissionTable {

		public static final String TABLE_STAFF_PERMISSION = "StaffPermission";
		public static final String COLUMN_STAFF_ROLE_ID = "staff_role_id";
		public static final String COLUMN_PERMMISSION_ITEM_ID = "permission_item_id";
		
		private static final String SQL_CREATE =
				"CREATE TABLE " + TABLE_STAFF_PERMISSION + " ( " +
				COLUMN_STAFF_ROLE_ID + " INTEGER DEFAULT 0, " +
				COLUMN_PERMMISSION_ITEM_ID + " INTEGER DEFAULT 0);";
		
		public static void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE);
		}

		public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
	}
	
	public static class StaffTable {

		public static final String TABLE_STAFF = "Staffs";
		public static final String COLUMN_STAFF_ID = "staff_id";
		public static final String COLUMN_STAFF_CODE = "staff_code";
		public static final String COLUMN_STAFF_NAME = "staff_name";
		public static final String COLUMN_STAFF_PASS = "staff_password";

		private static final String SQL_CREATE =
				"CREATE TABLE " + TABLE_STAFF + " ( " +
				COLUMN_STAFF_ID + " INTEGER, " +
				COLUMN_STAFF_CODE + " TEXT, " +
				COLUMN_STAFF_NAME + " TEXT, " +
				COLUMN_STAFF_PASS + " TEXT, " +
				"PRIMARY KEY (" + COLUMN_STAFF_ID + "));";

		public static void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE);
		}

		public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
	}
}
