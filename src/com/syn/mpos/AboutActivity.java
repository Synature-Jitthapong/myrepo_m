package com.syn.mpos;

import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.view.MenuItem;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		 ((TextView) findViewById(R.id.textView1)).setText(Secure.getString(this.getContentResolver(),
					Secure.ANDROID_ID));
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
