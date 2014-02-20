package com.syn.mpos;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class DialogDiscountBuilder extends AlertDialog.Builder{
	private TextView mTvProductName;
	private EditText mTxtDiscount;
	private RadioButton mRdoDiscountType;
	
	public DialogDiscountBuilder(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) 
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.discount_dialog, null);
		mTvProductName = (TextView) v.findViewById(R.id.tvProductName);
		mTxtDiscount = (EditText) v.findViewById(R.id.txtDiscount);
		mRdoDiscountType = (RadioButton) v.findViewById(R.id.rdoDiscountType);
		setView(v);
	}
}
