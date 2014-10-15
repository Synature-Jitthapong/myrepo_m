package com.synature.mpos;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

public class NetworkConnectionChecker extends AsyncTask<Void, Void, Object>{

	private Context mContext;
	private NetworkCheckerListener mListener;
	
	public NetworkConnectionChecker(Context context, NetworkCheckerListener listener){
		mContext = context;
		mListener = listener;
	}

	@Override
	protected void onPreExecute() {	
		mListener.onPre();
	}
	
	@Override
	protected void onPostExecute(Object result) {
		MyResponse myRes = (MyResponse) result;
		if(myRes.code != -1){
			if(myRes.code == 200){
				mListener.onLine();
			}else{
				mListener.serverProblem(myRes.code, myRes.reasonPhrase);
			}
		}else{
			mListener.offLine(mContext.getString(R.string.cannot_connect_to_network));
		}
	}
	
	@Override
	protected Object doInBackground(Void... arg0){
		MyResponse myRes = new MyResponse();
		ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// check server status
			HttpParams httpParam = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParam, 10 * 1000);
			HttpClient httpClient = new DefaultHttpClient(httpParam);
			HttpResponse res;
			try {
				HttpGet httpGet = new HttpGet(Utils.getFullUrl(mContext));
				res = httpClient.execute(httpGet);
				int resCode = res.getStatusLine().getStatusCode();
				myRes.code = resCode;
				if(resCode != 200){
					myRes.reasonPhrase = res.getStatusLine().getReasonPhrase();
				}
			} catch (ClientProtocolException e) {
				myRes.code = -1;
			} catch (IOException e) {
				myRes.code = -1;
			}
		}else{
			myRes.code = -1;
		}
		return myRes;
	}
	
	public static interface NetworkCheckerListener{
		void onPre();
		void onLine();
		void offLine(String msg);
		void serverProblem(int code, String msg);
	}
	
	private class MyResponse{
		private int code;
		private String reasonPhrase;
	}
}
