package com.syn.mpos;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

public class DateCondition extends AlertDialog.Builder{
	private OnCondClickListener condListener;
	private View condView;
	private RadioGroup rdoGroup;
	private RadioButton rdoYearOnly;
	private RadioButton rdoMonthYear;
	private RadioButton rdoDurTime;
	private Spinner spinnerYearOnly;
	private Spinner spinnerMonth;
	private Spinner spinnerYear;
	private Spinner spinnerDayFrom;
	private Spinner spinnerMonthFrom;
	private Spinner spinnerYearFrom;
	private Spinner spinnerDayTo;
	private Spinner spinnerMonthTo;
	private Spinner spinnerYearTo;
	
	private long dateFrom;
	private long dateTo;
	
	public DateCondition(Context context, OnCondClickListener listener) {
		super(context);
		this.condListener = listener;
		
		LayoutInflater inflater = LayoutInflater.from(super.getContext());
		condView = inflater.inflate(R.layout.sale_report_condition, null);
		rdoGroup = (RadioGroup) condView.findViewById(R.id.radioGroup1);
		rdoYearOnly = (RadioButton) condView.findViewById(R.id.rdoYearOnly);
		rdoMonthYear = (RadioButton) condView.findViewById(R.id.rdoMonthYear);
		rdoDurTime = (RadioButton) condView.findViewById(R.id.rdoDurTime);
		spinnerYearOnly = (Spinner) condView.findViewById(R.id.spinnerYearOnly);
		spinnerMonth = (Spinner) condView.findViewById(R.id.spinnerMonth);
		spinnerYear = (Spinner) condView.findViewById(R.id.spinnerYear);
		spinnerDayFrom = (Spinner) condView.findViewById(R.id.spinnerDayFrom);
		spinnerMonthFrom = (Spinner) condView.findViewById(R.id.spinnerMonthFrom);
		spinnerYearFrom = (Spinner) condView.findViewById(R.id.spinnerYearFrom);
		spinnerDayTo = (Spinner) condView.findViewById(R.id.spinnerDayTo);
		spinnerMonthTo = (Spinner) condView.findViewById(R.id.spinnerMonthTo);
		spinnerYearTo = (Spinner) condView.findViewById(R.id.spinnerYearTo);
		
		switch(rdoGroup.getCheckedRadioButtonId()){
		case R.id.rdoYearOnly:
			rdoYearOnly.setChecked(true);
			rdoMonthYear.setChecked(false);
			rdoDurTime.setChecked(false);
			break;
		case R.id.rdoMonthYear:
			rdoYearOnly.setChecked(false);
			rdoMonthYear.setChecked(true);
			rdoDurTime.setChecked(false);
			break;
			
		case R.id.rdoDurTime:
			rdoYearOnly.setChecked(false);
			rdoMonthYear.setChecked(false);
			rdoDurTime.setChecked(true);
			break;
		default:
			rdoYearOnly.setChecked(true);
			rdoMonthYear.setChecked(false);
			rdoDurTime.setChecked(false);
			break;
		}
		
		createDay();
		createMonth();
		createYear();
	}

	private void createDay(){
		List<SpinnVal> spinnLst = 
				new ArrayList<SpinnVal>();
		
		for(int i = 1; i <= 31; i++){
			SpinnVal spinnVal = new SpinnVal();
			spinnVal.setVal(i);
			spinnVal.setName(Integer.toString(i));
			spinnLst.add(spinnVal);
		}
		
		ArrayAdapter<SpinnVal> adapter = createAdapter(spinnLst);
		
		spinnerDayFrom.setAdapter(adapter);
		spinnerDayTo.setAdapter(adapter);
	}
	
	private void createMonth(){
		List<SpinnVal> spinnLst = 
				new ArrayList<SpinnVal>();
		
		String[] months = new DateFormatSymbols(Locale.getDefault()).getMonths();
		for(int i = 0; i < months.length; i++){
			SpinnVal spinnVal = new SpinnVal();
			spinnVal.setName(months[i]);
			spinnVal.setVal(i + 1);
			spinnLst.add(spinnVal);
		}

		ArrayAdapter<SpinnVal> adapter = createAdapter(spinnLst);
		
		spinnerMonth.setAdapter(adapter);
		spinnerMonthFrom.setAdapter(adapter);
		spinnerMonthTo.setAdapter(adapter);
	}
	
	private void createYear(){
		List<SpinnVal> spinnLst = 
				new ArrayList<SpinnVal>();
		
		int currYear = Calendar.getInstance(Locale.getDefault()).get(Calendar.YEAR);
		int strYear = currYear - 3;
		
		for(int i = strYear; i <= currYear; i++){
			SpinnVal spinnVal = new SpinnVal();
			spinnVal.setName(Integer.toString(i));
			spinnVal.setVal(i);
			spinnLst.add(spinnVal);
		}
		
		ArrayAdapter<SpinnVal> adapter = createAdapter(spinnLst);
		
		spinnerYearOnly.setAdapter(adapter);
		spinnerYear.setAdapter(adapter);
		spinnerYearFrom.setAdapter(adapter);
		spinnerYearTo.setAdapter(adapter);
	}
	
	private ArrayAdapter<SpinnVal> createAdapter(List<SpinnVal> spinnLst){
		return new ArrayAdapter<SpinnVal>(super.getContext(), 
						android.R.layout.simple_dropdown_item_1line, spinnLst);
	}
	
	@Override
	public Builder setView(View view) {
		if(view == null)
			view = condView; 
		return super.setView(view);
	}

	@Override
	public Builder setPositiveButton(int textId, OnClickListener listener) {
		condListener.onNegativeClick(dateFrom, dateTo);
		return super.setPositiveButton(textId, listener);
	}
	
	public static interface OnCondClickListener{
		void onNegativeClick(long dateFrom, long dateTo);
	}
	
	private class SpinnVal{
		private String name;
		private int val;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getVal() {
			return val;
		}
		public void setVal(int val) {
			this.val = val;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
}
