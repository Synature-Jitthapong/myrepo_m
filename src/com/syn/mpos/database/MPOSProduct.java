package com.syn.mpos.database;

import java.util.List;

import com.syn.pos.MenuGroups.MenuDept;
import com.syn.pos.MenuGroups.MenuGroup;
import com.syn.pos.MenuGroups.MenuItem;
import com.syn.pos.ProductGroups.PComponentGroup;
import com.syn.pos.ProductGroups.ProductComponent;
import com.syn.pos.ProductGroups.ProductDept;
import com.syn.pos.ProductGroups.ProductGroup;
import com.syn.pos.ProductGroups.Products;

import android.content.Context;

public class MPOSProduct {

	/*
	 * product data source
	 */
	private ProductsDataSource mProduct;
	
	public MPOSProduct(Context context){
		mProduct = new ProductsDataSource(context);
	}

	/**
	 * @param productId
	 * @return product vat type
	 */
	public int getVatType(int productId){
		return getProduct(productId).getVatType();
	}
	
	/**
	 * @param productId
	 * @return product vat rate
	 */
	public double getVatRate(int productId){
		return getProduct(productId).getVatRate();
	}
	
	/**
	 * @param productDeptId
	 * @return ProductsDataSource.ProductDept
	 */
	public ProductsDataSource.ProductDept getProductDept(int productDeptId){
		return mProduct.getProductDept(productDeptId);
	}
	
	/**
	 * @param productId
	 * @return ProductsDataSource.Product
	 */
	public ProductsDataSource.Product getProduct(int productId){
		return mProduct.getProduct(productId);
	}
	
	/**
	 * @return List<ProductsDataSource.ProductDept>
	 */
	public List<ProductsDataSource.ProductDept> listProductDept(){
		return mProduct.listProductDept();
	}
	
	/**
	 * @param productDeptId
	 * @return List<ProductsDataSource.Product>
	 */
	public List<ProductsDataSource.Product> listProduct(int productDeptId){
		return mProduct.listProduct(productDeptId);
	}
	
	/**
	 * @param pCompGroupLst
	 */
	public void addProductComponentGroup(List<PComponentGroup> pCompGroupLst){
		mProduct.insertPComponentGroup(pCompGroupLst);
	}
	
	/**
	 * @param pCompLst
	 */
	public void addProductComponent(List<ProductComponent> pCompLst){
		mProduct.insertProductComponent(pCompLst);
	}
	
	/**
	 * @param pgLst
	 * @param mgLst
	 */
	public void addProductGroup(List<ProductGroup> pgLst, List<MenuGroup> mgLst){
		mProduct.insertProductGroup(pgLst, mgLst);
	}
	
	/**
	 * @param pdLst
	 * @param mdLst
	 */
	public void addProductDept(List<ProductDept> pdLst, List<MenuDept> mdLst){
		mProduct.insertProductDept(pdLst, mdLst);
	}
	
	/**
	 * @param pLst
	 * @param mLst
	 */
	public void addProduct(List<Products> pLst, List<MenuItem> mLst){
		mProduct.insertProducts(pLst, mLst);
	}
}
