package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class Setting {
	private MPOSSettingHelper mDbHelper; 
	private String menuImageUrl;

	public Setting(Context c){
		mDbHelper = new MPOSSettingHelper(c);
	}
	
	public String getMenuImageUrl() {
		return menuImageUrl;
	}

	public void setMenuImageUrl(String menuImageUrl) {
		this.menuImageUrl = menuImageUrl;
	}

	public Printer getPrinter(){
		Printer p = new Printer();
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery("SELECT * FROM printer_setting");
		if(cursor.moveToFirst()){
			p.setPrinterIp(cursor.getString(cursor.getColumnIndex("printer_ip")));
		}
		cursor.close();
		mDbHelper.close();
		return p;
	}
	
	public SyncItem getSyncItem(int itemId){
		SyncItem syncItem = new SyncItem();
		String strSql = "SELECT * FROM sync_item WHERE sync_item_id=" + itemId;
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			syncItem.setSyncItemId(cursor.getInt(cursor.getColumnIndex("sync_item_id")));
			syncItem.setSyncEnable(cursor.getInt(cursor.getColumnIndex("sync_enable")) == 1 ? true : false);
			syncItem.setSyncItemName(cursor.getString(cursor.getColumnIndex("sync_item_name")));
			syncItem.setSyncAlready(cursor.getInt(cursor.getColumnIndex("sync_already")) == 1 ? true : false);
			syncItem.setSyncTime(cursor.getLong(cursor.getColumnIndex("sync_time")));
		}
		cursor.close();
		mDbHelper.close();
		return syncItem;
	}
	
	public List<SyncItem> listSyncItem(){
		List<SyncItem> syncLst = new ArrayList<SyncItem>();
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery("SELECT * FROM sync_item");
		if(cursor.moveToFirst()){
			do{
				SyncItem syncItem = new SyncItem();
				syncItem.setSyncItemId(cursor.getInt(cursor.getColumnIndex("sync_item_id")));
				syncItem.setSyncEnable(cursor.getInt(cursor.getColumnIndex("sync_enable")) == 1 ? true : false);
				syncItem.setSyncItemName(cursor.getString(cursor.getColumnIndex("sync_item_name")));
				syncItem.setSyncAlready(cursor.getInt(cursor.getColumnIndex("sync_already")) == 1 ? true : false);
				syncItem.setSyncTime(cursor.getLong(cursor.getColumnIndex("sync_time")));
				syncLst.add(syncItem);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mDbHelper.close();
		return syncLst;
	}
	
	public Connection getConnection(){
		Connection conn = new Connection();
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery("SELECT * FROM conn_setting");
		if(cursor.moveToFirst()){
			conn.setAddress(cursor.getString(cursor.getColumnIndex("address")));
			conn.setBackoffice(cursor.getString(cursor.getColumnIndex("backoffice")));
		}
		cursor.close();
		mDbHelper.close();
		return conn;
	}
	
	public boolean deleteSyncItem(int itemId){
		boolean isSuccess = false;
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL("DELETE FROM sync_item WHERE sync_item_id=" + itemId);
		mDbHelper.close();
		return isSuccess;
	}
	
	public boolean addSyncItem(boolean enable, String syncItemName, long syncTime){
		boolean isSuccess = false;
		ContentValues cv = new ContentValues();
		cv.put("sync_enable", enable == true ? 1 : 0);
		cv.put("sync_item_name", syncItemName);
		cv.put("sync_time", syncTime);
		cv.put("sync_already", 0);
		
		mDbHelper.open();
		isSuccess = mDbHelper.insert("sync_item", cv);
		mDbHelper.close();
		return isSuccess;
	}
	
	public boolean addPrinterSetting(String printerIp){
		boolean isSuccess = false;
		ContentValues cv = new ContentValues();
		cv.put("printer_ip", printerIp);
		
		mDbHelper.open();
		mDbHelper.execSQL("DELETE FROM printer_setting");
		isSuccess = mDbHelper.insert("printer_setting", cv);
		mDbHelper.close();
		return isSuccess;
	}
	
	public boolean addConnSetting(String address, String backoffice){
		boolean isSuccess = false;
		ContentValues cv = new ContentValues();
		cv.put("address", address);
		cv.put("backoffice", backoffice);
		
		mDbHelper.open();
		mDbHelper.execSQL("DELETE FROM conn_setting");
		isSuccess = mDbHelper.insert("conn_setting", cv);
		mDbHelper.close();
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
		private boolean syncEnable;
		private String syncItemName;
		private long syncTime;
		private boolean syncAlready;
		
		public int getSyncItemId() {
			return syncItemId;
		}
		public void setSyncItemId(int syncItemId) {
			this.syncItemId = syncItemId;
		}
		public boolean isSyncEnable() {
			return syncEnable;
		}
		public void setSyncEnable(boolean syncEnable) {
			this.syncEnable = syncEnable;
		}
		public String getSyncItemName() {
			return syncItemName;
		}
		public void setSyncItemName(String syncItemName) {
			this.syncItemName = syncItemName;
		}
		public long getSyncTime() {
			return syncTime;
		}
		public void setSyncTime(long syncTime) {
			this.syncTime = syncTime;
		}
		public boolean isSyncAlready() {
			return syncAlready;
		}
		public void setSyncAlready(boolean syncAlready) {
			this.syncAlready = syncAlready;
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
