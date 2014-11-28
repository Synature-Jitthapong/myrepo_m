package com.synature.pos;

public class PayType {
	private int PayTypeID;
	private String PayTypeCode;
	private String PayTypeName;
	private int Ordering;
	public int getPayTypeID() {
		return PayTypeID;
	}
	public void setPayTypeID(int payTypeID) {
		PayTypeID = payTypeID;
	}
	public String getPayTypeCode() {
		return PayTypeCode;
	}
	public void setPayTypeCode(String payTypeCode) {
		PayTypeCode = payTypeCode;
	}
	public String getPayTypeName() {
		return PayTypeName;
	}
	public void setPayTypeName(String payTypeName) {
		PayTypeName = payTypeName;
	}
	public int getOrdering() {
		return Ordering;
	}
	public void setOrdering(int ordering) {
		Ordering = ordering;
	}
}
