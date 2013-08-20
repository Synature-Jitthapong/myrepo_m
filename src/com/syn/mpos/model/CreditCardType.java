package com.syn.mpos.model;

public class CreditCardType {
	private int creditCardTypeId;
	private String creditCardTypeName;
	
	public CreditCardType(){
		
	}
	
	public CreditCardType(int type, String name){
		this.creditCardTypeId = type;
		this.creditCardTypeName = name;
	}

	public int getCreditCardTypeId() {
		return creditCardTypeId;
	}

	public void setCreditCardTypeId(int creditCardTypeId) {
		this.creditCardTypeId = creditCardTypeId;
	}

	public String getCreditCardTypeName() {
		return creditCardTypeName;
	}

	public void setCreditCardTypeName(String creditCardTypeName) {
		this.creditCardTypeName = creditCardTypeName;
	}

	@Override
	public String toString() {
		return creditCardTypeName;
	}
	
}
