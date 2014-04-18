package com.syn.mpos.database;

import java.util.List;

import com.syn.pos.OrderTransaction;

public class Orders extends OrderTransaction.OrderDetail{
	public List<ProductsDataSource.ProductSet.ProductSetDetail> mProductSetLst;
}
