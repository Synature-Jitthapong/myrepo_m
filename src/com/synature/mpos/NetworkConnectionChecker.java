package com.synature.mpos;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkConnectionChecker implements Runnable{

	private Context mContext;
	private NetworkCheckerListener mListener;
	
	public NetworkConnectionChecker(Context context, NetworkCheckerListener listener){
		mContext = context;
		mListener = listener;
	}
	
	@Override
	public void run() {
		ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// check server status
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse res;
			try {
				res = httpClient.execute(new HttpGet(Utils.getFullUrl(mContext)));
				int resCode = res.getStatusLine().getStatusCode();
				if(resCode == 200){
					mListener.onLine();
				}else{
					mListener.serverProblem(resCode, res.getStatusLine().getReasonPhrase());
				}
			} catch (ClientProtocolException e) {
				mListener.offLine();
			} catch (IOException e) {
				mListener.offLine();
			}
		}else{
			mListener.offLine();
		}
	}

	public static interface NetworkCheckerListener{
		void onLine();
		void offLine();
		void serverProblem(int code, String msg);
	}
}
