package com.synature.mpos;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.synature.mpos.PointServiceBase.MemberInfo;
import com.synature.mpos.database.PaymentDetailDao;
import com.synature.mpos.database.ShopDao;
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
	
	private Thread mMsrThread;
	private TransactionDao mTrans;
	private ShopDao mShop;
	private PointServiceBase.MemberInfo mMemberInfo;
	private CardReaderRunnable mCardReaderRunnable;
	private OnRedeemtionListener mListener;
	
	private int mTransId;
	private int mCompId;
	private int mStaffId;
	private String mCardTagCode;
	private Double mTotalPoint;
	
	private ProgressDialog mProgress;
	private TextView mTvMemberName;
	private TextView mTvMobile;
	private TextView mTvStatus;
	private TextView mTvPoint;
	private TextView mTvTotalPoint;
	private Button mBtnCancel;
	private Button mBtnOk;
	
	public static PointRedeemtionDialogFragment newInstance(int transId, int compId, int staffId){
		PointRedeemtionDialogFragment f = new PointRedeemtionDialogFragment();
		Bundle b = new Bundle();
		b.putInt("transId", transId);
		b.putInt("compId", compId);
		b.putInt("staffId", staffId);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTransId = getArguments().getInt("transId");
		mCompId = getArguments().getInt("compId");
		mStaffId = getArguments().getInt("staffId");
		mTrans = new TransactionDao(getActivity());
		mShop = new ShopDao(getActivity());
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
			mCardTagCode = content;
			getActivity().runOnUiThread(new Runnable(){

				@Override
				public void run() {
					if(!TextUtils.isEmpty(content)){
						OrderTransaction trans = mTrans.getTransaction(mTransId, true);
						new BalanceInquiryCard(getActivity(), "", trans.getTransactionUuid(), content, 
								mGetBalanceListener).execute(Utils.getFullPointUrl(getActivity()));
					}
				}
			});
		}
	};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getDialog().getWindow().setLayout(700, 450);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof OnRedeemtionListener){
			mListener = (OnRedeemtionListener) activity;
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
							mBtnOk.setEnabled(false);
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

	private void redeem(){
		TransactionDao trans = new TransactionDao(getActivity());
		List<OrderDetail> orderLst = trans.listAllOrder(mTransId);
		if(orderLst != null){
			String jsonRedeemItem = "";
			List<PointRedeemtion.RedeemItem> itemLst = new ArrayList<PointRedeemtion.RedeemItem>();
			for(OrderDetail order : orderLst){
				PointRedeemtion.RedeemItem item = new PointRedeemtion.RedeemItem();
				Double orderQty = order.getOrderQty();
				Double orderPrice = order.getProductPrice();
				item.setiProductID(order.getProductId());
				item.setSzItemName(order.getProductName());
				item.setiItemQty(orderQty.intValue());
				item.setiItemPoint(orderPrice.intValue());
				itemLst.add(item);
			}
			Gson gson = new Gson();
			jsonRedeemItem = gson.toJson(itemLst);
			Log.i(TAG, jsonRedeemItem);
			new PointRedeemtion(getActivity(), "", mCardTagCode, jsonRedeemItem, 
					mTransId, mRedeemListener).execute(Utils.getFullPointUrl(getActivity()));
		}
	}
	
	private PointRedeemtion.RedeemtionListener mRedeemListener = new PointRedeemtion.RedeemtionListener() {
				
				@Override
				public void onPre() {		
					mProgress.show();
				}
				
				@Override
				public void onPost() {
					if(mProgress.isShowing())
						mProgress.dismiss();
					Double currentPoint = mMemberInfo.getiCurrentCardPoint() - mTotalPoint;
					LayoutInflater inflater = getActivity().getLayoutInflater();
					View view = inflater.inflate(R.layout.point_balance, null);
					TextView tvPoint = (TextView) view.findViewById(R.id.tvCurrentPoint);
					tvPoint.setText(NumberFormat.getInstance().format(currentPoint));
					new AlertDialog.Builder(getActivity())
					.setTitle("Current Point:")
					.setView(view)
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
						}
					}).show();
					PaymentDetailDao payment = new PaymentDetailDao(getActivity());
					payment.addPaymentDetail(mTransId, mCompId, PaymentDetailDao.PAY_TYPE_CASH, 
							mTotalPoint, mTotalPoint, mCardTagCode, 0, 0, 0, 0, "Point");
					mTrans.updateTransactionPointInfo(mTransId, mMemberInfo.getSzIDCardNo(), 
							mMemberInfo.getSzFirstName() + " " + mMemberInfo.getSzLastName(), 
							mMemberInfo.getiCurrentCardPoint(), currentPoint);
					mTrans.closeTransaction(mTransId, mStaffId, mTotalPoint, mShop.getCompanyVatType(), mShop.getCompanyVatRate());
					mListener.onRedeemSuccess(mTransId, mTotalPoint.intValue(), currentPoint.intValue());
					getDialog().dismiss();
				}
				
				@Override
				public void onError(String msg) {
					if(mProgress.isShowing())
						mProgress.dismiss();
					new AlertDialog.Builder(getActivity())
					.setTitle(R.string.redeem)
					.setMessage(msg)
					.show();
					getDialog().dismiss();
				}
	};
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnCancel:
			getDialog().dismiss();
			break;
		case R.id.btnOk:
			redeem();
			break;
		}
	}
	
	public static interface OnRedeemtionListener{
		void onRedeemSuccess(int transId, int point, int currentPoint);
	}
}
