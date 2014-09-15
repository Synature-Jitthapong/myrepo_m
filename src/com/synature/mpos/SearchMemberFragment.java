package com.synature.mpos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class SearchMemberFragment extends DialogFragment{

	private OnSearchMember mListener;
	
	public static SearchMemberFragment newInstance(){
		SearchMemberFragment f = new SearchMemberFragment();
		return f;
	}
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof OnSearchMember){
			mListener = (OnSearchMember) activity;
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final View view = getActivity().getLayoutInflater().inflate(R.layout.edittext, null);
		final EditText txtCode = ((EditText) view);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Search");
		builder.setView(view);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
			}
		});
		builder.setPositiveButton(android.R.string.ok, null);
		final AlertDialog d = builder.create();
		d.show();
		d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String memberCode = txtCode.getText().toString();
				if(!TextUtils.isEmpty(memberCode)){
					mListener.onSearch(memberCode);
					d.dismiss();
				}
			}
		});
		return d;
	}

	public static interface OnSearchMember{
		void onSearch(String memberCode);
	}
}
