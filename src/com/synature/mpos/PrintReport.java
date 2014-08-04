package com.synature.mpos;

import java.text.NumberFormat;
import java.util.List;

import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.synature.mpos.database.Formater;
import com.synature.mpos.database.MPOSOrderTransaction;
import com.synature.mpos.database.PaymentDetail;
import com.synature.mpos.database.Products;
import com.synature.mpos.database.Reporting;
import com.synature.mpos.database.Session;
import com.synature.mpos.database.Shop;
import com.synature.mpos.database.Staffs;
import com.synature.mpos.database.Transaction;
import com.synature.mpos.database.Reporting.SimpleProductData;
import com.synature.pos.Payment;
import com.synature.pos.Report;

import android.content.Context;
import android.database.SQLException;

public class PrintReport implements Runnable{

	public static enum WhatPrint{
		SUMMARY_SALE,
		PRODUCT_REPORT,
		BILL_REPORT
	};
	
	private Context mContext;
	private Transaction mTrans;
	private Session mSession;
	private PaymentDetail mPayment;
	private Shop mShop;
	private Staffs mStaff;
	private Formater mFormat;
	private WhatPrint mWhatPrint;
	private String mDateFrom;
	private String mDateTo;
	
	private int mSessionId;
	private int mStaffId;
	
	public PrintReport(Context context, String dateFrom, String dateTo, 
			int staffId, WhatPrint whatPrint){
		mContext = context;
		mTrans = new Transaction(context);
		mSession = new Session(context);
		mPayment = new PaymentDetail(context);
		mShop = new Shop(context);
		mStaff = new Staffs(context);
		mFormat = new Formater(context);
		mWhatPrint = whatPrint;
		
		mDateFrom = dateFrom;
		mDateTo = dateTo;
		mStaffId = staffId;
	}
	
//	public PrintReport(Context context, String dateFrom, String dateTo, WhatPrint whatPrint){
//		this(context, dateFrom, dateTo, 0, whatPrint);
//	}
	
//	public PrintReport(Context context, int staffId, WhatPrint whatPrint){
//		this(context, "", "", staffId, whatPrint);
//	}
	
	public PrintReport(Context context, int sessionId, 
			int staffId, WhatPrint whatPrint){
		this(context, "", "", staffId, whatPrint);
		mSessionId = sessionId;
	}
	
	protected class EPSONPrintBillReport extends EPSONPrinter{

		public EPSONPrintBillReport(Context context) {
			super(context);
		}

		@Override
		public void onBatteryStatusChangeEvent(String arg0, int arg1) {
		}

		@Override
		public void onStatusChangeEvent(String arg0, int arg1) {
		}

		@Override
		public void prepareDataToPrint(int transactionId) {
		}

