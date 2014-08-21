package com.synature.mpos;

import org.ksoap2.serialization.PropertyInfo;

import com.synature.pos.WebServiceResult;

import android.content.Context;

public class MPOSWebServiceClient {

//	public static void authenDevice(final Context context, final AuthenDeviceListener listener){
//		final String url = Utils.getFullUrl(context);
//		new AuthenDevice(context, listener).execute(url);
//	}
//	
//	public static void loadShopData(final Context context, final int shopId, final WebServiceWorkingListener listener){
//		
//		final String url = Utils.getFullUrl(context);
//
//		new LoadShop(context, shopId, new LoadShopListener() {
//
//			@Override
//			public void onPreExecute() {
//				listener.onPreExecute();
//			}
//
//			@Override
//			public void onPostExecute() {
//			}
//
//			@Override
//			public void onError(String msg) {
//				listener.onError(msg);
//			}
//
//			@Override
//			public void onPost(ShopData sd) {
//				SyncHistory sync = new SyncHistory(context);
//				Shop shop = new Shop(context);
//				Computer computer = new Computer(context);
//				Formater format = new Formater(context);
//				Staffs staff = new Staffs(context);
//				Language lang = new Language(context);
//				HeaderFooterReceipt hf = new HeaderFooterReceipt(context);
//				Bank bank = new Bank(context);
//				CreditCard cd = new CreditCard(context);
//				PaymentDetail pd = new PaymentDetail(context);
//				PaymentAmountButton pb = new PaymentAmountButton(context);
//				try {
//					shop.insertShopProperty(sd.getShopProperty());
//					computer.insertComputer(sd.getComputerProperty());
//					format.insertProperty(sd.getGlobalProperty());
//					staff.insertStaff(sd.getStaffs());
//					lang.insertLanguage(sd.getLanguage());
//					hf.insertHeaderFooterReceipt(sd.getHeaderFooterReceipt());
//					bank.insertBank(sd.getBankName());
//					cd.insertCreditCardType(sd.getCreditCardType());
//					pd.insertPaytype(sd.getPayType());
//					pb.insertPaymentAmountButton(sd.getPaymentAmountButton());
//
//					// log to SyncMasterLogTable
//					sync.insertSyncLog(SyncHistory.SYNC_SHOP_TYPE, 
//							SyncHistory.SYNC_STATUS_SUCCESS);
//					
//					listener.onPostExecute();
//				} catch (Exception e) {
//					// log to SyncMasterLogTable
//					sync.insertSyncLog(SyncHistory.SYNC_SHOP_TYPE, 
//							SyncHistory.SYNC_STATUS_FAIL);
//					
//					Logger.appendLog(context, Utils.LOG_PATH, 
//							Utils.LOG_FILE_NAME, 
//							"Error when add shop data : " + e.getMessage());
//					listener.onError(e.getMessage());
//				}
//			}
//		}).execute(url);
//	}
//	
//	// load product
//	public static void loadProductData(final Context context, final int shopId,
//			final WebServiceWorkingListener progressListener){
//		
//		final String url = Utils.getFullUrl(context);
//
//		new LoadMenu(context, shopId, new LoadMenuListener() {
//
//			@Override
//			public void onPreExecute() {
//				progressListener.onPreExecute();
//			}
//
//			@Override
//			public void onPostExecute() {
//			}
//
//			@Override
//			public void onError(String msg) {
//				progressListener.onError(msg);
//			}
//
//			@Override
//			public void onPost(final MenuGroups mgs) {
//				new LoadProduct(context, shopId, new LoadProductListener() {
//
//					@Override
//					public void onPreExecute() {
//					}
//
//					@Override
//					public void onPostExecute() {
//					}
//
//					@Override
//					public void onError(String msg) {
//						progressListener.onError(msg);
//					}
//
//					@Override
//					public void onPost(ProductGroups pgs) {
//						Products pd = new Products(context);
//						MenuComment mc = new MenuComment(context);
//						SyncHistory sync = new SyncHistory(context);
//						PromotionDiscount promo = new PromotionDiscount(context);
//						try {
//							pd.insertProductGroup(pgs.getProductGroup(), mgs.getMenuGroup());
//							pd.insertProductDept(pgs.getProductDept(), mgs.getMenuDept());
//							pd.insertProducts(pgs.getProduct(), mgs.getMenuItem(), mgs.getMenuComment());
//							pd.insertPComponentGroup(pgs.getPComponentGroup());
//							pd.insertProductComponent(pgs.getPComponentSet());
//							mc.insertMenuComment(mgs.getMenuComment());
//							mc.insertMenuCommentGroup(mgs.getMenuCommentGroup());
//							mc.insertMenuFixComment(mgs.getMenuFixComment());
//							promo.insertPromotionPriceGroup(pgs.getPromotionPriceGroup());
//							promo.insertPromotionProductDiscount(pgs.getPromotionProductDiscount());
//							
//							// log to SyncMasterLogTable
//							sync.insertSyncLog(SyncHistory.SYNC_PRODUCT_TYPE, 
//									SyncHistory.SYNC_STATUS_SUCCESS);
//							
//							// clear all menu picture
//							FileManager fm = new FileManager(context, Utils.IMG_DIR);
//							fm.clear();
//
//							progressListener.onPostExecute();
//						} catch (Exception e) {
//							// log to SyncMasterLogTable
//							sync.insertSyncLog(SyncHistory.SYNC_PRODUCT_TYPE, 
//									SyncHistory.SYNC_STATUS_FAIL);
//							
//							Logger.appendLog(context, Utils.LOG_PATH, 
//								Utils.LOG_FILE_NAME, 
//								"Error when add product data : " + e.getMessage());
//							progressListener.onError(e.getMessage());
//						}
//					}
//				}).execute(url);
//			}
//		}).execute(url);
//	}
	
	
	
