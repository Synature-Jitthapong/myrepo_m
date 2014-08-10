package com.synature.mpos;

import java.util.List;

import android.content.Context;
import com.synature.mpos.database.PrintReceiptLog;
import com.synature.util.Logger;

public class PrintReceipt implements Runnable{
	
	public static final String TAG = "PrintReceipt";
	
	private PrintReceiptLog mPrintLog;
	private Context mContext;
	
	/**
	 * @param context
	 */
	public PrintReceipt(Context context){
		mContext = context;
		mPrintLog = new PrintReceiptLog(context);
	}

	@Override
	public void run() {
		List<PrintReceiptLog.PrintReceipt> printLogLst = mPrintLog.listPrintReceiptLog(); 
		for(int i = 0; i < printLogLst.size(); i++){
			PrintReceiptLog.PrintReceipt printReceipt = printLogLst.get(i);
			try {
				if(Utils.isInternalPrinterSetting(mContext)){
					WintecPrinter wtPrinter = new WintecPrinter(mContext);
					wtPrinter.createTextForPrintReceipt(printReceipt.getTransactionId(), printReceipt.isCopy());
					wtPrinter.print();
				}else{
					EPSONPrinter epPrinter = new EPSONPrinter(mContext);
					epPrinter.createTextForPrintReceipt(printReceipt.getTransactionId(), printReceipt.isCopy());
					epPrinter.print();
				}
				mPrintLog.deletePrintStatus(printReceipt.getPriceReceiptLogId());
				
			} catch (Exception e) {
				mPrintLog.updatePrintStatus(printReceipt.getPriceReceiptLogId(), PrintReceiptLog.PRINT_NOT_SUCCESS);
				Logger.appendLog(mContext, 
						Utils.LOG_PATH, Utils.LOG_FILE_NAME, 
						" Print receipt fail : " + e.getMessage());
			}
		}
	}
}
