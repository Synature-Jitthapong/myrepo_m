package com.synature.mpos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class BillViewerFragment extends DialogFragment{

	private int mTransactionId;
	
	private CustomFontTextView mTextView;
	
	public static BillViewerFragment newInstance(int transactionId){
		BillViewerFragment f = new BillViewerFragment();
		Bundle b = new Bundle();
		b.putInt("transactionId", transactionId);
		f.setArguments(b);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTransactionId = getArguments().getInt("transactionId");
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View content = inflater.inflate(R.layout.bill_viewer, null, false);
		mTextView = (CustomFontTextView) content.findViewById(R.id.textView1);

		TextPrint tp = new TextPrint(getActivity());
		tp.createTextForPrintReceipt(mTransactionId, false, true);
		mTextView.setText(tp.getTextToPrint());
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(content);
		AlertDialog d = builder.create();
		WindowManager.LayoutParams params = d.getWindow().getAttributes();
		//params.gravity = Gravity.LEFT;
		d.getWindow().setAttributes(params);
		d.show();
		return d;
	}
	
	private class TextPrint extends PrinterBase{

		public TextPrint(Context context) {
			super(context);
		}
		
	}
}
