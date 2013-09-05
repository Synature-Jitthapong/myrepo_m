package com.syn.mpos;

public interface POS {
	void init();
	void addOrder();
	void deleteOrder();
	void updateOrderQty();
	void summary();
}
