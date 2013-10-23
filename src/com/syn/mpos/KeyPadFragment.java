package com.syn.mpos;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class KeyPadFragment extends Fragment implements OnClickListener {
	
	public static final int KEY_0 = 0;
	public static final int KEY_1 = 1;
	public static final int KEY_2 = 2;
	public static final int KEY_3 = 3;
	public static final int KEY_4 = 4;
	public static final int KEY_5 = 5;
	public static final int KEY_6 = 6;
	public static final int KEY_7 = 7;
	public static final int KEY_8 = 8;
	public static final int KEY_9 = 9;
	public static final String KEY_DOT = ".";
	
	public static KeyPadListener callback;
	
	public static KeyPadFragment newInstance(KeyPadListener listener){
		callback = listener;
		KeyPadFragment f = new KeyPadFragment();
		return f;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Button btn0 = (Button) getActivity().findViewById(R.id.btnKey0);
		Button btn1 = (Button) getActivity().findViewById(R.id.btnKey1);
		Button btn2 = (Button) getActivity().findViewById(R.id.btnKey2);
		Button btn3 = (Button) getActivity().findViewById(R.id.btnKey3);
		Button btn4 = (Button) getActivity().findViewById(R.id.btnKey4);
		Button btn5 = (Button) getActivity().findViewById(R.id.btnKey5);
		Button btn6 = (Button) getActivity().findViewById(R.id.btnKey6);
		Button btn7 = (Button) getActivity().findViewById(R.id.btnKey7);
		Button btn8 = (Button) getActivity().findViewById(R.id.btnKey8);
		Button btn9 = (Button) getActivity().findViewById(R.id.btnKey9);
		Button btnEnter = (Button) getActivity().findViewById(R.id.btnKeyEnter);
		Button btnDot = (Button) getActivity().findViewById(R.id.btnKeyDot);
		Button btnDel = (Button) getActivity().findViewById(R.id.btnKeyDel);
		
		btn0.setOnClickListener(this);
		btn1.setOnClickListener(this);
		btn2.setOnClickListener(this);
		btn3.setOnClickListener(this);
		btn4.setOnClickListener(this);
		btn5.setOnClickListener(this);
		btn6.setOnClickListener(this);
		btn7.setOnClickListener(this);
		btn8.setOnClickListener(this);
		btn9.setOnClickListener(this);
		btnDel.setOnClickListener(this);
		btnDot.setOnClickListener(this);
		btnEnter.setOnClickListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.keypad_layout, container, false);
	}

	public static interface KeyPadListener{
		void onKey0(int key0);
		void onKey1(int key1);
		void onKey2(int key2);
		void onKey3(int key3);
		void onKey4(int key4);
		void onKey5(int key5);
		void onKey6(int key6);
		void onKey7(int key7);
		void onKey8(int key8);
		void onKey9(int key9);
		void onKeyDot(String keyDot);
		void onKeyDel();
		void onKeyEnter();
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnKey0:
			callback.onKey0(KEY_0);
			break;
		case R.id.btnKey1:
			callback.onKey1(KEY_1);
			break;
		case R.id.btnKey2:
			callback.onKey2(KEY_2);
			break;
		case R.id.btnKey3:
			callback.onKey3(KEY_3);
			break;
		case R.id.btnKey4:
			callback.onKey4(KEY_4);
			break;
		case R.id.btnKey5:
			callback.onKey5(KEY_5);
			break;
		case R.id.btnKey6:
			callback.onKey6(KEY_6);
			break;
		case R.id.btnKey7:
			callback.onKey7(KEY_7);
			break;
		case R.id.btnKey8:
			callback.onKey8(KEY_8);
			break;
		case R.id.btnKey9:
			callback.onKey9(KEY_9);
			break;
		case R.id.btnKeyDot:
			callback.onKeyDot(KEY_DOT);
			break;
		case R.id.btnKeyDel:
			callback.onKeyDel();
			break;
		case R.id.btnKeyEnter:
			callback.onKeyEnter();
			break;
		}
	}
}