	// send sale transaction
	
	
	// load shop data
//	private static class LoadShop extends MPOSMainService{
//		private LoadShopListener mListener;
//		
//		public LoadShop(Context context, int shopId, LoadShopListener listener) {
//			super(context, LOAD_SHOP_METHOD);
//			
//			mProperty = new PropertyInfo();
//			mProperty.setName(SHOP_ID_PARAM);
//			mProperty.setValue(shopId);
//			mProperty.setType(int.class);
//			mSoapRequest.addProperty(mProperty);
//			
//			mListener = listener;
//		}
//
//		@Override
//		protected void onPostExecute(String result) {
//			Gson gson = new Gson();
//			Type type = new TypeToken<ShopData>() {}.getType();
//			try {
//				ShopData shopData = (ShopData) gson.fromJson(result, type);
//				mListener.onPost(shopData);
//			} catch (Exception e) {
//				mListener.onError(result);
//			}
//		}
//
//		@Override
//		protected void onPreExecute() {
//			mListener.onPreExecute();
//		}
//	}
	
	// load products
//	private static class LoadProduct extends MPOSMainService{
//		private LoadProductListener mListener;
//		
//		public LoadProduct(Context context, int shopId, LoadProductListener listener) {
//			super(context, LOAD_PRODUCT_METHOD);
//			
//			mProperty = new PropertyInfo();
//			mProperty.setName(SHOP_ID_PARAM);
//			mProperty.setValue(shopId);
//			mProperty.setType(int.class);
//			mSoapRequest.addProperty(mProperty);
//			
//			mListener = listener;
//		}
//
//		@Override
//		protected void onPostExecute(String result) {
//			Gson gson = new Gson();
//			Type type = new TypeToken<ProductGroups>() {}.getType();
//			ProductGroups productData;
//			try {
//				productData = (ProductGroups) gson.fromJson(result, type);
//				mListener.onPost(productData);
//			} catch (Exception e) {
//				mListener.onError(result);
//				e.printStackTrace();
//			}
//			
//		}
//
//		@Override
//		protected void onPreExecute() {
//			mListener.onPreExecute();
//		}
//	}
	
	// load menu data
//	private static class LoadMenu extends MPOSMainService{
//		private LoadMenuListener mListener;
//		
//		public LoadMenu(Context context, int shopId, LoadMenuListener listener) {
//			super(context, LOAD_MENU_METHOD);
//			
//			mProperty = new PropertyInfo();
//			mProperty.setName(SHOP_ID_PARAM);
//			mProperty.setValue(shopId);
//			mProperty.setType(int.class);
//			mSoapRequest.addProperty(mProperty);
//			
//			mListener = listener;
//		}
//
//		@Override
//		protected void onPostExecute(String result) {
//			Gson gson = new Gson();
//			Type type = new TypeToken<MenuGroups>() {}.getType();
//			try {
//				MenuGroups menuGroup = (MenuGroups) gson.fromJson(result, type);
//				mListener.onPost(menuGroup);
//			} catch (Exception e) {
//				mListener.onError(result);
//				e.printStackTrace();
//			}
//		}
//
//		@Override
//		protected void onPreExecute() {
//			mListener.onPreExecute();
//		}
//	}
}
