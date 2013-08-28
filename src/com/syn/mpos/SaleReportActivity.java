package com.syn.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import com.syn.mpos.db.Report;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
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
	private Report report;
	private long dateFrom, dateTo;
	private ArrayList<HashMap<String, String>> mGroupLst;
	
	private Button btnDateFrom;
	private Button btnDateTo;
	private TableLayout tbReport;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sale_report);
		
		tbReport = (TableLayout) findViewById(R.id.tbReport);
		btnDateFrom = (Button) findViewById(R.id.btnDateFrom);
		btnDateTo = (Button) findViewById(R.id.btnDateTo);
		
		Intent intent = getIntent();
		mode = intent.getIntExtra("mode", 1);
		
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
		report = new Report(context, dateFrom, dateTo);
		ArrayList<HashMap<String, String>> reportLst = 
				report.getSaleReportByBill();

		tbReport.removeAllViews();
		LayoutInflater inflater = LayoutInflater.from(context);
		for(HashMap<String, String> order : reportLst){
			View tbRowDetail = inflater.inflate(R.layout.sale_report_template, null); 
			TextView tvCode = (TextView) tbRowDetail.findViewById(R.id.tvProCode);
			TextView tvName = (TextView) tbRowDetail.findViewById(R.id.tvProName);
			TextView tvUnitPrice = (TextView) tbRowDetail.findViewById(R.id.tvUnitPrice);
			TextView tvQty = (TextView) tbRowDetail.findViewById(R.id.tvQty);
			TextView tvSubTotal = (TextView) tbRowDetail.findViewById(R.id.tvSubTotal);
			TextView tvDiscount = (TextView) tbRowDetail.findViewById(R.id.tvDiscount);
			TextView tvTotalPrice = (TextView) tbRowDetail.findViewById(R.id.tvTotalPrice);
			TextView tvVatable = (TextView) tbRowDetail.findViewById(R.id.tvVat);
			
			
			tvCode.setText(order.get("totalBill"));
			tvQty.setText(format.qtyFormat(Float.parseFloat(order.get("totalAmount"))));
			tvSubTotal.setText(format.currencyFormat(Float.parseFloat(order.get("totalProductPrice"))));
			tvDiscount.setText(format.currencyFormat(Float.parseFloat(order.get("totalDiscount"))));
			tvTotalPrice.setText(format.currencyFormat(Float.parseFloat(order.get("totalSalePrice"))));
			
			tbReport.addView(tbRowDetail);
		}
	}
	
	private void createReportByProduct(){
		report = new Report(context, dateFrom, dateTo);
		mGroupLst = report.getGroupOfMenu();
		
		tbReport.removeAllViews();
		
		LayoutInflater inflater = LayoutInflater.from(context);
		
		for(HashMap<String, String> mGroup : mGroupLst){
			View tbRowHead = inflater.inflate(R.layout.sale_report_template, null);
			tbRowHead.setBackgroundResource(android.R.color.holo_orange_dark);
			TextView tvGroup = (TextView) tbRowHead.findViewById(R.id.tvProName);
			TextView tvProGroup = (TextView) tbRowHead.findViewById(R.id.tvProCode);
			
			tvProGroup.setText(R.string.product_group);
			tvProGroup.setTextColor(Color.WHITE);
			tvGroup.setTextColor(Color.WHITE);
			tvGroup.setText(mGroup.get("menuGroupName") + ":" + mGroup.get("menuDeptName"));
			
			ArrayList<HashMap<String, String>> reportLst = 
					report.getSaleReportByProduct(Integer.parseInt(mGroup.get("menuDeptId")));
			
			if(reportLst.size() > 0)
				tbReport.addView(tbRowHead);
			
			for(HashMap<String, String> order : reportLst){
				View tbRowDetail = inflater.inflate(R.layout.sale_report_template, null); 
				TextView tvCode = (TextView) tbRowDetail.findViewById(R.id.tvProCode);
				TextView tvName = (TextView) tbRowDetail.findViewById(R.id.tvProName);
				TextView tvUnitPrice = (TextView) tbRowDetail.findViewById(R.id.tvUnitPrice);
				TextView tvQty = (TextView) tbRowDetail.findViewById(R.id.tvQty);
				TextView tvSubTotal = (TextView) tbRowDetail.findViewById(R.id.tvSubTotal);
				TextView tvDiscount = (TextView) tbRowDetail.findViewById(R.id.tvDiscount);
				TextView tvTotalPrice = (TextView) tbRowDetail.findViewById(R.id.tvTotalPrice);
				TextView tvVatable = (TextView) tbRowDetail.findViewById(R.id.tvVat);
				
				
				tvCode.setText(order.get("productCode"));
				tvName.setText(order.get("productName"));
				tvUnitPrice.setText(format.currencyFormat(Float.parseFloat(order.get("salePrice"))));
				tvQty.setText(format.qtyFormat(Float.parseFloat(order.get("totalAmount"))));
				tvSubTotal.setText(format.currencyFormat(Float.parseFloat(order.get("totalProductPrice"))));
				tvDiscount.setText(format.currencyFormat(Float.parseFloat(order.get("totalDiscount"))));
				tvTotalPrice.setText(format.currencyFormat(Float.parseFloat(order.get("totalSalePrice"))));
				
				tbReport.addView(tbRowDetail);
			}
		}
	}
}
