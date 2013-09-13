package com.syn.mpos;

import java.util.List;
import com.epson.eposprint.BatteryStatusChangeEventListener;
import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;
import com.epson.eposprint.StatusChangeEventListener;
import com.syn.mpos.R;
import com.syn.mpos.db.MPOSOrder;
import com.syn.mpos.db.MPOSPayment;
import com.syn.mpos.db.MPOSTransaction;
import com.syn.mpos.db.Shop;
import com.syn.mpos.model.OrderTransaction;
import com.syn.mpos.model.ShopData.ShopProperty;
import com.syn.pos.Order;
import com.syn.pos.Payment;
import com.syn.pos.Transaction;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

public class PaymentActivity extends Activity  implements OnConfirmClickListener,
	StatusChangeEventListener, BatteryStatusChangeEventListener {
	private final String TAG = "PaymentActivity";
	public static final int PAY_TYPE_CASH = 1;
	public static final int PAY_TYPE_CREDIT = 2;
	private Context context;
	private Transaction mTrans;
	private Payment mPayment;
	private Order mOrder;
	private Formatter format;
	private int transactionId;
	private int computerId;
	private int staffId;
	private Print printer;
	
	private StringBuilder strTotalPay;
	private float totalSalePrice;
	private float totalPay;
	private float totalPaid;
	
	private TableLayout tableLayoutPaydetail;
	private TextView tvTotalPayment;
	private EditText txtTotalPay;
	private EditText txtTotalPaid;
	private EditText txtTobePaid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment);

		ActionBar actionBar = getActionBar();
		actionBar.setCustomView(R.layout.confirm_button);
	    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
	            | ActionBar.DISPLAY_SHOW_HOME);

		tableLayoutPaydetail = (TableLayout) findViewById(R.id.tableLayoutPaydetail);
		tvTotalPayment = (TextView) findViewById(R.id.textViewTotalPayment);
		txtTotalPay = (EditText) findViewById(R.id.editTextTotalPay);
		txtTotalPaid = (EditText) findViewById(R.id.editTextTotalPaid);
		txtTobePaid = (EditText) findViewById(R.id.editTextTobePaid);
		
		Intent intent = getIntent();
		transactionId = intent.getIntExtra("transactionId", 0);
		computerId = intent.getIntExtra("computerId", 0);
		staffId = intent.getIntExtra("staffId", 0);
		
		if(transactionId == 0 || computerId == 0 || staffId == 0){
			finish();
		}
	}

	@Override
	protected void onResume() {
		init();
		super.onResume();
	}

	private void init(){
		context = PaymentActivity.this;
		format = new Formatter(context);
		mTrans = new MPOSTransaction(context);
		mPayment = new MPOSPayment(context);
		mOrder = new MPOSOrder(context);
		
		summary();
		loadPayDetail();
	}
	
	private void summary(){
		OrderTransaction.OrderDetail orderDetail = 
				mOrder.getSummary(transactionId, computerId);
		
		float vat = orderDetail.getVat();
		totalSalePrice = orderDetail.getTotalSalePrice() + vat;
		
		displayTotalPrice();
	}
	
	private void loadPayDetail(){
		List<com.syn.mpos.model.Payment.PaymentDetail> payLst = 
				mPayment.listPayment(transactionId, computerId);
		totalPaid = mPayment.getTotalPaid(transactionId, computerId);
		float tobePaid = totalSalePrice - totalPaid; 
		
		LayoutInflater inflater = LayoutInflater.from(context);
		tableLayoutPaydetail.removeAllViews();
		for(final com.syn.mpos.model.Payment.PaymentDetail payment : payLst){
			View v = inflater.inflate(R.layout.payment_detail_template, null);
			TextView tvPayType = (TextView) v.findViewById(R.id.textViewPayType);
			TextView tvPayDetail = (TextView) v.findViewById(R.id.textViewPayDetail);
			TextView tvPayAmount = (TextView) v.findViewById(R.id.textViewPayAmount);
			
			String payTypeName = payment.getPayTypeID() == PAY_TYPE_CASH ? "Cash" : "Credit";
			if(payment.getPayTypeName() != null){
				payTypeName = payment.getPayTypeName();
			}
			
			tvPayType.setText(payTypeName);
			tvPayDetail.setText(payment.getRemark());
			tvPayAmount.setText(format.currencyFormat(payment.getPayAmount()));
			
			v.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					deletePayment(payment.getPaymentDetailID());
				}
				
			});
			tableLayoutPaydetail.addView(v);
		}
		
		txtTotalPaid.setText(format.currencyFormat(totalPaid));
		if(tobePaid < 0)
			tobePaid = 0.0f;
		
		txtTobePaid.setText(format.currencyFormat(tobePaid));
	}
	
	private void deletePayment(int paymentId){
		mPayment.deletePaymentDetail(paymentId);
		loadPayDetail();
	}
	
	private void addPayment(){
		if(totalPay > 0){
				mPayment.addPaymentDetail(transactionId, computerId, PAY_TYPE_CASH, totalPay, "",
						0, 0, 0, 0);
			loadPayDetail();
		}
	}
	
	private void displayTotalPrice(){
		tvTotalPayment.setText(format.currencyFormat(totalSalePrice));

		strTotalPay = new StringBuilder();
		displayTotalPaid();
	}
	
	private void displayTotalPaid(){
		try {
			totalPay = Float.parseFloat(strTotalPay.toString());
		} catch (NumberFormatException e) {
			totalPay = 0.0f;
		}
		txtTotalPay.setText(format.currencyFormat(totalPay));
	}
	
	public void creditPayClicked(final View v){
		Intent intent = new Intent(PaymentActivity.this, CreditPayActivity.class);
		intent.putExtra("transactionId", transactionId);
		intent.putExtra("computerId", computerId);
		startActivity(intent);
	}

	/*
	 * pay key pad
	 */
	public void onBtnPriceClick(final View v){
		switch(v.getId()){
		case R.id.btnPay0:
			strTotalPay.append("0");
			break;
		case R.id.btnPay1:
			strTotalPay.append("1");
			break;
		case R.id.btnPay2:
			strTotalPay.append("2");
			break;
		case R.id.btnPay3:
			strTotalPay.append("3");
			break;
		case R.id.btnPay4:
			strTotalPay.append("4");
			break;
		case R.id.btnPay5:
			strTotalPay.append("5");
			break;
		case R.id.btnPay6:
			strTotalPay.append("6");
			break;
		case R.id.btnPay7:
			strTotalPay.append("7");
			break;
		case R.id.btnPay8:
			strTotalPay.append("8");
			break;
		case R.id.btnPay9:
			strTotalPay.append("9");
			break;
		case R.id.btnPay20:
			strTotalPay.append("20");
			displayTotalPaid();
			addPayment();
			strTotalPay = new StringBuilder();
			break;
		case R.id.btnPay50:
			strTotalPay.append("50");
			displayTotalPaid();
			addPayment();
			strTotalPay = new StringBuilder();
			break;
		case R.id.btnPay100:
			strTotalPay.append("100");
			displayTotalPaid();
			addPayment();
			strTotalPay = new StringBuilder();
			break;
		case R.id.btnPay500:
			strTotalPay.append("500");
			displayTotalPaid();
			addPayment();
			strTotalPay = new StringBuilder();
			break;
		case R.id.btnPay1000:
			strTotalPay.append("1000");
			displayTotalPaid();
			addPayment();
			strTotalPay = new StringBuilder();
			break;
		case R.id.btnPayC:
			strTotalPay = new StringBuilder();
			break;
		case R.id.btnPayDel:
			try {
				strTotalPay.deleteCharAt(strTotalPay.length() - 1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case R.id.btnPayDot:
			strTotalPay.append(".");
			break;
		case R.id.btnPayEnter:
			addPayment();
			strTotalPay = new StringBuilder();
			break;
		}
		displayTotalPaid();
	}

	private String createSpace(int minLength, int maxLength){
		StringBuilder space = new StringBuilder();
		for(int sp = minLength; sp < maxLength; sp++){
			space.append(" ");
		}
		return space.toString();
	}
	
	private void print(){
		printer = new Print(context);
		printer.setStatusChangeEventCallback(this);
		printer.setBatteryStatusChangeEventCallback(this);
		
		try {
			printer.openPrinter(Print.DEVTYPE_TCP, "1.1.0.163", 0, 1000);	
			Builder builder = new Builder("TM-T88V", Builder.MODEL_ANK, context);
			builder.addTextLang(Builder.LANG_TH);
			builder.addTextFont(Builder.FONT_B);
			builder.addTextAlign(Builder.ALIGN_LEFT);
			builder.addTextLineSpace(30);
			builder.addTextSize(1, 1);
			builder.addTextStyle(Builder.FALSE, Builder.FALSE, Builder.FALSE, Builder.COLOR_1);
			
			OrderTransaction trans = mTrans.getTransaction(transactionId, computerId);
			OrderTransaction.OrderDetail summary = 
					mOrder.getSummary(transactionId, computerId);
	    	List<OrderTransaction.OrderDetail> orderLst = 
	    			mOrder.listAllOrders(transactionId, computerId);
	    	
	    	Shop s = new Shop(context);
	    	ShopProperty shopProp = s.getShopProperty();

			builder.addTextPosition(100);
			builder.addText("ใบเสร็จรับเงิน/ใบกำกับภาษีอย่างย่อ\n");
			
			if(!shopProp.getCompanyName().isEmpty()) 
				builder.addText("\t" + shopProp.getCompanyName() + "\t\n");
			if(!shopProp.getShopName().isEmpty())
				builder.addText("\t" + shopProp.getShopName() + "\t\n");
			if(!shopProp.getCompanyAddress1().isEmpty())
				builder.addText("\t" + shopProp.getCompanyAddress1() + "\n");
			if(!shopProp.getCompanyAddress2().isEmpty())
				builder.addText("\t" + shopProp.getCompanyAddress2() + "\n");
			if(!shopProp.getCompanyTaxID().isEmpty())
				builder.addText("\t" + shopProp.getCompanyTaxID() + "\n");
			
			builder.addTextPosition(0);
			builder.addText("______________________________________________________\n");

	    	for(int i = 0; i < orderLst.size(); i++){
	    		OrderTransaction.OrderDetail order = 
	    				orderLst.get(i);

	    		builder.addText(Integer.toString(i + 1) + createSpace(1, 3));
	    		builder.addText(order.getProductName());
	    		
	    		int len = order.getProductName().length();
	    		builder.addText(createSpace(len, 30));
	    		builder.addText(format.qtyFormat(order.getQty()));
	    		builder.addText("          ");
	    		builder.addText(format.currencyFormat(order.getTotalSalePrice()));
	    		builder.addText("\n");
	    	}
	    	
	    	builder.addText("______________________________________________________\n");
	    	String total = "Total";
	    	String net = "Net";
	    	String discount = "Discount";
	    	String vatable = "Vatable";
	    	String vat = "Vat";
	    	
	    	builder.addText(total + createSpace(total.length(), 43));
	    	builder.addText(format.currencyFormat(summary.getTotalRetailPrice()) + "\n");
	    	builder.addText(discount + createSpace(discount.length(), 43));
	    	builder.addText(format.currencyFormat(summary.getPriceDiscount()) + "\n");
	    	builder.addText(net + createSpace(net.length(), 43));
	    	builder.addText(format.currencyFormat(summary.getTotalSalePrice()) + "\n");
	    	
	    	builder.addText("______________________________________________________\n");
	    	builder.addText(vatable + createSpace(vatable.length(), 43));
	    	builder.addText(format.currencyFormat(trans.getTransactionVatable()) + "\n");
	    	builder.addText(vat + createSpace(vat.length(), 43));
	    	builder.addText(shopProp.getCompanyVat() + "\n");
			builder.addFeedUnit(30);
			builder.addCut(Builder.CUT_FEED);

			// send builder data
			int[] status = new int[1];
			int[] battery = new int[1];
			try {
				printer.sendData(builder, 10000, status, battery);
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
			printer.closePrinter();
			printer = null;
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

	@Override
	public void onOkClick(View v) {
		if(totalPaid >=totalSalePrice){
			mTrans.successTransaction(transactionId, computerId, staffId);
			
			print();
			
			if(totalPaid - totalSalePrice > 0){
				new AlertDialog.Builder(context)
				.setTitle(R.string.change)
				.setMessage(format.currencyFormat(totalPaid - totalSalePrice))
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
			new AlertDialog.Builder(context)
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

	@Override
	public void onCancelClick(View v) {
		mPayment.deleteAllPaymentDetail(transactionId, computerId);
		finish();
	}
}
