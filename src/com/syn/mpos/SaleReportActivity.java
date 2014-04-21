package com.syn.mpos;

import java.util.Calendar;
import java.util.List;

import com.syn.mpos.database.GlobalPropertyDataSource;
import com.syn.mpos.database.MPOSDatabase;
import com.syn.mpos.database.MPOSSQLiteHelper;
import com.syn.mpos.database.PaymentDetailDataSource;
import com.syn.mpos.database.Reporting;
import com.syn.pos.Payment.PaymentDetail;
import com.syn.pos.Report;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SaleReportActivity extends Activity {
	
	public static final int BILL_REPORT = 1;
	public static final int PRODUCT_REPORT = 2;
	
	private MPOSSQLiteHelper mSqliteHelper;
	private SQLiteDatabase mSqlite;

	BillReportFragment mBillFrag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_sale_report);

		mSqliteHelper = new MPOSSQLiteHelper(this);
		mSqlite = mSqliteHelper.getWritableDatabase();
		
		if (savedInstanceState == null) {
			mBillFrag = BillReportFragment.newInstance();
			
			getFragmentManager().beginTransaction()
					.add(R.id.container, mBillFrag).commit();
		}
	}

	public SQLiteDatabase getDatabase(){
		return mSqlite;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		default:
		return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class BillReportFragment extends Fragment {
		
		private Calendar mCalendar;
		
		private long mDateFrom;
		private long mDateTo;
		private int mReportType;
		
		private TableLayout mHeaderContent;
		private TableLayout mDetailContent;
		
		public static BillReportFragment newInstance(){
			BillReportFragment f = new BillReportFragment();
			return f;
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);

			mCalendar = Calendar.getInstance();
			mDateFrom = mCalendar.getTimeInMillis();
			mDateTo = mCalendar.getTimeInMillis();
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			// Inflate the menu; this adds items to the action bar if it is present.
			inflater.inflate(R.menu.activity_sale_report, menu);
			MenuItem itemReportType = menu.findItem(R.id.itemReportType);
			MenuItem itemCondition = menu.findItem(R.id.itemDateCondition);
			MenuItem itemCreateReport = menu.findItem(R.id.itemCreateReport);
			
			Spinner spReportType = (Spinner) itemReportType.getActionView().findViewById(R.id.spinner1);
			final Button btnDateFrom = (Button) itemCondition.getActionView().findViewById(R.id.btnDateFrom);
			final Button btnDateTo = (Button) itemCondition.getActionView().findViewById(R.id.btnDateTo);
			Button btnCreateReport = (Button) itemCreateReport.getActionView();
			
			String[] reportType = {
					getString(R.string.sale_report_by_bill),
					getString(R.string.sale_report_by_product)
			};
			
			spReportType.setAdapter(new ArrayAdapter<String>(getActivity(), 
					android.R.layout.simple_spinner_dropdown_item, reportType));
			
			btnCreateReport.setText(getString(R.string.create_report));
			
			btnDateFrom.setText(GlobalPropertyDataSource.dateFormat(getActivityDatabase(), mCalendar.getTime()));
			btnDateTo.setText(GlobalPropertyDataSource.dateFormat(getActivityDatabase(), mCalendar.getTime()));
			
			spReportType.setOnItemSelectedListener(new OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					switch(position){
					case 0:
						mReportType = BILL_REPORT;
						break;
					case 1:
						mReportType = PRODUCT_REPORT;
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub
					
				}
				
			});
			
			btnDateFrom.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					DialogFragment dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
						
						@Override
						public void onSetDate(long date) {
							mCalendar.setTimeInMillis(date);
							mDateFrom = mCalendar.getTimeInMillis();
							
							btnDateFrom.setText(GlobalPropertyDataSource.dateFormat(
									getActivityDatabase(), mCalendar.getTime()));
						}
					});
					dialogFragment.show(getFragmentManager(), "Condition");
				}
			});
			
			btnDateTo.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					DialogFragment dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
						
						@Override
						public void onSetDate(long date) {
							mCalendar.setTimeInMillis(date);
							mDateTo = mCalendar.getTimeInMillis();
							
							btnDateTo.setText(GlobalPropertyDataSource.dateFormat(
									getActivityDatabase(), mCalendar.getTime()));
						}
					});
					dialogFragment.show(getFragmentManager(), "Condition");
				}
			});
			
			btnCreateReport.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					createReport();
				}
				
			});
			super.onCreateOptionsMenu(menu, inflater);
		}

		public void createReport(){
			mDetailContent.removeAllViews();

			Reporting reporting = new Reporting(getActivityDatabase(), mDateFrom, mDateTo);
			Report report = reporting.getSaleReportByBill();
			
			PaymentDetailDataSource payment = 
					new PaymentDetailDataSource(getActivityDatabase());
			
			if(report != null){
				for(int i = 0; i < report.reportDetail.size(); i++){
					Report.ReportDetail detail = report.reportDetail.get(i);
					
					TableRow row = new TableRow(getActivity());
					
					TextView tvReceiptNo = createContent(getActivity(), detail.getReceiptNo());
					tvReceiptNo.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
					row.addView(tvReceiptNo);
					row.addView(createContent(getActivity(), GlobalPropertyDataSource.currencyFormat(
							getActivityDatabase(), detail.getTotalPrice())));
					row.addView(createContent(getActivity(), GlobalPropertyDataSource.currencyFormat(
							getActivityDatabase(), detail.getDiscount())));
					row.addView(createContent(getActivity(), GlobalPropertyDataSource.currencyFormat(
							getActivityDatabase(), detail.getTotalPrice())));
					row.addView(createContent(getActivity(), GlobalPropertyDataSource.currencyFormat(
							getActivityDatabase(), detail.getVatable())));
					row.addView(createContent(getActivity(), GlobalPropertyDataSource.currencyFormat(
							getActivityDatabase(), detail.getTotalVat())));
					
					// pay type
					List<PaymentDetail> payTypeLst = 
							payment.listPaymentGroupByType(detail.getTransactionId(), detail.getComputerId());
					for(PaymentDetail pay : payTypeLst){
						row.addView(createContent(getActivity(), 
								GlobalPropertyDataSource.currencyFormat(getActivityDatabase(), 
										pay.getPayAmount())));
					}
					
					row.addView(createContent(getActivity(), GlobalPropertyDataSource.currencyFormat(
							getActivityDatabase(), detail.getTotalPayment())));
					mDetailContent.addView(row);
				}
			}
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_sale_report,
					container, false);
			mHeaderContent = (TableLayout) rootView.findViewById(R.id.headerContent);
			mDetailContent = (TableLayout) rootView.findViewById(R.id.bodyContent);
			
			String[] headerContent = {
					getActivity().getString(R.string.bill_no),
					getActivity().getString(R.string.sale_price),
					getActivity().getString(R.string.discount),
					getActivity().getString(R.string.total_price),
					getActivity().getString(R.string.total_sale),
					getActivity().getString(R.string.total_vat),
					getActivity().getString(R.string.total_payment)
				};
			
			// create header
			addRowToHeaderTable(getActivity(), mHeaderContent, headerContent);
			return rootView;
		}

		private SQLiteDatabase getActivityDatabase(){
			return ((SaleReportActivity) getActivity()).getDatabase();
		}
	}
	
	public static void addRowToHeaderTable(Context c, TableLayout tbLayout, String[] contents){
		TableRow row = new TableRow(c);
		for(int i = 0; i < contents.length; i++){
			String content = contents[i];
			TextView tvContent = new TextView(c);
			TableRow.LayoutParams params = 
					new TableRow.LayoutParams(
							0, TableRow.LayoutParams.WRAP_CONTENT, 1f);

			tvContent.setLayoutParams(params);
			tvContent.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER);
			tvContent.setTextAppearance(c, R.style.HeaderText);
			tvContent.setBackgroundResource(R.drawable.border);
			tvContent.setText(content); 
			row.addView(tvContent);
		}
		tbLayout.addView(row);
	}
	
	public static TextView createContent(Context c, String content){
		TextView tvContent = new TextView(c);
		TableRow.LayoutParams params = 
				new TableRow.LayoutParams(
						0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
		tvContent.setLayoutParams(params);
		tvContent.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
		tvContent.setTextAppearance(c, R.style.BodyText);
		tvContent.setBackgroundResource(R.drawable.border);
		tvContent.setText(content);
		return tvContent;
	}
}
