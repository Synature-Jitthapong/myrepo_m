package com.syn.mpos;

import java.lang.reflect.Type;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.reflect.TypeToken;
import com.j1tth4.mobile.util.FileManager;
import com.j1tth4.mobile.util.JSONUtil;
import com.syn.mpos.provider.Computer;
import com.syn.mpos.provider.GlobalProperty;
import com.syn.mpos.provider.HeaderFooterReceipt;
import com.syn.mpos.provider.Language;
import com.syn.mpos.provider.Products;
import com.syn.mpos.provider.Shop;
import com.syn.mpos.provider.Staff;
import com.syn.pos.MenuGroups;
import com.syn.pos.ProductGroups;
import com.syn.pos.ShopData;
import com.syn.pos.WebServiceResult;

import android.content.Context;

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
						Shop shop = new Shop(MPOSApplication.getWriteDatabase());
						Computer comp = new Computer(
								MPOSApplication.getWriteDatabase());
						GlobalProperty global = new GlobalProperty(
								MPOSApplication.getWriteDatabase());
						Language lang = new Language(
								MPOSApplication.getWriteDatabase());
						Staff staff = new Staff(
								MPOSApplication.getWriteDatabase());
						HeaderFooterReceipt hf = new HeaderFooterReceipt(
								MPOSApplication.getWriteDatabase());
						try {
							shop.insertShopProperty(sd.getShopProperty());
							comp.insertComputer(sd.getComputerProperty());
							global.insertProperty(sd.getGlobalProperty());
							staff.insertStaff(sd.getStaffs());
							lang.insertLanguage(sd.getLanguage());
							hf.addHeaderFooterReceipt(sd
									.getHeaderFooterReceipt());
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
							p.addProductGroup(pgs.getProductGroup(),
									mgs.getMenuGroup());
							p.addProductDept(pgs.getProductDept(),
									mgs.getMenuDept());
							p.addProducts(pgs.getProduct(), mgs.getMenuItem());
							p.addPComponentSet(pgs.getPComponentSet());

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
			property = new PropertyInfo();
			property.setName(SHOP_ID_PARAM);
			property.setValue(MPOSApplication.getShopId());
			property.setType(int.class);
			soapRequest.addProperty(property);
			// computerId
			property = new PropertyInfo();
			property.setName(COMPUTER_ID_PARAM);
			property.setValue(MPOSApplication.getComputerId());
			property.setType(int.class);
			soapRequest.addProperty(property);
			// staffId
			property = new PropertyInfo();
			property.setName(STAFF_ID_PARAM);
			property.setValue(staffId);
			property.setType(int.class);
			soapRequest.addProperty(property);
			// json sale
			property = new PropertyInfo();
			property.setName(JSON_SALE_PARAM);
			property.setValue(jsonSale);
			property.setType(String.class);
			soapRequest.addProperty(property);
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
			
			property = new PropertyInfo();
			property.setName(SHOP_ID_PARAM);
			property.setValue(shopId);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
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
			
			property = new PropertyInfo();
			property.setName(SHOP_ID_PARAM);
			property.setValue(MPOSApplication.getShopId());
			property.setType(int.class);
			soapRequest.addProperty(property);
			
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
			
			property = new PropertyInfo();
			property.setName(SHOP_ID_PARAM);
			property.setValue(MPOSApplication.getShopId());
			property.setType(int.class);
			soapRequest.addProperty(property);
			
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
					mListener.onError(context.getString(R.string.device_not_register));
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
