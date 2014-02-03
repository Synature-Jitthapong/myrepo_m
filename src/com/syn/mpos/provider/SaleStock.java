package com.syn.mpos.provider;

import java.util.List;
import com.syn.pos.OrderTransaction;
import android.database.sqlite.SQLiteDatabase;

public class SaleStock extends StockDocument {

	public SaleStock(SQLiteDatabase db) {
		super(db);
	}

	public boolean createVoidDocument(int shopId, int staffId,
			List<OrderTransaction.OrderDetail> orderLst, String remark) {
		boolean isSuccess = false;

		int documentId = createDocument(shopId, VOID_DOC, staffId);
		if (documentId > 0) {
			for (OrderTransaction.OrderDetail order : orderLst) {
				if (addDocumentDetail(documentId, shopId, order.getProductId(),
						order.getQty(), order.getPricePerUnit(), "", remark) > 0) {
					isSuccess = true;
				} else {
					isSuccess = false;
				}
			}
			if(isSuccess)
				isSuccess = approveDocument(documentId, shopId, staffId, "");
		} else {
			isSuccess = false;
		}
		
		return isSuccess;
	}
	
	public boolean createSaleDocument(int shopId, int staffId,
			List<OrderTransaction.OrderDetail> orderLst) {
		boolean isSuccess = false;
		
		int documentId = createDocument(shopId, SALE_DOC, staffId);
		if (documentId > 0) {
			for (OrderTransaction.OrderDetail order : orderLst) {
				if (addDocumentDetail(documentId, shopId, order.getProductId(),
						order.getQty(), order.getPricePerUnit(), "") > 0) {
					isSuccess = true;
				} else {
					isSuccess = false;
				}
			}
			if(isSuccess)
				isSuccess = approveDocument(documentId, shopId, staffId, "");
		} else {
			isSuccess = false;
		}
		return isSuccess;
	}
}