package com.synature.mpos;

import com.synature.mpos.PointServiceBase.MemberInfo;
import com.synature.mpos.point.R;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

public class CardRefillDialogFragment extends DialogFragment{

	public static final String TAG = CardRefillDialogFragment.class.getSimpleName();
			
	public static final String REFILL_URL = "http://www.abcpoint.com";
	
	private Thread mMsrThread;
	private CardReaderRunnable mCardReaderRunnable;
	
	public static CardRefillDialogFragment newInstance(){
		CardRefillDialogFragment f = new CardRefillDialogFragment();
		return f;
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCardReaderRunnable = new CardReaderRunnable(getActivity(), mCardReaderListener);
		mMsrThread = new Thread(mCardReaderRunnable);
	}
	
	private CardReaderRunnable.CardReaderListener mCardReaderListener = new CardReaderRunnable.CardReaderListener() {
		
		@Override
		public void onRead(final String content) {
			getActivity().runOnUiThread(new Runnable(){

				@Override
				public void run() {
					if(!TextUtils.isEmpty(content)){
						new BalanceInquiryCard(getActivity(), "", "", content, 
								mGetBalanceListener).execute(BalanceInquiryCard.POINT_SERVICE_URL);
					}
				}
			});
		}
	};
	
	private BalanceInquiryCard.GetBalanceListener mGetBalanceListener = 
			new BalanceInquiryCard.GetBalanceListener() {
				
				@Override
				public void onPre() {
				}
				
				@Override
				public void onPost(MemberInfo member) {
					if(member != null){
						// check card
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse(REFILL_URL));
						startActivity(intent);
						getDialog().dismiss();
					}
				}
				
				@Override
				public void onError(String msg) {
					Log.e(TAG, msg);
				}
	};
	
	@Override
	public void onStart() {
		mCardReaderRunnable.setmIsRead(true);
		mMsrThread.start();
		Log.i(TAG, "Start msr thread");
		super.onStart();
	}

	@Override
	public void onStop() {
		if(mMsrThread != null){
			mMsrThread.interrupt();
			mMsrThread = null;
			mCardReaderRunnable.setmIsRead(false);
		}
		Log.i(TAG, "Stop msr thread");
		super.onStop();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getDialog().getWindow().setLayout(600, 450);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return inflater.inflate(R.layout.card_refill_web, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
}
