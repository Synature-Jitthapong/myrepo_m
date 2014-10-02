package com.synature.mpos;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class CardReaderRunnable implements Runnable{

	public static final String TAG = CardReaderRunnable.class.getSimpleName();
	
	private WintecMagneticReader mMsr;
	private boolean mIsRead = false;
	
	private CardReaderListener mListener;
	
	public CardReaderRunnable(Context context, CardReaderListener listener){
		mMsr = new WintecMagneticReader(context);
		mListener = listener;
	}
	
	@Override
	public void run() {
		while(mIsRead){
			String content = mMsr.getTrackData();
			if(!TextUtils.isEmpty(content)){
				Log.i(TAG, content);

				String[] track1 = content.split(":");
				String cardTagCode = track1[1].replace("?", "").replace("\r", "");//"A4605B579942C";
				mListener.onRead(cardTagCode);
			}
		}
		mMsr.close();
	}

	public boolean ismIsRead() {
		return mIsRead;
	}

	public void setmIsRead(boolean mIsRead) {
		this.mIsRead = mIsRead;
	}

	public static interface CardReaderListener{
		void onRead(String content);
	}
}
