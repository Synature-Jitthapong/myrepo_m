package com.syn.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import com.syn.mpos.datasource.PaymentDetail;
import com.syn.mpos.datasource.Reporting;
import com.syn.pos.Payment;
import com.syn.pos.Report;

import android.os.Bundle;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class SaleReportHtmlActivity extends Activity implements OnClickListener{
	private MenuItem mConditionItem;
	private Button mBtnDateFrom;
	private Button mBtnDateTo;
	private Button mBtnCreateReport;
	private MenuItem mReportTypeItem;
	private Spinner mSpReportType;
	private Calendar mCalendar;
	private long mDateFrom;
	private long mDateTo;
	private Report mReport;
	private Reporting mReporting;
	private WebView mWebView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sale_report_html);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mWebView = (WebView) findViewById(R.id.webView1);
		
		Calendar c = Calendar.getInstance();
		mCalendar = new GregorianCalendar(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		mDateFrom = mCalendar.getTimeInMillis();
		mDateTo = mCalendar.getTimeInMillis();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_sale_report, menu);
		mReportTypeItem = (MenuItem) menu.findItem(R.id.itemReportType);
		mConditionItem = (MenuItem) menu.findItem(R.id.itemDateCondition);
		mSpReportType = (Spinner) mReportTypeItem.getActionView().findViewById(R.id.spinner1);
		mBtnDateFrom = (Button) mConditionItem.getActionView().findViewById(R.id.btnDateFrom);
		mBtnDateTo = (Button) mConditionItem.getActionView().findViewById(R.id.btnDateTo);
		mBtnCreateReport = (Button) ((MenuItem) menu.findItem(R.id.itemCreateReport)).getActionView().findViewById(R.id.btnAction);
		mBtnCreateReport.setText(R.string.create_report);
		mBtnDateFrom.setText(MPOSApplication.getGlobalProperty().dateFormat(mCalendar.getTime()));
		mBtnDateTo.setText(MPOSApplication.getGlobalProperty().dateFormat(mCalendar.getTime()));
		mBtnDateFrom.setOnClickListener(this);
		mBtnDateTo.setOnClickListener(this);
		mSpReportType.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				switchReportType(position + 1);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		mSpReportType.setAdapter(new ReportTypeAdapter());
		mSpReportType.setSelection(0);
		
		return super.onCreateOptionsMenu(menu);
	}
	private class ReportTypeAdapter extends BaseAdapter{
		private List<HashMap<Integer, String>> reportType;
		
		public ReportTypeAdapter(){
			reportType = new ArrayList<HashMap<Integer, String>>();

			HashMap<Integer, String> type = new HashMap<Integer, String>();
			type.put(1, SaleReportHtmlActivity.this.getString(R.string.sale_report_by_bill));
			reportType.add(type);
			type = new HashMap<Integer, String>();
			type.put(2, SaleReportHtmlActivity.this.getString(R.string.sale_report_by_product));
			reportType.add(type);
		}
		
		@Override
		public int getCount() {
			return reportType.size();
		}

		@Override
		public HashMap<Integer, String> getItem(int position) {
			return reportType.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			HashMap<Integer, String> type = reportType.get(position);
			LayoutInflater inflater = (LayoutInflater)
					SaleReportHtmlActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			convertView = inflater.inflate(R.layout.text_view, null);
			TextView textView = (TextView)convertView;
			textView.setText(type.get(position + 1));
			return convertView;
		}
		
	}
	
	private void switchReportType(int type){
		switch(type){
		case 1:
			mBtnCreateReport.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					mReporting = new Reporting(MPOSApplication.getWriteDatabase(), mDateFrom, mDateTo);
					mReport = mReporting.getSaleReportByBill();
					createSaleHtml();
				}
				
			});
			break;
		case 2:
			mBtnCreateReport.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					
				}
				
			});			
			break;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void createSaleHtml(){
		if(mReport.reportDetail == null)
			return;
		
		PaymentDetail payment = new PaymentDetail(MPOSApplication.getWriteDatabase());
		StringBuilder strHtml = new StringBuilder();
		strHtml.append("<table width=\"100%\" style=\"border:1px solid black;border-collapse:collapse;\">");
		for(Report.ReportDetail report : mReport.reportDetail){
			strHtml.append("<tr>");
			strHtml.append("<td style=\"font-size:medium;border:1px solid black;border-collapse:collapse;\">" + report.getReceiptNo() + "</td>");
			strHtml.append("<td style=\"font-size:medium;text-align:right;border:1px solid black;border-collapse:collapse;\">" + report.getTotalPrice() + "</td>");
			strHtml.append("<td style=\"font-size:medium;text-align:right;border:1px solid black;border-collapse:collapse;\">" + report.getDiscount() + "</td>");
			strHtml.append("<td style=\"font-size:medium;text-align:right;border:1px solid black;border-collapse:collapse;\">" + report.getVatable() + "</td>");
			strHtml.append("<td style=\"font-size:medium;text-align:right;border:1px solid black;border-collapse:collapse;\">" + report.getTotalVat() + "</td>");
			
			List<Payment.PaymentDetail> payLst = 
					payment.listPaymentGroupByType(report.getTransactionId(), report.getComputerId());
			for(Payment.PaymentDetail payType : payLst){
				strHtml.append("<td style=\"font-size:medium; text-align:right;border:1px solid black;border-collapse:collapse;\">" + payType.getPayAmount() + "</td>");
			}
			strHtml.append("</tr>");
		}
		strHtml.append("</table>");
		mWebView.loadData(strHtml.toString(), "text/html", "utf-8");
	}

	@Override
	public void onClick(View v) {
		DialogFragment dialogFragment;
		
		switch(v.getId()){
		case R.id.btnDateFrom:
			dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
				
				@Override
				public void onSetDate(long date) {
					mCalendar.setTimeInMillis(date);
					mDateFrom = mCalendar.getTimeInMillis();
					
					mBtnDateFrom.setText(MPOSApplication.getGlobalProperty().dateFormat(mCalendar.getTime()));
				}
			});
			dialogFragment.show(getFragmentManager(), "Condition");
			break;
		case R.id.btnDateTo:
			dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
				
				@Override
				public void onSetDate(long date) {
					mCalendar.setTimeInMillis(date);
					mDateTo = mCalendar.getTimeInMillis();
					
					mBtnDateTo.setText(MPOSApplication.getGlobalProperty().dateFormat(mCalendar.getTime()));
				}
			});
			dialogFragment.show(getFragmentManager(), "Condition");
			break;
		}
	}
}
