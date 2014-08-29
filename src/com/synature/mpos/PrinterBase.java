package com.synature.mpos;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;

import android.content.Context;

import com.synature.mpos.database.CreditCard;
import com.synature.mpos.database.Formater;
import com.synature.mpos.database.HeaderFooterReceipt;
import com.synature.mpos.database.MPOSPaymentDetail;
import com.synature.mpos.database.PaymentDetail;
import com.synature.mpos.database.Products;
import com.synature.mpos.database.Reporting;
import com.synature.mpos.database.Session;
import com.synature.mpos.database.Shop;
import com.synature.mpos.database.Staffs;
import com.synature.mpos.database.Transaction;
import com.synature.mpos.database.Reporting.SimpleProductData;
import com.synature.mpos.database.model.Comment;
import com.synature.mpos.database.model.OrderDetail;
import com.synature.mpos.database.model.OrderSet.OrderSetDetail;
import com.synature.mpos.database.model.OrderTransaction;
import com.synature.pos.Report;
import com.synature.pos.Staff;
import com.synature.util.Logger;

public abstract class PrinterBase {
	
	public static final int HORIZONTAL_MAX_SPACE = 45;
	public static final int QTY_MAX_SPACE = 12;
	public static final int MAX_TEXT_LENGTH = 25;
	
	protected Transaction mTrans;
	protected PaymentDetail mPayment;
	protected Shop mShop;
	protected HeaderFooterReceipt mHeaderFooter;
	protected Formater mFormat;
	protected Staffs mStaff;
	protected CreditCard mCreditCard;
	protected Context mContext;
	
	protected StringBuilder mTextToPrint;
	
	public PrinterBase(Context context){
		mContext = context;
		mTrans = new Transaction(context);
		mPayment = new PaymentDetail(context);
		mShop = new Shop(context);
		mFormat = new Formater(context);
		mHeaderFooter = new HeaderFooterReceipt(context);
		mStaff = new Staffs(context);
		mCreditCard = new CreditCard(context);
		mTextToPrint = new StringBuilder();
	}
	 
	protected String createHorizontalSpace(int usedSpace){
		StringBuilder space = new StringBuilder();
		if(usedSpace > HORIZONTAL_MAX_SPACE){
			usedSpace = HORIZONTAL_MAX_SPACE - 2;
		}
		for(int i = usedSpace; i <= HORIZONTAL_MAX_SPACE; i++){
			space.append(" ");
		}
		return space.toString();
	}

	protected String adjustAlignCenter(String text){
		int rimSpace = (HORIZONTAL_MAX_SPACE - calculateLength(text)) / 2;
		StringBuilder empText = new StringBuilder();
		for(int i = 0; i < rimSpace; i++){
			empText.append(" ");
		}
		return empText.toString() + text + empText.toString();
	}
	
	protected String limitTextLength(String text){
		if(text == null)
			return "";
		if(text.length() > MAX_TEXT_LENGTH)
			text = text.substring(0, MAX_TEXT_LENGTH) + "...";
		return text;
	}
	
	protected int calculateLength(String text){
		if(text == null)
			return 0;
		int length = 0;
		for(int i = 0; i < text.length(); i++){
			int code = (int) text.charAt(i);
			if(code != 3633 
					// thai
					&& code != 3636
					&& code != 3637
					&& code != 3638
					&& code != 3639
					&& code != 3640
					&& code != 3641
					&& code != 3642
					&& code != 3655
					&& code != 3656
					&& code != 3657
					&& code != 3658
					&& code != 3659
					&& code != 3660
					&& code != 3661
					&& code != 3662
					// lao 
					&& code != 3761
					&& code != 3764
					&& code != 3765
					&& code != 3766
					&& code != 3767
					&& code != 3768
					&& code != 3769
					&& code != 3771
					&& code != 3772
					&& code != 3784
					&& code != 3785
					&& code != 3786
					&& code != 3787
					&& code != 3788
					&& code != 3789){
				length ++;
			}
		}
		return length == 0 ? text.length() : length;
	}
	
	protected String createQtySpace(int usedSpace){
		StringBuilder space = new StringBuilder();
		if(usedSpace > QTY_MAX_SPACE){
			usedSpace = QTY_MAX_SPACE - 2;
		}
		for(int i = usedSpace; i <= QTY_MAX_SPACE; i++){
			space.append(" ");
		}
		return space.toString();
	}
	
	protected String createLine(String sign){
		StringBuilder line = new StringBuilder();
		for(int i = 0; i <= HORIZONTAL_MAX_SPACE; i++){
			line.append(sign);
		}
		return line.toString();
	}
	
