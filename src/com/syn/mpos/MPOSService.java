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

import android.app.ProgressDialog;
import android.content.Context;

public class MPOSService {
	public Context mContext;
	public ProgressDialog mProgress;
	
	public MPOSService(Context c){
		mContext = c;
		mProgress = new ProgressDialog(c);
		mProgress.setCancelable(false);
	}
	
	public void sendSaleDataTransaction(Context c, int staffId, 
			String jsonSale, OnServiceProcessListener listener){
		new SendSaleTransactionTask(c, staffId, jsonSale, 
				listener).execute(GlobalVar.getFullUrl(c));
	}
	
	public void loadShopData(final OnServiceProcessListener listener){
		mProgress.show();
		final String url = GlobalVar.getFullUrl(mContext);
		
		new AuthenDevice(new OnAuthenDeviceListener(){

			@Override
			public void onAuthenSuccess(final int shopId) {
				// load shop data
				new LoadShopTask(shopId, new OnLoadShopListener(){

					@Override
					public void onError(String err) {
						mProgress.dismiss();
						listener.onError(err);
					}

					@Override
					public void onLoadShopSuccess(ShopData sd) {
						Shop shop = new Shop(mContext);
						Computer comp = new Computer(mContext);
						GlobalProperty global = new GlobalProperty(mContext);
						Language lang = new Language(mContext);
						Staff staff = new Staff(mContext);
						try {
							shop.insertShopProperty(sd.getShopProperty());
							comp.insertComputer(sd.getComputerProperty());
							global.insertProperty(sd.getGlobalProperty());
							staff.insertStaff(sd.getStaffs());
							lang.insertLanguage(sd.getLanguage());
							mProgress.dismiss();
							listener.onSuccess();
						} catch (Exception e) {
							mProgress.dismiss();
							listener.onError(e.getMessage());
						}
					}
					
				}).execute(url);
			}

			@Override
			public void onError(String err) {
				mProgress.dismiss();
				listener.onError(err);
			}
			
		}).execute(url);
	}

	public void loadProductData(final OnServiceProcessListener listener){
		
		mProgress.show();
		final String url = GlobalVar.getFullUrl(mContext);
		
		// load menu
		new LoadMenuTask(new OnLoadMenuListener(){

			@Override
			public void onError(String err) {
				mProgress.dismiss();
				listener.onError(err);
			}

			@Override
			public void onLoadMenuSuccess(final MenuGroups mgs) {
				
				new LoadProductTask(new OnLoadProductListener(){

					@Override
					public void onError(String err) {
						mProgress.dismiss();
						listener.onError(err);
					}

					@Override
					public void onLoadProductSuccess(ProductGroups pgs) {
						Products p = new Products(mContext);
						try {
							p.addProductGroup(pgs.getProductGroup(), mgs.getMenuGroup());
							p.addProductDept(pgs.getProductDept(), mgs.getMenuDept());
							p.addProducts(pgs.getProduct(), mgs.getMenuItem());
							p.addPComponentSet(pgs.getPComponentSet());

							mProgress.dismiss();	
							listener.onSuccess();
						} catch (Exception e) {
							mProgress.dismiss();
							listener.onError(e.getMessage());
						}
					}
					
				}).execute(url);
			}
			
		}).execute(url);
		// load menu
	}
	
	// send sale transaction
	private class SendSaleTransactionTask extends MPOSMainService{
		private OnServiceProcessListener mListener;
		
		public SendSaleTransactionTask(Context c, int staffId, String jsonSale, 
				OnServiceProcessListener listener) {
			super(c, SEND_SALE_TRANS_METHOD);
			mListener = listener;

			// shopId
			property = new PropertyInfo();
			property.setName(SHOP_ID_PARAM);
			property.setValue(GlobalVar.getShopId(c));
			property.setType(int.class);
			soapRequest.addProperty(property);
			// computerId
			property = new PropertyInfo();
			property.setName(COMPUTER_ID_PARAM);
			property.setValue(GlobalVar.getComputerId(c));
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
			if(mProgress.isShowing())
				mProgress.dismiss();
			
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
			mProgress.setMessage(mContext.getString(R.string.endday_progress));
			mProgress.show();
		}
	}
	
	// load shop data
	private class LoadShopTask extends MPOSMainService{
		private OnLoadShopListener mListener;
		
		public LoadShopTask(int shopId, OnLoadShopListener listener) {
			super(mContext, LOAD_SHOP_METHOD);
			
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
			mProgress.setMessage(context.getString(R.string.sync_shop_progress));
		}
	}
	
	// load products
	private class LoadProductTask extends MPOSMainService{
		private OnLoadProductListener mListener;
		
		public LoadProductTask(OnLoadProductListener listener) {
			super(mContext, LOAD_PRODUCT_METHOD);
			
			property = new PropertyInfo();
			property.setName(SHOP_ID_PARAM);
			property.setValue(GlobalVar.getShopId(mContext));
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
			mProgress.setMessage(context.getString(R.string.sync_product_progress));
		}
	}
	
	// load menu data
	private class LoadMenuTask extends MPOSMainService{
		private OnLoadMenuListener mListener;
		
		public LoadMenuTask(OnLoadMenuListener listener) {
			super(mContext, LOAD_MENU_METHOD);
			
			property = new PropertyInfo();
			property.setName(SHOP_ID_PARAM);
			property.setValue(GlobalVar.getShopId(mContext));
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
			mProgress.setMessage(context.getString(R.string.sync_menu_progress));
		}
	}

	// check authen shop
	private class AuthenDevice extends MPOSMainService{
		private OnAuthenDeviceListener mListener;
		
		public AuthenDevice(OnAuthenDeviceListener listener) {
			super(mContext, CHECK_DEVICE_METHOD);
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
			mProgress.setMessage(context.getString(R.string.check_device));
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
		void onError(String mesg);
	}
}
