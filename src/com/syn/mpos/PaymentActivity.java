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
import com.syn.mpos.database.Setting;
import com.syn.mpos.database.Shop;
import com.syn.mpos.inventory.MPOSSaleStock;
import com.syn.mpos.transaction.MPOSPayment;
import com.syn.mpos.transaction.MPOSTransaction;
import com.syn.pos.OrderTransaction;
import com.syn.pos.Payment;
import com.syn.pos.ShopData.ShopProperty;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
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

	private MPOSTransaction mTrans;
	private MPOSPayment mPayment;
	private MPOSSaleStock mSaleStock;
	private List<Payment.PaymentDetail> mPayLst;
	private PaymentAdapter mPaymentAdapter;
	private Formatter mFormat;
	private Setting mSetting;
	private int mShopId;
	private int mTransactionId;
	private int mComputerId;
	private int mStaffId;
	private Print mPrinter;
	
	private StringBuilder mStrTotalPay;
	private float mTotalSalePrice;
	private float mTotalPay;
	private float mTotalPaid;
	private float mPaymentLeft;
	
	private ListView mLvPayment;
	private EditText mTxtTotalPay;
	private EditText mTxtTotalPaid;
	private EditText mTxtTobePaid;
	private EditText mTxtTotalPrice;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment);

		mLvPayment = (ListView) findViewById(R.id.listView1);
		mTxtTotalPay = (EditText) findViewById(R.id.editTextTotalPay);
		mTxtTotalPaid = (EditText) findViewById(R.id.editTextTotalPaid);
		mTxtTobePaid = (EditText) findViewById(R.id.editTextTobePaid);
		mTxtTotalPrice = (EditText) findViewById(R.id.txtTotalPrice);
		
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
		init();
		super.onResume();
	}

	private void init(){
		mFormat = new Formatter(PaymentActivity.this);
		mTrans = new MPOSTransaction(PaymentActivity.this);
		mPayment = new MPOSPayment(PaymentActivity.this);
		mSaleStock = new MPOSSaleStock(PaymentActivity.this);
		mPaymentAdapter = new PaymentAdapter();
		mPayLst = new ArrayList<Payment.PaymentDetail>();
		mLvPayment.setAdapter(mPaymentAdapter);
		mStrTotalPay = new StringBuilder();
		mSetting = new Setting(this);
		
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
			Button imgDel = (Button) rowView.findViewById(R.id.button1);
			
			String payTypeName = payment.getPayTypeID() == PAY_TYPE_CASH ? "Cash" : "Credit";
			if(payment.getPayTypeName() != null){
				payTypeName = payment.getPayTypeName();
			}
			
			tvPayType.setText(payTypeName);
			tvPayDetail.setText(payment.getRemark());
			tvPayAmount.setText(mFormat.currencyFormat(payment.getPayAmount()));
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
		
		mPaymentLeft = mTotalSalePrice - mTotalPaid; 

		mTxtTotalPaid.setText(mFormat.currencyFormat(mTotalPaid));
		if(mPaymentLeft < 0)
			mPaymentLeft = 0.0f;
		
		mTxtTobePaid.setText(mFormat.currencyFormat(mPaymentLeft));
	}
	
	private void deletePayment(int paymentId){
		mPayment.deletePaymentDetail(paymentId);
		loadPayDetail();
	}
	
	private void addPayment(){
		if(mTotalPay > 0 && mPaymentLeft > 0){
				mPayment.addPaymentDetail(mTransactionId, mComputerId, PAY_TYPE_CASH, mTotalPay, "",
						0, 0, 0, 0);
			loadPayDetail();
		}
		mStrTotalPay = new StringBuilder();
		mTxtTotalPay.setText("");
	}
	
	private void displayTotalPrice(){
		mTxtTotalPrice.setText(mFormat.currencyFormat(mTotalSalePrice));
		displayTotalPaid();
	}
	
	private void displayTotalPaid(){
		try {
			mTotalPay = Float.parseFloat(mStrTotalPay.toString());
		} catch (NumberFormatException e) {
			mTotalPay = 0.0f;
		}
		mTxtTotalPay.setText(mFormat.currencyFormat(mTotalPay));
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
		String printerIp = mSetting.getPrinter().getPrinterIp();
		mPrinter = new Print(PaymentActivity.this);
		mPrinter.setStatusChangeEventCallback(this);
		mPrinter.setBatteryStatusChangeEventCallback(this);
		
		try {
			mPrinter.openPrinter(Print.DEVTYPE_TCP, printerIp, 0, 1000);	
			Builder builder = new Builder("TM-T88V", Builder.MODEL_ANK, PaymentActivity.this);
			builder.addTextLang(Builder.LANG_TH);
			builder.addTextFont(Builder.FONT_B);
			builder.addTextAlign(Builder.ALIGN_LEFT);
			builder.addTextLineSpace(30);
			builder.addTextSize(1, 1);
			builder.addTextStyle(Builder.FALSE, Builder.FALSE, Builder.FALSE, Builder.COLOR_1);

	    	Shop s = new Shop(PaymentActivity.this);
	    	ShopProperty shopProp = s.getShopProperty();

			//builder.addTextPosition(100);
			if(!shopProp.getCompanyName().isEmpty()) 
				builder.addText(shopProp.getCompanyName() + "\n");
			if(!shopProp.getShopName().isEmpty())
				builder.addText(shopProp.getShopName() + "\n");
			if(!shopProp.getCompanyAddress1().isEmpty())
				builder.addText(shopProp.getCompanyAddress1() + "\n");
			if(!shopProp.getCompanyAddress2().isEmpty())
				builder.addText(shopProp.getCompanyAddress2() + "\n");
			
			builder.addText("RECEIPT/TAX INVOICE(ABB) \n");
			builder.addText("TAX ID: " + shopProp.getCompanyTaxID() + "\n");
			builder.addText("Date: " + mFormat.dateFormat(new Date(), "d/MM/yy") + "\n");
			builder.addText("Receipt No: " + mTrans.getTransaction(mTransactionId, mComputerId).getReceiptNo() + "\n");
			
			builder.addTextPosition(0);
			builder.addText("______________________________________________________\n");

			int maxNameLength = 30;
	    	for(int i = 0; i < orderLst.size(); i++){
	    		OrderTransaction.OrderDetail order = 
	    				orderLst.get(i);

	    		String no = Integer.toString(i + 1);
	    		int noLength = no.length();
	    		builder.addText(no + createSpace(noLength, 3));
	    		int nameLength = order.getProductName().length();
	    		if(nameLength > maxNameLength){
	    			builder.addText(order.getProductName().substring(31, nameLength));
	    			nameLength = maxNameLength;
	    		}else{
	    			builder.addText(order.getProductName());
	    		}
	    		builder.addText(createSpace(nameLength, maxNameLength));
	    		builder.addText(mFormat.qtyFormat(order.getQty()));
	    		builder.addText("          ");
	    		builder.addText(mFormat.currencyFormat(order.getTotalSalePrice()));
	    		builder.addText("\n");
	    	}
	    	
	    	builder.addText("______________________________________________________\n");
	    	String total = getApplicationContext().getString(R.string.total);
	    	String payment = getApplicationContext().getString(R.string.payment);
	    	String change = getApplicationContext().getString(R.string.change);
	    	String discount = getApplicationContext().getString(R.string.discount);
	    	
	    	builder.addText(total + createSpace(total.length(), 44));
	    	builder.addText(mTxtTotalPrice.getText() + "\n");
	    	builder.addText(discount + createSpace(discount.length(), 44));
	    	builder.addText(mFormat.currencyFormat(summary.getPriceDiscount()) + "\n");
	    	builder.addText(payment + createSpace(payment.length(), 44));
	    	builder.addText(mFormat.currencyFormat(mTotalPaid) + "\n");
	    	builder.addText(change + createSpace(change.length(), 44));
	    	builder.addText(mFormat.currencyFormat(mTotalPaid - mTotalSalePrice) + "\n");
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
					new AlertDialog.Builder(PaymentActivity.this)
					.setTitle(R.string.change)
					.setCancelable(false)
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
		mPayment.deleteAllPaymentDetail(mTransactionId, mComputerId);
		finish();
	}
}
