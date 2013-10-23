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
	
	public static ProgressDialog progress;
	
	public static void loadImportantData(Setting.Connection connSetting, 
			final Context c, final String deviceCode, final OnServiceProcessListener listener){

		progress = new ProgressDialog(c);
		progress.show();
		final String url = connSetting.getFullUrl();
		
		new AuthenDevice(c, deviceCode, new OnAuthenDeviceListener(){

			@Override
			public void onAuthenSuccess(final int shopId) {
				// load shop data
				new LoadShopTask(c, shopId, deviceCode, new OnLoadShopListener(){

					@Override
					public void onError(String mesg) {
						listener.onError(mesg);
						dialog(c, R.string.load_shop, mesg);
						progress.dismiss();
					}

					@Override
					public void onLoadShopSuccess(ShopData sd) {
						Shop shop = new Shop(c);
						try {
							shop.insertShop(sd.getShopProperty());
							shop.insertComputer(sd.getComputerProperty());
							shop.insertProperty(sd.getGlobalProperty());
							shop.insertStaff(sd.getStaffs());
							shop.insertLanguage(sd.getLanguage());
							shop.insertProgramFeature(sd.getProgramFeature());
							
							// load menu
							new LoadMenuTask(c, shopId, deviceCode, new OnLoadMenuListener(){

								@Override
								public void onError(String mesg) {
									listener.onError(mesg);
									dialog(c, R.string.load_menu, mesg);
									progress.dismiss();
								}

								@Override
								public void onLoadMenuSuccess(final MenuGroups mgs) {
									
									new LoadProductTask(c, shopId, deviceCode, new OnLoadProductListener(){

										@Override
										public void onError(String mesg) {
											listener.onError(mesg);
											dialog(c, R.string.load_product, mesg);
											progress.dismiss();
										}

										@Override
										public void onLoadProductSuccess(ProductGroups pgs) {
											Products p = new Products(c);
											try {
												p.insertProductGroup(pgs.getProductGroup());
												p.insertProductDept(pgs.getProductDept());
												p.insertProducts(pgs.getProduct(), mgs.getMenuItem());
												
												listener.onSuccess();
											} catch (Exception e) {
												dialog(c, R.string.load_product, e.getMessage());
												listener.onError(e.getMessage());
											}

											progress.dismiss();
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
							
						} catch (Exception e) {
							dialog(c, R.string.load_shop, e.getMessage());
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
				dialog(c, R.string.check_device, mesg);
				listener.onError(mesg);
				progress.dismiss();
			}

			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				
			}
			
		}).execute(url);
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
	public static class LoadShopTask extends MPOSMainService{

		private OnLoadShopListener listener;
		
		public LoadShopTask(Context c, int shopId, String deviceCode, OnLoadShopListener listener) {
			super(c, deviceCode, "WSmPOS_JSON_LoadShopData");
			
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
			progress.setMessage(context.getString(R.string.load_shop));
		}
	}
	
	// load products
	public static class LoadProductTask extends MPOSMainService{
		
		private OnLoadProductListener listener;
		
		public LoadProductTask(Context c, int shopId, String deviceCode, OnLoadProductListener listener) {
			super(c, deviceCode, "WSmPOS_JSON_LoadProductDataV2");
			
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
			progress.setMessage(context.getString(R.string.load_product));
		}
	}
	
	// load menu data
	public static class LoadMenuTask extends MPOSMainService{
		
		private OnLoadMenuListener listener;
		
		public LoadMenuTask(Context c, int shopId, String deviceCode, OnLoadMenuListener listener) {
			super(c, deviceCode, "WSmPOS_JSON_LoadMenuDataV2");
			
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
			progress.setMessage(context.getString(R.string.load_menu));
		}
		
		
	}

	// check authen shop
	public static class AuthenDevice extends MPOSMainService{
		
		private OnAuthenDeviceListener listener;
		
		public AuthenDevice(Context c, String deviceCode, OnAuthenDeviceListener listener) {
			super(c, deviceCode, "WSmPOS_CheckAuthenShopDevice");
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
			progress.setMessage(context.getString(R.string.check_device));
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
