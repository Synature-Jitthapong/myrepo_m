package com.syn.mpos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

public class DiscountPopup extends DialogFragment implements DialogInterface.OnClickListener{
	
	public static EditText mTxtDiscount;
	public static RadioGroup mRdoDistype;
	
	public static DiscountPopup newInstance() {
		DiscountPopup f = new DiscountPopup();
		return f;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		getDialog().getWindow().setGravity(Gravity.CENTER | Gravity.BOTTOM);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = (LayoutInflater)
				getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.discount_dialog, null);
		mTxtDiscount = (EditText) view.findViewById(R.id.txtDiscount);
		mRdoDistype = (RadioGroup) view.findViewById(R.id.rdoDisType);
		return new AlertDialog.Builder(getActivity())
		.setView(view)
		.setNegativeButton(android.R.string.cancel, this)
		.setPositiveButton(android.R.string.ok, this)
		.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		
	}
}
