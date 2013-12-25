package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.database.inventory.StockDocument;
import com.syn.mpos.database.transaction.PaymentDetail;
import com.syn.mpos.database.transaction.Transaction;
import com.syn.pos.Report;

import android.content.Context;
import android.database.Cursor;

public class Reporting extends MPOSDatabase{
	protected long mDateFrom, mDateTo;
	
	public Reporting(Context c, long dFrom, long dTo){
		super(c);
		mDateFrom = dFrom;
		mDateTo = dTo;
	}
	
	public Reporting(Context c){
		super(c);
	}
	
	public Report.ReportDetail getSummaryByBill(){
		Report.ReportDetail reportDetail =
				new Report.ReportDetail();
		
		String strSql = " SELECT SUM(a.service_charge) AS totalServiceCharge, " +
				" SUM(a.transaction_vatable) AS transVatable, " +
				" SUM(a.transaction_vat) AS transVat, " +
				" SUM(a.transaction_exclude_vat) AS transExcludeVat, " +
				" SUM(b.total_retail_price) AS totalPrice, " +
				" SUM(b.total_sale_price) AS subTotal, " +
				" SUM(b.price_discount + b.member_discount) AS totalDiscount " +
				" FROM order_transaction a " +
				" LEFT JOIN order_detail b " +
				" ON a.transaction_id=b.transaction_id " +
				" AND a.computer_id=b.computer_id " +
				" WHERE a.transaction_status_id=2 " +
				" AND a.sale_date >= " + mDateFrom + 
				" AND a.sale_date <= " + mDateTo + 
				" GROUP BY a.receipt_year, a.receipt_month";

		open();
		Cursor cursor = mSqlite.rawQuery(strSql, null);
		if(cursor.moveToFirst()){
			do{
				reportDetail.setTotalPrice(cursor.getFloat(cursor.getColumnIndex("totalPrice")));
				reportDetail.setSubTotal(cursor.getFloat(cursor.getColumnIndex("subTotal")));
				reportDetail.setServiceCharge(cursor.getFloat(cursor.getColumnIndex("totalServiceCharge")));
				reportDetail.setTotalSale(reportDetail.getSubTotal() + reportDetail.getServiceCharge());
				reportDetail.setDiscount(cursor.getFloat(cursor.getColumnIndex("totalDiscount")));
				reportDetail.setVatable(cursor.getFloat(cursor.getColumnIndex("transVatable")));
				reportDetail.setTotalVat(cursor.getFloat(cursor.getColumnIndex("transVat")));
				reportDetail.setCash(0);
				reportDetail.setTotalPayment(0);
				reportDetail.setDiff(0);
			}while(cursor.moveToNext());
		}
		cursor.close();
		close();
		return reportDetail;
	}
	
	public float getTotalPay(int transactionId, int computerId, int payTypeId){
		float totalPay = 0.0f;
		
		open();
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
		close();
		return totalPay;
	}
	
