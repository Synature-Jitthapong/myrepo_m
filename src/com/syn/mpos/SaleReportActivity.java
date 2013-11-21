package com.syn.mpos;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import com.syn.mpos.database.Reporting;
import com.syn.pos.Report;
import android.os.Bundle;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SaleReportActivity extends Activity implements OnClickListener {
	//private static final String TAG = "SaleReportActivity"; 
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

        getActionBar().setDisplayHomeAsUpEnabled(true);
        
		tbReport = (TableLayout) findViewById(R.id.tbReport);
		trProductReportHeader = (TableRow) findViewById(R.id.tableRowByProduct);
		trBillReportHeader = (TableRow) findViewById(R.id.tableRowByBill);
		
		Intent intent = getIntent();
		mode = intent.getIntExtra("mode", 1);
		
		if(mode == 1){
			setTitle(R.string.sale_report_by_bill);
			trBillReportHeader.setVisibility(View.VISIBLE);
			trProductReportHeader.setVisibility(View.GONE);
		}else if (mode == 2){
			setTitle(R.string.sale_report_by_product);
			trBillReportHeader.setVisibility(View.GONE);
			trProductReportHeader.setVisibility(View.VISIBLE);
		}
		
		format = new Formatter(SaleReportActivity.this);
		Calendar c = Calendar.getInstance();
		calendar = new GregorianCalendar(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		
		dateFrom = calendar.getTimeInMillis();
		dateTo = calendar.getTimeInMillis();
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_sale_report, menu);
		MenuItem menuItem = (MenuItem) menu.findItem(R.id.itemDateCondition);
		btnDateFrom = (Button) menuItem.getActionView().findViewById(R.id.btnDateFrom);
		btnDateTo = (Button) menuItem.getActionView().findViewById(R.id.btnDateTo);
		btnDateFrom.setText(format.dateFormat(calendar.getTime()));
		btnDateTo.setText(format.dateFormat(calendar.getTime()));
		btnDateFrom.setOnClickListener(this);
		btnDateTo.setOnClickListener(this);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.itemCreateReport:
			createReport();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void createReportByBill(){
		report = new Reporting(SaleReportActivity.this, dateFrom, dateTo);
		Report reportData = report.getSaleReportByBill();

		tbReport.removeAllViews();
		LayoutInflater inflater = LayoutInflater.from(SaleReportActivity.this);
		
		float totalPrice = 0.0f;
		float totalDisc = 0.0f;
		float totalSub = 0.0f;
		float totalSc = 0.0f;
		float totalSale = 0.0f;
		float totalVatable = 0.0f;
		float totalVat = 0.0f;
		
		for(Report.ReportDetail reportDetail : reportData.reportDetail){
			View tbRowDetail = inflater.inflate(R.layout.sale_report_by_bill_template, null);
			TextView tvReceiptNo = (TextView) tbRowDetail.findViewById(R.id.tvReceipt);
			TextView tvTotalPrice = (TextView) tbRowDetail.findViewById(R.id.tvTotalPrice);
			TextView tvTotalDisc = (TextView) tbRowDetail.findViewById(R.id.tvTotalDisc);
			TextView tvSubTotal = (TextView) tbRowDetail.findViewById(R.id.tvSubTotal);
			TextView tvSc = (TextView) tbRowDetail.findViewById(R.id.tvSc);
			TextView tvTotalSale = (TextView) tbRowDetail.findViewById(R.id.tvTotalSale);
			TextView tvVatable = (TextView) tbRowDetail.findViewById(R.id.tvVatable);
			TextView tvTotalVat = (TextView) tbRowDetail.findViewById(R.id.tvTotalVat);

			tvReceiptNo.setText(reportDetail.getReceiptNo());
			tvReceiptNo.setSelected(true);
			
			tvTotalPrice.setText(format.currencyFormat(reportDetail.getTotalPrice()));
			tvTotalDisc.setText(format.currencyFormat(reportDetail.getDiscount()));
			tvSubTotal.setText(format.currencyFormat(reportDetail.getSubTotal()));
			tvSc.setText(format.currencyFormat(reportDetail.getServiceCharge()));
			tvTotalSale.setText(format.currencyFormat(reportDetail.getTotalSale()));
			tvVatable.setText(format.currencyFormat(reportDetail.getVatable()));
			tvTotalVat.setText(format.currencyFormat(reportDetail.getTotalVat()));
			
			tbReport.addView(tbRowDetail);
			
			totalPrice += reportDetail.getTotalPrice();
			totalDisc += reportDetail.getDiscount();
			totalSub += reportDetail.getSubTotal();
			totalSc += reportDetail.getServiceCharge();
			totalSale += reportDetail.getTotalSale();
			totalVatable += reportDetail.getVatable();
			totalVat += reportDetail.getTotalVat();
		}
		
		// summary
		View tbRowSumm = inflater.inflate(R.layout.sale_report_by_bill_template, null);
		TextView tvReceiptNo = (TextView) tbRowSumm.findViewById(R.id.tvReceipt);
		TextView tvTotalPrice = (TextView) tbRowSumm.findViewById(R.id.tvTotalPrice);
		TextView tvTotalDisc = (TextView) tbRowSumm.findViewById(R.id.tvTotalDisc);
		TextView tvSubTotal = (TextView) tbRowSumm.findViewById(R.id.tvSubTotal);
		TextView tvSc = (TextView) tbRowSumm.findViewById(R.id.tvSc);
		TextView tvTotalSale = (TextView) tbRowSumm.findViewById(R.id.tvTotalSale);
		TextView tvTotalVat = (TextView) tbRowSumm.findViewById(R.id.tvTotalVat);
		TextView tvVatable = (TextView) tbRowSumm.findViewById(R.id.tvVatable);

		tvReceiptNo.setBackgroundResource(R.color.gray_light_blue);
		tvTotalPrice.setBackgroundResource(R.color.gray_light_blue);
		tvTotalDisc.setBackgroundResource(R.color.gray_light_blue);
		tvSubTotal.setBackgroundResource(R.color.gray_light_blue);
		tvSc.setBackgroundResource(R.color.gray_light_blue);
		tvTotalSale.setBackgroundResource(R.color.gray_light_blue);
		tvTotalVat.setBackgroundResource(R.color.gray_light_blue);
		tvVatable.setBackgroundResource(R.color.gray_light_blue);
		
		tvReceiptNo.setText(R.string.total);
		tvReceiptNo.setGravity(Gravity.RIGHT);
		tvTotalPrice.setText(format.currencyFormat(totalPrice));
		tvTotalDisc.setText(format.currencyFormat(totalDisc));
		tvSubTotal.setText(format.currencyFormat(totalSub));
		tvSc.setText(format.currencyFormat(totalSc));
		tvTotalSale.setText(format.currencyFormat(totalSale));
		tvTotalVat.setText(format.currencyFormat(totalVat));
		tvVatable.setText(format.currencyFormat(totalVatable));
		
		tbReport.addView(tbRowSumm);
	}
	
	private void createReportByProduct(){
		report = new Reporting(SaleReportActivity.this, dateFrom, dateTo);
		List<Report> reportLst = report.getSaleReportByProduct();
		
		tbReport.removeAllViews();
		LayoutInflater inflater = LayoutInflater.from(SaleReportActivity.this);

		float totalQty = 0.0f;
		float totalSub = 0.0f;
		float totalDisc = 0.0f;
		float totalPrice = 0.0f;
		
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
			
			tvGroupCode.setBackgroundResource(R.color.light_gray);
			tvGroupName.setBackgroundResource(R.color.light_gray);
			tvGroupQty.setBackgroundResource(R.color.light_gray);
			tvGroupUnitPrice.setBackgroundResource(R.color.light_gray);
			tvGroupSubTotal.setBackgroundResource(R.color.light_gray);
			tvGroupDiscount.setBackgroundResource(R.color.light_gray);
			tvGroupTotalPrice.setBackgroundResource(R.color.light_gray);
			tvGroupVatable.setBackgroundResource(R.color.light_gray);
			
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
				
				totalQty += reportDetail.getQty();
				totalSub += reportDetail.getSubTotal();
				totalDisc += reportDetail.getDiscount();
				totalPrice += reportDetail.getTotalPrice();
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
			
			tvCode.setBackgroundResource(R.color.gray_light_blue);
			tvName.setBackgroundResource(R.color.gray_light_blue);
			tvQty.setBackgroundResource(R.color.gray_light_blue);
			tvUnitPrice.setBackgroundResource(R.color.gray_light_blue);
			tvSubTotal.setBackgroundResource(R.color.gray_light_blue);
			tvDiscount.setBackgroundResource(R.color.gray_light_blue);
			tvTotalPrice.setBackgroundResource(R.color.gray_light_blue);
			tvVatable.setBackgroundResource(R.color.gray_light_blue);
			
			tvCode.setText(R.string.summary);
			tvCode.setGravity(Gravity.RIGHT);
			tvName.setText(reportData.getProductDeptName());
			tvQty.setText(format.qtyFormat(deptSumm.getQty()));
			tvSubTotal.setText(format.currencyFormat(deptSumm.getSubTotal()));
			tvDiscount.setText(format.currencyFormat(deptSumm.getDiscount()));
			tvTotalPrice.setText(format.currencyFormat(deptSumm.getTotalPrice()));
			
			tbReport.addView(tbRowDetail);
		}
		
		// sum all
		View tbRowDetail = inflater.inflate(R.layout.sale_report_template, null); 
		TextView tvCode = (TextView) tbRowDetail.findViewById(R.id.tvProCode);
		TextView tvName = (TextView) tbRowDetail.findViewById(R.id.tvProName);
		TextView tvQty = (TextView) tbRowDetail.findViewById(R.id.tvQty);
		TextView tvUnitPrice = (TextView) tbRowDetail.findViewById(R.id.tvUnitPrice);
		TextView tvSubTotal = (TextView) tbRowDetail.findViewById(R.id.tvSubTotal);
		TextView tvDiscount = (TextView) tbRowDetail.findViewById(R.id.tvDiscount);
		TextView tvTotalPrice = (TextView) tbRowDetail.findViewById(R.id.tvTotalPrice);
		TextView tvVatable = (TextView) tbRowDetail.findViewById(R.id.tvVat);
		
		tvCode.setBackgroundResource(R.color.gray_light_blue);
		tvName.setBackgroundResource(R.color.gray_light_blue);
		tvQty.setBackgroundResource(R.color.gray_light_blue);
		tvUnitPrice.setBackgroundResource(R.color.gray_light_blue);
		tvSubTotal.setBackgroundResource(R.color.gray_light_blue);
		tvDiscount.setBackgroundResource(R.color.gray_light_blue);
		tvTotalPrice.setBackgroundResource(R.color.gray_light_blue);
		tvVatable.setBackgroundResource(R.color.gray_light_blue);
		
		tvName.setText(R.string.total);
		tvName.setGravity(Gravity.RIGHT);
		tvQty.setText(format.qtyFormat(totalQty));
		tvSubTotal.setText(format.currencyFormat(totalSub));
		tvDiscount.setText(format.currencyFormat(totalDisc));
		tvTotalPrice.setText(format.currencyFormat(totalPrice));
		
		tbReport.addView(tbRowDetail);
	}

	public void onDateFromClick() {
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

	public void onDateToClick() {
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

	public void createReport() {
		if (mode == 1)
			createReportByBill();
		else if (mode == 2)
			createReportByProduct();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnDateFrom:
			onDateFromClick();
			break;
		case R.id.btnDateTo:
			onDateToClick();
			break;
		}
	}
}
