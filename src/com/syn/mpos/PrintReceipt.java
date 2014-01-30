package com.syn.mpos;

import java.util.List;

import com.epson.eposprint.BatteryStatusChangeEventListener;
import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;
import com.epson.eposprint.StatusChangeEventListener;
import com.syn.mpos.database.HeaderFooterReceipt;
import com.syn.mpos.database.Shop;
import com.syn.mpos.database.Util;
import com.syn.mpos.database.transaction.PaymentDetail;
import com.syn.mpos.database.transaction.Transaction;
import com.syn.pos.OrderTransaction;
import com.syn.pos.ShopData;

public class PrintReceipt implements BatteryStatusChangeEventListener, StatusChangeEventListener{
	private Print mPrinter;
	private Transaction mTrans;
	private Shop mShop;
	private PaymentDetail mPayment;
	
	public PrintReceipt(){
		mShop = new Shop(MPOSApplication.getReadDatabase());
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
		double vatRate = mShop.getCompanyVatRate();
		double transactionVat = mTrans.getTransactionVat(transactionId, computerId);
		double transactionVatExclude = mTrans.getTransactionVatExclude(transactionId, computerId);
		double transactionVatable = mTrans.getTransactionVatable(transactionId, computerId);
		double totalRetailPrice = mTrans.getTotalRetailPrice(transactionId, computerId);
		double totalDiscount = mTrans.getPriceDiscount(transactionId, computerId);
		double totalSalePrice = mTrans.getTotalSalePrice(transactionId, computerId) + transactionVatExclude;
		double totalPaid = mPayment.getTotalPaid(transactionId, computerId);
		double change = totalPaid - totalSalePrice;
		double beforVat = transactionVatable - transactionVat - transactionVatExclude;
		
		try {
			mPrinter.openPrinter(Print.DEVTYPE_TCP, MPOSApplication.getPrinterIp(), 0, 1000);	
			Builder builder = new Builder(MPOSApplication.getPrinterName(), Builder.MODEL_ANK, 
					MPOSApplication.getContext());
			
			//builder.addTextLang(Builder.LANG_TH);
			builder.addTextFont(Builder.FONT_B); //b
			builder.addTextAlign(Builder.ALIGN_CENTER);
			builder.addTextSize(1, 1);
			//builder.addTextStyle(Builder.FALSE, Builder.FALSE, Builder.FALSE, Builder.COLOR_1);
			//builder.addText("\nไก่จิกเด็กตายบนปากโอ่ง\n");
			// add header
			HeaderFooterReceipt headerFooter = new HeaderFooterReceipt(MPOSApplication.getWriteDatabase());
			for(ShopData.HeaderFooterReceipt hf : 
				headerFooter.listHeaderFooter(HeaderFooterReceipt.HEADER_LINE_TYPE)){
				builder.addText(hf.getTextInLine());
				builder.addText("\n");
			}
			
			String saleDate = MPOSApplication.getContext().getString(R.string.date) + " " +
					MPOSApplication.getGlobalProperty().dateFormat(Util.getDate().getTime());
			String receiptNo = MPOSApplication.getContext().getString(R.string.receipt_no) + " " +
					mTrans.getReceiptNo(transactionId, computerId);
			builder.addText(saleDate + createLineSpace(saleDate.length()) + "\n");
			builder.addText(receiptNo + createLineSpace(receiptNo.length()));
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
	    	String changeText = MPOSApplication.getContext().getString(R.string.change) + " ";
	    	String beforeVatText = MPOSApplication.getContext().getString(R.string.before_vat);
	    	String discountText = MPOSApplication.getContext().getString(R.string.discount);
	    	String vatRateText = MPOSApplication.getContext().getString(R.string.vat) + String.valueOf(vatRate) + "%";
	    	
	    	String strTotalRetailPrice = MPOSApplication.getGlobalProperty().currencyFormat(totalRetailPrice);
	    	String strTotalSalePrice = MPOSApplication.getGlobalProperty().currencyFormat(totalSalePrice);
	    	String strTotalDiscount = "-" + MPOSApplication.getGlobalProperty().currencyFormat(
	    			mTrans.getPriceDiscount(transactionId, computerId));
	    	String strTotalPaid = MPOSApplication.getGlobalProperty().currencyFormat(
	    			mPayment.getTotalPaid(transactionId, computerId));
	    	String strTotalChange = MPOSApplication.getGlobalProperty().currencyFormat(change);
	    	String strBeforeVat = MPOSApplication.getGlobalProperty().currencyFormat(beforVat);
	    	String strTransactionVat = MPOSApplication.getGlobalProperty().currencyFormat(transactionVat);
	    	
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
	    	
	    	// total price
	    	builder.addText(totalText);
	    	builder.addText(createLineSpace(totalText.length() + strTotalSalePrice.length()));
	    	builder.addText(strTotalSalePrice + "\n");

	    	// total payment
	    	builder.addText(paymentText);
	    	if(change > 0){
		    	builder.addText(strTotalPaid);
	    		builder.addText(createLineSpace(paymentText.length() + strTotalPaid.length() + changeText.length() + strTotalChange.length()));
		    	builder.addText(changeText);
		    	builder.addText(strTotalChange);
	    	}else{
	    		builder.addText(createLineSpace(paymentText.length() + strTotalPaid.length()));
		    	builder.addText(strTotalPaid);
	    	}
		    builder.addText("\n" + createLine("=") + "\n");
		    
		    // before vat
		    builder.addText(beforeVatText);
		    builder.addText(createLineSpace(beforeVatText.length() + strBeforeVat.length()));
		    builder.addText(strBeforeVat + "\n");
		    
		    // transaction vat
	    	builder.addText(vatRateText);
	    	builder.addText(createLineSpace(vatRateText.length() + strTransactionVat.length()));
	    	builder.addText(strTransactionVat + "\n");
	    	
	    	// transaction exclude vat
	    	if(transactionVatExclude > 0){
	    		String vatExcludeText = MPOSApplication.getContext().getString(R.string.vat_exclude);
	    		String strVatExclude = MPOSApplication.getGlobalProperty().currencyFormat(transactionVatExclude);
	    		builder.addText(vatExcludeText);
	    		builder.addText(createLineSpace(vatExcludeText.length() + strVatExclude.length()));
	    		builder.addText(strVatExclude + "\n");
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
}
