package com.syn.pos.mobile.model;

public class DocumentType {
	private int documentTypeId;
	private String documentTypeHeader;
	private String documentTypeName;
	private int movementInStock;
	
	public int getDocumentTypeId() {
		return documentTypeId;
	}
	public void setDocumentTypeId(int documentTypeId) {
		this.documentTypeId = documentTypeId;
	}
	public String getDocumentTypeHeader() {
		return documentTypeHeader;
	}
	public void setDocumentTypeHeader(String documentTypeHeader) {
		this.documentTypeHeader = documentTypeHeader;
	}
	public String getDocumentTypeName() {
		return documentTypeName;
	}
	public void setDocumentTypeName(String documentTypeName) {
		this.documentTypeName = documentTypeName;
	}
	public int getMovementInStock() {
		return movementInStock;
	}
	public void setMovementInStock(int movementInStock) {
		this.movementInStock = movementInStock;
	}
}
