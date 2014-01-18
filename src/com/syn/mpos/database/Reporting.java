package com.syn.mpos.database;

import com.syn.mpos.database.inventory.StockDocument;
import com.syn.mpos.database.transaction.PaymentDetail;
import com.syn.mpos.database.transaction.Transaction;
import com.syn.pos.Report;

import android.database.Cursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;

public class Reporting extends MPOSDatabase{
	protected long mDateFrom, mDateTo;
	
	public Reporting(SQLiteDatabase db, long dFrom, long dTo){
		super(db);
		mDateFrom = dFrom;
		mDateTo = dTo;
	}
	
	public Reporting(SQLiteDatabase db){
		super(db);
	}
	
	public float getTotalPay(int transactionId, int computerId, int payTypeId){
		float totalPay = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + PaymentDetail.COL_PAY_AMOUNT + ") " +
				" WHERE " + Transaction.COL_TRANS_ID + "? " +
				" AND " + Computer.COL_COMPUTER_ID + "? " +
				" AND " + PaymentDetail.COL_PAY_TYPE_ID + "=?", 
				new String[]{String.valueOf(transactionId),
				String.valueOf(computerId), String.valueOf(payTypeId)});
		if(cursor.moveToFirst()){
			totalPay = cursor.getFloat(0);
		}
		return totalPay;
	}
	
	public Report getSaleReportByBill(){
		Report report = new Report();
		String strSql = " SELECT a." + Transaction.COL_TRANS_ID  + ", " +
				" a." + Computer.COL_COMPUTER_ID + ", " +
				" a." + Transaction.COL_STATUS_ID + ", " +
				" a." + Transaction.COL_RECEIPT_NO + "," +
				" a." + Transaction.COL_TRANS_EXCLUDE_VAT + ", " +
				" a." + Transaction.COL_TRANS_VAT + ", " +
				" a." + Transaction.COL_TRANS_VATABLE + ", " +
				" SUM(b." + Transaction.COL_TOTAL_RETAIL_PRICE + ") AS TotalRetailPrice, " +
				" SUM(b." + Transaction.COL_TOTAL_SALE_PRICE + ") AS TotalSalePrice, " +
				" a." + Transaction.COL_OTHER_DISCOUNT + " + " + 
				" SUM(b." + Transaction.COL_PRICE_DISCOUNT + " + " + 
				" b." + Transaction.COL_MEMBER_DISCOUNT + ") AS TotalDiscount, " +
				" c." + StockDocument.COL_DOC_TYPE_HEADER + 
				" FROM " + Transaction.TB_TRANS + " a " +
				" LEFT JOIN " + Transaction.TB_ORDER + " b " +
				" ON a." + Transaction.COL_TRANS_ID + "=b." + Transaction.COL_TRANS_ID +
				" AND a." + Computer.COL_COMPUTER_ID + "=b." + Computer.COL_COMPUTER_ID +
				" LEFT JOIN " + StockDocument.TB_DOCUMENT_TYPE + " c " +
				" ON a." + StockDocument.COL_DOC_TYPE + "=c." + StockDocument.COL_DOC_TYPE +
				" WHERE a." + Transaction.COL_STATUS_ID + " IN(?, ?) " +
				" AND a." + Transaction.COL_SALE_DATE + " BETWEEN ? AND ? " +  
				" GROUP BY a." + Transaction.COL_TRANS_ID;

		Cursor cursor = mSqlite.rawQuery(strSql, 
				new String[]{
				String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
				String.valueOf(Transaction.TRANS_STATUS_VOID),
				String.valueOf(mDateFrom), 
				String.valueOf(mDateTo)});
		
		if(cursor.moveToFirst()){
			do{
				Report.ReportDetail reportDetail = 
						new Report.ReportDetail();
				reportDetail.setTransactionId(cursor.getInt(cursor.getColumnIndex(Transaction.COL_TRANS_ID)));
				reportDetail.setComputerId(cursor.getInt(cursor.getColumnIndex(Computer.COL_COMPUTER_ID)));
				reportDetail.setTransStatus(cursor.getInt(cursor.getColumnIndex(Transaction.COL_STATUS_ID)));
				reportDetail.setReceiptNo(cursor.getString(cursor.getColumnIndex(Transaction.COL_RECEIPT_NO)));
				reportDetail.setTotalPrice(cursor.getFloat(cursor.getColumnIndex("TotalRetailPrice")));
				reportDetail.setSubTotal(cursor.getFloat(cursor.getColumnIndex("TotalSalePrice")));
				reportDetail.setVatExclude(cursor.getFloat(cursor.getColumnIndex(Transaction.COL_TRANS_EXCLUDE_VAT)));
				reportDetail.setDiscount(cursor.getFloat(cursor.getColumnIndex("TotalDiscount")));
				reportDetail.setVatable(cursor.getFloat(cursor.getColumnIndex(Transaction.COL_TRANS_VATABLE)));
				reportDetail.setTotalVat(cursor.getFloat(cursor.getColumnIndex(Transaction.COL_TRANS_VAT)));
				report.reportDetail.add(reportDetail);
				
			}while(cursor.moveToNext());
		}
		return report;
	}
}
