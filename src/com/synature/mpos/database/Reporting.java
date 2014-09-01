package com.synature.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.synature.mpos.database.model.OrderTransaction;
import com.synature.mpos.database.table.ComputerTable;
import com.synature.mpos.database.table.OrderDetailTable;
import com.synature.mpos.database.table.OrderTransTable;
import com.synature.mpos.database.table.PaymentDetailTable;
import com.synature.mpos.database.table.ProductDeptTable;
import com.synature.mpos.database.table.ProductGroupTable;
import com.synature.mpos.database.table.ProductTable;
import com.synature.mpos.database.table.SessionTable;
import com.synature.pos.Report;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class Reporting extends MPOSDatabase{
	
	public static final String SUMM_DEPT = "summ_dept";
	public static final String SUMM_GROUP = "summ_group";
	
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
	
	protected String mDateFrom;
	protected String mDateTo;
	
	public Reporting(Context context, String dFrom, String dTo){
		super(context);
		mDateFrom = dFrom;
		mDateTo = dTo;
	}
	
	public Reporting(Context context){
		super(context);
	}
	
	public void setDateFrom(String mDateFrom) {
		this.mDateFrom = mDateFrom;
	}

	public void setDateTo(String mDateTo) {
		this.mDateTo = mDateTo;
	}
	
	/**
	 * list transaction for print bill report
	 * @return List<SaleTransactionReport>
	 */
	public List<SaleTransactionReport> listTransactionReport(){
		List<SaleTransactionReport> transLst = new ArrayList<SaleTransactionReport>();
		Cursor mainCursor = getReadableDatabase().rawQuery(
				"SELECT " + OrderTransTable.COLUMN_SALE_DATE
				+ " FROM " + OrderTransTable.TABLE_ORDER_TRANS
				+ " WHERE " + OrderTransTable.COLUMN_SALE_DATE
				+ " BETWEEN ? AND ?"
				+ " AND " + OrderTransTable.COLUMN_STATUS_ID
				+ " IN (?,?)"
				+ " GROUP BY " + OrderTransTable.COLUMN_SALE_DATE,
				new String[]{
						mDateFrom,
						mDateTo,
						String.valueOf(Transaction.TRANS_STATUS_HOLD),
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS)
				});
		if(mainCursor.moveToFirst()){
			do{
				SaleTransactionReport trans = new SaleTransactionReport();
				trans.setSaleDate(mainCursor.getString(0));
				
				Cursor detailCursor = getReadableDatabase().rawQuery(
						"SELECT " + OrderTransTable.COLUMN_RECEIPT_NO + ", "
						+ OrderTransTable.COLUMN_CLOSE_TIME + ", "
						+ OrderTransTable.COLUMN_TRANS_VATABLE
						+ " FROM " + OrderTransTable.TABLE_ORDER_TRANS
						+ " WHERE " + OrderTransTable.COLUMN_SALE_DATE + "=?"
						+ " AND " + OrderTransTable.COLUMN_STATUS_ID + " IN(?,?)"
						+ " GROUP BY " + OrderTransTable.COLUMN_TRANS_ID,
						new String[]{
								mainCursor.getString(0),
								String.valueOf(Transaction.TRANS_STATUS_HOLD),
								String.valueOf(Transaction.TRANS_STATUS_SUCCESS)
						});
				if(detailCursor.moveToFirst()){
					do{
						OrderTransaction detail = new OrderTransaction();
						detail.setReceiptNo(detailCursor.getString(detailCursor.getColumnIndex(OrderTransTable.COLUMN_RECEIPT_NO)));
						detail.setCloseTime(detailCursor.getString(detailCursor.getColumnIndex(OrderTransTable.COLUMN_CLOSE_TIME)));
						detail.setTransactionVatable(detailCursor.getDouble(detailCursor.getColumnIndex(OrderTransTable.COLUMN_TRANS_VATABLE)));
						trans.getTransLst().add(detail);
					}while(detailCursor.moveToNext());
				}
				detailCursor.close();
				transLst.add(trans);
			}while(mainCursor.moveToNext());
		}
		mainCursor.close();
		return transLst;
	}
	
	/**
	 * Get Summary Product by Group for print Summary Sale By Day
	 * @return List<SimpleProductData>
	 */
	public List<SimpleProductData> listSummaryProductGroupInDay(int sessId){
		List<SimpleProductData> simpleLst = new ArrayList<SimpleProductData>();
		String selection = "a." + OrderTransTable.COLUMN_STATUS_ID + "=?"
				+ " AND a." + OrderTransTable.COLUMN_SALE_DATE + " BETWEEN ? AND ? "
				+ " AND b." + ProductTable.COLUMN_PRODUCT_TYPE_ID + " IN(?,?,?,?) ";
		String[] selectionArgs = new String[]{
			String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
			mDateFrom,
			mDateTo,
			String.valueOf(Products.NORMAL_TYPE),
			String.valueOf(Products.SET_CAN_SELECT),
			String.valueOf(Products.CHILD_OF_SET_HAVE_PRICE),
			String.valueOf(Products.COMMENT_HAVE_PRICE)
		}; 
		if(sessId != 0){
			selection += " AND a." + SessionTable.COLUMN_SESS_ID + "=?";
			selectionArgs = new String[]{
					String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
					mDateFrom,
					mDateTo,
					String.valueOf(Products.NORMAL_TYPE),
					String.valueOf(Products.SET_CAN_SELECT),
					String.valueOf(Products.CHILD_OF_SET_HAVE_PRICE),
					String.valueOf(Products.COMMENT_HAVE_PRICE),
					String.valueOf(sessId),
			}; 
		}
		Cursor groupCursor = getReadableDatabase().rawQuery(
				" SELECT SUM(b." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS " + OrderDetailTable.COLUMN_ORDER_QTY + ", "
				+ " SUM(b." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS " + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", "
				+ " e." + ProductTable.COLUMN_PRODUCT_GROUP_ID + ", "
				+ " e." + ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME
				+ " FROM " + OrderTransTable.TABLE_ORDER_TRANS + " a "
				+ " LEFT JOIN " + OrderDetailTable.TABLE_ORDER + " b "
				+ " ON a." + OrderTransTable.COLUMN_TRANS_ID + "=b." + OrderTransTable.COLUMN_TRANS_ID
				+ " LEFT JOIN " + ProductTable.TABLE_PRODUCT + " c "
				+ " ON b." + ProductTable.COLUMN_PRODUCT_ID + "=c." + ProductTable.COLUMN_PRODUCT_ID
				+ " LEFT JOIN " + ProductDeptTable.TABLE_PRODUCT_DEPT + " d "
				+ " ON c." + ProductTable.COLUMN_PRODUCT_DEPT_ID + "=d." + ProductTable.COLUMN_PRODUCT_DEPT_ID
				+ " LEFT JOIN " + ProductGroupTable.TABLE_PRODUCT_GROUP + " e "
				+ " ON d." + ProductTable.COLUMN_PRODUCT_GROUP_ID + "=e." + ProductTable.COLUMN_PRODUCT_GROUP_ID
				+ " WHERE " + selection
				+ " GROUP BY e." + ProductTable.COLUMN_PRODUCT_GROUP_ID
				+ " ORDER BY e." + COLUMN_ORDERING + ", e." + ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME + ","
				+ " d." + COLUMN_ORDERING + ", d." + ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME + ", "
				+ " c." + COLUMN_ORDERING + ", c." + ProductTable.COLUMN_PRODUCT_NAME, selectionArgs);
		
		if(groupCursor.moveToFirst()){
			do{
				SimpleProductData sp = new SimpleProductData();
				int pgId = groupCursor.getInt(groupCursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_GROUP_ID));
				sp.setDeptName(groupCursor.getString(groupCursor.getColumnIndex(ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME)));
				sp.setDeptTotalQty(groupCursor.getInt(groupCursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_QTY)));
				sp.setDeptTotalPrice(groupCursor.getDouble(groupCursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE)));
				
				String selection2 = " a." + OrderTransTable.COLUMN_STATUS_ID + "=?"
						+ " AND a." + OrderTransTable.COLUMN_SALE_DATE + " BETWEEN ? AND ? "
						+ " AND b." + ProductTable.COLUMN_PRODUCT_TYPE_ID + " IN (?,?,?,?) "
						+ " AND d." + ProductTable.COLUMN_PRODUCT_GROUP_ID + "=?";
				String[] selectionArgs2 = new String[]{
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						mDateFrom,
						mDateTo,
						String.valueOf(Products.NORMAL_TYPE),
						String.valueOf(Products.SET_CAN_SELECT),
						String.valueOf(Products.CHILD_OF_SET_HAVE_PRICE),
						String.valueOf(Products.COMMENT_HAVE_PRICE),
						String.valueOf(pgId)
				};
				if(sessId != 0){
					selection2 += " AND a." + SessionTable.COLUMN_SESS_ID + "=?";
					selectionArgs2 = new String[]{
							String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
							mDateFrom,
							mDateTo,
							String.valueOf(Products.NORMAL_TYPE),
							String.valueOf(Products.SET_CAN_SELECT),
							String.valueOf(Products.CHILD_OF_SET_HAVE_PRICE),
							String.valueOf(Products.COMMENT_HAVE_PRICE),
							String.valueOf(pgId),
							String.valueOf(sessId)
					};
				}
				Cursor cursor = getReadableDatabase().rawQuery(
						" SELECT SUM(b." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS " + OrderDetailTable.COLUMN_ORDER_QTY + ", "
						+ " SUM(b." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS " + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", "
						+ " c." + ProductTable.COLUMN_PRODUCT_TYPE_ID + ", "
						+ " c." + ProductTable.COLUMN_PRODUCT_CODE + ", "
						+ " c." + ProductTable.COLUMN_PRODUCT_NAME + ", "
						+ " c." + ProductTable.COLUMN_PRODUCT_NAME1 + ", "
						+ " c." + ProductTable.COLUMN_PRODUCT_NAME2
						+ " FROM " + OrderTransTable.TABLE_ORDER_TRANS + " a "
						+ " LEFT JOIN " + OrderDetailTable.TABLE_ORDER + " b "
						+ " ON a." + OrderTransTable.COLUMN_TRANS_ID + "=b." + OrderTransTable.COLUMN_TRANS_ID
						+ " LEFT JOIN " + ProductTable.TABLE_PRODUCT + " c "
						+ " ON b." + ProductTable.COLUMN_PRODUCT_ID + "=c." + ProductTable.COLUMN_PRODUCT_ID
						+ " LEFT JOIN " + ProductDeptTable.TABLE_PRODUCT_DEPT + " d "
						+ " ON c." + ProductTable.COLUMN_PRODUCT_DEPT_ID + "=d." + ProductTable.COLUMN_PRODUCT_DEPT_ID
						+ " WHERE " + selection2
						+ " GROUP BY c." + ProductTable.COLUMN_PRODUCT_ID
						+ " ORDER BY d." + COLUMN_ORDERING + ", d." + ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME + ","
						+ " c." + COLUMN_ORDERING + ", c." + ProductTable.COLUMN_PRODUCT_NAME, selectionArgs2);
				if(cursor.moveToFirst()){
					do{
						SimpleProductData.Item item = new SimpleProductData.Item();
						int productTypeId = cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_TYPE_ID));
						String itemName = cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME));
						String itemName1 = cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME1));
						String itemName2 = cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME2));
						if(productTypeId == Products.CHILD_OF_SET_HAVE_PRICE){
							itemName += "***";
							itemName1 += "***";
							itemName2 += "***";
						}
						item.setItemName(itemName);
						item.setItemName1(itemName1);
						item.setItemName2(itemName2);
						item.setTotalQty(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_QTY)));
						item.setTotalPrice(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE)));
						sp.getItemLst().add(item);
					}while(cursor.moveToNext());
				}
				cursor.close();
				simpleLst.add(sp);
			}while(groupCursor.moveToNext());
		}
		groupCursor.close();
		return simpleLst;
	}
	
	public Report.ReportDetail getBillSummary(){
		String transIds = getTransactionIds();
		Report.ReportDetail report = new Report.ReportDetail();
		String sql = "SELECT "
				+ " SUM(" + OrderTransTable.COLUMN_TRANS_VATABLE + ") AS TransVatable, "
				+ " SUM(" + OrderTransTable.COLUMN_TRANS_VAT + ") AS TransVat, "
				+ " SUM(" + OrderTransTable.COLUMN_TRANS_EXCLUDE_VAT + ") AS TransExcludeVat, "
				// total retail price
				+ " (SELECT SUM(" + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") "
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + OrderTransTable.COLUMN_TRANS_ID + " IN (" + transIds + ")) AS SummTotalRetailPrice, "
				// total discount
				+ " (SELECT SUM(" + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") "
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + OrderTransTable.COLUMN_TRANS_ID + " IN (" + transIds + ")) AS SummTotalDiscount, "
				// total sale price
				+ " (SELECT SUM(" + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + " + "
				+ OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE + ") "
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + OrderTransTable.COLUMN_TRANS_ID + " IN (" + transIds + ")) AS SummTotalSalePrice, "
				// total payment
				+ " (SELECT SUM(" + PaymentDetailTable.COLUMN_PAY_AMOUNT +  ") "
				+ " FROM " + PaymentDetailTable.TABLE_PAYMENT_DETAIL
				+ " WHERE " + OrderTransTable.COLUMN_TRANS_ID + " IN (" + transIds + ")) AS SummTotalPayment "
				+ " FROM " + OrderTransTable.TABLE_ORDER_TRANS
				+ " WHERE " + OrderTransTable.COLUMN_TRANS_ID + " IN (" + transIds + ")"
				+ " AND " + OrderTransTable.COLUMN_STATUS_ID + " =?";
		Cursor cursor = getReadableDatabase().rawQuery(
				sql, 
				new String[]{
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS)
		});
		if(cursor.moveToFirst()){
			report.setVatable(cursor.getDouble(cursor.getColumnIndex("TransVatable")));
			report.setTotalVat(cursor.getDouble(cursor.getColumnIndex("TransVat")));
			report.setVatExclude(cursor.getDouble(cursor.getColumnIndex("TransExcludeVat")));
			report.setTotalPrice(cursor.getDouble(cursor.getColumnIndex("SummTotalRetailPrice")));
			report.setSubTotal(cursor.getDouble(cursor.getColumnIndex("SummTotalSalePrice")));
			report.setDiscount(cursor.getDouble(cursor.getColumnIndex("SummTotalDiscount")));
			report.setTotalPayment(cursor.getDouble(cursor.getColumnIndex("SummTotalPayment")));
		}
		cursor.close();
		return report;
	}
	
	/**
	 * get sale report by bill
	 * @return Report
	 */
	public Report getSaleReportByBill(){
		Report report = new Report();
		String selection = " a." + OrderTransTable.COLUMN_SALE_DATE + " BETWEEN ? AND ? ";
		String strSql = " SELECT a." + OrderTransTable.COLUMN_TRANS_ID + ", "
				+ " a." + ComputerTable.COLUMN_COMPUTER_ID + ", " 
				+ " a." + OrderTransTable.COLUMN_STATUS_ID + ", "
				+ " a." + OrderTransTable.COLUMN_RECEIPT_NO + ","
				+ " a." + OrderTransTable.COLUMN_TRANS_EXCLUDE_VAT + ", "
				+ " a." + OrderTransTable.COLUMN_TRANS_VAT + ", "
				+ " a." + OrderTransTable.COLUMN_TRANS_VATABLE + ", "
				+ " a." + COLUMN_SEND_STATUS + ", " 
				+ " SUM(b." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS TotalRetailPrice, "
				+ " SUM(b." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") AS TotalSalePrice, " 
				+ " a." + OrderTransTable.COLUMN_OTHER_DISCOUNT + " + "
				+ " SUM(b." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + " + "
				+ " b." + OrderDetailTable.COLUMN_MEMBER_DISCOUNT + ") AS TotalDiscount, "
				+ " (SELECT SUM(" + PaymentDetailTable.COLUMN_PAY_AMOUNT + ") " 
				+ " FROM " + PaymentDetailTable.TABLE_PAYMENT_DETAIL 
				+ " WHERE " + OrderTransTable.COLUMN_TRANS_ID + " =a." + OrderTransTable.COLUMN_TRANS_ID + ") AS TotalPayment "
				+ " FROM " + OrderTransTable.TABLE_ORDER_TRANS + " a "
				+ " INNER JOIN " + OrderDetailTable.TABLE_ORDER + " b "
				+ " ON a." + OrderTransTable.COLUMN_TRANS_ID + "=b." + OrderTransTable.COLUMN_TRANS_ID
				+ " WHERE " + selection
				+ " GROUP BY a." + OrderTransTable.COLUMN_TRANS_ID;

		Cursor cursor = getReadableDatabase().rawQuery(strSql,
                new String[]{
                        mDateFrom,
                        mDateTo
                });
		
		if(cursor.moveToFirst()){
			do{
				Report.ReportDetail reportDetail = 
						new Report.ReportDetail();
				reportDetail.setTransactionId(cursor.getInt(cursor.getColumnIndex(OrderTransTable.COLUMN_TRANS_ID)));
				reportDetail.setComputerId(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
				reportDetail.setTransStatus(cursor.getInt(cursor.getColumnIndex(OrderTransTable.COLUMN_STATUS_ID)));
				reportDetail.setReceiptNo(cursor.getString(cursor.getColumnIndex(OrderTransTable.COLUMN_RECEIPT_NO)));
				reportDetail.setTotalPrice(cursor.getDouble(cursor.getColumnIndex("TotalRetailPrice")));
				reportDetail.setSubTotal(cursor.getDouble(cursor.getColumnIndex("TotalSalePrice")));
				reportDetail.setVatExclude(cursor.getDouble(cursor.getColumnIndex(OrderTransTable.COLUMN_TRANS_EXCLUDE_VAT)));
				reportDetail.setDiscount(cursor.getDouble(cursor.getColumnIndex("TotalDiscount")));
				reportDetail.setVatable(cursor.getDouble(cursor.getColumnIndex(OrderTransTable.COLUMN_TRANS_VATABLE)));
				reportDetail.setTotalVat(cursor.getDouble(cursor.getColumnIndex(OrderTransTable.COLUMN_TRANS_VAT)));
				reportDetail.setTotalPayment(cursor.getDouble(cursor.getColumnIndex("TotalPayment")));
				reportDetail.setSendStatus(cursor.getInt(cursor.getColumnIndex(COLUMN_SEND_STATUS)));
				report.getReportDetail().add(reportDetail);
				
			}while(cursor.moveToNext());
		}
		return report;
	}
	
	public Report getProductDataReport() throws SQLException {
		Report report = new Report();
		try {
			createReportProductTmp();
			createProductDataTmp();

			// group : dept
			List<Report.GroupOfProduct> groupLst = listProductGroup();
			if (groupLst != null) {
				for (int i = 0; i < groupLst.size(); i++) {
					Report.GroupOfProduct group = groupLst.get(i);
					Report.GroupOfProduct groupSection = new Report.GroupOfProduct();
					groupSection.setProductDeptName(group.getProductDeptName());
					groupSection.setProductGroupName(group.getProductGroupName());

					// product
					String sql = "SELECT a." + ProductTable.COLUMN_PRODUCT_TYPE_ID + ", "
							+ " a." + ProductTable.COLUMN_PRODUCT_PRICE + ", "
							+ " a." + COLUMN_PRODUCT_QTY + ", "
							+ " a." + COLUMN_PRODUCT_QTY_PERCENT + ", " 
							+ " a." + COLUMN_PRODUCT_SUB_TOTAL + ", " 
							+ " a." + COLUMN_PRODUCT_SUB_TOTAL_PERCENT + ", " 
							+ " a." + COLUMN_PRODUCT_DISCOUNT + ", " 
							+ " a." + COLUMN_PRODUCT_TOTAL_PRICE + ", " 
							+ " a." + COLUMN_PRODUCT_TOTAL_PRICE_PERCENT + ", " 
							+ " b." + ProductTable.COLUMN_PRODUCT_CODE + ", " 
							+ " b." + ProductTable.COLUMN_PRODUCT_NAME + ", " 
							+ " b." + ProductTable.COLUMN_PRODUCT_NAME1 + ", " 
							+ " b." + ProductTable.COLUMN_PRODUCT_NAME2 + ", "
							+ " b." + ProductTable.COLUMN_VAT_TYPE 
							+ " FROM " + TEMP_PRODUCT_REPORT + " a " 
							+ " LEFT JOIN " + ProductTable.TABLE_PRODUCT + " b " 
							+ " ON a." + ProductTable.COLUMN_PRODUCT_ID + "=b." + ProductTable.COLUMN_PRODUCT_ID 
							+ " WHERE b." + ProductTable.COLUMN_PRODUCT_DEPT_ID + "=?"
							+ " ORDER BY b." + COLUMN_ORDERING;
					Cursor cursor = getReadableDatabase().rawQuery(
							sql, 
							new String[] { 
								String.valueOf(group.getProductDeptId()) 
							});

					if (cursor.moveToFirst()) {
						do {
							Report.ReportDetail detail = new Report.ReportDetail();
							int productTypeId = cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_TYPE_ID));
							String productName = cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME));
							String productName1 = cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME1));
							String productName2 = cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME2));
							if(productTypeId == Products.CHILD_OF_SET_HAVE_PRICE){
								productName += "***";
								productName1 += "***";
								productName2 += "***";
							}
							detail.setProductCode(cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_CODE)));
							detail.setProductName(productName);
							detail.setProductName1(productName1);
							detail.setProductName2(productName2);
							detail.setPricePerUnit(cursor.getDouble(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_PRICE)));
							detail.setQty(cursor.getDouble(cursor.getColumnIndex(COLUMN_PRODUCT_QTY)));
							detail.setQtyPercent(cursor.getDouble(cursor.getColumnIndex(COLUMN_PRODUCT_QTY_PERCENT)));
							detail.setSubTotal(cursor.getDouble(cursor.getColumnIndex(COLUMN_PRODUCT_SUB_TOTAL)));
							detail.setSubTotalPercent(cursor.getDouble(cursor.getColumnIndex(COLUMN_PRODUCT_SUB_TOTAL_PERCENT)));
							detail.setDiscount(cursor.getDouble(cursor.getColumnIndex(COLUMN_PRODUCT_DISCOUNT)));
							detail.setTotalPrice(cursor.getDouble(cursor.getColumnIndex(COLUMN_PRODUCT_TOTAL_PRICE)));
							detail.setTotalPricePercent(cursor.getDouble(cursor.getColumnIndex(COLUMN_PRODUCT_TOTAL_PRICE_PERCENT)));
							int vatType = cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_VAT_TYPE));
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
							detail.setVat(vatTypeText);
							groupSection.getReportDetail().add(detail);
						} while (cursor.moveToNext());
					}
					
					// dept summary
					groupSection.getReportDetail().add(getSummaryByDept(group.getProductDeptId()));
					if(i < groupLst.size() - 1){
						if(group.getProductGroupId() != groupLst.get(i + 1).getProductGroupId()){
							groupSection.getReportDetail().add(getSummaryByGroup(group.getProductGroupId()));
						}
					}else if(i == groupLst.size() - 1){
						groupSection.getReportDetail().add(getSummaryByGroup(group.getProductGroupId()));
					}
					report.getGroupOfProductLst().add(groupSection);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return report;
	}
	
	/**
	 * get group summary
	 * @param groupId
	 * @return Report.ReportDetail
	 */
	public Report.ReportDetail getSummaryByGroup(int groupId){
		Report.ReportDetail report = null;
		String transIds = getTransactionIds();
		String selection = " a." + OrderTransTable.COLUMN_TRANS_ID + " IN (" + transIds + ")"
				+ " AND a." + ProductTable.COLUMN_PRODUCT_TYPE_ID + " IN(?,?,?,?) "
				+ " AND c." + ProductTable.COLUMN_PRODUCT_GROUP_ID + "=?";
		String sql = " SELECT SUM(a." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS " + OrderDetailTable.COLUMN_ORDER_QTY + ", "
				+ " SUM(a." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS " + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", "
				+ " SUM(a." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") AS " + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ", "
				+ " SUM(a." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") AS " + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE
				+ " FROM " + OrderDetailTable.TABLE_ORDER + " a "
				+ " LEFT JOIN " + ProductTable.TABLE_PRODUCT + " b "
				+ " ON a." + ProductTable.COLUMN_PRODUCT_ID + "=b." + ProductTable.COLUMN_PRODUCT_ID
				+ " LEFT JOIN " + ProductDeptTable.TABLE_PRODUCT_DEPT + " c "
				+ " ON b." + ProductTable.COLUMN_PRODUCT_DEPT_ID + "=c." + ProductTable.COLUMN_PRODUCT_DEPT_ID
				+ " WHERE " + selection
				+ " GROUP BY c." + ProductTable.COLUMN_PRODUCT_GROUP_ID;
		Cursor cursor = getReadableDatabase().rawQuery(
				sql,
				new String[]{
					String.valueOf(Products.NORMAL_TYPE),
					String.valueOf(Products.SET_CAN_SELECT),
					String.valueOf(Products.CHILD_OF_SET_HAVE_PRICE),
					String.valueOf(Products.COMMENT_HAVE_PRICE),
					String.valueOf(groupId)
				});
		
		if(cursor.moveToFirst()){
			Report.ReportDetail summReport = getProductSummary();
			report = new Report.ReportDetail();
			report.setProductName(SUMM_GROUP);
			report.setQty(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_QTY)));
			report.setQtyPercent(report.getQty() / summReport.getQty() * 100);
			report.setSubTotal(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE)));
			report.setSubTotalPercent(report.getSubTotal() / summReport.getSubTotal() * 100);
			report.setDiscount(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT)));
			report.setTotalPrice(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE)));
			report.setTotalPricePercent(report.getTotalPrice() / summReport.getTotalPrice() * 100);
		}
		cursor.close();
		return report;
	}
	
	/**
	 * get dept summary
	 * @param deptId
	 * @return Report.ReportDetail
	 */
	public Report.ReportDetail getSummaryByDept(int deptId){
		Report.ReportDetail report = null;
		String transIds = getTransactionIds();
		String selection = " a." + OrderTransTable.COLUMN_TRANS_ID + " IN (" + transIds + ")"
				+ " AND a." + ProductTable.COLUMN_PRODUCT_TYPE_ID + " IN(?,?,?,?)"
				+ " AND b." + ProductTable.COLUMN_PRODUCT_DEPT_ID + "=?";
		String sql = " SELECT SUM(a." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS " + OrderDetailTable.COLUMN_ORDER_QTY + "," 
				+ " SUM(a." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS " + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", " 
				+ " SUM(a." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") AS " + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ", " 
				+ " SUM(a." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") AS " + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE
				+ " FROM " + OrderDetailTable.TABLE_ORDER + " a "
				+ " LEFT JOIN " + ProductTable.TABLE_PRODUCT + " b "
				+ " ON a." + ProductTable.COLUMN_PRODUCT_ID + "=b." + ProductTable.COLUMN_PRODUCT_ID
				+ " WHERE " + selection
				+ " GROUP BY b." + ProductTable.COLUMN_PRODUCT_DEPT_ID;
		Cursor cursor = getReadableDatabase().rawQuery(
				sql,
				new String[]{
					String.valueOf(Products.NORMAL_TYPE),
					String.valueOf(Products.SET_CAN_SELECT),
					String.valueOf(Products.CHILD_OF_SET_HAVE_PRICE),
					String.valueOf(Products.COMMENT_HAVE_PRICE),
					String.valueOf(deptId)
				});
		
		if(cursor.moveToFirst()){
			Report.ReportDetail summReport = getProductSummary();
			report = new Report.ReportDetail();
			report.setProductName(SUMM_DEPT);
			report.setQty(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_QTY)));
			report.setQtyPercent(report.getQty() / summReport.getQty() * 100);
			report.setSubTotal(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE)));
			report.setSubTotalPercent(report.getSubTotal() / summReport.getSubTotal() * 100);
			report.setDiscount(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT)));
			report.setTotalPrice(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE)));
			report.setTotalPricePercent(report.getTotalPrice() / summReport.getTotalPrice() * 100);
		}
		cursor.close();
		return report;
	}
	
	/**
	 * get product report summary
	 * @return Report.ReportDetail
	 */
	public Report.ReportDetail getProductSummary(){
		Report.ReportDetail report = new Report.ReportDetail();
		Cursor cursor = getReadableDatabase().query(
				TEMP_PRODUCT_REPORT, 
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

    /**
     * Get transactionIds where status success
     * @return
     */
	public String getTransactionIds(){
		String transIds = "";
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT " + OrderTransTable.COLUMN_TRANS_ID
				+ " FROM " + OrderTransTable.TABLE_ORDER_TRANS
				+ " WHERE " + OrderTransTable.COLUMN_SALE_DATE
				+ " BETWEEN ? AND ? "
				+ " AND " + OrderTransTable.COLUMN_STATUS_ID + " =? ",
				new String[]{
						mDateFrom,
						mDateTo,
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS)
				});
		if(cursor.moveToFirst()){
			do{
				transIds += cursor.getString(cursor.getColumnIndex(OrderTransTable.COLUMN_TRANS_ID));
				if(!cursor.isLast())
					transIds += ",";
			}while(cursor.moveToNext());
		}
		cursor.close();
		return transIds;
	}
	
	/**
	 * Create product report temp
	 * @throws SQLException
	 */
	private void createProductDataTmp() throws SQLException{
		String transIds = getTransactionIds();
		String selection = OrderTransTable.COLUMN_TRANS_ID + " IN (" + transIds + ") "
				+ " AND " + ProductTable.COLUMN_PRODUCT_TYPE_ID 
				+ " IN(" + Products.NORMAL_TYPE + "," + Products.SET_CAN_SELECT + "," + Products.CHILD_OF_SET_HAVE_PRICE + "," + Products.COMMENT_HAVE_PRICE + ") ";
		String sql = " SELECT " + OrderDetailTable.COLUMN_ORDER_ID + ", "
				+ ProductTable.COLUMN_PRODUCT_ID + ", "
				+ ProductTable.COLUMN_PRODUCT_TYPE_ID + ", "
				+ ProductTable.COLUMN_PRODUCT_PRICE + ", "
				+ " SUM(" + OrderDetailTable.COLUMN_ORDER_QTY + ") AS TotalQty, "
				+ " SUM(" + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS TotalRetailPrice, "
				+ " SUM(" + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") AS TotalDiscount, "
				+ " SUM(" + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") AS TotalSalePrice, "
				// total qty
				+ " (SELECT SUM(" + OrderDetailTable.COLUMN_ORDER_QTY + ") "
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + selection + ") AS SummTotalQty, "
				// total retail price
				+ " (SELECT SUM(" + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") "
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + selection + ") AS SummTotalRetailPrice, "
				// total discount
				+ " (SELECT SUM(" + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") "
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + selection + ") AS SummTotalDiscount, "
				// total sale price
				+ " (SELECT SUM(" + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") "
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + selection + ") AS SummTotalSalePrice " 
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + selection
				+ " GROUP BY " + ProductTable.COLUMN_PRODUCT_ID + ", "
				+ ProductTable.COLUMN_PRODUCT_TYPE_ID;
		Cursor cursor = getWritableDatabase().rawQuery(sql, null);
		
		if(cursor.moveToFirst()){
			do{
				int productId = cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID));
				int productTypeId = cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_TYPE_ID));
				double productPrice = cursor.getDouble(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_PRICE));
				double qty = cursor.getDouble(cursor.getColumnIndex("TotalQty"));
				double summQty = cursor.getDouble(cursor.getColumnIndex("SummTotalQty"));
				double qtyPercent = (qty / summQty) * 100;
				double retailPrice = cursor.getDouble(cursor.getColumnIndex("TotalRetailPrice"));
				double summRetailPrice = cursor.getDouble(cursor.getColumnIndex("SummTotalRetailPrice"));
				double retailPricePercent = (retailPrice / summRetailPrice) * 100;
				double discount = cursor.getDouble(cursor.getColumnIndex("TotalDiscount"));
				double summDiscount = cursor.getDouble(cursor.getColumnIndex("SummTotalDiscount"));
				double salePrice = cursor.getDouble(cursor.getColumnIndex("TotalSalePrice"));
				double summSalePrice = cursor.getDouble(cursor.getColumnIndex("SummTotalSalePrice"));
				double salePricePercent = (salePrice / summSalePrice) * 100;
				
				ContentValues cv = new ContentValues();
				cv.put(ProductTable.COLUMN_PRODUCT_ID, productId);
				cv.put(ProductTable.COLUMN_PRODUCT_TYPE_ID, productTypeId);
				cv.put(ProductTable.COLUMN_PRODUCT_PRICE, productPrice);
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
				
				getWritableDatabase().insertOrThrow(TEMP_PRODUCT_REPORT, null, cv);
				
			}while(cursor.moveToNext());
		}
		cursor.close();
	}

	/**
	 * list group
	 * @return List<Report.GroupOfProduct>
	 */
	public List<Report.GroupOfProduct> listProductGroup(){
		List<Report.GroupOfProduct> reportLst = new ArrayList<Report.GroupOfProduct>();
		String sql = " SELECT c." + ProductTable.COLUMN_PRODUCT_DEPT_ID + ", "
				+ " c." + ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME + ", " 
				+ " d." + ProductTable.COLUMN_PRODUCT_GROUP_ID + ", "
				+ " d." + ProductGroupTable.COLUMN_IS_COMMENT + ", "
				+ " d." + ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME
				+ " FROM " + TEMP_PRODUCT_REPORT + " a "
				+ " LEFT JOIN " + ProductTable.TABLE_PRODUCT + " b "
				+ " ON a." + ProductTable.COLUMN_PRODUCT_ID + "=b." + ProductTable.COLUMN_PRODUCT_ID
				+ " LEFT JOIN " + ProductDeptTable.TABLE_PRODUCT_DEPT + " c " 
				+ " ON b." + ProductTable.COLUMN_PRODUCT_DEPT_ID + "=c." + ProductTable.COLUMN_PRODUCT_DEPT_ID
				+ " LEFT JOIN " + ProductGroupTable.TABLE_PRODUCT_GROUP + " d "
				+ " ON c." + ProductTable.COLUMN_PRODUCT_GROUP_ID + "=d." + ProductTable.COLUMN_PRODUCT_GROUP_ID
				+ " GROUP BY d." + ProductTable.COLUMN_PRODUCT_GROUP_ID + ", " + " c." + ProductTable.COLUMN_PRODUCT_DEPT_ID
				+ " ORDER BY d." + COLUMN_ORDERING + ",d." + ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME + ", "
				+ " c." + COLUMN_ORDERING + ",c." + ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME + ", "
				+ " b." + COLUMN_ORDERING + ", b." + ProductTable.COLUMN_PRODUCT_NAME; 
		Cursor cursor = getReadableDatabase().rawQuery(sql, null);
		
		if(cursor.moveToFirst()){
			do{
				Report.GroupOfProduct report = new Report.GroupOfProduct();
				report.setProductDeptId(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_DEPT_ID)));
				report.setProductGroupId(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_GROUP_ID)));
				report.setIsComment(cursor.getInt(cursor.getColumnIndex(ProductGroupTable.COLUMN_IS_COMMENT)));
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
				ProductTable.COLUMN_PRODUCT_ID + " INTEGER DEFAULT 0, " +
				ProductTable.COLUMN_PRODUCT_TYPE_ID + " INTEGER DEFAULT 0, " +
				ProductTable.COLUMN_PRODUCT_PRICE + " REAL DEFAULT 0, " +
				COLUMN_PRODUCT_QTY + " REAL DEFAULT 0, " +
				COLUMN_PRODUCT_QTY_PERCENT + " REAL DEFAULT 0, " +
				COLUMN_PRODUCT_SUB_TOTAL + " REAL DEFAULT 0, " +
				COLUMN_PRODUCT_SUB_TOTAL_PERCENT + " REAL DEFAULT 0, " +
				COLUMN_PRODUCT_DISCOUNT + " REAL DEFAULT 0, " +
				COLUMN_PRODUCT_TOTAL_PRICE + " REAL DEFAULT 0, " +
				COLUMN_PRODUCT_TOTAL_PRICE_PERCENT + " REAL DEFAULT 0, " +
				COLUMN_PRODUCT_SUMM_QTY + " REAL DEFAULT 0, " +
				COLUMN_PRODUCT_SUMM_SUB_TOTAL + " REAL DEFAULT 0, " +
				COLUMN_PRODUCT_SUMM_DISCOUNT + " REAL DEFAULT 0, " +
				COLUMN_PRODUCT_SUMM_TOTAL_PRICE + " REAL DEFAULT 0);"); 
	}
	
	public static class SaleTransactionReport{
		private String saleDate;
		
		private List<OrderTransaction> transLst = new ArrayList<OrderTransaction>();
		public String getSaleDate() {
			return saleDate;
		}
		public void setSaleDate(String saleDate) {
			this.saleDate = saleDate;
		}
		public List<OrderTransaction> getTransLst() {
			return transLst;
		}
	}
	
	/**
	 * @author j1tth4
	 * Create for Summary Sale By Day
	 */
	public static class SimpleProductData{
		private String deptName;
		private double deptTotalQty;
		private double deptTotalPrice;
		private List<Item> itemLst = new ArrayList<Item>();
		
		public String getDeptName() {
			return deptName;
		}

		public void setDeptName(String deptName) {
			this.deptName = deptName;
		}

		public double getDeptTotalQty() {
			return deptTotalQty;
		}

		public void setDeptTotalQty(double deptTotalQty) {
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
			private String itemName1;
			private String itemName2;
			private String itemName3;
			private double totalQty;
			private double totalPrice;
			
			public String getItemName1() {
				return itemName1;
			}
			public void setItemName1(String itemName1) {
				this.itemName1 = itemName1;
			}
			public String getItemName2() {
				return itemName2;
			}
			public void setItemName2(String itemName2) {
				this.itemName2 = itemName2;
			}
			public String getItemName3() {
				return itemName3;
			}
			public void setItemName3(String itemName3) {
				this.itemName3 = itemName3;
			}
			public String getItemName() {
				return itemName;
			}
			public void setItemName(String itemName) {
				this.itemName = itemName;
			}
			public double getTotalQty() {
				return totalQty;
			}
			public void setTotalQty(double totalQty) {
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
