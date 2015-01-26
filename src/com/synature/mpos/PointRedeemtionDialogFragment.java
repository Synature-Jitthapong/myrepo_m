package com.synature.mpos;

import java.text.NumberFormat;
import com.synature.mpos.PointServiceBase.MemberInfo;
import com.synature.mpos.database.TransactionDao;
import com.synature.mpos.database.model.OrderDetail;
import com.synature.mpos.database.model.OrderTransaction;
import com.synature.mpos.point.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
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

public class PointRedeemtionDialogFragment extends DialogFragment implements OnClickListener{

	public static final String TAG = PointRedeemtionDialogFragment.class.getSimpleName();
	public static final int WINDOW_WIDTH = 700;
	public static final int WINDOW_HEIGHT = 450;
	
	private Thread mMsrThread;
	private TransactionDao mTrans;
	private PointServiceBase.MemberInfo mMemberInfo;
	private CardReaderRunnable mCardReaderRunnable;
	private OnConfirmPointListener mListener;
	
	private int mTransId;
	private int mPayTypeId;
	private double mTotalPoint;
	
	private ProgressDialog mProgress;
	private TextView mTvMemberName;
	private TextView mTvMobile;
	private TextView mTvStatus;
	private TextView mTvPoint;
	private TextView mTvTotalPoint;
	private Button mBtnCancel;
	private Button mBtnOk;
	
	public static PointRedeemtionDialogFragment newInstance(int transId, int payTypeId){
		PointRedeemtionDialogFragment f = new PointRedeemtionDialogFragment();
		Bundle b = new Bundle();
		b.putInt("transId", transId);
		b.putInt("payTypeId", payTypeId);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTransId = getArguments().getInt("transId");
		mPayTypeId = getArguments().getInt("payTypeId");
		mTrans = new TransactionDao(getActivity());
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
						OrderTransaction trans = mTrans.getTransaction(mTransId, true);
						new BalanceInquiryCard(getActivity(), "", trans.getTransactionUuid(), content, 
								mGetBalanceListener).executeOnExecutor(
										AsyncTask.THREAD_POOL_EXECUTOR, Utils.getFullPointUrl(getActivity()));
					}
				}
			});
		}
	};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getDialog().getWindow().setLayout(WINDOW_WIDTH, WINDOW_HEIGHT);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof OnConfirmPointListener){
			mListener = (OnConfirmPointListener) activity;
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mTvMemberName = (TextView) view.findViewById(R.id.tvMemberName);
		mTvMobile = (TextView) view.findViewById(R.id.tvMobile);
		mTvStatus = (TextView) view.findViewById(R.id.tvStatus);
		mTvPoint = (TextView) view.findViewById(R.id.tvPoint);
		mTvTotalPoint = (TextView) view.findViewById(R.id.tvTotalPoint);
		mBtnCancel = (Button) view.findViewById(R.id.btnCancel);
		mBtnOk = (Button) view.findViewById(R.id.btnOk);
		mBtnCancel.setOnClickListener(this);
		mBtnOk.setOnClickListener(this);
		summary();
	}
	
	private void summary(){
		OrderDetail sumOrder = mTrans.getSummaryOrder(mTransId, true);
		mTotalPoint = sumOrder.getTotalRetailPrice();
		mTvTotalPoint.setText(NumberFormat.getInstance().format(mTotalPoint));
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return inflater.inflate(R.layout.fragment_redeem_point, container, false);
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
						mMemberInfo = member;
						mTvStatus.setText(null);
						mTvMemberName.setText(member.getSzFirstName() + " " + member.getSzLastName());
						mTvMobile.setText(member.getSzMobileNo());
						mTvPoint.setText(NumberFormat.getInstance().format(member.getiCurrentCardPoint()));
						if(member.getiCardStatus() == MemberPointInfoDialogFragment.INACTIVE){
							mTvStatus.setTextColor(Color.RED);
							mTvStatus.setText(member.getSzCardRemark());
							mBtnOk.setEnabled(false);
						}else{
							mTvStatus.setTextColor(Color.BLACK);
							mBtnOk.setEnabled(true);
						}
						if(member.getiCurrentCardPoint() < mTotalPoint){
							mTvStatus.setText(R.string.point_not_enough);
							mTvPoint.setTextColor(Color.RED);
							if(member.getiCurrentCardPoint() > 0){
								mBtnOk.setEnabled(true);
							}else{
								mBtnOk.setEnabled(false);
							}
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
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {}
					})
					.show();
				}
	};

	private void confirm(){
		mListener.onConfirmPoint(mMemberInfo, mPayTypeId);
	}
		
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnCancel:
			getDialog().dismiss();
			break;
		case R.id.btnOk:
			confirm();
			break;
		}
	}
	
	public static interface OnConfirmPointListener{
		void onConfirmPoint(PointServiceBase.MemberInfo memberInfo, int payTypeId);
	}
}
