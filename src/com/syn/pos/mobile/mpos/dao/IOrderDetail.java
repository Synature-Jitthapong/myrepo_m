package com.syn.pos.mobile.mpos.dao;

public interface IOrderDetail {
	public long getMaxOrderDetail(long transactionId, int computerId);

	public long addOrderDetail(long transactionId, int computerId,
			int productId, String productName, double productAmount, double productPrice);

	//public boolean updateOrderDetail(int tr)
	public boolean deleteOrderDetail(long transactionId, long orderDetailId);
	
	public boolean deleteAllOrderDetail(long transactionId);
}
