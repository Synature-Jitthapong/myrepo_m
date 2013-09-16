package com.syn.mpos;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import com.syn.mpos.database.Reporting;
import com.syn.pos.Report;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SaleReportActivity extends Activity {
	//private static final String TAG = "SaleReportActivity"; 
	private Context context;
	private int mode = 1;
	private Calendar calendar;
	private Formatter format;
	private Reporting report;
	private long dateFrom, dateTo;
	
	private Button btnDateFrom;
	private Button btnDateTo;
	private Button btnCreateReport;
	private TextView mTvTitle;
	private TableLayout tbReport;
	private TableRow trProductReportHeader;
	private TableRow trBillReportHeader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sale_report);
		
		ActionBar actionBar = getActionBar();
		actionBar.setCustomView(R.layout.date_condition);
	    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
	            | ActionBar.DISPLAY_SHOW_HOME);
		
		tbReport = (TableLayout) findViewById(R.id.tbReport);
		trProductReportHeader = (TableRow) findViewById(R.id.tableRowByProduct);
		trBillReportHeader = (TableRow) findViewById(R.id.tableRowByBill);
		btnDateFrom = (Button) actionBar.getCustomView().findViewById(R.id.btnDateFrom);
		btnDateTo = (Button) actionBar.getCustomView().findViewById(R.id.btnDateTo);
		btnCreateReport = (Button) actionBar.getCustomView().findViewById(R.id.btnGenReport);
		mTvTitle = (TextView) actionBar.getCustomView().findViewById(R.id.textView1);
		
		Intent intent = getIntent();
		mode = intent.getIntExtra("mode", 1);
		
		if(mode == 1){
			mTvTitle.setText(R.string.sale_report_by_bill);
			trBillReportHeader.setVisibility(View.VISIBLE);
			trProductReportHeader.setVisibility(View.GONE);
		}else if (mode == 2){
			mTvTitle.setText(R.string.sale_report_by_product);
			trBillReportHeader.setVisibility(View.GONE);
			trProductReportHeader.setVisibility(View.VISIBLE);
		}
		
		context = SaleReportActivity.this;
		format = new Formatter(context);
		Calendar c = Calendar.getInstance();
		calendar = new GregorianCalendar(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		btnDateFrom.setText(format.dateFormat(calendar.getTime()));
		btnDateTo.setText(format.dateFormat(calendar.getTime()));
		dateFrom = calendar.getTimeInMillis();
		dateTo = calendar.getTimeInMillis();
		
		btnDateFrom.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				DialogFragment dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
					
					@Override
					public void onSetDate(long date) {
						calendar.setTimeInMillis(date);
						dateFrom = calendar.getTimeInMillis();
						
						btnDateFrom.setText(format.dateFormat(calendar.getTime()));
					}
				});
				dialogFragment.show(getFragmentManager(), "Condition");
			}
			
		});
		
		btnDateTo.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				DialogFragment dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
					
					@Override
					public void onSetDate(long date) {
						calendar.setTimeInMillis(date);
						dateTo = calendar.getTimeInMillis();
						
						btnDateTo.setText(format.dateFormat(calendar.getTime()));
					}
				});
				dialogFragment.show(getFragmentManager(), "Condition");
			}
			
		});
		
		btnCreateReport.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if (mode == 1)
					createReportByBill();
				else if (mode == 2)
					createReportByProduct();
			}
		});
	}
	
	private void createReportByBill(){
		report = new Reporting(context, dateFrom, dateTo);
		Report reportData = report.getSaleReportByBill();

		tbReport.removeAllViews();
		LayoutInflater inflater = LayoutInflater.from(context);
		for(Report.ReportDetail reportDetail : reportData.reportDetail){
			View tbRowDetail = inflater.inflate(R.layout.sale_report_by_bill_template, null);
			TextView tvReceiptNo = (TextView) tbRowDetail.findViewById(R.id.tvReceipt);
			TextView tvTotalPrice = (TextView) tbRowDetail.findViewById(R.id.tvTotalPrice);
			TextView tvTotalDisc = (TextView) tbRowDetail.findViewById(R.id.tvTotalDisc);
			TextView tvSubTotal = (TextView) tbRowDetail.findViewById(R.id.tvSubTotal);
			TextView tvSc = (TextView) tbRowDetail.findViewById(R.id.tvSc);
			TextView tvTotalSale = (TextView) tbRowDetail.findViewById(R.id.tvTotalSale);
			TextView tvTotalVat = (TextView) tbRowDetail.findViewById(R.id.tvTotalVat);
			TextView tvVatable = (TextView) tbRowDetail.findViewById(R.id.tvVatable);

			tvReceiptNo.setText(reportDetail.getReceiptNo());
			tvTotalPrice.setText(format.currencyFormat(reportDetail.getTotalPrice()));
			tvTotalDisc.setText(format.currencyFormat(reportDetail.getDiscount()));
			tvSubTotal.setText(format.currencyFormat(reportDetail.getSubTotal()));
			tvSc.setText(format.currencyFormat(reportDetail.getServiceCharge()));
			tvTotalSale.setText(format.currencyFormat(reportDetail.getTotalSale()));
			tvTotalVat.setText(format.currencyFormat(reportDetail.getTotalVat()));
			tvVatable.setText(format.currencyFormat(reportDetail.getVatable()));
			
			tbReport.addView(tbRowDetail);
		}
		
		// summary
		Report.ReportDetail summary = report.getSummaryByBill();
		View tbRowSumm = inflater.inflate(R.layout.sale_report_by_bill_template, null);
		TextView tvReceiptNo = (TextView) tbRowSumm.findViewById(R.id.tvReceipt);
		TextView tvTotalPrice = (TextView) tbRowSumm.findViewById(R.id.tvTotalPrice);
		TextView tvTotalDisc = (TextView) tbRowSumm.findViewById(R.id.tvTotalDisc);
		TextView tvSubTotal = (TextView) tbRowSumm.findViewById(R.id.tvSubTotal);
		TextView tvSc = (TextView) tbRowSumm.findViewById(R.id.tvSc);
		TextView tvTotalSale = (TextView) tbRowSumm.findViewById(R.id.tvTotalSale);
		TextView tvTotalVat = (TextView) tbRowSumm.findViewById(R.id.tvTotalVat);
		TextView tvVatable = (TextView) tbRowSumm.findViewById(R.id.tvVatable);

		tvReceiptNo.setBackgroundResource(R.color.high_light_blue);
		tvTotalPrice.setBackgroundResource(R.color.high_light_blue);
		tvTotalDisc.setBackgroundResource(R.color.high_light_blue);
		tvSubTotal.setBackgroundResource(R.color.high_light_blue);
		tvSc.setBackgroundResource(R.color.high_light_blue);
		tvTotalSale.setBackgroundResource(R.color.high_light_blue);
		tvTotalVat.setBackgroundResource(R.color.high_light_blue);
		tvVatable.setBackgroundResource(R.color.high_light_blue);
		
		tvReceiptNo.setText(R.string.summary);
		tvTotalPrice.setText(format.currencyFormat(summary.getTotalPrice()));
		tvTotalDisc.setText(format.currencyFormat(summary.getDiscount()));
		tvSubTotal.setText(format.currencyFormat(summary.getSubTotal()));
		tvSc.setText(format.currencyFormat(summary.getServiceCharge()));
		tvTotalSale.setText(format.currencyFormat(summary.getTotalSale()));
		tvTotalVat.setText(format.currencyFormat(summary.getTotalVat()));
		tvVatable.setText(format.currencyFormat(summary.getVatable()));
		
		tbReport.addView(tbRowSumm);
	}
	
	private void createReportByProduct(){
		report = new Reporting(context, dateFrom, dateTo);
		List<Report> reportLst = report.getSaleReportByProduct();
		
		tbReport.removeAllViews();
		LayoutInflater inflater = LayoutInflater.from(context);

		for(Report reportData : reportLst){
			View tbRowHead = inflater.inflate(R.layout.sale_report_template, null);
			TextView tvGroupCode = (TextView) tbRowHead.findViewById(R.id.tvProCode);
			TextView tvGroupName = (TextView) tbRowHead.findViewById(R.id.tvProName);
			TextView tvGroupUnitPrice = (TextView) tbRowHead.findViewById(R.id.tvUnitPrice);
			TextView tvGroupQty = (TextView) tbRowHead.findViewById(R.id.tvQty);
			TextView tvGroupSubTotal = (TextView) tbRowHead.findViewById(R.id.tvSubTotal);
			TextView tvGroupDiscount = (TextView) tbRowHead.findViewById(R.id.tvDiscount);
			TextView tvGroupTotalPrice = (TextView) tbRowHead.findViewById(R.id.tvTotalPrice);
			TextView tvGroupVatable = (TextView) tbRowHead.findViewById(R.id.tvVat);
			
			tvGroupCode.setBackgroundResource(R.color.light_blue);
			tvGroupName.setBackgroundResource(R.color.light_blue);
			tvGroupQty.setBackgroundResource(R.color.light_blue);
			tvGroupUnitPrice.setBackgroundResource(R.color.light_blue);
			tvGroupSubTotal.setBackgroundResource(R.color.light_blue);
			tvGroupDiscount.setBackgroundResource(R.color.light_blue);
			tvGroupTotalPrice.setBackgroundResource(R.color.light_blue);
			tvGroupVatable.setBackgroundResource(R.color.light_blue);
			
			tvGroupCode.setText(R.string.product_group);
			tvGroupName.setText(reportData.getProductGroupName() + ":" + reportData.getProductDeptName());
			
			tbReport.addView(tbRowHead);
	
			for(Report.ReportDetail reportDetail : reportData.reportDetail){
				View tbRowDetail = inflater.inflate(R.layout.sale_report_template, null); 
				TextView tvCode = (TextView) tbRowDetail.findViewById(R.id.tvProCode);
				TextView tvName = (TextView) tbRowDetail.findViewById(R.id.tvProName);
				TextView tvUnitPrice = (TextView) tbRowDetail.findViewById(R.id.tvUnitPrice);
				TextView tvQty = (TextView) tbRowDetail.findViewById(R.id.tvQty);
				TextView tvSubTotal = (TextView) tbRowDetail.findViewById(R.id.tvSubTotal);
				TextView tvDiscount = (TextView) tbRowDetail.findViewById(R.id.tvDiscount);
				TextView tvTotalPrice = (TextView) tbRowDetail.findViewById(R.id.tvTotalPrice);
				TextView tvVatable = (TextView) tbRowDetail.findViewById(R.id.tvVat);
				
				tvCode.setText(reportDetail.getProductCode());
				tvName.setText(reportDetail.getProductName());
				tvUnitPrice.setText(format.currencyFormat(reportDetail.getPricePerUnit()));
				tvQty.setText(format.qtyFormat(reportDetail.getQty()));
				tvSubTotal.setText(format.currencyFormat(reportDetail.getSubTotal()));
				tvDiscount.setText(format.currencyFormat(reportDetail.getDiscount()));
				tvTotalPrice.setText(format.currencyFormat(reportDetail.getTotalPrice()));
				tvVatable.setText(reportDetail.getVat().equals("1") ? "V" : "");
				
				tbReport.addView(tbRowDetail);
				tbReport.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						
					}
				});
			}
			
			// summary dept
			Report.ReportDetail deptSumm = report.getSummaryByDept(reportData.getProductDeptId());
			View tbRowDetail = inflater.inflate(R.layout.sale_report_template, null); 
			TextView tvCode = (TextView) tbRowDetail.findViewById(R.id.tvProCode);
			TextView tvName = (TextView) tbRowDetail.findViewById(R.id.tvProName);
			TextView tvQty = (TextView) tbRowDetail.findViewById(R.id.tvQty);
			TextView tvUnitPrice = (TextView) tbRowDetail.findViewById(R.id.tvUnitPrice);
			TextView tvSubTotal = (TextView) tbRowDetail.findViewById(R.id.tvSubTotal);
			TextView tvDiscount = (TextView) tbRowDetail.findViewById(R.id.tvDiscount);
			TextView tvTotalPrice = (TextView) tbRowDetail.findViewById(R.id.tvTotalPrice);
			TextView tvVatable = (TextView) tbRowDetail.findViewById(R.id.tvVat);
			
			tvCode.setBackgroundResource(R.color.high_light_blue);
			tvName.setBackgroundResource(R.color.high_light_blue);
			tvQty.setBackgroundResource(R.color.high_light_blue);
			tvUnitPrice.setBackgroundResource(R.color.high_light_blue);
			tvSubTotal.setBackgroundResource(R.color.high_light_blue);
			tvDiscount.setBackgroundResource(R.color.high_light_blue);
			tvTotalPrice.setBackgroundResource(R.color.high_light_blue);
			tvVatable.setBackgroundResource(R.color.high_light_blue);
			
			tvCode.setText(R.string.summary);
			tvName.setText(reportData.getProductDeptName());
			tvQty.setText(format.qtyFormat(deptSumm.getQty()));
			tvSubTotal.setText(format.currencyFormat(deptSumm.getSubTotal()));
			tvDiscount.setText(format.currencyFormat(deptSumm.getDiscount()));
			tvTotalPrice.setText(format.currencyFormat(deptSumm.getTotalPrice()));
			
			tbReport.addView(tbRowDetail);
		}
	}
}
