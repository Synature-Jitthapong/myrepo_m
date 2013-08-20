package com.syn.mpos.model;

public class QueueInfo {
	private int iQueueID;
	private int iQueueIndex;
	private int iQueueGroupID;
	private String szQueueName;
	private String szCustomerName;
	private int iCustomerQty;
	private String szStartQueueDate;
	private int iWaitQueueMinTime;
	private int iWaitQueueCurrentOfGroup;
	private int iHasPreOrderList;

	public int getiHasPreOrderList() {
		return iHasPreOrderList;
	}

	public void setiHasPreOrderList(int iHasPreOrderList) {
		this.iHasPreOrderList = iHasPreOrderList;
	}

	public int getiQueueID() {
		return iQueueID;
	}

	public void setiQueueID(int iQueueID) {
		this.iQueueID = iQueueID;
	}

	public int getiQueueIndex() {
		return iQueueIndex;
	}

	public void setiQueueIndex(int iQueueIndex) {
		this.iQueueIndex = iQueueIndex;
	}

	public int getiQueueGroupID() {
		return iQueueGroupID;
	}

	public void setiQueueGroupID(int iQueueGroupID) {
		this.iQueueGroupID = iQueueGroupID;
	}

	public String getSzQueueName() {
		return szQueueName;
	}

	public void setSzQueueName(String szQueueName) {
		this.szQueueName = szQueueName;
	}

	public String getSzCustomerName() {
		return szCustomerName;
	}

	public void setSzCustomerName(String szCustomerName) {
		this.szCustomerName = szCustomerName;
	}

	public int getiCustomerQty() {
		return iCustomerQty;
	}

	public void setiCustomerQty(int iCustomerQty) {
		this.iCustomerQty = iCustomerQty;
	}

	public String getSzStartQueueDate() {
		return szStartQueueDate;
	}

	public void setSzStartQueueDate(String szStartQueueDate) {
		this.szStartQueueDate = szStartQueueDate;
	}

	public int getiWaitQueueMinTime() {
		return iWaitQueueMinTime;
	}

	public void setiWaitQueueMinTime(int iWaitQueueMinTime) {
		this.iWaitQueueMinTime = iWaitQueueMinTime;
	}

	public int getiWaitQueueCurrentOfGroup() {
		return iWaitQueueCurrentOfGroup;
	}

	public void setiWaitQueueCurrentOfGroup(int iWaitQueueCurrentOfGroup) {
		this.iWaitQueueCurrentOfGroup = iWaitQueueCurrentOfGroup;
	}
}
