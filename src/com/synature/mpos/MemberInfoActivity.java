package com.synature.mpos;

import java.io.IOException;

import com.synature.mpos.database.MemberDao;
import com.synature.mpos.database.TransactionDao;
import com.synature.mpos.database.model.Member;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;

public class MemberInfoActivity extends Activity {

	private MemberDao mMemberDao;
	private TransactionDao mTransDao;
	
	private int mTransactionId;
	private int mMemberId;
	
	private EditText mTxtMemberCode;
	private EditText mTxtMemberFirstName;
	private EditText mTxtMemberLastName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
	            WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	    LayoutParams params = getWindow().getAttributes();
	    params.width = 590;
	    params.height= 500;
	    params.alpha = 1.0f;
	    params.dimAmount = 0.5f;
	    getWindow().setAttributes((android.view.WindowManager.LayoutParams) params); 
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_member_info);
		
		mTxtMemberCode = (EditText) findViewById(R.id.txtMemberCode);
		mTxtMemberFirstName = (EditText) findViewById(R.id.txtMemberFirstName);
		mTxtMemberLastName = (EditText) findViewById(R.id.txtMemberLastName);
		mTxtMemberCode.setOnKeyListener(mOnSearchMemberListener);
		
		Intent intent = getIntent();
		mTransactionId = intent.getIntExtra("transactionId", 0);
		mMemberDao = new MemberDao(this);
		mTransDao = new TransactionDao(this);
		
		if(mMemberDao.countMember() == 0){
			try {
				mMemberDao.loadMembers();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void searchMemberClick(final View v){
		String code = mTxtMemberCode.getText().toString();
		if(!TextUtils.isEmpty(code)){
			searchMember(code);
		}
	}
	
	private OnKeyListener mOnSearchMemberListener = new OnKeyListener(){

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(event.getAction() != KeyEvent.ACTION_DOWN)
				return true;
			
			if(keyCode == KeyEvent.KEYCODE_ENTER){
				String code = ((EditText) v).getText().toString();
				if(!TextUtils.isEmpty(code)){
					searchMember(code);
				}
			}
			return false;
		}
		
	};
	
	private void searchMember(String memberCode){
		Member m = mMemberDao.getMember(memberCode);
		if(m != null){
			mMemberId = m.getMemberId();
			mTxtMemberFirstName.setText(m.getMemberFirstName());
			mTxtMemberLastName.setText(m.getMemberLastName());
		}else{
			new AlertDialog.Builder(this)
			.setTitle("Search Member")
			.setMessage("Not found member!")
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
				}
			}).show();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.member_info, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(id == android.R.id.home){
			finish();
		}else if (id == R.id.itemConfirm) {
			if(mMemberId != 0){
				mTransDao.updateMemberTransaction(mTransactionId, mMemberId);
				Intent intent = new Intent();
				intent.putExtra("memberId", mMemberId);
				setResult(RESULT_OK, intent);
				finish();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
