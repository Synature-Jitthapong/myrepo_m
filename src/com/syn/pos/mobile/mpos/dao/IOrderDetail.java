package com.syn.pos.mobile.mpos.dao;

public interface IOrderDetail {
	public int getMaxOrderDetail(int transactionId, int computerId);

	public boolean addOrderDetail(int transactionId, int computerId,
			int productId, double productPrice);

	//public boolean updateOrderDetail(int tr)
	public boolean deleteOrderDetail(int transactionId, int orderDetailId);
}
