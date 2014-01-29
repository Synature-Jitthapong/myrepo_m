package com.syn.mpos;

import java.util.List;

import com.epson.eposprint.BatteryStatusChangeEventListener;
import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;
import com.epson.eposprint.StatusChangeEventListener;
import com.syn.mpos.database.HeaderFooterReceipt;
import com.syn.mpos.database.Util;
import com.syn.mpos.database.transaction.PaymentDetail;
import com.syn.mpos.database.transaction.Transaction;
import com.syn.pos.OrderTransaction;
import com.syn.pos.ShopData;

public class PrintReceipt implements BatteryStatusChangeEventListener, StatusChangeEventListener{
	private Print mPrinter;
	private Transaction mTrans;
	private PaymentDetail mPayment;
	
	public PrintReceipt(){
		mTrans = new Transaction(MPOSApplication.getWriteDatabase());
		mPayment = new PaymentDetail(MPOSApplication.getWriteDatabase());
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
	
	public void printReceipt(int transactionId, int computerId){
		OrderTransaction orderTrans = mTrans.getTransaction(transactionId, computerId);
		float totalRetailPrice = mTrans.getTotalRetailPrice(transactionId, computerId, false);
		float discount = mTrans.getPriceDiscount(transactionId, computerId, false);
		float totalPay = mPayment.getTotalPaid(transactionId, computerId);
		float totalSalePrice = mTrans.getTotalSalePrice(transactionId, computerId, false);
		float change = totalSalePrice - totalPay;
		float transactionVat = mTrans.getTransactionVat(transactionId, computerId);
		float transactionVatable = mTrans.getTransactionVatable(transactionId, computerId);
		float beforVat = transactionVatable - transactionVat;
		
		try {
			mPrinter.openPrinter(Print.DEVTYPE_TCP, MPOSApplication.getPrinterIp(), 0, 1000);	
			Builder builder = new Builder(MPOSApplication.getPrinterName(), Builder.MODEL_ANK, 
					MPOSApplication.getContext());
			
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
			builder.addText(MPOSApplication.getContext().getString(R.string.date) + " ");
			builder.addText(MPOSApplication.getGlobalProperty().dateFormat(Util.getDate().getTime()) + "\n");
			builder.addText(MPOSApplication.getContext().getString(R.string.receipt_no) + " ");
			builder.addText(orderTrans.getReceiptNo());
			builder.addText("\n" + createLine("=") + "\n");
			
			List<OrderTransaction.OrderDetail> orderLst = 
					mTrans.listAllOrder(transactionId, computerId);
			builder.addTextAlign(Builder.ALIGN_CENTER);
	    	for(int i = 0; i < orderLst.size(); i++){
	    		OrderTransaction.OrderDetail order = 
	    				orderLst.get(i);
	    		
	    		String productName = order.getProductName();
	    		String productQty = MPOSApplication.getGlobalProperty().qtyFormat(order.getQty());
	    		String productPrice = MPOSApplication.getGlobalProperty().currencyFormat(order.getTotalSalePrice());
	    		
	    		builder.addText(productQty + " ");
	    		builder.addText(productName);
	    		builder.addText(createLineSpace(productQty.length() + 
	    				productName.length() + productPrice.length()));
	    		builder.addText(productPrice);
	    		builder.addText("\n");
	    	}
	    	builder.addText(createLine("-") + "\n");
	    	
	    	String itemText = MPOSApplication.getContext().getString(R.string.items) + ": ";
	    	String totalText = MPOSApplication.getContext().getString(R.string.total);
	    	String paymentText = MPOSApplication.getContext().getString(R.string.pay);
	    	String changeText = MPOSApplication.getContext().getString(R.string.change);
	    	String beforeVatText = MPOSApplication.getContext().getString(R.string.before_vat);
	    	String discountText = MPOSApplication.getContext().getString(R.string.discount);
	    	String vatPercentText = MPOSApplication.getContext().getString(R.string.vat) + " 7%";
	    	
	    	String totalRetail = MPOSApplication.getGlobalProperty().currencyFormat(totalRetailPrice);
	    	String totalPrice = MPOSApplication.getGlobalProperty().currencyFormat(totalSalePrice);
	    	String totalDiscount = MPOSApplication.getGlobalProperty().currencyFormat(
	    			mTrans.getPriceDiscount(transactionId, computerId, false));
	    	String totalPayment = MPOSApplication.getGlobalProperty().currencyFormat(
	    			mPayment.getTotalPaid(transactionId, computerId));
	    	String totalChange = MPOSApplication.getGlobalProperty().currencyFormat(change);
	    	String priceBeforeVat = MPOSApplication.getGlobalProperty().currencyFormat(beforVat);
	    	String transVat = MPOSApplication.getGlobalProperty().currencyFormat(transactionVat);
	    	
	    	// total item
	    	String itemCount = String.valueOf(orderLst.size());
	    	builder.addText(itemText);
	    	builder.addText(itemCount);
	    	builder.addText(createLineSpace(itemText.length() + itemCount.length() + totalRetail.length()));
	    	builder.addText(totalRetail + "\n");
	    	
	    	// total discount
	    	if(discount > 0){
		    	builder.addText(discountText);
		    	builder.addText(createLineSpace(discountText.length() + totalDiscount.length()));
		    	builder.addText(totalDiscount + "\n");
	    	}
	    	
	    	// total price
	    	builder.addText(totalText);
	    	builder.addText(createLineSpace(totalText.length() + totalPrice.length()));
	    	builder.addText(totalPrice + "\n");

	    	// total payment
	    	builder.addText(paymentText);
	    	if(change > 0){
		    	builder.addText(totalPayment);
		    	builder.addText(changeText);
	    		builder.addText(createLineSpace(paymentText.length() + totalPayment.length() + changeText.length() + totalChange.length()));
	    		builder.addText(totalChange);
	    	}else{
	    		builder.addText(createLineSpace(paymentText.length() + totalPayment.length()));
		    	builder.addText(totalPayment);
	    	}
		    builder.addText("\n" + createLine("=") + "\n");
		    
		    builder.addText(beforeVatText);
		    builder.addText(createLineSpace(beforeVatText.length() + priceBeforeVat.length()));
		    builder.addText(priceBeforeVat + "\n");
		    
	    	builder.addText(vatPercentText);
	    	builder.addText(createLineSpace(vatPercentText.length() + transVat.length()));
	    	builder.addText(transVat + "\n");
	    	
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
//				new AlertDialog.Builder(MPOSApplication.getContext())
//				.setMessage(e.getMessage())
//				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//					}
//				}).show();
			}
			if (builder != null) {
				builder.clearCommandBuffer();
			}
		} catch (EposException e) {
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
}
