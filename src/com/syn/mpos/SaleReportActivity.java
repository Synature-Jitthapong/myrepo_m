package com.syn.mpos;

import java.util.Calendar;

import com.syn.mpos.database.ComputerTable;
import com.syn.mpos.database.GlobalProperty;
import com.syn.mpos.database.MPOSDatabase;
import com.syn.mpos.database.MPOSSQLiteHelper;
import com.syn.mpos.database.OrderDetailTable;
import com.syn.mpos.database.OrderTransactionTable;
import com.syn.mpos.database.PayTypeTable;
import com.syn.mpos.database.PaymentDetail;
import com.syn.mpos.database.PaymentDetailTable;
import com.syn.mpos.database.Reporting;
import com.syn.mpos.database.Transaction;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
	
	private Calendar mCalendar;
	
	private long mDateFrom;
	private long mDateTo;
	private int mReportType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_sale_report);

		mSqliteHelper = new MPOSSQLiteHelper(this);
		mSqlite = mSqliteHelper.getWritableDatabase();
		
		mCalendar = Calendar.getInstance();
		mDateFrom = mCalendar.getTimeInMillis();
		mDateTo = mCalendar.getTimeInMillis();
		
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
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_sale_report, menu);
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
		
		spReportType.setAdapter(new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_dropdown_item, reportType));
		
		btnCreateReport.setText(getString(R.string.create_report));
		
		btnDateFrom.setText(GlobalProperty.dateFormat(mSqlite, mCalendar.getTime()));
		btnDateTo.setText(GlobalProperty.dateFormat(mSqlite, mCalendar.getTime()));
		
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
						
						
						btnDateFrom.setText(GlobalProperty.dateFormat(
								mSqlite, mCalendar.getTime()));
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
						
						
						btnDateTo.setText(GlobalProperty.dateFormat(
								mSqlite, mCalendar.getTime()));
					}
				});
				dialogFragment.show(getFragmentManager(), "Condition");
			}
		});
		
		btnCreateReport.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				mBillFrag.createReport(SaleReportActivity.this, mSqlite, mDateFrom, mDateTo);
			}
			
		});
		
		return true;
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
	 * create table header 
	 * @param tbLayout
	 */
	public static void createTableHeader(Context c, TableLayout tbLayout, String[] headerPrefix,
			String[] headerSubfix){

		TableRow row = new TableRow(c);
		
		// prefix
		for(String content : headerPrefix){
			row.addView(createContent(c, content));
		}
		
		// payment
		
		// subfix
		for(String content : headerSubfix){
			row.addView(createContent(c, content));
		}
		
		tbLayout.addView(row);
	}
	
	private static TextView createContent(Context c, String content){
		TextView tvContent = new TextView(c);
		tvContent.setText(content); 
		return tvContent;
	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class BillReportFragment extends Fragment {
		
		private TableLayout mHeaderContent;
		private TableLayout mDetailContent;
		
		public static BillReportFragment newInstance(){
			BillReportFragment f = new BillReportFragment();
			return f;
		}

		public void createReport(Context c, SQLiteDatabase db, long dateFrom, long dateTo){
			mDetailContent.removeAllViews();
			
			Cursor cursor = getReport(dateFrom, dateTo);
			
			if(cursor.moveToFirst()){
				do{
					TableRow.LayoutParams params = new TableRow.LayoutParams();
					params.width = LayoutParams.MATCH_PARENT;
					params.height = LayoutParams.WRAP_CONTENT;
					
					TableRow row = new TableRow(getActivity());
					
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(OrderTransactionTable.COLUMN_SALE_DATE)));
					
					row.addView(createContent(getActivity(), 
							GlobalProperty.dateFormat(db, calendar.getTime())), params); 
					row.addView(createContent(getActivity(), 
							cursor.getString(cursor.getColumnIndex(OrderTransactionTable.COLUMN_RECEIPT_NO))), params);
					row.addView(createContent(getActivity(), 
							GlobalProperty.currencyFormat(((SaleReportActivity) getActivity()).getDatabase(), 
									cursor.getDouble(cursor.getColumnIndex("TotalRetailPrice")))), params);
					row.addView(createContent(getActivity(), 
							GlobalProperty.currencyFormat(((SaleReportActivity) getActivity()).getDatabase(), 
									cursor.getDouble(cursor.getColumnIndex("TotalSalePrice")))), params);
					row.addView(createContent(getActivity(), 
							GlobalProperty.currencyFormat(((SaleReportActivity) getActivity()).getDatabase(), 
									cursor.getDouble(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT)))), params);
					row.addView(createContent(getActivity(), 
							GlobalProperty.currencyFormat(((SaleReportActivity) getActivity()).getDatabase(), 
									cursor.getDouble(cursor.getColumnIndex("TotalDiscount")))), params);
					row.addView(createContent(getActivity(), 
							GlobalProperty.currencyFormat(((SaleReportActivity) getActivity()).getDatabase(), 
									cursor.getDouble(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_VATABLE)))), params);
					row.addView(createContent(getActivity(), 
							GlobalProperty.currencyFormat(((SaleReportActivity) getActivity()).getDatabase(), 
									cursor.getDouble(cursor.getColumnIndex(OrderTransactionTable.COLUMN_TRANS_VAT)))), params);
					mDetailContent.addView(row);
				}while(cursor.moveToNext());
			}
			cursor.close();
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_sale_report,
					container, false);
			mHeaderContent = (TableLayout) rootView.findViewById(R.id.headerContent);
			mDetailContent = (TableLayout) rootView.findViewById(R.id.detailContent);
			
			String[] billHeaderColumnsPrefix = {
					getActivity().getString(R.string.date),
					getActivity().getString(R.string.bill_no),
					getActivity().getString(R.string.sale_price),
					getActivity().getString(R.string.discount),
					getActivity().getString(R.string.total_price),
					getActivity().getString(R.string.total_sale),
					getActivity().getString(R.string.before_vat),
					getActivity().getString(R.string.total_vat),
				};
				
			String[] billHeaderColumnsSubfix = {
					getActivity().getString(R.string.total_payment),
			};
			
			// create header
			createTableHeader(getActivity(), mHeaderContent, 
					billHeaderColumnsPrefix, billHeaderColumnsSubfix);
			
			return rootView;
		}
		
		private Cursor getReport(long dateFrom, long dateTo){
			String strSql = " SELECT a." + OrderTransactionTable.COLUMN_TRANSACTION_ID  + ", " +
					" a." + ComputerTable.COLUMN_COMPUTER_ID + ", " +
					" a." + OrderTransactionTable.COLUMN_SALE_DATE + ", " +
					" a." + OrderTransactionTable.COLUMN_STATUS_ID + ", " +
					" a." + OrderTransactionTable.COLUMN_RECEIPT_NO + "," +
					" a." + OrderTransactionTable.COLUMN_TRANS_EXCLUDE_VAT + ", " +
					" a." + OrderTransactionTable.COLUMN_TRANS_VAT + ", " +
					" a." + OrderTransactionTable.COLUMN_TRANS_VATABLE + ", " +
					" a." + MPOSDatabase.COLUMN_SEND_STATUS + ", " +
					" SUM(b." + OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE + " * b." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS TotalRetailPrice, " +
					" SUM(b." + OrderDetailTable.COLUMN_TOTAL_SALE_PRICE + " * b." + OrderDetailTable.COLUMN_ORDER_QTY + ") AS TotalSalePrice, " +
					" a." + OrderTransactionTable.COLUMN_OTHER_DISCOUNT + " + " + 
					" SUM(b." + OrderDetailTable.COLUMN_PRICE_DISCOUNT + " + " + 
					" b." + OrderDetailTable.COLUMN_MEMBER_DISCOUNT + ") AS TotalDiscount, " +
					"(SELECT SUM(" + PaymentDetailTable.COLUMN_PAY_AMOUNT + ") " +
					" FROM " + PaymentDetailTable.TABLE_NAME +
					" WHERE " + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=a." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
					" AND " + ComputerTable.COLUMN_COMPUTER_ID + "=a." + ComputerTable.COLUMN_COMPUTER_ID +
					" AND " + PayTypeTable.COLUMN_PAY_TYPE_ID + "=" + PaymentDetail.PAY_TYPE_CASH +
					") AS TotalCash, " +
					"(SELECT SUM(" + PaymentDetailTable.COLUMN_PAY_AMOUNT + ") " +
					" FROM " + PaymentDetailTable.TABLE_NAME +
					" WHERE " + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=a." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
					" AND " + ComputerTable.COLUMN_COMPUTER_ID + "=a." + ComputerTable.COLUMN_COMPUTER_ID +
					" AND " + PayTypeTable.COLUMN_PAY_TYPE_ID + "=" + PaymentDetail.PAY_TYPE_CREDIT +
					") AS TotalCredit " +
					" FROM " + OrderTransactionTable.TABLE_NAME + " a " +
					" INNER JOIN " + OrderDetailTable.TABLE_ORDER + " b " +
					" ON a." + OrderTransactionTable.COLUMN_TRANSACTION_ID + "=b." + OrderTransactionTable.COLUMN_TRANSACTION_ID +
					" AND a." + ComputerTable.COLUMN_COMPUTER_ID + "=b." + ComputerTable.COLUMN_COMPUTER_ID +
					" WHERE a." + OrderTransactionTable.COLUMN_STATUS_ID + " IN(?, ?) " +
					" AND a." + OrderTransactionTable.COLUMN_SALE_DATE + " BETWEEN ? AND ? " +  
					" GROUP BY a." + OrderTransactionTable.COLUMN_TRANSACTION_ID;

			return ((SaleReportActivity) getActivity()).getDatabase().rawQuery(strSql, 
					new String[]{
					String.valueOf(Transaction.TRANS_STATUS_SUCCESS),
					String.valueOf(Transaction.TRANS_STATUS_VOID),
					String.valueOf(dateFrom), 
					String.valueOf(dateTo)});
		}
	}

}
