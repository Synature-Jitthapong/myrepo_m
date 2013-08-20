package com.syn.mpos.data;

/**
 * 
 * @author j1tth4
 *
 */
public interface Order {
	int getMaxOrderDetail(int transactionId, int computerId);

	int addOrderDetail(int transactionId, int computerId, int productId,
			int productType, int vatType, float serviceCharge,
			String productName, float productAmount, float productPrice);

	boolean updateOrderDetail(int transactionId, int computerId,
			int orderDetailId, int vatType, float productAmount,
			float productPrice);

	boolean updateOrderDetail(int transactionId, int computerId,
			int orderDetailId, int vatType, float productAmount,
			float productPrice, float eatchProductDiscount, float memberDiscount);

	boolean deleteOrderDetail(int transactionId, int orderDetailId);

	boolean deleteAllOrderDetail(int transactionId);
}
