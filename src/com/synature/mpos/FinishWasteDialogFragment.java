package com.synature.mpos;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class FinishWasteDialogFragment extends DialogFragment implements OnClickListener{

	public static final String TAG = "FinishWasteDialogFragment";
	
	private TextView mTvTotalPrice;
	private EditText mTxtRemark;
	private Button mBtnConfirm;
	private Button mBtnCancel;
	public static FinishWasteDialogFragment newInstance(){
		FinishWasteDialogFragment f = new FinishWasteDialogFragment();
		
		return f;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return super.onCreateDialog(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.finish_waste_layout, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		mTvTotalPrice = (TextView) view.findViewById(R.id.tvTotalPrice);
		mTxtRemark = (EditText) view.findViewById(R.id.txtRemark);
		mBtnConfirm = (Button) view.findViewById(R.id.btnConfirm);
		mBtnCancel = (Button) view.findViewById(R.id.btnCancel);
		mBtnConfirm.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnConfirm:
			dismiss();
			break;
		case R.id.btnCancel:
			dismiss();
			break;
		}
	}

}
