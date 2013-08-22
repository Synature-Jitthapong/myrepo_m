package com.syn.mpos.db;

import java.util.List;

import com.syn.mpos.model.OrderTransaction;

/**
 * 
 * @author j1tth4
 *
 */
public interface Discount {
	boolean discountEatchProduct(int orderDetailId, int transactionId, 
			int computerId, int vatType, float amount, float discount, float salePrice);
}
