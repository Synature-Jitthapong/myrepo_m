package com.syn.mpos.database;

import java.util.List;

import com.syn.pos.BankName;
import com.syn.pos.CreditCardType;
import com.syn.pos.Payment.PayType;
import com.syn.pos.Payment.PaymentAmountButton;
import com.syn.pos.ShopData.ComputerProperty;
import com.syn.pos.ShopData.GlobalProperty;
import com.syn.pos.ShopData.HeaderFooterReceipt;
import com.syn.pos.ShopData.Language;
import com.syn.pos.ShopData.ShopProperty;
import com.syn.pos.ShopData.Staff;

import android.content.Context;

public class MPOSShopManager{
	
	/*
	 * shop data source
	 */
	private ShopDataSource mShop;
	
	/*
	 * computer data source
	 */
	private ComputerDataSource mComputer;
	
	/*
	 * staff data source
	 */
	private StaffDataSource mStaff;
	
	/*
	 * global property data source
	 */
	private GlobalPropertyDataSource mGlobalProp;
	
	/*
	 * bank data source
	 */
	private BankDataSource mBank;
	
	/*
	 * credit card data source
	 */
	private CreditCardDataSource mCreditCard;
	
	/*
	 * payment data source 
	 */
	private PaymentDetailDataSource mPayment;
	
	/*
	 * payment button data source
	 */
	private PaymentAmountButtonDataSource mPaymentButton;
	
	/*
	 * language data source 
	 */
	private LanguageDataSource mLanguage;
	
	/*
	 * bill header footer data source
	 */
	private HeaderFooterReceiptDataSource mHeaderFooter;
	
	public MPOSShopManager(Context context){
		mShop = new ShopDataSource(context);
		mComputer = new ComputerDataSource(context);
		mGlobalProp = new GlobalPropertyDataSource(context);
		mStaff = new StaffDataSource(context);
		mBank = new BankDataSource(context);
		mCreditCard = new CreditCardDataSource(context);
		mPayment = new PaymentDetailDataSource(context);
		mPaymentButton = new PaymentAmountButtonDataSource(context);
		mLanguage = new LanguageDataSource(context);
		mHeaderFooter = new HeaderFooterReceiptDataSource(context);
	}
	
	/**
	 * @return computerId
	 */
	public int getComputerId(){
		return getComputer().getComputerID();
	}
	
	/**
	 * @return shopId
	 */
	public int getShopId(){
		return getShop().getShopID();
	}
	
	/**
	 * @return ComputerProperty
	 */
	public ComputerProperty getComputer(){
		return mComputer.getComputerProperty();
	}
	
	/**
	 * @return ShopProperty
	 */
	public ShopProperty getShop(){
		return mShop.getShopProperty();
	}

	/**
	 * @param headerFooterLst
	 */
	public void addHeaderFooter(List<HeaderFooterReceipt> headerFooterLst){
		mHeaderFooter.addHeaderFooterReceipt(headerFooterLst);
	}
	
	/**
	 * @param langLst
	 */
	public void addLanguage(List<Language> langLst){
		mLanguage.insertLanguage(langLst);
	}
	
	/**
	 * @param paymentAmountLst
	 */
	public void addPaymentButton(List<PaymentAmountButton> paymentAmountLst){
		mPaymentButton.insertPaymentAmountButton(paymentAmountLst);
	}
	
	/**
	 * @param payTypeLst
	 */
	public void addPaymentType(List<PayType> payTypeLst){
		mPayment.insertPaytype(payTypeLst);
	}
	
	/**
	 * @param creditCardLst
	 */
	public void addCreditCard(List<CreditCardType> creditCardLst){
		mCreditCard.insertCreditCardType(creditCardLst);
	}
	
	/**
	 * @param bankLst
	 */
	public void addBank(List<BankName> bankLst){
		mBank.insertBank(bankLst);
	}
	
	/**
	 * @param staffLst
	 */
	public void addStaff(List<Staff> staffLst){
		mStaff.insertStaff(staffLst);
	}
	
	/**
	 * @param globalLst
	 */
	public void addGlobalProperty(List<GlobalProperty> globalLst){
		mGlobalProp.insertProperty(globalLst);
	}
	
	/**
	 * @param compLst
	 */
	public void addComputer(List<ComputerProperty> compLst){
		mComputer.insertComputer(compLst);
	}
	
	/**
	 * @param shopPropLst
	 */
	public void addShop(List<ShopProperty> shopPropLst){
		mShop.insertShopProperty(shopPropLst);
	}
}
