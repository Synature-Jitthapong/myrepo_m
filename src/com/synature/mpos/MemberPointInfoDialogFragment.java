package com.synature.mpos;

import java.text.NumberFormat;

import com.synature.mpos.PointServiceBase.MemberInfo;
import com.synature.mpos.point.R;

import android.app.Activity;
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

public class MemberPointInfoDialogFragment extends DialogFragment implements OnClickListener{

	public static final String TAG = MemberPointInfoDialogFragment.class.getSimpleName();
	
	public static final int INACTIVE = 0;
	public static final int ACTIVE = 1;
	
	public static final int INFO_MODE = 3;
	public static final int REDEEM_MODE = 4;
	
	private Thread mMsrThread;
	private CardReaderRunnable mCardReaderRunnable;
	private int mMode;
	
	private ProgressDialog mProgress;
	private TextView mTvMemberName;
	private TextView mTvMobile;
	private TextView mTvStatus;
	private TextView mTvRemark;
	private TextView mTvPoint;
	private Button mBtnCancel;
	private Button mBtnOk;
	private Button mBtnClose;
	
	public static MemberPointInfoDialogFragment newInstance(int mode){
		MemberPointInfoDialogFragment f = new MemberPointInfoDialogFragment();
		Bundle b = new Bundle();
		b.putInt("mode", mode);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMode = getArguments().getInt("mode");
		mCardReaderRunnable = new CardReaderRunnable(getActivity(), mCardReaderListener);
		mMsrThread = new Thread(mCardReaderRunnable);
		mProgress = new ProgressDialog(getActivity());
		mProgress.setCancelable(false);
	}
	
	@Override
	public void onStart() {
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
						new BalanceInquiryCard(getActivity(), "", content, 
								mGetBalanceListener).execute(BalanceInquiryCard.POINT_SERVICE_URL);
					}
				}
			});
		}
	};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getDialog().getWindow().setLayout(700, 500);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mTvMemberName = (TextView) view.findViewById(R.id.tvMemberName);
		mTvMobile = (TextView) view.findViewById(R.id.tvMobile);
		mTvStatus = (TextView) view.findViewById(R.id.tvStatus);
		mTvRemark = (TextView) view.findViewById(R.id.tvRemark);
		mTvPoint = (TextView) view.findViewById(R.id.tvPoint);
		mBtnCancel = (Button) view.findViewById(R.id.btnCancel);
		mBtnClose = (Button) view.findViewById(R.id.btnClose);
		mBtnOk = (Button) view.findViewById(R.id.btnOk);
		mBtnCancel.setOnClickListener(this);
		mBtnClose.setOnClickListener(this);
		mBtnOk.setOnClickListener(this);
		switch(mMode){
		case INFO_MODE:
			mBtnCancel.setVisibility(View.GONE);
			mBtnOk.setVisibility(View.GONE);
			mBtnClose.setVisibility(View.VISIBLE);
			break;
		case REDEEM_MODE:
			mBtnCancel.setVisibility(View.VISIBLE);
			mBtnOk.setVisibility(View.VISIBLE);
			mBtnClose.setVisibility(View.GONE);
			break;
		default:
			mBtnCancel.setVisibility(View.GONE);
			mBtnOk.setVisibility(View.GONE);
			mBtnClose.setVisibility(View.VISIBLE);
			break;
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return inflater.inflate(R.layout.fragment_member_point_info, container, false);
	}
	
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
						mTvMemberName.setText(member.getSzFirstName() + " " + member.getSzLastName());
						mTvMobile.setText(member.getSzMobileNo());
						mTvPoint.setText(NumberFormat.getInstance().format(member.getiCurrentCardPoint()));
						if(member.getiCardStatus() == INACTIVE){
							mTvStatus.setText("InActive");
							mTvStatus.setTextColor(Color.RED);
							mTvRemark.setText(member.getSzCardRemark());
						}else{
							mTvStatus.setTextColor(Color.GREEN);
							mTvStatus.setText("Active");
						}
					}
				}
				
				@Override
				public void onError(String msg) {
					if(mProgress.isShowing())
						mProgress.dismiss();
					Log.e(TAG, msg);
				}
	};

	@Override
	public void onClick(View v) {
		getDialog().dismiss();
		switch(v.getId()){
		case R.id.btnCancel:
			break;
		case R.id.btnClose:
			break;
		case R.id.btnOk:
			
			break;
		}
	}
	
}
