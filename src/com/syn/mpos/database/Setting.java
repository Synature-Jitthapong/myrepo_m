package com.syn.mpos.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class Setting extends MPOSSQLiteHelper{
	public static final String TB_CONNECTION_CONFIG = "ConnectionConfig";
	public static final String COL_ADDR = "Addr";
	public static final String COL_BACKOFFICE = "BackOffice";
	
	public static final String TB_PRINTER_CONFIG = "PrinterConfig";
	public static final String COL_PRINTER_IP = "PrinterIp";
	
	public static String mMenuImageUrl;

	public Setting(Context c){
		super(c);
	}

	public Printer getPrinter(){
		Printer p = new Printer();
		
		open();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + TB_PRINTER_CONFIG, null);
		if(cursor.moveToFirst()){
			p.setPrinterIp(cursor.getString(cursor.getColumnIndex(COL_PRINTER_IP)));
		}
		cursor.close();
		close();
		return p;
	}
	
	public Connection getConnection(){
		Connection conn = new Connection();
		
		open();
		Cursor cursor = mSqlite.rawQuery("SELECT * FROM " + TB_CONNECTION_CONFIG, null);
		if(cursor.moveToFirst()){
			conn.setAddress(cursor.getString(cursor.getColumnIndex(COL_ADDR)));
			conn.setBackoffice(cursor.getString(cursor.getColumnIndex(COL_BACKOFFICE)));
			conn.setFullUrl(conn.getProtocal() + conn.getAddress() + 
					"/" + conn.getBackoffice() + "/" + conn.getService());
			mMenuImageUrl = conn.getProtocal() + conn.getAddress() + "/" + 
					conn.getBackoffice() + "/Resources/Shop/MenuImage/";
		}
		cursor.close();
		close();
		return conn;
	}
	
	public void addPrinterSetting(String printerIp) throws SQLException{
		ContentValues cv = new ContentValues();
		cv.put(COL_PRINTER_IP, printerIp);
		
		open();
		mSqlite.execSQL("DELETE FROM " + TB_PRINTER_CONFIG);
		mSqlite.insertOrThrow(TB_PRINTER_CONFIG, null, cv);
		close();
	}
	
	public void addConnectionConfig(String address, String backoffice) throws SQLException{
		ContentValues cv = new ContentValues();
		cv.put(COL_ADDR, address);
		cv.put(COL_BACKOFFICE, backoffice);
		
		open();
		mSqlite.execSQL("DELETE FROM " + TB_CONNECTION_CONFIG);
		mSqlite.insertOrThrow(TB_CONNECTION_CONFIG, null, cv);
		close();
	}
	
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
