package com.syn.pos.mobile.mpos;

public interface IServiceStateListener {
	public void onProgress();
	public void onSuccess();
	public void onFail(String msg);
}
