package com.synature.mpos;

import org.ksoap2.serialization.PropertyInfo;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.synature.mpos.database.Bank;
import com.synature.mpos.database.Computer;
import com.synature.mpos.database.CreditCard;
import com.synature.mpos.database.Formater;
import com.synature.mpos.database.HeaderFooterReceipt;
import com.synature.mpos.database.Language;
import com.synature.mpos.database.MenuComment;
import com.synature.mpos.database.PaymentAmountButton;
import com.synature.mpos.database.PaymentDetail;
import com.synature.mpos.database.ProductPrice;
import com.synature.mpos.database.Products;
import com.synature.mpos.database.PromotionDiscount;
import com.synature.mpos.database.Shop;
import com.synature.mpos.database.Staffs;
import com.synature.mpos.database.SyncHistory;
import com.synature.pos.MasterData;
import com.synature.util.FileManager;
import com.synature.util.Logger;

public class MasterDataLoader extends MPOSServiceBase{
	
	public static final String LOAD_MASTER_METHOD = "WSmPOS_JSON_LoadShopMasterData";
	
	/**
	 * Total operation
	 */
	public static final int TOTAL_OPT = 21;
	
	private WebServiceWorkingListener mListener;

	/**
	 * @param context
	 * @param listener
	 */
	public MasterDataLoader(Context context, int shopId, WebServiceWorkingListener listener) {
		super(context, LOAD_MASTER_METHOD);
		// shopId
		mProperty = new PropertyInfo();
		mProperty.setName(SHOP_ID_PARAM);
		mProperty.setValue(shopId);
		mProperty.setType(int.class);
		mSoapRequest.addProperty(mProperty);
		
		mListener = listener;
	}

	@Override
	protected void onPostExecute(String result) {
		Gson gson = new Gson();
		try {
			MasterData master = gson.fromJson(result, MasterData.class);
			updateMasterData(master);
		} catch (JsonSyntaxException e) {
			if(mListener != null)
				mListener.onError(result);
		}
	}

	@Override
	protected void onPreExecute() {
		if(mListener != null)
			mListener.onPreExecute();
	}

	private void updateMasterData(MasterData master){
		SyncHistory sync = new SyncHistory(mContext);
		Shop shop = new Shop(mContext);
		Computer computer = new Computer(mContext);
		Formater format = new Formater(mContext);
		Staffs staff = new Staffs(mContext);
		Language lang = new Language(mContext);
		HeaderFooterReceipt hf = new HeaderFooterReceipt(mContext);
		Bank bank = new Bank(mContext);
		CreditCard cd = new CreditCard(mContext);
		PaymentDetail pd = new PaymentDetail(mContext);
		PaymentAmountButton pb = new PaymentAmountButton(mContext);
		Products p = new Products(mContext);
		ProductPrice pp = new ProductPrice(mContext);
		MenuComment mc = new MenuComment(mContext);
		PromotionDiscount promo = new PromotionDiscount(mContext);
		try {
			shop.insertShopProperty(master.getShopProperty());
			computer.insertComputer(master.getComputerProperty());
			format.insertProperty(master.getGlobalProperty());
			staff.insertStaff(master.getStaffs());
			lang.insertLanguage(master.getLanguage());
			hf.insertHeaderFooterReceipt(master.getHeaderFooterReceipt());
			bank.insertBank(master.getBankName());
			cd.insertCreditCardType(master.getCreditCardType());
			pd.insertPaytype(master.getPayType());
			pb.insertPaymentAmountButton(master.getPaymentAmountButton());
			p.insertProductGroup(master.getProductGroup());
			p.insertProductDept(master.getProductDept());
			p.insertProducts(master.getProducts());
			pp.insertProductPrice(master.getProductPrice());
			p.insertPComponentGroup(master.getPComponentGroup());
			p.insertProductComponent(master.getProductComponent());
			mc.insertMenuComment(master.getMenuComment());
			mc.insertMenuCommentGroup(master.getMenuCommentGroup());
			mc.insertMenuFixComment(master.getMenuFixComment());
			promo.insertPromotionPriceGroup(master.getPromotionPriceGroup());
			promo.insertPromotionProductDiscount(master.getPromotionProductDiscount());
			
			// clear all menu picture
			FileManager fm = new FileManager(mContext, Utils.IMG_DIR);
			fm.clear();
			// log sync history
			sync.insertSyncLog(SyncHistory.SYNC_STATUS_SUCCESS);
			if(mListener != null)
				mListener.onPostExecute();
		} catch (Exception e) {
			// log sync history
			sync.insertSyncLog(SyncHistory.SYNC_STATUS_FAIL);
			Logger.appendLog(mContext, Utils.LOG_PATH, 
					Utils.LOG_FILE_NAME, 
					"Error when add shop data : " + e.getMessage());
			if(mListener != null)
				mListener.onError(e.getMessage());
		}
	}
}
