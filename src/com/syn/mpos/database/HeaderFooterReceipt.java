package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;
import com.syn.pos.ShopData;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class HeaderFooterReceipt extends MPOSDatabase{
	public static final int HEADER_LINE_TYPE = 0;
	public static final int FOOTER_LINE_TYPE = 1;

	public static final String TABLE_HEADER_FOOTER_RECEIPT = "HeaderFooterReceipt";
	public static final String COLUMN_TEXT_IN_LINE = "text_inline";
	public static final String COLUMN_LINE_TYPE = "line_type";
	public static final String COLUMN_LINE_ORDER = "line_order";
	
	public HeaderFooterReceipt(SQLiteDatabase db) {
		super(db);
	}
	
	public List<ShopData.HeaderFooterReceipt> listHeaderFooter(int lineType){
		List<ShopData.HeaderFooterReceipt> hfLst = 
				new ArrayList<ShopData.HeaderFooterReceipt>();

		Cursor cursor = mSqlite.query(TABLE_HEADER_FOOTER_RECEIPT, 
				new String[]{COLUMN_TEXT_IN_LINE, 
				COLUMN_LINE_TYPE, COLUMN_LINE_ORDER}, 
				COLUMN_LINE_TYPE + "=?", 
				new String[]{String.valueOf(lineType)}, null, null, COLUMN_LINE_ORDER);
		if(cursor.moveToFirst()){
			do{
				ShopData.HeaderFooterReceipt hf = new ShopData.HeaderFooterReceipt();
				hf.setTextInLine(cursor.getString(cursor.getColumnIndex(COLUMN_TEXT_IN_LINE)));
				hf.setLineType(cursor.getInt(cursor.getColumnIndex(COLUMN_LINE_TYPE)));
				hf.setLineOrder(cursor.getInt(cursor.getColumnIndex(COLUMN_LINE_ORDER)));
				hfLst.add(hf);
			}while(cursor.moveToNext());
		}
		cursor.close();

		return hfLst;
	}
	
	public void addHeaderFooterReceipt(
			List<ShopData.HeaderFooterReceipt> headerFooterLst) throws SQLException{

		mSqlite.delete(TABLE_HEADER_FOOTER_RECEIPT, null, null);
		for(ShopData.HeaderFooterReceipt hf : headerFooterLst){
			ContentValues cv = new ContentValues();
			cv.put(COLUMN_TEXT_IN_LINE, hf.getTextInLine());
			cv.put(COLUMN_LINE_TYPE, hf.getLineType());
			cv.put(COLUMN_LINE_ORDER, hf.getLineOrder());
			mSqlite.insertOrThrow(TABLE_HEADER_FOOTER_RECEIPT, null, cv);
		}
	}
}
