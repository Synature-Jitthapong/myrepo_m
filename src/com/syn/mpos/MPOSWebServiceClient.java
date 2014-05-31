package com.syn.mpos;

import java.lang.reflect.Type;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syn.mpos.dao.Bank;
import com.syn.mpos.dao.Computer;
import com.syn.mpos.dao.CreditCard;
import com.syn.mpos.dao.Formater;
import com.syn.mpos.dao.HeaderFooterReceipt;
import com.syn.mpos.dao.Language;
import com.syn.mpos.dao.PaymentAmountButton;
import com.syn.mpos.dao.PaymentDetail;
import com.syn.mpos.dao.Products;
import com.syn.mpos.dao.Shop;
import com.syn.mpos.dao.Staffs;
import com.synature.pos.MenuGroups;
import com.synature.pos.ProductGroups;
import com.synature.pos.ShopData;
import com.synature.pos.WebServiceResult;
import com.synature.util.FileManager;
import com.synature.util.Logger;

import android.content.Context;

public class MPOSWebServiceClient {

	public void authenDevice(final Context context, final AuthenDeviceListener listener){
		final String url = MPOSApplication.getFullUrl(context);
		new AuthenDevice(context, listener).execute(url);
	}
	
	public void loadShopData(final Context context, final int shopId, final ProgressListener listener){
		
		final String url = MPOSApplication.getFullUrl(context);

		new LoadShop(context, shopId, new LoadShopListener() {

			@Override
			public void onPre() {
				listener.onPre();
			}

			@Override
			public void onPost() {
			}

			@Override
			public void onError(String msg) {
				listener.onError(msg);
			}

			@Override
			public void onPost(ShopData sd) {
				Shop shop = new Shop(context.getApplicationContext());
				Computer computer = new Computer(context.getApplicationContext());
				Formater format = new Formater(context.getApplicationContext());
				Staffs staff = new Staffs(context.getApplicationContext());
				Language lang = new Language(context.getApplicationContext());
				HeaderFooterReceipt hf = new HeaderFooterReceipt(context.getApplicationContext());
				Bank bank = new Bank(context.getApplicationContext());
				CreditCard cd = new CreditCard(context.getApplicationContext());
				PaymentDetail pd = new PaymentDetail(context.getApplicationContext());
				PaymentAmountButton pb = new PaymentAmountButton(context.getApplicationContext());
				try {
					shop.insertShopProperty(sd.getShopProperty());
					computer.insertComputer(sd.getComputerProperty());
					format.insertProperty(sd.getGlobalProperty());
					staff.insertStaff(sd.getStaffs());
					lang.insertLanguage(sd.getLanguage());
					hf.insertHeaderFooterReceipt(sd.getHeaderFooterReceipt());
					bank.insertBank(sd.getBankName());
					cd.insertCreditCardType(sd.getCreditCardType());
					pd.insertPaytype(sd.getPayType());
					pb.insertPaymentAmountButton(sd.getPaymentAmountButton());
					listener.onPost();
				} catch (Exception e) {
					Logger.appendLog(context, MPOSApplication.LOG_DIR, 
							MPOSApplication.LOG_FILE_NAME, 
							"Error when add shop data : " + e.getMessage());
					listener.onError(e.getMessage());
				}
			}
		}).execute(url);
	}
	
	// load product
	public void loadProductData(final Context context, final int shopId,
			final ProgressListener progressListener){
		
		final String url = MPOSApplication.getFullUrl(context);

		new LoadMenu(context, shopId, new LoadMenuListener() {

			@Override
			public void onPre() {
				progressListener.onPre();
			}

			@Override
			public void onPost() {
			}

			@Override
			public void onError(String msg) {
				progressListener.onError(msg);
			}

			@Override
			public void onPost(final MenuGroups mgs) {
				new LoadProduct(context, shopId, new LoadProductListener() {

					@Override
					public void onPre() {
					}

					@Override
					public void onPost() {
					}

					@Override
					public void onError(String msg) {
						progressListener.onError(msg);
					}

					@Override
					public void onPost(ProductGroups pgs) {
						Products pd = new Products(context.getApplicationContext());
						try {
							pd.insertProductGroup(pgs.getProductGroup(), mgs.getMenuGroup());
							pd.insertProductDept(pgs.getProductDept(), mgs.getMenuDept());
							pd.insertProducts(pgs.getProduct(), mgs.getMenuItem());
							pd.insertPComponentGroup(pgs.getPComponentGroup());
							pd.insertProductComponent(pgs.getPComponentSet());
							
							// clear all menu picture
							FileManager fm = new FileManager(context.getApplicationContext(), MPOSApplication.IMG_DIR);
							fm.clear();

							progressListener.onPost();
						} catch (Exception e) {
							Logger.appendLog(context.getApplicationContext(), MPOSApplication.LOG_DIR, 
								MPOSApplication.LOG_FILE_NAME, 
								"Error when add product data : " + e.getMessage());
							progressListener.onError(e.getMessage());
						}
					}
				}).execute(url);
			}
		}).execute(url);
	}
	
