package com.synature.mpos;

import java.text.NumberFormat;

import com.synature.mpos.PointServiceBase.MemberInfo;
import com.synature.mpos.point.R;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class MemberPointInfoDialogFragment extends DialogFragment{

	public static final String TAG = MemberPointInfoDialogFragment.class.getSimpleName();

	public static final int INACTIVE = 0;
	public static final int ACTIVE = 1;
	
	private Thread mMsrThread;
	private CardReaderRunnable mCardReaderRunnable;

	private ProgressDialog mProgress;
	private TextView mTvMemberName;
	private TextView mTvMobile;
	private TextView mTvPoint;
	private TextView mTvStatus;
	private Button mBtnClose;
	
	public static MemberPointInfoDialogFragment newInstance(){
		MemberPointInfoDialogFragment f = new MemberPointInfoDialogFragment();
		return f;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getDialog().getWindow().setLayout(600, 450);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCardReaderRunnable = new CardReaderRunnable(getActivity(), mCardReaderListener);
		mMsrThread = new Thread(mCardReaderRunnable);
		mProgress = new ProgressDialog(getActivity());
		mProgress.setCancelable(false);
	}

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
			mCardReaderRunnable.setmIsRead(false);
			mMsrThread.interrupt();
			mMsrThread = null;
			Log.i(TAG, "Stop msr thread");
		}
		super.onStop();
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
					mProgress.setMessage(getString(R.string.loading));
					mProgress.show();
				}
				
				@Override
				public void onPost(MemberInfo member) {
					if(mProgress.isShowing())
						mProgress.dismiss();
					if(member != null){
						mTvStatus.setText(null);
						mTvMemberName.setText(member.getSzFirstName() + " " + member.getSzLastName());
						mTvMobile.setText(member.getSzMobileNo());
						mTvPoint.setText(NumberFormat.getInstance().format(member.getiCurrentCardPoint()));
						if(member.getiCardStatus() == INACTIVE){
							mTvStatus.setTextColor(Color.RED);
							mTvStatus.setText(member.getSzCardRemark());
						}else{
							mTvStatus.setTextColor(Color.BLACK);
						}
					}
				}
				
				@Override
				public void onError(String msg) {
					if(mProgress.isShowing())
						mProgress.dismiss();
					new AlertDialog.Builder(getActivity())
					.setTitle(R.string.check_card)
					.setMessage(msg)
					.show();
				}
	};
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mTvMemberName = (TextView) view.findViewById(R.id.tvMemberName);
		mTvMobile = (TextView) view.findViewById(R.id.tvMobile);
		mTvPoint = (TextView) view.findViewById(R.id.tvPoint);
		mTvStatus = (TextView) view.findViewById(R.id.tvStatus);
		mBtnClose = (Button) view.findViewById(R.id.btnClose);
		mBtnClose.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				getDialog().dismiss();
			}
			
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return inflater.inflate(R.layout.fragment_member_point_info, container, false);
	}

}
