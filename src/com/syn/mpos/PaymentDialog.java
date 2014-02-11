package com.syn.mpos;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.R;
import com.syn.mpos.provider.PaymentDetail;
import com.syn.mpos.provider.Transaction;
import com.syn.pos.Payment;
import android.os.Bundle;
import android.os.Handler;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class PaymentDialog extends DialogFragment  implements OnClickListener {
	//private final String TAG = "PaymentActivity";
	private double mTotalSalePrice;
	private static int sTransactionId;
	private static int sComputerId;
	private static int sStaffId;
	private Transaction mTransaction;
	private PaymentDetail mPayment;
	private List<Payment.PaymentDetail> mPayLst;
	private PaymentAdapter mPaymentAdapter;
	
	private StringBuilder mStrTotalPay;
	private double mTotalPay;
	private double mTotalPaid;
	private double mPaymentLeft;
	private double mChange;
	
	private ListView mLvPayment;
	private Button mBtnCash;
	private Button mBtnCredit;
	private EditText mTxtEnterPrice;
	private EditText mTxtTotalPaid;
	private EditText mTxtPaymentLeft;
	private EditText mTxtTotalPrice;
	

	public static PaymentDialog newInstance(int transactionId, int computerId, int staffId){
		Bundle b = new Bundle();
		b.putInt("transactionId", transactionId);
		b.putInt("computerId", computerId);
		b.putInt("staffId", staffId);
		PaymentDialog f = new PaymentDialog();
		f.setArguments(b);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		sTransactionId = getArguments().getInt("transactionId");
		sComputerId = getArguments().getInt("computerId");
		sStaffId = getArguments().getInt("staffId");
		
		mTransaction = new Transaction(MPOSApplication.getWriteDatabase());
		mPayment = new PaymentDetail(MPOSApplication.getWriteDatabase());
		mPaymentAdapter = new PaymentAdapter();
		mPayLst = new ArrayList<Payment.PaymentDetail>();
		mStrTotalPay = new StringBuilder();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// TODO Auto-generated method stub
		super.onDismiss(dialog);
	}

	@Override
	public Dialog getDialog() {
		return super.getDialog();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = 
				(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.activity_payment, null);
		mLvPayment = (ListView) v.findViewById(R.id.lvPayDetail);
		mTxtEnterPrice = (EditText) v.findViewById(R.id.txtEnterPrice);
		mTxtTotalPaid = (EditText) v.findViewById(R.id.txtTotalPaid);
		mTxtPaymentLeft = (EditText) v.findViewById(R.id.txtPaymentLeft);
		mTxtTotalPrice = (EditText) v.findViewById(R.id.txtTotalPrice);
		mBtnCash = (Button) v.findViewById(R.id.btnCash);
		mBtnCredit = (Button) v.findViewById(R.id.btnCredit);
		mBtnCredit.setOnClickListener(this);
		mBtnCash.setOnClickListener(this);
		mLvPayment.setAdapter(mPaymentAdapter);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.payment);
		builder.setView(v);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		builder.setPositiveButton(android.R.string.ok, null);
		
		if(mTransaction.getTransaction(sTransactionId, 
				sComputerId).getTransactionStatusId() == Transaction.TRANS_STATUS_SUCCESS){
			getDialog().dismiss();
		}else{
			summary();
			loadPayDetail();
		}
		return builder.create();
	}

	private void summary(){ 
		mTotalSalePrice = mTransaction.getTransactionVatable(sTransactionId, sComputerId);
		displayTotalPrice();
	}
	
	private class PaymentAdapter extends BaseAdapter{
		
		private LayoutInflater inflater;
		
		public PaymentAdapter(){
			inflater = (LayoutInflater)
					getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mPayLst != null ? mPayLst.size() : 0;
		}

		@Override
		public Payment.PaymentDetail getItem(int position) {
			return mPayLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Payment.PaymentDetail payment = mPayLst.get(position);
			View rowView = convertView;
			
			rowView = inflater.inflate(R.layout.payment_detail_template, null);
			TextView tvPayType = (TextView) rowView.findViewById(R.id.tvPayType);
			TextView tvPayDetail = (TextView) rowView.findViewById(R.id.tvPayDetail);
			TextView tvPayAmount = (TextView) rowView.findViewById(R.id.tvPayAmount);
			Button imgDel = (Button) rowView.findViewById(R.id.btnDelete);
			
			String payTypeCash = getActivity().getString(R.string.cash);
			String payTypeCredit = getActivity().getString(R.string.credit);
			String payTypeName = payment.getPayTypeID() == PaymentDetail.PAY_TYPE_CASH ? payTypeCash : payTypeCredit;
			if(payment.getPayTypeName() != null){
				payTypeName = payment.getPayTypeName();
			}
			
			tvPayType.setText(payTypeName);
			tvPayDetail.setText(payment.getRemark());
			tvPayAmount.setText(MPOSApplication.getGlobalProperty().currencyFormat(payment.getPayAmount()));
			imgDel.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					deletePayment(payment.getPayTypeID());
				}
				
			});
			
			return rowView;
		}
	}
	
	private void loadPayDetail(){
		mPayLst = mPayment.listPayment(sTransactionId, sComputerId);
		mPaymentAdapter.notifyDataSetChanged();
		
		mTotalPaid = mPayment.getTotalPaid(sTransactionId, sComputerId);
		
		mPaymentLeft = mTotalSalePrice - mTotalPaid; 

		mTxtTotalPaid.setText(MPOSApplication.getGlobalProperty().currencyFormat(mTotalPaid));
		if(mPaymentLeft < 0)
			mPaymentLeft = 0.0f;
		
		mTxtPaymentLeft.setText(MPOSApplication.getGlobalProperty().currencyFormat(mPaymentLeft));
	}
	
	private void deletePayment(int paymentId){
		mPayment.deletePaymentDetail(paymentId);
		loadPayDetail();
	}
	
	private void addPayment(){
		if(mTotalPay > 0 && mPaymentLeft > 0){
				mPayment.addPaymentDetail(sTransactionId, 
						sComputerId, PaymentDetail.PAY_TYPE_CASH, mTotalPay, "",
						0, 0, 0, 0);
			loadPayDetail();
		}
		mStrTotalPay = new StringBuilder();
		displayEnterPrice();
	}
	
	private void displayTotalPrice(){
		mTxtTotalPrice.setText(MPOSApplication.getGlobalProperty().currencyFormat(mTotalSalePrice));
		displayEnterPrice();
	}
	
	private void calculateInputPrice(){
		try {
			mTotalPay = Float.parseFloat(mStrTotalPay.toString());
		} catch (NumberFormatException e) {
			mTotalPay = 0.0f;
		}
	}
	
	private void displayEnterPrice(){
		calculateInputPrice();
		mTxtEnterPrice.setText(MPOSApplication.getGlobalProperty().currencyFormat(mTotalPay));
	}
	
	public void creditPay(){
		if(mTotalSalePrice > 0 && mPaymentLeft > 0){
			final CreditPayDialogBuilder builder = 
					new CreditPayDialogBuilder(getActivity(), sTransactionId, sComputerId, mPaymentLeft);
			builder.setTitle(R.string.credit_pay);
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
					builder.addPayment(new CreditPayDialogBuilder.OnCreditPayListener() {
						
						@Override
						public void onRequiredSomeParam() {
						}
						
						@Override
						public void onOk() {
							d.dismiss();
							onResume();
						}
						
						@Override
						public void onCancel() {
							d.dismiss();
						}
					});
				}
				
			});
		}
	}
	
	private void sendSale(){
		new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				MPOSUtil.doSendSale(sStaffId, new ProgressListener(){

					@Override
					public void onPre() {
					}

					@Override
					public void onPost() {
					}

					@Override
					public void onError(String msg) {
					}
					
				});
			}
			
		}, 1000);
	}
	
	private void print(){
		new Handler().post(new Runnable(){

			@Override
			public void run() {
				PrintReceipt printReceipt = new PrintReceipt();
				printReceipt.printReceipt(sTransactionId, sComputerId);
			}
			
		});
	}
	
	public void confirm() {
		if(mTotalPaid >=mTotalSalePrice){
			if(mTransaction.successTransaction(sTransactionId, 
					sComputerId, sStaffId)){
				mChange = mTotalPaid - mTotalSalePrice;
				if(mChange > 0){
					LayoutInflater inflater = (LayoutInflater) 
							getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					TextView tvChange = (TextView) inflater.inflate(R.layout.tv_large, null);
					tvChange.setText(MPOSApplication.getGlobalProperty().currencyFormat(mChange));
					
					new AlertDialog.Builder(getActivity())
					.setTitle(R.string.change)
					.setCancelable(false)
					.setView(tvChange)
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							print();
							sendSale();
							getDialog().dismiss();
						}
					})
					.show();
				}else{
					print();
					sendSale();
					getDialog().dismiss();
				}
				
			}else{
				
			}
		}else{
			new AlertDialog.Builder(getActivity())
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.payment)
			.setMessage(R.string.enter_enough_money)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			})
			.show();
			
		}
	}

	public void cancel() {
		mPayment.deleteAllPaymentDetail(sTransactionId, 
				sComputerId);
		getDialog().dismiss();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnPay0:
			mStrTotalPay.append("0");
			displayEnterPrice();
			break;
		case R.id.btnPay1:
			mStrTotalPay.append("1");
			displayEnterPrice();
			break;
		case R.id.btnPay2:
			mStrTotalPay.append("2");
			displayEnterPrice();
			break;
		case R.id.btnPay3:
			mStrTotalPay.append("3");
			displayEnterPrice();
			break;
		case R.id.btnPay4:
			mStrTotalPay.append("4");
			displayEnterPrice();
			break;
		case R.id.btnPay5:
			mStrTotalPay.append("5");
			displayEnterPrice();
			break;
		case R.id.btnPay6:
			mStrTotalPay.append("6");
			displayEnterPrice();
			break;
		case R.id.btnPay7:
			mStrTotalPay.append("7");
			displayEnterPrice();
			break;
		case R.id.btnPay8:
			mStrTotalPay.append("8");
			displayEnterPrice();
			break;
		case R.id.btnPay9:
			mStrTotalPay.append("9");
			displayEnterPrice();
			break;
		case R.id.btnPay20:
			mStrTotalPay = new StringBuilder();
			mStrTotalPay.append("20");
			calculateInputPrice();
			addPayment();
			break;
		case R.id.btnPay50:
			mStrTotalPay = new StringBuilder();
			mStrTotalPay.append("50");
			calculateInputPrice();
			addPayment();
			break;
		case R.id.btnPay100:
			mStrTotalPay = new StringBuilder();
			mStrTotalPay.append("100");
			calculateInputPrice();
			addPayment();
			break;
		case R.id.btnPay500:
			mStrTotalPay = new StringBuilder();
			mStrTotalPay.append("500");
			calculateInputPrice();
			addPayment();
			break;
		case R.id.btnPay1000:
			mStrTotalPay = new StringBuilder();
			mStrTotalPay.append("1000");
			calculateInputPrice();
			addPayment();
			break;
		case R.id.btnPayC:
			mStrTotalPay = new StringBuilder();
			displayEnterPrice();
			break;
		case R.id.btnPayDel:
			try {
				mStrTotalPay.deleteCharAt(mStrTotalPay.length() - 1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			displayEnterPrice();
			break;
		case R.id.btnPayDot:
			mStrTotalPay.append(".");
			displayEnterPrice();
			break;
		case R.id.btnPayEnter:
			if(!mStrTotalPay.toString().isEmpty()){
				addPayment();
			}
			break;
		case R.id.btnCash:
			mBtnCash.setPressed(true);
			break;
		case R.id.btnCredit:
			creditPay();
			break;
		}
	}
}
