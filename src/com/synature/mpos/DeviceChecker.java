package com.synature.mpos;

import android.content.Context;

public class DeviceChecker extends MPOSServiceBase {
	
	public static final String CHECK_DEVICE_METHOD = "WSmPOS_CheckAuthenShopDevice";
	
	private AuthenDeviceListener mListener;

	/**
	 * @author j1tth4
	 *
	 */
	public static interface AuthenDeviceListener extends WebServiceWorkingListener {
		void onPostExecute(int shopId);
	}

	/**
	 * @param context
	 * @param listener
	 */
	public DeviceChecker(Context context, AuthenDeviceListener listener) {
		super(context, CHECK_DEVICE_METHOD);
		mListener = listener;
	}

	@Override
	protected void onPostExecute(String result) {
		try {
			int shopId = Integer.parseInt(result);
			if (shopId > 0)
				mListener.onPostExecute(shopId);
			else if (shopId == 0)
				mListener.onError(mContext
						.getString(R.string.device_not_register));
			else if (shopId == -1)
				mListener.onError(mContext
						.getString(R.string.computer_setting_not_valid));
		} catch (NumberFormatException e) {
			this.mListener.onError(result);
		}
	}

	@Override
	protected void onPreExecute() {
		mListener.onPreExecute();
	}
}
