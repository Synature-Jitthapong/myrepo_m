package com.syn.mpos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.epson.eposprint.BatteryStatusChangeEventListener;
import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;
import com.epson.eposprint.StatusChangeEventListener;
import com.syn.mpos.R;
import com.syn.mpos.database.GlobalProperty;
import com.syn.mpos.database.HeaderFooterReceipt;
import com.syn.mpos.database.Shop;
import com.syn.mpos.database.Util;
import com.syn.mpos.database.transaction.PaymentDetail;
import com.syn.mpos.database.transaction.Transaction;
import com.syn.pos.OrderTransaction;
import com.syn.pos.Payment;
import com.syn.pos.ShopData;
import com.syn.pos.ShopData.ShopProperty;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class PaymentActivity extends Activity  implements StatusChangeEventListener, 
	BatteryStatusChangeEventListener {
	//private final String TAG = "PaymentActivity";
	public static final int PAY_TYPE_CASH = 1;
	public static final int PAY_TYPE_CREDIT = 2;

	public static float sTotalSalePrice;
	
	private int mTransactionId;
	private int mComputerId;
	private int mStaffId;
	private GlobalProperty mGlobalProp;
	private Transaction mTransaction;
	private PaymentDetail mPayment;
	private List<Payment.PaymentDetail> mPayLst;
	private PaymentAdapter mPaymentAdapter;
	private Print mPrinter;
	
	private StringBuilder mStrTotalPay;
	private float mTotalPay;
	private float mTotalPaid;
	private float mPaymentLeft;
	private float mChange;
	
	private ListView mLvPayment;
	private EditText mTxtTotalPay;
	private EditText mTxtTotalPaid;
	private EditText mTxtTobePaid;
	private EditText mTxtTotalPrice;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment);

		mLvPayment = (ListView) findViewById(R.id.lvPayDetail);
		mTxtTotalPay = (EditText) findViewById(R.id.editTextTotalPay);
		mTxtTotalPaid = (EditText) findViewById(R.id.editTextTotalPaid);
		mTxtTobePaid = (EditText) findViewById(R.id.editTextTobePaid);
		mTxtTotalPrice = (EditText) findViewById(R.id.txtTotalPrice);
		
		Intent intent = getIntent();
		mTransactionId = intent.getIntExtra("transactionId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);
		mStaffId = intent.getIntExtra("staffId", 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_confirm, menu);
		menu.findItem(R.id.itemClose).setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.itemCancel:
			cancel();
			return true;
		case R.id.itemConfirm:
			confirm();
			return true;
		default:
		return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		init();
	}

	private void init(){
		mGlobalProp = new GlobalProperty(MPOSApplication.getWriteDatabase());
		mTransaction = new Transaction(MPOSApplication.getWriteDatabase());
		mPayment = new PaymentDetail(MPOSApplication.getWriteDatabase());
		mPaymentAdapter = new PaymentAdapter();
		mPayLst = new ArrayList<Payment.PaymentDetail>();
		mLvPayment.setAdapter(mPaymentAdapter);
		mStrTotalPay = new StringBuilder();
		
//		if(mTransaction.getTransaction(
//				mTransactionId, mComputerId).getTransactionStatusId() == 
//				Transaction.TRANS_STATUS_SUCCESS)
//		{
//			finish();
//		}else{
			summary();
			loadPayDetail();
//		}
	}
	
	private void summary(){
		float vatExclude = mTransaction.getTotalVatExclude(mTransactionId, mComputerId, false); 
		sTotalSalePrice = mTransaction.getTotalSalePrice(mTransactionId, mComputerId, false) + 
				vatExclude;
		displayTotalPrice();
	}
	
	private class PaymentAdapter extends BaseAdapter{
		
		private LayoutInflater inflater;
		
		public PaymentAdapter(){
			inflater = LayoutInflater.from(PaymentActivity.this);
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
			
			String payTypeName = payment.getPayTypeID() == PAY_TYPE_CASH ? "Cash" : "Credit";
			if(payment.getPayTypeName() != null){
				payTypeName = payment.getPayTypeName();
			}
			
			tvPayType.setText(payTypeName);
			tvPayDetail.setText(payment.getRemark());
			tvPayAmount.setText(mGlobalProp.currencyFormat(payment.getPayAmount()));
			imgDel.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					deletePayment(payment.getPaymentDetailID());
				}
				
			});
			
			return rowView;
		}
	}
	
	private void loadPayDetail(){
		mPayLst = mPayment.listPayment(mTransactionId, mComputerId);
		mPaymentAdapter.notifyDataSetChanged();
		
		mTotalPaid = mPayment.getTotalPaid(mTransactionId, mComputerId);
		
		mPaymentLeft = sTotalSalePrice - mTotalPaid; 

		mTxtTotalPaid.setText(mGlobalProp.currencyFormat(mTotalPaid));
		if(mPaymentLeft < 0)
			mPaymentLeft = 0.0f;
		
		mTxtTobePaid.setText(mGlobalProp.currencyFormat(mPaymentLeft));
	}
	
	private void deletePayment(int paymentId){
		mPayment.deletePaymentDetail(paymentId);
		loadPayDetail();
	}
	
	private void addPayment(){
		if(mTotalPay > 0 && mPaymentLeft > 0){
				mPayment.addPaymentDetail(mTransactionId, 
						mComputerId, PAY_TYPE_CASH, mTotalPay, "",
						0, 0, 0, 0);
			loadPayDetail();
		}
		mStrTotalPay = new StringBuilder();
		mTxtTotalPay.setText("");
	}
	
	private void displayTotalPrice(){
		mTxtTotalPrice.setText(mGlobalProp.currencyFormat(sTotalSalePrice));
		displayTotalPaid();
	}
	
	private void displayTotalPaid(){
		try {
			mTotalPay = Float.parseFloat(mStrTotalPay.toString());
		} catch (NumberFormatException e) {
			mTotalPay = 0.0f;
		}
		mTxtTotalPay.setText(mGlobalProp.currencyFormat(mTotalPay));
	}
	
	public void creditPayClicked(final View v){
		Intent intent = new Intent(PaymentActivity.this, CreditPayActivity.class);
		intent.putExtra("transactionId", mTransactionId);
		intent.putExtra("computerId", mComputerId);
		startActivity(intent);
	}

	/*
	 * pay key pad
	 */
	public void onBtnPriceClick(final View v){
		switch(v.getId()){
		case R.id.btnPay0:
			mStrTotalPay.append("0");
			displayTotalPaid();
			break;
		case R.id.btnPay1:
			mStrTotalPay.append("1");
			displayTotalPaid();
			break;
		case R.id.btnPay2:
			mStrTotalPay.append("2");
			displayTotalPaid();
			break;
		case R.id.btnPay3:
			mStrTotalPay.append("3");
			displayTotalPaid();
			break;
		case R.id.btnPay4:
			mStrTotalPay.append("4");
			displayTotalPaid();
			break;
		case R.id.btnPay5:
			mStrTotalPay.append("5");
			displayTotalPaid();
			break;
		case R.id.btnPay6:
			mStrTotalPay.append("6");
			displayTotalPaid();
			break;
		case R.id.btnPay7:
			mStrTotalPay.append("7");
			displayTotalPaid();
			break;
		case R.id.btnPay8:
			mStrTotalPay.append("8");
			displayTotalPaid();
			break;
		case R.id.btnPay9:
			mStrTotalPay.append("9");
			displayTotalPaid();
			break;
		case R.id.btnPay20:
			mStrTotalPay = new StringBuilder();
			mStrTotalPay.append("20");
			displayTotalPaid();
			addPayment();
			break;
		case R.id.btnPay50:
			mStrTotalPay = new StringBuilder();
			mStrTotalPay.append("50");
			displayTotalPaid();
			addPayment();
			break;
		case R.id.btnPay100:
			mStrTotalPay = new StringBuilder();
			mStrTotalPay.append("100");
			displayTotalPaid();
			addPayment();
			break;
		case R.id.btnPay500:
			mStrTotalPay = new StringBuilder();
			mStrTotalPay.append("500");
			displayTotalPaid();
			addPayment();
			break;
		case R.id.btnPay1000:
			mStrTotalPay = new StringBuilder();
			mStrTotalPay.append("1000");
			displayTotalPaid();
			addPayment();
			break;
		case R.id.btnPayC:
			mStrTotalPay = new StringBuilder();
			displayTotalPaid();
			break;
		case R.id.btnPayDel:
			try {
				mStrTotalPay.deleteCharAt(mStrTotalPay.length() - 1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			displayTotalPaid();
			break;
		case R.id.btnPayDot:
			mStrTotalPay.append(".");
			displayTotalPaid();
			break;
		case R.id.btnPayEnter:
			if(!mStrTotalPay.toString().isEmpty()){
				addPayment();
				mStrTotalPay = new StringBuilder();
			}
			break;
		}
	}
	
	private String createLine(String sign){
		StringBuilder line = new StringBuilder();
		for(int i = 0; i <= 45; i++){
			line.append(sign);
		}
		return line.toString();
	}
	
	private String createLineSpace(int usedSpace){
		int maxSpace = 45;
		StringBuilder space = new StringBuilder();
		if(usedSpace > maxSpace){
			usedSpace = usedSpace - 2;
		}
		for(int i = usedSpace; i <= maxSpace; i++){
			space.append(" ");
		}
		return space.toString();
	}
	
	private void print(){
		String printerIp = MPOSApplication.getPrinterIp();
		String printerName = MPOSApplication.getPrinterName();
		mPrinter = new Print(PaymentActivity.this);
		mPrinter.setStatusChangeEventCallback(this);
		mPrinter.setBatteryStatusChangeEventCallback(this);
		
		try {
			mPrinter.openPrinter(Print.DEVTYPE_TCP, printerIp, 0, 1000);	
			Builder builder = new Builder(printerName, Builder.MODEL_ANK, PaymentActivity.this);
			
			builder.addTextLang(Builder.LANG_TH);
			builder.addTextFont(Builder.FONT_B); //b
			builder.addTextAlign(Builder.ALIGN_CENTER);
			builder.addTextSize(1, 1);
			builder.addTextStyle(Builder.FALSE, Builder.FALSE, Builder.FALSE, Builder.COLOR_1);

			// add header
			HeaderFooterReceipt headerFooter = new HeaderFooterReceipt(MPOSApplication.getWriteDatabase());
			for(ShopData.HeaderFooterReceipt hf : 
				headerFooter.listHeaderFooter(HeaderFooterReceipt.HEADER_LINE_TYPE)){
				builder.addText(hf.getTextInLine());
				builder.addText("\n");
			}
			OrderTransaction orderTrans = mTransaction.getTransaction(mTransactionId, mComputerId);
			builder.addText(this.getString(R.string.date) + " ");
			builder.addText(mGlobalProp.dateFormat(Util.getDate().getTime()) + "\n");
			builder.addText(this.getString(R.string.receipt_no) + " ");
			builder.addText(orderTrans.getReceiptNo());
			builder.addText("\n" + createLine("=") + "\n");
			
			List<OrderTransaction.OrderDetail> orderLst = 
					mTransaction.listAllOrder(mTransactionId, mComputerId);
			builder.addTextAlign(Builder.ALIGN_CENTER);
	    	for(int i = 0; i < orderLst.size(); i++){
	    		OrderTransaction.OrderDetail order = 
	    				orderLst.get(i);
	    		
	    		String productName = order.getProductName();
	    		String productQty = mGlobalProp.qtyFormat(order.getQty());
	    		String productPrice = mGlobalProp.currencyFormat(order.getTotalSalePrice());
	    		
	    		builder.addText(productQty + " ");
	    		builder.addText(productName);
	    		builder.addText(createLineSpace(productQty.length() + 
	    				productName.length() + productPrice.length()));
	    		builder.addText(productPrice);
	    		builder.addText("\n");
	    	}
	    	builder.addText(createLine("-") + "\n");
	    	
	    	String totalText = getApplicationContext().getString(R.string.total);
	    	String paymentText = getApplicationContext().getString(R.string.payment);
	    	String changeText = getApplicationContext().getString(R.string.change);
	    	String discountText = getApplicationContext().getString(R.string.discount);
	    	String totalPrice = mTxtTotalPrice.getText().toString();
	    	String totalDiscount = mGlobalProp.currencyFormat(mTransaction.getPriceDiscount(mTransactionId, 
	    			mComputerId, false));
	    	String totalPayment = mGlobalProp.currencyFormat(mTotalPaid);
	    	String totalChange = mGlobalProp.currencyFormat(mChange);
	    	
	    	// total price
	    	builder.addText(totalText);
	    	builder.addText(createLineSpace(totalText.length() + totalPrice.length()));
	    	builder.addText(totalPrice + "\n");
	    	
	    	// total discount
	    	builder.addText(discountText);
	    	builder.addText(createLineSpace(discountText.length() + totalDiscount.length()));
	    	builder.addText(totalDiscount + "\n");
	    	
	    	// total payment
	    	builder.addText(paymentText);
	    	builder.addText(createLineSpace(paymentText.length() + totalPayment.length()));
	    	builder.addText(totalPayment + "\n");
	    	
	    	// change
	    	builder.addText(changeText);
	    	builder.addText(createLineSpace(changeText.length() + totalChange.length()));
	    	builder.addText(totalChange);
	    	builder.addText("\n" + createLine("-") + "\n");
	    	
	    	// add footer
	    	for(ShopData.HeaderFooterReceipt hf : 
				headerFooter.listHeaderFooter(HeaderFooterReceipt.FOOTER_LINE_TYPE)){
				builder.addText(hf.getTextInLine());
				builder.addText("\n");
			}
	    	
			builder.addFeedUnit(30);
			builder.addCut(Builder.CUT_FEED);

			// send builder data
			int[] status = new int[1];
			int[] battery = new int[1];
			try {
				mPrinter.sendData(builder, 10000, status, battery);
			} catch (EposException e) {
//				Util.alert(context, android.R.drawable.ic_dialog_alert,
//						R.string.title_activity_payment, e.getErrorStatus());
			}

			if (builder != null) {
				builder.clearCommandBuffer();
			}
		} catch (EposException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			mPrinter.closePrinter();
			mPrinter = null;
		} catch (EposException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onBatteryStatusChangeEvent(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChangeEvent(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
	public void confirm() {
		if(mTotalPaid >=sTotalSalePrice){
			if(mTransaction.successTransaction(mTransactionId, 
					mComputerId, mStaffId)){
				
				// send real time sale
				MPOSUtil.sendRealTimeSale(mStaffId, mTransactionId, mComputerId);
				
				mChange = mTotalPaid - sTotalSalePrice;
				print();
				
				if(mChange > 0){
					LayoutInflater inflater = (LayoutInflater) 
							PaymentActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					TextView tvChange = (TextView) inflater.inflate(R.layout.tv_large, null);
					tvChange.setText(mGlobalProp.currencyFormat(mChange));
					
					new AlertDialog.Builder(PaymentActivity.this)
					.setTitle(R.string.change)
					.setCancelable(false)
					.setView(tvChange)
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					})
					.show();
				}else{
					finish();
				}
				
			}else{
				
			}
		}else{
			new AlertDialog.Builder(PaymentActivity.this)
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
		mPayment.deleteAllPaymentDetail(mTransactionId, 
				mComputerId);
		finish();
	}
}
