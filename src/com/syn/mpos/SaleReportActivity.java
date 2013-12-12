package com.syn.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import com.syn.mpos.database.Reporting;
import com.syn.pos.Report;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SaleReportActivity extends Activity implements OnClickListener {
	//private static final String TAG = "SaleReportActivity";	 
	private Calendar mCalendar;
	private Formatter mFormat;
	private Reporting mReport;
	private long mDateFrom, mDateTo;
	
	private MenuItem mConditionItem;
	private MenuItem mReportTypeItem;
	private Spinner mSpReportType;
	private Button mBtnDateFrom;
	private Button mBtnDateTo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sale_report);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        
		mFormat = new Formatter(SaleReportActivity.this);
		Calendar c = Calendar.getInstance();
		mCalendar = new GregorianCalendar(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		
		mDateFrom = mCalendar.getTimeInMillis();
		mDateTo = mCalendar.getTimeInMillis();
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
	
	private void setReportTypeAdapter(){
		mSpReportType.setAdapter(new ReportTypeAdapter());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_sale_report, menu);
		mConditionItem = (MenuItem) menu.findItem(R.id.itemDateCondition);
		mReportTypeItem = (MenuItem) menu.findItem(R.id.itemReportType);
		mSpReportType = (Spinner) mReportTypeItem.getActionView().findViewById(R.id.spinner1);
		mBtnDateFrom = (Button) mConditionItem.getActionView().findViewById(R.id.btnDateFrom);
		mBtnDateTo = (Button) mConditionItem.getActionView().findViewById(R.id.btnDateTo);
		mBtnDateFrom.setText(mFormat.dateFormat(mCalendar.getTime()));
		mBtnDateTo.setText(mFormat.dateFormat(mCalendar.getTime()));
		mBtnDateFrom.setOnClickListener(this);
		mBtnDateTo.setOnClickListener(this);
		
		setReportTypeAdapter();
		
		return super.onCreateOptionsMenu(menu);
	}

	private static class BillReportFragment extends Fragment{
		
	}
	
	private static class ProductReportFragment extends Fragment{
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.itemCreateReport:
			
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onDateFromClick() {
		DialogFragment dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
			
			@Override
			public void onSetDate(long date) {
				mCalendar.setTimeInMillis(date);
				mDateFrom = mCalendar.getTimeInMillis();
				
				mBtnDateFrom.setText(mFormat.dateFormat(mCalendar.getTime()));
			}
		});
		dialogFragment.show(getFragmentManager(), "Condition");
	}

	public void onDateToClick() {
		DialogFragment dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
			
			@Override
			public void onSetDate(long date) {
				mCalendar.setTimeInMillis(date);
				mDateTo = mCalendar.getTimeInMillis();
				
				mBtnDateTo.setText(mFormat.dateFormat(mCalendar.getTime()));
			}
		});
		dialogFragment.show(getFragmentManager(), "Condition");
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
