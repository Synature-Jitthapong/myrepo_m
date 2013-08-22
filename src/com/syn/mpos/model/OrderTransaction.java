package com.syn.mpos.model;

import java.util.List;

public class OrderTransaction {
	private int transactionId;
	private int computerId;
	private int shopId;
	private long openTime;
	private long closeTime;
	private int openStaffId;
	private long paidTime;
	private int paidStaffId;
	private int transactionStatusId;
	private int saleMode;
	private int documentTypeId;
	private int receiptYear;
	private int receiptMonth;
	private int receiptId;
	private long saleDate;
	private int sessionId;
	private int voidStaffId;
	private String voidReason;
	private long voidTime;
	private int memberId;
	private float transactionVat;
	private float transactionVatExclude;
	private float serviceCharge;
	private float serviceChargeVat;

	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}

	public long getOpenTime() {
		return openTime;
	}

	public void setOpenTime(long openTime) {
		this.openTime = openTime;
	}

	public long getCloseTime() {
		return closeTime;
	}

	public void setCloseTime(long closeTime) {
		this.closeTime = closeTime;
	}

	public long getPaidTime() {
		return paidTime;
	}

	public void setPaidTime(long paidTime) {
		this.paidTime = paidTime;
	}

	public long getSaleDate() {
		return saleDate;
	}

	public void setSaleDate(long saleDate) {
		this.saleDate = saleDate;
	}

	public long getVoidTime() {
		return voidTime;
	}

	public void setVoidTime(long voidTime) {
		this.voidTime = voidTime;
	}

	public float getTransactionVat() {
		return transactionVat;
	}

	public void setTransactionVat(float transactionVat) {
		this.transactionVat = transactionVat;
	}

	public float getTransactionVatExclude() {
		return transactionVatExclude;
	}

	public void setTransactionVatExclude(float transactionVatExclude) {
		this.transactionVatExclude = transactionVatExclude;
	}

	public float getServiceCharge() {
		return serviceCharge;
	}

	public void setServiceCharge(float serviceCharge) {
		this.serviceCharge = serviceCharge;
	}

	public float getServiceChargeVat() {
		return serviceChargeVat;
	}

	public void setServiceChargeVat(float serviceChargeVat) {
		this.serviceChargeVat = serviceChargeVat;
	}

	public int getPaidStaffId() {
		return paidStaffId;
	}

	public void setPaidStaffId(int paidStaffId) {
		this.paidStaffId = paidStaffId;
	}

	public int getReceiptYear() {
		return receiptYear;
	}

	public void setReceiptYear(int receiptYear) {
		this.receiptYear = receiptYear;
	}

	public int getReceiptMonth() {
		return receiptMonth;
	}

	public void setReceiptMonth(int receiptMonth) {
		this.receiptMonth = receiptMonth;
	}

	public int getComputerId() {
		return computerId;
	}

	public void setComputerId(int computerId) {
		this.computerId = computerId;
	}

	public int getShopId() {
		return shopId;
	}

	public void setShopId(int shopId) {
		this.shopId = shopId;
	}

	public int getOpenStaffId() {
		return openStaffId;
	}

	public void setOpenStaffId(int openStaffId) {
		this.openStaffId = openStaffId;
	}

	public int getTransactionStatusId() {
		return transactionStatusId;
	}

	public void setTransactionStatusId(int transactionStatusId) {
		this.transactionStatusId = transactionStatusId;
	}

	public int getSaleMode() {
		return saleMode;
	}

	public void setSaleMode(int saleMode) {
		this.saleMode = saleMode;
	}

	public int getDocumentTypeId() {
		return documentTypeId;
	}

	public void setDocumentTypeId(int documentTypeId) {
		this.documentTypeId = documentTypeId;
	}

	public int getReceiptId() {
		return receiptId;
	}

	public void setReceiptId(int receiptId) {
		this.receiptId = receiptId;
	}

	public int getSessionId() {
		return sessionId;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public int getVoidStaffId() {
		return voidStaffId;
	}

	public void setVoidStaffId(int voidStaffId) {
		this.voidStaffId = voidStaffId;
	}

	public String getVoidReason() {
		return voidReason;
	}

	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	// order detail class
	public static class OrderDetail {
		private int orderDetailId;
		private int transactionId;
		private int computerId;
		private int productId;
		private String productName;
		private int saleMode;
		private float productAmount;
		private float productPrice;
		private float salePrice;
		private float totalPrice;
		private float memberDiscount;
		private float eachProductDiscount;
		private int vatType;
		private float vat;
		private float vatExclude;
		private float serviceCharge;
		private float serviceChargeVat;

		public float getTotalPrice() {
			return totalPrice;
		}

		public void setTotalPrice(float totalPrice) {
			this.totalPrice = totalPrice;
		}

		public float getSalePrice() {
			return salePrice;
		}

		public void setSalePrice(float salePrice) {
			this.salePrice = salePrice;
		}

		public int getVatType() {
			return vatType;
		}

		public void setVatType(int vatType) {
			this.vatType = vatType;
		}

		public float getVat() {
			return vat;
		}

		public void setVat(float vat) {
			this.vat = vat;
		}

		public float getVatExclude() {
			return vatExclude;
		}

		public void setVatExclude(float vatExclude) {
			this.vatExclude = vatExclude;
		}

		public float getServiceCharge() {
			return serviceCharge;
		}

		public void setServiceCharge(float serviceCharge) {
			this.serviceCharge = serviceCharge;
		}
		
		public float getServiceChargeVat() {
			return serviceChargeVat;
		}

		public void setServiceChargeVat(float serviceChargeVat) {
			this.serviceChargeVat = serviceChargeVat;
		}

		public float getProductAmount() {
			return productAmount;
		}

		public int getOrderDetailId() {
			return orderDetailId;
		}

		public void setOrderDetailId(int orderDetailId) {
			this.orderDetailId = orderDetailId;
		}

		public int getTransactionId() {
			return transactionId;
		}

		public void setTransactionId(int transactionId) {
			this.transactionId = transactionId;
		}

		public float getProductPrice() {
			return productPrice;
		}

		public void setProductPrice(float productPrice) {
			this.productPrice = productPrice;
		}

		public float getMemberDiscount() {
			return memberDiscount;
		}

		public void setMemberDiscount(float memberDiscount) {
			this.memberDiscount = memberDiscount;
		}

		public float getEachProductDiscount() {
			return eachProductDiscount;
		}

		public void setEachProductDiscount(float eachProductDiscount) {
			this.eachProductDiscount = eachProductDiscount;
		}

		public void setProductAmount(float productAmount) {
			this.productAmount = productAmount;
		}

		public int getComputerId() {
			return computerId;
		}

		public void setComputerId(int computerId) {
			this.computerId = computerId;
		}

		public int getProductId() {
			return productId;
		}

		public void setProductId(int productId) {
			this.productId = productId;
		}

		public String getProductName() {
			return productName;
		}

		public void setProductName(String productName) {
			this.productName = productName;
		}

		public int getSaleMode() {
			return saleMode;
		}

		public void setSaleMode(int saleMode) {
			this.saleMode = saleMode;
		}
	}
}
