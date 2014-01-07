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
	
	public void loadShopData(final String deviceCode, 
			final OnServiceProcessListener listener){

		mProgress.show();
		final String url = GlobalVar.getFullUrl(mContext);
		
		new AuthenDevice(deviceCode, new OnAuthenDeviceListener(){

			@Override
			public void onAuthenSuccess(final int shopId) {
				// load shop data
				new LoadShopTask(shopId, deviceCode, new OnLoadShopListener(){

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

	public void loadProductData(final int shopId, final String deviceCode, 
			final OnServiceProcessListener listener){
		
		mProgress.show();
		final String url = GlobalVar.getFullUrl(mContext);
		
		// load menu
		new LoadMenuTask(shopId, deviceCode, new OnLoadMenuListener(){

			@Override
			public void onError(String err) {
				mProgress.dismiss();
				listener.onError(err);
			}

			@Override
			public void onLoadMenuSuccess(final MenuGroups mgs) {
				
				new LoadProductTask(shopId, deviceCode, new OnLoadProductListener(){

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
	
	// send stock
	public static class SendStockTask extends MPOSMainService{

		public SendStockTask(Context c, String deviceCode) {
			super(c, deviceCode, SEND_STOCK_METHOD);
			// TODO Auto-generated constructor stub
		}
		
	}
	
	// send sale transaction
	public static class SendSaleTransactionTask extends MPOSMainService{
		public SendSaleTransactionTask(Context c, String deviceCode, String saleJson,
				OnServiceProcessListener listener) {
			super(c, deviceCode, SEND_SALE_TRANS_METHOD);
		}
		
		@Override
		protected void onPostExecute(String result) {
		}


		@Override
		protected void onPreExecute() {
		}

		
	}
	
	// load shop data
	private class LoadShopTask extends MPOSMainService{

		private OnLoadShopListener listener;
		
		public LoadShopTask(int shopId, String deviceCode, 
				OnLoadShopListener listener) {
			super(mContext, deviceCode, LOAD_SHOP_METHOD);
			
			property = new PropertyInfo();
			property.setName("iShopID");
			property.setValue(shopId);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			this.listener = listener;
		}

		@Override
		protected void onPostExecute(String result) {
			JSONUtil jsonUtil = new JSONUtil();
			Type type = new TypeToken<ShopData>() {}.getType();
			
			try {
				ShopData shopData = (ShopData) jsonUtil.toObject(type, result);
				this.listener.onLoadShopSuccess(shopData);
			} catch (Exception e) {
				this.listener.onError(result);
			}
		}

		@Override
		protected void onPreExecute() {
			mProgress.setMessage(context.getString(R.string.sync_shop_progress));
		}
	}
	
	// load products
	private class LoadProductTask extends MPOSMainService{
		
		private OnLoadProductListener listener;
		
		public LoadProductTask(int shopId, String deviceCode, 
				OnLoadProductListener listener) {
			super(mContext, deviceCode, LOAD_PRODUCT_METHOD);
			
			property = new PropertyInfo();
			property.setName("iShopID");
			property.setValue(shopId);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			this.listener = listener;
		}

		@Override
		protected void onPostExecute(String result) {
			JSONUtil jsonUtil = new JSONUtil();
			Type type = new TypeToken<ProductGroups>() {}.getType();
			
			ProductGroups productData;
			try {
				productData = (ProductGroups) jsonUtil.toObject(type, result);
				this.listener.onLoadProductSuccess(productData);
			} catch (Exception e) {
				this.listener.onError(result);
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
		
		private OnLoadMenuListener listener;
		
		public LoadMenuTask(int shopId, String deviceCode, 
				OnLoadMenuListener listener) {
			super(mContext, deviceCode, LOAD_MENU_METHOD);
			
			property = new PropertyInfo();
			property.setName("iShopID");
			property.setValue(shopId);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			this.listener = listener;
		}

		@Override
		protected void onPostExecute(String result) {
			JSONUtil jsonUtil = new JSONUtil();
			Type type = new TypeToken<MenuGroups>() {}.getType();
			
			try {
				MenuGroups menuGroup = (MenuGroups) jsonUtil.toObject(type, result);
				this.listener.onLoadMenuSuccess(menuGroup);
			} catch (Exception e) {
				this.listener.onError(result);
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
		
		private OnAuthenDeviceListener listener;
		
		public AuthenDevice(String deviceCode, 
				OnAuthenDeviceListener listener) {
			super(mContext, deviceCode, CHECK_DEVICE_METHOD);
			this.listener = listener;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				int shopId = Integer.parseInt(result);
				if(shopId > 0)
					this.listener.onAuthenSuccess(shopId);
				else
					this.listener.onError(context.getString(R.string.device_not_register));
			} catch (NumberFormatException e) {
				this.listener.onError(result);
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
