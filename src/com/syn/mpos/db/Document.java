package com.syn.mpos.db;

/**
 * 
 * @author j1tth4
 *
 */
public interface Document {
	int getMaxDocument(int shopId);
	int getMaxDocumentNo(int documentId, int shopId, int documentMonth, int documentYear);
	boolean addDocument(int shopId, int documentTypeId);
}
