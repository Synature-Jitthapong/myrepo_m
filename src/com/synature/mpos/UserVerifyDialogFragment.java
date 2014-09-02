package com.synature.mpos;

import com.synature.mpos.database.Staffs;
import com.synature.mpos.database.UserVerification;
import com.synature.pos.Staff;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class UserVerifyDialogFragment extends DialogFragment{
	
	private int mPermissionId;
	private OnCheckPermissionListener mListener;
	
	private TextView mTvMsg;
	private EditText mTxtStaffCode;
	private EditText mTxtPass;
	
	public static UserVerifyDialogFragment newInstance(int permissId){
		UserVerifyDialogFragment f = new UserVerifyDialogFragment();
		Bundle b = new Bundle();
		b.putInt("permissId", permissId);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof OnCheckPermissionListener){
			mListener = (OnCheckPermissionListener) activity;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mPermissionId = getArguments().getInt("permissId");
		super.onCreate(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View content = inflater.inflate(R.layout.user_verify_layout, null);
		mTvMsg = (TextView) content.findViewById(R.id.textView1);
		mTxtStaffCode = (EditText) content.findViewById(R.id.txtStaffCode);
		mTxtPass = (EditText) content.findViewById(R.id.txtStaffPass);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setCancelable(false);
		builder.setTitle(R.string.permission_required);
		builder.setView(content);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		builder.setPositiveButton(android.R.string.ok, null);
		final AlertDialog d = builder.show();
		d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String code = mTxtStaffCode.getText().toString();
				String pass = mTxtPass.getText().toString();
				if(!TextUtils.isEmpty(code)){
					if(!TextUtils.isEmpty(pass)){
						UserVerification verify = new UserVerification(getActivity(), code, pass);
						if(verify.checkUser()){
							Staff s = verify.checkLogin();
							if(s != null){
								mTvMsg.setVisibility(View.GONE);
								Staffs st = new Staffs(getActivity());
								switch(mPermissionId){
								case Staffs.VOID_PERMISSION:
									if(st.checkVoidPermission(s.getStaffRoleID())){
										d.dismiss();
										mListener.onAllow(Staffs.VOID_PERMISSION);
									}else{
										mTvMsg.setVisibility(View.VISIBLE);
										mTvMsg.setText(R.string.not_have_permission_to_void);
									}
									break;
								case Staffs.OTHER_DISCOUNT_PERMISSION:
									if(st.checkOtherDiscountPermission(s.getStaffRoleID())){
										d.dismiss();
										mListener.onAllow(Staffs.OTHER_DISCOUNT_PERMISSION);
									}else{
										mTvMsg.setVisibility(View.VISIBLE);
										mTvMsg.setText(R.string.not_have_permission_to_other_discount);
									}
									break;
								}
							}else{
								mTxtStaffCode.setError(null);
								mTxtPass.setError(getString(R.string.incorrect_password));
							}
						}else{
							mTxtStaffCode.setError(getString(R.string.incorrect_staff_code));
							mTxtPass.setError(null);
						}
					}else{
						mTxtStaffCode.setError(null);
						mTxtPass.setError(getString(R.string.enter_password));
					}
				}else{
					mTxtStaffCode.setError(getString(R.string.enter_staff_code));
					mTxtPass.setError(null);
				}
			}
		});
		return d;
	}
	
	public static interface OnCheckPermissionListener{
		void onAllow(int permissionId);
	}
}