		@Override
		public void prepareDataToPrint() {
			String date = mFormat.dateFormat(mDateTo);
			if(!mDateFrom.equals(mDateTo)){
				date = mFormat.dateFormat(mDateFrom) + " - " + 
						mFormat.dateFormat(mDateTo);
			}
			try {
				mBuilder.addTextAlign(Builder.ALIGN_CENTER);
				// header
				mBuilder.addText(mContext.getString(R.string.sale_by_bill_report) + "\n");
				mBuilder.addText(date + "\n");
				mBuilder.addText(mContext.getString(R.string.shop) + " " + mShop.getShopProperty().getShopName() + "\n");
				mBuilder.addText(mContext.getString(R.string.print_date) + " " + mFormat.dateTimeFormat(Utils.getCalendar().getTime()) + "\n");

				String receiptHeader = mContext.getString(R.string.receipt);
				String totalSaleHeader = mContext.getString(R.string.total);
				String closeTimeHeader = mContext.getString(R.string.time) + 
						createQtySpace(calculateLength(totalSaleHeader));
				
				// line
				mBuilder.addText(createLine("-") + "\n");
				mBuilder.addText(receiptHeader);
				mBuilder.addText(createHorizontalSpace(
						calculateLength(receiptHeader) 
						+ calculateLength(closeTimeHeader) 
						+ calculateLength(totalSaleHeader)));
				mBuilder.addText(closeTimeHeader);
				mBuilder.addText(totalSaleHeader + "\n");
				mBuilder.addText(createLine("-") + "\n");
				
				Reporting reporting = new Reporting(mContext, mDateFrom, mDateTo);
				List<Reporting.SaleTransactionReport> saleReportLst = 
						reporting.listTransactionReport();
				for(Reporting.SaleTransactionReport report : saleReportLst){
					String saleDate = mFormat.dateFormat(report.getSaleDate());
					mBuilder.addText(saleDate);
					mBuilder.addText(createHorizontalSpace(calculateLength(saleDate)) + "\n");
					for(MPOSOrderTransaction trans : report.getTransLst()){
						String receiptNo = trans.getReceiptNo();
						String totalSale = mFormat.currencyFormat(trans.getTransactionVatable());
						String closeTime = mFormat.timeFormat(trans.getCloseTime()) + 
								createQtySpace(calculateLength(totalSale));
						mBuilder.addText(receiptNo);
						mBuilder.addText(createHorizontalSpace(calculateLength(receiptNo) + 
								calculateLength(totalSale) 
								+ calculateLength(closeTime)));
						mBuilder.addText(closeTime);
						mBuilder.addText(totalSale + "\n");
					}
					mBuilder.addText("\n");
				}
			} catch (EposException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	protected class WintecPrintBillReport extends WintecPrinter{

		public WintecPrintBillReport(Context context) {
			super(context);
		}

		@Override
		public void prepareDataToPrint() {
			String date = mFormat.dateFormat(mDateTo);
			if(!mDateFrom.equals(mDateTo)){
				date = mFormat.dateFormat(mDateFrom) + " - " + 
						mFormat.dateFormat(mDateTo);
			}
			// header
			mBuilder.append("<c>" + mContext.getString(R.string.sale_by_bill_report) + "\n");
			mBuilder.append("<c>" + date + "\n");
			mBuilder.append("<c>" + mContext.getString(R.string.shop) + " " + mShop.getShopProperty().getShopName() + "\n");
			mBuilder.append(mContext.getString(R.string.print_date) + " " + mFormat.dateTimeFormat(Utils.getCalendar().getTime()) + "\n");
			
			String receiptHeader = mContext.getString(R.string.receipt);
			String totalSaleHeader = mContext.getString(R.string.total);
			String closeTimeHeader = mContext.getString(R.string.time) + 
					createQtySpace(calculateLength(totalSaleHeader));
			
			// line
			mBuilder.append(createLine("-") + "\n");
			mBuilder.append(receiptHeader);
			mBuilder.append(createHorizontalSpace(
					calculateLength(receiptHeader) 
					+ calculateLength(closeTimeHeader) 
					+ calculateLength(totalSaleHeader)));
			mBuilder.append(closeTimeHeader);
			mBuilder.append(totalSaleHeader + "\n");
			mBuilder.append(createLine("-") + "\n");
			
			Reporting reporting = new Reporting(mContext, mDateFrom, mDateTo);
			List<Reporting.SaleTransactionReport> saleReportLst = 
					reporting.listTransactionReport();
			for(Reporting.SaleTransactionReport report : saleReportLst){
				mBuilder.append(mFormat.dateFormat(report.getSaleDate()) + "\n");
				for(MPOSOrderTransaction trans : report.getTransLst()){
					String receiptNo = trans.getReceiptNo();
					String totalSale = mFormat.currencyFormat(trans.getTransactionVatable());
					String closeTime = mFormat.timeFormat(trans.getCloseTime()) + 
							createQtySpace(calculateLength(totalSale));
					mBuilder.append(receiptNo);
					mBuilder.append(createHorizontalSpace(calculateLength(receiptNo) + 
							calculateLength(totalSale) 
							+ calculateLength(closeTime)));
					mBuilder.append(closeTime);
					mBuilder.append(totalSale + "\n");
				}
				mBuilder.append("\n");
			}
		}
	}
	
	protected class EPSONPrintSaleByProduct extends EPSONPrinter{

		public EPSONPrintSaleByProduct(Context context) {
			super(context);
		}

		@Override
		public void onBatteryStatusChangeEvent(String arg0, int arg1) {
		}

		@Override
		public void onStatusChangeEvent(String arg0, int arg1) {
		}

		@Override
		public void prepareDataToPrint(int transactionId) {
		}

		@Override
		public void prepareDataToPrint() {
			MPOSOrderTransaction.MPOSOrderDetail summOrder 
			= mTrans.getSummaryOrderInDay(mDateFrom, mDateTo);
		
			String date = mFormat.dateFormat(mDateTo);
			if(!mDateFrom.equals(mDateTo)){
				date = mFormat.dateFormat(mDateFrom) + " - " + 
						mFormat.dateFormat(mDateTo);
			}
			
			try {
				mBuilder.addTextAlign(Builder.ALIGN_CENTER);
				// header
				mBuilder.addText(mContext.getString(R.string.sale_by_product_report) + "\n");
				mBuilder.addText( date + "\n");
				mBuilder.addText(mContext.getString(R.string.shop) + " " + mShop.getShopProperty().getShopName() + "\n");
				mBuilder.addText(mContext.getString(R.string.print_date) + " " + mFormat.dateTimeFormat(Utils.getCalendar().getTime()) + "\n\n");

				// Product Summary
				Reporting reporting = new Reporting(mContext, mDateFrom, mDateTo);
				Report reportData = reporting.getProductDataReport();
				for(Report.GroupOfProduct group : reportData.getGroupOfProductLst()){
					String groupDept = group.getProductGroupName() + ": " + group.getProductDeptName();
					mBuilder.addText(groupDept);
					mBuilder.addText(createHorizontalSpace(calculateLength(groupDept)) + "\n");
					for(Report.ReportDetail detail : group.getReportDetail()){
						String itemName = detail.getProductName();
						if(detail.getProductName() == Reporting.SUMM_DEPT){
							itemName = group.getProductDeptName() + " " +
									mContext.getString(R.string.summary);
							mBuilder.addText(itemName);
						}else if(detail.getProductName() == Reporting.SUMM_GROUP){
							itemName = group.getProductGroupName() + " " +
									mContext.getString(R.string.summary);
							mBuilder.addText(itemName);
						}else{
							mBuilder.addText(itemName);
						}
						String itemTotalPrice = mFormat.currencyFormat(detail.getSubTotal());
						String itemTotalQty = mFormat.qtyFormat(detail.getQty()) + 
								createQtySpace(calculateLength(itemTotalPrice));
						mBuilder.addText(createHorizontalSpace(calculateLength(itemName) + 
								calculateLength(itemTotalQty) 
								+ calculateLength(itemTotalPrice)));
						mBuilder.addText(itemTotalQty);
						mBuilder.addText(itemTotalPrice + "\n");
						if(detail.getProductName() == Reporting.SUMM_GROUP){
							mBuilder.addText(createLine("-") + "\n");
						}
					}
					mBuilder.addText("\n");
				}
				
				String discountText = mContext.getString(R.string.discount);
				String discount = mFormat.currencyFormat(summOrder.getPriceDiscount());
				String subTotalText = mContext.getString(R.string.sub_total) + " ";
				String subTotal = mFormat.currencyFormat(summOrder.getTotalRetailPrice());
				
				mBuilder.addText(subTotalText);
				mBuilder.addText(createHorizontalSpace(calculateLength(subTotalText) 
						+ calculateLength(subTotal)));
				mBuilder.addText(subTotal + "\n");
				mBuilder.addText(discountText);
				mBuilder.addText(createHorizontalSpace(calculateLength(discountText) 
						+ calculateLength(discount)));
				mBuilder.addText(discount + "\n");
				
				// Vat Exclude
				if(summOrder.getVatExclude() > 0){
					String vatExcludeText = mContext.getString(R.string.vat_exclude) + " " +
							NumberFormat.getInstance().format(mShop.getCompanyVatRate()) + "%";
					String vatExclude = mFormat.currencyFormat(summOrder.getVatExclude());
					mBuilder.addText(vatExcludeText);
					mBuilder.addText(createHorizontalSpace(
							calculateLength(vatExcludeText) 
							+ calculateLength(vatExclude)));
					mBuilder.addText(vatExclude + "\n\n");
				}else{
					mBuilder.addText("\n");
				}
				
				String grandTotalText = mContext.getString(R.string.grand_total);
				String grandTotal = mFormat.currencyFormat(summOrder.getTotalSalePrice());
				mBuilder.addText(grandTotalText);
				mBuilder.addText(createHorizontalSpace(
						calculateLength(grandTotalText) 
						+ calculateLength(grandTotal)));
				mBuilder.addText(grandTotal + "\n");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EposException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	protected class WintecPrintSaleByProduct extends WintecPrinter{

		public WintecPrintSaleByProduct(Context context) {
			super(context);
		}

		@Override
		public void prepareDataToPrint() {
			MPOSOrderTransaction.MPOSOrderDetail summOrder 
				= mTrans.getSummaryOrderInDay(mDateFrom, mDateTo);
			
			String date = mFormat.dateFormat(mDateTo);
			if(!mDateFrom.equals(mDateTo)){
				date = mFormat.dateFormat(mDateFrom) + " - " + 
						mFormat.dateFormat(mDateTo);
			}
			
			// header
			mBuilder.append("<c>" + mContext.getString(R.string.sale_by_product_report) + "\n");
			mBuilder.append("<c>" + date + "\n");
			mBuilder.append("<c>" + mContext.getString(R.string.shop) + " " + mShop.getShopProperty().getShopName() + "\n");
			mBuilder.append(mContext.getString(R.string.print_date) + " " + mFormat.dateTimeFormat(Utils.getCalendar().getTime()) + "\n\n");
			
			// Product Summary
			Reporting reporting = new Reporting(mContext, mDateFrom, mDateTo);
			Report reportData = reporting.getProductDataReport();
			for(Report.GroupOfProduct group : reportData.getGroupOfProductLst()){
				mBuilder.append("<b>" + group.getProductGroupName() + ": " + group.getProductDeptName()+ "\n");
				for(Report.ReportDetail detail : group.getReportDetail()){
					String itemName = detail.getProductName();
					if(detail.getProductName() == Reporting.SUMM_DEPT){
						itemName = group.getProductDeptName() + " " +
								mContext.getString(R.string.summary);
						mBuilder.append(itemName);
					}else if(detail.getProductName() == Reporting.SUMM_GROUP){
						itemName = group.getProductGroupName() + " " +
								mContext.getString(R.string.summary);
						mBuilder.append("<b>" + itemName);
					}else{
						mBuilder.append(itemName);
					}
					String itemTotalPrice = mFormat.currencyFormat(detail.getSubTotal());
					String itemTotalQty = mFormat.qtyFormat(detail.getQty()) + 
							createQtySpace(calculateLength(itemTotalPrice));
					mBuilder.append(createHorizontalSpace(calculateLength(itemName) + 
							calculateLength(itemTotalQty) + 
							calculateLength(itemTotalPrice)));
					mBuilder.append(itemTotalQty);
					mBuilder.append(itemTotalPrice + "\n");
					if(detail.getProductName() == Reporting.SUMM_GROUP){
						mBuilder.append(createLine("-") + "\n");
					}
				}
				mBuilder.append("\n");
			}
			
			String discountText = mContext.getString(R.string.discount);
			String discount = mFormat.currencyFormat(summOrder.getPriceDiscount());
			String subTotalText = mContext.getString(R.string.sub_total) + " ";
			String subTotal = mFormat.currencyFormat(summOrder.getTotalRetailPrice());
			
			mBuilder.append(subTotalText);
			mBuilder.append(createHorizontalSpace(
					calculateLength(subTotalText) 
					+ calculateLength(subTotal)));
			mBuilder.append(subTotal + "\n");
			mBuilder.append(discountText);
			mBuilder.append(createHorizontalSpace(calculateLength(discountText) 
					+ calculateLength(discount)));
			mBuilder.append(discount + "\n");
			
			// Vat Exclude
			if(summOrder.getVatExclude() > 0){
				String vatExcludeText = mContext.getString(R.string.vat_exclude) + " " +
						NumberFormat.getInstance().format(mShop.getCompanyVatRate()) + "%";
				String vatExclude = mFormat.currencyFormat(summOrder.getVatExclude());
				mBuilder.append(vatExcludeText);
				mBuilder.append(createHorizontalSpace(calculateLength(vatExcludeText) 
						+ calculateLength(vatExclude)));
				mBuilder.append(vatExclude + "\n\n");
			}else{
				mBuilder.append("\n");
			}
			
			String grandTotalText = mContext.getString(R.string.grand_total);
			String grandTotal = mFormat.currencyFormat(summOrder.getTotalSalePrice());
			mBuilder.append(grandTotalText);
			mBuilder.append(createHorizontalSpace(calculateLength(grandTotalText) 
					+ calculateLength(grandTotal)));
			mBuilder.append(grandTotal + "\n");
		}
	}
	
	protected class EPSONPrintSummarySale extends EPSONPrinter{

		public EPSONPrintSummarySale(Context context) {
			super(context);
		}

		@Override
		public void onBatteryStatusChangeEvent(String arg0, int arg1) {
		}

		@Override
		public void onStatusChangeEvent(String arg0, int arg1) {
		}

		@Override
		public void prepareDataToPrint(int transactionId) {
		}

		@Override
		public void prepareDataToPrint() {
			Session session = new Session(mContext.getApplicationContext());
			String sessionDate = session.getSessionDate(mSessionId);
			MPOSOrderTransaction trans = mTrans.getTransaction(sessionDate); 
			MPOSOrderTransaction.MPOSOrderDetail summOrder 
				= mTrans.getSummaryOrderInDay(sessionDate, sessionDate);

			try {
				mBuilder.addTextAlign(Builder.ALIGN_CENTER);
				// header
				mBuilder.addText(mContext.getString(R.string.endday_report) + "\n");
				mBuilder.addText(mFormat.dateFormat(sessionDate) + "\n");
				mBuilder.addText(mContext.getString(R.string.shop) + " " + mShop.getShopProperty().getShopName() + "\n");
				mBuilder.addText(mContext.getString(R.string.print_by) + " " + mStaff.getStaff(mStaffId).getStaffName() + "\n");
				mBuilder.addText(mContext.getString(R.string.print_date) + " " + mFormat.dateTimeFormat(Utils.getCalendar().getTime()) + "\n");
				
				// ReceiptNo.
				String receiptNo = mContext.getString(R.string.receipt_no);
				String from = mTrans.getMinReceiptNo(sessionDate) + " - ";
				String to = mTrans.getMaxReceiptNo(sessionDate);
				mBuilder.addText(receiptNo);
				mBuilder.addText(createHorizontalSpace(calculateLength(receiptNo)) + "\n");
				mBuilder.addText(from);
				mBuilder.addText(createHorizontalSpace(calculateLength(from)) + "\n");
				mBuilder.addText(to);
				mBuilder.addText(createHorizontalSpace(calculateLength(to)) + "\n\n");
				
				// Product Summary
				Reporting report = new Reporting(mContext, sessionDate, sessionDate);
				List<SimpleProductData> simpleLst = report.listSummaryProductGroupInDay();
				if(simpleLst != null){
					for(SimpleProductData sp : simpleLst){
						String groupName = sp.getDeptName();
						String groupTotalPrice = mFormat.currencyFormat(sp.getDeptTotalPrice());
						String groupTotalQty = mFormat.qtyFormat(sp.getDeptTotalQty()) + 
								createQtySpace(calculateLength(groupTotalPrice));
						mBuilder.addText(groupName);
						mBuilder.addText(createHorizontalSpace(calculateLength(groupName) + 
								calculateLength(groupTotalQty) 
								+ calculateLength(groupTotalPrice)));
						mBuilder.addText(groupTotalQty);
						mBuilder.addText(groupTotalPrice + "\n");
						if(sp.getItemLst() != null){
							for(SimpleProductData.Item item : sp.getItemLst()){
								String itemName = limitTextLength("-" + item.getItemName());
								String itemTotalPrice = mFormat.currencyFormat(item.getTotalPrice());
								String itemTotalQty = mFormat.qtyFormat(item.getTotalQty()) + 
										createQtySpace(calculateLength(itemTotalPrice));
								mBuilder.addText(itemName);
								mBuilder.addText(createHorizontalSpace(calculateLength(itemName) + 
										calculateLength(itemTotalQty) 
										+ calculateLength(itemTotalPrice)));
								mBuilder.addText(itemTotalQty);
								mBuilder.addText(itemTotalPrice + "\n");
							}
						}
					}
					// Sub Total
					mBuilder.addText("\n");
					String subTotalText = mContext.getString(R.string.sub_total);
					String subTotalPrice = mFormat.currencyFormat(summOrder.getTotalRetailPrice());
					String subTotalQty = mFormat.qtyFormat(summOrder.getQty()) + 
							createQtySpace(calculateLength(subTotalPrice));
					mBuilder.addText(subTotalText);
					mBuilder.addText(createHorizontalSpace(calculateLength(subTotalText) 
							+ calculateLength(subTotalQty) 
							+ calculateLength(subTotalPrice)));
					mBuilder.addText(subTotalQty);
					mBuilder.addText(subTotalPrice + "\n\n");
				}
				
				String discountText = mContext.getString(R.string.discount);
				String discount = mFormat.currencyFormat(summOrder.getPriceDiscount());
//				String subTotalText = mContext.getString(R.string.sub_total) + " ";
//				String subTotal = mFormat.currencyFormat(summOrder.getTotalSalePrice());
				
				mBuilder.addText(discountText);
				mBuilder.addText(createHorizontalSpace(calculateLength(discountText) 
						+ calculateLength(discount)));
				mBuilder.addText(discount + "\n");
//				mBuilder.addText(subTotalText);
//				mBuilder.addText(createHorizontalSpace(calculateLength(subTotalText) 
//						+ calculateLength(subTotal)));
//				mBuilder.addText(subTotal + "\n");
				
				// Vat Exclude
				if(summOrder.getVatExclude() > 0){
					String vatExcludeText = mContext.getString(R.string.vat_exclude) + " " +
							NumberFormat.getInstance().format(mShop.getCompanyVatRate()) + "%";
					String vatExclude = mFormat.currencyFormat(summOrder.getVatExclude());
					mBuilder.addText(vatExcludeText);
					mBuilder.addText(createHorizontalSpace(
							calculateLength(vatExcludeText) 
							+ calculateLength(vatExclude)));
					mBuilder.addText(vatExclude + "\n\n");
				}
				
				String totalSaleText = mContext.getString(R.string.total_sale);
				String totalSale = mFormat.currencyFormat(summOrder.getTotalSalePrice());
				mBuilder.addText(totalSaleText);
				mBuilder.addText(createHorizontalSpace(
						calculateLength(totalSaleText) 
						+ calculateLength(totalSale)));
				mBuilder.addText(totalSale + "\n");
				
				if(mShop.getCompanyVatType() == Products.VAT_TYPE_INCLUDED){
					String beforeVatText = mContext.getString(R.string.before_vat);
					String beforeVat = mFormat.currencyFormat(trans.getTransactionVatable() - trans.getTransactionVat());
					String totalVatText = mContext.getString(R.string.total_vat) + " " +
							NumberFormat.getInstance().format(mShop.getCompanyVatRate()) + "%";
					String totalVat = mFormat.currencyFormat(trans.getTransactionVat());
					mBuilder.addText(beforeVatText);
					mBuilder.addText(createHorizontalSpace(
							calculateLength(beforeVatText) 
							+ calculateLength(beforeVat)));
					mBuilder.addText(beforeVat + "\n");
					mBuilder.addText(totalVatText);
					mBuilder.addText(createHorizontalSpace(
							calculateLength(totalVatText) 
							+ calculateLength(totalVat)));
					mBuilder.addText(totalVat + "\n\n");
				}
				
				String seperateTransIds = mTrans.getSeperateTransactionId(sessionDate);
				// open/close shift
				String floatInText = mContext.getString(R.string.float_in);
				String totalCashText = mContext.getString(R.string.total_cash);
				String cashInDrawerText = mContext.getString(R.string.cash_in_drawer);
				String cashCountText = mContext.getString(R.string.cash_count);
				String overShotText = mContext.getString(R.string.over_or_short);
//					double totalBillPayByCashAmount = mPayment.getTotalTransPayByCash(session.getSessionDate());
				double floatInAmount = mSession.getOpenAmount(mSessionId);
				double totalCashAmount = mPayment.getTotalCash(seperateTransIds);
				double cashInDrawerAmount = floatInAmount + totalCashAmount;
				double cashCountAmount = mSession.getCloseAmount(mSessionId);
				String floatIn = mFormat.currencyFormat(floatInAmount);
				String cashInDrawer = mFormat.currencyFormat(cashInDrawerAmount);
				String totalCash = mFormat.currencyFormat(totalCashAmount);
				String cashCount = mFormat.currencyFormat(cashCountAmount);
				String overShot = mFormat.currencyFormat(cashCountAmount - cashInDrawerAmount);
//					String totalBillPayByCash = mFormat.currencyFormat(totalBillPayByCashAmount)
//							+ createQtySpace(calculateLength(totalCash));
				
//					mBuilder.addText(totalCashText);
//					mBuilder.addText(createHorizontalSpace(calculateLength(totalCashText)
//							+ calculateLength(totalBillPayByCash)
//							+ calculateLength(totalCash)));
//					mBuilder.addText(totalBillPayByCash);
//					mBuilder.addText(totalCash + "\n");
				mBuilder.addText(floatInText);
				mBuilder.addText(createHorizontalSpace(calculateLength(floatInText) + 
						calculateLength(floatIn)));
				mBuilder.addText(floatIn + "\n");
				mBuilder.addText(totalCashText);
				mBuilder.addText(createHorizontalSpace(calculateLength(totalCashText) + 
						calculateLength(totalCash)));
				mBuilder.addText(totalCash + "\n");
				mBuilder.addText(cashInDrawerText);
				mBuilder.addText(createHorizontalSpace(calculateLength(cashInDrawerText) + 
						calculateLength(cashInDrawer)));
				mBuilder.addText(cashInDrawer + "\n");
				mBuilder.addText(cashCountText);
				mBuilder.addText(createHorizontalSpace(calculateLength(cashCountText) + 
						calculateLength(cashCount)));
				mBuilder.addText(cashCount + "\n");
				mBuilder.addText(overShotText);
				mBuilder.addText(createHorizontalSpace(calculateLength(overShotText) + 
						calculateLength(overShot)));
				mBuilder.addText(overShot + "\n\n");
				
				List<Payment.PaymentDetail> summaryPaymentLst = 
						mPayment.listSummaryPayment(seperateTransIds);
				if(summaryPaymentLst != null){
					String paymentDetailText = mContext.getString(R.string.payment_detail);
					mBuilder.addText(paymentDetailText);
					mBuilder.addText(createHorizontalSpace(
							calculateLength(paymentDetailText)) + "\n");
					for(Payment.PaymentDetail payment : summaryPaymentLst){
						String payTypeName = payment.getPayTypeName();
						String payAmount = mFormat.currencyFormat(payment.getPayAmount());
						mBuilder.addText(payTypeName);
						mBuilder.addText(createHorizontalSpace(
								calculateLength(payTypeName) 
								+ calculateLength(payAmount)));
						mBuilder.addText(payAmount + "\n");
					}
					mBuilder.addText("\n");
				}
				String totalReceiptInDay = mContext.getString(R.string.total_receipt_in_day);
				String totalReceipt = String.valueOf(mTrans.getTotalReceipt(sessionDate));
				mBuilder.addText(totalReceiptInDay);
				mBuilder.addText(createHorizontalSpace(
						calculateLength(totalReceiptInDay) 
						+ calculateLength(totalReceipt)));
				mBuilder.addText(totalReceipt + "\n\n");
				
				MPOSOrderTransaction.MPOSOrderDetail summVoidOrder = 
						mTrans.getSummaryVoidOrderInDay(sessionDate);
				String voidBillText = mContext.getString(R.string.void_bill);
				mBuilder.addText(voidBillText);
				mBuilder.addText(createHorizontalSpace(calculateLength(voidBillText)) + "\n");
				String voidBill = mContext.getString(R.string.void_bill_after_paid);
				String totalVoidPrice = mFormat.currencyFormat(summVoidOrder.getTotalSalePrice());
				String totalVoidQty = mFormat.qtyFormat(summVoidOrder.getQty()) +
						createQtySpace(calculateLength(totalVoidPrice));
				mBuilder.addText(voidBill);
				mBuilder.addText(createHorizontalSpace(
						calculateLength(voidBill) 
						+ calculateLength(totalVoidQty) 
						+ calculateLength(totalVoidPrice)));
				mBuilder.addText(totalVoidQty);
				mBuilder.addText(totalVoidPrice);
			} catch (EposException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	protected class WintecPrintSummarySale extends WintecPrinter{
		
		public WintecPrintSummarySale(Context context) {
			super(context);
		}

		@Override
		public void prepareDataToPrint() {
			Session session = new Session(mContext.getApplicationContext());
			String sessionDate = session.getSessionDate(mSessionId);
			MPOSOrderTransaction trans = mTrans.getTransaction(sessionDate); 
			MPOSOrderTransaction.MPOSOrderDetail summOrder 
				= mTrans.getSummaryOrderInDay(sessionDate, sessionDate);

			// header
			mBuilder.append("<c>" + mContext.getString(R.string.endday_report) + "\n");
			mBuilder.append("<c>" + mFormat.dateFormat(sessionDate) + "\n");
			mBuilder.append("<c>" + mContext.getString(R.string.shop) + " " + mShop.getShopProperty().getShopName() + "\n");
			mBuilder.append(mContext.getString(R.string.print_by) + " " + mStaff.getStaff(mStaffId).getStaffName() + "\n");
			mBuilder.append(mContext.getString(R.string.print_date) + " " + mFormat.dateTimeFormat(Utils.getCalendar().getTime()) + "\n");
			
			// ReceiptNo.
			mBuilder.append("<u>" + mContext.getString(R.string.receipt_no) + "\n");
			mBuilder.append(mTrans.getMinReceiptNo(sessionDate) + " -\n");
			mBuilder.append(mTrans.getMaxReceiptNo(sessionDate) + "\n\n");
			
			// Product Summary
			Reporting report = new Reporting(mContext, sessionDate, sessionDate);
			List<SimpleProductData> simpleLst = report.listSummaryProductGroupInDay();
			if(simpleLst != null){
				for(SimpleProductData sp : simpleLst){
					String groupName = sp.getDeptName();
					String groupTotalPrice = mFormat.currencyFormat(sp.getDeptTotalPrice());
					String groupTotalQty = mFormat.qtyFormat(sp.getDeptTotalQty()) + 
							createQtySpace(calculateLength(groupTotalPrice));
					mBuilder.append("<b>" + groupName);
					mBuilder.append(createHorizontalSpace(
							calculateLength(groupName) 
							+ calculateLength(groupTotalQty) 
							+ calculateLength(groupTotalPrice)));
					mBuilder.append("<b>" + groupTotalQty);
					mBuilder.append("<b>" + groupTotalPrice + "\n");
					if(sp.getItemLst() != null){
						for(SimpleProductData.Item item : sp.getItemLst()){
							String itemName = limitTextLength("-" + item.getItemName());
							String itemTotalPrice = mFormat.currencyFormat(item.getTotalPrice());
							String itemTotalQty = mFormat.qtyFormat(item.getTotalQty()) + 
									createQtySpace(calculateLength(itemTotalPrice));
							mBuilder.append(itemName);
							mBuilder.append(createHorizontalSpace(
									calculateLength(itemName) 
									+ calculateLength(itemTotalQty) 
									+ calculateLength(itemTotalPrice)));
							mBuilder.append(itemTotalQty);
							mBuilder.append(itemTotalPrice + "\n");
						}
					}
				}
				// Sub Total
				mBuilder.append("\n");
				String subTotalText = mContext.getString(R.string.sub_total);
				String subTotalPrice = mFormat.currencyFormat(summOrder.getTotalRetailPrice());
				String subTotalQty = mFormat.qtyFormat(summOrder.getQty()) + 
						createQtySpace(calculateLength(subTotalPrice));
				mBuilder.append(subTotalText);
				mBuilder.append(createHorizontalSpace(
						calculateLength(subTotalText) 
						+ calculateLength(subTotalQty) 
						+ calculateLength(subTotalPrice)));
				mBuilder.append(subTotalQty);
				mBuilder.append(subTotalPrice + "\n\n");
			}
			
			String discountText = mContext.getString(R.string.discount);
			String discount = mFormat.currencyFormat(summOrder.getPriceDiscount());
//			String subTotalText = mContext.getString(R.string.sub_total) + " ";
//			String subTotal = mFormat.currencyFormat(summOrder.getTotalSalePrice());
			
			mBuilder.append(discountText);
			mBuilder.append(createHorizontalSpace(
					calculateLength(discountText) 
					+ calculateLength(discount)));
			mBuilder.append(discount + "\n");
//			mBuilder.append(subTotalText);
//			mBuilder.append(createHorizontalSpace(
//					calculateLength(subTotalText) 
//					+ calculateLength(subTotal)));
//			mBuilder.append(subTotal + "\n");
			
			// Vat Exclude
			if(summOrder.getVatExclude() > 0){
				String vatExcludeText = mContext.getString(R.string.vat_exclude) + " " +
						NumberFormat.getInstance().format(mShop.getCompanyVatRate()) + "%";
				String vatExclude = mFormat.currencyFormat(summOrder.getVatExclude());
				mBuilder.append(vatExcludeText);
				mBuilder.append(createHorizontalSpace(
						calculateLength(vatExcludeText) 
						+ calculateLength(vatExclude)));
				mBuilder.append(vatExclude + "\n\n");
			}
			
			String totalSaleText = mContext.getString(R.string.total_sale);
			String totalSale = mFormat.currencyFormat(summOrder.getTotalSalePrice());
			mBuilder.append(totalSaleText);
			mBuilder.append(createHorizontalSpace(
					calculateLength(totalSaleText) 
					+ calculateLength(totalSale)));
			mBuilder.append(totalSale + "\n");
			
			if(mShop.getCompanyVatType() == Products.VAT_TYPE_INCLUDED){
				String beforeVatText = mContext.getString(R.string.before_vat);
				String beforeVat = mFormat.currencyFormat(trans.getTransactionVatable() - trans.getTransactionVat());
				String totalVatText = mContext.getString(R.string.total_vat) + " " +
						NumberFormat.getInstance().format(mShop.getCompanyVatRate()) + "%";
				String totalVat = mFormat.currencyFormat(trans.getTransactionVat());
				mBuilder.append(beforeVatText);
				mBuilder.append(createHorizontalSpace(
						calculateLength(beforeVatText) 
						+ calculateLength(beforeVat)));
				mBuilder.append(beforeVat + "\n");
				mBuilder.append(totalVatText);
				mBuilder.append(createHorizontalSpace(
						calculateLength(totalVatText) 
						+ calculateLength(totalVat)));
				mBuilder.append(totalVat + "\n\n");
			}
			
			String seperateTransIds = mTrans.getSeperateTransactionId(sessionDate);
			// open/close shift
			String floatInText = mContext.getString(R.string.float_in);
			String totalCashText = mContext.getString(R.string.total_cash);
			String cashInDrawerText = mContext.getString(R.string.cash_in_drawer);
			String cashCountText = mContext.getString(R.string.cash_count);
			String overShotText = mContext.getString(R.string.over_or_short);
			//double totalBillPayByCashAmount = mPayment.getTotalTransPayByCash(session.getSessionDate());
			double floatInAmount = mSession.getOpenAmount(mSessionId);
			double totalCashAmount = mPayment.getTotalCash(seperateTransIds);
			double cashInDrawerAmount = floatInAmount + totalCashAmount;
			double cashCountAmount = mSession.getCloseAmount(mSessionId);
			String floatIn = mFormat.currencyFormat(floatInAmount);
			String cashInDrawer = mFormat.currencyFormat(cashInDrawerAmount);
			String totalCash = mFormat.currencyFormat(totalCashAmount);
			String cashCount = mFormat.currencyFormat(cashCountAmount);
			String overShot = mFormat.currencyFormat(cashCountAmount - cashInDrawerAmount);
//				String totalBillPayByCash = mFormat.qtyFormat(totalBillPayByCashAmount)
//						+ createQtySpace(calculateLength(totalCash));

//				mBuilder.append(totalCashText);
//				mBuilder.append(createHorizontalSpace(calculateLength(totalCashText)
//						+ calculateLength(totalBillPayByCash)
//						+ calculateLength(totalCash)));
//				mBuilder.append(totalBillPayByCash);
//				mBuilder.append(totalCash + "\n");
			mBuilder.append(floatInText);
			mBuilder.append(createHorizontalSpace(calculateLength(floatInText) + 
					calculateLength(floatIn)));
			mBuilder.append(floatIn + "\n");
			mBuilder.append(totalCashText);
			mBuilder.append(createHorizontalSpace(calculateLength(totalCashText) + 
					calculateLength(totalCash)));
			mBuilder.append(totalCash + "\n");
			mBuilder.append(cashInDrawerText);
			mBuilder.append(createHorizontalSpace(calculateLength(cashInDrawerText) + 
					calculateLength(cashInDrawer)));
			mBuilder.append(cashInDrawer + "\n");
			mBuilder.append(cashCountText);
			mBuilder.append(createHorizontalSpace(calculateLength(cashCountText) + 
					calculateLength(cashCount)));
			mBuilder.append(cashCount + "\n");
			mBuilder.append(overShotText);
			mBuilder.append(createHorizontalSpace(calculateLength(overShotText) + 
					calculateLength(overShot)));
			mBuilder.append(overShot + "\n\n");
			
			List<Payment.PaymentDetail> summaryPaymentLst = 
					mPayment.listSummaryPayment(seperateTransIds);
			if(summaryPaymentLst != null){
				mBuilder.append(mContext.getString(R.string.payment_detail) + "\n");
				for(Payment.PaymentDetail payment : summaryPaymentLst){
					String payTypeName = payment.getPayTypeName();
					String payAmount = mFormat.currencyFormat(payment.getPayAmount());
					mBuilder.append(payTypeName);
					mBuilder.append(createHorizontalSpace(
							calculateLength(payTypeName) 
							+ calculateLength(payAmount)));
					mBuilder.append(payAmount + "\n");
				}
				mBuilder.append("\n");
			}
			String totalReceiptInDay = mContext.getString(R.string.total_receipt_in_day);
			String totalReceipt = String.valueOf(mTrans.getTotalReceipt(sessionDate));
			mBuilder.append(totalReceiptInDay);
			mBuilder.append(createHorizontalSpace(
					calculateLength(totalReceiptInDay) 
					+ calculateLength(totalReceipt)));
			mBuilder.append(totalReceipt + "\n\n");
			
			MPOSOrderTransaction.MPOSOrderDetail summVoidOrder = 
					mTrans.getSummaryVoidOrderInDay(sessionDate);
			mBuilder.append(mContext.getString(R.string.void_bill) + "\n");
			String voidBill = mContext.getString(R.string.void_bill_after_paid);
			String totalVoidPrice = mFormat.currencyFormat(summVoidOrder.getTotalSalePrice());
			String totalVoidQty = mFormat.qtyFormat(summVoidOrder.getQty()) +
					createQtySpace(calculateLength(totalVoidPrice));
			mBuilder.append(voidBill);
			mBuilder.append(createHorizontalSpace(
					calculateLength(voidBill) 
					+ calculateLength(totalVoidQty) 
					+ calculateLength(totalVoidPrice)));
			mBuilder.append(totalVoidQty);
			mBuilder.append(totalVoidPrice);
		}
	}

	@Override
	public void run() {
		if(Utils.isInternalPrinterSetting(mContext)){
			switch(mWhatPrint){
			case SUMMARY_SALE:
				WintecPrintSummarySale wtPrinter = new WintecPrintSummarySale(mContext);
				wtPrinter.prepareDataToPrint();
				wtPrinter.print();
				break;
			case PRODUCT_REPORT:
				WintecPrintSaleByProduct wtPrintProduct = new WintecPrintSaleByProduct(mContext);
				wtPrintProduct.prepareDataToPrint();
				wtPrintProduct.print();
				break;
			case BILL_REPORT:
				WintecPrintBillReport wtPrintBill = new WintecPrintBillReport(mContext);
				wtPrintBill.prepareDataToPrint();
				wtPrintBill.print();
				break;
			}
		}else{
			switch(mWhatPrint){
			case SUMMARY_SALE:
				EPSONPrintSummarySale epSumSale = new EPSONPrintSummarySale(mContext);
				epSumSale.prepareDataToPrint();
				epSumSale.print();
				break;
			case PRODUCT_REPORT:
				EPSONPrintSaleByProduct epProduct = new EPSONPrintSaleByProduct(mContext);
				epProduct.prepareDataToPrint();
				epProduct.print();
				break;
			case BILL_REPORT:
				EPSONPrintBillReport epBill = new EPSONPrintBillReport(mContext);
				epBill.prepareDataToPrint();
				epBill.print();
				break;
			}
		}
	}
}
