package com.syn.mpos.db;

/**
 * 
 * @author j1tth4
 *
 */
public abstract class Util {
	
	protected float calculateVat(float productPrice, float productAmount, float vat){
		float vatAmount = productPrice * productAmount * toVatPercent(vat);
		return vatAmount;
	}
	
	protected float toVatPercent(float vat){
		return vat / 100;
	}
}