	public static class SendPartialSaleTransaction extends SendSaleTransaction{

		public SendPartialSaleTransaction(Context context, int staffId, int shopId, int computerId,
				String jsonSale, ProgressListener listener) {
			super(context, MPOSMainService.SEND_PARTIAL_SALE_TRANS_METHOD, 
					staffId, shopId, computerId, jsonSale, listener);
		}
	}
	
	// send sale transaction
	public static class SendSaleTransaction extends MPOSMainService{
		
		private ProgressListener mListener;
		
		public SendSaleTransaction(Context context, String method, int shopId, int computerId,
				int staffId, String jsonSale, ProgressListener listener) {
			super(context, method);
			mListener = listener;

			// shopId
			mProperty = new PropertyInfo();
			mProperty.setName(SHOP_ID_PARAM);
			mProperty.setValue(shopId);
			mProperty.setType(int.class);
			mSoapRequest.addProperty(mProperty);
			// computerId
			mProperty = new PropertyInfo();
			mProperty.setName(COMPUTER_ID_PARAM);
			mProperty.setValue(computerId);
			mProperty.setType(int.class);
			mSoapRequest.addProperty(mProperty);
			// staffId
			mProperty = new PropertyInfo();
			mProperty.setName(STAFF_ID_PARAM);
			mProperty.setValue(staffId);
			mProperty.setType(int.class);
			mSoapRequest.addProperty(mProperty);
			// json sale
			mProperty = new PropertyInfo();
			mProperty.setName(JSON_SALE_PARAM);
			mProperty.setValue(jsonSale);
			mProperty.setType(String.class);
			mSoapRequest.addProperty(mProperty);
		}
		
		@Override
		protected void onPostExecute(String result) {
			try {
				WebServiceResult ws = (WebServiceResult) super.toServiceObject(result);
				if(ws.getiResultID() == WebServiceResult.SUCCESS_STATUS){
					mListener.onPost();
				}else{
					mListener.onError(ws.getSzResultData().equals("") ? result :
						ws.getSzResultData());
				}
			} catch (Exception e) {
				mListener.onError(result);
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			mListener.onPre();
		}
	}
	
	// load shop data
	private class LoadShop extends MPOSMainService{
		private LoadShopListener mListener;
		
		public LoadShop(Context context, int shopId, LoadShopListener listener) {
			super(context, LOAD_SHOP_METHOD);
			
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
			Type type = new TypeToken<ShopData>() {}.getType();
			try {
				ShopData shopData = (ShopData) gson.fromJson(result, type);
				mListener.onPost(shopData);
			} catch (Exception e) {
				mListener.onError(result);
			}
		}

		@Override
		protected void onPreExecute() {
			mListener.onPre();
		}
	}
	
	// load products
	private class LoadProduct extends MPOSMainService{
		private LoadProductListener mListener;
		
		public LoadProduct(Context context, int shopId, LoadProductListener listener) {
			super(context, LOAD_PRODUCT_METHOD);
			
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
			Type type = new TypeToken<ProductGroups>() {}.getType();
			ProductGroups productData;
			try {
				productData = (ProductGroups) gson.fromJson(result, type);
				mListener.onPost(productData);
			} catch (Exception e) {
				mListener.onError(result);
				e.printStackTrace();
			}
			
		}

		@Override
		protected void onPreExecute() {
			mListener.onPre();
		}
	}
	
	// load menu data
	private class LoadMenu extends MPOSMainService{
		private LoadMenuListener mListener;
		
		public LoadMenu(Context context, int shopId, LoadMenuListener listener) {
			super(context, LOAD_MENU_METHOD);
			
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
			Type type = new TypeToken<MenuGroups>() {}.getType();
			try {
				MenuGroups menuGroup = (MenuGroups) gson.fromJson(result, type);
				mListener.onPost(menuGroup);
			} catch (Exception e) {
				mListener.onError(result);
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			mListener.onPre();
		}
	}

	// check authen shop
	private class AuthenDevice extends MPOSMainService{
		private AuthenDeviceListener mListener;
		
		public AuthenDevice(Context context, AuthenDeviceListener listener) {
			super(context, CHECK_DEVICE_METHOD);
			mListener = listener;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				int shopId = Integer.parseInt(result);
				if(shopId > 0)
					mListener.onPost(shopId);
				else if(shopId == 0)
					mListener.onError(mContext.getString(R.string.device_not_register));
				else if(shopId == -1)
					mListener.onError(mContext.getString(R.string.computer_setting_not_valid));
			} catch (NumberFormatException e) {
				this.mListener.onError(result);
			}
		}

		@Override
		protected void onPreExecute() {
			mListener.onPre();
		}
	}	
	
	public static interface LoadMenuListener extends ProgressListener{
		void onPost(MenuGroups mgs);
	}
	
	public static interface LoadProductListener extends ProgressListener{
		void onPost(ProductGroups pgs);
	}
	
	public static interface LoadShopListener extends ProgressListener{
		void onPost(ShopData sd);
	}
	
	public static interface AuthenDeviceListener extends ProgressListener{
		void onPost(int shopId);
	}
}