	public Report getSaleReportByBill(){
		Report report = new Report();
		
		String strSql = " SELECT a." + Transaction.COL_TRANS_ID  + ", " +
				" a." + Computer.COL_COMPUTER_ID + ", " +
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
				" WHERE a." + Transaction.COL_STATUS_ID + "=" + Transaction.TRANS_STATUS_SUCCESS +
				" AND a." + Transaction.COL_SALE_DATE + 
				" BETWEEN " + mDateFrom + " AND " + mDateTo + 
				" GROUP BY a." + Transaction.COL_TRANS_ID;
		
		open();
		Cursor cursor = mSqlite.rawQuery(strSql, null);
		if(cursor.moveToFirst()){
			do{
				Report.ReportDetail reportDetail = 
						new Report.ReportDetail();
				reportDetail.setTransactionId(cursor.getInt(cursor.getColumnIndex(Transaction.COL_TRANS_ID)));
				reportDetail.setComputerId(cursor.getInt(cursor.getColumnIndex(Computer.COL_COMPUTER_ID)));
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
		cursor.close();
		close();
		return report;
	}
	
	public Report.ReportDetail getSummaryByGroup(){
		Report.ReportDetail reportDetail =
				new Report.ReportDetail();
		
		String strSql = " SELECT SUM(b.order_qty) AS totalQty, " +
				" SUM(b.total_retail_price) AS totalPrice, " +
				" SUM(b.total_sale_price) AS subTotal, " +
				" SUM(b.price_discount + b.member_discount) AS totalDiscount, " +
				" c.product_code, " +
				" c.product_name, " +
				" c.product_price, " +
				" c.vat_type " +
				" FROM order_transaction a " +
				" LEFT JOIN order_detail b " +
				" ON a.transaction_id=b.transaction_id " +
				" AND a.computer_id=b.computer_id " +
				" LEFT JOIN products c " +
				" ON b.product_id=c.product_id " +
				" WHERE a.sale_date >= " + mDateFrom + 
				" AND a.sale_date <= " + mDateTo +
				" GROUP BY c.product_group_id " +
				" ORDER BY c.product_id ";
		
		open();
		Cursor cursor = mSqlite.rawQuery(strSql, null);
		if(cursor.moveToFirst()){
			reportDetail.setProductCode(cursor.getString(cursor.getColumnIndex("product_code")));
			reportDetail.setProductName(cursor.getString(cursor.getColumnIndex("product_name")));
			reportDetail.setPricePerUnit(cursor.getFloat(cursor.getColumnIndex("product_price")));
			reportDetail.setQty(cursor.getFloat(cursor.getColumnIndex("totalQty")));
			reportDetail.setTotalPrice(cursor.getFloat(cursor.getColumnIndex("totalPrice")));
			reportDetail.setDiscount(cursor.getFloat(cursor.getColumnIndex("totalDiscount")));
			reportDetail.setSubTotal(cursor.getFloat(cursor.getColumnIndex("subTotal")));
			reportDetail.setVat(cursor.getString(cursor.getColumnIndex("vat_type")));
		}
		cursor.close();
		close();
		return reportDetail;
	}
	
	public Report.ReportDetail getSummaryByDept(int deptId){
		Report.ReportDetail reportDetail =
				new Report.ReportDetail();
		
		String strSql = " SELECT SUM(b.order_qty) AS totalQty, " +
				" SUM(b.total_retail_price) AS totalPrice, " +
				" SUM(b.total_sale_price) AS subTotal, " +
				" SUM(b.price_discount + b.member_discount) AS totalDiscount, " +
				" c.product_code, " +
				" c.product_name, " +
				" c.product_price, " +
				" c.vat_type " +
				" FROM order_transaction a " +
				" LEFT JOIN order_detail b " +
				" ON a.transaction_id=b.transaction_id " +
				" AND a.computer_id=b.computer_id " +
				" LEFT JOIN products c " +
				" ON b.product_id=c.product_id " +
				" WHERE c.product_dept_id=" + deptId +
				" AND a.sale_date >= " + mDateFrom + 
				" AND a.sale_date <= " + mDateTo +
				" GROUP BY c.product_dept_id " +
				" ORDER BY c.product_id ";
		
		open();
		Cursor cursor = mSqlite.rawQuery(strSql, null);
		if(cursor.moveToFirst()){
			reportDetail.setProductCode(cursor.getString(cursor.getColumnIndex("product_code")));
			reportDetail.setProductName(cursor.getString(cursor.getColumnIndex("product_name")));
			reportDetail.setPricePerUnit(cursor.getFloat(cursor.getColumnIndex("product_price")));
			reportDetail.setQty(cursor.getFloat(cursor.getColumnIndex("totalQty")));
			reportDetail.setTotalPrice(cursor.getFloat(cursor.getColumnIndex("totalPrice")));
			reportDetail.setDiscount(cursor.getFloat(cursor.getColumnIndex("totalDiscount")));
			reportDetail.setSubTotal(cursor.getFloat(cursor.getColumnIndex("subTotal")));
			reportDetail.setVat(cursor.getString(cursor.getColumnIndex("vat_type")));
		}
		cursor.close();
		close();
		return reportDetail;
	}
	
	public List<Report> getSaleReportByProduct(){
		List<Report> reportLst = new ArrayList<Report>();
		
		String strSql = "SELECT a.product_group_id, " +
				" b.product_dept_id, " +
				" a.product_group_name, " +
				" b.product_dept_name " +
				" FROM product_group a " +
				" LEFT JOIN product_dept b " +
				" ON a.product_group_id=b.product_group_id";
		
		open();
		Cursor cursor1 = mSqlite.rawQuery(strSql, null);
		if(cursor1.moveToFirst()){
			do{
				Report report = new Report();
				report.setProductGroupId(cursor1.getInt(cursor1.getColumnIndex("product_group_id")));
				report.setProductDeptId(cursor1.getInt(cursor1.getColumnIndex("product_dept_id")));
				report.setProductGroupName(cursor1.getString(cursor1.getColumnIndex("product_group_name")));
				report.setProductDeptName(cursor1.getString(cursor1.getColumnIndex("product_dept_name")));
			
				strSql = " SELECT SUM(b.order_qty) AS totalQty, " +
						" SUM(b.total_retail_price) AS totalPrice, " +
						" SUM(b.total_sale_price) AS subTotal, " +
						" SUM(b.price_discount + b.member_discount) AS totalDiscount, " +
						" c.product_code, " +
						" c.product_name, " +
						" c.product_price, " +
						" c.vat_type " +
						" FROM order_transaction a " +
						" LEFT JOIN order_detail b " +
						" ON a.transaction_id=b.transaction_id " +
						" AND a.computer_id=b.computer_id " +
						" LEFT JOIN products c " +
						" ON b.product_id=c.product_id " +
						" WHERE c.product_dept_id=" + cursor1.getInt(cursor1.getColumnIndex("product_dept_id")) +
						" AND a.sale_date >= " + mDateFrom + 
						" AND a.sale_date <= " + mDateTo +
						" GROUP BY c.product_id " +
						" ORDER BY c.product_id ";
				
				Cursor cursor2 = mSqlite.rawQuery(strSql, null);
				if(cursor2.moveToFirst()){
					do{
						Report.ReportDetail reportDetail = 
								new Report.ReportDetail();
						reportDetail.setProductCode(cursor2.getString(cursor2.getColumnIndex("product_code")));
						reportDetail.setProductName(cursor2.getString(cursor2.getColumnIndex("product_name")));
						reportDetail.setPricePerUnit(cursor2.getFloat(cursor2.getColumnIndex("product_price")));
						reportDetail.setQty(cursor2.getFloat(cursor2.getColumnIndex("totalQty")));
						reportDetail.setTotalPrice(cursor2.getFloat(cursor2.getColumnIndex("totalPrice")));
						reportDetail.setDiscount(cursor2.getFloat(cursor2.getColumnIndex("totalDiscount")));
						reportDetail.setSubTotal(cursor2.getFloat(cursor2.getColumnIndex("subTotal")));
						reportDetail.setVat(cursor2.getString(cursor2.getColumnIndex("vat_type")));
						
						report.reportDetail.add(reportDetail);
					}while(cursor2.moveToNext());
					
					reportLst.add(report);
				}
				cursor2.close();
				
			}while(cursor1.moveToNext());
		}
		cursor1.close();	
		close();
		return reportLst;
	}
}
