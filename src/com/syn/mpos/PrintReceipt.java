package com.syn.mpos;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.epson.eposprint.BatteryStatusChangeEventListener;
import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;
import com.epson.eposprint.StatusChangeEventListener;
import com.j1tth4.mobile.util.Logger;
import com.syn.mpos.database.GlobalProperty;
import com.syn.mpos.database.HeaderFooterReceipt;
import com.syn.mpos.database.PaymentDetail;
import com.syn.mpos.database.PrintReceiptLog;
import com.syn.mpos.database.Products;
import com.syn.mpos.database.Shop;
import com.syn.mpos.database.Staff;
import com.syn.mpos.database.Transaction;
import com.syn.mpos.database.Util;
import com.syn.pos.OrderTransaction;
import com.syn.pos.Payment;
import com.syn.pos.ShopData;

public class PrintReceipt extends AsyncTask<Void, Void, Void> implements BatteryStatusChangeEventListener, StatusChangeEventListener{
	
	public static final String TAG = "PrintReceipt";
	private PrintStatusListener mPrintListener;
	private Context mContext;
	private Print mPrinter;
	private Transaction mTrans;
	private Staff mStaff;
	private Shop mShop;
	private PaymentDetail mPayment;
	private int mStaffId;
	
	public PrintReceipt(Context c, int staffId, PrintStatusListener listener){
		mContext = c;
		mPrintListener = listener;
		mStaffId = staffId;
		mShop = new Shop(c);
		mTrans = new Transaction(c);
		mPayment = new PaymentDetail(c);
		mStaff = new Staff(c);
		
		mTrans.open();
		mShop.open();
		mPayment.open();
		mStaff.open();
		
		mPrinter = new Print(MPOSApplication.getContext());
		mPrinter.setStatusChangeEventCallback(this);
		mPrinter.setBatteryStatusChangeEventCallback(this);
	}
	
	private static String createLine(String sign){
		StringBuilder line = new StringBuilder();
		for(int i = 0; i <= 45; i++){
			line.append(sign);
		}
		return line.toString();
	}
	
