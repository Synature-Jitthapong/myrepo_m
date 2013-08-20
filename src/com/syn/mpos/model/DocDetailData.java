package com.syn.mpos.model;

import java.util.List;

public class DocDetailData {
	private int documentId;
	private int shopId;
	private int docType;
	
	private List<DocDetail> docDetailLst;
	
	public int getDocType() {
		return docType;
	}
	public void setDocType(int docType) {
		this.docType = docType;
	}
	public int getDocumentId() {
		return documentId;
	}
	public void setDocumentId(int documentId) {
		this.documentId = documentId;
	}
	public int getShopId() {
		return shopId;
	}
	public void setShopId(int shopId) {
		this.shopId = shopId;
	}
	public List<DocDetail> getDocDetailLst() {
		return docDetailLst;
	}
	public void setDocDetailLst(List<DocDetail> docDetailLst) {
		this.docDetailLst = docDetailLst;
	}
}
