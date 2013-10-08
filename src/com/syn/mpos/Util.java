package com.syn.mpos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;

public class Util {
	public static void alert(Context context, int icon, int title, String mesg){
		new AlertDialog.Builder(context)
		.setIcon(icon)
		.setTitle(title)
		.setMessage(mesg)
		.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		})
		.show();
	}
	
	public static void alert(Context context, int icon, int title, int mesg){
		new AlertDialog.Builder(context)
		.setIcon(icon)
		.setTitle(title)
		.setMessage(mesg)
		.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		})
		.show();
	}
}
