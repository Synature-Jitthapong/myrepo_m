package com.synature.mpos;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.synature.mpos.database.GlobalPropertyDao;
import com.synature.mpos.database.PaymentAmountButtonDao;
import com.synature.mpos.database.PaymentDetailDao;
import com.synature.mpos.database.ShopDao;
import com.synature.mpos.database.TransactionDao;
import com.synature.mpos.database.model.MPOSPaymentDetail;
import com.synature.mpos.database.model.OrderDetail;
import com.synature.mpos.point.R;
import com.synature.pos.PayType;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class PaymentActivity extends Activity implements OnClickListener, 
	PointRedeemtionDialogFragment.OnConfirmPointListener{
	
	public static final int REQUEST_CREDIT_PAY = 1;
	public static final int RESULT_ENOUGH = 2;
	public static final int RESULT_NOT_ENOUGH = 3;

	public static boolean sIsRunning = false;
	
	/*
	 * credit pay not enough result code
	 */
	private int mResultCreditCode = RESULT_NOT_ENOUGH;

	private PaymentDetailDao mPayment;
	private TransactionDao mTrans;
	private GlobalPropertyDao mFormat;
	private PointServiceBase.MemberInfo mMemberInfo;
	
	private List<MPOSPaymentDetail> mPayLst;
	private PaymentAdapter mPaymentAdapter;
	private PaymentButtonAdapter mPaymentButtonAdapter;

	private StringBuilder mStrTotalPay;
	private int mTransactionId;
	private int mComputerId;
	private int mStaffId;
	private double mTotalSalePrice;
	private double mTotalPay;
	private double mTotalPaid;
	private double mPaymentLeft;
	private double mChange;
	private double mPointDeducted;
	
	private ListView mLvPayment;
	private EditText mTxtEnterPrice;
	private TextView mTvTotalPaid;
	private TextView mTvPaymentLeft;
	private TextView mTvTotalPrice;
	private TextView mTvChange;
	private GridView mGvPaymentButton;
	private Button mBtnConfirm;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
	            WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	    LayoutParams params = getWindow().getAttributes();
	    params.width = WindowManager.LayoutParams.MATCH_PARENT;
	    params.height= getResources().getInteger(R.integer.activity_dialog_height);
	    params.alpha = 1.0f;
	    params.dimAmount = 0.5f;
	    getWindow().setAttributes((android.view.WindowManager.LayoutParams) params); 
		setContentView(R.layout.activity_payment);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	    setFinishOnTouchOutside(false);
		mLvPayment = (ListView) findViewById(R.id.lvPayDetail);
		mTxtEnterPrice = (EditText) findViewById(R.id.txtDisplay);
		mTvTotalPaid = (TextView) findViewById(R.id.tvTotalPaid);
		mTvPaymentLeft = (TextView) findViewById(R.id.tvPaymentLeft);
		mTvTotalPrice = (TextView) findViewById(R.id.tvTotalPrice);
		mTvChange = (TextView) findViewById(R.id.tvChange);
		mGvPaymentButton = (GridView) findViewById(R.id.gvPaymentButton);
		mBtnConfirm = (Button) findViewById(R.id.btnConfirm);
		mBtnConfirm.setOnClickListener(mOnConfirmClick);
		
		Intent intent = getIntent();
		mTransactionId = intent.getIntExtra("transactionId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);
		mStaffId = intent.getIntExtra("staffId", 0);
		
		mTrans = new TransactionDao(getApplicationContext());
		mPayment = new PaymentDetailDao(getApplicationContext());
		mFormat = new GlobalPropertyDao(getApplicationContext());
		
		mPaymentAdapter = new PaymentAdapter();
		mPayLst = new ArrayList<MPOSPaymentDetail>();
		mPaymentButtonAdapter = new PaymentButtonAdapter();
		mStrTotalPay = new StringBuilder();
		mLvPayment.setAdapter(mPaymentAdapter);
		mGvPaymentButton.setAdapter(mPaymentButtonAdapter);
		setupPayTypeButton();
		displayEnterPrice();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_CREDIT_PAY){
			mResultCreditCode = resultCode;
		}
	}
	
	@Override
	protected void onResume() {
		summary();
		loadPayDetail();
//		if(mResultCreditCode == RESULT_ENOUGH)
//			confirm();
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
		sIsRunning = true;
	}

	@Override
	protected void onStop() {
		sIsRunning = false;
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_payment, menu);
		return true;
	}

	OnClickListener mOnConfirmClick = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			confirm();
		}
		
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			cancel();
			return true;
		case R.id.itemConfirm:
			confirm();
			return true;
		default:
			return super.onOptionsItemSelected(item);	
		}
	}
	
	private void summary(){ 
		OrderDetail summOrder = mTrans.getSummaryOrder(mTransactionId, true);
		mTotalSalePrice = summOrder.getTotalSalePrice();
		mTvTotalPrice.setText(mFormat.currencyFormat(mTotalSalePrice));
	}
	
	private class PaymentAdapter extends BaseAdapter{
		
		private LayoutInflater mInflater;
		
		public PaymentAdapter(){
			mInflater = getLayoutInflater();
		}

		@Override
		public int getCount() {
			return mPayLst != null ? mPayLst.size() : 0;
		}

		@Override
		public MPOSPaymentDetail getItem(int position) {
			return mPayLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			PaymentDetailViewHolder holder;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.payment_detail_template, parent, false);
				holder = new PaymentDetailViewHolder();
				holder.tvPayType = (TextView) convertView.findViewById(R.id.tvPayType);
				holder.tvPayDetail = (TextView) convertView.findViewById(R.id.tvPayDetail);
				holder.tvPayAmount = (TextView) convertView.findViewById(R.id.tvPayAmount);
				holder.btnDel = (Button) convertView.findViewById(R.id.btnDelete);
				convertView.setTag(holder);
			}else{
				holder = (PaymentDetailViewHolder) convertView.getTag();
			}
			final MPOSPaymentDetail payment = mPayLst.get(position);
			holder.tvPayType.setText(payment.getPayTypeName());
			holder.tvPayDetail.setText(payment.getRemark());
			holder.tvPayAmount.setText(mFormat.currencyFormat(payment.getTotalPay()));
			holder.btnDel.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					deletePayment(payment.getTransactionId(), payment.getPayTypeId());
				}
				
			});
			
			return convertView;
		}
		
		private class PaymentDetailViewHolder{
			TextView tvPayType;
			TextView tvPayDetail;
			TextView tvPayAmount;
			Button btnDel;
		}
	}
	
	private void loadPayDetail(){
		mPayLst = mPayment.listPayment(mTransactionId);
		mPaymentAdapter.notifyDataSetChanged();
		mTotalPaid = mPayment.getTotalPayAmount(mTransactionId);
		mPaymentLeft = mTotalSalePrice - mTotalPaid;
		mChange = mTotalPaid - mTotalSalePrice;
		mTvTotalPaid.setText(mFormat.currencyFormat(mTotalPaid));
		if(mPaymentLeft < 0)
			mPaymentLeft = 0.0d;
		if(mChange < 0)
			mChange = 0.0d;
		mTvPaymentLeft.setText(mFormat.currencyFormat( mPaymentLeft));
		mTvChange.setText(mFormat.currencyFormat(mChange));
	}
	
	private void deletePayment(int transactionId, int payTypeId){
		mPayment.deletePaymentDetail(transactionId, payTypeId);
		loadPayDetail();
	}
	
	private void addPayment(int payTypeId, String remark){
		if(mTotalPay > 0 && mPaymentLeft > 0){ 
			mPayment.addPaymentDetail(mTransactionId, mComputerId, payTypeId, mTotalPay, 
					mTotalPay >= mPaymentLeft ? mPaymentLeft : mTotalPay, "",
					0, 0, 0, 0, remark);
			loadPayDetail();
		}
		mStrTotalPay = new StringBuilder();
		displayEnterPrice();
	}
	
	private void calculateInputPrice(){
		try {
			mTotalPay = Utils.stringToDouble(mStrTotalPay.toString());
		} catch (ParseException e) {
			mTotalPay = 0.0d;
		}
	}
	
	private void displayEnterPrice(){
		calculateInputPrice();
		mTxtEnterPrice.setText(mFormat.currencyFormat(mTotalPay));
	}
	
	private void pointPay(int payTypeId){
		if(mTotalSalePrice > 0 && mPaymentLeft > 0){
			String transUUID = mTrans.getTransaction(mTransactionId, true).getTransactionUuid();
			PointRedeemtionDialogFragment f = PointRedeemtionDialogFragment.newInstance(
					transUUID, payTypeId, mPaymentLeft);
			f.show(getFragmentManager(), "RedeemPointDialog");
		}
	}
	
	private void creditPay(){
		if(mTotalSalePrice > 0 && mPaymentLeft > 0){
			Intent intent = new Intent(PaymentActivity.this, CreditPayActivity.class);
			intent.putExtra("transactionId", mTransactionId);
			intent.putExtra("computerId", mComputerId);
			intent.putExtra("paymentLeft", mPaymentLeft);
			startActivityForResult(intent, REQUEST_CREDIT_PAY);
		}
	}

	private class OnRedeemPointListener implements PointRedeemtion.RedeemtionListener{

		private ProgressDialog progress;
		
		public OnRedeemPointListener() {
			progress = new ProgressDialog(PaymentActivity.this);
			progress.setMessage(getString(R.string.loading));
			progress.setCancelable(false);
		}
		
		@Override
		public void onPre() {
			progress.show();
		}
	
		@Override
		public void onPost() {
			if(progress.isShowing())
				progress.dismiss();
			double currentPoint = mMemberInfo.getiCurrentCardPoint() - mPointDeducted;
			LayoutInflater inflater = getLayoutInflater();
			View view = inflater.inflate(R.layout.point_balance, null);
			TextView tvPoint = (TextView) view.findViewById(R.id.tvCurrentPoint);
			tvPoint.setText(NumberFormat.getInstance().format(currentPoint));
			new AlertDialog.Builder(PaymentActivity.this)
			.setTitle(R.string.current_point)
			.setView(view)
			.setCancelable(false)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// close the transaction finally
					closeTransaction();
					setResultAndFinish();
				}
			}).show();
			
			mTrans.updateTransactionPointInfo(mTransactionId, mMemberInfo.getSzIDCardNo(), 
					mMemberInfo.getSzFirstName() + " " + mMemberInfo.getSzLastName(), 
					mMemberInfo.getiCurrentCardPoint(), currentPoint);
		}
	
		@Override
		public void onError(String msg) {
			if(progress.isShowing())
				progress.dismiss();
			new AlertDialog.Builder(PaymentActivity.this)
			.setTitle(R.string.fail)
			.setMessage(msg)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			})
			.show();
		}
		
	}

	private void confirm() {
		if(mTotalPaid >= mTotalSalePrice){
			if(mMemberInfo != null){
				deductPoint();
			}else{
				closeTransaction();
				setResultAndFinish();
			}
			// open cash drawer
			WintecCashDrawer drw = new WintecCashDrawer(getApplicationContext());
			drw.openCashDrawer();
			drw.close();
		}else{
			new AlertDialog.Builder(PaymentActivity.this)
			.setMessage(R.string.enter_enough_money)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			})
			.show();
		}
	}

	private void deductPoint(){
		List<OrderDetail> orderLst = mTrans.listAllOrder(mTransactionId);
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
			new PointRedeemtion(PaymentActivity.this, "", mMemberInfo.getSzCardTagCode(), jsonRedeemItem, 
					mTransactionId, new OnRedeemPointListener()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
							Utils.getFullPointUrl(PaymentActivity.this));
			Log.i("json_redeem_item", jsonRedeemItem);
		}
	}
	
	private void setResultAndFinish(){
		mChange = mTotalPaid - mTotalSalePrice;
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("totalSalePrice", mTotalSalePrice);
		intent.putExtra("totalPaid", mTotalPaid);
		intent.putExtra("change", mChange);
		intent.putExtra("transactionId", mTransactionId);
		intent.putExtra("staffId", mStaffId);
		setResult(RESULT_OK, intent);
		finish();	
	}
	
	private void closeTransaction(){
		ShopDao shop = new ShopDao(this);
		mTrans.closeTransaction(mTransactionId, mStaffId, mTotalSalePrice, 
				shop.getCompanyVatType(), shop.getCompanyVatRate());
	}
	
	public void cancel() {
		mPayment.deleteAllPaymentDetail(mTransactionId);
		finish();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btn0:
			mStrTotalPay.append("0");
			displayEnterPrice();
			break;
		case R.id.btn1:
			mStrTotalPay.append("1");
			displayEnterPrice();
			break;
		case R.id.btn2:
			mStrTotalPay.append("2");
			displayEnterPrice();
			break;
		case R.id.btn3:
			mStrTotalPay.append("3");
			displayEnterPrice();
			break;
		case R.id.btn4:
			mStrTotalPay.append("4");
			displayEnterPrice();
			break;
		case R.id.btn5:
			mStrTotalPay.append("5");
			displayEnterPrice();
			break;
		case R.id.btn6:
			mStrTotalPay.append("6");
			displayEnterPrice();
			break;
		case R.id.btn7:
			mStrTotalPay.append("7");
			displayEnterPrice();
			break;
		case R.id.btn8:
			mStrTotalPay.append("8");
			displayEnterPrice();
			break;
		case R.id.btn9:
			mStrTotalPay.append("9");
			displayEnterPrice();
			break;
		case R.id.btnClear:
			mStrTotalPay = new StringBuilder();
			displayEnterPrice();
			break;
		case R.id.btnDel:
			try {
				mStrTotalPay.deleteCharAt(mStrTotalPay.length() - 1);
			} catch (Exception e) {
				mStrTotalPay = new StringBuilder();
			}
			displayEnterPrice();
			break;
		case R.id.btnDot:
			mStrTotalPay.append(".");
			displayEnterPrice();
			break;
		case R.id.btnEnter:
			if(!mStrTotalPay.toString().isEmpty()){
				addPayment(PaymentDetailDao.PAY_TYPE_CASH, "");
			}
			break;
		}
	}
	
	private void popupOtherPayment(String payTypeName, final int payTypeId){
		LayoutInflater inflater = (LayoutInflater)
				this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.other_payment_layout, null);
		final EditText txtAmount = (EditText) v.findViewById(R.id.txtAmount);
		final EditText txtRemark = (EditText) v.findViewById(R.id.txtRemark);
		txtAmount.setText(mFormat.currencyFormat(mTotalSalePrice));
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(payTypeName);
		builder.setView(v);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		builder.setPositiveButton(android.R.string.ok, null);
		final AlertDialog d = builder.create();
		d.show();
		d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				double paid = 0;
				try {
					paid = Utils.stringToDouble(txtAmount.getText().toString());
					if(paid > 0){
						mStrTotalPay = new StringBuilder();
						mStrTotalPay.append(mFormat.currencyFormat(paid));
						calculateInputPrice();
						addPayment(payTypeId, txtRemark.getText().toString());
						d.dismiss();
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
	}
	
	private void setupPayTypeButton(){
		List<PayType> payTypeLst = mPayment.listPayType();
		LinearLayout payTypeContent = (LinearLayout) findViewById(R.id.payTypeContent);
		payTypeContent.removeAllViews();
		for(final PayType payType : payTypeLst){
			final Button btnPayType = new Button(PaymentActivity.this);
			btnPayType.setMinWidth(128);
			btnPayType.setMinHeight(64);
			btnPayType.setText(payType.getPayTypeName());
			btnPayType.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					int payTypeId = payType.getPayTypeID();
					if(payTypeId == PaymentDetailDao.PAY_TYPE_CASH){
						
					}else if(payTypeId == PaymentDetailDao.PAY_TYPE_CREDIT){
						creditPay();
					}else if(payTypeId == PaymentDetailDao.PAY_TYPE_POINT){
						pointPay(payTypeId);
					}
				}
				
			});
			payTypeContent.addView(btnPayType);
		}
	}
	
	public class PaymentButtonAdapter extends BaseAdapter{
		
		private PaymentAmountButtonDao mPaymentButton;
		private List<com.synature.pos.PaymentAmountButton> mPaymentButtonLst;
		private LayoutInflater mInflater;
		
		public PaymentButtonAdapter(){
			mInflater = (LayoutInflater)
					PaymentActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mPaymentButton = new PaymentAmountButtonDao(getApplicationContext());
			mPaymentButtonLst = mPaymentButton.listPaymentButton();
		}
		
		@Override
		public int getCount() {
			return mPaymentButtonLst != null ? mPaymentButtonLst.size() : 0;
		}

		@Override
		public com.synature.pos.PaymentAmountButton getItem(int position) {
			return mPaymentButtonLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final com.synature.pos.PaymentAmountButton paymentButton = 
					mPaymentButtonLst.get(position);
			ViewHolder holder;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.button_template, null);
				holder.btnPayment = (Button) convertView;
				holder.btnPayment.setMinWidth(128);
				holder.btnPayment.setMinHeight(96);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}	
			holder.btnPayment.setText(mFormat.currencyFormat(
					paymentButton.getPaymentAmount()));
			holder.btnPayment.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					mStrTotalPay = new StringBuilder();
					mStrTotalPay.append(mFormat.currencyFormat(
							paymentButton.getPaymentAmount()));
					calculateInputPrice();
					addPayment(PaymentDetailDao.PAY_TYPE_CASH, "");
				}
				
			});
			return convertView;
		}
		
		class ViewHolder{
			Button btnPayment;
		}
	}

	// on point confirm
	@Override
	public void onConfirmPoint(PointServiceBase.MemberInfo memberInfo, int payTypeId) {
		mMemberInfo = memberInfo;
		if(mMemberInfo != null){
			if(mPaymentLeft > 0){
				double money = convertPointToMoney(memberInfo.getiCurrentCardPoint());
				mTotalPay = money >= mPaymentLeft ? mPaymentLeft : money; 
				mPointDeducted = mTotalPay;
				mPayment.addPaymentDetail(mTransactionId, mComputerId, payTypeId, mTotalPay, 
						mTotalPay, memberInfo.getSzCardTagCode(), 0, 0, 0, 0, "");
				loadPayDetail();
			}
		}
	}
	
	@Override
	public void onCancel(){
		mMemberInfo = null;
	}

	private double convertPointToMoney(double point){
		double ratio = Utils.getPointRatio(this);
		return point / ratio;
	}
}
