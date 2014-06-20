package com.synature.mpos;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.synature.mpos.database.CreditCard;
import com.synature.mpos.database.Formater;
import com.synature.mpos.database.HeaderFooterReceipt;
import com.synature.mpos.database.MPOSOrderTransaction;
import com.synature.mpos.database.PaymentDetail;
import com.synature.mpos.database.PrintReceiptLog;
import com.synature.mpos.database.Products;
import com.synature.mpos.database.Shop;
import com.synature.mpos.database.Staffs;
import com.synature.mpos.database.Transaction;
import com.synature.mpos.database.Util;
import com.synature.pos.Payment;
import com.synature.pos.ShopData;
import com.synature.util.Logger;

public class PrintReceipt extends AsyncTask<Void, Void, Void>{
	
	public static final String TAG = "PrintReceipt";
	private Transaction mOrders;
	private PaymentDetail mPayment;
	private Shop mShop;
	private HeaderFooterReceipt mHeaderFooter;
	private Formater mFormat;
	private Staffs mStaff;
	private CreditCard mCreditCard;
	private PrintStatusListener mPrintListener;
	private Context mContext;
	
	/**
	 * @param context
	 * @param listener
	 */
	public PrintReceipt(Context context, PrintStatusListener listener){
		mContext = context;
		mOrders = new Transaction(context);
		mPayment = new PaymentDetail(context);
		mShop = new Shop(context);
		mFormat = new Formater(context);
		mHeaderFooter = new HeaderFooterReceipt(context);
		mStaff = new Staffs(context);
		mCreditCard = new CreditCard(context);
		mPrintListener = listener;
	}
	
	protected class EPSONPrintReceipt extends EPSONPrinter{

		
		public EPSONPrintReceipt(Context context) {
			super(context);
		}

		@Override
		public void onBatteryStatusChangeEvent(String arg0, int arg1) {
		}

		@Override
		public void onStatusChangeEvent(String arg0, int arg1) {
		}

