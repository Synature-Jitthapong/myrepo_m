package com.syn.mpos;

import java.text.NumberFormat;
import java.util.List;

import com.syn.mpos.dao.Formater;
import com.syn.mpos.dao.MPOSOrderTransaction;
import com.syn.mpos.dao.PaymentDetail;
import com.syn.mpos.dao.Reporting;
import com.syn.mpos.dao.Reporting.SimpleProductData;
import com.syn.mpos.dao.Products;
import com.syn.mpos.dao.Session;
import com.syn.mpos.dao.Shop;
import com.syn.mpos.dao.Staffs;
import com.syn.mpos.dao.Transaction;
import com.syn.mpos.dao.Util;
import com.synature.pos.Payment;
import com.synature.pos.Report;

import android.content.Context;
import android.os.AsyncTask;

public class PrintReport extends AsyncTask<Void, Void, Void> {

	public static enum WhatPrint{
		SUMMARY_SALE,
		PRODUCT_REPORT,
		BILL_REPORT
	};
	
	private Context mContext;
	private Transaction mTrans;
	private PaymentDetail mPayment;
	private Shop mShop;
	private Staffs mStaff;
	private Formater mFormat;
	private WhatPrint mWhatPrint;
	private String mDateFrom;
	private String mDateTo;
	
	private int mStaffId;
	
	public PrintReport(Context context, String dateFrom, String dateTo, 
			int staffId, WhatPrint whatPrint){
		mContext = context;
		mTrans = new Transaction(context.getApplicationContext());
		mPayment = new PaymentDetail(context.getApplicationContext());
		mShop = new Shop(context.getApplicationContext());
		mStaff = new Staffs(context.getApplicationContext());
		mFormat = new Formater(context.getApplicationContext());
		mWhatPrint = whatPrint;
		
		mDateFrom = dateFrom;
		mDateTo = dateTo;
		mStaffId = staffId;
	}
	
	public PrintReport(Context context, String dateFrom, String dateTo, WhatPrint whatPrint){
		this(context, dateFrom, dateTo, 0, whatPrint);
	}
	
	public PrintReport(Context context, int staffId, WhatPrint whatPrint){
		this(context, "", "", staffId, whatPrint);
	}
	
	public class WintecPrintBillReport extends WintecPrinter{

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
			mBuilder.append(mContext.getString(R.string.print_date) + " " + mFormat.dateTimeFormat(Util.getCalendar().getTime()) + "\n");
			
			String receiptHeader = mContext.getString(R.string.receipt);
			String totalSaleHeader = mContext.getString(R.string.total);
			String closeTimeHeader = mContext.getString(R.string.time) + 
					createQtySpace(totalSaleHeader.length());
			
