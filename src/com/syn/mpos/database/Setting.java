package com.syn.mpos.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class Setting {
	private MPOSSQLiteHelper mSqlite; 
	private String menuImageUrl;

	public Setting(Context c){
		mSqlite = new MPOSSQLiteHelper(c);
	}
	
	public String getMenuImageUrl() {
		return menuImageUrl;
	}

	public void setMenuImageUrl(String menuImageUrl) {
		this.menuImageUrl = menuImageUrl;
	}

	public Printer getPrinter(){
		Printer p = new Printer();
		
		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM printer_setting");
		if(cursor.moveToFirst()){
			p.setPrinterIp(cursor.getString(cursor.getColumnIndex("printer_ip")));
		}
		cursor.close();
		mSqlite.close();
		return p;
	}
	
	public Connection getConnection(){
		Connection conn = new Connection();
		
		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM conn_setting");
		if(cursor.moveToFirst()){
			conn.setAddress(cursor.getString(cursor.getColumnIndex("address")));
			conn.setBackoffice(cursor.getString(cursor.getColumnIndex("backoffice")));
		}
		cursor.close();
		mSqlite.close();
		return conn;
	}
	
	public boolean deleteSyncItem(int itemId){
		boolean isSuccess = false;
		
		mSqlite.open();
		isSuccess = mSqlite.execSQL("DELETE FROM sync_item WHERE sync_item_id=" + itemId);
		mSqlite.close();
		return isSuccess;
	}
	
	public SyncItem getSyncItem(int syncItemId){
		SyncItem syncItem = new SyncItem();
		
		mSqlite.open();
		Cursor cursor = mSqlite.rawQuery("SELECT * " +
				" FROM sync_item " +
				" WHERE sync_item_id=" + syncItemId);
		if(cursor.moveToFirst()){
			syncItem.setSyncItemId(syncItemId);
			syncItem.setSyncItemName(cursor.getString(cursor.getColumnIndex("sync_name")));
			syncItem.setSyncTime(cursor.getLong(cursor.getColumnIndex("sync_time")));
			syncItem.setSyncEnabled(cursor.getInt(cursor.getColumnIndex("sync_enabled")) == 1 ? true : false);
			syncItem.setSyncStatus(cursor.getInt(cursor.getColumnIndex("sync_status")));
		}
		cursor.close();
		mSqlite.close();
		return syncItem;
	}
	
	public boolean addSyncItem(int syncItemId, boolean enable, String syncItemName, 
			int syncStatus, long syncTime){
		boolean isSuccess = false;
		
		ContentValues cv = new ContentValues();
		cv.put("sync_item_id", syncItemId);
		cv.put("sync_enable", enable == true ? 1 : 0);
		cv.put("sync_item_name", syncItemName);
		cv.put("sync_time", syncTime);
		cv.put("sync_status", syncStatus);
		
		mSqlite.open();
		mSqlite.execSQL("DELETE FROM sync_item WHERE sync_item_id=" + syncItemId);
		isSuccess = mSqlite.insert("sync_item", cv);
		mSqlite.close();
		return isSuccess;
	}
	
	public boolean addPrinterSetting(String printerIp){
		boolean isSuccess = false;
		ContentValues cv = new ContentValues();
		cv.put("printer_ip", printerIp);
		
		mSqlite.open();
		mSqlite.execSQL("DELETE FROM printer_setting");
		isSuccess = mSqlite.insert("printer_setting", cv);
		mSqlite.close();
		return isSuccess;
	}
	
	public boolean addConnSetting(String address, String backoffice){
		boolean isSuccess = false;
		ContentValues cv = new ContentValues();
		cv.put("address", address);
		cv.put("backoffice", backoffice);
		
		mSqlite.open();
		mSqlite.execSQL("DELETE FROM conn_setting");
		isSuccess = mSqlite.insert("conn_setting", cv);
		mSqlite.close();
		return isSuccess;
	}
	
//	public String getMenuImageUrl() {
//		return menuImageUrl;
//	}
//
//	public void setMenuImageUrl(String menuImageUrl) {
//		this.menuImageUrl = menuImageUrl;
//	}

	public static class Printer{
		private String printerIp;

		public String getPrinterIp() {
			return printerIp;
		}

		public void setPrinterIp(String printerIp) {
			this.printerIp = printerIp;
		}
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
		
		@Override
		public String toString() {
			return syncItemName;
		}
	}
	
	public static class Connection{
		private String protocal = "http://";
		private String address;
		private String backoffice;
		private String service = "ws_mpos.asmx";
		private String fullUrl;
		
		public String getProtocal() {
			return protocal;
		}
		public void setProtocal(String protocal) {
			this.protocal = protocal;
		}
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public String getBackoffice() {
			return backoffice;
		}
		public void setBackoffice(String backoffice) {
			this.backoffice = backoffice;
		}
		public String getService() {
			return service;
		}
		public void setService(String service) {
			this.service = service;
		}
		public String getFullUrl() {
			return fullUrl;
		}
		public void setFullUrl(String fullUrl) {
			this.fullUrl = fullUrl;
		}
	}
}
