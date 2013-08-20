package com.syn.mpos.model;

public class BankName {
	private int bankNameId;
	private String bankName;
	
	public BankName(){
		
	}
	
	public BankName(int id, String name){
		this.bankNameId = id;
		this.bankName = name;
	}
	
	public int getBankNameId() {
		return bankNameId;
	}
	public void setBankNameId(int bankNameId) {
		this.bankNameId = bankNameId;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	@Override
	public String toString() {
		return bankName;
	}
}
