package com.synature.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.synature.mpos.database.table.BankTable;
import com.synature.mpos.database.table.ComputerTable;
import com.synature.mpos.database.table.CreditCardTable;
import com.synature.mpos.database.table.OrderTransTable;
import com.synature.mpos.database.table.PayTypeTable;
import com.synature.mpos.database.table.PaymentDetailTable;
import com.synature.mpos.database.table.SessionTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class PaymentDetail extends MPOSDatabase {
	
	public static final int PAY_TYPE_CASH = 1;
	public static final int PAY_TYPE_CREDIT = 2;

	/**
	 * All payment columns
	 */
	public static final String[] ALL_PAYMENT_DETAIL_COLUMNS = {
		PaymentDetailTable.COLUMN_PAY_ID,
		ComputerTable.COLUMN_COMPUTER_ID,
		BankTable.COLUMN_BANK_ID,
		PayTypeTable.COLUMN_PAY_TYPE_ID,
		CreditCardTable.COLUMN_CREDITCARD_TYPE_ID,
		CreditCardTable.COLUMN_CREDITCARD_NO,
		CreditCardTable.COLUMN_EXP_MONTH,
		CreditCardTable.COLUMN_EXP_YEAR,
		PaymentDetailTable.COLUMN_REMARK,
		PaymentDetailTable.COLUMN_PAY_AMOUNT
	};
	
	public PaymentDetail(Context context) {
		super(context);
	}
	
	/**
	 * Get total bill that pay by cash
     * @param sessId
	 * @param saleDate
	 * @return total transaction pay by cash
	 */
	public int getTotalTransPayByCash(int sessId, String saleDate){
		int totalTrans = 0;
        String selection = " a." + OrderTransTable.COLUMN_SALE_DATE + "=?"
                + " AND b." + PayTypeTable.COLUMN_PAY_TYPE_ID + "=?";
        String[] selectionArgs = {
                saleDate,
                String.valueOf(PaymentDetail.PAY_TYPE_CASH)
        };
        if(sessId != 0){
            selection += " AND a." + SessionTable.COLUMN_SESS_ID + "=?";
            selectionArgs = new String[]{
                    saleDate,
                    String.valueOf(PaymentDetail.PAY_TYPE_CASH),
                    String.valueOf(sessId)
            };
        }
        String sql = "SELECT COUNT(a." + OrderTransTable.COLUMN_TRANS_ID + ")"
                + " FROM " + OrderTransTable.TABLE_ORDER_TRANS + " a "
                + " LEFT JOIN " + PaymentDetailTable.TABLE_PAYMENT_DETAIL + " b "
                + " ON a." + OrderTransTable.COLUMN_TRANS_ID + "=b." + OrderTransTable.COLUMN_TRANS_ID
                + " WHERE " + selection
                + " GROUP BY a." + OrderTransTable.COLUMN_SALE_DATE;
		Cursor cursor = getReadableDatabase().rawQuery(
				sql, selectionArgs);
		if(cursor.moveToFirst()){
			totalTrans = cursor.getInt(0);
		}
		cursor.close();
		return totalTrans;
	}
	
	/**
	 * Get summary of payment
     * @param sessId
	 * @param saleDate
	 * @return List<MPOSPaymentDetail>
	 */
	public List<MPOSPaymentDetail> listSummaryPayment(int sessId, String saleDate){
		List<MPOSPaymentDetail> paymentLst = new ArrayList<MPOSPaymentDetail>();
        String selection = " a." + OrderTransTable.COLUMN_SALE_DATE + "=? "
                + " AND a." + OrderTransTable.COLUMN_STATUS_ID + "=?";
        String[] selectionArgs = {
                saleDate,
                String.valueOf(Transaction.TRANS_STATUS_SUCCESS)
        };
        if(sessId != 0){
            selection += " AND a." + SessionTable.COLUMN_SESS_ID + "=?";
            selectionArgs = new String[]{
                    saleDate,
                    String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
                    String.valueOf(sessId)
            };
        }
        String sql = "SELECT c." + PayTypeTable.COLUMN_PAY_TYPE_NAME + ", "
                + " SUM(b." + PaymentDetailTable.COLUMN_PAY_AMOUNT + ") "
                + " AS " + PaymentDetailTable.COLUMN_PAY_AMOUNT
                + " FROM " + OrderTransTable.TABLE_ORDER_TRANS + " a "
                + " LEFT JOIN " + PaymentDetailTable.TABLE_PAYMENT_DETAIL + " b "
                + " ON a." + OrderTransTable.COLUMN_TRANS_ID + "=b." + OrderTransTable.COLUMN_TRANS_ID
                + " LEFT JOIN " + PayTypeTable.TABLE_PAY_TYPE + " c "
                + " ON b." + PayTypeTable.COLUMN_PAY_TYPE_ID + "=c." + PayTypeTable.COLUMN_PAY_TYPE_ID
                + " WHERE " + selection
                + " GROUP BY c." + PayTypeTable.COLUMN_PAY_TYPE_ID;
		Cursor cursor = getReadableDatabase().rawQuery(
				sql, selectionArgs);
		if(cursor.moveToFirst()){
			do{
				MPOSPaymentDetail payment = new MPOSPaymentDetail();
				payment.setPayTypeName(cursor.getString(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_NAME)));
				payment.setPayAmount(cursor.getDouble(cursor.getColumnIndex(PaymentDetailTable.COLUMN_PAY_AMOUNT)));
				paymentLst.add(payment);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return paymentLst;
	}
	
	/**
	 * @return List<Payment.PayType>
	 */
	public List<com.synature.pos.PayType> listPayType(){
		List<com.synature.pos.PayType> payTypeLst = new ArrayList<com.synature.pos.PayType>();
		Cursor cursor = getReadableDatabase().query(PayTypeTable.TABLE_PAY_TYPE,
				new String[]{
				PayTypeTable.COLUMN_PAY_TYPE_ID,
				PayTypeTable.COLUMN_PAY_TYPE_CODE,
				PayTypeTable.COLUMN_PAY_TYPE_NAME
				}, null, null, null, null, COLUMN_ORDERING);
		if(cursor.moveToFirst()){
			do{
				com.synature.pos.PayType payType = new com.synature.pos.PayType();
				payType.setPayTypeID(cursor.getInt(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_ID)));
				payType.setPayTypeCode(cursor.getString(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_CODE)));
				payType.setPayTypeName(cursor.getString(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_NAME)));
				payTypeLst.add(payType);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return payTypeLst;
	}
	
	/**
	 * @param transactionId
	 * @return List<MPOSPaymentDetail>
	 */
	public List<MPOSPaymentDetail> listPaymentGroupByType(int transactionId){
		List<MPOSPaymentDetail> paymentLst = new ArrayList<MPOSPaymentDetail>();
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT a." + PayTypeTable.COLUMN_PAY_TYPE_ID + ", " + " a."
						+ CreditCardTable.COLUMN_CREDITCARD_TYPE_ID + ", "
						+ " a." + CreditCardTable.COLUMN_CREDITCARD_NO + ", "
						+ " SUM(a." + PaymentDetailTable.COLUMN_TOTAL_PAY_AMOUNT + ") AS "
						+ PaymentDetailTable.COLUMN_TOTAL_PAY_AMOUNT + ", " + " SUM(a."
						+ PaymentDetailTable.COLUMN_PAY_AMOUNT + ") AS "
						+ PaymentDetailTable.COLUMN_PAY_AMOUNT + ", " + " b."
						+ PayTypeTable.COLUMN_PAY_TYPE_CODE + ", " + " b."
						+ PayTypeTable.COLUMN_PAY_TYPE_NAME + " FROM "
						+ PaymentDetailTable.TABLE_PAYMENT_DETAIL + " a " + " LEFT JOIN "
						+ PayTypeTable.TABLE_PAY_TYPE + " b " + " ON a."
						+ PayTypeTable.COLUMN_PAY_TYPE_ID + "=b."
						+ PayTypeTable.COLUMN_PAY_TYPE_ID + " WHERE a."
						+ OrderTransTable.COLUMN_TRANS_ID + "=?"
						+ " GROUP BY a." + PayTypeTable.COLUMN_PAY_TYPE_ID
						+ " ORDER BY a." + PaymentDetailTable.COLUMN_PAY_ID,
				new String[]{
					String.valueOf(transactionId)
				}
		);
		if(cursor.moveToFirst()){
			do{
				MPOSPaymentDetail payment = new MPOSPaymentDetail();
				payment.setPayTypeId(cursor.getInt(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_ID)));
				payment.setCreditCardTypeId(cursor.getInt(cursor.getColumnIndex(CreditCardTable.COLUMN_CREDITCARD_TYPE_ID)));
				payment.setCreditCardNo(cursor.getString(cursor.getColumnIndex(CreditCardTable.COLUMN_CREDITCARD_NO)));
				payment.setTotalPay(cursor.getDouble(cursor.getColumnIndex(PaymentDetailTable.COLUMN_TOTAL_PAY_AMOUNT)));
				payment.setPayAmount(cursor.getDouble(cursor.getColumnIndex(PaymentDetailTable.COLUMN_PAY_AMOUNT)));
				payment.setPayTypeCode(cursor.getString(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_CODE)));
				payment.setPayTypeName(cursor.getString(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_NAME)));
				paymentLst.add(payment);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return paymentLst;
	}
	
	/**
	 * @param transactionId
	 * @return row affected
	 */
	public int deleteAllPaymentDetail(int transactionId){
		return getWritableDatabase().delete(PaymentDetailTable.TABLE_PAYMENT_DETAIL, 
					OrderTransTable.COLUMN_TRANS_ID + "=?",
					new String[]{String.valueOf(transactionId)});
	}
	
	/**
	 * @return max paymentDetailId
	 */
	public int getMaxPaymentDetailId() {
		int maxPaymentId = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT MAX(" + PaymentDetailTable.COLUMN_PAY_ID + ") "
						+ " FROM " + PaymentDetailTable.TABLE_PAYMENT_DETAIL, null);
		if(cursor.moveToFirst()){
			maxPaymentId = cursor.getInt(0);
		}
		cursor.close();
		return maxPaymentId + 1;
	}

	/**
	 * @param transactionId
	 * @param computerId
	 * @param payTypeId
	 * @param totalPay
	 * @param pay
	 * @param creditCardNo
	 * @param expireMonth
	 * @param expireYear
	 * @param bankId
	 * @param creditCardTypeId
	 * @param remark
	 * @return The ID of newly row inserted
	 * @throws SQLException
	 */
	public void addPaymentDetail(int transactionId, int computerId, 
			int payTypeId, double totalPay, double pay , String creditCardNo, int expireMonth, 
			int expireYear, int bankId,int creditCardTypeId, String remark) throws SQLException {
		if(checkThisPayTypeIsAdded(transactionId, payTypeId)){
			// update payment
			double totalPayment = getTotalPayAmount(transactionId) + totalPay;
			double totalPayed = getTotalPayedAmount(transactionId) + pay;
			updatePaymentDetail(transactionId, payTypeId, totalPayment, totalPayed);
		}else{
			// add new payment
			int paymentId = getMaxPaymentDetailId();
			ContentValues cv = new ContentValues();
			cv.put(PaymentDetailTable.COLUMN_PAY_ID, paymentId);
			cv.put(OrderTransTable.COLUMN_TRANS_ID, transactionId);
			cv.put(ComputerTable.COLUMN_COMPUTER_ID, computerId);
			cv.put(PayTypeTable.COLUMN_PAY_TYPE_ID, payTypeId);
			cv.put(PaymentDetailTable.COLUMN_TOTAL_PAY_AMOUNT, totalPay);
			cv.put(PaymentDetailTable.COLUMN_PAY_AMOUNT, pay);
			cv.put(CreditCardTable.COLUMN_CREDITCARD_NO, creditCardNo);
			cv.put(CreditCardTable.COLUMN_EXP_MONTH, expireMonth);
			cv.put(CreditCardTable.COLUMN_EXP_YEAR, expireYear);
			cv.put(CreditCardTable.COLUMN_CREDITCARD_TYPE_ID, creditCardTypeId);
			cv.put(BankTable.COLUMN_BANK_ID, bankId);
			cv.put(PaymentDetailTable.COLUMN_REMARK, remark);
			getWritableDatabase().insertOrThrow(PaymentDetailTable.TABLE_PAYMENT_DETAIL, null, cv);
		}
	}

	/**
	 * @param transactionId
	 * @param payTypeId
	 * @return added or not
	 */
	public boolean checkThisPayTypeIsAdded(int transactionId, int payTypeId){
		boolean isAdded = false;
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT COUNT(" + PaymentDetailTable.COLUMN_PAY_ID + ")"
				+ " FROM " + PaymentDetailTable.TABLE_PAYMENT_DETAIL
				+ " WHERE " + OrderTransTable.COLUMN_TRANS_ID + "=?"
				+ " AND " + PayTypeTable.COLUMN_PAY_TYPE_ID + "=?", 
				new String[]{String.valueOf(transactionId), String.valueOf(payTypeId)});
		if(cursor.moveToFirst()){
			if(cursor.getInt(0) > 0)
				isAdded = true;
		}
		cursor.close();
		return isAdded;
	}
	
	/**
	 * @param transactionId
	 * @param payTypeId
	 * @param paid
	 * @param amount
	 * @return row affected
	 */
	public int updatePaymentDetail(int transactionId, int payTypeId,
			double paid, double amount){
		ContentValues cv = new ContentValues();
		cv.put(PaymentDetailTable.COLUMN_TOTAL_PAY_AMOUNT, paid);
		cv.put(PaymentDetailTable.COLUMN_PAY_AMOUNT, amount);
		return getWritableDatabase().update(PaymentDetailTable.TABLE_PAYMENT_DETAIL, cv, 
				OrderTransTable.COLUMN_TRANS_ID + "=? "
								+ " AND " + PayTypeTable.COLUMN_PAY_TYPE_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(payTypeId)
				}
		);
	}

	/**
	 * @param transactionId
	 * @param payTypeId
	 * @return row affected
	 */
	public int deletePaymentDetail(int transactionId, int payTypeId){
		return getWritableDatabase().delete(PaymentDetailTable.TABLE_PAYMENT_DETAIL,
				OrderTransTable.COLUMN_TRANS_ID + "=? AND "
				+ PayTypeTable.COLUMN_PAY_TYPE_ID + "=?", 
				new String[]{
					String.valueOf(transactionId),
					String.valueOf(payTypeId)});
	}
	
	/**
     * @param sessId
	 * @param saleDate
	 * @return total cash amount
	 */
	public double getTotalCash(int sessId, String saleDate){
		double totalCash = 0.0d;
        String selection = " a." + OrderTransTable.COLUMN_SALE_DATE + "=? "
                + " AND a." + OrderTransTable.COLUMN_STATUS_ID + "=?"
                + " AND b." + PayTypeTable.COLUMN_PAY_TYPE_ID + "=?";
        String[] selectionArgs = {
                saleDate,
                String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
                String.valueOf(PAY_TYPE_CASH)
        };
        if(sessId != 0){
            selection += " AND a." + SessionTable.COLUMN_SESS_ID + "=?";
            selectionArgs = new String[]{
                    saleDate,
                    String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
                    String.valueOf(PAY_TYPE_CASH),
                    String.valueOf(sessId)
            };
        }
        String sql = " SELECT SUM(b." + PaymentDetailTable.COLUMN_PAY_AMOUNT + ") "
                + " FROM " + OrderTransTable.TABLE_ORDER_TRANS + " a "
                + " INNER JOIN " + PaymentDetailTable.TABLE_PAYMENT_DETAIL + " b "
                + " ON a." + OrderTransTable.COLUMN_TRANS_ID + "=b." + OrderTransTable.COLUMN_TRANS_ID
                + " WHERE " + selection;
		Cursor cursor = getReadableDatabase().rawQuery(
				sql, selectionArgs);
		if(cursor.moveToFirst()){
			totalCash = cursor.getDouble(0);
		}
		cursor.close();
		return totalCash;
	}
	
	/**
	 * Get total payed
	 * @param transactionId
	 * @return total payed amount
	 */
	public double getTotalPayedAmount(int transactionId){
		double totalPaid = 0.0d;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT SUM(" + PaymentDetailTable.COLUMN_PAY_AMOUNT + ") "
						+ " FROM " + PaymentDetailTable.TABLE_PAYMENT_DETAIL 
						+ " WHERE " + OrderTransTable.COLUMN_TRANS_ID + "=?",
				new String[]{
						String.valueOf(transactionId)
				});
		if(cursor.moveToFirst()){
			totalPaid = cursor.getDouble(0);
		}
		cursor.close();
		return totalPaid;
	}
	
	/**
	 * Get total pay
	 * @param transactionId
	 * @return total pay amount
	 */
	public double getTotalPayAmount(int transactionId){
		double totalPaid = 0.0d;
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT SUM(" + PaymentDetailTable.COLUMN_TOTAL_PAY_AMOUNT + ") "
						+ " FROM " + PaymentDetailTable.TABLE_PAYMENT_DETAIL 
						+ " WHERE " + OrderTransTable.COLUMN_TRANS_ID + "=?",
				new String[]{
						String.valueOf(transactionId)
				});
		if(cursor.moveToFirst()){
			totalPaid = cursor.getDouble(0);
		}
		cursor.close();
		return totalPaid;
	}
	
	/**
	 * @param transactionId
	 * @return List<MPOSPaymentDetail>
	 */
	public List<MPOSPaymentDetail> listPayment(int transactionId){
		List<MPOSPaymentDetail> paymentLst = new ArrayList<MPOSPaymentDetail>();
		Cursor cursor = getReadableDatabase().rawQuery(
				" SELECT a." + PaymentDetailTable.COLUMN_PAY_ID + ", " 
						+ " a." + PayTypeTable.COLUMN_PAY_TYPE_ID + ", " 
						+ " a." + PaymentDetailTable.COLUMN_TOTAL_PAY_AMOUNT + ", " 
						+ " a." + PaymentDetailTable.COLUMN_REMARK + ", " 
						+ " b." + PayTypeTable.COLUMN_PAY_TYPE_CODE + ", " 
						+ " b." + PayTypeTable.COLUMN_PAY_TYPE_NAME 
						+ " FROM " + PaymentDetailTable.TABLE_PAYMENT_DETAIL + " a " 
						+ " INNER JOIN " + PayTypeTable.TABLE_PAY_TYPE + " b "
						+ " ON a." + PayTypeTable.COLUMN_PAY_TYPE_ID 
						+ "=b." + PayTypeTable.COLUMN_PAY_TYPE_ID 
						+ " WHERE a." + OrderTransTable.COLUMN_TRANS_ID + "=?"
						+ " GROUP BY a." + PayTypeTable.COLUMN_PAY_TYPE_ID,
				new String[]{
						String.valueOf(transactionId)});
		if(cursor.moveToFirst()){
			do{
				MPOSPaymentDetail payDetail = new MPOSPaymentDetail();
				payDetail.setTransactionId(transactionId);
				payDetail.setPaymentDetailId(cursor.getInt(cursor.getColumnIndex(PaymentDetailTable.COLUMN_PAY_ID)));
				payDetail.setPayTypeId(cursor.getInt(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_ID)));
				payDetail.setPayTypeCode(cursor.getString(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_CODE)));
				payDetail.setPayTypeName(cursor.getString(cursor.getColumnIndex(PayTypeTable.COLUMN_PAY_TYPE_NAME)));
				payDetail.setTotalPay(cursor.getDouble(cursor.getColumnIndex(PaymentDetailTable.COLUMN_TOTAL_PAY_AMOUNT)));
				payDetail.setRemark(cursor.getString(cursor.getColumnIndex(PaymentDetailTable.COLUMN_REMARK)));
				paymentLst.add(payDetail);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return paymentLst;
	}
	
	/**
	 * @param payTypeLst
	 */
	public void insertPaytype(List<com.synature.pos.PayType> payTypeLst){
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(PayTypeTable.TABLE_PAY_TYPE, null, null);
			for(com.synature.pos.PayType payType : payTypeLst){
				ContentValues cv = new ContentValues();
				cv.put(PayTypeTable.COLUMN_PAY_TYPE_ID, payType.getPayTypeID());
				cv.put(PayTypeTable.COLUMN_PAY_TYPE_CODE, payType.getPayTypeCode());
				cv.put(PayTypeTable.COLUMN_PAY_TYPE_NAME, payType.getPayTypeName());
				getWritableDatabase().insertOrThrow(PayTypeTable.TABLE_PAY_TYPE, null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally {
			getWritableDatabase().endTransaction();
		}
	}
}