	private static String createLineSpace(int usedSpace){
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
	
	protected void printReceipt(int transactionId, int computerId){
		double vatRate = mShop.getCompanyVatRate();
		double transactionVat = mTrans.getTransactionVat(transactionId, computerId);
		double transactionVatExclude = mTrans.getTransactionVatExclude(transactionId, computerId);
		double transactionVatable = mTrans.getTransactionVatable(transactionId, computerId);
		double totalRetailPrice = mTrans.getTotalRetailPrice(transactionId, computerId);
		double totalDiscount = mTrans.getPriceDiscount(transactionId, computerId);
		double totalSalePrice = transactionVatable;//mTrans.getTotalSalePrice(transactionId, computerId) + transactionVatExclude;
		double totalPaid = mPayment.getTotalPaid(transactionId, computerId);
		double change = totalPaid - totalSalePrice;
		double beforVat = transactionVatable - transactionVat;
		
		try {
			mPrinter.openPrinter(Print.DEVTYPE_TCP, MPOSApplication.getPrinterIp(), 0, 1000);	
			Builder builder = new Builder(MPOSApplication.getPrinterName(), Builder.MODEL_ANK, 
					MPOSApplication.getContext());
			
			//builder.addTextLang(Builder.LANG_TH);
			if(MPOSApplication.getPrinterFont().equals("a")){
				builder.addTextFont(Builder.FONT_A);
			}else if(MPOSApplication.getPrinterFont().equals("b")){
				builder.addTextFont(Builder.FONT_B);
			}
			builder.addTextAlign(Builder.ALIGN_CENTER);
			builder.addTextSize(1, 1);
			// add header
			HeaderFooterReceipt headerFooter = new HeaderFooterReceipt(mContext);
			for(ShopData.HeaderFooterReceipt hf : 
				headerFooter.listHeaderFooter(HeaderFooterReceipt.HEADER_LINE_TYPE)){
				builder.addText(hf.getTextInLine());
				builder.addText("\n");
			}
			
			String saleDate = MPOSApplication.getContext().getString(R.string.date) + " " +
					GlobalProperty.dateTimeFormat(mContext, Util.getDateTime().getTime());
			String receiptNo = MPOSApplication.getContext().getString(R.string.receipt_no) + " " +
					mTrans.getReceiptNo(transactionId, computerId);
			String cashCheer = MPOSApplication.getContext().getString(R.string.cashier) + " " +
					mStaff.getStaff(mStaffId);
			builder.addText(saleDate + createLineSpace(saleDate.length()) + "\n");
			builder.addText(receiptNo + createLineSpace(receiptNo.length()) + "\n");
			builder.addText(cashCheer + createLineSpace(cashCheer.length()));
			builder.addText("\n" + createLine("=") + "\n");
			
			List<OrderTransaction.OrderDetail> orderLst = 
					mTrans.listAllOrderGroupByProduct(transactionId, computerId);
			builder.addTextAlign(Builder.ALIGN_CENTER);
	    	for(int i = 0; i < orderLst.size(); i++){
	    		OrderTransaction.OrderDetail order = 
	    				orderLst.get(i);
	    		
	    		String productName = order.getProductName();
	    		String productQty = GlobalProperty.qtyFormat(mContext, order.getQty()) + "x ";
	    		String productPrice = GlobalProperty.currencyFormat(mContext, order.getTotalRetailPrice());
	    		
	    		builder.addText(productQty);
	    		builder.addText(productName);
	    		builder.addText(createLineSpace(productQty.length() + 
	    				productName.length() + productPrice.length()));
	    		builder.addText(productPrice);
	    		builder.addText("\n");
	    	}
	    	builder.addText(createLine("-") + "\n");
	    	
	    	String itemText = MPOSApplication.getContext().getString(R.string.items) + ": ";
	    	String totalText = MPOSApplication.getContext().getString(R.string.total) + "...............";
	    	String changeText = MPOSApplication.getContext().getString(R.string.change) + " ";
	    	String beforeVatText = MPOSApplication.getContext().getString(R.string.before_vat);
	    	String discountText = MPOSApplication.getContext().getString(R.string.discount);
	    	String vatRateText = MPOSApplication.getContext().getString(R.string.tax) + " " +
	    			GlobalProperty.currencyFormat(vatRate, "#,###.##") + "%";
	    	
	    	String strTotalRetailPrice = GlobalProperty.currencyFormat(mContext, totalRetailPrice);
	    	String strTotalSalePrice = GlobalProperty.currencyFormat(mContext, totalSalePrice);
	    	String strTotalDiscount = "-" + GlobalProperty.currencyFormat(mContext,
	    			mTrans.getPriceDiscount(transactionId, computerId));
	    	//String strTotalPaid = GlobalProperty.currencyFormat(
	    	//		mPayment.getTotalPaid(transactionId, computerId));
	    	String strTotalChange = GlobalProperty.currencyFormat(mContext, change);
	    	String strBeforeVat = GlobalProperty.currencyFormat(mContext, beforVat);
	    	String strTransactionVat = GlobalProperty.currencyFormat(mContext, transactionVat);
	    	
	    	// total item
	    	String strTotalQty = String.valueOf(mTrans.getTotalQty(transactionId, computerId));
	    	builder.addText(itemText);
	    	builder.addText(strTotalQty);
	    	builder.addText(createLineSpace(itemText.length() + strTotalQty.length() + strTotalRetailPrice.length()));
	    	builder.addText(strTotalRetailPrice + "\n");
	    	
	    	// total discount
	    	if(totalDiscount > 0){
		    	builder.addText(discountText);
		    	builder.addText(createLineSpace(discountText.length() + strTotalDiscount.length()));
		    	builder.addText(strTotalDiscount + "\n");
	    	}
	    	
	    	// transaction exclude vat
	    	if(transactionVatExclude > 0){
	    		String vatExcludeText = MPOSApplication.getContext().getString(R.string.tax) + " " +
	    				GlobalProperty.currencyFormat(vatRate, "#,###.##") + "%";
	    		String strVatExclude = GlobalProperty.currencyFormat(mContext, transactionVatExclude);
	    		builder.addText(vatExcludeText);
	    		builder.addText(createLineSpace(vatExcludeText.length() + strVatExclude.length()));
	    		builder.addText(strVatExclude + "\n");
	    	}
	    	
	    	// total price
	    	builder.addText(totalText);
	    	builder.addText(createLineSpace(totalText.length() + strTotalSalePrice.length()));
	    	builder.addText(strTotalSalePrice + "\n");

	    	// total payment
	    	PaymentDetail paymentDetail = new PaymentDetail(mContext);
	    	List<Payment.PaymentDetail> paymentLst = paymentDetail.listPaymentGroupByType(transactionId, computerId);
	    	for(int i = 0; i < paymentLst.size(); i++){
	    		Payment.PaymentDetail payment = paymentLst.get(i);
		    	String paymentText = payment.getPayTypeName() + " ";
		    	String strTotalPaid = GlobalProperty.currencyFormat(mContext, payment.getPaid());
		    	if(i < paymentLst.size() - 1){
			    	builder.addText(paymentText);
		    		builder.addText(createLineSpace(paymentText.length() + strTotalPaid.length()));
			    	builder.addText(strTotalPaid);
		    	}else if(i == paymentLst.size() - 1){
			    	if(change > 0){
				    	builder.addText(paymentText);
				    	builder.addText(strTotalPaid);
			    		builder.addText(createLineSpace(changeText.length() + strTotalChange.length() + paymentText.length() + strTotalPaid.length()));
				    	builder.addText(changeText);
				    	builder.addText(strTotalChange);
				    }else{
				    	builder.addText(paymentText);
			    		builder.addText(createLineSpace(paymentText.length() + strTotalPaid.length()));
				    	builder.addText(strTotalPaid);
				    }
		    	}
	    		builder.addText("\n");
	    	}
		    builder.addText(createLine("=") + "\n");
		    
		    if(mShop.getShopProperty().getVatType() == Products.VAT_TYPE_INCLUDED){
			    // before vat
			    builder.addText(beforeVatText);
			    builder.addText(createLineSpace(beforeVatText.length() + strBeforeVat.length()));
			    builder.addText(strBeforeVat + "\n");
			    
			    // transaction vat
		    	builder.addText(vatRateText);
		    	builder.addText(createLineSpace(vatRateText.length() + strTransactionVat.length()));
		    	builder.addText(strTransactionVat + "\n");
		    }
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
				e.printStackTrace();
			}
			if (builder != null) {
				builder.clearCommandBuffer();
			}
		} catch (EposException e) {
			switch(e.getErrorStatus()){
			case EposException.ERR_PARAM:
				
				break;
			case EposException.ERR_MEMORY:
				
				break;
			case EposException.ERR_UNSUPPORTED:
				
				break;
			case EposException.ERR_FAILURE:
				
				break;
			}
			e.printStackTrace();
		}
		
		try {
			mPrinter.closePrinter();
			mPrinter = null;
		} catch (EposException e) {
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
	protected void onPostExecute(Void result) {
		mPrintListener.onPrintSuccess();
	}

	@Override
	protected void onPreExecute() {
		mPrintListener.onPrepare();
	}

	@Override
	protected Void doInBackground(Void... params) {
		PrintReceiptLog printLog = new PrintReceiptLog(mContext);
		printLog.open();
		for(PrintReceiptLog.PrintReceipt printReceipt : printLog.listPrintReceiptLog()){
			try {
				printReceipt(printReceipt.getTransactionId(), printReceipt.getComputerId());
				printLog.deletePrintStatus(printReceipt.getPriceReceiptLogId());
				
			} catch (Exception e) {
				printLog.updatePrintStatus(printReceipt.getPriceReceiptLogId(), PrintReceiptLog.PRINT_NOT_SUCCESS);
				Logger.appendLog(MPOSApplication.getContext(), 
						MPOSApplication.LOG_DIR, MPOSApplication.LOG_FILE_NAME, e.getMessage());
			}
		}
		return null;
	}
	
	public static interface PrintStatusListener{
		void onPrepare();
		void onPrintSuccess();
		void onPrintFail(String msg);
	}
}