	public String getTextToPrint(){
		return mTextToPrint.toString();
	}
	
	/**
	 * Create text for print sale by bill report
	 * @param dateFrom
	 * @param dateTo
	 */
	protected void createTextForPrintSaleByBillReport(String dateFrom, String dateTo){
		String date = mFormat.dateFormat(dateTo);
		if(!dateFrom.equals(dateTo)){
			date = mFormat.dateFormat(dateFrom) + " - " + 
					mFormat.dateFormat(dateTo);
		}
		// header
		mTextToPrint.append(adjustAlignCenter(mContext.getString(R.string.sale_by_bill_report)) + "\n");
		mTextToPrint.append(date + "\n");
		mTextToPrint.append(mContext.getString(R.string.shop) + " " + mShop.getShopProperty().getShopName() + "\n");
		mTextToPrint.append(mContext.getString(R.string.print_date) + " " + mFormat.dateTimeFormat(Utils.getCalendar().getTime()) + "\n");
		
		String receiptHeader = mContext.getString(R.string.receipt);
		String totalSaleHeader = mContext.getString(R.string.total);
		String closeTimeHeader = mContext.getString(R.string.time) + 
				createQtySpace(calculateLength(totalSaleHeader));
		
		// line
		mTextToPrint.append(createLine("-") + "\n");
		mTextToPrint.append(receiptHeader);
		mTextToPrint.append(createHorizontalSpace(
				calculateLength(receiptHeader) 
				+ calculateLength(closeTimeHeader) 
				+ calculateLength(totalSaleHeader)));
		mTextToPrint.append(closeTimeHeader);
		mTextToPrint.append(totalSaleHeader + "\n");
		mTextToPrint.append(createLine("-") + "\n");
		
		Reporting reporting = new Reporting(mContext, dateFrom, dateTo);
		List<Reporting.SaleTransactionReport> saleReportLst = 
				reporting.listTransactionReport();
		for(Reporting.SaleTransactionReport report : saleReportLst){
			mTextToPrint.append(mFormat.dateFormat(report.getSaleDate()) + "\n");
			for(OrderTransaction trans : report.getTransLst()){
				String receiptNo = trans.getReceiptNo();
				String totalSale = mFormat.currencyFormat(trans.getTransactionVatable());
				String closeTime = mFormat.timeFormat(trans.getCloseTime()) + 
						createQtySpace(calculateLength(totalSale));
				mTextToPrint.append(receiptNo);
				mTextToPrint.append(createHorizontalSpace(calculateLength(receiptNo) + 
						calculateLength(totalSale) 
						+ calculateLength(closeTime)));
				mTextToPrint.append(closeTime);
				mTextToPrint.append(totalSale + "\n");
			}
			mTextToPrint.append("\n");
		}
	}
	
