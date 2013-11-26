package com.syn.mpos;

import java.lang.reflect.Type;
import org.ksoap2.serialization.PropertyInfo;
import com.google.gson.reflect.TypeToken;
import com.j1tth4.mobile.util.JSONUtil;
import com.syn.mpos.database.Computer;
import com.syn.mpos.database.GlobalProperty;
import com.syn.mpos.database.Language;
import com.syn.mpos.database.Products;
import com.syn.mpos.database.Setting;
import com.syn.mpos.database.Shop;
import com.syn.mpos.database.Staff;
import com.syn.pos.MenuGroups;
import com.syn.pos.ProductGroups;
import com.syn.pos.ShopData;
import android.app.ProgressDialog;
import android.content.Context;

public class MPOSService {
	
	public Context mContext;
	public Setting.Connection mSettingConn;
	public ProgressDialog mProgress;
	
	public MPOSService(Context c, Setting.Connection conn){
		mContext = c;
		mSettingConn = conn;
		mProgress = new ProgressDialog(c);
		mProgress.setCancelable(false);
	}
	
	public void loadShopData(final String deviceCode, 
			final OnServiceProcessListener listener){

		mProgress.show();
		final String url = mSettingConn.getFullUrl();
		
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
							shop.addShopProperty(sd.getShopProperty());
							comp.insertComputer(sd.getComputerProperty());
							global.insertProperty(sd.getGlobalProperty());
							staff.addStaff(sd.getStaffs());
							lang.insertLanguage(sd.getLanguage());
							listener.onSuccess();
						} catch (Exception e) {
							listener.onError(e.getMessage());
						}

						mProgress.dismiss();
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
		final String url = mSettingConn.getFullUrl();
		
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
							p.insertProductGroup(pgs.getProductGroup(), mgs.getMenuGroup());
							p.insertProductDept(pgs.getProductDept(), mgs.getMenuDept());
							p.insertProducts(pgs.getProduct(), mgs.getMenuItem());
							
							listener.onSuccess();
						} catch (Exception e) {
							listener.onError(e.getMessage());
						}

						mProgress.dismiss();
					}
					
				}).execute(url);
			}
			
		}).execute(url);
		// load menu
	}
	
	// load shop data
	public class LoadShopTask extends MPOSMainService{

		private OnLoadShopListener listener;
		
		public LoadShopTask(int shopId, String deviceCode, 
				OnLoadShopListener listener) {
			super(mContext, deviceCode, "WSmPOS_JSON_LoadShopData");
			
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
	public class LoadProductTask extends MPOSMainService{
		
		private OnLoadProductListener listener;
		
		public LoadProductTask(int shopId, String deviceCode, 
				OnLoadProductListener listener) {
			super(mContext, deviceCode, "WSmPOS_JSON_LoadProductDataV2");
			
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
	public class LoadMenuTask extends MPOSMainService{
		
		private OnLoadMenuListener listener;
		
		public LoadMenuTask(int shopId, String deviceCode, 
				OnLoadMenuListener listener) {
			super(mContext, deviceCode, "WSmPOS_JSON_LoadMenuDataV2");
			
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
	public class AuthenDevice extends MPOSMainService{
		
		private OnAuthenDeviceListener listener;
		
		public AuthenDevice(String deviceCode, 
				OnAuthenDeviceListener listener) {
			super(mContext, deviceCode, "WSmPOS_CheckAuthenShopDevice");
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
				this.listener.onError(context.getString(R.string.device_not_register));
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
