package com.syn.mpos;

import java.lang.reflect.Type;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.reflect.TypeToken;
import com.j1tth4.mobile.util.FileManager;
import com.j1tth4.mobile.util.JSONUtil;
import com.syn.mpos.database.Bank;
import com.syn.mpos.database.Computer;
import com.syn.mpos.database.CreditCard;
import com.syn.mpos.database.GlobalProperty;
import com.syn.mpos.database.HeaderFooterReceipt;
import com.syn.mpos.database.Language;
import com.syn.mpos.database.PaymentAmountButton;
import com.syn.mpos.database.PaymentDetail;
import com.syn.mpos.database.Products;
import com.syn.mpos.database.Shop;
import com.syn.mpos.database.Staff;
import com.syn.pos.MenuGroups;
import com.syn.pos.ProductGroups;
import com.syn.pos.ShopData;
import com.syn.pos.WebServiceResult;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class MPOSService {

	public void loadShopData(final ProgressListener progressListener){
		final String url = MPOSApplication.getFullUrl();

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
			public void onPost(int shopId) {
				final LoadShopListener loadShopListener = new LoadShopListener() {

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
						SQLiteDatabase sqlite = MPOSApplication.getWriteDatabase();
						Shop shop = new Shop(sqlite);
						Computer comp = new Computer(sqlite);
						GlobalProperty global = new GlobalProperty(sqlite);
						Language lang = new Language(sqlite);
						Staff staff = new Staff(sqlite);
						HeaderFooterReceipt hf = new HeaderFooterReceipt(sqlite);
						Bank bank = new Bank(sqlite);
						CreditCard credit = new CreditCard(sqlite);
						PaymentDetail payment = new PaymentDetail(sqlite);
						PaymentAmountButton payButton = new PaymentAmountButton(sqlite);
						try {
							shop.insertShopProperty(sd.getShopProperty());
							comp.insertComputer(sd.getComputerProperty());
							global.insertProperty(sd.getGlobalProperty());
							staff.insertStaff(sd.getStaffs());
							lang.insertLanguage(sd.getLanguage());
							hf.addHeaderFooterReceipt(sd
									.getHeaderFooterReceipt());
							bank.insertBank(sd.getBankName());
							credit.insertCreditCardType(sd.getCreditCardType());
							payment.insertPaytype(sd.getPayType());
							payButton.insertPaymentAmountButton(sd.getPaymentAmountButton());
							progressListener.onPost();
						} catch (Exception e) {
							progressListener.onError(e.getMessage());
						}
					}
				};
				new LoadShop(shopId, loadShopListener).execute(url);
			}
		};
		new AuthenDevice(authenDeviceListener).execute(url);
	}

	// load product
	public void loadProductData(final ProgressListener progressListener){
		
		final String url = MPOSApplication.getFullUrl();

		final LoadMenuListener loadMenuListener = new LoadMenuListener() {

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
				final LoadProductListener loadProductListener = new LoadProductListener() {

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
						Products p = new Products(
								MPOSApplication.getWriteDatabase());
						try {
							p.insertProductGroup(pgs.getProductGroup(),
									mgs.getMenuGroup());
							p.insertProductDept(pgs.getProductDept(),
									mgs.getMenuDept());
							p.insertProducts(pgs.getProduct(), mgs.getMenuItem());
							p.insertPComponentGroup(pgs.getPComponentGroup());
							p.insertProductComponent(pgs.getPComponentSet());

							// clear all menu picture
							FileManager fm = new FileManager(
									MPOSApplication.getContext(),
									MPOSApplication.IMG_DIR);
							fm.clear();

							progressListener.onPost();
						} catch (Exception e) {
							progressListener.onError(e.getMessage());
						}
					}
				};
				new LoadProduct(loadProductListener).execute(url);
			}
		};
		new LoadMenu(loadMenuListener).execute(url);
	}
	
	public static class SendPartialSaleTransaction extends SendSaleTransaction{

		public SendPartialSaleTransaction(Context c, int staffId,
				String jsonSale, ProgressListener listener) {
			super(MPOSMainService.SEND_PARTIAL_SALE_TRANS_METHOD, 
					staffId, jsonSale, listener);
		}
	}
	
	// send sale transaction
	public static class SendSaleTransaction extends MPOSMainService{
		private ProgressListener mListener;
		
		public SendSaleTransaction(String method, 
				int staffId, String jsonSale, ProgressListener listener) {
			super(method);
			mListener = listener;

			// shopId
			mProperty = new PropertyInfo();
			mProperty.setName(SHOP_ID_PARAM);
			mProperty.setValue(MPOSApplication.getShopId());
			mProperty.setType(int.class);
			mSoapRequest.addProperty(mProperty);
			// computerId
			mProperty = new PropertyInfo();
			mProperty.setName(COMPUTER_ID_PARAM);
			mProperty.setValue(MPOSApplication.getComputerId());
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
		
		public LoadShop(int shopId, LoadShopListener listener) {
			super(LOAD_SHOP_METHOD);
			
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
		
		public LoadProduct(LoadProductListener listener) {
			super(LOAD_PRODUCT_METHOD);
			
			mProperty = new PropertyInfo();
			mProperty.setName(SHOP_ID_PARAM);
			mProperty.setValue(MPOSApplication.getShopId());
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
		
		public LoadMenu(LoadMenuListener listener) {
			super(LOAD_MENU_METHOD);
			
			mProperty = new PropertyInfo();
			mProperty.setName(SHOP_ID_PARAM);
			mProperty.setValue(MPOSApplication.getShopId());
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
		
		public AuthenDevice(AuthenDeviceListener listener) {
			super(CHECK_DEVICE_METHOD);
			mListener = listener;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				int shopId = Integer.parseInt(result);
				if(shopId > 0)
					mListener.onPost(shopId);
				else
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
