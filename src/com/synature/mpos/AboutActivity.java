package com.synature.mpos;

import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		 ((TextView) findViewById(R.id.textView2)).setText(this.getString(R.string.device_code) +
				 Secure.getString(this.getContentResolver(),
					Secure.ANDROID_ID));
		 
		ImageView img = (ImageView) findViewById(R.id.imageView1); 
//		img.setOnClickListener(new OnClickListener() {
//
//					@Override
//					public void onClick(View arg0) {
//						if(mCountClick == TOTAL_CLICK){
//                            mCountClick = 0;
//                            LayoutInflater inflater = getLayoutInflater();
//                            View content = inflater.inflate(R.layout.edittext_password, null);
//                            final EditText txtPass = (EditText) content.findViewById(R.id.txtPassword);
//                            AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
//                            builder.setTitle("Clear all sale data");
//                            builder.setCancelable(false);
//                            builder.setMessage("Are you sure you want to clear all sale data?");
//                            builder.setView(content);
//                            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                    }
//                            });
//                            builder.setPositiveButton(android.R.string.ok, null);
//                            
//                            final AlertDialog d = builder.create();
//                            d.show();
//                            d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
//								
//								@Override
//								public void onClick(View v) {
//									String pass = txtPass.getText().toString();
//									if(!TextUtils.isEmpty(pass)){
//										if(pass.equals(PASS)){
//											Utils.backupDatabase(AboutActivity.this);
//											Utils.clearSale(AboutActivity.this);
//											d.dismiss();
//										}else{
//											txtPass.setError("Password incorrect.");
//										}
//									}else{
//										txtPass.setError("Please enter password.");
//									}
//								}
//							});
//
//						}else {
//                            mCountClick++;
//                        }
//					}
//				});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		case R.id.itemClearSale:
			ClearSaleDialogFragment f = ClearSaleDialogFragment.newInstance();
			f.show(getFragmentManager(), ClearSaleDialogFragment.TAG);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_about, menu);
		return super.onCreateOptionsMenu(menu);
	}
}
