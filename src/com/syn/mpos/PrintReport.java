package com.syn.mpos;

import java.util.List;

import com.syn.mpos.dao.FormatPropertyDao;
import com.syn.mpos.dao.MPOSOrderTransaction;
import com.syn.mpos.dao.PaymentDao;
import com.syn.mpos.dao.Reporting;
import com.syn.mpos.dao.Reporting.SimpleProductData;
import com.syn.mpos.dao.ProductsDao;
import com.syn.mpos.dao.SessionDao;
import com.syn.mpos.dao.ShopDao;
import com.syn.mpos.dao.StaffDao;
import com.syn.mpos.dao.TransactionDao;
import com.syn.mpos.dao.Util;
import com.syn.pos.Payment;
import com.syn.pos.Report;

import android.content.Context;
import android.os.AsyncTask;

public class PrintReport extends AsyncTask<Void, Void, Void> {

	public static enum WhatPrint{
		SUMMARY_SALE,
		PRODUCT_REPORT,
		BILL_REPORT
	};
	
	private Context mContext;
	private TransactionDao mTrans;
	private PaymentDao mPayment;
	private ShopDao mShop;
	private StaffDao mStaff;
	private FormatPropertyDao mFormat;
	private WhatPrint mWhatPrint;
	
	private int mStaffId;
	
	public PrintReport(Context context, int staffId, WhatPrint whatPrint){
		mContext = context;
		mTrans = new TransactionDao(context.getApplicationContext());
		mPayment = new PaymentDao(context.getApplicationContext());
		mShop = new ShopDao(context.getApplicationContext());
		mStaff = new StaffDao(context.getApplicationContext());
		mFormat = new FormatPropertyDao(context.getApplicationContext());
		mWhatPrint = whatPrint;
		
		mStaffId = staffId;
	}
	
	public PrintReport(Context context, WhatPrint whatPrint){
		this(context, 0, whatPrint);
	}
	
	public class WintecPrintSaleByProduct extends WintecPrinter{

		@Override
		public void prepareDataToPrint() {
			SessionDao session = new SessionDao(mContext.getApplicationContext());
			MPOSOrderTransaction.MPOSOrderDetail summOrder 
				= mTrans.getSummaryOrderInDay(session.getSessionDate());

			// header
			mBuilder.append("<c>" + mContext.getString(R.string.sale_by_product_report) + "\n");
			mBuilder.append("<c>" + mFormat.dateFormat(Util.getDate().getTime()) + "\n");
			mBuilder.append("<c>" + mContext.getString(R.string.shop) + " " + mShop.getShopProperty().getShopName() + "\n");
			mBuilder.append(mContext.getString(R.string.print_date) + " " + mFormat.dateTimeFormat(Util.getCalendar().getTime()) + "\n\n");
			
			// Product Summary
			Reporting reporting = new Reporting(mContext, session.getSessionDate(), session.getSessionDate());
			Report reportData = reporting.getProductDataReport();
			for(Report.GroupOfProduct group : reportData.getGroupOfProductLst()){
				mBuilder.append("<b>" + group.getProductGroupName() + ": " + group.getProductDeptName()+ "\n");
				for(Report.ReportDetail detail : group.getReportDetail()){
					String itemName = detail.getProductName();
					if(detail.getProductName() == Reporting.SUMM_DEPT){
						itemName = group.getProductDeptName() + " " +
								mContext.getString(R.string.summary);
					}else if(detail.getProductName() == Reporting.SUMM_GROUP){
						itemName = group.getProductGroupName() + " " +
								mContext.getString(R.string.summary);
					}
					String itemTotalPrice = mFormat.currencyFormat(detail.getSubTotal());
					String itemTotalQty = mFormat.qtyFormat(detail.getQty()) + 
							createQtySpace(itemTotalPrice.length());
					mBuilder.append(itemName);
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
			String subTotal = mFormat.currencyFormat(summOrder.getTotalSalePrice());
			
			mBuilder.append(subTotalText);
			mBuilder.append(createHorizontalSpace(subTotalText.length() + subTotal.length()));
			mBuilder.append(subTotal + "\n");
			mBuilder.append(discountText);
			mBuilder.append(createHorizontalSpace(discountText.length() + discount.length()));
			mBuilder.append(discount + "\n");
			
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
			SessionDao session = new SessionDao(mContext.getApplicationContext());
			MPOSOrderTransaction trans = mTrans.getTransaction(session.getSessionDate()); 
			MPOSOrderTransaction.MPOSOrderDetail summOrder 
				= mTrans.getSummaryOrderInDay(session.getSessionDate());

			// header
			mBuilder.append("<c>" + mContext.getString(R.string.summary_sale_by_day) + "\n");
			mBuilder.append("<c>" + mFormat.dateFormat(Util.getDate().getTime()) + "\n");
			mBuilder.append("<c>" + mContext.getString(R.string.shop) + " " + mShop.getShopProperty().getShopName() + "\n");
			mBuilder.append(mContext.getString(R.string.print_by) + " " + mStaff.getStaff(mStaffId).getStaffName() + "\n");
			mBuilder.append(mContext.getString(R.string.print_date) + " " + mFormat.dateTimeFormat(Util.getCalendar().getTime()) + "\n");
			
			// ReceiptNo.
			mBuilder.append("<u>" + mContext.getString(R.string.receipt_no) + "\n");
			mBuilder.append(mTrans.getMinReceiptNo(session.getSessionDate()) + " -\n");
			mBuilder.append(mTrans.getMaxReceiptNo(session.getSessionDate()) + "\n\n");
			
			// Product Summary
			Reporting report = new Reporting(mContext, session.getSessionDate(), session.getSessionDate());
			List<SimpleProductData> simpleLst = report.getSummaryProductGroupInDay();
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
				String vatExcludeText = mContext.getString(R.string.vat_exclude);
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
			
			if(mShop.getCompanyVatType() == ProductsDao.VAT_TYPE_INCLUDED){
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
			default:
				break;
			}
		}else{
			
		}
		return null;
	}
}
