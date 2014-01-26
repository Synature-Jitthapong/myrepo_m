package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.database.inventory.StockDocument;
import com.syn.mpos.database.transaction.PaymentDetail;
import com.syn.mpos.database.transaction.Transaction;
import com.syn.pos.Report;
import com.syn.pos.Report.GroupOfProduct;
import com.syn.pos.Report.ReportDetail;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class Reporting extends MPOSDatabase{
	public static final String SUMM_DEPT = "summ_dept";
	public static final String SUMM_GROUP = "summ_group";
	
	public static final String TMP_PRODUCT_REPORT = "tmp_product_report";
	public static final String COL_PRODUCT_QTY = "product_qty";
	public static final String COL_PRODUCT_SUMM_QTY = "product_summ_qty";
	public static final String COL_PRODUCT_QTY_PERCENT = "product_qty_percent";
	public static final String COL_PRODUCT_SUB_TOTAL = "product_sub_total";
	public static final String COL_PRODUCT_SUMM_SUB_TOTAL = "product_summ_sub_total";
	public static final String COL_PRODUCT_SUB_TOTAL_PERCENT = "product_sub_total_percent";
	public static final String COL_PRODUCT_DISCOUNT = "product_discount";
	public static final String COL_PRODUCT_SUMM_DISCOUNT = "product_summ_discount";
	public static final String COL_PRODUCT_TOTAL_PRICE = "product_total_price";
	public static final String COL_PRODUCT_SUMM_TOTAL_PRICE = "product_summ_total_price";
	public static final String COL_PRODUCT_TOTAL_PRICE_PERCENT = "product_totale_price_percent";
	
	protected long mDateFrom, mDateTo;
	
	public Reporting(SQLiteDatabase db, long dFrom, long dTo){
		super(db);
		mDateFrom = dFrom;
		mDateTo = dTo;
	}
	
	public Reporting(SQLiteDatabase db){
		super(db);
	}
	
	public float getTotalPayByPayType(int transactionId, int computerId, int payTypeId){
		float totalPay = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + PaymentDetail.COL_PAY_AMOUNT + ") " +
				" FROM " + PaymentDetail.TB_PAYMENT +
				" WHERE " + Transaction.COL_TRANS_ID + "=? " +
				" AND " + Computer.COL_COMPUTER_ID + "=? " +
				" AND " + PaymentDetail.COL_PAY_TYPE_ID + "=?", 
				new String[]{String.valueOf(transactionId),
				String.valueOf(computerId), String.valueOf(payTypeId)});
		if(cursor.moveToFirst()){
			totalPay = cursor.getFloat(0);
		}
		return totalPay;
	}
	
	public Report getSaleReportByBill(){
		Report report = null;
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
			report = new Report();
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
	
	public Report getProductDataReport() throws SQLException {
		Report report = null;
		try {
			createReportProductTmp();
			createProductDataTmp();

			// group : dept
			List<Report.GroupOfProduct> groupLst = listProductGroup();
			if (groupLst != null) {
				report = new Report();
				
				int lastGroupId = -1;
				for (Report.GroupOfProduct group : groupLst) {
					Report.GroupOfProduct groupSection = new Report.GroupOfProduct();
					groupSection.setProductDeptName(group.getProductDeptName());
					groupSection.setProductGroupName(group
							.getProductGroupName()
							+ ":"
							+ group.getProductDeptName());

					// product
					Cursor cursor = mSqlite.rawQuery("SELECT a."
							+ COL_PRODUCT_QTY + ", " + " a."
							+ COL_PRODUCT_QTY_PERCENT + ", " + " a."
							+ COL_PRODUCT_SUB_TOTAL + ", " + " a."
							+ COL_PRODUCT_SUB_TOTAL_PERCENT + ", " + " a."
							+ COL_PRODUCT_DISCOUNT + ", " + " a."
							+ COL_PRODUCT_TOTAL_PRICE + ", " + " a."
							+ COL_PRODUCT_TOTAL_PRICE_PERCENT + ", " + " b."
							+ Products.COL_PRODUCT_CODE + ", " + " b."
							+ Products.COL_PRODUCT_NAME + ", " + " b."
							+ Products.COL_PRODUCT_PRICE + ", " + " b."
							+ Products.COL_VAT_TYPE + " FROM "
							+ TMP_PRODUCT_REPORT + " a " + " INNER JOIN "
							+ Products.TB_PRODUCT + " b " + " ON a."
							+ Products.COL_PRODUCT_ID + "=b."
							+ Products.COL_PRODUCT_ID + " WHERE b."
							+ Products.COL_PRODUCT_DEPT_ID + "=?"
							+ " ORDER BY b." + Products.COL_ORDERING,
							new String[] { String.valueOf(group
									.getProductDeptId()) });

					if (cursor.moveToFirst()) {
						do {
							Report.ReportDetail reportDetail = new Report.ReportDetail();
							reportDetail
									.setProductCode(cursor.getString(cursor
											.getColumnIndex(Products.COL_PRODUCT_CODE)));
							reportDetail
									.setProductName(cursor.getString(cursor
											.getColumnIndex(Products.COL_PRODUCT_NAME)));
							reportDetail
									.setPricePerUnit(cursor.getFloat(cursor
											.getColumnIndex(Products.COL_PRODUCT_PRICE)));
							reportDetail.setQty(cursor.getFloat(cursor
									.getColumnIndex(COL_PRODUCT_QTY)));
							reportDetail.setQtyPercent(cursor.getFloat(cursor
									.getColumnIndex(COL_PRODUCT_QTY_PERCENT)));
							reportDetail.setSubTotal(cursor.getFloat(cursor
									.getColumnIndex(COL_PRODUCT_SUB_TOTAL)));
							reportDetail
									.setSubTotalPercent(cursor.getFloat(cursor
											.getColumnIndex(COL_PRODUCT_SUB_TOTAL_PERCENT)));
							reportDetail.setDiscount(cursor.getFloat(cursor
									.getColumnIndex(COL_PRODUCT_DISCOUNT)));
							reportDetail.setTotalPrice(cursor.getFloat(cursor
									.getColumnIndex(COL_PRODUCT_TOTAL_PRICE)));
							reportDetail
									.setTotalPricePercent(cursor.getFloat(cursor
											.getColumnIndex(COL_PRODUCT_TOTAL_PRICE_PERCENT)));
							int vatType = cursor.getInt(cursor.getColumnIndex(Products.COL_VAT_TYPE));
							String vatTypeText = "N";
							switch(vatType){
							case 0:
								vatTypeText = "N";
								break;
							case 1:
								vatTypeText = "V";
								break;
							case 2:
								vatTypeText = "Exc";
								break;
							}
							reportDetail.setVat(vatTypeText);
							groupSection.reportDetail.add(reportDetail);
						} while (cursor.moveToNext());
					}
					
					// dept summary
					groupSection.reportDetail.add(getSummaryByDept(group.getProductDeptId()));
					if(lastGroupId != -1 && lastGroupId != group.getProductGroupId()){
						groupSection.reportDetail.add(getSummaryByGroup(group.getProductGroupId()));
					}
					lastGroupId = group.getProductGroupId();
					report.groupOfProductLst.add(groupSection);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return report;
	}
	
	public Report.ReportDetail getSummaryByGroup(int groupId){
		Report.ReportDetail report = null;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(o." + Transaction.COL_ORDER_QTY + ") AS TotalQty, " +
				" SUM(o." + Transaction.COL_TOTAL_RETAIL_PRICE + ") AS TotalRetailPrice, " +
				" SUM(o." + Transaction.COL_PRICE_DISCOUNT + ") AS TotalDiscount, " +
				" SUM(o." + Transaction.COL_TOTAL_SALE_PRICE + ") AS TotalSalePrice " +
				" FROM " + Transaction.TB_TRANS + " t " + 
				" INNER JOIN " + Transaction.TB_ORDER + " o " +
				" ON t." + Transaction.COL_TRANS_ID + "=o." + Transaction.COL_TRANS_ID +
				" AND t." + Computer.COL_COMPUTER_ID + "=o." + Computer.COL_COMPUTER_ID +
				" INNER JOIN " + Products.TB_PRODUCT + " p " +
				" ON o." + Products.COL_PRODUCT_ID + "=p." + Products.COL_PRODUCT_ID +
				" INNER JOIN " + Products.TB_PRODUCT_DEPT + " pd " +
				" ON p." + Products.COL_PRODUCT_DEPT_ID + "=pd." + Products.COL_PRODUCT_DEPT_ID +
				" WHERE t." + Transaction.COL_SALE_DATE + " BETWEEN ? AND ?" +
				" AND t." + Transaction.COL_STATUS_ID + "=?" +
				" AND pd." + Products.COL_PRODUCT_GROUP_ID + "=?" +
				" GROUP BY pd." + Products.COL_PRODUCT_GROUP_ID,
				new String[]{
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						String.valueOf(groupId)
				});
		
		if(cursor.moveToFirst()){
			Report.ReportDetail summReport = getProductSummaryAll();
			report = new Report.ReportDetail();
			report.setProductName(SUMM_GROUP);
			report.setQty(cursor.getFloat(cursor.getColumnIndex("TotalQty")));
			report.setQtyPercent(report.getQty() / summReport.getQty() * 100);
			report.setSubTotal(cursor.getFloat(cursor.getColumnIndex("TotalRetailPrice")));
			report.setSubTotalPercent(report.getSubTotal() / summReport.getSubTotal() * 100);
			report.setDiscount(cursor.getFloat(cursor.getColumnIndex("TotalDiscount")));
			report.setTotalPrice(cursor.getFloat(cursor.getColumnIndex("TotalSalePrice")));
			report.setTotalPricePercent(report.getTotalPrice() / summReport.getTotalPrice() * 100);
		}
		cursor.close();
		return report;
	}
	
	public Report.ReportDetail getSummaryByDept(int deptId){
		Report.ReportDetail report = null;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(o." + Transaction.COL_ORDER_QTY + ") AS TotalQty, " +
				" SUM(o." + Transaction.COL_TOTAL_RETAIL_PRICE + ") AS TotalRetailPrice, " +
				" SUM(o." + Transaction.COL_PRICE_DISCOUNT + ") AS TotalDiscount, " +
				" SUM(o." + Transaction.COL_TOTAL_SALE_PRICE + ") AS TotalSalePrice " +
				" FROM " + Transaction.TB_TRANS + " t " + 
				" INNER JOIN " + Transaction.TB_ORDER + " o " +
				" ON t." + Transaction.COL_TRANS_ID + "=o." + Transaction.COL_TRANS_ID +
				" AND t." + Computer.COL_COMPUTER_ID + "=o." + Computer.COL_COMPUTER_ID +
				" INNER JOIN " + Products.TB_PRODUCT + " p " +
				" ON o." + Products.COL_PRODUCT_ID + "=p." + Products.COL_PRODUCT_ID +
				" WHERE t." + Transaction.COL_SALE_DATE + " BETWEEN ? AND ?" +
				" AND t." + Transaction.COL_STATUS_ID + "=?" +
				" AND p." + Products.COL_PRODUCT_DEPT_ID + "=?" +
				" GROUP BY p." + Products.COL_PRODUCT_DEPT_ID,
				new String[]{
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						String.valueOf(deptId)
				});
		
		if(cursor.moveToFirst()){
			Report.ReportDetail summReport = getProductSummaryAll();
			report = new Report.ReportDetail();
			report.setProductName(SUMM_DEPT);
			report.setQty(cursor.getFloat(cursor.getColumnIndex("TotalQty")));
			report.setQtyPercent(report.getQty() / summReport.getQty() * 100);
			report.setSubTotal(cursor.getFloat(cursor.getColumnIndex("TotalRetailPrice")));
			report.setSubTotalPercent(report.getSubTotal() / summReport.getSubTotal() * 100);
			report.setDiscount(cursor.getFloat(cursor.getColumnIndex("TotalDiscount")));
			report.setTotalPrice(cursor.getFloat(cursor.getColumnIndex("TotalSalePrice")));
			report.setTotalPricePercent(report.getTotalPrice() / summReport.getTotalPrice() * 100);
		}
		cursor.close();
		return report;
	}
	
	public Report.ReportDetail getProductSummaryAll(){
		Report.ReportDetail report = null;
		Cursor cursor = mSqlite.query(TMP_PRODUCT_REPORT, 
				new String[]{
					COL_PRODUCT_SUMM_QTY,
					COL_PRODUCT_SUMM_SUB_TOTAL,
					COL_PRODUCT_SUMM_DISCOUNT,
					COL_PRODUCT_SUMM_TOTAL_PRICE,
				}, null, null, null, null, null, "1");
		
		if(cursor.moveToFirst()){
			report = new Report.ReportDetail();
			report.setQty(cursor.getFloat(cursor.getColumnIndex(COL_PRODUCT_SUMM_QTY)));
			report.setSubTotal(cursor.getFloat(cursor.getColumnIndex(COL_PRODUCT_SUMM_SUB_TOTAL)));
			report.setDiscount(cursor.getFloat(cursor.getColumnIndex(COL_PRODUCT_SUMM_DISCOUNT)));
			report.setTotalPrice(cursor.getFloat(cursor.getColumnIndex(COL_PRODUCT_SUMM_TOTAL_PRICE)));
		}
		cursor.close();
		return report;
	}
	
	private void createProductDataTmp() throws SQLException{
		Cursor cursor = mSqlite.rawQuery(
				" SELECT b." + Products.COL_PRODUCT_ID + ", " +
				" SUM(b." + Transaction.COL_ORDER_QTY + ") AS Qty, " +
			    " SUM(b." + Transaction.COL_TOTAL_RETAIL_PRICE + ") AS RetailPrice, " +
				" SUM(b." + Transaction.COL_PRICE_DISCOUNT + ") AS Discount, " +
			    " SUM(b." + Transaction.COL_TOTAL_SALE_PRICE + ") AS SalePrice, " +
				// total qty
				" (SELECT SUM(o." + Transaction.COL_ORDER_QTY + ") " +
				" FROM " + Transaction.TB_TRANS + " t " + 
				" INNER JOIN " + Transaction.TB_ORDER + " o " +
				" ON t." + Transaction.COL_TRANS_ID + "=o." + Transaction.COL_TRANS_ID +
				" AND t." + Computer.COL_COMPUTER_ID + "=o." + Computer.COL_COMPUTER_ID +
				" WHERE t." + Transaction.COL_SALE_DATE + " BETWEEN ? AND ?" +
				" AND t." + Transaction.COL_STATUS_ID + "=?) AS TotalQty, " +
				// total retail price
				" (SELECT SUM(o." + Transaction.COL_TOTAL_RETAIL_PRICE + ") " +
				" FROM " + Transaction.TB_TRANS + " t " + 
				" INNER JOIN " + Transaction.TB_ORDER + " o " +
				" ON t." + Transaction.COL_TRANS_ID + "=o." + Transaction.COL_TRANS_ID +
				" AND t." + Computer.COL_COMPUTER_ID + "=o." + Computer.COL_COMPUTER_ID +
				" WHERE t." + Transaction.COL_SALE_DATE + " BETWEEN ? AND ?" +
				" AND t." + Transaction.COL_STATUS_ID + "=?) AS TotalRetailPrice, " +
				// total discount
				" (SELECT SUM(o." + Transaction.COL_PRICE_DISCOUNT + ") " +
				" FROM " + Transaction.TB_TRANS + " t " + 
				" INNER JOIN " + Transaction.TB_ORDER + " o " +
				" ON t." + Transaction.COL_TRANS_ID + "=o." + Transaction.COL_TRANS_ID +
				" AND t." + Computer.COL_COMPUTER_ID + "=o." + Computer.COL_COMPUTER_ID +
				" WHERE t." + Transaction.COL_SALE_DATE + " BETWEEN ? AND ?" +
				" AND t." + Transaction.COL_STATUS_ID + "=?) AS TotalDiscount, " +
				// total sale price
				" (SELECT SUM(o." + Transaction.COL_TOTAL_SALE_PRICE + ") " +
				" FROM " + Transaction.TB_TRANS + " t " + 
				" INNER JOIN " + Transaction.TB_ORDER + " o " +
				" ON t." + Transaction.COL_TRANS_ID + "=o." + Transaction.COL_TRANS_ID +
				" AND t." + Computer.COL_COMPUTER_ID + "=o." + Computer.COL_COMPUTER_ID +
				" WHERE t." + Transaction.COL_SALE_DATE + " BETWEEN ? AND ?" +
				" AND t." + Transaction.COL_STATUS_ID + "=?) AS TotalSalePrice " +
				" FROM " + Transaction.TB_TRANS + " a " +
				" INNER JOIN " + Transaction.TB_ORDER + " b " +
				" ON a." + Transaction.COL_TRANS_ID + "=b." + Transaction.COL_TRANS_ID +
				" AND a." + Computer.COL_COMPUTER_ID + "=b." + Computer.COL_COMPUTER_ID + 
				" WHERE a." + Transaction.COL_SALE_DATE + " BETWEEN ? AND ?" +
				" AND a." + Transaction.COL_STATUS_ID + "=?" +
				" GROUP BY b." + Products.COL_PRODUCT_ID, 
				new String[]{
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS)
				});
		
		if(cursor.moveToFirst()){
			do{
				float qty = cursor.getFloat(cursor.getColumnIndex("Qty"));
				float summQty = cursor.getFloat(cursor.getColumnIndex("TotalQty"));
				float qtyPercent = (qty / summQty) * 100;
				float retailPrice = cursor.getFloat(cursor.getColumnIndex("RetailPrice"));
				float summRetailPrice = cursor.getFloat(cursor.getColumnIndex("TotalRetailPrice"));
				float retailPricePercent = (retailPrice / summRetailPrice) * 100;
				float discount = cursor.getFloat(cursor.getColumnIndex("Discount"));
				float summDiscount = cursor.getFloat(cursor.getColumnIndex("TotalDiscount"));
				float salePrice = cursor.getFloat(cursor.getColumnIndex("SalePrice"));
				float summSalePrice = cursor.getFloat(cursor.getColumnIndex("TotalSalePrice"));
				float salePricePercent = (salePrice / summSalePrice) * 100;
				
				ContentValues cv = new ContentValues();
				cv.put(Products.COL_PRODUCT_ID, cursor.getInt(cursor.getColumnIndex(Products.COL_PRODUCT_ID)));
				cv.put(COL_PRODUCT_QTY, qty);
				cv.put(COL_PRODUCT_SUMM_QTY, summQty);
				cv.put(COL_PRODUCT_QTY_PERCENT, qtyPercent);
				cv.put(COL_PRODUCT_SUB_TOTAL, retailPrice);
				cv.put(COL_PRODUCT_SUMM_SUB_TOTAL, summRetailPrice);
				cv.put(COL_PRODUCT_SUB_TOTAL_PERCENT, retailPricePercent);
				cv.put(COL_PRODUCT_DISCOUNT, discount);
				cv.put(COL_PRODUCT_SUMM_DISCOUNT, summDiscount);
				cv.put(COL_PRODUCT_TOTAL_PRICE, salePrice);
				cv.put(COL_PRODUCT_SUMM_TOTAL_PRICE, summSalePrice);
				cv.put(COL_PRODUCT_TOTAL_PRICE_PERCENT, salePricePercent);
				
				try {
					mSqlite.insertOrThrow(TMP_PRODUCT_REPORT, null, cv);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				
			}while(cursor.moveToNext());
		}
		cursor.close();
	}
	
	public List<Report.GroupOfProduct> listProductGroup(){
		List<Report.GroupOfProduct> reportLst = null;
		
		Cursor cursor = mSqlite.rawQuery(
					" SELECT c." + Products.COL_PRODUCT_DEPT_ID + ", " + 
					" c." + Products.COL_PRODUCT_DEPT_NAME + ", " + 
					" d." + Products.COL_PRODUCT_GROUP_ID + ", " +
					" d." + Products.COL_PRODUCT_GROUP_NAME + 
					" FROM " + TMP_PRODUCT_REPORT + " a " +
					" INNER JOIN " + Products.TB_PRODUCT + " b " +
					" ON a." + Products.COL_PRODUCT_ID + "=b." + Products.COL_PRODUCT_ID +
					" INNER JOIN " + Products.TB_PRODUCT_DEPT + " c " +
					" ON b." + Products.COL_PRODUCT_DEPT_ID + "=c." + Products.COL_PRODUCT_DEPT_ID +
					" INNER JOIN " + Products.TB_PRODUCT_GROUP + " d " +
					" ON c." + Products.COL_PRODUCT_GROUP_ID + "=d." + Products.COL_PRODUCT_GROUP_ID + 
					" GROUP BY d." + Products.COL_PRODUCT_GROUP_ID + 
					" ORDER BY c." + Products.COL_PRODUCT_DEPT_ID + ", " + 
					" d." + Products.COL_ORDERING + ", " +
					" c." + Products.COL_ORDERING + ", " +
					" b." + Products.COL_ORDERING, null);
		
		if(cursor.moveToFirst()){
			reportLst = new ArrayList<Report.GroupOfProduct>();
			do{
				Report.GroupOfProduct report = new Report.GroupOfProduct();
				report.setProductDeptId(cursor.getInt(cursor.getColumnIndex(Products.COL_PRODUCT_DEPT_ID)));
				report.setProductGroupId(cursor.getInt(cursor.getColumnIndex(Products.COL_PRODUCT_GROUP_ID)));
				report.setProductGroupName(cursor.getString(cursor.getColumnIndex(Products.COL_PRODUCT_GROUP_NAME)));
				report.setProductDeptName(cursor.getString(cursor.getColumnIndex(Products.COL_PRODUCT_DEPT_NAME)));
				reportLst.add(report);
			}while(cursor.moveToNext());
		}
		cursor.close();
		
		return reportLst;
	}
	
	private void createReportProductTmp() throws SQLException{
		mSqlite.execSQL("DROP TABLE IF EXISTS " + TMP_PRODUCT_REPORT);
		mSqlite.execSQL("CREATE TABLE " + TMP_PRODUCT_REPORT + " ( " +
				Products.COL_PRODUCT_ID + " INTEGER, " +
				COL_PRODUCT_QTY + " REAL, " +
				COL_PRODUCT_QTY_PERCENT + " REAL, " +
				COL_PRODUCT_SUB_TOTAL + " REAL, " +
				COL_PRODUCT_SUB_TOTAL_PERCENT + " REAL, " +
				COL_PRODUCT_DISCOUNT + " REAL, " +
				COL_PRODUCT_TOTAL_PRICE + " REAL, " +
				COL_PRODUCT_TOTAL_PRICE_PERCENT + " REAL, " +
				COL_PRODUCT_SUMM_QTY + " REAL, " +
				COL_PRODUCT_SUMM_SUB_TOTAL + " REAL, " +
				COL_PRODUCT_SUMM_DISCOUNT + " REAL, " +
				COL_PRODUCT_SUMM_TOTAL_PRICE + " REAL);"); 
	}
}
