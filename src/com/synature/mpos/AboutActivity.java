package com.synature.mpos;

import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends Activity {
	
	public static final int TOTAL_CLICK = 4;
	
	private int mCountClick = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		 ((TextView) findViewById(R.id.textView2)).setText(this.getString(R.string.device_code) +
				 Secure.getString(this.getContentResolver(),
					Secure.ANDROID_ID));
		((ImageView) findViewById(R.id.imageView1))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if(mCountClick == TOTAL_CLICK){
							Utils.clearSale(AboutActivity.this);
						}
						mCountClick++;
					}
				});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
