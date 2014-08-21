package com.synature.mpos;

public interface WebServiceWorkingListener {
	void onPreExecute();
	void onProgressUpdate(int value);
	void onPostExecute();
	void onError(String msg);
}
