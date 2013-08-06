package com.syn.pos.mobile.mpos.dao;

public abstract class Vat {
	public double calculateVat(double productPrice, double productAmount, double vatPercent){
		return productPrice * productAmount * vatPercent;
	}
}
