package com.syn.mpos.inventory;

import java.util.List;

import com.syn.pos.OrderTransaction;

import android.content.Context;

public class MPOSSaleStock extends MPOSStockDocument {

	public MPOSSaleStock(Context context) {
		super(context);
	}

	public boolean createVoidDocument(int shopId, int staffId,
			List<OrderTransaction.OrderDetail> orderLst, String remark) {

		boolean isSuccess = false;

		int documentId = createDocument(shopId, VOID_DOC, staffId);
		if (documentId > 0) {
			for (OrderTransaction.OrderDetail order : orderLst) {
				if (addDocumentDetail(documentId, shopId, order.getProductId(),
						order.getQty(), 0, order.getPricePerUnit(),
						order.getVatType(), remark) > 0) {
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
						order.getQty(), 0, order.getPricePerUnit(),
						order.getVatType(), "") > 0) {
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
