package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class Sync {

	private MPOSSQLiteHelper mSqlite;
	
	public Sync(Context context){
		mSqlite = new MPOSSQLiteHelper(context);
	}
	
	public List<SyncItem> listSync(){
		List<SyncItem> syncLst = new ArrayList<SyncItem>();
		
		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM sync_item");
		if(cursor.moveToFirst()){
			do{
				SyncItem sync = new SyncItem();
				sync.setSyncItemId(cursor.getInt(cursor.getColumnIndex("sync_item_id")));
				sync.setSyncItemName(cursor.getString(cursor.getColumnIndex("sync_name")));
				sync.setSyncEnabled(cursor.getInt(cursor.getColumnIndex("sync_endabled")) == 1 ? true : false);
				sync.setSyncStatus(cursor.getInt(cursor.getColumnIndex("sync_status")));
				sync.setSyncTime(cursor.getLong(cursor.getColumnIndex("sync_time")));
				syncLst.add(sync);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mSqlite.close();
		return syncLst;
	}
	
	public boolean deleteSyncItem(int itemId){
		boolean isSuccess = false;
		mSqlite.open();
		isSuccess = mSqlite.execSQL("DELETE FROM sync_item " +
				"WHERE sync_item=" + itemId);
		mSqlite.close();
		return isSuccess;
	}
	
	public boolean insertSyncItem(String syncName, boolean enabled){
		boolean isSuccess = false;
		ContentValues cv = new ContentValues();
		cv.put("sync_name", syncName);
		cv.put("sync_enabled", enabled == true ? 1 : 0);
		
		mSqlite.open();
		isSuccess = mSqlite.insert("sync_item", cv);
		mSqlite.close();
		return isSuccess;
	}
	
	public static class SyncItem{
		private int syncItemId;
		private String syncItemName;
		private boolean syncEnabled;
		private long syncTime;
		private int syncStatus;
		
		public int getSyncItemId() {
			return syncItemId;
		}
		public void setSyncItemId(int syncItemId) {
			this.syncItemId = syncItemId;
		}
		public String getSyncItemName() {
			return syncItemName;
		}
		public void setSyncItemName(String syncItemName) {
			this.syncItemName = syncItemName;
		}
		public boolean isSyncEnabled() {
			return syncEnabled;
		}
		public void setSyncEnabled(boolean syncEnabled) {
			this.syncEnabled = syncEnabled;
		}
		public long getSyncTime() {
			return syncTime;
		}
		public void setSyncTime(long syncTime) {
			this.syncTime = syncTime;
		}
		public int getSyncStatus() {
			return syncStatus;
		}
		public void setSyncStatus(int syncStatus) {
			this.syncStatus = syncStatus;
		}
	}
}
