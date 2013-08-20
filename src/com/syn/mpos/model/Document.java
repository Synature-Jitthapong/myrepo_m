package com.syn.mpos.model;

import java.util.List;

public class Document{
	public List<DocDetail> docDetailLst;
	
	private int documentId;
	private int shopId;
	private int documentTypeId;
	private int documentNo;
	private String documentNumber;		// running no
	private String DocumentDate;
	private int UpdateBy;
	private String updateDate;
	private int documentStatus;
	private String remark;
	private int isSendToHQ;
	private String SendToHQDateTime;
	
	public String getDocumentNumber() {
		return documentNumber;
	}
	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
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
	public int getDocumentTypeId() {
		return documentTypeId;
	}
	public void setDocumentTypeId(int documentTypeId) {
		this.documentTypeId = documentTypeId;
	}
	public int getDocumentNo() {
		return documentNo;
	}
	public void setDocumentNo(int documentNo) {
		this.documentNo = documentNo;
	}
	public String getDocumentDate() {
		return DocumentDate;
	}
	public void setDocumentDate(String documentDate) {
		DocumentDate = documentDate;
	}
	public int getUpdateBy() {
		return UpdateBy;
	}
	public void setUpdateBy(int updateBy) {
		UpdateBy = updateBy;
	}
	public String getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}
	public int getDocumentStatus() {
		return documentStatus;
	}
	public void setDocumentStatus(int documentStatus) {
		this.documentStatus = documentStatus;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public int getIsSendToHQ() {
		return isSendToHQ;
	}
	public void setIsSendToHQ(int isSendToHQ) {
		this.isSendToHQ = isSendToHQ;
	}
	public String getSendToHQDateTime() {
		return SendToHQDateTime;
	}
	public void setSendToHQDateTime(String sendToHQDateTime) {
		SendToHQDateTime = sendToHQDateTime;
	}
}