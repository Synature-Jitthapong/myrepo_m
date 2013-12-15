package com.syn.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import com.syn.mpos.database.Reporting;
import com.syn.mpos.database.transaction.PaymentDetail;
import com.syn.pos.Report;

import android.os.Bundle;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class SaleReportActivity extends Activity implements OnClickListener{
	//private static final String TAG = "SaleReportActivity";	 
	private Reporting mReporting;
	private Report mReport;
	private BillReportAdapter mReportAdapter;
	private Calendar mCalendar;
	private long mDateFrom;
	private long mDateTo;
	private MenuItem mConditionItem;
	private Button mBtnDateFrom;
	private Button mBtnDateTo;
	private ListView mLvReport;
	private MenuItem mReportTypeItem;
	private Spinner mSpReportType;
	private LinearLayout mBillHeader;
	private LinearLayout mProductHeader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sale_report);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		mBillHeader = (LinearLayout) findViewById(R.id.billHeader);
		mProductHeader = (LinearLayout) findViewById(R.id.productHeader);
		mLvReport = (ListView) findViewById(R.id.lvReport); 
		
		Calendar c = Calendar.getInstance();
		mCalendar = new GregorianCalendar(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		mDateFrom = mCalendar.getTimeInMillis();
		mDateTo = mCalendar.getTimeInMillis();
		
		getReportData();
	}
	
	private void getReportData(){
		mReporting = new Reporting(this, mDateFrom, mDateTo);
		mReport = mReporting.getSaleReportByBill();
		mReportAdapter = new BillReportAdapter();
		mLvReport.setAdapter(mReportAdapter);
	}
	
	private class ReportTypeAdapter extends BaseAdapter{
		private List<HashMap<Integer, String>> reportType;
		
		public ReportTypeAdapter(){
			reportType = new ArrayList<HashMap<Integer, String>>();

			HashMap<Integer, String> type = new HashMap<Integer, String>();
			type.put(1, SaleReportActivity.this.getString(R.string.sale_report_by_bill));
			reportType.add(type);
			type = new HashMap<Integer, String>();
			type.put(2, SaleReportActivity.this.getString(R.string.sale_report_by_product));
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
					SaleReportActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			convertView = inflater.inflate(R.layout.text_view, null);
			TextView textView = (TextView)convertView;
			textView.setText(type.get(position + 1));
			return convertView;
		}
		
	}
	
	private void switchReportType(int type){
		switch(type){
		case 1:	
			mBillHeader.setVisibility(View.VISIBLE);
			mProductHeader.setVisibility(View.GONE);
			break;
		case 2:
			mBillHeader.setVisibility(View.GONE);
			mProductHeader.setVisibility(View.VISIBLE);
			break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_sale_report, menu);
		mReportTypeItem = (MenuItem) menu.findItem(R.id.itemReportType);
		mConditionItem = (MenuItem) menu.findItem(R.id.itemDateCondition);
		mSpReportType = (Spinner) mReportTypeItem.getActionView().findViewById(R.id.spinner1);
		mBtnDateFrom = (Button) mConditionItem.getActionView().findViewById(R.id.btnDateFrom);
		mBtnDateTo = (Button) mConditionItem.getActionView().findViewById(R.id.btnDateTo);
		mBtnDateFrom.setText(MPOSApplication.sGlobalVar.dateFormat(mCalendar.getTime()));
		mBtnDateTo.setText(MPOSApplication.sGlobalVar.dateFormat(mCalendar.getTime()));
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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.itemCreateReport:
			getReportData();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	private class BillReportAdapter extends BaseAdapter{
		private PaymentDetail mPaymentDetail;
		
		public BillReportAdapter(){
			mPaymentDetail = new PaymentDetail(SaleReportActivity.this);
		}
		
		@Override
		public int getCount() {
			return mReport.reportDetail != null ? mReport.reportDetail.size() : 0;
		}

		@Override
		public Report.ReportDetail getItem(int position) {
			return mReport.reportDetail.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater)
					SaleReportActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ViewHolder holder;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.sale_report_by_bill_template, null);
				holder = new ViewHolder();
				holder.mTvReceipt = (TextView) convertView.findViewById(R.id.tvReceipt);
				holder.mTvTotalPrice = (TextView) convertView.findViewById(R.id.tvTotalPrice);
				holder.mTvDiscount = (TextView) convertView.findViewById(R.id.tvTotalDisc);
				holder.mTvSubTotal = (TextView) convertView.findViewById(R.id.tvSubTotal);
				holder.mTvTotalSale = (TextView) convertView.findViewById(R.id.tvTotalSale);
				holder.mTvVatable = (TextView) convertView.findViewById(R.id.tvVatable);
				holder.mTvTotalVat = (TextView) convertView.findViewById(R.id.tvTotalVat);
				holder.mTvTotalPayment = (TextView) convertView.findViewById(R.id.tvTotalPayment);
				convertView.setTag(holder);	
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			Report.ReportDetail reportDetail = mReport.reportDetail.get(position);
			
			float vatable = reportDetail.getVatable();
			float totalVat = reportDetail.getTotalVat();
			float totalPrice = reportDetail.getTotalPrice();
			float totalDiscount = reportDetail.getDiscount();
			float subTotal = reportDetail.getSubTotal();
			
			holder.mTvReceipt.setText(reportDetail.getReceiptNo());
			holder.mTvTotalPrice.setText(MPOSApplication.sGlobalVar.currencyFormat(totalPrice));
			holder.mTvDiscount.setText(MPOSApplication.sGlobalVar.currencyFormat(totalDiscount));
			holder.mTvSubTotal.setText(MPOSApplication.sGlobalVar.currencyFormat(subTotal));
			holder.mTvVatable.setText(MPOSApplication.sGlobalVar.currencyFormat(vatable));
			holder.mTvTotalVat.setText(MPOSApplication.sGlobalVar.currencyFormat(totalVat));
			holder.mTvTotalPayment.setText(MPOSApplication.sGlobalVar.currencyFormat(
					mPaymentDetail.getTotalPaid(reportDetail.getTransactionId(), 
							reportDetail.getComputerId())));
			
			return convertView;
		}
		
	}
	
	public static class ViewHolder{
		public TextView mTvReceipt;
		public TextView mTvTotalPrice;
		public TextView mTvDiscount;
		public TextView mTvSubTotal;
		public TextView mTvTotalSale;
		public TextView mTvVatable;
		public TextView mTvTotalVat;
		public TextView mTvTotalPayment;
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
					
					mBtnDateFrom.setText(MPOSApplication.sGlobalVar.dateFormat(mCalendar.getTime()));
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
					
					mBtnDateTo.setText(MPOSApplication.sGlobalVar.dateFormat(mCalendar.getTime()));
				}
			});
			dialogFragment.show(getFragmentManager(), "Condition");
			break;
		}
	}
}
