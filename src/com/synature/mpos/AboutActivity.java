package com.synature.mpos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.synature.mpos.foodcourt.R;

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
		 
		ImageView img = (ImageView) findViewById(R.id.imageView1); 
		img.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if(mCountClick == TOTAL_CLICK){
                            mCountClick = 0;
                            LayoutInflater inflater = getLayoutInflater();
                            View content = inflater.inflate(R.layout.edittext_password, null);
                            final EditText txtPass = (EditText) content.findViewById(R.id.txtPassword);
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
											Utils.backupDatabase(AboutActivity.this);
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
//		img.setOnLongClickListener(new OnLongClickListener() {
//			
//			@Override
//			public boolean onLongClick(View v) {
//				new AlertDialog.Builder(AboutActivity.this)
//				.setMessage("Restore database ?")
//				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						
//					}
//				})
//				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						Utils.restoreDatabase(AboutActivity.this);
//					}
//				}).show();
//				return false;
//			}
//		});
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
