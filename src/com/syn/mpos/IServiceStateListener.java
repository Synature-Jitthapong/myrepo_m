package com.syn.mpos;

public interface IServiceStateListener {
	public void onProgress();
	public void onSuccess();
	public void onFail(String msg);
}
