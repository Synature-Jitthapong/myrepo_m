package com.syn.mpos;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.syn.mpos.db.Reporting;
import com.syn.mpos.model.Report;

import android.os.Bundle;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SaleReportActivity extends Activity {
	private static final String TAG = "SaleReportActivity"; 
	private Context context;
	private int mode = 1;
	private Calendar calendar;
	private Formatter format;
	private Reporting report;
	private long dateFrom, dateTo;
	
	private Button btnDateFrom;
	private Button btnDateTo;
	private TableLayout tbReport;
	private TableRow trProductReportHeader;
	private TableRow trBillReportHeader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sale_report);
		
		tbReport = (TableLayout) findViewById(R.id.tbReport);
		trProductReportHeader = (TableRow) findViewById(R.id.tableRowByProduct);
		trBillReportHeader = (TableRow) findViewById(R.id.tableRowByBill);
		btnDateFrom = (Button) findViewById(R.id.btnDateFrom);
		btnDateTo = (Button) findViewById(R.id.btnDateTo);
		
		Intent intent = getIntent();
		mode = intent.getIntExtra("mode", 1);
		
		if(mode == 1){
			trBillReportHeader.setVisibility(View.GONE);
			trProductReportHeader.setVisibility(View.VISIBLE);
		}else if (mode == 2){
			trBillReportHeader.setVisibility(View.VISIBLE);
			trProductReportHeader.setVisibility(View.GONE);
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
	}

	public void createReportClicked(final View v){
		if(mode == 1)
			createReportByProduct();
		else if(mode == 2)
			createReportByBill();
	}
	
	private void createReportByBill(){
		report = new Reporting(context, dateFrom, dateTo);
		Report reportData = report.getSaleReportByBill();

		tbReport.removeAllViews();
		LayoutInflater inflater = LayoutInflater.from(context);
		for(Report.ReportDetail reportDetail : reportData.reportDetail){
			View tbRowDetail = inflater.inflate(R.layout.sale_report_by_bill_template, null);
			TextView tvDate = (TextView) tbRowDetail.findViewById(R.id.tvDate);
			TextView tvTotalBill = (TextView) tbRowDetail.findViewById(R.id.tvTotalBill);
			TextView tvTotalPrice = (TextView) tbRowDetail.findViewById(R.id.tvTotalPrice);
			TextView tvTotalDisc = (TextView) tbRowDetail.findViewById(R.id.tvTotalDisc);
			TextView tvSubTotal = (TextView) tbRowDetail.findViewById(R.id.tvSubTotal);
			TextView tvSc = (TextView) tbRowDetail.findViewById(R.id.tvSc);
			TextView tvTotalSale = (TextView) tbRowDetail.findViewById(R.id.tvTotalSale);
			TextView tvVatable = (TextView) tbRowDetail.findViewById(R.id.tvVatable);
			TextView tvCash = (TextView) tbRowDetail.findViewById(R.id.tvCash);
			TextView tvTotalPay = (TextView) tbRowDetail.findViewById(R.id.tvTotalPay);
			TextView tvDiff = (TextView) tbRowDetail.findViewById(R.id.tvDiff);
			
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(reportDetail.getSaleDate());
			
			tvDate.setText(format.dateFormat(c.getTime()));
			tvTotalBill.setText(format.qtyFormat(reportDetail.getTotalBill()));
			tvTotalPrice.setText(format.currencyFormat(reportDetail.getTotalPrice()));
			tvTotalDisc.setText(format.currencyFormat(reportDetail.getDiscount()));
			tvSubTotal.setText(format.currencyFormat(reportDetail.getSubTotal()));
			tvSc.setText(format.currencyFormat(reportDetail.getServiceCharge()));
			tvTotalSale.setText(format.currencyFormat(reportDetail.getTotalSale()));
			tvVatable.setText(format.currencyFormat(reportDetail.getVatable()));
			tvCash.setText(format.currencyFormat(reportDetail.getCash()));
			tvTotalPay.setText(format.currencyFormat(reportDetail.getTotalPayment()));
			tvDiff.setText(format.currencyFormat(reportDetail.getTotalPrice() - reportDetail.getSubTotal()));
			
			tbReport.addView(tbRowDetail);
		}
	}
	
	private void createReportByProduct(){
		report = new Reporting(context, dateFrom, dateTo);
		List<Report> reportLst = report.getSaleReportByProduct();
		
		tbReport.removeAllViews();
		LayoutInflater inflater = LayoutInflater.from(context);

		for(Report report : reportLst){
			View tbRowHead = inflater.inflate(R.layout.sale_report_template, null);
			tbRowHead.setBackgroundResource(android.R.color.holo_orange_dark);
			TextView tvGroup = (TextView) tbRowHead.findViewById(R.id.tvProName);
			TextView tvProGroup = (TextView) tbRowHead.findViewById(R.id.tvProCode);
			
			tvProGroup.setText(R.string.product_group);
			tvProGroup.setTextColor(Color.WHITE);
			tvGroup.setTextColor(Color.WHITE);
			tvGroup.setText(report.getProductGroupName() + ":" + report.getProductDeptName());
			
			tbReport.addView(tbRowHead);
	
			for(Report.ReportDetail reportDetail : report.reportDetail){
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
				tvUnitPrice.setText(format.currencyFormat(reportDetail.getProductPrice()));
				tvQty.setText(format.qtyFormat(reportDetail.getProductAmount()));
				tvSubTotal.setText(format.currencyFormat(reportDetail.getSubTotal()));
				tvDiscount.setText(format.currencyFormat(reportDetail.getDiscount()));
				tvTotalPrice.setText(format.currencyFormat(reportDetail.getTotalPrice()));
				
				tbReport.addView(tbRowDetail);
			}
		}
	}
}
