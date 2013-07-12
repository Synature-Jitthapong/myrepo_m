package com.syn.pos.mobile.model;

import java.util.List;

public class Payment {
	private List<PayType> PayType;
	private List<PaymentAmountButton> PaymentAmountButton;
	
	public static class PaymentAmountButton{
		private int PaymentAmountID;
		private double PaymentAmount;
		
		public int getPaymentAmountID() {
			return PaymentAmountID;
		}
		public void setPaymentAmountID(int paymentAmountID) {
			PaymentAmountID = paymentAmountID;
		}
		public double getPaymentAmount() {
			return PaymentAmount;
		}
		public void setPaymentAmount(double paymentAmount) {
			PaymentAmount = paymentAmount;
		}
	}
	
	public static class PaymentDetail{
		private int PaymentDetailID;
		private int TransactionID;
		private int ComputerID;
		private int ShopID;
		private int PayTypeID;
		private double PayAmount;
		private String CreaditCardNo;
		private int ExpireMonth;
		private int ExpireYear;
		private int BankNameID;
		private int CreditCardType;
		private String Remark;
		
		public int getPaymentDetailID() {
			return PaymentDetailID;
		}
		public void setPaymentDetailID(int paymentDetailID) {
			PaymentDetailID = paymentDetailID;
		}
		public int getTransactionID() {
			return TransactionID;
		}
		public void setTransactionID(int transactionID) {
			TransactionID = transactionID;
		}
		public int getComputerID() {
			return ComputerID;
		}
		public void setComputerID(int computerID) {
			ComputerID = computerID;
		}
		public int getShopID() {
			return ShopID;
		}
		public void setShopID(int shopID) {
			ShopID = shopID;
		}
		public int getPayTypeID() {
			return PayTypeID;
		}
		public void setPayTypeID(int payTypeID) {
			PayTypeID = payTypeID;
		}
		public double getPayAmount() {
			return PayAmount;
		}
		public void setPayAmount(double payAmount) {
			PayAmount = payAmount;
		}
		public String getCreaditCardNo() {
			return CreaditCardNo;
		}
		public void setCreaditCardNo(String creaditCardNo) {
			CreaditCardNo = creaditCardNo;
		}
		public int getExpireMonth() {
			return ExpireMonth;
		}
		public void setExpireMonth(int expireMonth) {
			ExpireMonth = expireMonth;
		}
		public int getExpireYear() {
			return ExpireYear;
		}
		public void setExpireYear(int expireYear) {
			ExpireYear = expireYear;
		}
		public int getBankNameID() {
			return BankNameID;
		}
		public void setBankNameID(int bankNameID) {
			BankNameID = bankNameID;
		}
		public int getCreditCardType() {
			return CreditCardType;
		}
		public void setCreditCardType(int creditCardType) {
			CreditCardType = creditCardType;
		}
		public String getRemark() {
			return Remark;
		}
		public void setRemark(String remark) {
			Remark = remark;
		}
	}
	
	public static class PayType{
		private long PayTypeID;
		private String PayTypeCode;
		private String PayTypeName;
		
		public long getPayTypeID() {
			return PayTypeID;
		}
		public void setPayTypeID(long payTypeID) {
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
	}

	
	public List<PayType> getPayType() {
		return PayType;
	}
	

	public void setPayType(List<PayType> payType) {
		PayType = payType;
	}

	public List<PaymentAmountButton> getPaymentAmountButton() {
		return PaymentAmountButton;
	}

	public void setPaymentAmountButton(List<PaymentAmountButton> paymentAmountButton) {
		PaymentAmountButton = paymentAmountButton;
	}
}
