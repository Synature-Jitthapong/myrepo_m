package com.syn.pos.mobile.mpos.dao;

public interface IOrderDetail {
	public int getMaxOrderDetail(int transactionId, int computerId);

	public int addOrderDetail(int transactionId, int computerId,
			int productId, int productType, int vatType, float serviceCharge,
			String productName, float productAmount, float productPrice);

	// public boolean updateOrderDetail(int tr)
	public boolean deleteOrderDetail(int transactionId, int orderDetailId);

	public boolean deleteAllOrderDetail(int transactionId);
}
