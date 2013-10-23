package com.syn.mpos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class DiscountKeyPadFragment extends KeyPadFragment implements KeyPadFragment.KeyPadListener {
	
	private Formatter mFormat;
	private StringBuilder mStrDiscount;
	private EditText mTxtDiscount;
	private float mTotalDiscount;
	private float mTotalPrice;
	
	public static DiscountKeyPadFragment newInstance(KeyPadListener listener){
		DiscountKeyPadFragment f = new DiscountKeyPadFragment();
		callback = listener;
		return f;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mTxtDiscount = (EditText) getActivity().findViewById(R.id.txtDiscount);
		
		mFormat = new Formatter(getActivity());
		mStrDiscount = new StringBuilder();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.discount_dialog, container, false);
	}

	void setTxtDiscount(){
		try {
			mTotalDiscount = Float.parseFloat(mStrDiscount.toString());
		} catch (NumberFormatException e) {
			mTotalDiscount = 0.0f;
		}
		
		mTxtDiscount.setText(mFormat.currencyFormat(mTotalDiscount));
	}
	
	@Override
	public void onKey0(int key0) {
		mStrDiscount.append(key0);
		setTxtDiscount();
	}

	@Override
	public void onKey1(int key1) {
		mStrDiscount.append(key1);
		setTxtDiscount();
	}

	@Override
	public void onKey2(int key2) {
		mStrDiscount.append(key2);
		setTxtDiscount();
	}

	@Override
	public void onKey3(int key3) {
		mStrDiscount.append(key3);
		setTxtDiscount();
	}

	@Override
	public void onKey4(int key4) {
		mStrDiscount.append(key4);
		setTxtDiscount();
	}

	@Override
	public void onKey5(int key5) {
		mStrDiscount.append(key5);
		setTxtDiscount();
	}

	@Override
	public void onKey6(int key6) {
		mStrDiscount.append(key6);
		setTxtDiscount();
	}

	@Override
	public void onKey7(int key7) {
		mStrDiscount.append(key7);
	}

	@Override
	public void onKey8(int key8) {
		mStrDiscount.append(key8);
		setTxtDiscount();
	}

	@Override
	public void onKey9(int key9) {
		mStrDiscount.append(key9);
		setTxtDiscount();
	}

	@Override
	public void onKeyDot(String keyDot) {
		mStrDiscount.append(keyDot);
		setTxtDiscount();
	}

	@Override
	public void onKeyDel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onKeyEnter() {
		// TODO Auto-generated method stub
		
	}
}
