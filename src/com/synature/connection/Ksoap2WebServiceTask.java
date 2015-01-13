package com.synature.connection;

import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.synature.mpos.R;

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
		//System.setProperty("http.keepAlive", "false");
		ConnectivityManager connMgr = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(mSoapRequest);
			String soapAction = NAME_SPACE + mWebMethod;
			HttpTransportSE androidHttpTransport = new HttpTransportSE(mUrl, mTimeOut);
			//androidHttpTransport.debug = true;
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
			mResult = mContext.getString(R.string.cannot_connect_to_network);
		}
		onPostExecute(mResult);
	}
}
