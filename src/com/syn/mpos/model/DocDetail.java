package com.syn.mpos.model;

public class DocDetail{
	private int docDetailId;
	private int documentId;
	private int shopId;
	private int materialId;
	private String unitName;
	private double materialQty;
	private double materialPricePerUnit;
	private double materialNetPrice;
	private int materialTaxType;
	private double materialTaxPrice;
	
	public int getDocDetailId() {
		return docDetailId;
	}
	public void setDocDetailId(int docDetailId) {
		this.docDetailId = docDetailId;
	}
	public int getDocumentId() {
		return documentId;
	}
	public void setDocumentId(int documentId) {
		this.documentId = documentId;
	}
	public int getShopId() {
		return shopId;
	}
	public void setShopId(int shopId) {
		this.shopId = shopId;
	}
	public int getMaterialId() {
		return materialId;
	}
	public void setMaterialId(int materialId) {
		this.materialId = materialId;
	}
	public String getUnitName() {
		return unitName;
	}
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
	public double getMaterialQty() {
		return materialQty;
	}
	public void setMaterialQty(double materialQty) {
		this.materialQty = materialQty;
	}
	public double getMaterialPricePerUnit() {
		return materialPricePerUnit;
	}
	public void setMaterialPricePerUnit(double materialPricePerUnit) {
		this.materialPricePerUnit = materialPricePerUnit;
	}
	public double getMaterialNetPrice() {
		return materialNetPrice;
	}
	public void setMaterialNetPrice(double materialNetPrice) {
		this.materialNetPrice = materialNetPrice;
	}
	public int getMaterialTaxType() {
		return materialTaxType;
	}
	public void setMaterialTaxType(int materialTaxType) {
		this.materialTaxType = materialTaxType;
	}
	public double getMaterialTaxPrice() {
		return materialTaxPrice;
	}
	public void setMaterialTaxPrice(double materialTaxPrice) {
		this.materialTaxPrice = materialTaxPrice;
	}
}
