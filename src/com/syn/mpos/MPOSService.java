package com.syn.mpos;

import java.lang.reflect.Type;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.j1tth4.mobile.util.JSONUtil;
import com.syn.mpos.db.MenuDept;
import com.syn.mpos.db.MenuGroup;
import com.syn.mpos.db.MenuItem;
import com.syn.mpos.db.Product;
import com.syn.mpos.db.Shop;
import com.syn.mpos.model.MenuGroups;
import com.syn.mpos.model.ProductGroups;
import com.syn.mpos.model.ShopData;
import com.syn.mpos.model.WebServiceResult;
import com.syn.mpos.model.ShopData.ShopProperty;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

public class MPOSService {
	
	public static void sync(final Context c, final IServiceStateListener listener){
		final String url = "http://61.90.204.61/promise6_table/ws_mpos.asmx";
		final ProgressDialog progress = new ProgressDialog(c);
		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress.setIndeterminate(false);
		progress.setCancelable(false);   
		progress.setMax(100);
		
		new AuthenDevice(c, new IServiceStateListener(){

			@Override
			public void onProgress() {
				progress.setMessage("check device");
				progress.show();
			}

			@Override
			public void onSuccess() {
				progress.setProgress(100 / 4);
				new LoadShopTask(c, new IServiceStateListener(){

					@Override
					public void onProgress() {
						progress.setMessage("load shop");
					}

					@Override
					public void onSuccess() {
						progress.setProgress(100 / 2);
						new LoadProductTask(c, new IServiceStateListener(){

							@Override
							public void onProgress() {
								progress.setMessage("load product");
							}

							@Override
							public void onSuccess() {
								progress.setProgress(100 / 1);
								new LoadMenuTask(c, new IServiceStateListener(){

									@Override
									public void onProgress() {
										progress.setMessage("load menu");
									}

									@Override
									public void onSuccess() {
										progress.setProgress(100 / 1);
										progress.dismiss();
										listener.onSuccess();
									}

									@Override
									public void onFail(String msg) {
										progress.dismiss();
									}
									
								}).execute(url);
							}

							@Override
							public void onFail(String msg) {
								progress.dismiss();
							}
						}).execute(url);
					}

					@Override
					public void onFail(String msg) {
						progress.dismiss();
						new AlertDialog.Builder(c)
						.setTitle("Error")
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setMessage(msg)
						.setNeutralButton("Close", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
							}
						})
						.show();
					}
					
				}).execute(url);
			}

			@Override
			public void onFail(String msg) {
				progress.dismiss();
			}
			
		}).execute(url);
	}
	
	// load products
	public static class LoadProductTask extends MPOSMainService{

		public LoadProductTask(Context c, IServiceStateListener listener) {
			super(c, "WSmPOS_JSON_LoadProductDataV2");
			serviceState = listener;
		}

		@Override
		protected void onPostExecute(String result) {
			JSONUtil jsonUtil = new JSONUtil();
			Type type = new TypeToken<ProductGroups>() {}.getType();
			
			ProductGroups productData = (ProductGroups) jsonUtil.toObject(type, result);
			Product p = new Product(context);
			p.addProducts(productData.getProduct());
			
			
			serviceState.onSuccess();
		}

		@Override
		protected void onPreExecute() {
			serviceState.onProgress();
		}
		
	}
	
	// load shop data
	public static class LoadShopTask extends MPOSMainService{

		public LoadShopTask(Context c, IServiceStateListener listener) {
			super(c, "WSmPOS_JSON_LoadShopData");
			serviceState = listener;
		}

		@Override
		protected void onPostExecute(String result) {
			JSONUtil jsonUtil = new JSONUtil();
			Type type = new TypeToken<ShopData>() {}.getType();
			
			ShopData shopData = (ShopData) jsonUtil.toObject(type, result);
			
			Shop shop = new Shop(context);
			if(shop.addShopProperty(shopData.getShopProperty())){
				if(shop.addComputerProperty(shopData.getComputerProperty())){
					if(shop.addGlobalProperty(shopData.getGlobalProperty())){
						if(shop.addStaff(shopData.getStaffs())){
							if(shop.addLanguage(shopData.getLanguage())){
								if(shop.addProgramFeature(shopData.getProgramFeature())){
									serviceState.onSuccess();
								}else{
									
								}
							}else{
								
							}
						}else{
							
						}
					}else{
						
					}
				}else{
					serviceState.onFail("cannot update computer");
				}
			}else{
				
			}
		}

		@Override
		protected void onPreExecute() {
			serviceState.onProgress();
		}
	}
	
	// load menu data
	public static class LoadMenuTask extends MPOSMainService{
		
		public LoadMenuTask(Context c, IServiceStateListener listener) {
			super(c, "WSmPOS_JSON_LoadMenuDataV2");
			serviceState = listener;
		}

		@Override
		protected void onPostExecute(String result) {
			JSONUtil jsonUtil = new JSONUtil();
			Type type = new TypeToken<MenuGroups>() {}.getType();
			
			MenuGroups menuGroup = (MenuGroups) jsonUtil.toObject(type, result);
			
			try {
				MenuGroup mg = new MenuGroup(context);
				mg.addMenuGroup(menuGroup.getMenuGroup());
				
				MenuDept md = new MenuDept(context);
				md.addMenuDept(menuGroup.getMenuDept());
				
				MenuItem mi = new MenuItem(context);
				mi.addMenuItem(menuGroup.getMenuItem());

				serviceState.onSuccess();
			} catch (Exception e) {
				serviceState.onFail(e.getMessage());
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			serviceState.onProgress();
		}
		
		
	}
	
	// check authen shop
	public static class AuthenDevice extends MPOSMainService{
		
		public AuthenDevice(Context c, IServiceStateListener listener) {
			super(c, "WSmPOS_CheckAuthenShopDevice");
			serviceState = listener;
		}

		@Override
		protected void onPostExecute(String result) {
			int shopId = 0;
			try {
				shopId = Integer.parseInt(result); 
				if (shopId > 0) {
					serviceState.onSuccess();
				} else if (shopId == -1) {
					serviceState.onFail("");
				} else {
					serviceState.onFail("");
				}
			} catch (NumberFormatException e) {
				serviceState.onFail(e.getMessage());
			}
		}

		@Override
		protected void onPreExecute() {
			serviceState.onProgress();
		}
	}
}
