package com.syn.mpos.db;

/**
 * 
 * @author j1tth4
 *
 */
public interface Order extends Discount{
	int getMaxOrderDetail(int transactionId, int computerId);

	int addOrderDetail(int transactionId, int computerId, int productId,
			int productType, int vatType, float serviceCharge,
			String productName, float productAmount, float productPrice);

	boolean updateOrderDetail(int transactionId, int computerId,
			int orderDetailId, int vatType, float productAmount,
			float productPrice);

	boolean deleteOrderDetail(int transactionId, int computerId, int orderDetailId);

	boolean deleteAllOrderDetail(int transactionId, int computerId);
}
