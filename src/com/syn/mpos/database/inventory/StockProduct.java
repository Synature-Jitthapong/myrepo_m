package com.syn.mpos.database.inventory;

public class StockProduct {
	private int id;
	private int proId;
	private String code;
	private String name;
	private float init;
	private float receive;
	private float sale;
	private float variance;
	private float diff;
	private float currQty;
	private float countQty;
	private float unitPrice;
	private String unitName;

	public StockProduct() {

	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public int getProId() {
		return proId;
	}

	public void setProId(int proId) {
		this.proId = proId;
	}

	public float getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(float unitPrice) {
		this.unitPrice = unitPrice;
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
