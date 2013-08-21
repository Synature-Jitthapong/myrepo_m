package com.syn.mpos.db;

/**
 * 
 * @author j1tth4
 *
 */
public interface Discount {
	boolean discountEatchProduct(int orderDetailId, int transactionId, 
			int computerId, float discount, float salePrice);
}
