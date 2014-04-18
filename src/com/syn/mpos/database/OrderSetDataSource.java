package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class OrderSetDataSource extends MPOSDatabase{

	public OrderSetDataSource(SQLiteDatabase db) {
		super(db);
	}
	
	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @return
	 */
	public List<ProductsDataSource.ProductSet> listOrderSet(int transactionId, int orderDetailId){
		
		List<ProductsDataSource.ProductSet> productSetLst = 
				new ArrayList<ProductsDataSource.ProductSet>();
		
		Cursor mainCursor = mSqlite.rawQuery(
				" SELECT b." + ProductComponentTable.COLUMN_PGROUP_ID + ", "  
				+ " b." + ProductComponentGroupTable.COLUMN_SET_GROUP_NO + ", "
				+ " b." + ProductComponentGroupTable.COLUMN_SET_GROUP_NAME + ", "
				+ " b." + ProductComponentGroupTable.COLUMN_REQ_AMOUNT
				+ " FROM " + OrderSetTable.TABLE_NAME + " a "
				+ " LEFT JOIN " + ProductComponentGroupTable.TABLE_NAME + " b "
				+ " ON a." + ProductComponentTable.COLUMN_PGROUP_ID + "=" 
				+ " b." + ProductComponentTable.COLUMN_PGROUP_ID
				+ " WHERE a." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? "
				+ " AND a." + OrderDetailTable.COLUMN_ORDER_ID + "=?", 
				new String[]{
						String.valueOf(transactionId), 
						String.valueOf(orderDetailId)
				});
		
		if(mainCursor.moveToFirst()){
			do{
				int pcompGroupId = mainCursor.getInt(mainCursor.getColumnIndex(
						ProductComponentTable.COLUMN_PGROUP_ID));
				
				ProductsDataSource.ProductSet group = new ProductsDataSource.ProductSet();
				group.setTransactionId(transactionId);
				group.setOrderDetailId(orderDetailId);
				group.setProductGroupId(pcompGroupId);
				group.setGroupNo(mainCursor.getInt(mainCursor.getColumnIndex(
						ProductComponentGroupTable.COLUMN_SET_GROUP_NO)));
				group.setGroupName(mainCursor.getString(mainCursor.getColumnIndex(
						ProductComponentGroupTable.COLUMN_SET_GROUP_NAME)));
				group.setRequireAmount(mainCursor.getDouble(mainCursor.getColumnIndex(
						ProductComponentGroupTable.COLUMN_REQ_AMOUNT)));
				
				// query set detail
				Cursor detailCursor = mSqlite.query(OrderSetTable.TABLE_NAME, 
						new String[] {
							OrderSetTable.COLUMN_ORDER_SET_ID,
							ProductsTable.COLUMN_PRODUCT_ID,
							ProductsTable.COLUMN_PRODUCT_NAME,
							OrderSetTable.COLUMN_ORDER_SET_QTY
						}, 
						OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? "
						+ " AND " + OrderDetailTable.COLUMN_ORDER_ID + "=? "
						+ " AND " + ProductComponentTable.COLUMN_PGROUP_ID + "=?",
						new String[]{
							String.valueOf(transactionId), 
							String.valueOf(orderDetailId), 
							String.valueOf(pcompGroupId)
						},null, null, null);
				
				if(detailCursor.moveToFirst()){
					do{
						ProductsDataSource.ProductSet.ProductSetDetail detail = 
								new ProductsDataSource.ProductSet.ProductSetDetail();
						detail.setOrderSetId(detailCursor.getInt(detailCursor.getColumnIndex(
								OrderSetTable.COLUMN_ORDER_SET_ID)));
						detail.setProductId(detailCursor.getInt(detailCursor.getColumnIndex(
								ProductsTable.COLUMN_PRODUCT_ID)));
						detail.setProductName(detailCursor.getString(detailCursor.getColumnIndex(
								ProductsTable.COLUMN_PRODUCT_NAME)));
						detail.setOrderSetQty(detailCursor.getDouble(detailCursor.getColumnIndex(
								OrderSetTable.COLUMN_ORDER_SET_QTY)));
						group.mProductLst.add(detail);
					}while(detailCursor.moveToNext());
				}
				detailCursor.close();
				
				// productSet to list
				productSetLst.add(group);
				
			}while(mainCursor.moveToNext());
		}
		mainCursor.close();
		
		return productSetLst;
	}
	
	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param pcompGroupId
	 * @return
	 */
	public double getTotalQty(int transactionId, int orderDetailId, int pcompGroupId){
		double totalQty = 0;
		
		Cursor cursor = mSqlite.rawQuery(
				" SELECT SUM(" + OrderSetTable.COLUMN_ORDER_SET_QTY + ") "
				+ " FROM " + OrderSetTable.TABLE_NAME
				+ " WHERE " + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? "
				+ " AND " + OrderDetailTable.COLUMN_ORDER_ID + "=? "
				+ " AND " + ProductComponentTable.COLUMN_PGROUP_ID + "=? ", 
				new String[]{
						String.valueOf(transactionId),
						String.valueOf(orderDetailId),
						String.valueOf(pcompGroupId)
				});
		
		if(cursor.moveToFirst()){
			totalQty = cursor.getDouble(0);
		}
		cursor.close();
		return totalQty;
	}
	
	/**
	 * @param transactionId
	 * @param orderDetailId
	 */
	public void deleteOrderSet(int transactionId, int orderDetailId){
		mSqlite.delete(OrderSetTable.TABLE_NAME, 
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? "
				+ " AND " + OrderDetailTable.COLUMN_ORDER_ID + "=? ", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(orderDetailId)
				});
	}
	
	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param orderSetId
	 */
	public void deleteOrderSet(int transactionId, int orderDetailId, int orderSetId){
		mSqlite.delete(OrderSetTable.TABLE_NAME, 
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? "
				+ " AND " + OrderDetailTable.COLUMN_ORDER_ID + "=? "
				+ " AND " + OrderSetTable.COLUMN_ORDER_SET_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(orderDetailId), 
					String.valueOf(orderSetId)
				});
	}
	
	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param orderSetId
	 * @param productId
	 * @param orderSetQty
	 */
	public void updateOrderSet(int transactionId, int orderDetailId, int orderSetId,
			int productId, double orderSetQty){
		
		ContentValues cv = new ContentValues();
		cv.put(OrderSetTable.COLUMN_ORDER_SET_QTY, orderSetQty);
		
		mSqlite.update(OrderSetTable.TABLE_NAME, cv, 
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? "
						+ " AND " + OrderDetailTable.COLUMN_ORDER_ID + "=? "
								+ " AND " + OrderSetTable.COLUMN_ORDER_SET_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(orderDetailId), 
					String.valueOf(orderSetId)
				});
	}
	
	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param productId
	 * @param productName
	 * @param pcompGroupId
	 * @param reqAmount
	 */
	public void addOrderSet(int transactionId, int orderDetailId, int productId, 
			String productName, int pcompGroupId, double reqAmount){
		
		int maxOrderSetId = getMaxOrderSetId(transactionId, orderDetailId);
		
		ContentValues cv = new ContentValues();
		cv.put(OrderSetTable.COLUMN_ORDER_SET_ID, maxOrderSetId);
		cv.put(OrderTransactionTable.COLUMN_TRANSACTION_ID, transactionId);
		cv.put(OrderDetailTable.COLUMN_ORDER_ID, orderDetailId);
		cv.put(ProductsTable.COLUMN_PRODUCT_ID, productId);
		cv.put(ProductsTable.COLUMN_PRODUCT_NAME, productName);
		cv.put(ProductComponentTable.COLUMN_PGROUP_ID, pcompGroupId);
		cv.put(ProductComponentGroupTable.COLUMN_REQ_AMOUNT, reqAmount);
		cv.put(OrderSetTable.COLUMN_ORDER_SET_QTY, 1);
		
		mSqlite.insertOrThrow(OrderSetTable.TABLE_NAME, ProductsTable.COLUMN_PRODUCT_NAME, cv);
	}
	
	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @return
	 */
	public int getMaxOrderSetId(int transactionId, int orderDetailId){
		int maxOrderSetId = 0;
		
		Cursor cursor = mSqlite.rawQuery(
				"SELECT MAX (" + OrderSetTable.COLUMN_ORDER_SET_ID + ")"
						+ " FROM " + OrderSetTable.TABLE_NAME 
						+ " WHERE " + OrderTransactionTable.COLUMN_TRANSACTION_ID 
						+ " =? AND " + OrderDetailTable.COLUMN_ORDER_ID + "=?", 
						new String[]{String.valueOf(transactionId), String.valueOf(orderDetailId)});
		if(cursor.moveToFirst()){
			maxOrderSetId = cursor.getInt(0);
		}
		cursor.close();
		return maxOrderSetId + 1;
	}
}
