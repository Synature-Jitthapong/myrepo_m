package com.syn.mpos;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import cn.wintec.wtandroidjar2.ComIO;
import cn.wintec.wtandroidjar2.Printer;

import com.epson.eposprint.BatteryStatusChangeEventListener;
import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;
import com.epson.eposprint.StatusChangeEventListener;
import com.j1tth4.util.Logger;
import com.syn.mpos.dao.CreditCardDao;
import com.syn.mpos.dao.GlobalPropertyDao;
import com.syn.mpos.dao.HeaderFooterReceiptDao;
import com.syn.mpos.dao.MPOSOrderTransaction;
import com.syn.mpos.dao.PaymentDao;
import com.syn.mpos.dao.PrintReceiptLogDao;
import com.syn.mpos.dao.ProductsDao;
import com.syn.mpos.dao.ShopDao;
import com.syn.mpos.dao.StaffDao;
import com.syn.mpos.dao.TransactionDao;
import com.syn.mpos.dao.Util;
import com.syn.pos.Payment;
import com.syn.pos.ShopData;

public class PrintReceipt extends AsyncTask<Void, Void, Void> 
	implements BatteryStatusChangeEventListener, StatusChangeEventListener{
	
	public static final String TAG = "PrintReceipt";
	private TransactionDao mOrders;
	private PaymentDao mPayment;
	private ShopDao mShop;
	private HeaderFooterReceiptDao mHeaderFooter;
	private GlobalPropertyDao mGlobal;
	private StaffDao mStaff;
	private CreditCardDao mCreditCard;
	private PrintStatusListener mPrintListener;
	private Context mContext;
	private Print mPrinter;
	
	/**
	 * @param context
	 * @param listener
	 */
	public PrintReceipt(Context context, PrintStatusListener listener){
		mContext = context;
		mOrders = new TransactionDao(context.getApplicationContext());
		mPayment = new PaymentDao(context.getApplicationContext());
		mShop = new ShopDao(context.getApplicationContext());
		mGlobal = new GlobalPropertyDao(context.getApplicationContext());
		mHeaderFooter = new HeaderFooterReceiptDao(context.getApplicationContext());
		mStaff = new StaffDao(context.getApplicationContext());
		mCreditCard = new CreditCardDao(context.getApplicationContext());
		mPrintListener = listener;
	}
	
	private static String createLine(String sign){
		StringBuilder line = new StringBuilder();
		for(int i = 0; i <= 45; i++){
			line.append(sign);
		}
		return line.toString();
	}
	
	private static String adjustAlignCenter(String text){
		int maxSpace = 45;
		int rimSpace = (maxSpace - text.length()) / 2;
		StringBuilder empText = new StringBuilder();
		for(int i = 0; i < rimSpace; i++){
			empText.append(" ");
		}
		return empText.toString() + text + empText.toString();
	}
	
	private static String createHorizontalSpace(int usedSpace){
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
	
	/**
	 * @param transactionId
	 */
	protected void printReceiptEpson(int transactionId){
		MPOSOrderTransaction trans = mOrders.getTransaction(transactionId);
		MPOSOrderTransaction.MPOSOrderDetail summOrder = mOrders.getSummaryOrder(transactionId);
		double beforVat = trans.getTransactionVatable() - trans.getTransactionVat();
		double change = mPayment.getTotalPaid(transactionId) - (summOrder.getTotalSalePrice() + summOrder.getVatExclude());

		mPrinter = new Print(mContext.getApplicationContext());
		mPrinter.setStatusChangeEventCallback(this);
		mPrinter.setBatteryStatusChangeEventCallback(this);
		try {
			mPrinter.openPrinter(Print.DEVTYPE_TCP, MPOSApplication.getPrinterIp(mContext), 0, 1000);	
			Builder builder = new Builder(MPOSApplication.getPrinterName(mContext), Builder.MODEL_ANK, 
					mContext);
			
			//builder.addTextLang(Builder.LANG_TH);
			if(MPOSApplication.getPrinterFont(mContext).equals("a")){
				builder.addTextFont(Builder.FONT_A);
			}else if(MPOSApplication.getPrinterFont(mContext).equals("b")){
				builder.addTextFont(Builder.FONT_B);
			}

			builder.addTextAlign(Builder.ALIGN_CENTER);
			builder.addTextSize(1, 1);
			// add void header
			if(trans.getTransactionStatusId() == TransactionDao.TRANS_STATUS_VOID){
				String voidReceipt = mContext.getString(R.string.void_receipt);
				Calendar cVoidTime = Calendar.getInstance();
				cVoidTime.setTimeInMillis(Long.parseLong(trans.getVoidTime()));
				String voidTime = mContext.getString(R.string.void_time) + " " + mGlobal.dateTimeFormat(cVoidTime.getTime());
				String voidBy = mContext.getString(R.string.void_by) + " " + mStaff.getStaff(trans.getVoidStaffId()).getStaffName();
				String voidReason = mContext.getString(R.string.reason) + " " + trans.getVoidReason();
				builder.addText(voidReceipt + "\n");
				builder.addText(voidTime);
				builder.addText(createHorizontalSpace(voidTime.length()) + "\n");
				builder.addText(voidBy);
				builder.addText(createHorizontalSpace(voidBy.length()) + "\n");
				builder.addText(voidReason);
				builder.addText(createHorizontalSpace(voidReason.length()) +"\n\n");
			}
			
			// add header
			for(ShopData.HeaderFooterReceipt hf : 
				mHeaderFooter.listHeaderFooter(HeaderFooterReceiptDao.HEADER_LINE_TYPE)){
				builder.addText(hf.getTextInLine());
				builder.addText("\n");
			}
			
			String saleDate = mContext.getString(R.string.date) + " " +
					mGlobal.dateTimeFormat(Util.getCalendar().getTime());
			String receiptNo = mContext.getString(R.string.receipt_no) + " " +
					trans.getReceiptNo();
			String cashCheer = mContext.getString(R.string.cashier) + " " +
					mStaff.getStaff(trans.getOpenStaffId()).getStaffName();
			builder.addText(saleDate + createHorizontalSpace(saleDate.length()) + "\n");
			builder.addText(receiptNo + createHorizontalSpace(receiptNo.length()) + "\n");
			builder.addText(cashCheer + createHorizontalSpace(cashCheer.length()));
			builder.addText("\n" + createLine("=") + "\n");
			
			List<MPOSOrderTransaction.MPOSOrderDetail> orderLst = 
					mOrders.listAllOrderGroupByProduct(transactionId);
	    	for(int i = 0; i < orderLst.size(); i++){
	    		MPOSOrderTransaction.MPOSOrderDetail order = 
	    				orderLst.get(i);
	    		
	    		String productName = order.getProductName();
	    		String productQty = mGlobal.qtyFormat(order.getQty()) + "x ";
	    		String productPrice = mGlobal.currencyFormat(order.getPricePerUnit());
	    		
	    		builder.addText(productQty);
	    		builder.addText(productName);
	    		builder.addText(createHorizontalSpace(productQty.length() + 
	    				productName.length() + productPrice.length()));
	    		builder.addText(productPrice);
	    		builder.addText("\n");
	    		
	    		// orderSet
	    		if(order.getOrderSetDetailLst() != null){
	    			for(MPOSOrderTransaction.OrderSet.OrderSetDetail setDetail :
	    				order.getOrderSetDetailLst()){
	    				String setName = setDetail.getProductName();
	    				String setQty = "   " + mGlobal.qtyFormat(setDetail.getOrderSetQty()) + "x ";
	    				String setPrice = mGlobal.currencyFormat(setDetail.getProductPrice());
	    				builder.addText(setQty);
	    				builder.addText(setName);
	    				builder.addText(createHorizontalSpace(setQty.length() + setName.length() + setPrice.length()));
	    				builder.addText(setPrice);
	    				builder.addText("\n");
	    			}
	    		}
	    	}
	    	builder.addText(createLine("-") + "\n");
	    	
	    	String itemText = mContext.getString(R.string.items) + ": ";
	    	String totalText = mContext.getString(R.string.total) + "...............";
	    	String changeText = mContext.getString(R.string.change) + " ";
	    	String beforeVatText = mContext.getString(R.string.before_vat);
	    	String discountText = mContext.getString(R.string.discount);
	    	String vatRateText = mContext.getString(R.string.tax) + " " +
	    			mGlobal.currencyFormat(mShop.getCompanyVatRate(), "#,###.##") + "%";
	    	
	    	String strTotalRetailPrice = mGlobal.currencyFormat(summOrder.getTotalRetailPrice());
	    	String strTotalSale = mGlobal.currencyFormat(summOrder.getTotalSalePrice() + summOrder.getVatExclude());
	    	String strTotalDiscount = "-" + mGlobal.currencyFormat(summOrder.getPriceDiscount());
	    	String strTotalChange = mGlobal.currencyFormat(change);
	    	String strBeforeVat = mGlobal.currencyFormat(beforVat);
	    	String strTransactionVat = mGlobal.currencyFormat(trans.getTransactionVat());
	    	
	    	// total item
	    	String strTotalQty = NumberFormat.getInstance().format(summOrder.getQty());
	    	builder.addText(itemText);
	    	builder.addText(strTotalQty);
	    	builder.addText(createHorizontalSpace(itemText.length() + strTotalQty.length() + strTotalRetailPrice.length()));
	    	builder.addText(strTotalRetailPrice + "\n");
	    	
	    	// total discount
	    	if(summOrder.getPriceDiscount() > 0){
		    	builder.addText(discountText);
		    	builder.addText(createHorizontalSpace(discountText.length() + strTotalDiscount.length()));
		    	builder.addText(strTotalDiscount + "\n");
	    	}
	    	
	    	// transaction exclude vat
	    	if(trans.getTransactionVatExclude() > 0){
	    		String vatExcludeText = mContext.getString(R.string.tax) + " " +
	    				mGlobal.currencyFormat(mShop.getCompanyVatRate(), "#,###.##") + "%";
	    		String strVatExclude = mGlobal.currencyFormat(trans.getTransactionVatExclude());
	    		builder.addText(vatExcludeText);
	    		builder.addText(createHorizontalSpace(vatExcludeText.length() + strVatExclude.length()));
	    		builder.addText(strVatExclude + "\n");
	    	}
	    	
	    	// total price
	    	builder.addText(totalText);
	    	builder.addText(createHorizontalSpace(totalText.length() + strTotalSale.length()));
	    	builder.addText(strTotalSale + "\n");

	    	// total payment
	    	List<Payment.PaymentDetail> paymentLst = 
	    			mPayment.listPaymentGroupByType(transactionId);
	    	for(int i = 0; i < paymentLst.size(); i++){
	    		Payment.PaymentDetail payment = paymentLst.get(i);
		    	String strTotalPaid = mGlobal.currencyFormat(payment.getPaid());
		    	if(payment.getPayTypeID() == PaymentDao.PAY_TYPE_CREDIT){
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
		    		builder.addText(paymentText);
		    		builder.addText(createHorizontalSpace(paymentText.length()));
		    		builder.addText("\n");
	    			builder.addText(cardNoText);
	    			builder.addText(createHorizontalSpace(cardNoText.length() + strTotalPaid.length()));
	    			builder.addText(strTotalPaid);
		    	}else{
		    		String paymentText = payment.getPayTypeName() + " ";
			    	if(i < paymentLst.size() - 1){
				    	builder.addText(paymentText);
			    		builder.addText(createHorizontalSpace(paymentText.length() + strTotalPaid.length()));
				    	builder.addText(strTotalPaid);
			    	}else if(i == paymentLst.size() - 1){
				    	if(change > 0){
					    	builder.addText(paymentText);
					    	builder.addText(strTotalPaid);
				    		builder.addText(createHorizontalSpace(changeText.length() + strTotalChange.length() + paymentText.length() + strTotalPaid.length()));
					    	builder.addText(changeText);
					    	builder.addText(strTotalChange);
					    }else{
					    	builder.addText(paymentText);
				    		builder.addText(createHorizontalSpace(paymentText.length() + strTotalPaid.length()));
					    	builder.addText(strTotalPaid);
					    }
			    	}
		    	}
	    		builder.addText("\n");
	    	}
		    builder.addText(createLine("=") + "\n");
		    
		    if(mShop.getCompanyVatType() == ProductsDao.VAT_TYPE_INCLUDED){
			    // before vat
			    builder.addText(beforeVatText);
			    builder.addText(createHorizontalSpace(beforeVatText.length() + strBeforeVat.length()));
			    builder.addText(strBeforeVat + "\n");
			    
			    // transaction vat
		    	builder.addText(vatRateText);
		    	builder.addText(createHorizontalSpace(vatRateText.length() + strTransactionVat.length()));
		    	builder.addText(strTransactionVat + "\n");
		    }
	    	// add footer
	    	for(ShopData.HeaderFooterReceipt hf : 
				mHeaderFooter.listHeaderFooter(HeaderFooterReceiptDao.FOOTER_LINE_TYPE)){
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

	/**
	 * @param transactionId
	 */
	public void printReceiptWintec(int transactionId){
		StringBuilder builder = new StringBuilder();
		Printer printer=null;
		final String devicePath = "/dev/ttySAC1";
		final ComIO.Baudrate baudrate = ComIO.Baudrate.valueOf("BAUD_38400");
		printer = new Printer(devicePath,baudrate);
		
		MPOSOrderTransaction trans = mOrders.getTransaction(transactionId);
		MPOSOrderTransaction.MPOSOrderDetail summOrder = mOrders.getSummaryOrder(transactionId);
		double beforVat = trans.getTransactionVatable() - trans.getTransactionVat();
		double change = mPayment.getTotalPaid(transactionId) - (summOrder.getTotalSalePrice() + summOrder.getVatExclude());
		
		// add void header
		if(trans.getTransactionStatusId() == TransactionDao.TRANS_STATUS_VOID){
			builder.append("<c>" + mContext.getString(R.string.void_bill) + "\n");
			Calendar voidTime = Calendar.getInstance();
			voidTime.setTimeInMillis(Long.parseLong(trans.getVoidTime()));
			builder.append(mContext.getString(R.string.void_time) + " " + mGlobal.dateTimeFormat(voidTime.getTime()) + "\n");
			builder.append(mContext.getString(R.string.void_by) + " " + mStaff.getStaff(trans.getVoidStaffId()).getStaffName() + "\n");
			builder.append(mContext.getString(R.string.reason) + " " + trans.getVoidReason() + "\n\n");
		}
		
		// add header
		for(ShopData.HeaderFooterReceipt hf : 
			mHeaderFooter.listHeaderFooter(HeaderFooterReceiptDao.HEADER_LINE_TYPE)){
			builder.append("<c>");
			builder.append(hf.getTextInLine());
			builder.append("\n");
		}
		
		String saleDate = mContext.getString(R.string.date) + " " +
				mGlobal.dateTimeFormat(Util.getCalendar().getTime());
		String receiptNo = mContext.getString(R.string.receipt_no) + " " +
				trans.getReceiptNo();
		String cashCheer = mContext.getString(R.string.cashier) + " " +
				mStaff.getStaff(trans.getOpenStaffId()).getStaffName();
		builder.append(saleDate + createHorizontalSpace(saleDate.length()) + "\n");
		builder.append(receiptNo + createHorizontalSpace(receiptNo.length()) + "\n");
		builder.append(cashCheer + createHorizontalSpace(cashCheer.length()) + "\n");
		builder.append(createLine("=") + "\n");
		
		List<MPOSOrderTransaction.MPOSOrderDetail> orderLst = 
				mOrders.listAllOrderGroupByProduct(transactionId);
    	for(int i = 0; i < orderLst.size(); i++){
    		MPOSOrderTransaction.MPOSOrderDetail order = 
    				orderLst.get(i);
    		String productName = order.getProductName();
    		String productQty = mGlobal.qtyFormat(order.getQty()) + "x ";
    		String productPrice = mGlobal.currencyFormat(order.getPricePerUnit());
    		
    		builder.append(productQty);
    		builder.append(productName);
    		builder.append(createHorizontalSpace(productQty.length() + 
    				productName.length() + productPrice.length()));
    		builder.append(productPrice);
    		builder.append("\n");
    		
    		// orderSet
    		if(order.getOrderSetDetailLst() != null){
    			for(MPOSOrderTransaction.OrderSet.OrderSetDetail setDetail :
    				order.getOrderSetDetailLst()){
    				String setName = setDetail.getProductName();
    				String setQty = "   " + mGlobal.qtyFormat(setDetail.getOrderSetQty()) + "x ";
    				String setPrice = mGlobal.currencyFormat(setDetail.getProductPrice());
    				builder.append(setQty);
    				builder.append(setName);
    				builder.append(createHorizontalSpace(setQty.length() + setName.length() + setPrice.length()));
    				builder.append(setPrice);
    				builder.append("\n");
    			}
    		}
    	}
    	builder.append(createLine("-") + "\n");
    	
    	String itemText = mContext.getString(R.string.items) + ": ";
    	String totalText = mContext.getString(R.string.total) + "...............";
    	String changeText = mContext.getString(R.string.change) + " ";
    	String beforeVatText = mContext.getString(R.string.before_vat);
    	String discountText = mContext.getString(R.string.discount);
    	String vatRateText = mContext.getString(R.string.tax) + " " +
    			mGlobal.currencyFormat(mShop.getCompanyVatRate(), "#,###.##") + "%";
    	
    	String strTotalRetailPrice = mGlobal.currencyFormat(summOrder.getTotalRetailPrice());
    	String strTotalSale = mGlobal.currencyFormat(summOrder.getTotalSalePrice() + summOrder.getVatExclude());
    	String strTotalDiscount = "-" + mGlobal.currencyFormat(summOrder.getPriceDiscount());
    	String strTotalChange = mGlobal.currencyFormat(change);
    	String strBeforeVat = mGlobal.currencyFormat(beforVat);
    	String strTransactionVat = mGlobal.currencyFormat(trans.getTransactionVat());
    	
    	// total item
    	String strTotalQty = NumberFormat.getInstance().format(summOrder.getQty());
    	builder.append(itemText);
    	builder.append(strTotalQty);
    	builder.append(createHorizontalSpace(itemText.length() + strTotalQty.length() + strTotalRetailPrice.length()));
    	builder.append(strTotalRetailPrice + "\n");
    	
    	// total discount
    	if(summOrder.getPriceDiscount() > 0){
	    	builder.append(discountText);
	    	builder.append(createHorizontalSpace(discountText.length() + strTotalDiscount.length()));
	    	builder.append(strTotalDiscount + "\n");
    	}
    	
    	// transaction exclude vat
    	if(trans.getTransactionVatExclude() > 0){
    		String vatExcludeText = mContext.getString(R.string.tax) + " " +
    				mGlobal.currencyFormat(mShop.getCompanyVatRate(), "#,###.##") + "%";
    		String strVatExclude = mGlobal.currencyFormat(trans.getTransactionVatExclude());
    		builder.append(vatExcludeText);
    		builder.append(createHorizontalSpace(vatExcludeText.length() + strVatExclude.length()));
    		builder.append(strVatExclude + "\n");
    	}
    	
    	// total price
    	builder.append(totalText);
    	builder.append(createHorizontalSpace(totalText.length() + strTotalSale.length()));
    	builder.append(strTotalSale + "\n");

    	// total payment
    	List<Payment.PaymentDetail> paymentLst = 
    			mPayment.listPaymentGroupByType(transactionId);
    	for(int i = 0; i < paymentLst.size(); i++){
    		Payment.PaymentDetail payment = paymentLst.get(i);
	    	String strTotalPaid = mGlobal.currencyFormat(payment.getPaid());
	    	if(payment.getPayTypeID() == PaymentDao.PAY_TYPE_CREDIT){
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
	    		builder.append(paymentText);
	    		builder.append(createHorizontalSpace(paymentText.length()));
	    		builder.append("\n");
    			builder.append(cardNoText);
    			builder.append(createHorizontalSpace(cardNoText.length() + strTotalPaid.length()));
    			builder.append(strTotalPaid);
	    	}else{
	    		String paymentText = payment.getPayTypeName() + " ";
		    	if(i < paymentLst.size() - 1){
			    	builder.append(paymentText);
		    		builder.append(createHorizontalSpace(paymentText.length() + strTotalPaid.length()));
			    	builder.append(strTotalPaid);
		    	}else if(i == paymentLst.size() - 1){
			    	if(change > 0){
				    	builder.append(paymentText);
				    	builder.append(strTotalPaid);
			    		builder.append(createHorizontalSpace(changeText.length() + strTotalChange.length() + paymentText.length() + strTotalPaid.length()));
				    	builder.append(changeText);
				    	builder.append(strTotalChange);
				    }else{
				    	builder.append(paymentText);
			    		builder.append(createHorizontalSpace(paymentText.length() + strTotalPaid.length()));
				    	builder.append(strTotalPaid);
				    }
		    	}
	    	}
    		builder.append("\n");
    	}
	    builder.append(createLine("=") + "\n");
	    
	    if(mShop.getCompanyVatType() == ProductsDao.VAT_TYPE_INCLUDED){
		    // before vat
		    builder.append(beforeVatText);
		    builder.append(createHorizontalSpace(beforeVatText.length() + strBeforeVat.length()));
		    builder.append(strBeforeVat + "\n");
		    
		    // transaction vat
	    	builder.append(vatRateText);
	    	builder.append(createHorizontalSpace(vatRateText.length() + strTransactionVat.length()));
	    	builder.append(strTransactionVat + "\n");
	    }
	    
    	// add footer
    	for(ShopData.HeaderFooterReceipt hf : 
			mHeaderFooter.listHeaderFooter(HeaderFooterReceiptDao.FOOTER_LINE_TYPE)){
    		builder.append("<c>");
			builder.append(hf.getTextInLine());
			builder.append("\n");
		}
    	
    	String[] subElement = builder.toString().split("\n");
    	for(int i=0;i < subElement.length;i++){
    		String data = subElement[i];
			if(data.contains("<c>")){
				data = adjustAlignCenter(data.replace("<c>", ""));
			}
    		printer.PRN_Print(data);
		}
    	printer.PRN_PrintAndFeedLine(6);		
		printer.PRN_HalfCutPaper();	
		printer.PRN_Close();
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
		PrintReceiptLogDao printLog = new PrintReceiptLogDao(mContext.getApplicationContext());
		for(PrintReceiptLogDao.PrintReceipt printReceipt : printLog.listPrintReceiptLog()){
			try {
				if(MPOSApplication.getInternalPrinterSetting(mContext)){
					printReceiptWintec(printReceipt.getTransactionId());
				}else{
					printReceiptEpson(printReceipt.getTransactionId());
				}
				printLog.deletePrintStatus(printReceipt.getPriceReceiptLogId());
				
			} catch (Exception e) {
				printLog.updatePrintStatus(printReceipt.getPriceReceiptLogId(), PrintReceiptLogDao.PRINT_NOT_SUCCESS);
				Logger.appendLog(mContext, 
						MPOSApplication.LOG_DIR, MPOSApplication.LOG_FILE_NAME, e.getMessage());
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
