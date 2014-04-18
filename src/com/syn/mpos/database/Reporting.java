package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.syn.pos.Report;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class Reporting extends MPOSDatabase{
	
	public static final String SUMM_DEPT = "summ_dept";
	public static final String SUMM_GROUP = "summ_group";
	
	public static final String TEMP_BILL_REPORT = "tmp_bill_report";
	
	public static final String TEMP_PRODUCT_REPORT = "tmp_product_report";
	public static final String COLUMN_PRODUCT_QTY = "product_qty";
	public static final String COLUMN_PRODUCT_SUMM_QTY = "product_summ_qty";
	public static final String COLUMN_PRODUCT_QTY_PERCENT = "product_qty_percent";
	public static final String COLUMN_PRODUCT_SUB_TOTAL = "product_sub_total";
	public static final String COLUMN_PRODUCT_SUMM_SUB_TOTAL = "product_summ_sub_total";
	public static final String COLUMN_PRODUCT_SUB_TOTAL_PERCENT = "product_sub_total_percent";
	public static final String COLUMN_PRODUCT_DISCOUNT = "product_discount";
	public static final String COLUMN_PRODUCT_SUMM_DISCOUNT = "product_summ_discount";
	public static final String COLUMN_PRODUCT_TOTAL_PRICE = "product_total_price";
	public static final String COLUMN_PRODUCT_SUMM_TOTAL_PRICE = "product_summ_total_price";
	public static final String COLUMN_PRODUCT_TOTAL_PRICE_PERCENT = "product_totale_price_percent";
	
	protected long mDateFrom, mDateTo;
	
	public Reporting(SQLiteDatabase db, long dFrom, long dTo){
		super(db);
		mDateFrom = dFrom;
		mDateTo = dTo;
	}
	
	public Reporting(SQLiteDatabase db){
		super(db);
	}
	
	public double getTotalPayByPayType(int transactionId, int computerId, int payTypeId){
		double totalPay = 0.0f;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + PaymentDetailTable.COLUMN_PAY_AMOUNT + ") " +
				" FROM " + PaymentDetailTable.TABLE_NAME +
				" WHERE " + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? " +
				" AND " + ComputerTable.COLUMN_COMPUTER_ID + "=? " +
				" AND " + PayTypeTable.COLUMN_PAY_TYPE_ID + "=?", 
				new String[]{String.valueOf(transactionId),
				String.valueOf(computerId), String.valueOf(payTypeId)});
		if(cursor.moveToFirst()){
			totalPay = cursor.getDouble(0);
		}
		return totalPay;
	}
	
	public Report getSaleReportByBill(){
		Report report = null;
		String strSql = " SELECT a." + OrderTransactionTable.COLUMN_TRANSACTION_ID  + ", " +
				" a." + ComputerTable.COLUMN_COMPUTER_ID + ", " +
				" a." + OrderTransactionTable.COLUMN_STATUS_ID + ", " +
				" a." + OrderTransactionTable.COLUMN_RECEIPT_NO + "," +
				" a." + OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT + ", " +
				" a." + OrderTransactionTable.COLUMN_TRANS_VAT + ", " +
				" a." + OrderTransactionTable.COLUMN_TRANS_VATABLE + ", " +
				" a." + MPOSDatabase.COLUMN_SEND_STATUS + ", " +
				" SUM(b." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + " * b." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS TotalRetailPrice, " +
				" SUM(b." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + " * b." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS TotalSalePrice, " +
				" a." + OrderTransactionTable.COLUMN_OTHER_DISCOUNT + " + " + 
				" SUM(b." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + " + " + 
				" b." + OrderDetailTable.COLUMN_MEMBER_DISCOUNT + ") AS TotalDiscount, " +
				"(SELECT SUM(" + PaymentDetailTable.COLUMN_PAY_AMOUNT + ") " +
				" FROM " + PaymentDetailTable.TABLE_NAME +
				" WHERE " + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=a." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND " + ComputerTable.COLUMN_COMPUTER_ID + "=a." + ComputerTable.COLUMN_COMPUTER_ID +
				" AND " + PayTypeTable.COLUMN_PAY_TYPE_ID + "=" + PaymentDetailDataSource.PAY_TYPE_CASH +
				") AS TotalCash, " +
				"(SELECT SUM(" + PaymentDetailTable.COLUMN_PAY_AMOUNT + ") " +
				" FROM " + PaymentDetailTable.TABLE_NAME +
				" WHERE " + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=a." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND " + ComputerTable.COLUMN_COMPUTER_ID + "=a." + ComputerTable.COLUMN_COMPUTER_ID +
				" AND " + PayTypeTable.COLUMN_PAY_TYPE_ID + "=" + PaymentDetailDataSource.PAY_TYPE_CREDIT +
				") AS TotalCredit " +
				" FROM " + OrderTransactionTable.TABLE_NAME + " a " +
				" INNER JOIN " + OrderDetailTable.TABLE_ORDER + " b " +
				" ON a." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=b." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND a." + ComputerTable.COLUMN_COMPUTER_ID + "=b." + ComputerTable.COLUMN_COMPUTER_ID +
				" WHERE a." + OrderTransactionTable.COLUMN_STATUS_ID + " IN(?, ?) " +
				" AND a." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ? " +  
				" GROUP BY a." + OrderTransactionTable.COLUMN_TRANSACTION_ID;

		Cursor cursor = mSqlite.rawQuery(strSql, 
				new String[]{
				String.valueOf(OrderTransactionDataSource.TRANS_STATUS_SUCCESS),
				String.valueOf(OrderTransactionDataSource.TRANS_STATUS_VOID),
				String.valueOf(mDateFrom), 
				String.valueOf(mDateTo)});
		
		if(cursor.moveToFirst()){
			report = new Report();
			do{
				Report.ReportDetail reportDetail = 
						new Report.ReportDetail();
				reportDetail.setTransactionId(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANSACTION_ID)));
				reportDetail.setComputerId(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
				reportDetail.setTransStatus(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_STATUS_ID)));
				reportDetail.setReceiptNo(cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_NO)));
				reportDetail.setTotalPrice(cursor.getDouble(cursor.getColumnIndex("TotalRetailPrice")));
				reportDetail.setSubTotal(cursor.getDouble(cursor.getColumnIndex("TotalSalePrice")));
				reportDetail.setVatExclude(cursor.getDouble(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT)));
				reportDetail.setDiscount(cursor.getDouble(cursor.getColumnIndex("TotalDiscount")));
				reportDetail.setVatable(cursor.getDouble(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_VATABLE)));
				reportDetail.setTotalVat(cursor.getDouble(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_VAT)));
				reportDetail.setCash(cursor.getDouble(cursor.getColumnIndex("TotalCash")));
				reportDetail.setCredit(cursor.getDouble(cursor.getColumnIndex("TotalCredit")));
				reportDetail.setSendStatus(cursor.getInt(cursor.getColumnIndex(MPOSDatabase.COLUMN_SEND_STATUS)));
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
				for (int i = 0; i < groupLst.size(); i++) {
					Report.GroupOfProduct group = groupLst.get(i);
					Report.GroupOfProduct groupSection = new Report.GroupOfProduct();
					groupSection.setProductDeptName(group.getProductDeptName());
					groupSection.setProductGroupName(group.getProductGroupName());

					// product
					Cursor cursor = mSqlite.rawQuery("SELECT a."
							+ COLUMN_PRODUCT_QTY + ", " + " a."
							+ COLUMN_PRODUCT_QTY_PERCENT + ", " + " a."
							+ COLUMN_PRODUCT_SUB_TOTAL + ", " + " a."
							+ COLUMN_PRODUCT_SUB_TOTAL_PERCENT + ", " + " a."
							+ COLUMN_PRODUCT_DISCOUNT + ", " + " a."
							+ COLUMN_PRODUCT_TOTAL_PRICE + ", " + " a."
							+ COLUMN_PRODUCT_TOTAL_PRICE_PERCENT + ", " + " b."
							+ ProductsTable.COLUMN_PRODUCT_CODE + ", " + " b."
							+ ProductsTable.COLUMN_PRODUCT_NAME + ", " + " b."
							+ ProductsTable.COLUMN_PRODUCT_PRICE + ", " + " b."
							+ ProductsTable.COLUMN_VAT_TYPE + " FROM "
							+ TEMP_PRODUCT_REPORT + " a " + " INNER JOIN "
							+ ProductsTable.TABLE_NAME + " b " + " ON a."
							+ ProductsTable.COLUMN_PRODUCT_ID + "=b."
							+ ProductsTable.COLUMN_PRODUCT_ID + " WHERE b."
							+ ProductsTable.COLUMN_PRODUCT_DEPT_ID + "=?"
							+ " ORDER BY b." + ProductsTable.COLUMN_ORDERING,
							new String[] { 
									String.valueOf(group.getProductDeptId()) 
							});

					if (cursor.moveToFirst()) {
						do {
							Report.ReportDetail reportDetail = new Report.ReportDetail();
							reportDetail
									.setProductCode(cursor.getString(cursor
											.getColumnIndex(ProductsTable.COLUMN_PRODUCT_CODE)));
							reportDetail
									.setProductName(cursor.getString(cursor
											.getColumnIndex(ProductsTable.COLUMN_PRODUCT_NAME)));
							reportDetail
									.setPricePerUnit(cursor.getDouble(cursor
											.getColumnIndex(ProductsTable.COLUMN_PRODUCT_PRICE)));
							reportDetail.setQty(cursor.getDouble(cursor
									.getColumnIndex(COLUMN_PRODUCT_QTY)));
							reportDetail.setQtyPercent(cursor.getDouble(cursor
									.getColumnIndex(COLUMN_PRODUCT_QTY_PERCENT)));
							reportDetail.setSubTotal(cursor.getDouble(cursor
									.getColumnIndex(COLUMN_PRODUCT_SUB_TOTAL)));
							reportDetail
									.setSubTotalPercent(cursor.getDouble(cursor
											.getColumnIndex(COLUMN_PRODUCT_SUB_TOTAL_PERCENT)));
							reportDetail.setDiscount(cursor.getDouble(cursor
									.getColumnIndex(COLUMN_PRODUCT_DISCOUNT)));
							reportDetail.setTotalPrice(cursor.getDouble(cursor
									.getColumnIndex(COLUMN_PRODUCT_TOTAL_PRICE)));
							reportDetail
									.setTotalPricePercent(cursor.getDouble(cursor
											.getColumnIndex(COLUMN_PRODUCT_TOTAL_PRICE_PERCENT)));
							int vatType = cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_VAT_TYPE));
							String vatTypeText = "N";
							switch(vatType){
							case 0:
								vatTypeText = "N";
								break;
							case 1:
								vatTypeText = "V";
								break;
							case 2:
								vatTypeText = "E";
								break;
							}
							reportDetail.setVat(vatTypeText);
							groupSection.reportDetail.add(reportDetail);
						} while (cursor.moveToNext());
					}
					
					// dept summary
					groupSection.reportDetail.add(getSummaryByDept(group.getProductDeptId()));
					if(i < groupLst.size() - 1){
						if(group.getProductGroupId() != groupLst.get(i + 1).getProductGroupId()){
							groupSection.reportDetail.add(getSummaryByGroup(group.getProductGroupId()));
						}
					}else if(i == groupLst.size() - 1){
						groupSection.reportDetail.add(getSummaryByGroup(group.getProductGroupId()));
					}
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
				" SELECT SUM(o." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS TotalQty, " +
				" SUM(o." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS TotalRetailPrice, " +
				" SUM(o." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") AS TotalDiscount, " +
				" SUM(o." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") AS TotalSalePrice " +
				" FROM " + OrderTransactionTable.TABLE_NAME + " t " + 
				" INNER JOIN " + OrderDetailTable.TABLE_ORDER + " o " +
				" ON t." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=o." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND t." + ComputerTable.COLUMN_COMPUTER_ID + "=o." + ComputerTable.COLUMN_COMPUTER_ID +
				" INNER JOIN " + ProductsTable.TABLE_NAME + " p " +
				" ON o." + ProductsTable.COLUMN_PRODUCT_ID + "=p." + ProductsTable.COLUMN_PRODUCT_ID +
				" INNER JOIN " + ProductDeptTable.TABLE_NAME + " pd " +
				" ON p." + ProductsTable.COLUMN_PRODUCT_DEPT_ID + "=pd." + ProductsTable.COLUMN_PRODUCT_DEPT_ID +
				" WHERE t." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ?" +
				" AND t." + OrderTransactionTable.COLUMN_STATUS_ID + "=?" +
				" AND pd." + ProductsTable.COLUMN_PRODUCT_GROUP_ID + "=?" +
				" GROUP BY pd." + ProductsTable.COLUMN_PRODUCT_GROUP_ID,
				new String[]{
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(OrderTransactionDataSource.TRANS_STATUS_SUCCESS),
						String.valueOf(groupId)
				});
		
		if(cursor.moveToFirst()){
			Report.ReportDetail summReport = getProductSummaryAll();
			report = new Report.ReportDetail();
			report.setProductName(SUMM_GROUP);
			report.setQty(cursor.getDouble(cursor.getColumnIndex("TotalQty")));
			report.setQtyPercent(report.getQty() / summReport.getQty() * 100);
			report.setSubTotal(cursor.getDouble(cursor.getColumnIndex("TotalRetailPrice")));
			report.setSubTotalPercent(report.getSubTotal() / summReport.getSubTotal() * 100);
			report.setDiscount(cursor.getDouble(cursor.getColumnIndex("TotalDiscount")));
			report.setTotalPrice(cursor.getDouble(cursor.getColumnIndex("TotalSalePrice")));
			report.setTotalPricePercent(report.getTotalPrice() / summReport.getTotalPrice() * 100);
		}
		cursor.close();
		return report;
	}
	
	public Report.ReportDetail getSummaryByDept(int deptId){
		Report.ReportDetail report = null;
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(o." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS TotalQty, " +
				" SUM(o." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS TotalRetailPrice, " +
				" SUM(o." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") AS TotalDiscount, " +
				" SUM(o." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") AS TotalSalePrice " +
				" FROM " + OrderTransactionTable.TABLE_NAME + " t " + 
				" INNER JOIN " + OrderDetailTable.TABLE_ORDER + " o " +
				" ON t." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=o." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND t." + ComputerTable.COLUMN_COMPUTER_ID + "=o." + ComputerTable.COLUMN_COMPUTER_ID +
				" INNER JOIN " + ProductsTable.TABLE_NAME + " p " +
				" ON o." + ProductsTable.COLUMN_PRODUCT_ID + "=p." + ProductsTable.COLUMN_PRODUCT_ID +
				" WHERE t." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ?" +
				" AND t." + OrderTransactionTable.COLUMN_STATUS_ID + "=?" +
				" AND p." + ProductsTable.COLUMN_PRODUCT_DEPT_ID + "=?" +
				" GROUP BY p." + ProductsTable.COLUMN_PRODUCT_DEPT_ID,
				new String[]{
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(OrderTransactionDataSource.TRANS_STATUS_SUCCESS),
						String.valueOf(deptId)
				});
		
		if(cursor.moveToFirst()){
			Report.ReportDetail summReport = getProductSummaryAll();
			report = new Report.ReportDetail();
			report.setProductName(SUMM_DEPT);
			report.setQty(cursor.getDouble(cursor.getColumnIndex("TotalQty")));
			report.setQtyPercent(report.getQty() / summReport.getQty() * 100);
			report.setSubTotal(cursor.getDouble(cursor.getColumnIndex("TotalRetailPrice")));
			report.setSubTotalPercent(report.getSubTotal() / summReport.getSubTotal() * 100);
			report.setDiscount(cursor.getDouble(cursor.getColumnIndex("TotalDiscount")));
			report.setTotalPrice(cursor.getDouble(cursor.getColumnIndex("TotalSalePrice")));
			report.setTotalPricePercent(report.getTotalPrice() / summReport.getTotalPrice() * 100);
		}
		cursor.close();
		return report;
	}
	
	public Report.ReportDetail getProductSummaryAll(){
		Report.ReportDetail report = null;
		Cursor cursor = mSqlite.query(TEMP_PRODUCT_REPORT, 
				new String[]{
					COLUMN_PRODUCT_SUMM_QTY,
					COLUMN_PRODUCT_SUMM_SUB_TOTAL,
					COLUMN_PRODUCT_SUMM_DISCOUNT,
					COLUMN_PRODUCT_SUMM_TOTAL_PRICE,
				}, null, null, null, null, null, "1");
		
		if(cursor.moveToFirst()){
			report = new Report.ReportDetail();
			report.setQty(cursor.getDouble(cursor.getColumnIndex(COLUMN_PRODUCT_SUMM_QTY)));
			report.setSubTotal(cursor.getDouble(cursor.getColumnIndex(COLUMN_PRODUCT_SUMM_SUB_TOTAL)));
			report.setDiscount(cursor.getDouble(cursor.getColumnIndex(COLUMN_PRODUCT_SUMM_DISCOUNT)));
			report.setTotalPrice(cursor.getDouble(cursor.getColumnIndex(COLUMN_PRODUCT_SUMM_TOTAL_PRICE)));
		}
		cursor.close();
		return report;
	}
	
	private void createProductDataTmp() throws SQLException{
		Cursor cursor = mSqlite.rawQuery(
				" SELECT b." + ProductsTable.COLUMN_PRODUCT_ID + ", " +
				" SUM(b." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS Qty, " +
			    " SUM(b." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS RetailPrice, " +
				" SUM(b." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") AS Discount, " +
			    " SUM(b." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") AS SalePrice, " +
				// total qty
				" (SELECT SUM(o." + OrderDetailTable.COLUMN_ORDER_QTY + ") " +
				" FROM " + OrderTransactionTable.TABLE_NAME + " t " + 
				" INNER JOIN " + OrderDetailTable.TABLE_ORDER + " o " +
				" ON t." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=o." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND t." + ComputerTable.COLUMN_COMPUTER_ID + "=o." + ComputerTable.COLUMN_COMPUTER_ID +
				" WHERE t." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ?" +
				" AND t." + OrderTransactionTable.COLUMN_STATUS_ID + "=?) AS TotalQty, " +
				// total retail price
				" (SELECT SUM(o." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") " +
				" FROM " + OrderTransactionTable.TABLE_NAME + " t " + 
				" INNER JOIN " + OrderDetailTable.TABLE_ORDER + " o " +
				" ON t." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=o." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND t." + ComputerTable.COLUMN_COMPUTER_ID + "=o." + ComputerTable.COLUMN_COMPUTER_ID +
				" WHERE t." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ?" +
				" AND t." + OrderTransactionTable.COLUMN_STATUS_ID + "=?) AS TotalRetailPrice, " +
				// total discount
				" (SELECT SUM(o." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") " +
				" FROM " + OrderTransactionTable.TABLE_NAME + " t " + 
				" INNER JOIN " + OrderDetailTable.TABLE_ORDER + " o " +
				" ON t." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=o." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND t." + ComputerTable.COLUMN_COMPUTER_ID + "=o." + ComputerTable.COLUMN_COMPUTER_ID +
				" WHERE t." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ?" +
				" AND t." + OrderTransactionTable.COLUMN_STATUS_ID + "=?) AS TotalDiscount, " +
				// total sale price
				" (SELECT SUM(o." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") " +
				" FROM " + OrderTransactionTable.TABLE_NAME + " t " + 
				" INNER JOIN " + OrderDetailTable.TABLE_ORDER + " o " +
				" ON t." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=o." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND t." + ComputerTable.COLUMN_COMPUTER_ID + "=o." + ComputerTable.COLUMN_COMPUTER_ID +
				" WHERE t." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ?" +
				" AND t." + OrderTransactionTable.COLUMN_STATUS_ID + "=?) AS TotalSalePrice " +
				" FROM " + OrderTransactionTable.TABLE_NAME + " a " +
				" INNER JOIN " + OrderDetailTable.TABLE_ORDER + " b " +
				" ON a." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=b." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND a." + ComputerTable.COLUMN_COMPUTER_ID + "=b." + ComputerTable.COLUMN_COMPUTER_ID + 
				" WHERE a." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ?" +
				" AND a." + OrderTransactionTable.COLUMN_STATUS_ID + "=?" +
				" GROUP BY b." + ProductsTable.COLUMN_PRODUCT_ID, 
				new String[]{
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(OrderTransactionDataSource.TRANS_STATUS_SUCCESS),
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(OrderTransactionDataSource.TRANS_STATUS_SUCCESS),
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(OrderTransactionDataSource.TRANS_STATUS_SUCCESS),
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(OrderTransactionDataSource.TRANS_STATUS_SUCCESS),
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(OrderTransactionDataSource.TRANS_STATUS_SUCCESS)
				});
		
		if(cursor.moveToFirst()){
			do{
				double qty = cursor.getDouble(cursor.getColumnIndex("Qty"));
				double summQty = cursor.getDouble(cursor.getColumnIndex("TotalQty"));
				double qtyPercent = (qty / summQty) * 100;
				double retailPrice = cursor.getDouble(cursor.getColumnIndex("RetailPrice"));
				double summRetailPrice = cursor.getDouble(cursor.getColumnIndex("TotalRetailPrice"));
				double retailPricePercent = (retailPrice / summRetailPrice) * 100;
				double discount = cursor.getDouble(cursor.getColumnIndex("Discount"));
				double summDiscount = cursor.getDouble(cursor.getColumnIndex("TotalDiscount"));
				double salePrice = cursor.getDouble(cursor.getColumnIndex("SalePrice"));
				double summSalePrice = cursor.getDouble(cursor.getColumnIndex("TotalSalePrice"));
				double salePricePercent = (salePrice / summSalePrice) * 100;
				
				ContentValues cv = new ContentValues();
				cv.put(ProductsTable.COLUMN_PRODUCT_ID, cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_ID)));
				cv.put(COLUMN_PRODUCT_QTY, qty);
				cv.put(COLUMN_PRODUCT_SUMM_QTY, summQty);
				cv.put(COLUMN_PRODUCT_QTY_PERCENT, qtyPercent);
				cv.put(COLUMN_PRODUCT_SUB_TOTAL, retailPrice);
				cv.put(COLUMN_PRODUCT_SUMM_SUB_TOTAL, summRetailPrice);
				cv.put(COLUMN_PRODUCT_SUB_TOTAL_PERCENT, retailPricePercent);
				cv.put(COLUMN_PRODUCT_DISCOUNT, discount);
				cv.put(COLUMN_PRODUCT_SUMM_DISCOUNT, summDiscount);
				cv.put(COLUMN_PRODUCT_TOTAL_PRICE, salePrice);
				cv.put(COLUMN_PRODUCT_SUMM_TOTAL_PRICE, summSalePrice);
				cv.put(COLUMN_PRODUCT_TOTAL_PRICE_PERCENT, salePricePercent);
				
				try {
					mSqlite.insertOrThrow(TEMP_PRODUCT_REPORT, null, cv);
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
					" SELECT c." + ProductsTable.COLUMN_PRODUCT_DEPT_ID + ", " + 
					" c." + ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME + ", " + 
					" d." + ProductsTable.COLUMN_PRODUCT_GROUP_ID + ", " +
					" d." + ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME + 
					" FROM " + TEMP_PRODUCT_REPORT + " a " +
					" INNER JOIN " + ProductsTable.TABLE_NAME + " b " +
					" ON a." + ProductsTable.COLUMN_PRODUCT_ID + "=b." + ProductsTable.COLUMN_PRODUCT_ID +
					" INNER JOIN " + ProductDeptTable.TABLE_NAME + " c " +
					" ON b." + ProductsTable.COLUMN_PRODUCT_DEPT_ID + "=c." + ProductsTable.COLUMN_PRODUCT_DEPT_ID +
					" INNER JOIN " + ProductGroupTable.TABLE_NAME + " d " +
					" ON c." + ProductsTable.COLUMN_PRODUCT_GROUP_ID + "=d." + ProductsTable.COLUMN_PRODUCT_GROUP_ID + 
					" GROUP BY d." + ProductsTable.COLUMN_PRODUCT_GROUP_ID + ", " +
					" c." + ProductsTable.COLUMN_PRODUCT_DEPT_ID +
					" ORDER BY d." + ProductsTable.COLUMN_ORDERING + "," +
					" c." + ProductsTable.COLUMN_ORDERING, null);
		
		if(cursor.moveToFirst()){
			reportLst = new ArrayList<Report.GroupOfProduct>();
			do{
				Report.GroupOfProduct report = new Report.GroupOfProduct();
				report.setProductDeptId(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_DEPT_ID)));
				report.setProductGroupId(cursor.getInt(cursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_GROUP_ID)));
				report.setProductGroupName(cursor.getString(cursor.getColumnIndex(ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME)));
				report.setProductDeptName(cursor.getString(cursor.getColumnIndex(ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME)));
				reportLst.add(report);
			}while(cursor.moveToNext());
		}
		cursor.close();
		
		return reportLst;
	}
	
	private void createReportProductTmp() throws SQLException{
		mSqlite.execSQL("DROP TABLE IF EXISTS " + TEMP_PRODUCT_REPORT);
		mSqlite.execSQL("CREATE TABLE " + TEMP_PRODUCT_REPORT + " ( " +
				ProductsTable.COLUMN_PRODUCT_ID + " INTEGER, " +
				COLUMN_PRODUCT_QTY + " REAL, " +
				COLUMN_PRODUCT_QTY_PERCENT + " REAL, " +
				COLUMN_PRODUCT_SUB_TOTAL + " REAL, " +
				COLUMN_PRODUCT_SUB_TOTAL_PERCENT + " REAL, " +
				COLUMN_PRODUCT_DISCOUNT + " REAL, " +
				COLUMN_PRODUCT_TOTAL_PRICE + " REAL, " +
				COLUMN_PRODUCT_TOTAL_PRICE_PERCENT + " REAL, " +
				COLUMN_PRODUCT_SUMM_QTY + " REAL, " +
				COLUMN_PRODUCT_SUMM_SUB_TOTAL + " REAL, " +
				COLUMN_PRODUCT_SUMM_DISCOUNT + " REAL, " +
				COLUMN_PRODUCT_SUMM_TOTAL_PRICE + " REAL);"); 
	}
}
