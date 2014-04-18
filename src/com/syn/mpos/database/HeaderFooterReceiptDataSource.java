package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.syn.pos.ShopData;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class HeaderFooterReceiptDataSource extends MPOSDatabase{
	
	public static final int HEADER_LINE_TYPE = 0;
	public static final int FOOTER_LINE_TYPE = 1;

	public HeaderFooterReceiptDataSource(SQLiteDatabase db) {
		super(db);
	}
	
	public List<ShopData.HeaderFooterReceipt> listHeaderFooter(int lineType){
		List<ShopData.HeaderFooterReceipt> hfLst = 
				new ArrayList<ShopData.HeaderFooterReceipt>();

		Cursor cursor = mSqlite.query(HeaderFooterReceiptTable.TABLE_NAME, 
				new String[]{HeaderFooterReceiptTable.COLUMN_TEXT_IN_LINE, 
				HeaderFooterReceiptTable.COLUMN_LINE_TYPE, 
				HeaderFooterReceiptTable.COLUMN_LINE_ORDER}, 
				HeaderFooterReceiptTable.COLUMN_LINE_TYPE + "=?", 
				new String[]{String.valueOf(lineType)}, null, null, 
				HeaderFooterReceiptTable.COLUMN_LINE_ORDER);
		if(cursor.moveToFirst()){
			do{
				ShopData.HeaderFooterReceipt hf = new ShopData.HeaderFooterReceipt();
				hf.setTextInLine(cursor.getString(cursor.getColumnIndex(HeaderFooterReceiptTable.COLUMN_TEXT_IN_LINE)));
				hf.setLineType(cursor.getInt(cursor.getColumnIndex(HeaderFooterReceiptTable.COLUMN_LINE_TYPE)));
				hf.setLineOrder(cursor.getInt(cursor.getColumnIndex(HeaderFooterReceiptTable.COLUMN_LINE_ORDER)));
				hfLst.add(hf);
			}while(cursor.moveToNext());
		}
		cursor.close();

		return hfLst;
	}
	
	public void addHeaderFooterReceipt(
		List<ShopData.HeaderFooterReceipt> headerFooterLst) throws SQLException{
		mSqlite.beginTransaction();
		try {
			mSqlite.delete(HeaderFooterReceiptTable.TABLE_NAME, null, null);
			for(ShopData.HeaderFooterReceipt hf : headerFooterLst){
				ContentValues cv = new ContentValues();
				cv.put(HeaderFooterReceiptTable.COLUMN_TEXT_IN_LINE, hf.getTextInLine());
				cv.put(HeaderFooterReceiptTable.COLUMN_LINE_TYPE, hf.getLineType());
				cv.put(HeaderFooterReceiptTable.COLUMN_LINE_ORDER, hf.getLineOrder());
				mSqlite.insertOrThrow(HeaderFooterReceiptTable.TABLE_NAME, null, cv);
			}
			mSqlite.setTransactionSuccessful();
		} finally {
			mSqlite.endTransaction();
		}
	}
}