	/**
	 * Create text for print sale by product report
	 * @param dateFrom
	 * @param dateTo
	 */
	protected void createTextForPrintSaleByProductReport(String dateFrom, String dateTo){
		OrderDetail summOrder = mTrans.getSummaryOrder(dateFrom, dateTo);
	
		String date = mFormat.dateFormat(dateTo);
		if(!dateFrom.equals(dateTo)){
			date = mFormat.dateFormat(dateFrom) + " - " + 
					mFormat.dateFormat(dateTo);
		}
		
		// header
		mTextToPrint.append(adjustAlignCenter(mContext.getString(R.string.sale_by_product_report)) + "\n");
		mTextToPrint.append(date + "\n");
		mTextToPrint.append(mContext.getString(R.string.shop) + " " + mShop.getShopProperty().getShopName() + "\n");
		mTextToPrint.append(mContext.getString(R.string.print_date) + " " + mFormat.dateTimeFormat(Utils.getCalendar().getTime()) + "\n\n");
		
		// Product Summary
		Reporting reporting = new Reporting(mContext, dateFrom, dateTo);
		Report reportData = reporting.getProductDataReport();
		for(Report.GroupOfProduct group : reportData.getGroupOfProductLst()){
			mTextToPrint.append(group.getProductGroupName() + ": " + group.getProductDeptName()+ "\n");
			for(Report.ReportDetail detail : group.getReportDetail()){
				String itemName = detail.getProductName();
				if(detail.getProductName() == Reporting.SUMM_DEPT){
					itemName = group.getProductDeptName() + " " +
							mContext.getString(R.string.summary);
					mTextToPrint.append(itemName);
				}else if(detail.getProductName() == Reporting.SUMM_GROUP){
					itemName = group.getProductGroupName() + " " +
							mContext.getString(R.string.summary);
					mTextToPrint.append(itemName);
				}else{
					mTextToPrint.append(itemName);
				}
				String itemTotalPrice = mFormat.currencyFormat(detail.getSubTotal());
				String itemTotalQty = mFormat.qtyFormat(detail.getQty()) + 
						createQtySpace(calculateLength(itemTotalPrice));
				mTextToPrint.append(createHorizontalSpace(calculateLength(itemName) + 
						calculateLength(itemTotalQty) + 
						calculateLength(itemTotalPrice)));
				mTextToPrint.append(itemTotalQty);
				mTextToPrint.append(itemTotalPrice + "\n");
				if(detail.getProductName() == Reporting.SUMM_GROUP){
					mTextToPrint.append(createLine("-"));
				}
			}
			mTextToPrint.append("\n");
		}
		
		String discountText = mContext.getString(R.string.discount);
		String discount = mFormat.currencyFormat(summOrder.getPriceDiscount());
		String subTotalText = mContext.getString(R.string.sub_total) + " ";
		String subTotal = mFormat.currencyFormat(summOrder.getTotalRetailPrice());
		
		mTextToPrint.append(subTotalText);
		mTextToPrint.append(createHorizontalSpace(
				calculateLength(subTotalText) 
				+ calculateLength(subTotal)));
		mTextToPrint.append(subTotal + "\n");
		mTextToPrint.append(discountText);
		mTextToPrint.append(createHorizontalSpace(calculateLength(discountText) 
				+ calculateLength(discount)));
		mTextToPrint.append(discount + "\n");
		
		// Vat Exclude
		if(summOrder.getVatExclude() > 0){
			String vatExcludeText = mContext.getString(R.string.vat_exclude) + " " +
					NumberFormat.getInstance().format(mShop.getCompanyVatRate()) + "%";
			String vatExclude = mFormat.currencyFormat(summOrder.getVatExclude());
			mTextToPrint.append(vatExcludeText);
			mTextToPrint.append(createHorizontalSpace(calculateLength(vatExcludeText) 
					+ calculateLength(vatExclude)));
			mTextToPrint.append(vatExclude + "\n\n");
		}else{
			mTextToPrint.append("\n");
		}
		
		String grandTotalText = mContext.getString(R.string.grand_total);
		String grandTotal = mFormat.currencyFormat(summOrder.getTotalSalePrice());
		mTextToPrint.append(grandTotalText);
		mTextToPrint.append(createHorizontalSpace(calculateLength(grandTotalText) 
				+ calculateLength(grandTotal)));
		mTextToPrint.append(grandTotal + "\n");
	}
	
