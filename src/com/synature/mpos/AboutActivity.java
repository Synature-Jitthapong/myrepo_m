package com.synature.mpos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class AboutActivity extends Activity {
	
	public static final String PASS = "mposclear";
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
                            mCountClick = 0;
                            final EditText txtPass = new EditText(AboutActivity.this);
                            txtPass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            		LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.setMargins(8, 0, 8, 8);
                            LinearLayout content = new LinearLayout(AboutActivity.this);
                            content.addView(txtPass, params);
                            AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
                            builder.setTitle("Clear all sale data");
                            builder.setCancelable(false);
                            builder.setMessage("Are you sure you want to clear all sale data?");
                            builder.setView(content);
                            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                            });
                            builder.setPositiveButton(android.R.string.ok, null);
                            
                            final AlertDialog d = builder.create();
                            d.show();
                            d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									String pass = txtPass.getText().toString();
									if(!TextUtils.isEmpty(pass)){
										if(pass.equals(PASS)){
											Utils.clearSale(AboutActivity.this);
											d.dismiss();
										}else{
											txtPass.setError("Password incorrect.");
										}
									}else{
										txtPass.setError("Please enter password.");
									}
								}
							});

						}else {
                            mCountClick++;
                        }
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
