package com.syn.mpos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class DiscountPopup extends DialogFragment{
	private static OnKeyListener onKeyCallback;
	private static OnCheckedChangeListener onCheckedCallback;
	private String mDiscount;
	private int mDistype;
	
	public static DiscountPopup newInstance(String discount, int disType, 
			OnKeyListener onKey, OnCheckedChangeListener onChecked) {
		onKeyCallback = onKey;
		onCheckedCallback = onChecked;
		
		Bundle b = new Bundle();
		b.putString("discount", discount);
		b.putInt("disType", disType);
		DiscountPopup f = new DiscountPopup();
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDiscount = getArguments().getString("discount");
		mDistype = getArguments().getInt("disType");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		getDialog().getWindow().setGravity(Gravity.CENTER | Gravity.BOTTOM);
		getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, 
				WindowManager.LayoutParams.WRAP_CONTENT);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = (LayoutInflater)
				getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.discount_dialog, null);
		EditText txtDiscount = (EditText) view.findViewById(R.id.txtDiscount);
		RadioGroup rdoDistype = (RadioGroup) view.findViewById(R.id.rdoDisType);
		txtDiscount.setSelectAllOnFocus(true);
		txtDiscount.setText(mDiscount);
		rdoDistype.check(mDistype == 1 ? R.id.rdoPrice : R.id.rdoPercent);
		txtDiscount.setOnKeyListener(onKeyCallback);
		rdoDistype.setOnCheckedChangeListener(onCheckedCallback);
		return new AlertDialog.Builder(getActivity())
		.setView(view)
		.create();
	}
}
