package com.syn.mpos;

import java.util.List;
import com.epson.eposprint.BatteryStatusChangeEventListener;
import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;
import com.epson.eposprint.StatusChangeEventListener;
import com.syn.mpos.R;
import com.syn.mpos.database.Shop;
import com.syn.mpos.inventory.MPOSSaleStock;
import com.syn.mpos.transaction.MPOSPayment;
import com.syn.mpos.transaction.MPOSTransaction;
import com.syn.pos.OrderTransaction;
import com.syn.pos.Payment;
import com.syn.pos.ShopData.ShopProperty;
import android.os.Bundle;
import android.app.ActionBar;
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
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

public class PaymentActivity extends Activity  implements OnConfirmClickListener,
	StatusChangeEventListener, BatteryStatusChangeEventListener {
	//private final String TAG = "PaymentActivity";
	public static final int PAY_TYPE_CASH = 1;
	public static final int PAY_TYPE_CREDIT = 2;
	private Context mContext;
	private MPOSTransaction mTrans;
	private MPOSPayment mPayment;
	private MPOSSaleStock mSaleStock;
	private Formatter mFormat;
	private int mShopId;
	private int mTransactionId;
	private int mComputerId;
	private int mStaffId;
	private Print mPrinter;
	
	private StringBuilder mStrTotalPay;
	private float mTotalSalePrice;
	private float mTotalPay;
	private float mTotalPaid;
	
	private TableLayout tableLayoutPaydetail;
	private TextView tvTotalPayment;
	private EditText txtTotalPay;
	private EditText txtTotalPaid;
	private EditText txtTobePaid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment);

		tableLayoutPaydetail = (TableLayout) findViewById(R.id.tableLayoutPaydetail);
		tvTotalPayment = (TextView) findViewById(R.id.textViewTotalPayment);
		txtTotalPay = (EditText) findViewById(R.id.editTextTotalPay);
		txtTotalPaid = (EditText) findViewById(R.id.editTextTotalPaid);
		txtTobePaid = (EditText) findViewById(R.id.editTextTobePaid);
		
		Intent intent = getIntent();
		
		mShopId = intent.getIntExtra("shopId", 0);
		mTransactionId = intent.getIntExtra("transactionId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);
		mStaffId = intent.getIntExtra("staffId", 0);
		
		if (mShopId == 0 || mTransactionId == 0 || mComputerId == 0
				|| mStaffId == 0) {
			finish();
		}
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
			onCancelClick(item.getActionView());
			return true;
		case R.id.itemConfirm:
			onConfirmClick(item.getActionView());
			return true;
		default:
		return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		init();
		super.onResume();
	}

	private void init(){
		mContext = PaymentActivity.this;
		mFormat = new Formatter(mContext);
		mTrans = new MPOSTransaction(mContext);
		mPayment = new MPOSPayment(mContext);
		mSaleStock = new MPOSSaleStock(mContext);
		
		summary();
		loadPayDetail();
	}
	
	private void summary(){
		OrderTransaction.OrderDetail orderDetail = 
				mTrans.getSummary(mTransactionId, mComputerId);
		
		float vat = orderDetail.getVat();
		mTotalSalePrice = orderDetail.getTotalSalePrice() + vat;
		
		displayTotalPrice();
	}
	
	private void loadPayDetail(){
		List<Payment.PaymentDetail> payLst = 
				mPayment.listPayment(mTransactionId, mComputerId);
		mTotalPaid = mPayment.getTotalPaid(mTransactionId, mComputerId);
		float tobePaid = mTotalSalePrice - mTotalPaid; 
		
		LayoutInflater inflater = LayoutInflater.from(mContext);
		tableLayoutPaydetail.removeAllViews();
		for(final Payment.PaymentDetail payment : payLst){
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
			tvPayAmount.setText(mFormat.currencyFormat(payment.getPayAmount()));
			
			v.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					deletePayment(payment.getPaymentDetailID());
				}
				
			});
			tableLayoutPaydetail.addView(v);
		}
		
		txtTotalPaid.setText(mFormat.currencyFormat(mTotalPaid));
		if(tobePaid < 0)
			tobePaid = 0.0f;
		
		txtTobePaid.setText(mFormat.currencyFormat(tobePaid));
	}
	
	private void deletePayment(int paymentId){
		mPayment.deletePaymentDetail(paymentId);
		loadPayDetail();
	}
	
	private void addPayment(){
		if(mTotalPay > 0){
				mPayment.addPaymentDetail(mTransactionId, mComputerId, PAY_TYPE_CASH, mTotalPay, "",
						0, 0, 0, 0);
			loadPayDetail();
		}
	}
	
	private void displayTotalPrice(){
		tvTotalPayment.setText(mFormat.currencyFormat(mTotalSalePrice));

		mStrTotalPay = new StringBuilder();
		displayTotalPaid();
	}
	
	private void displayTotalPaid(){
		try {
			mTotalPay = Float.parseFloat(mStrTotalPay.toString());
		} catch (NumberFormatException e) {
			mTotalPay = 0.0f;
		}
		txtTotalPay.setText(mFormat.currencyFormat(mTotalPay));
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
			break;
		case R.id.btnPay1:
			mStrTotalPay.append("1");
			break;
		case R.id.btnPay2:
			mStrTotalPay.append("2");
			break;
		case R.id.btnPay3:
			mStrTotalPay.append("3");
			break;
		case R.id.btnPay4:
			mStrTotalPay.append("4");
			break;
		case R.id.btnPay5:
			mStrTotalPay.append("5");
			break;
		case R.id.btnPay6:
			mStrTotalPay.append("6");
			break;
		case R.id.btnPay7:
			mStrTotalPay.append("7");
			break;
		case R.id.btnPay8:
			mStrTotalPay.append("8");
			break;
		case R.id.btnPay9:
			mStrTotalPay.append("9");
			break;
		case R.id.btnPay20:
			mStrTotalPay.append("20");
			displayTotalPaid();
			addPayment();
			mStrTotalPay = new StringBuilder();
			break;
		case R.id.btnPay50:
			mStrTotalPay.append("50");
			displayTotalPaid();
			addPayment();
			mStrTotalPay = new StringBuilder();
			break;
		case R.id.btnPay100:
			mStrTotalPay.append("100");
			displayTotalPaid();
			addPayment();
			mStrTotalPay = new StringBuilder();
			break;
		case R.id.btnPay500:
			mStrTotalPay.append("500");
			displayTotalPaid();
			addPayment();
			mStrTotalPay = new StringBuilder();
			break;
		case R.id.btnPay1000:
			mStrTotalPay.append("1000");
			displayTotalPaid();
			addPayment();
			mStrTotalPay = new StringBuilder();
			break;
		case R.id.btnPayC:
			mStrTotalPay = new StringBuilder();
			break;
		case R.id.btnPayDel:
			try {
				mStrTotalPay.deleteCharAt(mStrTotalPay.length() - 1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case R.id.btnPayDot:
			mStrTotalPay.append(".");
			break;
		case R.id.btnPayEnter:
			addPayment();
			mStrTotalPay = new StringBuilder();
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
	
	private void print(OrderTransaction trans, 
			OrderTransaction.OrderDetail summary, 
			List<OrderTransaction.OrderDetail> orderLst){
		mPrinter = new Print(mContext);
		mPrinter.setStatusChangeEventCallback(this);
		mPrinter.setBatteryStatusChangeEventCallback(this);
		
		try {
			mPrinter.openPrinter(Print.DEVTYPE_TCP, "1.1.0.163", 0, 1000);	
			Builder builder = new Builder("TM-T88V", Builder.MODEL_ANK, mContext);
			builder.addTextLang(Builder.LANG_TH);
			builder.addTextFont(Builder.FONT_B);
			builder.addTextAlign(Builder.ALIGN_LEFT);
			builder.addTextLineSpace(30);
			builder.addTextSize(1, 1);
			builder.addTextStyle(Builder.FALSE, Builder.FALSE, Builder.FALSE, Builder.COLOR_1);

	    	Shop s = new Shop(mContext);
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
	    		builder.addText(mFormat.qtyFormat(order.getQty()));
	    		builder.addText("          ");
	    		builder.addText(mFormat.currencyFormat(order.getTotalSalePrice()));
	    		builder.addText("\n");
	    	}
	    	
	    	builder.addText("______________________________________________________\n");
	    	String total = "Total";
	    	String net = "Net";
	    	String discount = "Discount";
	    	String vatable = "Vatable";
	    	String vat = "Vat";
	    	
	    	builder.addText(total + createSpace(total.length(), 43));
	    	builder.addText(mFormat.currencyFormat(summary.getTotalRetailPrice()) + "\n");
	    	builder.addText(discount + createSpace(discount.length(), 43));
	    	builder.addText(mFormat.currencyFormat(summary.getPriceDiscount()) + "\n");
	    	builder.addText(net + createSpace(net.length(), 43));
	    	builder.addText(mFormat.currencyFormat(summary.getTotalSalePrice()) + "\n");
	    	
	    	builder.addText("______________________________________________________\n");
	    	builder.addText(vatable + createSpace(vatable.length(), 43));
	    	builder.addText(mFormat.currencyFormat(trans.getTransactionVatable()) + "\n");
	    	builder.addText(vat + createSpace(vat.length(), 43));
	    	builder.addText(shopProp.getCompanyVat() + "\n");
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

	@Override 
	public void onSaveClick(View v){
		
	}
	
	@Override
	public void onConfirmClick(View v) {
		if(mTotalPaid >=mTotalSalePrice){
			if(mTrans.successTransaction(mTransactionId, mComputerId, mStaffId)){
				
				OrderTransaction trans = mTrans.getTransaction(mTransactionId, mComputerId);
				OrderTransaction.OrderDetail summary = 
						mTrans.getSummary(mTransactionId, mComputerId);
		    	List<OrderTransaction.OrderDetail> orderLst = 
		    			mTrans.listAllOrders(mTransactionId, mComputerId);
		    	
				print(trans, summary, orderLst);
				mSaleStock.createSaleDocument(mShopId, mStaffId, orderLst);
				
				if(mTotalPaid - mTotalSalePrice > 0){
					new AlertDialog.Builder(mContext)
					.setTitle(R.string.change)
					.setMessage(mFormat.currencyFormat(mTotalPaid - mTotalSalePrice))
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
			new AlertDialog.Builder(mContext)
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
		mPayment.deleteAllPaymentDetail(mTransactionId, mComputerId);
		finish();
	}
}