	/**
	 * Create text for print summary report
	 * @param sessionId
	 * @param staffId
	 */
	protected void createTextForPrintSummaryReport(int sessionId, int staffId){
		Session session = new Session(mContext.getApplicationContext());
		String sessionDate = session.getLastSessionDate();
		
		OrderTransaction trans = null; 
		OrderDetail summOrder = null;

		if(sessionId != 0){
			trans = mTrans.getSummaryTransaction(sessionId, sessionDate);
			summOrder = mTrans.getSummaryOrder(sessionId, sessionDate, sessionDate);
		}else{
			trans = mTrans.getSummaryTransaction(sessionDate);
			summOrder = mTrans.getSummaryOrder(sessionDate, sessionDate);
		}
			
		// header
		String headerName = mContext.getString(R.string.summary_sale_report);
		if(sessionId != 0)
			headerName = mContext.getString(R.string.shift_close_report);
		mTextToPrint.append(adjustAlignCenter(headerName) + "\n\n");
		mTextToPrint.append(mFormat.dateFormat(sessionDate) + "\n");
		mTextToPrint.append(mContext.getString(R.string.shop) + " " + mShop.getShopProperty().getShopName() + "\n");
		if(sessionId != 0){
			com.synature.mpos.database.model.Session sess = session.getSession(sessionId);
			Staffs st = new Staffs(mContext);
			Staff std = st.getStaff(sess.getOpenStaff());
			mTextToPrint.append(mContext.getString(R.string.open_by) + " " + std.getStaffName() + " " + mFormat.timeFormat(sess.getOpenDate()) + "\n");
			std = st.getStaff(sess.getCloseStaff());
			String closeBy = std != null ? std.getStaffName() : "";
			String closeTime = sess.getCloseDate() != null ? mFormat.timeFormat(sess.getCloseDate()) : "";
			mTextToPrint.append(mContext.getString(R.string.close_by) + " " + closeBy + " " + closeTime + "\n");
		}
		mTextToPrint.append(mContext.getString(R.string.print_by) + " " + mStaff.getStaff(staffId).getStaffName() + "\n");
		mTextToPrint.append(mContext.getString(R.string.print_date) + " " + mFormat.dateTimeFormat(Utils.getCalendar().getTime()) + "\n");
		
		// ReceiptNo.
		mTextToPrint.append(mContext.getString(R.string.receipt_no) + "\n");
		mTextToPrint.append(mTrans.getMinReceiptNo(sessionId, sessionDate) + " -\n");
		mTextToPrint.append(mTrans.getMaxReceiptNo(sessionId, sessionDate) + "\n\n");
		
		// Product Summary
		Reporting report = new Reporting(mContext, sessionDate, sessionDate);
		List<SimpleProductData> simpleLst = report.listSummaryProductGroupInDay(sessionId);
		if(simpleLst != null){
			for(SimpleProductData sp : simpleLst){
				String groupName = sp.getDeptName();
				String groupTotalPrice = mFormat.currencyFormat(sp.getDeptTotalPrice());
				String groupTotalQty = mFormat.qtyFormat(sp.getDeptTotalQty()) + 
						createQtySpace(calculateLength(groupTotalPrice));
				mTextToPrint.append(groupName);
				mTextToPrint.append(createHorizontalSpace(
						calculateLength(groupName) 
						+ calculateLength(groupTotalQty) 
						+ calculateLength(groupTotalPrice)));
				mTextToPrint.append(groupTotalQty);
				mTextToPrint.append(groupTotalPrice + "\n");
				if(sp.getItemLst() != null){
					for(SimpleProductData.Item item : sp.getItemLst()){
						String itemName = limitTextLength("-" + item.getItemName());
						String itemTotalPrice = mFormat.currencyFormat(item.getTotalPrice());
						String itemTotalQty = mFormat.qtyFormat(item.getTotalQty()) + 
								createQtySpace(calculateLength(itemTotalPrice));
						mTextToPrint.append(itemName);
						mTextToPrint.append(createHorizontalSpace(
								calculateLength(itemName) 
								+ calculateLength(itemTotalQty) 
								+ calculateLength(itemTotalPrice)));
						mTextToPrint.append(itemTotalQty);
						mTextToPrint.append(itemTotalPrice + "\n");
					}
				}
			}
			// Sub Total
			mTextToPrint.append("\n");
			String subTotalText = mContext.getString(R.string.sub_total);
			String subTotalPrice = mFormat.currencyFormat(summOrder.getTotalRetailPrice());
			String subTotalQty = mFormat.qtyFormat(summOrder.getOrderQty()) + 
					createQtySpace(calculateLength(subTotalPrice));
			mTextToPrint.append(subTotalText);
			mTextToPrint.append(createHorizontalSpace(
					calculateLength(subTotalText) 
					+ calculateLength(subTotalQty) 
					+ calculateLength(subTotalPrice)));
			mTextToPrint.append(subTotalQty);
			mTextToPrint.append(subTotalPrice + "\n\n");
		}
		
		String discountText = mContext.getString(R.string.discount);
		String discount = mFormat.currencyFormat(summOrder.getPriceDiscount());
//		String subTotalText = mContext.getString(R.string.sub_total) + " ";
//		String subTotal = mFormat.currencyFormat(summOrder.getTotalSalePrice());
		
		mTextToPrint.append(discountText);
		mTextToPrint.append(createHorizontalSpace(
				calculateLength(discountText) 
				+ calculateLength(discount)));
		mTextToPrint.append(discount + "\n");
//		mTextToPrint.append(subTotalText);
//		mTextToPrint.append(createHorizontalSpace(
//				calculateLength(subTotalText) 
//				+ calculateLength(subTotal)));
//		mTextToPrint.append(subTotal + "\n");
		
		// Vat Exclude
		if(summOrder.getVatExclude() > 0){
			String vatExcludeText = mContext.getString(R.string.vat_exclude) + " " +
					NumberFormat.getInstance().format(mShop.getCompanyVatRate()) + "%";
			String vatExclude = mFormat.currencyFormat(summOrder.getVatExclude());
			mTextToPrint.append(vatExcludeText);
			mTextToPrint.append(createHorizontalSpace(
					calculateLength(vatExcludeText) 
					+ calculateLength(vatExclude)));
			mTextToPrint.append(vatExclude + "\n\n");
		}
		
		String totalSaleText = mContext.getString(R.string.total_sale);
		String totalSale = mFormat.currencyFormat(summOrder.getTotalSalePrice());
		mTextToPrint.append(totalSaleText);
		mTextToPrint.append(createHorizontalSpace(
				calculateLength(totalSaleText) 
				+ calculateLength(totalSale)));
		mTextToPrint.append(totalSale + "\n");
		
		if(mShop.getCompanyVatType() == Products.VAT_TYPE_INCLUDED){
			String beforeVatText = mContext.getString(R.string.before_vat);
			String beforeVat = mFormat.currencyFormat(trans.getTransactionVatable() - trans.getTransactionVat());
			String totalVatText = mContext.getString(R.string.total_vat) + " " +
					NumberFormat.getInstance().format(mShop.getCompanyVatRate()) + "%";
			String totalVat = mFormat.currencyFormat(trans.getTransactionVat());
			mTextToPrint.append(beforeVatText);
			mTextToPrint.append(createHorizontalSpace(
					calculateLength(beforeVatText) 
					+ calculateLength(beforeVat)));
			mTextToPrint.append(beforeVat + "\n");
			mTextToPrint.append(totalVatText);
			mTextToPrint.append(createHorizontalSpace(
					calculateLength(totalVatText) 
					+ calculateLength(totalVat)));
			mTextToPrint.append(totalVat + "\n\n");
		}
		
		String seperateTransIds = mTrans.getSeperateTransactionId(sessionId, sessionDate);
		if(sessionId != 0){
			// open/close shift
			String floatInText = mContext.getString(R.string.float_in);
			String totalCashText = mContext.getString(R.string.total_cash);
			String cashInDrawerText = mContext.getString(R.string.cash_in_drawer);
			String cashCountText = mContext.getString(R.string.cash_count);
			String overShotText = mContext.getString(R.string.over_or_short);
			//double totalBillPayByCashAmount = mPayment.getTotalTransPayByCash(session.getSessionDate());
			double floatInAmount = session.getOpenAmount(sessionId);
			double totalCashAmount = mPayment.getTotalCash(seperateTransIds);
			double cashInDrawerAmount = floatInAmount + totalCashAmount;
			double cashCountAmount = session.getCloseAmount(sessionId);
			String floatIn = mFormat.currencyFormat(floatInAmount);
			String cashInDrawer = mFormat.currencyFormat(cashInDrawerAmount);
			String totalCash = mFormat.currencyFormat(totalCashAmount);
			String cashCount = mFormat.currencyFormat(cashCountAmount);
			String overShot = mFormat.currencyFormat(cashCountAmount - cashInDrawerAmount);
	//			String totalBillPayByCash = mFormat.qtyFormat(totalBillPayByCashAmount)
	//					+ createQtySpace(calculateLength(totalCash));
	
	//			mTextToPrint.append(totalCashText);
	//			mTextToPrint.append(createHorizontalSpace(calculateLength(totalCashText)
	//					+ calculateLength(totalBillPayByCash)
	//					+ calculateLength(totalCash)));
	//			mTextToPrint.append(totalBillPayByCash);
	//			mTextToPrint.append(totalCash + "\n");
			mTextToPrint.append(floatInText);
			mTextToPrint.append(createHorizontalSpace(calculateLength(floatInText) + 
					calculateLength(floatIn)));
			mTextToPrint.append(floatIn + "\n");
			mTextToPrint.append(totalCashText);
			mTextToPrint.append(createHorizontalSpace(calculateLength(totalCashText) + 
					calculateLength(totalCash)));
			mTextToPrint.append(totalCash + "\n");
			mTextToPrint.append(cashInDrawerText);
			mTextToPrint.append(createHorizontalSpace(calculateLength(cashInDrawerText) + 
					calculateLength(cashInDrawer)));
			mTextToPrint.append(cashInDrawer + "\n");
			mTextToPrint.append(cashCountText);
			mTextToPrint.append(createHorizontalSpace(calculateLength(cashCountText) + 
					calculateLength(cashCount)));
			mTextToPrint.append(cashCount + "\n");
			mTextToPrint.append(overShotText);
			mTextToPrint.append(createHorizontalSpace(calculateLength(overShotText) + 
					calculateLength(overShot)));
			mTextToPrint.append(overShot + "\n\n");
		}
		
		List<MPOSPaymentDetail> summaryPaymentLst = 
				mPayment.listSummaryPayment(seperateTransIds);
		if(summaryPaymentLst != null){
			mTextToPrint.append(mContext.getString(R.string.payment_detail) + "\n");
			for(MPOSPaymentDetail payment : summaryPaymentLst){
				String payTypeName = payment.getPayTypeName();
				String payAmount = mFormat.currencyFormat(payment.getPayAmount());
				mTextToPrint.append(payTypeName);
				mTextToPrint.append(createHorizontalSpace(
						calculateLength(payTypeName) 
						+ calculateLength(payAmount)));
				mTextToPrint.append(payAmount + "\n");
			}
			mTextToPrint.append("\n");
		}
		String totalReceiptInDay = mContext.getString(R.string.total_receipt);
		String totalReceipt = String.valueOf(mTrans.getTotalReceipt(sessionId, sessionDate));
		mTextToPrint.append(totalReceiptInDay);
		mTextToPrint.append(createHorizontalSpace(
				calculateLength(totalReceiptInDay) 
				+ calculateLength(totalReceipt)));
		mTextToPrint.append(totalReceipt + "\n\n");
		
		OrderDetail summVoidOrder = mTrans.getSummaryVoidOrderInDay(sessionId, sessionDate);
		mTextToPrint.append(mContext.getString(R.string.void_bill) + "\n");
		String voidBill = mContext.getString(R.string.void_bill_after_paid);
		String totalVoidPrice = mFormat.currencyFormat(summVoidOrder.getTotalSalePrice());
		String totalVoidQty = mFormat.qtyFormat(summVoidOrder.getOrderQty()) +
				createQtySpace(calculateLength(totalVoidPrice));
		mTextToPrint.append(voidBill);
		mTextToPrint.append(createHorizontalSpace(
				calculateLength(voidBill) 
				+ calculateLength(totalVoidQty) 
				+ calculateLength(totalVoidPrice)));
		mTextToPrint.append(totalVoidQty);
		mTextToPrint.append(totalVoidPrice);	
	}
	
