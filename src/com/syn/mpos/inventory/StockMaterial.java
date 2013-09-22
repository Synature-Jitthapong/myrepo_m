package com.syn.mpos.inventory;

public class StockMaterial {
	private int id;
	private String code;
	private String name;
	private float init;
	private float receive;
	private float sale;
	private float variance;
	private float diff;
	private float currQty;
	private float countQty;
	private float pricePerUnit;
	private float netPrice;
	private float taxType;
	private float taxPrice;

	public StockMaterial() {

	}

	public StockMaterial(int id, String code, String name, float currQty,
			float countQty) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.currQty = currQty;
		this.countQty = countQty;
	}

	public StockMaterial(int id, String code, String name, float init,
			float receive, float sale, float variance, float diff) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.init = init;
		this.receive = receive;
		this.sale = sale;
		this.variance = variance;
		this.diff = diff;
	}
	
	public StockMaterial(int id, String code, String name, float qty,
			float pricePerUnit, float netPrice, int taxType, float taxPrice) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.currQty = qty;
		this.pricePerUnit = pricePerUnit;
		this.netPrice = netPrice;
		this.taxType = taxType;
		this.taxPrice = taxPrice;
	}

	public float getPricePerUnit() {
		return pricePerUnit;
	}

	public void setPricePerUnit(float pricePerUnit) {
		this.pricePerUnit = pricePerUnit;
	}

	public float getNetPrice() {
		return netPrice;
	}

	public void setNetPrice(float netPrice) {
		this.netPrice = netPrice;
	}

	public float getTaxType() {
		return taxType;
	}

	public void setTaxType(float taxType) {
		this.taxType = taxType;
	}

	public float getTaxPrice() {
		return taxPrice;
	}

	public void setTaxPrice(float taxPrice) {
		this.taxPrice = taxPrice;
	}

	public float getInit() {
		return init;
	}

	public void setInit(float init) {
		this.init = init;
	}

	public float getReceive() {
		return receive;
	}

	public void setReceive(float receive) {
		this.receive = receive;
	}

	public float getSale() {
		return sale;
	}

	public void setSale(float sale) {
		this.sale = sale;
	}

	public float getVariance() {
		return variance;
	}

	public void setVariance(float variance) {
		this.variance = variance;
	}

	public float getDiff() {
		return diff;
	}

	public void setDiff(float diff) {
		this.diff = diff;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getCurrQty() {
		return currQty;
	}

	public void setCurrQty(float currQty) {
		this.currQty = currQty;
	}

	public float getCountQty() {
		return countQty;
	}

	public void setCountQty(float countQty) {
		this.countQty = countQty;
	}
}
