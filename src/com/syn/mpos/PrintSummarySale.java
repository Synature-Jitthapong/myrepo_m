package com.syn.mpos;

import java.util.List;

import com.syn.mpos.dao.GlobalPropertyDao;
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

import android.content.Context;
import android.os.AsyncTask;

public class PrintSummarySale extends AsyncTask<Void, Void, Void> {

	private Context mContext;
	private TransactionDao mTransDao;
	private PaymentDao mPaymentDao;
	private ShopDao mShopDao;
	private StaffDao mStaffDao;
	private GlobalPropertyDao mGlobalDao;
	
	private int mStaffId;
	
	public PrintSummarySale(Context context, int staffId){
		mContext = context;
		mTransDao = new TransactionDao(context.getApplicationContext());
		mPaymentDao = new PaymentDao(context.getApplicationContext());
		mShopDao = new ShopDao(context.getApplicationContext());
		mStaffDao = new StaffDao(context.getApplicationContext());
		mGlobalDao = new GlobalPropertyDao(context.getApplicationContext());
		
		mStaffId = staffId;
	}
	
	public class WintecPrintSummarySale extends WintecPrinter{
		
		public void preparedDataToPrint(){
			SessionDao session = new SessionDao(mContext.getApplicationContext());
			MPOSOrderTransaction trans = mTransDao.getTransaction(session.getSessionDate()); 
			MPOSOrderTransaction.MPOSOrderDetail summOrder 
				= mTransDao.getSummaryOrderInDay(session.getSessionDate());

			// header
			mBuilder.append("<c>" + mContext.getString(R.string.summary_sale_by_day) + "\n");
			mBuilder.append("<c>" + mGlobalDao.dateFormat(Util.getDate().getTime()) + "\n");
			mBuilder.append("<c>" + mContext.getString(R.string.shop) + " " + mShopDao.getShopProperty().getShopName() + "\n");
			mBuilder.append(mContext.getString(R.string.print_by) + " " + mStaffDao.getStaff(mStaffId).getStaffName() + "\n");
			mBuilder.append(mContext.getString(R.string.print_date) + " " + mGlobalDao.dateTimeFormat(Util.getCalendar().getTime()) + "\n");
			
			// ReceiptNo.
			mBuilder.append("<u>" + mContext.getString(R.string.receipt_no) + "\n");
			mBuilder.append(mTransDao.getMinReceiptNo(session.getSessionDate()) + " -\n");
			mBuilder.append(mTransDao.getMaxReceiptNo(session.getSessionDate()) + "\n\n");
			
			// Product Summary
			Reporting report = new Reporting(mContext, session.getSessionDate(), session.getSessionDate());
			List<SimpleProductData> simpleLst = report.getSummaryProductGroupInDay();
			if(simpleLst != null){
				for(SimpleProductData sp : simpleLst){
					String deptName = sp.getDeptName();
					String deptTotalPrice = mGlobalDao.currencyFormat(sp.getDeptTotalPrice());
					String deptTotalQty = mGlobalDao.qtyFormat(sp.getDeptTotalQty()) + 
							createQtySpace(deptTotalPrice.length());
					mBuilder.append("<b>" + deptName);
					mBuilder.append(createHorizontalSpace(deptName.length() + 
							deptTotalQty.length() + deptTotalPrice.length()));
					mBuilder.append("<b>" + deptTotalQty);
					mBuilder.append("<b>" + deptTotalPrice + "\n");
					if(sp.getItemLst() != null){
						for(SimpleProductData.Item item : sp.getItemLst()){
							String itemName = "-" + item.getItemName();
							String itemTotalPrice = mGlobalDao.currencyFormat(item.getTotalPrice());
							String itemTotalQty = mGlobalDao.qtyFormat(item.getTotalQty()) + 
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
				String subTotalPrice = mGlobalDao.currencyFormat(summOrder.getTotalRetailPrice());
				String subTotalQty = mGlobalDao.qtyFormat(summOrder.getQty()) + 
						createQtySpace(subTotalPrice.length());
				mBuilder.append(subTotalText);
				mBuilder.append(createHorizontalSpace(subTotalText.length() + subTotalQty.length() 
						+ subTotalPrice.length()));
				mBuilder.append(subTotalQty);
				mBuilder.append(subTotalPrice + "\n\n");
			}
			
			String discountText = mContext.getString(R.string.discount);
			String discount = mGlobalDao.currencyFormat(summOrder.getPriceDiscount());
			String subTotalText = mContext.getString(R.string.sub_total) + " ";
			String subTotal = mGlobalDao.currencyFormat(summOrder.getTotalSalePrice());
			
			mBuilder.append(discountText);
			mBuilder.append(createHorizontalSpace(discountText.length() + discount.length()));
			mBuilder.append(discount + "\n");
			mBuilder.append(subTotalText);
			mBuilder.append(createHorizontalSpace(subTotalText.length() + subTotal.length()));
			mBuilder.append(subTotal + "\n");
			
			// Vat Exclude
			if(summOrder.getVatExclude() > 0){
				String vatExcludeText = mContext.getString(R.string.vat_exclude);
				String vatExclude = mGlobalDao.currencyFormat(summOrder.getVatExclude());
				mBuilder.append(vatExcludeText);
				mBuilder.append(createHorizontalSpace(vatExcludeText.length() + vatExclude.length()));
				mBuilder.append(vatExclude + "\n\n");
			}
			
			String totalSaleText = mContext.getString(R.string.total_sale);
			String totalSale = mGlobalDao.currencyFormat(summOrder.getTotalSalePrice() + summOrder.getVatExclude());
			mBuilder.append(totalSaleText);
			mBuilder.append(createHorizontalSpace(totalSaleText.length() + totalSale.length()));
			mBuilder.append(totalSale + "\n");
			
			if(mShopDao.getCompanyVatType() == ProductsDao.VAT_TYPE_INCLUDED){
				String beforeVatText = mContext.getString(R.string.before_vat);
				String beforeVat = mGlobalDao.currencyFormat(trans.getTransactionVatable() - trans.getTransactionVat());
				String totalVatText = mContext.getString(R.string.total_vat);
				String totalVat = mGlobalDao.currencyFormat(trans.getTransactionVat());
				mBuilder.append(beforeVatText);
				mBuilder.append(createHorizontalSpace(beforeVatText.length() + beforeVat.length()));
				mBuilder.append(beforeVat + "\n");
				mBuilder.append(totalVatText);
				mBuilder.append(createHorizontalSpace(totalVatText.length() + totalVat.length()));
				mBuilder.append(totalVat + "\n\n");
			}
			
			List<Payment.PaymentDetail> summaryPaymentLst = 
					mPaymentDao.listSummaryPayment(
							mTransDao.getSeperateTransactionId(session.getSessionDate()));
			if(summaryPaymentLst != null){
				mBuilder.append(mContext.getString(R.string.payment_detail) + "\n");
				for(Payment.PaymentDetail payment : summaryPaymentLst){
					String payTypeName = payment.getPayTypeName();
					String payAmount = mGlobalDao.currencyFormat(payment.getPayAmount());
					mBuilder.append(payTypeName);
					mBuilder.append(createHorizontalSpace(payTypeName.length() + payAmount.length()));
					mBuilder.append(payAmount + "\n\n");
				}
			}
			String totalReceiptInDay = mContext.getString(R.string.total_receipt_in_day);
			String totalReceipt = String.valueOf(mTransDao.getTotalReceipt(session.getSessionDate()));
			mBuilder.append(totalReceiptInDay);
			mBuilder.append(createHorizontalSpace(totalReceiptInDay.length() + totalReceipt.length()));
			mBuilder.append(totalReceipt + "\n\n");
			
			MPOSOrderTransaction.MPOSOrderDetail summVoidOrder = 
					mTransDao.getSummaryVoidOrderInDay(session.getSessionDate());
			mBuilder.append(mContext.getString(R.string.void_bill) + "\n");
			String voidBill = mContext.getString(R.string.void_bill_after_paid);
			String totalVoidPrice = mGlobalDao.currencyFormat(summVoidOrder.getTotalSalePrice());
			String totalVoidQty = mGlobalDao.qtyFormat(summVoidOrder.getQty()) +
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
		WintecPrintSummarySale wtPrinter = new WintecPrintSummarySale();
		wtPrinter.preparedDataToPrint();
		wtPrinter.print();
		return null;
	}

}
