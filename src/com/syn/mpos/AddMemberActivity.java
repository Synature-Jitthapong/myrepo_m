package com.syn.mpos;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;

public class AddMemberActivity extends Activity {

	private Context mContext;
	private Formatter mFormat; 
	private Calendar mCalendar;
	private long mDate;
	private String mMode;
	
	private TextView mTvTitle;
	private Button mBtnExpDate;
	private Button mBtnBirthDay;
	private Spinner mSpMemGroup;
	private Spinner mSpProvince;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_member);
		mContext = AddMemberActivity.this;
		
		mBtnExpDate = (Button) findViewById(R.id.btnExp);
		mBtnBirthDay = (Button) findViewById(R.id.btnBirthDay);
		mSpMemGroup = (Spinner) findViewById(R.id.spMemGroup);
		mSpProvince = (Spinner) findViewById(R.id.spProvince);
		mTvTitle = (TextView) findViewById(R.id.tvTitle);
		
		Intent intent = getIntent();
		mMode = intent.getStringExtra("mode");
		if(mMode.equals("add")){
			mTvTitle.setText(R.string.title_activity_add_member);
		}else if(mMode.equals("search")){
			mTvTitle.setText(R.string.title_search_member);
		}
		
		init();
	}
	
	private void init(){
		mFormat = new Formatter(mContext);
		mCalendar = Calendar.getInstance();
		mBtnExpDate.setText(mFormat.dateFormat(mCalendar.getTime()));
		mBtnBirthDay.setText(mFormat.dateFormat(mCalendar.getTime()));
		
		mBtnExpDate.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				DialogFragment dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
					
					@Override
					public void onSetDate(long date) {
						mCalendar.setTimeInMillis(date);
						mBtnExpDate.setText(mFormat.dateFormat(mCalendar.getTime()));
					}
				});
				dialogFragment.show(getFragmentManager(), "Condition");
			}
			
		});
		mBtnBirthDay.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				DialogFragment dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
					
					@Override
					public void onSetDate(long date) {
						mCalendar.setTimeInMillis(date);
						mBtnBirthDay.setText(mFormat.dateFormat(mCalendar.getTime()));
					}
				});
				dialogFragment.show(getFragmentManager(), "Condition");
			}
			
		});
	}
	
	public void addMemberClicked(final View v){
		finish();
	}
	
	public void cancelClicked(final View v){
		finish();
	}
}
