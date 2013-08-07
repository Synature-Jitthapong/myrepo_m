package com.syn.pos.mobile.mpos.dao;

public abstract class POSUtil implements IPOS {
	
	public double calculateVat(double productPrice, double productAmount, double vat){
		return productPrice * productAmount * toVatPercent(vat);
	}
	
	protected double toVatPercent(double vat){
		return vat / 100;
	}

	@Override
	public void payment() {
		
	}

	@Override
	public void discount() {
		
	}
}
