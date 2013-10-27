package com.syn.mpos;

import java.lang.reflect.Type;
import org.ksoap2.serialization.PropertyInfo;
import com.google.gson.reflect.TypeToken;
import com.j1tth4.mobile.util.JSONUtil;
import com.syn.mpos.database.Products;
import com.syn.mpos.database.Setting;
import com.syn.mpos.database.Shop;
import com.syn.pos.MenuGroups;
import com.syn.pos.ProductGroups;
import com.syn.pos.ShopData;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

public class MPOSService {
	
	public Context mContext;
	public Setting.Connection mSettingConn;
	public ProgressDialog mProgress;
	
	public MPOSService(Context c, Setting.Connection conn){
		mContext = c;
		mSettingConn = conn;
		mProgress = new ProgressDialog(c);
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
					public void onError(String mesg) {
						listener.onError(mesg);
						dialog(mContext, R.string.load_shop, mesg);
						mProgress.dismiss();
					}

					@Override
					public void onLoadShopSuccess(ShopData sd) {
						Shop shop = new Shop(mContext);
						try {
							shop.insertShop(sd.getShopProperty());
							shop.insertComputer(sd.getComputerProperty());
							shop.insertProperty(sd.getGlobalProperty());
							shop.insertStaff(sd.getStaffs());
							shop.insertLanguage(sd.getLanguage());
							shop.insertProgramFeature(sd.getProgramFeature());
							
						} catch (Exception e) {
							dialog(mContext, R.string.load_shop, e.getMessage());
						}
					}

					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						
					}
					
				}).execute(url);
			}

			@Override
			public void onError(String mesg) {
				dialog(mContext, R.string.check_device, mesg);
				listener.onError(mesg);
				mProgress.dismiss();
			}

			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				
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
			public void onError(String mesg) {
				listener.onError(mesg);
				dialog(mContext, R.string.load_menu, mesg);
				mProgress.dismiss();
			}

			@Override
			public void onLoadMenuSuccess(final MenuGroups mgs) {
				
				new LoadProductTask(shopId, deviceCode, new OnLoadProductListener(){

					@Override
					public void onError(String mesg) {
						listener.onError(mesg);
						dialog(mContext, R.string.load_product, mesg);
						mProgress.dismiss();
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
							dialog(mContext, R.string.load_product, e.getMessage());
							listener.onError(e.getMessage());
						}

						mProgress.dismiss();
					}

					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						
					}
					
				}).execute(url);
			}

			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				
			}
			
		}).execute(url);
		// load menu
	}
	
	private static void dialog(Context c, int title, String mesg){
		new AlertDialog.Builder(c)
		.setTitle(title)
		.setMessage(mesg)
		.setCancelable(false)
		.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		}).show();
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
				this.listener.onError(e.getMessage());
			}
		}

		@Override
		protected void onPreExecute() {
			mProgress.setMessage(context.getString(R.string.load_shop));
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
			
			ProductGroups productData = (ProductGroups) jsonUtil.toObject(type, result);
			
			this.listener.onLoadProductSuccess(productData);
		}

		@Override
		protected void onPreExecute() {
			mProgress.setMessage(context.getString(R.string.load_product));
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
			
			MenuGroups menuGroup = (MenuGroups) jsonUtil.toObject(type, result);
			
			this.listener.onLoadMenuSuccess(menuGroup);
		}

		@Override
		protected void onPreExecute() {
			mProgress.setMessage(context.getString(R.string.load_menu));
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
				this.listener.onError(e.getMessage());
			}
		}

		@Override
		protected void onPreExecute() {
			mProgress.setMessage(context.getString(R.string.check_device));
		}
	}	
	
	public static interface OnLoadMenuListener extends OnServiceProcessListener{
		void onLoadMenuSuccess(MenuGroups mgs);
	}
	
	public static interface OnLoadProductListener extends OnServiceProcessListener{
		void onLoadProductSuccess(ProductGroups pgs);
	}
	
	public static interface OnLoadShopListener extends OnServiceProcessListener{
		void onLoadShopSuccess(ShopData sd);
	}
	
	public static interface OnAuthenDeviceListener extends OnServiceProcessListener{
		void onAuthenSuccess(int shopId);
	}
	
	public static interface OnServiceProcessListener{
		void onSuccess();
		void onError(String mesg);
	}
}
