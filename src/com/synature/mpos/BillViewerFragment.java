package com.synature.mpos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;

public class BillViewerFragment extends DialogFragment implements OnClickListener{

	public static final int CHECK_VIEW = 1;
	public static final int REPORT_VIEW = 2;
	
	private int mTransactionId;
	private int mViewMode;
	
	private TextPrint mTextPrint;
	
	private ImageButton mBtnPrint;
	private CustomFontTextView mTextView;
	
	public static BillViewerFragment newInstance(int transactionId, int viewMode){
		BillViewerFragment f = new BillViewerFragment();
		Bundle b = new Bundle();
		b.putInt("transactionId", transactionId);
		b.putInt("viewMode", viewMode);
		f.setArguments(b);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTransactionId = getArguments().getInt("transactionId");
		mViewMode = getArguments().getInt("viewMode");
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View content = inflater.inflate(R.layout.bill_viewer, null, false);
		mBtnPrint = (ImageButton) content.findViewById(R.id.btnPrint);
		mTextView = (CustomFontTextView) content.findViewById(R.id.textView1);

		mBtnPrint.setOnClickListener(this);
		
		mTextPrint = new TextPrint(getActivity());
		if(mViewMode == CHECK_VIEW){
			mTextPrint.createTextForPrintCheckReceipt(mTransactionId);
		}else if (mViewMode == REPORT_VIEW){
			mTextPrint.createTextForPrintReceipt(mTransactionId, false, false);
		}
		mTextView.setText(mTextPrint.getTextToPrint());
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(content);
		AlertDialog d = builder.create();
		WindowManager.LayoutParams params = d.getWindow().getAttributes();
		//params.gravity = Gravity.LEFT;
		d.getWindow().setAttributes(params);
		d.show();
		return d;
	}
	
	private class TextPrint extends WintecPrinter{

		public TextPrint(Context context) {
			super(context);
		}
	}

	@Override
	public void onClick(View v) {
		mTextPrint.print();
	}
}
