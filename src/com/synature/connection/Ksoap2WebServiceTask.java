package com.synature.connection;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public abstract class Ksoap2WebServiceTask implements Runnable{
	
	public static final String NAME_SPACE = "http://tempuri.org/"; 
			
	protected String mUrl;
	protected SoapObject mSoapRequest;
	protected int mTimeOut = 30 * 1000;
	protected String mWebMethod;
	protected Context mContext;
	protected PropertyInfo mProperty;
	protected String mResult;
	
	public Ksoap2WebServiceTask(Context c, String url, String method, int timeOut){
		mContext = c;
		mUrl = url;
		mWebMethod = method;
		mTimeOut = timeOut;
		mSoapRequest = new SoapObject(NAME_SPACE, mWebMethod);
	}

	protected abstract void onPostExecute(String result);
	
	@Override
	public void run() {
		System.setProperty("http.keepAlive", "false");
		ConnectivityManager connMgr = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// check server status
			HttpClient httpClient = new DefaultHttpClient();
			try {
				HttpResponse res = httpClient.execute(new HttpGet(mUrl));
				int resCode = res.getStatusLine().getStatusCode();
				if(resCode == 200){
					SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
					envelope.dotNet = true;
					envelope.setOutputSoapObject(mSoapRequest);
					String soapAction = NAME_SPACE + mWebMethod;
					HttpTransportSE androidHttpTransport = new HttpTransportSE(mUrl, mTimeOut);
					androidHttpTransport.debug = true;
					try {
						androidHttpTransport.call(soapAction, envelope);
						if(envelope.bodyIn instanceof SoapObject){
							SoapObject soapResult = (SoapObject) envelope.bodyIn;
							if(soapResult != null){
								mResult = soapResult.getProperty(0).toString();
							}else{
								mResult = "No result!";
							}
						}else if(envelope.bodyIn instanceof SoapFault){
							SoapFault soapFault = (SoapFault) envelope.bodyIn;
							mResult = soapFault.getMessage();
						}
					} catch (IOException e) {
						mResult = e.getMessage();
					} catch (XmlPullParserException e) {
						mResult = e.getMessage();
					}
				}else{
					mResult = "Status: " + resCode + " " + res.getStatusLine().getReasonPhrase();
				}
			} catch (ClientProtocolException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}else{
			mResult = "Cannot connect to network!";
		}
		Activity act = (Activity) mContext;
		act.runOnUiThread(new Runnable(){

			@Override
			public void run() {
				onPostExecute(mResult);
			}
			
		});
	}
}
