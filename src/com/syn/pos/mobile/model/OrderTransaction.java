package com.syn.pos.mobile.model;

import java.util.List;

public class OrderTransaction {
	private int transactionId;
	private int computerId;
	private int shopId;
	private String openTime;
	private String closeTime;
	private int openStaffId;
	private int updateStaffId;
	private int transactionStatusId;
	private int saleMode;
	private int documentTypeId;
	private int receiptNo;
	private int receiptId;
	private String saleDate;
	private double transactionVat;
	private double serviceCharge;
	private double serviceChargeVat;
	private double transactionVatAble;
	private int sessionId;
	private int voidStaffId;
	private String voidReason;
	private String voidDateTime;
	private int memberId;
	
	private List<OrderDetail> orderDetailLst;
	private List<OrderDiscountDetail> orderDiscountLst;
	
	public List<OrderDetail> getOrderDetailLst() {
		return orderDetailLst;
	}

	public void setOrderDetailLst(List<OrderDetail> orderDetailLst) {
		this.orderDetailLst = orderDetailLst;
	}

	public List<OrderDiscountDetail> getOrderDiscountLst() {
		return orderDiscountLst;
	}

	public void setOrderDiscountLst(List<OrderDiscountDetail> orderDiscountLst) {
		this.orderDiscountLst = orderDiscountLst;
	}

	public int getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(int transactionId) {
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

	public int getUpdateStaffId() {
		return updateStaffId;
	}

	public void setUpdateStaffId(int updateStaffId) {
		this.updateStaffId = updateStaffId;
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

	public int getReceiptNo() {
		return receiptNo;
	}

	public void setReceiptNo(int receiptNo) {
		this.receiptNo = receiptNo;
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

	public String getVoidDateTime() {
		return voidDateTime;
	}

	public void setVoidDateTime(String voidDateTime) {
		this.voidDateTime = voidDateTime;
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
		private int shopId;
		private int productId;
		private String productName;
		private int productTypeId;
		private int saleMode;
		private double productQty;
		private double pricePerUnit;
		private double totalRetailPrice;
		private double totalSalePrice;
		private double totalVatAmount;
		private double memberDiscountAmount;
		private double priceDiscountAmount;
		
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
		public int getProductTypeId() {
			return productTypeId;
		}
		public void setProductTypeId(int productTypeId) {
			this.productTypeId = productTypeId;
		}
		public int getSaleMode() {
			return saleMode;
		}
		public void setSaleMode(int saleMode) {
			this.saleMode = saleMode;
		}
		public double getProductQty() {
			return productQty;
		}
		public void setProductQty(double productQty) {
			this.productQty = productQty;
		}
		public double getPricePerUnit() {
			return pricePerUnit;
		}
		public void setPricePerUnit(double pricePerUnit) {
			this.pricePerUnit = pricePerUnit;
		}
		public double getTotalRetailPrice() {
			return totalRetailPrice;
		}
		public void setTotalRetailPrice(double totalRetailPrice) {
			this.totalRetailPrice = totalRetailPrice;
		}
		public double getTotalSalePrice() {
			return totalSalePrice;
		}
		public void setTotalSalePrice(double totalSalePrice) {
			this.totalSalePrice = totalSalePrice;
		}
		public double getTotalVatAmount() {
			return totalVatAmount;
		}
		public void setTotalVatAmount(double totalVatAmount) {
			this.totalVatAmount = totalVatAmount;
		}
		public double getMemberDiscountAmount() {
			return memberDiscountAmount;
		}
		public void setMemberDiscountAmount(double memberDiscountAmount) {
			this.memberDiscountAmount = memberDiscountAmount;
		}
		public double getPriceDiscountAmount() {
			return priceDiscountAmount;
		}
		public void setPriceDiscountAmount(double priceDiscountAmount) {
			this.priceDiscountAmount = priceDiscountAmount;
		}
	}
	
	// order discount detail class
	public static class OrderDiscountDetail {
		private int orderDetailId;
		private int transactionId;
		private int computerId;
		private int shopId;
		private int discountTypeId;
		private int promotionId;
		private double discountPrice;
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
		public int getDiscountTypeId() {
			return discountTypeId;
		}
		public void setDiscountTypeId(int discountTypeId) {
			this.discountTypeId = discountTypeId;
		}
		public int getPromotionId() {
			return promotionId;
		}
		public void setPromotionId(int promotionId) {
			this.promotionId = promotionId;
		}
		public double getDiscountPrice() {
			return discountPrice;
		}
		public void setDiscountPrice(double discountPrice) {
			this.discountPrice = discountPrice;
		}
	}
}
