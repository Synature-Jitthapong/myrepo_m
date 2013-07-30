package com.syn.pos.mobile.model;

import java.util.List;

public class OrderTransaction {
	private long transactionId;
	private int computerId;
	private int shopId;
	private String openTime;
	private String closeTime;
	private int openStaffId;
	private String paidTime;
	private int paidStaffId;
	private int transactionStatusId;
	private int saleMode;
	private int documentTypeId;
	private int receiptYear;
	private int receiptMonth;
	private int receiptId;
	private String saleDate;
	private double transactionVat;
	private double transactionExcludeVat;
	private double serviceCharge;
	private double serviceChargeVat;
	private double transactionVatAble;
	private int sessionId;
	private int voidStaffId;
	private String voidReason;
	private String voidTime;
	private int memberId;
	
	public OrderDetail orderDetail;
	public List<OrderDetail> orderDetailLst;
	
	public String getPaidTime() {
		return paidTime;
	}

	public void setPaidTime(String paidTime) {
		this.paidTime = paidTime;
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

	public double getTransactionExcludeVat() {
		return transactionExcludeVat;
	}

	public void setTransactionExcludeVat(double transactionExcludeVat) {
		this.transactionExcludeVat = transactionExcludeVat;
	}

	public String getVoidTime() {
		return voidTime;
	}

	public void setVoidTime(String voidTime) {
		this.voidTime = voidTime;
	}

	public long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
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

	public String getOpenTime() {
		return openTime;
	}

	public void setOpenTime(String openTime) {
		this.openTime = openTime;
	}

	public String getCloseTime() {
		return closeTime;
	}

	public void setCloseTime(String closeTime) {
		this.closeTime = closeTime;
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

	public String getSaleDate() {
		return saleDate;
	}

	public void setSaleDate(String saleDate) {
		this.saleDate = saleDate;
	}

	public double getTransactionVat() {
		return transactionVat;
	}

	public void setTransactionVat(double transactionVat) {
		this.transactionVat = transactionVat;
	}

	public double getServiceCharge() {
		return serviceCharge;
	}

	public void setServiceCharge(double serviceCharge) {
		this.serviceCharge = serviceCharge;
	}

	public double getServiceChargeVat() {
		return serviceChargeVat;
	}

	public void setServiceChargeVat(double serviceChargeVat) {
		this.serviceChargeVat = serviceChargeVat;
	}

	public double getTransactionVatAble() {
		return transactionVatAble;
	}

	public void setTransactionVatAble(double transactionVatAble) {
		this.transactionVatAble = transactionVatAble;
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
		private long orderDetailId;
		private long transactionId;
		private int computerId;
		private int productId;
		private String productName;
		private int saleMode;
		private double productAmount;
		private double productPrice;
		private double memberDiscount;
		private double eachProductDiscount;
		private double vat;
		private double serviceCharge;
		private double totalRetailPrice;
		private double totalPrice;
		
		public double getProductAmount() {
			return productAmount;
		}
		public void setProductAmount(double productAmount) {
			this.productAmount = productAmount;
		}
		public double getProductPrice() {
			return productPrice;
		}
		public void setProductPrice(double productPrice) {
			this.productPrice = productPrice;
		}
		public double getMemberDiscount() {
			return memberDiscount;
		}
		public void setMemberDiscount(double memberDiscount) {
			this.memberDiscount = memberDiscount;
		}
		public double getEachProductDiscount() {
			return eachProductDiscount;
		}
		public void setEachProductDiscount(double eachProductDiscount) {
			this.eachProductDiscount = eachProductDiscount;
		}
		public double getVat() {
			return vat;
		}
		public void setVat(double vat) {
			this.vat = vat;
		}
		public double getServiceCharge() {
			return serviceCharge;
		}
		public void setServiceCharge(double serviceCharge) {
			this.serviceCharge = serviceCharge;
		}
		public double getTotalPrice() {
			return totalPrice;
		}
		public void setTotalPrice(double totalPrice) {
			this.totalPrice = totalPrice;
		}
		public long getOrderDetailId() {
			return orderDetailId;
		}
		public void setOrderDetailId(long orderDetailId) {
			this.orderDetailId = orderDetailId;
		}
		public long getTransactionId() {
			return transactionId;
		}
		public void setTransactionId(long transactionId) {
			this.transactionId = transactionId;
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
		public double getTotalRetailPrice() {
			return totalRetailPrice;
		}
		public void setTotalRetailPrice(double totalRetailPrice) {
			this.totalRetailPrice = totalRetailPrice;
		}
	}
}
