package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;
import com.syn.pos.Report;
import android.content.Context;
import android.database.Cursor;

public class Reporting extends MPOSSQLiteHelper{
	protected long dateFrom, dateTo;
	
	public Reporting(Context c, long dFrom, long dTo){
		super(c);
		dateFrom = dFrom;
		dateTo = dTo;
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
				" AND a.sale_date >= " + dateFrom + 
				" AND a.sale_date <= " + dateTo + 
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
	
	public Report getSaleReportByBill(){
		Report report = new Report();
		
		String strSql = " SELECT a.receipt_year, a.receipt_month, " +
				" a.receipt_id, c.document_type_header, " +
				" a.service_charge AS totalServiceCharge, " +
				" a.transaction_vatable AS transVatable, " +
				" a.transaction_vat AS transVat, " +
				" a.transaction_exclude_vat AS transExcludeVat, " +
				" SUM(b.total_retail_price) AS totalPrice, " +
				" SUM(b.total_sale_price) AS subTotal, " +
				" SUM(b.price_discount + b.member_discount) AS totalDiscount " +
				" FROM order_transaction a " +
				" LEFT JOIN order_detail b " +
				" ON a.transaction_id=b.transaction_id " +
				" AND a.computer_id=b.computer_id " +
				" LEFT JOIN document_type c " +
				" ON a.document_type_id=c.document_type_id " +
				" WHERE a.transaction_status_id=2 " +
				" AND a.sale_date >= " + dateFrom + 
				" AND a.sale_date <= " + dateTo + 
				" GROUP BY a.transaction_id";
		
		open();
		Cursor cursor = mSqlite.rawQuery(strSql, null);
		if(cursor.moveToFirst()){
			do{
				Report.ReportDetail reportDetail = 
						new Report.ReportDetail();
				String docTypeHeader = cursor.getString(cursor.getColumnIndex("document_type_header"));
				String receiptYear = String.format("%04d", cursor.getInt(cursor.getColumnIndex("receipt_year")));
				String receiptMonth = String.format("%02d", cursor.getInt(cursor.getColumnIndex("receipt_month")));
				String receiptId = String.format("%06d", cursor.getInt(cursor.getColumnIndex("receipt_id")));
				
				reportDetail.setReceiptNo(docTypeHeader + receiptMonth + receiptYear + receiptId);
				reportDetail.setTotalPrice(cursor.getFloat(cursor.getColumnIndex("totalPrice")));
				reportDetail.setSubTotal(cursor.getFloat(cursor.getColumnIndex("subTotal")));
				reportDetail.setServiceCharge(cursor.getFloat(cursor.getColumnIndex("totalServiceCharge")));
				reportDetail.setVatExclude(cursor.getFloat(cursor.getColumnIndex("transExcludeVat")));
				reportDetail.setTotalSale(reportDetail.getSubTotal() + reportDetail.getServiceCharge() + 
						reportDetail.getVatExclude());
				reportDetail.setDiscount(cursor.getFloat(cursor.getColumnIndex("totalDiscount")));
				reportDetail.setVatable(cursor.getFloat(cursor.getColumnIndex("transVatable")));
				reportDetail.setTotalVat(cursor.getFloat(cursor.getColumnIndex("transVat")));
				reportDetail.setCash(0);
				reportDetail.setTotalPayment(0);
				reportDetail.setDiff(0);
				
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
				" WHERE a.sale_date >= " + dateFrom + 
				" AND a.sale_date <= " + dateTo +
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
				" AND a.sale_date >= " + dateFrom + 
				" AND a.sale_date <= " + dateTo +
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
						" AND a.sale_date >= " + dateFrom + 
						" AND a.sale_date <= " + dateTo +
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