	/**
	 * Create text for print receipt
	 * @param transId
	 * @param isCopy
	 */
	protected void createTextForPrintReceipt(int transId, boolean isCopy){
		OrderTransaction trans = mTrans.getTransaction(transId);
		OrderDetail summOrder = mTrans.getSummaryOrder(transId);
		double beforVat = trans.getTransactionVatable() - trans.getTransactionVat();
		double change = mPayment.getTotalPayAmount(transId) - (summOrder.getTotalSalePrice());
		boolean isVoid = trans.getTransactionStatusId() == Transaction.TRANS_STATUS_VOID;
		
		// have copy
		if(isCopy){
			String copyText = mContext.getString(R.string.copy);
			mTextToPrint.append(createLine("-") + "\n");
			mTextToPrint.append(adjustAlignCenter(copyText) + "\n");
			mTextToPrint.append(createLine("-") + "\n\n");
		}
		// add void header
		if(isVoid){
			mTextToPrint.append(mContext.getString(R.string.void_bill) + "\n");
			Calendar voidTime = Calendar.getInstance();
			voidTime.setTimeInMillis(Long.parseLong(trans.getVoidTime()));
			mTextToPrint.append(mContext.getString(R.string.void_time) + " " + mFormat.dateTimeFormat(voidTime.getTime()) + "\n");
			mTextToPrint.append(mContext.getString(R.string.void_by) + " " + mStaff.getStaff(trans.getVoidStaffId()).getStaffName() + "\n");
			mTextToPrint.append(mContext.getString(R.string.reason) + " " + trans.getVoidReason() + "\n\n");
		}
		// add header
		for(com.synature.pos.HeaderFooterReceipt hf : 
			mHeaderFooter.listHeaderFooter(HeaderFooterReceipt.HEADER_LINE_TYPE)){
			mTextToPrint.append(adjustAlignCenter(hf.getTextInLine()) + "\n");
		}
		
		String saleDate = mContext.getString(R.string.date) + " " +
				mFormat.dateTimeFormat(Utils.getCalendar().getTime());
		String receiptNo = mContext.getString(R.string.receipt_no) + " " +
				trans.getReceiptNo();
		String cashCheer = mContext.getString(R.string.cashier) + " " +
				mStaff.getStaff(trans.getOpenStaffId()).getStaffName();
		mTextToPrint.append(saleDate + createHorizontalSpace(calculateLength(saleDate)) + "\n");
		mTextToPrint.append(receiptNo + createHorizontalSpace(calculateLength(receiptNo)) + "\n");
		mTextToPrint.append(cashCheer + createHorizontalSpace(calculateLength(cashCheer)) + "\n");
		mTextToPrint.append(createLine("=") + "\n");
		
		List<OrderDetail> orderLst = mTrans.listAllOrderGroupByProduct(transId);
    	for(int i = 0; i < orderLst.size(); i++){
    		OrderDetail order = orderLst.get(i);
    		String productName = order.getProductName();
    		String productQty = mFormat.qtyFormat(order.getOrderQty()) + "x ";
    		String productPrice = mFormat.currencyFormat(order.getProductPrice());
    		mTextToPrint.append(productQty);
    		mTextToPrint.append(productName);
    		mTextToPrint.append(createHorizontalSpace(
    				calculateLength(productQty) + 
    				calculateLength(productName) + 
    				calculateLength(productPrice)));
    		mTextToPrint.append(productPrice);
    		mTextToPrint.append("\n");
    		if(order.getOrderCommentLst().size() > 0){
    			for(Comment comm : order.getOrderCommentLst()){
    				if(comm.getCommentPrice() > 0){
	    				String commName = comm.getCommentName();
	    				String commQty = "   " + mFormat.qtyFormat(comm.getCommentQty()) + "x ";
	    				String commPrice = mFormat.currencyFormat(comm.getCommentPrice());
	    				mTextToPrint.append(commQty);
	    				mTextToPrint.append(commName);
	    				mTextToPrint.append(createHorizontalSpace(
	    						calculateLength(commQty) +
	    						calculateLength(commName) + 
	    						calculateLength(commPrice)));
	    				mTextToPrint.append(commPrice);
	    				mTextToPrint.append("\n");
    				}
    			}
    		}
    		if(order.getOrdSetDetailLst().size() > 0){
    			for(OrderSetDetail setDetail : order.getOrdSetDetailLst()){
    				String setName = setDetail.getProductName();
    				String setQty = "   " + mFormat.qtyFormat(setDetail.getOrderSetQty()) + "x ";
    				String setPrice = mFormat.currencyFormat(setDetail.getProductPrice());
    				mTextToPrint.append(setQty);
    				mTextToPrint.append(setName);
    				mTextToPrint.append(createHorizontalSpace(
    						calculateLength(setQty) + 
    						calculateLength(setName) +
    						calculateLength(setPrice)));
    				mTextToPrint.append(setPrice);
    				mTextToPrint.append("\n");
    			}
    		}
    	}
    	mTextToPrint.append(createLine("-") + "\n");
    	
    	String itemText = mContext.getString(R.string.items) + ": ";
    	String totalText = mContext.getString(R.string.total) + "...............";
    	String changeText = mContext.getString(R.string.change) + " ";
    	String beforeVatText = mContext.getString(R.string.before_vat);
    	String discountText = summOrder.getPromotionName().equals("") ? mContext.getString(R.string.discount) : summOrder.getPromotionName();
    	String vatRateText = mContext.getString(R.string.vat) + " " +
    			NumberFormat.getInstance().format(mShop.getCompanyVatRate()) + "%";
    	
    	String strTotalRetailPrice = mFormat.currencyFormat(summOrder.getTotalRetailPrice());
    	String strTotalSale = mFormat.currencyFormat(summOrder.getTotalSalePrice());
    	String strTotalDiscount = "-" + mFormat.currencyFormat(summOrder.getPriceDiscount());
    	String strTotalChange = mFormat.currencyFormat(change);
    	String strBeforeVat = mFormat.currencyFormat(beforVat);
    	String strTransactionVat = mFormat.currencyFormat(trans.getTransactionVat());
    	
    	// total item
    	String strTotalQty = NumberFormat.getInstance().format(summOrder.getOrderQty());
    	mTextToPrint.append(itemText);
    	mTextToPrint.append(strTotalQty);
    	mTextToPrint.append(createHorizontalSpace(
    			calculateLength(itemText) + 
    			calculateLength(strTotalQty) + 
    			calculateLength(strTotalRetailPrice)));
    	mTextToPrint.append(strTotalRetailPrice + "\n");
    	
    	// total discount
    	if(summOrder.getPriceDiscount() > 0){
	    	mTextToPrint.append(discountText);
	    	mTextToPrint.append(createHorizontalSpace(
	    			calculateLength(discountText) + 
	    			calculateLength(strTotalDiscount)));
	    	mTextToPrint.append(strTotalDiscount + "\n");
    	}
    	
    	// transaction exclude vat
    	if(trans.getTransactionVatExclude() > 0){
    		String vatExcludeText = mContext.getString(R.string.vat) + " " +
    				NumberFormat.getInstance().format(mShop.getCompanyVatRate()) + "%";
    		String strVatExclude = mFormat.currencyFormat(trans.getTransactionVatExclude());
    		mTextToPrint.append(vatExcludeText);
    		mTextToPrint.append(createHorizontalSpace(
    				calculateLength(vatExcludeText) + 
    				calculateLength(strVatExclude)));
    		mTextToPrint.append(strVatExclude + "\n");
    	}
    	
    	// total price
    	mTextToPrint.append(totalText);
    	mTextToPrint.append(createHorizontalSpace(
    			calculateLength(totalText) + 
    			calculateLength(strTotalSale)));
    	mTextToPrint.append(strTotalSale + "\n");

    	// total payment
    	List<MPOSPaymentDetail> paymentLst = 
    			mPayment.listPaymentGroupByType(transId);
    	for(int i = 0; i < paymentLst.size(); i++){
    		MPOSPaymentDetail payment = paymentLst.get(i);
	    	String strTotalPaid = mFormat.currencyFormat(payment.getTotalPay());
	    	if(payment.getPayTypeId() == PaymentDetail.PAY_TYPE_CREDIT){
	    		String paymentText = payment.getPayTypeName();
	    		String cardNoText = "xxxx xxxx xxxx ";
	    		try {
	    			paymentText = payment.getPayTypeName() + ":" + 
    					mCreditCard.getCreditCardType(payment.getCreditCardTypeId());
	    			cardNoText += payment.getCreditCardNo().substring(12, 16);
	    		} catch (Exception e) {
	    			Logger.appendLog(mContext, Utils.LOG_PATH, 
	    					Utils.LOG_FILE_NAME, "Error gen creditcard no : " + e.getMessage());
	    		}
	    		mTextToPrint.append(paymentText);
	    		mTextToPrint.append(createHorizontalSpace(calculateLength(paymentText)));
	    		mTextToPrint.append("\n");
    			mTextToPrint.append(cardNoText);
    			mTextToPrint.append(createHorizontalSpace(
    					calculateLength(cardNoText) + 
    					calculateLength(strTotalPaid)));
    			mTextToPrint.append(strTotalPaid);
	    	}else{
	    		String paymentText = payment.getPayTypeName() + " ";
		    	if(i < paymentLst.size() - 1){
			    	mTextToPrint.append(paymentText);
		    		mTextToPrint.append(createHorizontalSpace(
		    				calculateLength(paymentText) + 
		    				calculateLength(strTotalPaid)));
			    	mTextToPrint.append(strTotalPaid);
		    	}else if(i == paymentLst.size() - 1){
			    	if(change > 0){
				    	mTextToPrint.append(paymentText);
				    	mTextToPrint.append(strTotalPaid);
			    		mTextToPrint.append(createHorizontalSpace(
			    				calculateLength(changeText) + 
			    				calculateLength(strTotalChange) + 
			    				calculateLength(paymentText) + 
			    				calculateLength(strTotalPaid)));
				    	mTextToPrint.append(changeText);
				    	mTextToPrint.append(strTotalChange);
				    }else{
				    	mTextToPrint.append(paymentText);
			    		mTextToPrint.append(createHorizontalSpace(
			    				calculateLength(paymentText) + 
			    				calculateLength(strTotalPaid)));
				    	mTextToPrint.append(strTotalPaid);
				    }
		    	}
	    	}
    		mTextToPrint.append("\n");
    	}
	    mTextToPrint.append(createLine("=") + "\n");
	    
	    if(mShop.getCompanyVatType() == Products.VAT_TYPE_INCLUDED){
		    // before vat
		    mTextToPrint.append(beforeVatText);
		    mTextToPrint.append(createHorizontalSpace(
		    		calculateLength(beforeVatText) + 
		    		calculateLength(strBeforeVat)));
		    mTextToPrint.append(strBeforeVat + "\n");
		    
		    // transaction vat
	    	mTextToPrint.append(vatRateText);
	    	mTextToPrint.append(createHorizontalSpace(
	    			calculateLength(vatRateText) + 
	    			calculateLength(strTransactionVat)));
	    	mTextToPrint.append(strTransactionVat + "\n");
	    }
	    
    	// add footer
    	for(com.synature.pos.HeaderFooterReceipt hf : 
			mHeaderFooter.listHeaderFooter(HeaderFooterReceipt.FOOTER_LINE_TYPE)){
			mTextToPrint.append(adjustAlignCenter(hf.getTextInLine()) + "\n");
		}
    	
    	if(!isCopy && !isVoid){
	    	// set e-journal to transaction
	    	mTrans.updateTransactionEjournal(transId, mTextToPrint.toString());
    	}
    	if(isVoid){
    		mTrans.updateTransactionVoidEjournal(transId, mTextToPrint.toString());
    	}
	}
}
