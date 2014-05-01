package com.syn.mpos.database;

import java.util.List;

import com.syn.pos.BankName;
import com.syn.pos.CreditCardType;
import com.syn.pos.Payment.PayType;
import com.syn.pos.Payment.PaymentAmountButton;
import com.syn.pos.ShopData;
import com.syn.pos.ShopData.ComputerProperty;
import com.syn.pos.ShopData.GlobalProperty;
import com.syn.pos.ShopData.HeaderFooterReceipt;
import com.syn.pos.ShopData.Language;
import com.syn.pos.ShopData.ShopProperty;
import com.syn.pos.ShopData.Staff;

import android.content.Context;

public class MPOSShop{
	
	/**
	 * shop data source
	 */
	private ShopDataSource mShop;
	
	/**
	 * computer data source
	 */
	private ComputerDataSource mComputer;
	
	/**
	 * staff data source
	 */
	private StaffDataSource mStaff;
	
	/**
	 * global property data source
	 */
	private GlobalPropertyDataSource mGlobalProp;
	
	/**
	 * bank data source
	 */
	private BankDataSource mBank;
	
	/**
	 * credit card data source
	 */
	private CreditCardDataSource mCreditCard;
	
	/**
	 * payment data source 
	 */
	private PaymentDetailDataSource mPayment;
	
	/**
	 * payment button data source
	 */
	private PaymentAmountButtonDataSource mPaymentButton;
	
	/**
	 * language data source 
	 */
	private LanguageDataSource mLanguage;
	
	/**
	 * bill header footer data source
	 */
	private HeaderFooterReceiptDataSource mHeaderFooter;
	/**
	 * SyncSaleLog data source
	 */
	private SyncSaleLogDataSource mSyncSaleLog;
	
	public MPOSShop(Context context){
		mShop = new ShopDataSource(context.getApplicationContext());
		mComputer = new ComputerDataSource(context.getApplicationContext());
		mGlobalProp = new GlobalPropertyDataSource(context.getApplicationContext());
		mStaff = new StaffDataSource(context.getApplicationContext());
		mBank = new BankDataSource(context.getApplicationContext());
		mCreditCard = new CreditCardDataSource(context.getApplicationContext());
		mPayment = new PaymentDetailDataSource(context.getApplicationContext());
		mPaymentButton = new PaymentAmountButtonDataSource(context.getApplicationContext());
		mLanguage = new LanguageDataSource(context.getApplicationContext());
		mHeaderFooter = new HeaderFooterReceiptDataSource(context.getApplicationContext());
		mSyncSaleLog = new SyncSaleLogDataSource(context.getApplicationContext());
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
	 * @param typeId
	 * @return card type
	 */
	public String getCreditCardType(int typeId){
	 	return mCreditCard.getCreditCardType(typeId);
	}
	
	/**
	 * @return isMainComputer
	 */
	public boolean checkIsMainComputer(){
		return mComputer.checkIsMainComputer(getComputerId());
	}
	
	/**
	 * @return company vat type
	 */
	public int getCompanyVatType(){
		return getShop().getVatType();
	}
	
	/**
	 * @return company vat rate
	 */
	public double getCompanyVatRate(){
		return getShop().getCompanyVat();
	}
	
	/**
	 * @return ComputerProperty
	 */
	public ComputerProperty getComputer(){
		return mComputer.getComputerProperty();
	}
	
	/**
	 * @return GlobalPropertyDataSource
	 */
	public GlobalPropertyDataSource getGlobalProperty(){
		return mGlobalProp;
	}
	
	/**
	 * @return ShopProperty
	 */
	public ShopProperty getShop(){
		return mShop.getShopProperty();
	}

	/**
	 * @param staffId
	 * @return ShopData.Staff
	 */
	public ShopData.Staff getStaff(int staffId){
		return mStaff.getStaff(staffId);
	}
	
	/**
	 * Update status of sync sale log
	 * @param saleDate
	 * @param status
	 */
	public void updateSyncSaleLog(String saleDate, int status){
		mSyncSaleLog.updateSyncSaleLog(saleDate, status);
	}
	
	/**
	 * @param lineType
	 * @return List<ShopData.HeaderFooterReceipt>
	 */
	public List<ShopData.HeaderFooterReceipt> listHeaderFooterReceipt(int lineType){
		return mHeaderFooter.listHeaderFooter(lineType);
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
