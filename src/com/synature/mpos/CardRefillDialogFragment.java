package com.synature.mpos;

import com.synature.mpos.PointServiceBase.MemberInfo;
import com.synature.mpos.point.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
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
	
	private Thread mMsrThread;
	private CardReaderRunnable mCardReaderRunnable;
	
	private ProgressDialog mProgress;
	
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
		mProgress = new ProgressDialog(getActivity());
		mProgress.setCancelable(false);
		mProgress.setMessage(getString(R.string.loading));
	}
	
	private CardReaderRunnable.CardReaderListener mCardReaderListener = new CardReaderRunnable.CardReaderListener() {
		
		@Override
		public void onRead(final String content) {
			getActivity().runOnUiThread(new Runnable(){

				@Override
				public void run() {
					if(!TextUtils.isEmpty(content)){
						new BalanceInquiryCard(getActivity(), "", "", content, 
								mGetBalanceListener).execute(Utils.getFullPointUrl(getActivity()));
					}
				}
			});
		}
	};
	
	private BalanceInquiryCard.GetBalanceListener mGetBalanceListener = 
			new BalanceInquiryCard.GetBalanceListener() {
				
				@Override
				public void onPre() {
					mProgress.show();
				}
				
				@Override
				public void onPost(MemberInfo member) {
					if(mProgress.isShowing())
						mProgress.dismiss();
					if(member != null){
						// check card
						String cardNoParam = member.getSzCardNo();
						String refillUrl = Utils.getRefillUrl(getActivity()) + cardNoParam;
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse(refillUrl));
						startActivity(intent);
						getDialog().dismiss();
					}
				}
				
				@Override
				public void onError(String msg) {
					Log.e(TAG, msg);
					if(mProgress.isShowing())
						mProgress.dismiss();
					new AlertDialog.Builder(getActivity())
					.setTitle(R.string.check_card)
					.setMessage(msg)
					.show();
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
