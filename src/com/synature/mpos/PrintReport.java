package com.synature.mpos;

import android.content.Context;

public class PrintReport implements Runnable{

	public static enum WhatPrint{
		SUMMARY_SALE,
		PRODUCT_REPORT,
		BILL_REPORT
	};
	
	private Context mContext;
	private WhatPrint mWhatPrint;
	private String mDateFrom;
	private String mDateTo;
	private int mSessionId;
	private int mStaffId;
	
	/**
	 * @param context
	 * @param whatPrint
	 * @param sessionId
	 * @param staffId
	 */
	public PrintReport(Context context, WhatPrint whatPrint, int sessionId, int staffId){
		mContext = context;
		mWhatPrint = whatPrint;
		mSessionId = sessionId;
		mStaffId = staffId;
	}
	
	/**
	 * @param context
	 * @param whatPrint
	 * @param dateFrom
	 * @param dateTo
	 */
	public PrintReport(Context context, WhatPrint whatPrint, String dateFrom, String dateTo){
		this(context, whatPrint, 0, 0);
		mDateFrom = dateFrom;
		mDateTo = dateTo;
	}

	@Override
	public void run() {
		if(Utils.isInternalPrinterSetting(mContext)){
			WintecPrinter wtPrinter = new WintecPrinter(mContext);
			switch(mWhatPrint){
			case SUMMARY_SALE:
				wtPrinter.createTextForPrintSummaryReport(mSessionId, mStaffId);
				wtPrinter.print();
				break;
			case PRODUCT_REPORT:
				wtPrinter.createTextForPrintSaleByProductReport(mDateFrom, mDateTo);
				wtPrinter.print();
				break;
			case BILL_REPORT:
				wtPrinter.createTextForPrintSaleByBillReport(mDateFrom, mDateTo);
				wtPrinter.print();
				break;
			}
		}else{
			EPSONPrinter epPrinter = new EPSONPrinter(mContext);
			switch(mWhatPrint){
			case SUMMARY_SALE:
				epPrinter.createTextForPrintSummaryReport(mSessionId, mStaffId);
				epPrinter.print();
				break;
			case PRODUCT_REPORT:
				epPrinter.createTextForPrintSaleByProductReport(mDateFrom, mDateTo);
				epPrinter.print();
				break;
			case BILL_REPORT:
				epPrinter.createTextForPrintSaleByBillReport(mDateFrom, mDateTo);
				epPrinter.print();
				break;
			}
		}
	}
}
