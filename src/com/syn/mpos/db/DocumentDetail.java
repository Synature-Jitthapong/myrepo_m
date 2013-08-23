package com.syn.mpos.db;

/**
 * 
 * @author j1tth4
 *
 */
public interface DocumentDetail {
	int getMaxDocumentDetailId(int documentId, int shopId);
	boolean addDocumentDetail(int dcoumentId, int shopId, float productAmount);
}
