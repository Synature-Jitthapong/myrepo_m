package com.syn.pos.mobile.mpos.dao;

import java.math.BigDecimal;

public abstract class POSUtil implements IPOS {
	
	public BigDecimal calculateVat(float productPrice, float productAmount, float vat){
		return new BigDecimal(productPrice * productAmount * toVatPercent(vat));
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
