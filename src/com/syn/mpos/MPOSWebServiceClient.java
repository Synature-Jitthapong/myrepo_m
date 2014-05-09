package com.syn.mpos;

import java.lang.reflect.Type;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.reflect.TypeToken;
import com.j1tth4.util.FileManager;
import com.j1tth4.util.JSONUtil;
import com.j1tth4.util.Logger;
import com.syn.mpos.database.BankDataSource;
import com.syn.mpos.database.ComputerDataSource;
import com.syn.mpos.database.CreditCardDataSource;
import com.syn.mpos.database.GlobalPropertyDataSource;
import com.syn.mpos.database.HeaderFooterReceiptDataSource;
import com.syn.mpos.database.LanguageDataSource;
import com.syn.mpos.database.PaymentAmountButtonDataSource;
import com.syn.mpos.database.PaymentDetailDataSource;
import com.syn.mpos.database.ProductsDataSource;
import com.syn.mpos.database.ShopDataSource;
import com.syn.mpos.database.StaffDataSource;
import com.syn.pos.MenuGroups;
import com.syn.pos.ProductGroups;
import com.syn.pos.ShopData;
import com.syn.pos.WebServiceResult;

import android.content.Context;

public class MPOSWebServiceClient {

	public void loadShopData(final Context context, final AuthenDeviceListener progressListener){
		
		final String url = MPOSApplication.getFullUrl(context);

		final AuthenDeviceListener authenDeviceListener = new AuthenDeviceListener() {

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
			public void onPost(final int shopId) {
				new LoadShop(context, shopId, new LoadShopListener() {

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
					public void onPost(ShopData sd) {
						ShopDataSource shop = new ShopDataSource(context.getApplicationContext());
						ComputerDataSource computer = new ComputerDataSource(context.getApplicationContext());
						GlobalPropertyDataSource global = new GlobalPropertyDataSource(context.getApplicationContext());
						StaffDataSource staff = new StaffDataSource(context.getApplicationContext());
						LanguageDataSource lang = new LanguageDataSource(context.getApplicationContext());
						HeaderFooterReceiptDataSource hf = new HeaderFooterReceiptDataSource(context.getApplicationContext());
						BankDataSource bank = new BankDataSource(context.getApplicationContext());
						CreditCardDataSource cd = new CreditCardDataSource(context.getApplicationContext());
						PaymentDetailDataSource pd = new PaymentDetailDataSource(context.getApplicationContext());
						PaymentAmountButtonDataSource pb = new PaymentAmountButtonDataSource(context.getApplicationContext());
						try {
							shop.insertShopProperty(sd.getShopProperty());
							computer.insertComputer(sd.getComputerProperty());
							global.insertProperty(sd.getGlobalProperty());
							staff.insertStaff(sd.getStaffs());
							lang.insertLanguage(sd.getLanguage());
							hf.insertHeaderFooterReceipt(sd.getHeaderFooterReceipt());
							bank.insertBank(sd.getBankName());
							cd.insertCreditCardType(sd.getCreditCardType());
							pd.insertPaytype(sd.getPayType());
							pb.insertPaymentAmountButton(sd.getPaymentAmountButton());
							progressListener.onPost(shopId);
						} catch (Exception e) {
							Logger.appendLog(context, MPOSApplication.LOG_DIR, 
									MPOSApplication.LOG_FILE_NAME, 
									"Error when add shop data : " + e.getMessage());
							progressListener.onError(e.getMessage());
						}
					}
				}).execute(url);
			}
		};
		new AuthenDevice(context, authenDeviceListener).execute(url);
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
						ProductsDataSource pd = new ProductsDataSource(context.getApplicationContext());
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
			JSONUtil jsonUtil = new JSONUtil();
			Type type = new TypeToken<WebServiceResult>(){}.getType();
			
			try {
				WebServiceResult ws = (WebServiceResult) jsonUtil.toObject(type, result);
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
			JSONUtil jsonUtil = new JSONUtil();
			Type type = new TypeToken<ShopData>() {}.getType();
			
			try {
				ShopData shopData = (ShopData) jsonUtil.toObject(type, result);
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
			JSONUtil jsonUtil = new JSONUtil();
			Type type = new TypeToken<ProductGroups>() {}.getType();
			
			ProductGroups productData;
			try {
				productData = (ProductGroups) jsonUtil.toObject(type, result);
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
			JSONUtil jsonUtil = new JSONUtil();
			Type type = new TypeToken<MenuGroups>() {}.getType();
			
			try {
				MenuGroups menuGroup = (MenuGroups) jsonUtil.toObject(type, result);
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
