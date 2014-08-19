package com.synature.mpos;

public interface WebServiceWorkingListener {
	void onPreExecute();
	void onPostExecute();
	void onError(String msg);
}
