package com.syn.mpos;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

public class InvDirectReceiveActivity extends Activity {
	
	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inv_direct_receive);
		mContext = InvDirectReceiveActivity.this;
	}

	public void popReceiveClicked(final View v){
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View addProView = inflater.inflate(R.layout.add_product_layout, null);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setView(addProView);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
			
		});
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
			
		});
		dialog.show();
	}
}
