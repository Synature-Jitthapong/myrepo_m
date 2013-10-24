package com.syn.mpos;

import java.util.Calendar;
import java.util.List;

import com.syn.mpos.database.Member;
import com.syn.mpos.database.Province;
import com.syn.pos.MemberGroup;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;

public class AddMemberActivity extends Activity implements{
	
	private Formatter mFormat; 
	private Calendar mCalendar;
	private Member mMember;
	private int mShopId;
	private int mStaffId;
	private String mMode;
	private int mProvince;
	private int mGroup;
	private int mGender = 1;
	private long mBirthDay;
	private long mExpDate;
	
	private EditText mTxtMemberCode;
	private EditText mTxtMemberFName;
	private EditText mTxtMemberLName;
	private EditText mTxtMemberAddr1;
	private EditText mTxtMemberAddr2;
	private EditText mTxtMemberCity;
	private EditText mTxtMemberZipCode;
	private EditText mTxtMemberMobile;
	private EditText mTxtMemberTel;
	private EditText mTxtMemberFax;
	private EditText mTxtMemberEmail;
	private EditText mTxtRemark;
	private Button mBtnSearch;
	private Button mBtnExpDate;
	private Button mBtnBirthDay;
	private RadioGroup mRdoGender;
	private Spinner mSpMemGroup;
	private Spinner mSpProvince;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_member);
		
		mBtnSearch = (Button) findViewById(R.id.btnSearch);
		mBtnExpDate = (Button) findViewById(R.id.btnExp);
		mBtnBirthDay = (Button) findViewById(R.id.btnBirthDay);
		mRdoGender = (RadioGroup) findViewById(R.id.rdoGender);
		mSpMemGroup = (Spinner) findViewById(R.id.spMemGroup);
		mSpProvince = (Spinner) findViewById(R.id.spProvince);
		mTxtMemberCode = (EditText) findViewById(R.id.txtMemberCode);
		mTxtMemberFName = (EditText) findViewById(R.id.txtFirstName);
		mTxtMemberLName = (EditText) findViewById(R.id.txtLastName);
		mTxtMemberAddr1 = (EditText) findViewById(R.id.txtAddr1);
		mTxtMemberAddr2 = (EditText) findViewById(R.id.txtAddr2);
		mTxtMemberCity = (EditText) findViewById(R.id.txtCity);
		mTxtMemberZipCode = (EditText) findViewById(R.id.txtZipCode);
		mTxtMemberMobile = (EditText) findViewById(R.id.txtMobileNo);
		mTxtMemberTel = (EditText) findViewById(R.id.txtPhoneNo);
		mTxtMemberFax = (EditText) findViewById(R.id.txtFaxNo);
		mTxtMemberEmail = (EditText) findViewById(R.id.txtEmail);
		mTxtRemark = (EditText) findViewById(R.id.txtRemark);
		
		
		Intent intent = getIntent();
		mMode = intent.getStringExtra("mode");
		mShopId = intent.getIntExtra("shopId", 0);
		mStaffId = intent.getIntExtra("staffId", 0);
		if(mMode.equals("add")){
			setTitle(R.string.title_activity_add_member);
			mBtnSearch.setVisibility(View.GONE);
		}else if(mMode.equals("search")){
			setTitle(R.string.title_search_member);
			mBtnSearch.setVisibility(View.VISIBLE);
		}
		
		init();
		setupProvince();
		setupMemberGroup();
		registerEventListener();
	}
	
	private void setupMemberGroup(){
		List<MemberGroup> mgLst = mMember.listMemberGroups(mShopId);
		
		ArrayAdapter<MemberGroup> adapter = new ArrayAdapter<MemberGroup>(
				AddMemberActivity.this, android.R.layout.simple_spinner_dropdown_item, mgLst);
		
		mSpMemGroup.setAdapter(adapter);
	}
	
	private void setupProvince(){
		Province province = new Province(AddMemberActivity.this);
		List<com.syn.pos.Province> pLst = province.listProvince(); 
		
		ArrayAdapter<com.syn.pos.Province> adapter = new ArrayAdapter<com.syn.pos.Province>(
				AddMemberActivity.this, android.R.layout.simple_spinner_dropdown_item, pLst);
		
		mSpProvince.setAdapter(adapter);
	}
	
	private void registerEventListener(){
		
		mTxtMemberCode.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				EditText txtCode = (EditText) v;
				String code = txtCode.getText().toString();
				if(!hasFocus){
					if(!mMember.checkMemberCode(code)){
						txtCode.setBackgroundColor(Color.RED);
					}
				}
			}
			
		});
		
		mSpMemGroup.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				MemberGroup mg = (MemberGroup) parent.getItemAtPosition(position);
				mGroup = mg.getMemberGroupId();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		mSpProvince.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				com.syn.pos.Province province = 
						(com.syn.pos.Province) parent.getItemAtPosition(position);
				
				mProvince = province.getProvinceId();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		mRdoGender.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RadioButton rdo;
				switch (checkedId) {
				case R.id.rdoMale:
					rdo = (RadioButton) group.findViewById(checkedId);
					if (rdo.isChecked())
						mGender = 1;
					break;
				case R.id.rdoFemale:
					rdo = (RadioButton) group.findViewById(checkedId);
					if (rdo.isChecked())
						mGender = 2;
					break;
				}
			}
			
		});
		
		mBtnExpDate.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				DialogFragment dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
					
					@Override
					public void onSetDate(long date) {
						mCalendar.setTimeInMillis(date);
						mBtnExpDate.setText(mFormat.dateFormat(mCalendar.getTime()));
						mExpDate = mCalendar.getTimeInMillis();
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
						mBirthDay = mCalendar.getTimeInMillis();
					}
				});
				dialogFragment.show(getFragmentManager(), "Condition");
			}
			
		});
	}
	
	private void init(){
		mMember = new Member(AddMemberActivity.this);
		mFormat = new Formatter(AddMemberActivity.this);
		mCalendar = Calendar.getInstance();
		mBtnExpDate.setText(mFormat.dateFormat(mCalendar.getTime()));
		mBtnBirthDay.setText(mFormat.dateFormat(mCalendar.getTime()));
		mBirthDay = mCalendar.getTimeInMillis();
		mExpDate = mCalendar.getTimeInMillis();
	}
	
	public void addMemberClicked(final View v){
		String code = mTxtMemberCode.getText().toString();
		String fName = mTxtMemberFName.getText().toString();
		String lName = mTxtMemberLName.getText().toString();
		String addr1 = mTxtMemberAddr1.getText().toString();
		String addr2 = mTxtMemberAddr2.getText().toString();
		String city = mTxtMemberCity.getText().toString();
		String zipCode = mTxtMemberZipCode.getText().toString();
		String mobile = mTxtMemberMobile.getText().toString();
		String tel = mTxtMemberTel.getText().toString();
		String fax = mTxtMemberFax.getText().toString();
		String email = mTxtMemberEmail.getText().toString();
		String remark = mTxtRemark.getText().toString();
		
		if(!code.isEmpty() && !fName.isEmpty() && !lName.isEmpty() &&
				!mobile.isEmpty()){
			if(mMember.addMember(code, fName, lName, mGender, 
					mGroup, addr1, addr2, city, mProvince, 
					zipCode, mobile, tel, fax, email, mBirthDay, 
					mExpDate, remark, mStaffId, mShopId)){
				finish();
			}
		}else{
			if(code.isEmpty())
				mTxtMemberCode.setBackgroundColor(Color.RED);
			if(fName.isEmpty())
				mTxtMemberFName.setBackgroundColor(Color.RED);
			if(lName.isEmpty())
				mTxtMemberLName.setBackgroundColor(Color.RED);
			if(mobile.isEmpty())
				mTxtMemberMobile.setBackgroundColor(Color.RED);
		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_confirm, menu);
		menu.findItem(R.id.itemClose).setVisible(false);
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.itemConfirm:
			onConfirmClick(item.getActionView());
			return true;
		case R.id.itemCancel:
			onCancelClick(item.getActionView());
			return true;
		default :
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSaveClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConfirmClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCancelClick(View v) {
		finish();
	}
}
