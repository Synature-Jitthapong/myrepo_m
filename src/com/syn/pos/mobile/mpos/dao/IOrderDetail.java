package com.syn.pos.mobile.mpos.dao;

public interface IOrderDetail {
	public long getMaxOrderDetail(long transactionId, int computerId);

	public boolean addOrderDetail(long transactionId, int computerId,
			int productId, double productPrice);

	//public boolean updateOrderDetail(int tr)
	public boolean deleteOrderDetail(long transactionId, long orderDetailId);
}
