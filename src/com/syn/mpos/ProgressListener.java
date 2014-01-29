package com.syn.mpos;

public interface ProgressListener {
	void onPre();
	void onPost();
	void onError(String msg);
}