		@Override
		public void prepareDataToPrint(int transactionId){
			MPOSOrderTransaction trans = mOrders.getTransaction(transactionId);
			MPOSOrderTransaction.MPOSOrderDetail summOrder = mOrders.getSummaryOrder(transactionId);
			double beforVat = trans.getTransactionVatable() - trans.getTransactionVat();
			double change = mPayment.getTotalPaid(transactionId) - (summOrder.getTotalSalePrice() + summOrder.getVatExclude());

			try {
				mBuilder.addTextAlign(Builder.ALIGN_CENTER);
				// add void header
				if(trans.getTransactionStatusId() == Transaction.TRANS_STATUS_VOID){
					String voidReceipt = mContext.getString(R.string.void_receipt);
					Calendar cVoidTime = Calendar.getInstance();
					cVoidTime.setTimeInMillis(Long.parseLong(trans.getVoidTime()));
					String voidTime = mContext.getString(R.string.void_time) + " " + mFormat.dateTimeFormat(cVoidTime.getTime());
					String voidBy = mContext.getString(R.string.void_by) + " " + mStaff.getStaff(trans.getVoidStaffId()).getStaffName();
					String voidReason = mContext.getString(R.string.reason) + " " + trans.getVoidReason();
					mBuilder.addText(voidReceipt + "\n");
					mBuilder.addText(voidTime);
					mBuilder.addText(createHorizontalSpace(voidTime.length()) + "\n");
					mBuilder.addText(voidBy);
					mBuilder.addText(createHorizontalSpace(voidBy.length()) + "\n");
					mBuilder.addText(voidReason);
					mBuilder.addText(createHorizontalSpace(voidReason.length()) +"\n\n");
				}
				
				// add header
				for(ShopData.HeaderFooterReceipt hf : 
					mHeaderFooter.listHeaderFooter(HeaderFooterReceipt.HEADER_LINE_TYPE)){
					mBuilder.addText(hf.getTextInLine());
					mBuilder.addText("\n");
				}
				
				String saleDate = mContext.getString(R.string.date) + " " +
						mFormat.dateTimeFormat(Util.getCalendar().getTime());
				String receiptNo = mContext.getString(R.string.receipt_no) + " " +
						trans.getReceiptNo();
				String cashCheer = mContext.getString(R.string.cashier) + " " +
						mStaff.getStaff(trans.getOpenStaffId()).getStaffName();
				mBuilder.addText(saleDate + createHorizontalSpace(saleDate.length()) + "\n");
				mBuilder.addText(receiptNo + createHorizontalSpace(receiptNo.length()) + "\n");
				mBuilder.addText(cashCheer + createHorizontalSpace(cashCheer.length()));
				mBuilder.addText("\n" + createLine("=") + "\n");
				
				List<MPOSOrderTransaction.MPOSOrderDetail> orderLst = 
						mOrders.listAllOrderGroupByProduct(transactionId);
		    	for(int i = 0; i < orderLst.size(); i++){
		    		MPOSOrderTransaction.MPOSOrderDetail order = 
		    				orderLst.get(i);
		    		
		    		String productName = order.getProductName();
		    		String productQty = mFormat.qtyFormat(order.getQty()) + "x ";
		    		String productPrice = mFormat.currencyFormat(order.getPricePerUnit());
		    		
		    		mBuilder.addText(productQty);
		    		mBuilder.addText(productName);
		    		mBuilder.addText(createHorizontalSpace(productQty.length() + 
		    				productName.length() + productPrice.length()));
		    		mBuilder.addText(productPrice);
		    		mBuilder.addText("\n");
		    		
		    		// orderSet
		    		if(order.getOrderSetDetailLst() != null){
		    			for(MPOSOrderTransaction.OrderSet.OrderSetDetail setDetail :
		    				order.getOrderSetDetailLst()){
		    				String setName = setDetail.getProductName();
		    				String setQty = "   " + mFormat.qtyFormat(setDetail.getOrderSetQty()) + "x ";
		    				String setPrice = mFormat.currencyFormat(setDetail.getProductPrice());
		    				mBuilder.addText(setQty);
		    				mBuilder.addText(setName);
		    				mBuilder.addText(createHorizontalSpace(setQty.length() + setName.length() + setPrice.length()));
		    				mBuilder.addText(setPrice);
		    				mBuilder.addText("\n");
		    			}
		    		}
		    	}
		    	mBuilder.addText(createLine("-") + "\n");
		    	
		    	String itemText = mContext.getString(R.string.items) + ": ";
		    	String totalText = mContext.getString(R.string.total) + "...............";
		    	String changeText = mContext.getString(R.string.change) + " ";
		    	String beforeVatText = mContext.getString(R.string.before_vat);
		    	String discountText = mContext.getString(R.string.discount);
		    	String vatRateText = mContext.getString(R.string.vat) + " " +
		    			mFormat.currencyFormat(mShop.getCompanyVatRate(), "#,###.##") + "%";
		    	
		    	String strTotalRetailPrice = mFormat.currencyFormat(summOrder.getTotalRetailPrice());
		    	String strTotalSale = mFormat.currencyFormat(summOrder.getTotalSalePrice() + summOrder.getVatExclude());
		    	String strTotalDiscount = "-" + mFormat.currencyFormat(summOrder.getPriceDiscount());
		    	String strTotalChange = mFormat.currencyFormat(change);
		    	String strBeforeVat = mFormat.currencyFormat(beforVat);
		    	String strTransactionVat = mFormat.currencyFormat(trans.getTransactionVat());
		    	
		    	// total item
		    	String strTotalQty = NumberFormat.getInstance().format(summOrder.getQty());
		    	mBuilder.addText(itemText);
		    	mBuilder.addText(strTotalQty);
		    	mBuilder.addText(createHorizontalSpace(itemText.length() + strTotalQty.length() + strTotalRetailPrice.length()));
		    	mBuilder.addText(strTotalRetailPrice + "\n");
		    	
		    	// total discount
		    	if(summOrder.getPriceDiscount() > 0){
			    	mBuilder.addText(discountText);
			    	mBuilder.addText(createHorizontalSpace(discountText.length() + strTotalDiscount.length()));
			    	mBuilder.addText(strTotalDiscount + "\n");
		    	}
		    	
		    	// transaction exclude vat
		    	if(trans.getTransactionVatExclude() > 0){
		    		String vatExcludeText = mContext.getString(R.string.vat) + " " +
		    				mFormat.currencyFormat(mShop.getCompanyVatRate(), "#,###.##") + "%";
		    		String strVatExclude = mFormat.currencyFormat(trans.getTransactionVatExclude());
		    		mBuilder.addText(vatExcludeText);
		    		mBuilder.addText(createHorizontalSpace(vatExcludeText.length() + strVatExclude.length()));
		    		mBuilder.addText(strVatExclude + "\n");
		    	}
		    	
		    	// total price
		    	mBuilder.addText(totalText);
		    	mBuilder.addText(createHorizontalSpace(totalText.length() + strTotalSale.length()));
		    	mBuilder.addText(strTotalSale + "\n");

		    	// total payment
		    	List<Payment.PaymentDetail> paymentLst = 
		    			mPayment.listPaymentGroupByType(transactionId);
		    	for(int i = 0; i < paymentLst.size(); i++){
		    		Payment.PaymentDetail payment = paymentLst.get(i);
			    	String strTotalPaid = mFormat.currencyFormat(payment.getPaid());
			    	if(payment.getPayTypeID() == PaymentDetail.PAY_TYPE_CREDIT){
			    		String paymentText = payment.getPayTypeName();
			    		String cardNoText = "xxxx xxxx xxxx ";
			    		try {
			    			paymentText = payment.getPayTypeName() + ":" + 
		    					mCreditCard.getCreditCardType(payment.getCreditCardType());
			    			cardNoText += payment.getCreaditCardNo().substring(12, 16);
			    		} catch (Exception e) {
			    			Logger.appendLog(mContext, MPOSApplication.LOG_DIR, 
			    					MPOSApplication.LOG_FILE_NAME, "Error gen creditcard no : " + e.getMessage());
			    		}
			    		mBuilder.addText(paymentText);
			    		mBuilder.addText(createHorizontalSpace(paymentText.length()));
			    		mBuilder.addText("\n");
		    			mBuilder.addText(cardNoText);
		    			mBuilder.addText(createHorizontalSpace(cardNoText.length() + strTotalPaid.length()));
		    			mBuilder.addText(strTotalPaid);
			    	}else{
			    		String paymentText = payment.getPayTypeName() + " ";
				    	if(i < paymentLst.size() - 1){
					    	mBuilder.addText(paymentText);
				    		mBuilder.addText(createHorizontalSpace(paymentText.length() + strTotalPaid.length()));
					    	mBuilder.addText(strTotalPaid);
				    	}else if(i == paymentLst.size() - 1){
					    	if(change > 0){
						    	mBuilder.addText(paymentText);
						    	mBuilder.addText(strTotalPaid);
					    		mBuilder.addText(createHorizontalSpace(changeText.length() + strTotalChange.length() + paymentText.length() + strTotalPaid.length()));
						    	mBuilder.addText(changeText);
						    	mBuilder.addText(strTotalChange);
						    }else{
						    	mBuilder.addText(paymentText);
					    		mBuilder.addText(createHorizontalSpace(paymentText.length() + strTotalPaid.length()));
						    	mBuilder.addText(strTotalPaid);
						    }
				    	}
			    	}
		    		mBuilder.addText("\n");
		    	}
			    mBuilder.addText(createLine("=") + "\n");
			    
			    if(mShop.getCompanyVatType() == Products.VAT_TYPE_INCLUDED){
				    // before vat
				    mBuilder.addText(beforeVatText);
				    mBuilder.addText(createHorizontalSpace(beforeVatText.length() + strBeforeVat.length()));
				    mBuilder.addText(strBeforeVat + "\n");
				    
				    // transaction vat
			    	mBuilder.addText(vatRateText);
			    	mBuilder.addText(createHorizontalSpace(vatRateText.length() + strTransactionVat.length()));
			    	mBuilder.addText(strTransactionVat + "\n");
			    }
		    	// add footer
		    	for(ShopData.HeaderFooterReceipt hf : 
					mHeaderFooter.listHeaderFooter(HeaderFooterReceipt.FOOTER_LINE_TYPE)){
					mBuilder.addText(hf.getTextInLine());
					mBuilder.addText("\n");
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
		}

		@Override
		public void prepareDataToPrint() {
		}
		
	}

	protected class WintecPrintReceipt extends WintecPrinter{

		@Override
		public void prepareDataToPrint(int transactionId) {
			MPOSOrderTransaction trans = mOrders.getTransaction(transactionId);
			MPOSOrderTransaction.MPOSOrderDetail summOrder = mOrders.getSummaryOrder(transactionId);
			double beforVat = trans.getTransactionVatable() - trans.getTransactionVat();
			double change = mPayment.getTotalPaid(transactionId) - (summOrder.getTotalSalePrice() + summOrder.getVatExclude());
			
			// add void header
			if(trans.getTransactionStatusId() == Transaction.TRANS_STATUS_VOID){
				mBuilder.append("<c>" + mContext.getString(R.string.void_bill) + "\n");
				Calendar voidTime = Calendar.getInstance();
				voidTime.setTimeInMillis(Long.parseLong(trans.getVoidTime()));
				mBuilder.append(mContext.getString(R.string.void_time) + " " + mFormat.dateTimeFormat(voidTime.getTime()) + "\n");
				mBuilder.append(mContext.getString(R.string.void_by) + " " + mStaff.getStaff(trans.getVoidStaffId()).getStaffName() + "\n");
				mBuilder.append(mContext.getString(R.string.reason) + " " + trans.getVoidReason() + "\n\n");
			}
			
			// add header
			for(ShopData.HeaderFooterReceipt hf : 
				mHeaderFooter.listHeaderFooter(HeaderFooterReceipt.HEADER_LINE_TYPE)){
				mBuilder.append("<c>");
				mBuilder.append(hf.getTextInLine());
				mBuilder.append("\n");
			}
			
			String saleDate = mContext.getString(R.string.date) + " " +
					mFormat.dateTimeFormat(Util.getCalendar().getTime());
			String receiptNo = mContext.getString(R.string.receipt_no) + " " +
					trans.getReceiptNo();
			String cashCheer = mContext.getString(R.string.cashier) + " " +
					mStaff.getStaff(trans.getOpenStaffId()).getStaffName();
			mBuilder.append(saleDate + createHorizontalSpace(saleDate.length()) + "\n");
			mBuilder.append(receiptNo + createHorizontalSpace(receiptNo.length()) + "\n");
			mBuilder.append(cashCheer + createHorizontalSpace(cashCheer.length()) + "\n");
			mBuilder.append(createLine("=") + "\n");
			
			List<MPOSOrderTransaction.MPOSOrderDetail> orderLst = 
					mOrders.listAllOrderGroupByProduct(transactionId);
	    	for(int i = 0; i < orderLst.size(); i++){
	    		MPOSOrderTransaction.MPOSOrderDetail order = 
	    				orderLst.get(i);
	    		String productName = order.getProductName();
	    		String productQty = mFormat.qtyFormat(order.getQty()) + "x ";
	    		String productPrice = mFormat.currencyFormat(order.getPricePerUnit());
	    		
	    		mBuilder.append(productQty);
	    		mBuilder.append(productName);
	    		mBuilder.append(createHorizontalSpace(productQty.length() + 
	    				productName.length() + productPrice.length()));
	    		mBuilder.append(productPrice);
	    		mBuilder.append("\n");
	    		
	    		// orderSet
	    		if(order.getOrderSetDetailLst() != null){
	    			for(MPOSOrderTransaction.OrderSet.OrderSetDetail setDetail :
	    				order.getOrderSetDetailLst()){
	    				String setName = setDetail.getProductName();
	    				String setQty = "   " + mFormat.qtyFormat(setDetail.getOrderSetQty()) + "x ";
	    				String setPrice = mFormat.currencyFormat(setDetail.getProductPrice());
	    				mBuilder.append(setQty);
	    				mBuilder.append(setName);
	    				mBuilder.append(createHorizontalSpace(setQty.length() + setName.length() + setPrice.length()));
	    				mBuilder.append(setPrice);
	    				mBuilder.append("\n");
	    			}
	    		}
	    	}
	    	mBuilder.append(createLine("-") + "\n");
	    	
	    	String itemText = mContext.getString(R.string.items) + ": ";
	    	String totalText = mContext.getString(R.string.total) + "...............";
	    	String changeText = mContext.getString(R.string.change) + " ";
	    	String beforeVatText = mContext.getString(R.string.before_vat);
	    	String discountText = mContext.getString(R.string.discount);
	    	String vatRateText = mContext.getString(R.string.vat) + " " +
	    			mFormat.currencyFormat(mShop.getCompanyVatRate(), "#,###.##") + "%";
	    	
	    	String strTotalRetailPrice = mFormat.currencyFormat(summOrder.getTotalRetailPrice());
	    	String strTotalSale = mFormat.currencyFormat(summOrder.getTotalSalePrice() + summOrder.getVatExclude());
	    	String strTotalDiscount = "-" + mFormat.currencyFormat(summOrder.getPriceDiscount());
	    	String strTotalChange = mFormat.currencyFormat(change);
	    	String strBeforeVat = mFormat.currencyFormat(beforVat);
	    	String strTransactionVat = mFormat.currencyFormat(trans.getTransactionVat());
	    	
	    	// total item
	    	String strTotalQty = NumberFormat.getInstance().format(summOrder.getQty());
	    	mBuilder.append(itemText);
	    	mBuilder.append(strTotalQty);
	    	mBuilder.append(createHorizontalSpace(itemText.length() + strTotalQty.length() + strTotalRetailPrice.length()));
	    	mBuilder.append(strTotalRetailPrice + "\n");
	    	
	    	// total discount
	    	if(summOrder.getPriceDiscount() > 0){
		    	mBuilder.append(discountText);
		    	mBuilder.append(createHorizontalSpace(discountText.length() + strTotalDiscount.length()));
		    	mBuilder.append(strTotalDiscount + "\n");
	    	}
	    	
	    	// transaction exclude vat
	    	if(trans.getTransactionVatExclude() > 0){
	    		String vatExcludeText = mContext.getString(R.string.vat) + " " +
	    				mFormat.currencyFormat(mShop.getCompanyVatRate(), "#,###.##") + "%";
	    		String strVatExclude = mFormat.currencyFormat(trans.getTransactionVatExclude());
	    		mBuilder.append(vatExcludeText);
	    		mBuilder.append(createHorizontalSpace(vatExcludeText.length() + strVatExclude.length()));
	    		mBuilder.append(strVatExclude + "\n");
	    	}
	    	
	    	// total price
	    	mBuilder.append(totalText);
	    	mBuilder.append(createHorizontalSpace(totalText.length() + strTotalSale.length()));
	    	mBuilder.append(strTotalSale + "\n");

	    	// total payment
	    	List<Payment.PaymentDetail> paymentLst = 
	    			mPayment.listPaymentGroupByType(transactionId);
	    	for(int i = 0; i < paymentLst.size(); i++){
	    		Payment.PaymentDetail payment = paymentLst.get(i);
		    	String strTotalPaid = mFormat.currencyFormat(payment.getPaid());
		    	if(payment.getPayTypeID() == PaymentDetail.PAY_TYPE_CREDIT){
		    		String paymentText = payment.getPayTypeName();
		    		String cardNoText = "xxxx xxxx xxxx ";
		    		try {
		    			paymentText = payment.getPayTypeName() + ":" + 
	    					mCreditCard.getCreditCardType(payment.getCreditCardType());
		    			cardNoText += payment.getCreaditCardNo().substring(12, 16);
		    		} catch (Exception e) {
		    			Logger.appendLog(mContext, MPOSApplication.LOG_DIR, 
		    					MPOSApplication.LOG_FILE_NAME, "Error gen creditcard no : " + e.getMessage());
		    		}
		    		mBuilder.append(paymentText);
		    		mBuilder.append(createHorizontalSpace(paymentText.length()));
		    		mBuilder.append("\n");
	    			mBuilder.append(cardNoText);
	    			mBuilder.append(createHorizontalSpace(cardNoText.length() + strTotalPaid.length()));
	    			mBuilder.append(strTotalPaid);
		    	}else{
		    		String paymentText = payment.getPayTypeName() + " ";
			    	if(i < paymentLst.size() - 1){
				    	mBuilder.append(paymentText);
			    		mBuilder.append(createHorizontalSpace(paymentText.length() + strTotalPaid.length()));
				    	mBuilder.append(strTotalPaid);
			    	}else if(i == paymentLst.size() - 1){
				    	if(change > 0){
					    	mBuilder.append(paymentText);
					    	mBuilder.append(strTotalPaid);
				    		mBuilder.append(createHorizontalSpace(changeText.length() + strTotalChange.length() + paymentText.length() + strTotalPaid.length()));
					    	mBuilder.append(changeText);
					    	mBuilder.append(strTotalChange);
					    }else{
					    	mBuilder.append(paymentText);
				    		mBuilder.append(createHorizontalSpace(paymentText.length() + strTotalPaid.length()));
					    	mBuilder.append(strTotalPaid);
					    }
			    	}
		    	}
	    		mBuilder.append("\n");
	    	}
		    mBuilder.append(createLine("=") + "\n");
		    
		    if(mShop.getCompanyVatType() == Products.VAT_TYPE_INCLUDED){
			    // before vat
			    mBuilder.append(beforeVatText);
			    mBuilder.append(createHorizontalSpace(beforeVatText.length() + strBeforeVat.length()));
			    mBuilder.append(strBeforeVat + "\n");
			    
			    // transaction vat
		    	mBuilder.append(vatRateText);
		    	mBuilder.append(createHorizontalSpace(vatRateText.length() + strTransactionVat.length()));
		    	mBuilder.append(strTransactionVat + "\n");
		    }
		    
	    	// add footer
	    	for(ShopData.HeaderFooterReceipt hf : 
				mHeaderFooter.listHeaderFooter(HeaderFooterReceipt.FOOTER_LINE_TYPE)){
	    		mBuilder.append("<c>");
				mBuilder.append(hf.getTextInLine());
				mBuilder.append("\n");
			}
		}

		@Override
		public void prepareDataToPrint() {
		}
		
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
		PrintReceiptLog printLog = new PrintReceiptLog(mContext.getApplicationContext());
		for(PrintReceiptLog.PrintReceipt printReceipt : printLog.listPrintReceiptLog()){
			try {
				if(MPOSApplication.getInternalPrinterSetting(mContext)){
					WintecPrintReceipt wt = new WintecPrintReceipt();
					wt.prepareDataToPrint(printReceipt.getTransactionId());
					wt.print();
				}else{
					EPSONPrintReceipt ep = new EPSONPrintReceipt(mContext);
					ep.prepareDataToPrint(printReceipt.getTransactionId());
					ep.print();
				}
				printLog.deletePrintStatus(printReceipt.getPriceReceiptLogId());
				
			} catch (Exception e) {
				printLog.updatePrintStatus(printReceipt.getPriceReceiptLogId(), PrintReceiptLog.PRINT_NOT_SUCCESS);
				Logger.appendLog(mContext, 
						MPOSApplication.LOG_DIR, MPOSApplication.LOG_FILE_NAME, 
						" Print receipt fail : " + e.getMessage());
				mPrintListener.onPrintFail(e.getMessage());
			}
		}
		return null;
	}
	
	public static String unicodeToASCII(String unicode) {
		// initial temporary space of ascii.
		StringBuffer ascii = new StringBuffer(unicode);
		int code;

		// continue loop based on number of character.
		for (int i = 0; i < unicode.length(); i++) {
			// reading a value of each character in the unicode (as String).
			code = (int) unicode.charAt(i);

			// check the value is Thai language in Unicode scope or not.
			if ((0xE01 <= code) && (code <= 0xE5B)) {
				// if yes, it will be converted to Thai language in ASCII scope.
				ascii.setCharAt(i, (char) (code - 0xD60));
			}
		}
		return ascii.toString();
	}
		
	public static OPOSThaiText parsingThaiCodePage21(String prntTxt) {
		String strLine1 = null;
		String strLine2 = null;
		String strLine3 = null;
		int i = 0;
		int aCode = 0;
		int aNextCode = 0;
		String strChar = null;
		OPOSThaiText resultText = new OPOSThaiText();
		strLine1 = "";
		strLine2 = "";
		strLine3 = "";
		for (i = 0; i < prntTxt.length(); i++) {
			aCode = prntTxt.charAt(i);
			strChar = Character.toString(prntTxt.charAt(i));
			switch (aCode) {
			case 0xd1:
			case 0xd4:
			case 213:
			case 214:
			case 215:
				// ��������������������������������� ��������� ��������������������������� ������������ ������ ������
				// Check if letter next to it is ���������������������������
				if (i == prntTxt.length() - 1) {
					// This is the last letter
					strChar = Character.toString(prntTxt.charAt(i));
				} else {
					aNextCode = (int) prntTxt.charAt(i + 1);
					switch (aNextCode) {
					// Comboine current and the next character into new one
					case 0xe8:
					case 233:
					case 234:
					case 235:
					case 236:
					case 237:
						// ��������������������������� ������������������������������
						switch (aNextCode) {
						case 0xe8:
							// ������������������
							switch (aCode) {
							case 0xd1:
								// ���������������������������������
								aNextCode = 0x80;
								break;
							case 0xd4:
								// ������
								aNextCode = 0x84;
								break;
							case 0xd5:
								// ������
								aNextCode = 0x89;
								break;
							case 0xd6:
								// ������
								aNextCode = 0x8d;
								break;
							case 0xd7:
								// ������
								aNextCode = 0x91;
								break;
							}

							break;
						case 0xe9:
							// ���������������
							switch (aCode) {
							case 0xd1:
								// ���������������������������������
								aNextCode = 0x81;
								break;
							case 0xd4:
								// ������
								aNextCode = 0x85;
								break;
							case 0xd5:
								// ������
								aNextCode = 0x8a;
								break;
							case 0xd6:
								// ������
								aNextCode = 0x8e;
								break;
							case 0xd7:
								// ������
								aNextCode = 0x92;
								break;
							}

							break;
						case 0xea:
							// ������������������
							switch (aCode) {
							case 0xd1:
								// ���������������������������������
								aNextCode = 0x82;
								break;
							case 0xd4:
								// ������
								aNextCode = 0x86;
								break;
							case 0xd5:
								// ������
								aNextCode = 0x8b;
								break;
							case 0xd6:
								// ������
								aNextCode = 0x8f;
								break;
							case 0xd7:
								// ������
								aNextCode = 0x93;
								break;
							}
							break;
						case 0xeb:
							// ������������������������
							switch (aCode) {
							case 0xd1:
								// ���������������������������������
								aNextCode = 0x83;
								break;
							case 0xd4:
								// ������
								aNextCode = 0x87;
								break;
							case 0xd5:
								// ������
								aNextCode = 0x8c;
								break;
							case 0xd6:
								// ������
								aNextCode = 0x90;
								break;
							case 0xd7:
								// ������
								aNextCode = 0x94;
								break;
							}
							break;
						case 0xec:
							// ���������������������
							switch (aCode) {
							case 0xd4:
								// ������
								aNextCode = 0x88;

								break;
							}
							break;
						}
						strChar = String.valueOf(aNextCode);
						i += 1;

						break;
					default:
						strChar = Character.toString(prntTxt.charAt(i));
						break;
					}
				}
				strLine1 = strLine1.substring(1, strLine1.length() - 1)
						+ strChar;

				break;
			case 0xe7:
			case 232:
			case 233:
			case 234:
			case 235:
			case 236:
			case 237:
				// ���������������������������
				// Check if letter next to it is ���������������������������
				if (i == prntTxt.length() - 1) {
					// This is the last letter
					strLine1 = strLine1.substring(1, strLine1.length() - 1)
							+ prntTxt.charAt(i);
				} else {
					aNextCode = (int) prntTxt.charAt(i);
					switch (aNextCode) {
					case 0xd3:
						// ���������������
						switch (aCode) {
						// ��������������������� ��������������� ������������ ��������������� ��������� ���������������������
						// ���������������������������������������������������������������������������������
						case 0xe8:
							// ������������������
							strLine1 = strLine1.substring(1,
									strLine1.length() - 1) + (char) 0x95 + " ";
							break;
						case 0xe9:
							// ���������������
							strLine1 = strLine1.substring(1,
									strLine1.length() - 1) + (char) 0x96 + " ";
							break;
						case 0xea:
							// ������������������
							strLine1 = strLine1.substring(1,
									strLine1.length() - 1) + (char) 0x97 + " ";
							break;
						case 0xeb:
							// ������������������������
							strLine1 = strLine1.substring(1,
									strLine1.length() - 1) + (char) 0x98 + " ";
							break;
						}
						strLine2 += (char) 0xd2;
						strLine3 += " ";
						i += 1;
						break;
					default:
						strLine1 = strLine1.substring(1, strLine1.length() - 1)
								+ prntTxt.charAt(i);
						break;
					}
				}

				break;
			case 0xd8:
			case 217:
				// ��������� ������ ������
				strLine3 = strLine3.substring(1, strLine3.length() - 1)
						+ Character.toString(prntTxt.charAt(i));

				break;
			default:
				strLine1 += " ";
				strLine2 += Character.toString(prntTxt.charAt(i));
				strLine3 += " ";
				break;
			}
		}
		resultText.TextLine1 = strLine1;
		resultText.TextLine2 = strLine2;
		resultText.TextLine3 = strLine3;
		return resultText;
	}

	public static class OPOSThaiText
	{
	    public String TextLine1;
	    public String TextLine2;
	    public String TextLine3;
	}
	
	public static interface PrintStatusListener{
		void onPrepare();
		void onPrintSuccess();
		void onPrintFail(String msg);
	}
}