			// line
			mBuilder.append(createLine("-") + "\n");
			mBuilder.append(receiptHeader);
			mBuilder.append(createHorizontalSpace(receiptHeader.length() 
					+ closeTimeHeader.length() + totalSaleHeader.length()));
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
							createQtySpace(totalSale.length());
					mBuilder.append(receiptNo);
					mBuilder.append(createHorizontalSpace(receiptNo.length() + 
							totalSale.length() + closeTime.length()));
					mBuilder.append(closeTime);
					mBuilder.append(totalSale + "\n");
				}
				mBuilder.append("\n");
			}
		}
		
	}
	
	public class WintecPrintSaleByProduct extends WintecPrinter{

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
			mBuilder.append(mContext.getString(R.string.print_date) + " " + mFormat.dateTimeFormat(Util.getCalendar().getTime()) + "\n\n");
			
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
							createQtySpace(itemTotalPrice.length());
					mBuilder.append(createHorizontalSpace(itemName.length() + 
							itemTotalQty.length() + itemTotalPrice.length()));
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
			mBuilder.append(createHorizontalSpace(subTotalText.length() + subTotal.length()));
			mBuilder.append(subTotal + "\n");
			mBuilder.append(discountText);
			mBuilder.append(createHorizontalSpace(discountText.length() + discount.length()));
			mBuilder.append(discount + "\n");
			
			// Vat Exclude
			if(summOrder.getVatExclude() > 0){
				String vatExcludeText = mContext.getString(R.string.tax_exclude) + " " +
						NumberFormat.getInstance().format(mShop.getCompanyVatRate()) + "%";
				String vatExclude = mFormat.currencyFormat(summOrder.getVatExclude());
				mBuilder.append(vatExcludeText);
				mBuilder.append(createHorizontalSpace(vatExcludeText.length() + vatExclude.length()));
				mBuilder.append(vatExclude + "\n\n");
			}else{
				mBuilder.append("\n");
			}
			
			String grandTotalText = mContext.getString(R.string.grand_total);
			String grandTotal = mFormat.currencyFormat(summOrder.getTotalSalePrice() + summOrder.getVatExclude());
			mBuilder.append(grandTotalText);
			mBuilder.append(createHorizontalSpace(grandTotalText.length() + grandTotal.length()));
			mBuilder.append(grandTotal + "\n");
		}
	}
	
	public class WintecPrintSummarySale extends WintecPrinter{
		
		@Override
		public void prepareDataToPrint() {
			Session session = new Session(mContext.getApplicationContext());
			MPOSOrderTransaction trans = mTrans.getTransaction(session.getSessionDate()); 
			MPOSOrderTransaction.MPOSOrderDetail summOrder 
				= mTrans.getSummaryOrderInDay(session.getSessionDate(), session.getSessionDate());

			// header
			mBuilder.append("<c>" + mContext.getString(R.string.summary_sale_by_day) + "\n");
			mBuilder.append("<c>" + mFormat.dateFormat(session.getSessionDate()) + "\n");
			mBuilder.append("<c>" + mContext.getString(R.string.shop) + " " + mShop.getShopProperty().getShopName() + "\n");
			mBuilder.append(mContext.getString(R.string.print_by) + " " + mStaff.getStaff(mStaffId).getStaffName() + "\n");
			mBuilder.append(mContext.getString(R.string.print_date) + " " + mFormat.dateTimeFormat(Util.getCalendar().getTime()) + "\n");
			
			// ReceiptNo.
			mBuilder.append("<u>" + mContext.getString(R.string.receipt_no) + "\n");
			mBuilder.append(mTrans.getMinReceiptNo(session.getSessionDate()) + " -\n");
			mBuilder.append(mTrans.getMaxReceiptNo(session.getSessionDate()) + "\n\n");
			
			// Product Summary
			Reporting report = new Reporting(mContext, session.getSessionDate(), session.getSessionDate());
			List<SimpleProductData> simpleLst = report.listSummaryProductGroupInDay();
			if(simpleLst != null){
				for(SimpleProductData sp : simpleLst){
					String groupName = sp.getDeptName();
					String groupTotalPrice = mFormat.currencyFormat(sp.getDeptTotalPrice());
					String groupTotalQty = mFormat.qtyFormat(sp.getDeptTotalQty()) + 
							createQtySpace(groupTotalPrice.length());
					mBuilder.append("<b>" + groupName);
					mBuilder.append(createHorizontalSpace(groupName.length() + 
							groupTotalQty.length() + groupTotalPrice.length()));
					mBuilder.append("<b>" + groupTotalQty);
					mBuilder.append("<b>" + groupTotalPrice + "\n");
					if(sp.getItemLst() != null){
						for(SimpleProductData.Item item : sp.getItemLst()){
							String itemName = "-" + item.getItemName();
							String itemTotalPrice = mFormat.currencyFormat(item.getTotalPrice());
							String itemTotalQty = mFormat.qtyFormat(item.getTotalQty()) + 
									createQtySpace(itemTotalPrice.length());
							mBuilder.append(itemName);
							mBuilder.append(createHorizontalSpace(itemName.length() + 
									itemTotalQty.length() + itemTotalPrice.length()));
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
						createQtySpace(subTotalPrice.length());
				mBuilder.append(subTotalText);
				mBuilder.append(createHorizontalSpace(subTotalText.length() + subTotalQty.length() 
						+ subTotalPrice.length()));
				mBuilder.append(subTotalQty);
				mBuilder.append(subTotalPrice + "\n\n");
			}
			
			String discountText = mContext.getString(R.string.discount);
			String discount = mFormat.currencyFormat(summOrder.getPriceDiscount());
			String subTotalText = mContext.getString(R.string.sub_total) + " ";
			String subTotal = mFormat.currencyFormat(summOrder.getTotalSalePrice());
			
			mBuilder.append(discountText);
			mBuilder.append(createHorizontalSpace(discountText.length() + discount.length()));
			mBuilder.append(discount + "\n");
			mBuilder.append(subTotalText);
			mBuilder.append(createHorizontalSpace(subTotalText.length() + subTotal.length()));
			mBuilder.append(subTotal + "\n");
			
			// Vat Exclude
			if(summOrder.getVatExclude() > 0){
				String vatExcludeText = mContext.getString(R.string.vat_exclude) + " " +
						NumberFormat.getInstance().format(mShop.getCompanyVatRate()) + "%";
				String vatExclude = mFormat.currencyFormat(summOrder.getVatExclude());
				mBuilder.append(vatExcludeText);
				mBuilder.append(createHorizontalSpace(vatExcludeText.length() + vatExclude.length()));
				mBuilder.append(vatExclude + "\n\n");
			}
			
			String totalSaleText = mContext.getString(R.string.total_sale);
			String totalSale = mFormat.currencyFormat(summOrder.getTotalSalePrice() + summOrder.getVatExclude());
			mBuilder.append(totalSaleText);
			mBuilder.append(createHorizontalSpace(totalSaleText.length() + totalSale.length()));
			mBuilder.append(totalSale + "\n");
			
			if(mShop.getCompanyVatType() == Products.VAT_TYPE_INCLUDED){
				String beforeVatText = mContext.getString(R.string.before_vat);
				String beforeVat = mFormat.currencyFormat(trans.getTransactionVatable() - trans.getTransactionVat());
				String totalVatText = mContext.getString(R.string.total_vat);
				String totalVat = mFormat.currencyFormat(trans.getTransactionVat());
				mBuilder.append(beforeVatText);
				mBuilder.append(createHorizontalSpace(beforeVatText.length() + beforeVat.length()));
				mBuilder.append(beforeVat + "\n");
				mBuilder.append(totalVatText);
				mBuilder.append(createHorizontalSpace(totalVatText.length() + totalVat.length()));
				mBuilder.append(totalVat + "\n\n");
			}
			
			List<Payment.PaymentDetail> summaryPaymentLst = 
					mPayment.listSummaryPayment(
							mTrans.getSeperateTransactionId(session.getSessionDate()));
			if(summaryPaymentLst != null){
				mBuilder.append(mContext.getString(R.string.payment_detail) + "\n");
				for(Payment.PaymentDetail payment : summaryPaymentLst){
					String payTypeName = payment.getPayTypeName();
					String payAmount = mFormat.currencyFormat(payment.getPayAmount());
					mBuilder.append(payTypeName);
					mBuilder.append(createHorizontalSpace(payTypeName.length() + payAmount.length()));
					mBuilder.append(payAmount + "\n\n");
				}
			}
			String totalReceiptInDay = mContext.getString(R.string.total_receipt_in_day);
			String totalReceipt = String.valueOf(mTrans.getTotalReceipt(session.getSessionDate()));
			mBuilder.append(totalReceiptInDay);
			mBuilder.append(createHorizontalSpace(totalReceiptInDay.length() + totalReceipt.length()));
			mBuilder.append(totalReceipt + "\n\n");
			
			MPOSOrderTransaction.MPOSOrderDetail summVoidOrder = 
					mTrans.getSummaryVoidOrderInDay(session.getSessionDate());
			mBuilder.append(mContext.getString(R.string.void_bill) + "\n");
			String voidBill = mContext.getString(R.string.void_bill_after_paid);
			String totalVoidPrice = mFormat.currencyFormat(summVoidOrder.getTotalSalePrice());
			String totalVoidQty = mFormat.qtyFormat(summVoidOrder.getQty()) +
					createQtySpace(totalVoidPrice.length());
			mBuilder.append(voidBill);
			mBuilder.append(createHorizontalSpace(voidBill.length() + totalVoidQty.length() + 
					totalVoidPrice.length()));
			mBuilder.append(totalVoidQty);
			mBuilder.append(totalVoidPrice);
		}
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		if(MPOSApplication.getInternalPrinterSetting(mContext)){
			switch(mWhatPrint){
			case SUMMARY_SALE:
				WintecPrintSummarySale wtPrinter = new WintecPrintSummarySale();
				wtPrinter.prepareDataToPrint();
				wtPrinter.print();
				break;
			case PRODUCT_REPORT:
				WintecPrintSaleByProduct wtPrintProduct = new WintecPrintSaleByProduct();
				wtPrintProduct.prepareDataToPrint();
				wtPrintProduct.print();
				break;
			case BILL_REPORT:
				WintecPrintBillReport wtPrintBill = new WintecPrintBillReport();
				wtPrintBill.prepareDataToPrint();
				wtPrintBill.print();
				break;
			default:
				break;
			}
		}else{
			
		}
		return null;
	}
}
