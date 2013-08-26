package com.syn.mpos;

import com.syn.mpos.db.Report;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;

public class SaleReportActivity extends Activity {

	private Context context;
	private Report report;
	private long dFrom, dTo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sale_report);
		
		context = SaleReportActivity.this;
	}

	public void createReportClicked(final View v){
		DateCondition condition = new DateCondition(context, new DateCondition.OnCondClickListener() {
			
			@Override
			public void onNegativeClick(long dateFrom, long dateTo) {
				dFrom = dateFrom;
				dTo = dateTo;
			}
		});
		
		condition.setTitle(R.string.condition)
		.setView(null)
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		})
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		})
		.show();
	}
}
