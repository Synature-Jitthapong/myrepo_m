package com.syn.mpos;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

@SuppressLint("ValidFragment")
public class DiscountPopup extends DialogFragment implements OnEditorActionListener, OnCheckedChangeListener{
	
	public interface EditDiscountListener{
		void onFinishEdit(int position, String discount, int discountType);
	}
	
	private int mPosition;
	private String mDiscount;
	private int mDiscountType;
	
	public DiscountPopup(int position, String discount, int disType){
		mPosition = position;
		mDiscount = discount;
		mDiscountType = disType;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.discount_input, null);
		EditText txtDiscount = (EditText) view.findViewById(R.id.txtDiscount);
		RadioGroup rdoDistype = (RadioGroup) view.findViewById(R.id.rdoDisType);
		
		rdoDistype.setOnCheckedChangeListener(this);
		txtDiscount.setSelectAllOnFocus(true);
		
		txtDiscount.setText(mDiscount);
		txtDiscount.setOnEditorActionListener(this);
		rdoDistype.check(mDiscountType == 1 ? R.id.rdoPrice : R.id.rdoPercent);
		
		getDialog().setTitle(R.string.discount);
		getDialog().getWindow().setGravity(Gravity.CENTER | Gravity.BOTTOM);
		getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, 
				WindowManager.LayoutParams.WRAP_CONTENT);
		
		return view;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if(EditorInfo.IME_ACTION_DONE == actionId){
			EditDiscountListener activity = (EditDiscountListener) getActivity();
			activity.onFinishEdit(mPosition, v.getText().toString(), mDiscountType);
			
			return true;
		}
		return false;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		RadioButton rdo = (RadioButton) group.findViewById(checkedId);
		switch(checkedId){
		case R.id.rdoPrice:
			if(rdo.isChecked())
				mDiscountType = 1;
			break;
		case R.id.rdoPercent:
			if(rdo.isChecked())
				mDiscountType = 2;
			break;
		}
	}
}
