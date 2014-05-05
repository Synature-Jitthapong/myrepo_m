package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.database.MPOSOrderTransaction.OrderSet;
import com.syn.mpos.database.table.OrderDetailTable;
import com.syn.mpos.database.table.OrderSetTable;
import com.syn.mpos.database.table.OrderTransactionTable;
import com.syn.mpos.database.table.ProductComponentGroupTable;
import com.syn.mpos.database.table.ProductComponentTable;
import com.syn.mpos.database.table.ProductsTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class OrderSetDataSource extends MPOSDatabase{

	public OrderSetDataSource(Context context) {
		super(context);
	}
	
	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @return List<MPOSOrderTransaction.OrderSet> 
	 */
	public List<MPOSOrderTransaction.OrderSet> listOrderSet(int transactionId, int orderDetailId){
		
		List<MPOSOrderTransaction.OrderSet> productSetLst = 
				new ArrayList<MPOSOrderTransaction.OrderSet>();
		
		Cursor mainCursor = getReadableDatabase().rawQuery(
				" SELECT b." + ProductComponentTable.COLUMN_PGROUP_ID + ", "  
				+ " b." + ProductComponentGroupTable.COLUMN_SET_GROUP_NO + ", "
				+ " b." + ProductComponentGroupTable.COLUMN_SET_GROUP_NAME + ", "
				+ " b." + ProductComponentGroupTable.COLUMN_REQ_AMOUNT
				+ " FROM " + OrderSetTable.TABLE_ORDER_SET + " a "
				+ " LEFT JOIN " + ProductComponentGroupTable.TABLE_PCOMPONENT_GROUP + " b "
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
				
				MPOSOrderTransaction.OrderSet group = new MPOSOrderTransaction.OrderSet();
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
				Cursor detailCursor = getReadableDatabase().query(OrderSetTable.TABLE_ORDER_SET, 
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
						MPOSOrderTransaction.OrderSet.OrderSetDetail detail = 
								new MPOSOrderTransaction.OrderSet.OrderSetDetail();
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
	 * List order set detail
	 * @param transactionId
	 * @param orderDetailId
	 * @return List<OrderSet.OrderSetDetail
	 */
	public List<OrderSet.OrderSetDetail> listOrderSetDetail(int transactionId, int orderDetailId){
		List<OrderSet.OrderSetDetail> orderSetDetailLst = 
				new ArrayList<OrderSet.OrderSetDetail>();
		// query set detail
		Cursor detailCursor = getReadableDatabase().query(OrderSetTable.TABLE_ORDER_SET, 
				new String[] {
					OrderSetTable.COLUMN_ORDER_SET_ID,
					ProductsTable.COLUMN_PRODUCT_ID,
					ProductsTable.COLUMN_PRODUCT_NAME,
					OrderSetTable.COLUMN_ORDER_SET_QTY
				}, 
				OrderTransactionTable.COLUMN_TRANSACTION_ID + "=? "
				+ " AND " + OrderDetailTable.COLUMN_ORDER_ID + "=? ",
				new String[]{
					String.valueOf(transactionId), 
					String.valueOf(orderDetailId)
				},null, null, null);
		
		if(detailCursor.moveToFirst()){
			do{
				MPOSOrderTransaction.OrderSet.OrderSetDetail detail = 
						new MPOSOrderTransaction.OrderSet.OrderSetDetail();
				detail.setOrderSetId(detailCursor.getInt(detailCursor.getColumnIndex(
						OrderSetTable.COLUMN_ORDER_SET_ID)));
				detail.setProductId(detailCursor.getInt(detailCursor.getColumnIndex(
						ProductsTable.COLUMN_PRODUCT_ID)));
				detail.setProductName(detailCursor.getString(detailCursor.getColumnIndex(
						ProductsTable.COLUMN_PRODUCT_NAME)));
				detail.setOrderSetQty(detailCursor.getDouble(detailCursor.getColumnIndex(
						OrderSetTable.COLUMN_ORDER_SET_QTY)));
				orderSetDetailLst.add(detail);
			}while(detailCursor.moveToNext());
		}
		detailCursor.close();
		return orderSetDetailLst;
	}
	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @param pcompGroupId
	 * @return total qty of group
	 */
	public double getTotalQty(int transactionId, int orderDetailId, int pcompGroupId){
		
		double totalQty = 0;
		
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT SUM(" + OrderSetTable.COLUMN_ORDER_SET_QTY + ") "
				+ " FROM " + OrderSetTable.TABLE_ORDER_SET
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
		getWritableDatabase().delete(OrderSetTable.TABLE_ORDER_SET, 
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
		getWritableDatabase().delete(OrderSetTable.TABLE_ORDER_SET, 
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
		
		getWritableDatabase().update(OrderSetTable.TABLE_ORDER_SET, cv, 
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
		
		getWritableDatabase().insertOrThrow(
				OrderSetTable.TABLE_ORDER_SET, ProductsTable.COLUMN_PRODUCT_NAME, cv);
	}
	
	/**
	 * @param transactionId
	 * @param orderDetailId
	 * @return max orderSetId
	 * 0 if no row
	 */
	public int getMaxOrderSetId(int transactionId, int orderDetailId){
		
		int maxOrderSetId = 0;
		
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT MAX (" + OrderSetTable.COLUMN_ORDER_SET_ID + ")"
						+ " FROM " + OrderSetTable.TABLE_ORDER_SET 
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
