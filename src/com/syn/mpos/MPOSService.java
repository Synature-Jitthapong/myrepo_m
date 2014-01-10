package com.syn.mpos;

import java.lang.reflect.Type;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.reflect.TypeToken;
import com.j1tth4.mobile.util.JSONUtil;
import com.syn.mpos.database.Computer;
import com.syn.mpos.database.GlobalProperty;
import com.syn.mpos.database.Language;
import com.syn.mpos.database.Products;
import com.syn.mpos.database.Shop;
import com.syn.mpos.database.Staff;
import com.syn.pos.MenuGroups;
import com.syn.pos.ProductGroups;
import com.syn.pos.ShopData;
import com.syn.pos.WebServiceResult;
import android.content.Context;

public class MPOSService {
	
	public void sendPartialSaleTransaction(int staffId,
			String jsonSale, OnServiceProcessListener listener){
		new SendPartialSaleTransactionTask(MPOSApplication.getContext(), staffId, jsonSale, 
				listener).execute(MPOSApplication.getFullUrl());
	}
	
	public void sendSaleDataTransaction(int staffId, 
			String jsonSale, OnServiceProcessListener listener){
		new SendSaleTransactionTask(MPOSMainService.SEND_SALE_TRANS_METHOD,
				staffId, jsonSale,listener).execute(MPOSApplication.getFullUrl());
	}
	
	public void loadShopData(final OnServiceProcessListener listener){
		final String url = MPOSApplication.getFullUrl();
		
		new AuthenDevice(new OnAuthenDeviceListener(){

			@Override
			public void onAuthenSuccess(final int shopId) {
				// load shop data
				new LoadShopTask(shopId, new OnLoadShopListener(){

					@Override
					public void onError(String err) {
						listener.onError(err);
					}

					@Override
					public void onLoadShopSuccess(ShopData sd) {
						Shop shop = new Shop(MPOSApplication.getContext());
						Computer comp = new Computer(MPOSApplication.getContext());
						GlobalProperty global = new GlobalProperty(MPOSApplication.getContext());
						Language lang = new Language(MPOSApplication.getContext());
						Staff staff = new Staff(MPOSApplication.getContext());
						try {
							shop.insertShopProperty(sd.getShopProperty());
							comp.insertComputer(sd.getComputerProperty());
							global.insertProperty(sd.getGlobalProperty());
							staff.insertStaff(sd.getStaffs());
							lang.insertLanguage(sd.getLanguage());
							listener.onSuccess();
						} catch (Exception e) {
							listener.onError(e.getMessage());
						}
					}
					
				}).execute(url);
			}

			@Override
			public void onError(String err) {
				listener.onError(err);
			}
			
		}).execute(url);
	}

	public void loadProductData(final OnServiceProcessListener listener){
		
		final String url = MPOSApplication.getFullUrl();
		
		// load menu
		new LoadMenuTask(new OnLoadMenuListener(){

			@Override
			public void onError(String err) {
				listener.onError(err);
			}

			@Override
			public void onLoadMenuSuccess(final MenuGroups mgs) {
				
				new LoadProductTask(new OnLoadProductListener(){

					@Override
					public void onError(String err) {
						listener.onError(err);
					}

					@Override
					public void onLoadProductSuccess(ProductGroups pgs) {
						Products p = new Products(MPOSApplication.getContext());
						try {
							p.addProductGroup(pgs.getProductGroup(), mgs.getMenuGroup());
							p.addProductDept(pgs.getProductDept(), mgs.getMenuDept());
							p.addProducts(pgs.getProduct(), mgs.getMenuItem());
							p.addPComponentSet(pgs.getPComponentSet());
							listener.onSuccess();
						} catch (Exception e) {
							listener.onError(e.getMessage());
						}
					}
					
				}).execute(url);
			}
			
		}).execute(url);
		// load menu
	}
	
	private class SendPartialSaleTransactionTask extends SendSaleTransactionTask{

		public SendPartialSaleTransactionTask(Context c, int staffId,
				String jsonSale, OnServiceProcessListener listener) {
			super(MPOSMainService.SEND_PARTIAL_SALE_TRANS_METHOD, 
					staffId, jsonSale, listener);
		}

		@Override
		protected void onPreExecute() {
		}
		
	}
	
	// send sale transaction
	private class SendSaleTransactionTask extends MPOSMainService{
		private OnServiceProcessListener mListener;
		
