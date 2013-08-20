package com.syn.mpos.model;

public class DocumentParam {
	private int documentId;
	private int shopId;
	private int staffId;
	private int documentStatus;
	private String documentDate;
	private String documentNumber;
	private String remark;
	
	public DocumentParam(){
		
	}
	
	public DocumentParam(int documentId, int shopId, int staffId, 
			String documentDate, String documentNumber, int documentStatus) {
		this.documentId = documentId;
		this.shopId = shopId;
		this.staffId = staffId;
		this.documentDate = documentDate;
		this.documentNumber = documentNumber;
		this.documentStatus = documentStatus;
	}

	
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public String getDocumentDate() {
		return documentDate;
	}

	public void setDocumentDate(String documentDate) {
		this.documentDate = documentDate;
	}

	public int getStaffId() {
		return staffId;
	}

	public void setStaffId(int staffId) {
		this.staffId = staffId;
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

	public int getDocumentStatus() {
		return documentStatus;
	}

	public void setDocumentStatus(int documentStatus) {
		this.documentStatus = documentStatus;
	}
}
