package com.syn.mpos;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.view.Menu;

public class SettingActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.setting, menu);
		return true;
	}

	public static class SettingCategoryFragment extends ListFragment{
		
	}
	
	public static class SettingFragment extends Fragment{
		
	}
}