		public SendSaleTransactionTask(String method, 
				int staffId, String jsonSale, OnServiceProcessListener listener) {
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
					mListener.onSuccess();
				}else{
					mListener.onError(ws.getSzResultData());
				}
			} catch (Exception e) {
				mListener.onError(result);
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
//			mProgress.setMessage(mContext.getString(R.string.endday_progress));
//			mProgress.show();
		}
	}
	
	// load shop data
	private class LoadShopTask extends MPOSMainService{
		private OnLoadShopListener mListener;
		
		public LoadShopTask(int shopId, OnLoadShopListener listener) {
			super(LOAD_SHOP_METHOD);
			
			property = new PropertyInfo();
			property.setName(SHOP_ID_PARAM);
			property.setValue(shopId);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			this.mListener = listener;
		}

		@Override
		protected void onPostExecute(String result) {
			JSONUtil jsonUtil = new JSONUtil();
			Type type = new TypeToken<ShopData>() {}.getType();
			
			try {
				ShopData shopData = (ShopData) jsonUtil.toObject(type, result);
				this.mListener.onLoadShopSuccess(shopData);
			} catch (Exception e) {
				this.mListener.onError(result);
			}
		}

		@Override
		protected void onPreExecute() {
			//mProgress.setMessage(context.getString(R.string.sync_shop_progress));
		}
	}
	
	// load products
	private class LoadProductTask extends MPOSMainService{
		private OnLoadProductListener mListener;
		
		public LoadProductTask(OnLoadProductListener listener) {
			super(LOAD_PRODUCT_METHOD);
			
			property = new PropertyInfo();
			property.setName(SHOP_ID_PARAM);
			property.setValue(MPOSApplication.getShopId());
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			this.mListener = listener;
		}

		@Override
		protected void onPostExecute(String result) {
			JSONUtil jsonUtil = new JSONUtil();
			Type type = new TypeToken<ProductGroups>() {}.getType();
			
			ProductGroups productData;
			try {
				productData = (ProductGroups) jsonUtil.toObject(type, result);
				this.mListener.onLoadProductSuccess(productData);
			} catch (Exception e) {
				this.mListener.onError(result);
				e.printStackTrace();
			}
			
		}

		@Override
		protected void onPreExecute() {
			//mProgress.setMessage(context.getString(R.string.sync_product_progress));
		}
	}
	
	// load menu data
	private class LoadMenuTask extends MPOSMainService{
		private OnLoadMenuListener mListener;
		
		public LoadMenuTask(OnLoadMenuListener listener) {
			super(LOAD_MENU_METHOD);
			
			property = new PropertyInfo();
			property.setName(SHOP_ID_PARAM);
			property.setValue(MPOSApplication.getShopId());
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			this.mListener = listener;
		}

		@Override
		protected void onPostExecute(String result) {
			JSONUtil jsonUtil = new JSONUtil();
			Type type = new TypeToken<MenuGroups>() {}.getType();
			
			try {
				MenuGroups menuGroup = (MenuGroups) jsonUtil.toObject(type, result);
				this.mListener.onLoadMenuSuccess(menuGroup);
			} catch (Exception e) {
				this.mListener.onError(result);
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			//mProgress.setMessage(context.getString(R.string.sync_menu_progress));
		}
	}

	// check authen shop
	private class AuthenDevice extends MPOSMainService{
		private OnAuthenDeviceListener mListener;
		
		public AuthenDevice(OnAuthenDeviceListener listener) {
			super(CHECK_DEVICE_METHOD);
			this.mListener = listener;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				int shopId = Integer.parseInt(result);
				if(shopId > 0)
					this.mListener.onAuthenSuccess(shopId);
				else
					this.mListener.onError(context.getString(R.string.device_not_register));
			} catch (NumberFormatException e) {
				this.mListener.onError(result);
			}
		}

		@Override
		protected void onPreExecute() {
			//mProgress.setMessage(context.getString(R.string.check_device));
		}
	}	
	
	public static interface OnLoadMenuListener extends OnServiceError{
		void onLoadMenuSuccess(MenuGroups mgs);
	}
	
	public static interface OnLoadProductListener extends OnServiceError{
		void onLoadProductSuccess(ProductGroups pgs);
	}
	
	public static interface OnLoadShopListener extends OnServiceError{
		void onLoadShopSuccess(ShopData sd);
	}
	
	public static interface OnAuthenDeviceListener extends OnServiceError{
		void onAuthenSuccess(int shopId);
	}
	
	public static interface OnServiceError{
		void onError(String error);
	}
	
	public static interface OnServiceProcessListener{
		void onSuccess();
		void onError(String msg);
	}
}
