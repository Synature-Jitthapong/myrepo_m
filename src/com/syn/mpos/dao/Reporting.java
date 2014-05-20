package com.syn.mpos.dao;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.dao.ComputerDao.ComputerTable;
import com.syn.mpos.dao.PaymentDao.PayTypeTable;
import com.syn.mpos.dao.PaymentDao.PaymentDetailTable;
import com.syn.mpos.dao.ProductsDao.ProductDeptTable;
import com.syn.mpos.dao.ProductsDao.ProductGroupTable;
import com.syn.mpos.dao.ProductsDao.ProductsTable;
import com.syn.mpos.dao.TransactionDao.OrderDetailTable;
import com.syn.mpos.dao.TransactionDao.OrderTransactionTable;
import com.syn.pos.Report;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

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
	
	public Reporting(Context context, long dFrom, long dTo){
		super(context);
		mDateFrom = dFrom;
		mDateTo = dTo;
	}
	
	public Reporting(Context context){
		super(context);
	}
	
	/**
	 * For create Summary By Sale Day
	 * @param saleDate
	 * @return List<SimpleProductData> 
	 */
	public List<SimpleProductData> listSummaryByProductDept(String saleDate){
		List<SimpleProductData> simpleLst = new ArrayList<SimpleProductData>();
		Cursor mainCursor = getReadableDatabase().rawQuery(
				"SELECT SUM(b." + OrderDetailTable.COLUMN_ORDER_QTY + ")"
				+ " AS " + OrderDetailTable.COLUMN_ORDER_QTY + ","
				+ " SUM(b." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") "
				+ " AS " + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ","
				+ " d." + ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME + ", "
				+ " d." + ProductsTable.COLUMN_PRODUCT_DEPT_ID
				+ " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS + " a "
				+ " LEFT JOIN " + OrderDetailTable.TABLE_ORDER + " b "
				+ " ON a." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "="
				+ " b." + OrderTransactionTable.COLUMN_TRANSACTION_ID 
				+ " LEFT JOIN " + ProductsTable.TABLE_PRODUCTS + " c "
				+ " ON b." + ProductsTable.COLUMN_PRODUCT_ID + "="
				+ " c." + ProductsTable.COLUMN_PRODUCT_ID
				+ " LEFT JOIN " + ProductDeptTable.TABLE_PRODUCT_DEPT + " d "
				+ " ON c." + ProductsTable.COLUMN_PRODUCT_DEPT_ID
				+ " WHERE a." + OrderTransactionTable.COLUMN_SALE_DATE + "=?"
				+ " AND a." + OrderTransactionTable.COLUMN_STATUS_ID + "=?"
				+ " GROUP BY d." + ProductsTable.COLUMN_PRODUCT_DEPT_ID,
				new String[]{
						saleDate,
						String.valueOf(TransactionDao.TRANS_STATUS_SUCCESS)
				});
		if(mainCursor.moveToFirst()){
			do{
				SimpleProductData sp = new SimpleProductData();
				sp.setDeptTotalQty(mainCursor.getInt(mainCursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_QTY)));
				sp.setDeptTotalPrice(mainCursor.getDouble(mainCursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE)));
				sp.setDeptName(mainCursor.getString(mainCursor.getColumnIndex(ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME)));
				
				Cursor detailCursor = getReadableDatabase().rawQuery(
						"SELECT SUM(b." + OrderDetailTable.COLUMN_ORDER_QTY + ") "
						+ " AS " + OrderDetailTable.COLUMN_ORDER_QTY + ","
						+ " SUM(b." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ")"
						+ " AS " + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", "
						+ " c." + ProductsTable.COLUMN_PRODUCT_NAME
						+ " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS + " a "
						+ " LEFT JOIN " + OrderDetailTable.TABLE_ORDER + " b "
						+ " ON a." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "="
						+ " b." + OrderTransactionTable.COLUMN_TRANSACTION_ID 
						+ " LEFT JOIN " + ProductsTable.TABLE_PRODUCTS + " c "
						+ " ON b." + ProductsTable.COLUMN_PRODUCT_ID + "="
						+ " c." + ProductsTable.COLUMN_PRODUCT_ID
						+ " WHERE a." + OrderTransactionTable.COLUMN_SALE_DATE + "=?"
						+ " AND a." + OrderTransactionTable.COLUMN_STATUS_ID + "=?"
						+ " AND c." + ProductsTable.COLUMN_PRODUCT_DEPT_ID + "=?"
						+ " GROUP BY c." + ProductsTable.COLUMN_PRODUCT_ID, 
						new String[]{
								saleDate,
								String.valueOf(TransactionDao.TRANS_STATUS_SUCCESS),
								String.valueOf(mainCursor.getInt(mainCursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_DEPT_ID)))
						});
				if(detailCursor.moveToFirst()){
					do{
						SimpleProductData.Item item = new SimpleProductData.Item();
						item.setItemName(detailCursor.getString(detailCursor.getColumnIndex(ProductsTable.COLUMN_PRODUCT_NAME)));
						item.setTotalQty(detailCursor.getInt(detailCursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_QTY)));
						item.setTotalPrice(detailCursor.getDouble(detailCursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE)));
						sp.getItemLst().add(item);
					}while(detailCursor.moveToNext());
				}
				simpleLst.add(sp);
				detailCursor.close();
			}while(mainCursor.moveToNext());
		}
		mainCursor.close();
		return simpleLst;
	}
	
	public double getTotalPayByPayType(int transactionId, int payTypeId){
		double totalPay = 0.0f;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT SUM(" + PaymentDetailTable.COLUMN_PAY_AMOUNT + ") " +
				" FROM " + PaymentDetailTable.TABLE_PAYMENT_DETAIL +
				" WHERE " + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? " +
				" AND " + PayTypeTable.COLUMN_PAY_TYPE_ID + "=?", 
				new String[]{String.valueOf(transactionId),
				String.valueOf(payTypeId)});
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
				" a." + BaseColumn.COLUMN_SEND_STATUS + ", " +
				" SUM(b." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + " * b." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS TotalRetailPrice, " +
				" SUM(b." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + " * b." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS TotalSalePrice, " +
				" a." + OrderTransactionTable.COLUMN_OTHER_DISCOUNT + " + " + 
				" SUM(b." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + " + " + 
				" b." + OrderDetailTable.COLUMN_MEMBER_DISCOUNT + ") AS TotalDiscount, " +
				"(SELECT SUM(" + PaymentDetailTable.COLUMN_PAY_AMOUNT + ") " +
				" FROM " + PaymentDetailTable.TABLE_PAYMENT_DETAIL +
				" WHERE " + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=a." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND " + ComputerTable.COLUMN_COMPUTER_ID + "=a." + ComputerTable.COLUMN_COMPUTER_ID +
				") AS TotalPayment " +
				" FROM " + OrderTransactionTable.TABLE_ORDER_TRANS + " a " +
				" INNER JOIN " + OrderDetailTable.TABLE_ORDER + " b " +
				" ON a." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=b." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND a." + ComputerTable.COLUMN_COMPUTER_ID + "=b." + ComputerTable.COLUMN_COMPUTER_ID +
				" WHERE a." + OrderTransactionTable.COLUMN_STATUS_ID + " IN(?, ?) " +
				" AND a." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ? " +  
				" GROUP BY a." + OrderTransactionTable.COLUMN_TRANSACTION_ID;

		Cursor cursor = getReadableDatabase().rawQuery(strSql, 
				new String[]{
				String.valueOf(TransactionDao.TRANS_STATUS_SUCCESS),
				String.valueOf(TransactionDao.TRANS_STATUS_VOID),
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
				reportDetail.setTotalPayment(cursor.getDouble(cursor.getColumnIndex("TotalPayment")));
				reportDetail.setSendStatus(cursor.getInt(cursor.getColumnIndex(BaseColumn.COLUMN_SEND_STATUS)));
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
					Cursor cursor = getReadableDatabase().rawQuery("SELECT a."
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
							+ ProductsTable.TABLE_PRODUCTS + " b " + " ON a."
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
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT SUM(o." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS TotalQty, " +
				" SUM(o." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS TotalRetailPrice, " +
				" SUM(o." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") AS TotalDiscount, " +
				" SUM(o." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") AS TotalSalePrice " +
				" FROM " + OrderTransactionTable.TABLE_ORDER_TRANS + " t " + 
				" INNER JOIN " + OrderDetailTable.TABLE_ORDER + " o " +
				" ON t." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=o." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND t." + ComputerTable.COLUMN_COMPUTER_ID + "=o." + ComputerTable.COLUMN_COMPUTER_ID +
				" INNER JOIN " + ProductsTable.TABLE_PRODUCTS + " p " +
				" ON o." + ProductsTable.COLUMN_PRODUCT_ID + "=p." + ProductsTable.COLUMN_PRODUCT_ID +
				" INNER JOIN " + ProductDeptTable.TABLE_PRODUCT_DEPT + " pd " +
				" ON p." + ProductsTable.COLUMN_PRODUCT_DEPT_ID + "=pd." + ProductsTable.COLUMN_PRODUCT_DEPT_ID +
				" WHERE t." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ?" +
				" AND t." + OrderTransactionTable.COLUMN_STATUS_ID + "=?" +
				" AND pd." + ProductsTable.COLUMN_PRODUCT_GROUP_ID + "=?" +
				" GROUP BY pd." + ProductsTable.COLUMN_PRODUCT_GROUP_ID,
				new String[]{
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(TransactionDao.TRANS_STATUS_SUCCESS),
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
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT SUM(o." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS TotalQty, " +
				" SUM(o." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS TotalRetailPrice, " +
				" SUM(o." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") AS TotalDiscount, " +
				" SUM(o." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") AS TotalSalePrice " +
				" FROM " + OrderTransactionTable.TABLE_ORDER_TRANS + " t " + 
				" INNER JOIN " + OrderDetailTable.TABLE_ORDER + " o " +
				" ON t." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=o." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND t." + ComputerTable.COLUMN_COMPUTER_ID + "=o." + ComputerTable.COLUMN_COMPUTER_ID +
				" INNER JOIN " + ProductsTable.TABLE_PRODUCTS + " p " +
				" ON o." + ProductsTable.COLUMN_PRODUCT_ID + "=p." + ProductsTable.COLUMN_PRODUCT_ID +
				" WHERE t." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ?" +
				" AND t." + OrderTransactionTable.COLUMN_STATUS_ID + "=?" +
				" AND p." + ProductsTable.COLUMN_PRODUCT_DEPT_ID + "=?" +
				" GROUP BY p." + ProductsTable.COLUMN_PRODUCT_DEPT_ID,
				new String[]{
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(TransactionDao.TRANS_STATUS_SUCCESS),
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
		Cursor cursor = getReadableDatabase().query(TEMP_PRODUCT_REPORT, 
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
		Cursor cursor = getWritableDatabase().rawQuery(
				" SELECT b." + ProductsTable.COLUMN_PRODUCT_ID + ", " +
				" SUM(b." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS Qty, " +
			    " SUM(b." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS RetailPrice, " +
				" SUM(b." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") AS Discount, " +
			    " SUM(b." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") AS SalePrice, " +
				// total qty
				" (SELECT SUM(o." + OrderDetailTable.COLUMN_ORDER_QTY + ") " +
				" FROM " + OrderTransactionTable.TABLE_ORDER_TRANS + " t " + 
				" INNER JOIN " + OrderDetailTable.TABLE_ORDER + " o " +
				" ON t." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=o." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND t." + ComputerTable.COLUMN_COMPUTER_ID + "=o." + ComputerTable.COLUMN_COMPUTER_ID +
				" WHERE t." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ?" +
				" AND t." + OrderTransactionTable.COLUMN_STATUS_ID + "=?) AS TotalQty, " +
				// total retail price
				" (SELECT SUM(o." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") " +
				" FROM " + OrderTransactionTable.TABLE_ORDER_TRANS + " t " + 
				" INNER JOIN " + OrderDetailTable.TABLE_ORDER + " o " +
				" ON t." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=o." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND t." + ComputerTable.COLUMN_COMPUTER_ID + "=o." + ComputerTable.COLUMN_COMPUTER_ID +
				" WHERE t." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ?" +
				" AND t." + OrderTransactionTable.COLUMN_STATUS_ID + "=?) AS TotalRetailPrice, " +
				// total discount
				" (SELECT SUM(o." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") " +
				" FROM " + OrderTransactionTable.TABLE_ORDER_TRANS + " t " + 
				" INNER JOIN " + OrderDetailTable.TABLE_ORDER + " o " +
				" ON t." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=o." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND t." + ComputerTable.COLUMN_COMPUTER_ID + "=o." + ComputerTable.COLUMN_COMPUTER_ID +
				" WHERE t." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ?" +
				" AND t." + OrderTransactionTable.COLUMN_STATUS_ID + "=?) AS TotalDiscount, " +
				// total sale price
				" (SELECT SUM(o." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") " +
				" FROM " + OrderTransactionTable.TABLE_ORDER_TRANS + " t " + 
				" INNER JOIN " + OrderDetailTable.TABLE_ORDER + " o " +
				" ON t." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=o." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND t." + ComputerTable.COLUMN_COMPUTER_ID + "=o." + ComputerTable.COLUMN_COMPUTER_ID +
				" WHERE t." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ?" +
				" AND t." + OrderTransactionTable.COLUMN_STATUS_ID + "=?) AS TotalSalePrice " +
				" FROM " + OrderTransactionTable.TABLE_ORDER_TRANS + " a " +
				" INNER JOIN " + OrderDetailTable.TABLE_ORDER + " b " +
				" ON a." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=b." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
				" AND a." + ComputerTable.COLUMN_COMPUTER_ID + "=b." + ComputerTable.COLUMN_COMPUTER_ID + 
				" WHERE a." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ?" +
				" AND a." + OrderTransactionTable.COLUMN_STATUS_ID + "=?" +
				" GROUP BY b." + ProductsTable.COLUMN_PRODUCT_ID, 
				new String[]{
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(TransactionDao.TRANS_STATUS_SUCCESS),
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(TransactionDao.TRANS_STATUS_SUCCESS),
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(TransactionDao.TRANS_STATUS_SUCCESS),
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(TransactionDao.TRANS_STATUS_SUCCESS),
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(TransactionDao.TRANS_STATUS_SUCCESS)
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
					getWritableDatabase().insertOrThrow(TEMP_PRODUCT_REPORT, null, cv);
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
		
		Cursor cursor = getReadableDatabase().rawQuery(
					" SELECT c." + ProductsTable.COLUMN_PRODUCT_DEPT_ID + ", " + 
					" c." + ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME + ", " + 
					" d." + ProductsTable.COLUMN_PRODUCT_GROUP_ID + ", " +
					" d." + ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME + 
					" FROM " + TEMP_PRODUCT_REPORT + " a " +
					" INNER JOIN " + ProductsTable.TABLE_PRODUCTS + " b " +
					" ON a." + ProductsTable.COLUMN_PRODUCT_ID + "=b." + ProductsTable.COLUMN_PRODUCT_ID +
					" INNER JOIN " + ProductDeptTable.TABLE_PRODUCT_DEPT + " c " +
					" ON b." + ProductsTable.COLUMN_PRODUCT_DEPT_ID + "=c." + ProductsTable.COLUMN_PRODUCT_DEPT_ID +
					" INNER JOIN " + ProductGroupTable.TABLE_PRODUCT_GROUP + " d " +
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
		getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + TEMP_PRODUCT_REPORT);
		getWritableDatabase().execSQL("CREATE TABLE " + TEMP_PRODUCT_REPORT + " ( " +
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
	
	/**
	 * @author j1tth4
	 * Create for Summary Sale By Day
	 */
	public static class SimpleProductData{
		private String deptName;
		private int deptTotalQty;
		private double deptTotalPrice;
		private List<Item> itemLst = new ArrayList<Item>();
		
		public String getDeptName() {
			return deptName;
		}

		public void setDeptName(String deptName) {
			this.deptName = deptName;
		}

		public int getDeptTotalQty() {
			return deptTotalQty;
		}

		public void setDeptTotalQty(int deptTotalQty) {
			this.deptTotalQty = deptTotalQty;
		}

		public double getDeptTotalPrice() {
			return deptTotalPrice;
		}

		public void setDeptTotalPrice(double deptTotalPrice) {
			this.deptTotalPrice = deptTotalPrice;
		}

		public List<Item> getItemLst() {
			return itemLst;
		}

		public void setItemLst(List<Item> itemLst) {
			this.itemLst = itemLst;
		}

		public static class Item{
			private String itemName;
			private int totalQty;
			private double totalPrice;
			
			public String getItemName() {
				return itemName;
			}
			public void setItemName(String itemName) {
				this.itemName = itemName;
			}
			public int getTotalQty() {
				return totalQty;
			}
			public void setTotalQty(int totalQty) {
				this.totalQty = totalQty;
			}
			public double getTotalPrice() {
				return totalPrice;
			}
			public void setTotalPrice(double totalPrice) {
				this.totalPrice = totalPrice;
			}
		}
	}
}
