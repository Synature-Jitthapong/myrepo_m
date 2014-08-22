package com.synature.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.synature.mpos.database.table.ComputerTable;
import com.synature.mpos.database.table.MenuCommentTable;
import com.synature.mpos.database.table.OrderDetailTable;
import com.synature.mpos.database.table.OrderTransactionTable;
import com.synature.mpos.database.table.PaymentDetailTable;
import com.synature.mpos.database.table.ProductDeptTable;
import com.synature.mpos.database.table.ProductGroupTable;
import com.synature.mpos.database.table.ProductTable;
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
				"SELECT " + OrderTransactionTable.COLUMN_SALE_DATE
				+ " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS
				+ " WHERE " + OrderTransactionTable.COLUMN_SALE_DATE 
				+ " BETWEEN ? AND ?"
				+ " AND " + OrderTransactionTable.COLUMN_STATUS_ID
				+ " IN (?,?)"
				+ " GROUP BY " + OrderTransactionTable.COLUMN_SALE_DATE, 
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
						"SELECT " + OrderTransactionTable.COLUMN_RECEIPT_NO + ", "
						+ OrderTransactionTable.COLUMN_CLOSE_TIME + ", "
						+ OrderTransactionTable.COLUMN_TRANS_VATABLE
						+ " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS
						+ " WHERE " + OrderTransactionTable.COLUMN_SALE_DATE + "=?" 
						+ " AND " + OrderTransactionTable.COLUMN_STATUS_ID + " IN(?,?)"
						+ " GROUP BY " + OrderTransactionTable.COLUMN_TRANS_ID, 
						new String[]{
								mainCursor.getString(0),
								String.valueOf(Transaction.TRANS_STATUS_HOLD),
								String.valueOf(Transaction.TRANS_STATUS_SUCCESS)
						});
				if(detailCursor.moveToFirst()){
					do{
						MPOSOrderTransaction detail = new MPOSOrderTransaction();
						detail.setReceiptNo(detailCursor.getString(
								detailCursor.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_NO)));
						detail.setCloseTime(detailCursor.getString(
								detailCursor.getColumnIndex(OrderTransactionTable.COLUMN_CLOSE_TIME)));
						detail.setTransactionVatable(detailCursor.getDouble(
								detailCursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_VATABLE)));
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
	public List<SimpleProductData> listSummaryProductGroupInDay(){
		List<SimpleProductData> simpleLst = new ArrayList<SimpleProductData>();
		
		createReportProductTmp();
		createProductDataTmp();
		
		Cursor groupCursor = getReadableDatabase().rawQuery(
				" SELECT SUM(a." + COLUMN_PRODUCT_QTY + ") AS " + COLUMN_PRODUCT_QTY + ", "
				+ " SUM(a." + COLUMN_PRODUCT_SUB_TOTAL + ") AS " + COLUMN_PRODUCT_SUB_TOTAL + ", "
				+ " d." + ProductTable.COLUMN_PRODUCT_GROUP_ID + ", "
				+ " d." + ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME
				+ " FROM " + TEMP_PRODUCT_REPORT + " a "
				+ " INNER JOIN " + ProductTable.TABLE_PRODUCT + " b "
				+ " ON a." + ProductTable.COLUMN_PRODUCT_ID + "=b." + ProductTable.COLUMN_PRODUCT_ID 
				+ " INNER JOIN " + ProductDeptTable.TABLE_PRODUCT_DEPT + " c "
				+ " ON b." + ProductTable.COLUMN_PRODUCT_DEPT_ID + "=c." + ProductTable.COLUMN_PRODUCT_DEPT_ID
				+ " INNER JOIN " + ProductGroupTable.TABLE_PRODUCT_GROUP + " d "
				+ " ON c." + ProductTable.COLUMN_PRODUCT_GROUP_ID + "=d." + ProductTable.COLUMN_PRODUCT_GROUP_ID
				+ " GROUP BY d." + ProductTable.COLUMN_PRODUCT_GROUP_ID
				+ " ORDER BY d." + COLUMN_ORDERING, null);
		
		if(groupCursor.moveToFirst()){
			do{
				SimpleProductData sp = new SimpleProductData();
				sp.setDeptName(groupCursor.getString(groupCursor.getColumnIndex(ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME)));
				sp.setDeptTotalQty(groupCursor.getInt(groupCursor.getColumnIndex(COLUMN_PRODUCT_QTY)));
				sp.setDeptTotalPrice(groupCursor.getDouble(groupCursor.getColumnIndex(COLUMN_PRODUCT_SUB_TOTAL)));
				
				Cursor cursor = getReadableDatabase().rawQuery(
						//proSql,
						"SELECT a." + ProductTable.COLUMN_PRODUCT_TYPE_ID + ", "
						+ " a." + COLUMN_PRODUCT_QTY + ", "
						+ " a." + COLUMN_PRODUCT_SUB_TOTAL + ", "
						+ " b." + ProductTable.COLUMN_PRODUCT_NAME + ", "
						+ " b." + ProductTable.COLUMN_PRODUCT_NAME1 + ", "
						+ " b." + ProductTable.COLUMN_PRODUCT_NAME2 + ", "
						+ " b." + ProductTable.COLUMN_PRODUCT_NAME3
						+ " FROM " + TEMP_PRODUCT_REPORT + " a "
						+ " INNER JOIN " + ProductTable.TABLE_PRODUCT + " b " 
						+ " ON a." + ProductTable.COLUMN_PRODUCT_ID + "=b." + ProductTable.COLUMN_PRODUCT_ID 
						+ " INNER JOIN " + ProductDeptTable.TABLE_PRODUCT_DEPT + " c "
						+ " ON b." + ProductTable.COLUMN_PRODUCT_DEPT_ID + " =c." + ProductTable.COLUMN_PRODUCT_DEPT_ID
						+ " WHERE c." + ProductTable.COLUMN_PRODUCT_GROUP_ID + "=?"
						+ " ORDER BY b." + COLUMN_ORDERING,
						new String[] { 
								String.valueOf(groupCursor.getInt(
										groupCursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_GROUP_ID))) 
						});
				if(cursor.moveToFirst()){
					do{
						SimpleProductData.Item item = new SimpleProductData.Item();
						int productTypeId = cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_TYPE_ID));
						String itemName = cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME));
						String itemName1 = cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME1));
						String itemName2 = cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME2));
						String itemName3 = cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME3));
						if(productTypeId == Products.CHILD_OF_SET_HAVE_PRICE){
							itemName += "***";
							itemName1 += "***";
							itemName2 += "***";
							itemName3 += "***";
						}
						item.setItemName(itemName);
						item.setItemName1(itemName1);
						item.setItemName2(itemName2);
						item.setItemName3(itemName3);
						item.setTotalQty(cursor.getDouble(cursor.getColumnIndex(COLUMN_PRODUCT_QTY)));
						item.setTotalPrice(cursor.getDouble(cursor.getColumnIndex(COLUMN_PRODUCT_SUB_TOTAL)));
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
				+ " SUM(" + OrderTransactionTable.COLUMN_TRANS_VATABLE + ") AS TransVatable, "
				+ " SUM(" + OrderTransactionTable.COLUMN_TRANS_VAT + ") AS TransVat, "
				+ " SUM(" + OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT + ") AS TransExcludeVat, "
				// total retail price
				+ " (SELECT SUM(" + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") "
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + OrderTransactionTable.COLUMN_TRANS_ID + " IN (" + transIds + ")) AS SummTotalRetailPrice, "
				// total discount
				+ " (SELECT SUM(" + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") "
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + OrderTransactionTable.COLUMN_TRANS_ID + " IN (" + transIds + ")) AS SummTotalDiscount, " 
				// total sale price
				+ " (SELECT SUM(" + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + " + "
				+ OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE + ") "
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + OrderTransactionTable.COLUMN_TRANS_ID + " IN (" + transIds + ")) AS SummTotalSalePrice, "
				// total payment
				+ " (SELECT SUM(" + PaymentDetailTable.COLUMN_PAY_AMOUNT +  ") "
				+ " FROM " + PaymentDetailTable.TABLE_PAYMENT_DETAIL
				+ " WHERE " + OrderTransactionTable.COLUMN_TRANS_ID + " IN (" + transIds + ")) AS SummTotalPayment "
				+ " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS 
				+ " WHERE " + OrderTransactionTable.COLUMN_TRANS_ID + " IN (" + transIds + ")"
				+ " AND " + OrderTransactionTable.COLUMN_STATUS_ID + " =?";
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
	
	public Report getSaleReportByBill(){
		Report report = new Report();
		String strSql = 
				" SELECT a." + OrderTransactionTable.COLUMN_TRANS_ID + ", "
				+ " a." + ComputerTable.COLUMN_COMPUTER_ID + ", " 
				+ " a." + OrderTransactionTable.COLUMN_STATUS_ID + ", " 
				+ " a." + OrderTransactionTable.COLUMN_RECEIPT_NO + "," 
				+ " a." + OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT + ", " 
				+ " a." + OrderTransactionTable.COLUMN_TRANS_VAT + ", " 
				+ " a." + OrderTransactionTable.COLUMN_TRANS_VATABLE + ", " 
				+ " a." + COLUMN_SEND_STATUS + ", " 
				+ " SUM(b." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS TotalRetailPrice, "
				+ " SUM(b." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") AS TotalSalePrice, " 
				+ " a." + OrderTransactionTable.COLUMN_OTHER_DISCOUNT + " + "
				+ " SUM(b." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + " + "
				+ " b." + OrderDetailTable.COLUMN_MEMBER_DISCOUNT + ") AS TotalDiscount, "
				+ " (SELECT SUM(" + PaymentDetailTable.COLUMN_PAY_AMOUNT + ") " 
				+ " FROM " + PaymentDetailTable.TABLE_PAYMENT_DETAIL 
				+ " WHERE " + OrderTransactionTable.COLUMN_TRANS_ID + " =a." + OrderTransactionTable.COLUMN_TRANS_ID + ") AS TotalPayment "
				+ " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS + " a "
				+ " INNER JOIN " + OrderDetailTable.TABLE_ORDER + " b "
				+ " ON a." + OrderTransactionTable.COLUMN_TRANS_ID + "=b." + OrderTransactionTable.COLUMN_TRANS_ID
				+ " WHERE a." + OrderTransactionTable.COLUMN_STATUS_ID + " IN(?, ?) "
				+ " AND a." + OrderTransactionTable.COLUMN_SALE_DATE
				+ " BETWEEN ? AND ? " 
				+ " GROUP BY a." + OrderTransactionTable.COLUMN_TRANS_ID;

		Cursor cursor = getReadableDatabase().rawQuery(strSql, 
				new String[]{
				String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
				String.valueOf(Transaction.TRANS_STATUS_VOID),
				String.valueOf(mDateFrom), 
				String.valueOf(mDateTo)});
		
		if(cursor.moveToFirst()){
			do{
				Report.ReportDetail reportDetail = 
						new Report.ReportDetail();
				reportDetail.setTransactionId(cursor.getInt(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_ID)));
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
							+ " b." + ProductTable.COLUMN_PRODUCT_NAME3 + ", " 
							+ " b." + ProductTable.COLUMN_PRODUCT_PRICE + ", " 
							+ " b." + ProductTable.COLUMN_VAT_TYPE 
							+ " FROM " + TEMP_PRODUCT_REPORT + " a " 
							+ " INNER JOIN " + ProductTable.TABLE_PRODUCT + " b " 
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
							String productName3 = cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME3));
							if(productTypeId == Products.CHILD_OF_SET_HAVE_PRICE){
								productName += "***";
								productName1 += "***";
								productName2 += "***";
								productName3 += "***";
							}
							detail.setProductCode(cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_CODE)));
							detail.setProductName(productName);
							detail.setProductName1(productName1);
							detail.setProductName2(productName2);
							detail.setProductName3(productName3);
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
	
	public Report.ReportDetail getSummaryByGroup(int groupId){
		Report.ReportDetail report = null;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT SUM(o." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS TotalQty, " +
				" SUM(o." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS TotalRetailPrice, " +
				" SUM(o." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") AS TotalDiscount, " +
				" SUM(o." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") AS TotalSalePrice " +
				" FROM " + OrderTransactionTable.TABLE_ORDER_TRANS + " t " + 
				" INNER JOIN " + OrderDetailTable.TABLE_ORDER + " o " +
				" ON t." + OrderTransactionTable.COLUMN_TRANS_ID + "=o." + OrderTransactionTable.COLUMN_TRANS_ID +
				" INNER JOIN " + ProductTable.TABLE_PRODUCT + " p " +
				" ON o." + ProductTable.COLUMN_PRODUCT_ID + "=p." + ProductTable.COLUMN_PRODUCT_ID +
				" INNER JOIN " + ProductDeptTable.TABLE_PRODUCT_DEPT + " pd " +
				" ON p." + ProductTable.COLUMN_PRODUCT_DEPT_ID + "=pd." + ProductTable.COLUMN_PRODUCT_DEPT_ID +
				" WHERE t." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ?"  +
				" AND t." + OrderTransactionTable.COLUMN_STATUS_ID + " =?" +
				" AND pd." + ProductTable.COLUMN_PRODUCT_GROUP_ID + "=?" +
				" GROUP BY pd." + ProductTable.COLUMN_PRODUCT_GROUP_ID,
				new String[]{
						String.valueOf(mDateFrom),
						String.valueOf(mDateTo),
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
						String.valueOf(groupId)
				});
		
		if(cursor.moveToFirst()){
			Report.ReportDetail summReport = getProductSummary();
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
		String transIds = getTransactionIds();
		Report.ReportDetail report = null;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT SUM(a." + OrderDetailTable.COLUMN_ORDER_QTY + ")"
				+ " AS " + OrderDetailTable.COLUMN_ORDER_QTY + "," 
				+ " SUM(a." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") "
				+ " AS " + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ", " 
				+ " SUM(a." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") "
				+ " AS " + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ", " 
				+ " SUM(a." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") "
				+ " AS " + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ","
				+ "(SELECT SUM(s." + OrderDetailTable.COLUMN_ORDER_SET_PRICE + " * s." + OrderDetailTable.COLUMN_ORDER_SET_QTY + ") "
				+ "FROM " + OrderDetailTable.TABLE_ORDER + " s "
				+ "LEFT JOIN " + ProductTable.TABLE_PRODUCT + " p "
				+ " ON s." + ProductTable.COLUMN_PRODUCT_ID + "=p." + ProductTable.COLUMN_PRODUCT_ID
				+ " WHERE s." + OrderTransactionTable.COLUMN_TRANS_ID + " IN (" + transIds + ") "
				+ " AND p." + ProductTable.COLUMN_PRODUCT_DEPT_ID + "=?) AS TotalSetPrice,"
				+ "(SELECT SUM(c." + OrderDetailTable.COLUMN_ORDER_COMMENT_PRICE + " * c." + OrderDetailTable.COLUMN_ORDER_COMMENT_QTY + ") "
				+ "FROM " + OrderDetailTable.TABLE_ORDER + " c "
				+ "LEFT JOIN " + ProductTable.TABLE_PRODUCT + " p "
				+ " ON c." + MenuCommentTable.COLUMN_COMMENT_ID + "=p." + ProductTable.COLUMN_PRODUCT_ID
				+ " WHERE c." + OrderTransactionTable.COLUMN_TRANS_ID + " IN (" + transIds + " )"
				+ " AND p." + ProductTable.COLUMN_PRODUCT_DEPT_ID + "=?) AS TotalCommentPrice"
				+ " FROM " + OrderDetailTable.TABLE_ORDER + " a "
				+ " LEFT JOIN " + ProductTable.TABLE_PRODUCT + " b " 
				+ " ON a." + ProductTable.COLUMN_PRODUCT_ID + "=b." + ProductTable.COLUMN_PRODUCT_ID
				+ " WHERE a." + OrderTransactionTable.COLUMN_TRANS_ID + " IN (" + transIds + ")"
				+ " AND b." + ProductTable.COLUMN_PRODUCT_DEPT_ID + "=?"
				+ " GROUP BY b." + ProductTable.COLUMN_PRODUCT_DEPT_ID,
				new String[]{
						String.valueOf(deptId),
						String.valueOf(deptId),
						String.valueOf(deptId)
				});
		
		if(cursor.moveToFirst()){
			double totalSetPrice = cursor.getDouble(cursor.getColumnIndex("TotalSetPrice"));
			double totalCommentPrice = cursor.getDouble(cursor.getColumnIndex("TotalCommentPrice"));
			Report.ReportDetail summReport = getProductSummary();
			report = new Report.ReportDetail();
			report.setProductName(SUMM_DEPT);
			report.setQty(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_QTY)));
			report.setQtyPercent(report.getQty() / summReport.getQty() * 100);
			report.setSubTotal(totalSetPrice + totalCommentPrice + 
					cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE)));
			report.setSubTotalPercent(report.getSubTotal() / summReport.getSubTotal() * 100);
			report.setDiscount(cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_PRICE_DISCOUNT)));
			report.setTotalPrice(totalSetPrice + totalCommentPrice +
					cursor.getDouble(cursor.getColumnIndex(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE)));
			report.setTotalPricePercent(report.getTotalPrice() / summReport.getTotalPrice() * 100);
		}
		cursor.close();
		return report;
	}
	
	public Report.ReportDetail getProductSummary(){
		Report.ReportDetail report = new Report.ReportDetail();
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
	
	public String getTransactionIds(){
		String transIds = "";
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT " + OrderTransactionTable.COLUMN_TRANS_ID
				+ " FROM " + OrderTransactionTable.TABLE_ORDER_TRANS
				+ " WHERE " + OrderTransactionTable.COLUMN_SALE_DATE
				+ " BETWEEN ? AND ? "
				+ " AND " + OrderTransactionTable.COLUMN_STATUS_ID + " =? ", 
				new String[]{
						mDateFrom,
						mDateTo,
						String.valueOf(Transaction.TRANS_STATUS_SUCCESS)
				});
		if(cursor.moveToFirst()){
			do{
				transIds += cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_ID));
				if(!cursor.isLast())
					transIds += ",";
			}while(cursor.moveToNext());
		}
		cursor.close();
		return transIds;
	}
	
	private void createProductDataTmp() throws SQLException{
		String transIds = getTransactionIds();
		Cursor cursor = getWritableDatabase().rawQuery(
				" SELECT " + OrderDetailTable.COLUMN_ORDER_ID + ", "
				+ ProductTable.COLUMN_PRODUCT_ID + ", "
				+ ProductTable.COLUMN_PRODUCT_TYPE_ID + ", "
				+ " SUM(" + OrderDetailTable.COLUMN_ORDER_QTY + ") AS TotalQty, "
				+ " SUM(" + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") AS TotalRetailPrice, "
				+ " SUM(" + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") AS TotalDiscount, "
				+ " SUM(" + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") AS TotalSalePrice, "
				// total qty
				+ " (SELECT SUM(" + OrderDetailTable.COLUMN_ORDER_QTY + ") "
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + OrderTransactionTable.COLUMN_TRANS_ID
				+ " IN (" + transIds + ")) AS SummTotalQty, "
				// total retail price
				+ " (SELECT SUM(" + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + ") "
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + OrderTransactionTable.COLUMN_TRANS_ID 
				+ " IN (" + transIds + ")) AS SummTotalRetailPrice, "
				// total discount
				+ " (SELECT SUM(" + OrderDetailTable.COLUMN_PRICE_DISCOUNT + ") "
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + OrderTransactionTable.COLUMN_TRANS_ID 
				+ " IN (" + transIds + ")) AS SummTotalDiscount, "
				// total sale price
				+ " (SELECT SUM(" + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + ") "
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + OrderTransactionTable.COLUMN_TRANS_ID 
				+ " IN (" + transIds + ")) AS SummTotalSalePrice " 
				+ " FROM " + OrderDetailTable.TABLE_ORDER
				+ " WHERE " + OrderTransactionTable.COLUMN_TRANS_ID
				+ " IN (" + transIds + ")"
				+ " GROUP BY " + ProductTable.COLUMN_PRODUCT_ID + ", "
				+ ProductTable.COLUMN_PRODUCT_TYPE_ID, null);
		
		if(cursor.moveToFirst()){
			do{
				int productId = cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID));
				int productTypeId = cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_TYPE_ID));
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

	public List<Report.GroupOfProduct> listProductGroup(){
		List<Report.GroupOfProduct> reportLst = new ArrayList<Report.GroupOfProduct>();
		
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT c." + ProductTable.COLUMN_PRODUCT_DEPT_ID + ", "
						+ " c." + ProductDeptTable.COLUMN_PRODUCT_DEPT_NAME + ", " 
						+ " d." + ProductTable.COLUMN_PRODUCT_GROUP_ID + ", "
						+ " d." + ProductGroupTable.COLUMN_IS_COMMENT + ", "
						+ " d." + ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME
						+ " FROM " + TEMP_PRODUCT_REPORT + " a "
						+ " INNER JOIN " + ProductTable.TABLE_PRODUCT + " b "
						+ " ON a." + ProductTable.COLUMN_PRODUCT_ID + "=b." + ProductTable.COLUMN_PRODUCT_ID 
						+ " INNER JOIN " + ProductDeptTable.TABLE_PRODUCT_DEPT + " c "
						+ " ON b." + ProductTable.COLUMN_PRODUCT_DEPT_ID + "=c." + ProductTable.COLUMN_PRODUCT_DEPT_ID
						+ " INNER JOIN " + ProductGroupTable.TABLE_PRODUCT_GROUP + " d "
						+ " ON c." + ProductTable.COLUMN_PRODUCT_GROUP_ID + "=d." + ProductTable.COLUMN_PRODUCT_GROUP_ID
						+ " GROUP BY d." + ProductTable.COLUMN_PRODUCT_GROUP_ID + ", " 
						+ " c." + ProductTable.COLUMN_PRODUCT_DEPT_ID
						+ " ORDER BY d." + COLUMN_ORDERING + "," + " c." + COLUMN_ORDERING, null);
		
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
				ProductTable.COLUMN_PRODUCT_ID + " INTEGER NOT NULL DEFAULT 0, " +
				ProductTable.COLUMN_PRODUCT_TYPE_ID + " INTEGER DEFAULT 0, " +
				COLUMN_PRODUCT_QTY + " REAL NOT NULL DEFAULT 0, " +
				COLUMN_PRODUCT_QTY_PERCENT + " REAL NOT NULL DEFAULT 0, " +
				COLUMN_PRODUCT_SUB_TOTAL + " REAL NOT NULL DEFAULT 0, " +
				COLUMN_PRODUCT_SUB_TOTAL_PERCENT + " REAL NOT NULL DEFAULT 0, " +
				COLUMN_PRODUCT_DISCOUNT + " REAL NOT NULL DEFAULT 0, " +
				COLUMN_PRODUCT_TOTAL_PRICE + " REAL NOT NULL DEFAULT 0, " +
				COLUMN_PRODUCT_TOTAL_PRICE_PERCENT + " REAL NOT NULL DEFAULT 0, " +
				COLUMN_PRODUCT_SUMM_QTY + " REAL NOT NULL DEFAULT 0, " +
				COLUMN_PRODUCT_SUMM_SUB_TOTAL + " REAL NOT NULL DEFAULT 0, " +
				COLUMN_PRODUCT_SUMM_DISCOUNT + " REAL NOT NULL DEFAULT 0, " +
				COLUMN_PRODUCT_SUMM_TOTAL_PRICE + " REAL NOT NULL DEFAULT 0);"); 
	}
	
	public static class SaleTransactionReport{
		private String saleDate;
		
		private List<MPOSOrderTransaction> transLst = 
				new ArrayList<MPOSOrderTransaction>();
		public String getSaleDate() {
			return saleDate;
		}
		public void setSaleDate(String saleDate) {
			this.saleDate = saleDate;
		}
		public List<MPOSOrderTransaction> getTransLst() {
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
