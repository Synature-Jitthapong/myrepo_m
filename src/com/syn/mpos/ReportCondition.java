package com.syn.mpos;

import java.util.Calendar;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.Spinner;

public class ReportCondition extends Fragment{
	private OnConditionSelectedListener mCallback;
	
	public static final int CURR_YEAR = Calendar.getInstance().get(Calendar.YEAR);
	public static final String[] DAYS = {"1","2","3","4","5","6","7","8","9","10",
		"11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"};
	public static final String[] MONTHS = {"1","2","3","4","5","6","7","8","9","10","11","12"};
	public static final String[] YEARS = {String.valueOf(CURR_YEAR)};
	
	private int mYear;
	private int mMonth;
	private int mDay;
	private int mYearTo;
	private int mMonthTo;
	private int mDayTo;
	
	private RadioButton mRdoDay;
	private RadioButton mRdoMonth;
	private RadioButton mRdoDate;
	private Spinner mSpDay;
	private Spinner mSpMonth;
	private Spinner mSpYear;
	private Spinner mSpMmonth;
	private Spinner mSpMyear;
	private Spinner mSpDayFrom;
	private Spinner mSpMonthFrom;
	private Spinner mSpYearFrom;
	private Spinner mSpDayTo;
	private Spinner mSpMonthTo;
	private Spinner mSpYearTo;
	private Button mBtnOk;

	private void setYearAdapter(){
		ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(getActivity(), 
				android.R.layout.simple_spinner_dropdown_item, YEARS);
		mSpYear.setAdapter(yearAdapter);
		mSpMyear.setAdapter(yearAdapter);
		mSpYearFrom.setAdapter(yearAdapter);
		mSpYearTo.setAdapter(yearAdapter);
	}
	
	private void setMonthAdapter(){
		ArrayAdapter<String> monthAdapter = new ArrayAdapter<String>(getActivity(), 
				android.R.layout.simple_spinner_dropdown_item, MONTHS);
		mSpMonth.setAdapter(monthAdapter);
		mSpMmonth.setAdapter(monthAdapter);
		mSpMonthFrom.setAdapter(monthAdapter);
		mSpMonthTo.setAdapter(monthAdapter);
		
		mSpMonth.setSelection(mMonth-1);
		
	}
	
	private void setDayAdapter(){
		ArrayAdapter<String> dayAdapter = new ArrayAdapter<String>(getActivity(), 
				android.R.layout.simple_spinner_dropdown_item, DAYS);
		mSpDay.setAdapter(dayAdapter);
		mSpDayFrom.setAdapter(dayAdapter);
		mSpDayTo.setAdapter(dayAdapter);
		
		mSpDay.setSelection(mDay);
		mSpDayFrom.setSelection(0);
		mSpDayTo.setSelection(mDay-1);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof OnConditionSelectedListener){
			mCallback = (OnConditionSelectedListener) activity;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		mDayTo = mDay;
		mMonthTo = mMonth;
		mYearTo = mYear;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.report_condition, container, false);

		mRdoDay = (RadioButton) v.findViewById(R.id.rdoDay);
		mRdoMonth = (RadioButton) v.findViewById(R.id.rdoMonth);
		mRdoDate = (RadioButton) v.findViewById(R.id.rdoDate);
		mBtnOk = (Button) v.findViewById(R.id.btnOk);
		mSpDay = (Spinner) v.findViewById(R.id.spDay);
		mSpMonth = (Spinner) v.findViewById(R.id.spMonth);
		mSpYear = (Spinner) v.findViewById(R.id.spYear);
		mSpMmonth = (Spinner) v.findViewById(R.id.spMmonth);
		mSpMyear = (Spinner) v.findViewById(R.id.spMyear);
		mSpDayFrom = (Spinner) v.findViewById(R.id.spDayFrom);
		mSpMonthFrom = (Spinner) v.findViewById(R.id.spMonthFrom);
		mSpYearFrom = (Spinner) v.findViewById(R.id.spYearFrom);
		mSpDayTo = (Spinner) v.findViewById(R.id.spDayTo);
		mSpMonthTo = (Spinner) v.findViewById(R.id.spMonthTo);
		mSpYearTo = (Spinner) v.findViewById(R.id.spYearTo);
		
		mRdoDay.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				
			}
			
		});
		mRdoMonth.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				
			}
			
		});
		mRdoDate.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				
			}
			
		});
		mBtnOk.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(mRdoDay.isChecked()){
					mCallback.onDaySelected(mYear, mMonth, mDay);
				}else if(mRdoMonth.isChecked()){
					mCallback.onMonthSelected(mYear, mMonth);
				}else if(mRdoDate.isChecked()){
					mCallback.onDateSelected(mYear, mMonth, mDay, mYearTo, mMonthTo, mDayTo);
				}
			}
			
		});
		mSpDay.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		mSpMonth.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		mSpYear.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		mSpMmonth.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		mSpMyear.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		mSpDayFrom.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		mSpMonthFrom.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		mSpYearFrom.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		mSpDayTo.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		mSpMonthTo.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		mSpYearTo.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		setDayAdapter();
		setMonthAdapter();
		setYearAdapter();
		return v;
	}

	public static interface OnConditionSelectedListener{
		void onDaySelected(int year, int month, int day);
		void onMonthSelected(int year, int month);
		void onDateSelected(int yearFrom, int monthFrom, 
				int dayFrom, int yearTo, int monthTo, int dayTo);
	}
	
//	@Override
//	public void onItemSelected(AdapterView<?> parent, View v, int position,
//			long id) {
//		int value = (Integer) parent.getItemAtPosition(position);
//		
//		switch(v.getId()){
//		case R.id.spDay:
//			mDay = value;
//			break;
//		case R.id.spMonth:
//			mMonth = value;
//			break;
//		case R.id.spYear:
//			mYear = value;
//			break;
//		case R.id.spMmonth:
//			mMonth = value;
//			break;
//		case R.id.spMyear:
//			mYear = value;
//			break;
//		case R.id.spDayFrom:
//			mDay = value;
//			break;
//		case R.id.spMonthFrom:
//			mMonth = value;
//			break;
//		case R.id.spYearFrom:
//			mYear = value;
//			break;
//		case R.id.spDayTo:
//			mDayTo = value;
//			break;
//		case R.id.spMonthTo:
//			mMonthTo = value;
//			break;
//		case R.id.spYearTo:
//			mYearTo = value;
//			break;
//		}
//	}
//
//	@Override
//	public void onNothingSelected(AdapterView<?> parent) {
//		
//	}
//
//	@Override
//	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//		// TODO Auto-generated method stub
//		
//	}
}
