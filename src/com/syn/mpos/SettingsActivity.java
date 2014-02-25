package com.syn.mpos;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;

public class SettingsActivity extends PreferenceActivity {

	public static final String KEY_PREF_SERVER_URL = "server_url";
	public static final String KEY_PREF_PRINTER_IP = "printer_ip";
	public static final String KEY_PREF_PRINTER_LIST = "printer_list";
	public static final String KEY_PREF_PRINTER_FONT_LIST = "printer_font_list";
	
	private static final boolean ALWAYS_SIMPLE_PREFS = false;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        
		setupSimplePreferencesScreen();
	}

	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}
		
		addPreferencesFromResource(R.xml.pref_general);
		addPreferencesFromResource(R.xml.pref_printer);
		bindPreferenceSummaryToValue(findPreference(KEY_PREF_SERVER_URL));
		bindPreferenceSummaryToValue(findPreference(KEY_PREF_PRINTER_IP));
		bindPreferenceSummaryToValue(findPreference(KEY_PREF_PRINTER_LIST));
		bindPreferenceSummaryToValue(findPreference(KEY_PREF_PRINTER_FONT_LIST));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		default :
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}

	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		if (!isSimplePreferences(this)) {
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
	}

	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);
				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);

			} else {
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	private static void bindPreferenceSummaryToValue(Preference preference) {
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
	}
	
	public static class PrinterPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_printer);
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_PRINTER_IP));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_PRINTER_LIST));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_PRINTER_LIST));
		}
	}
	
	public static class ConnectionPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);

			bindPreferenceSummaryToValue(findPreference(KEY_PREF_SERVER_URL));
		}
	}
	
	public void printTestClick(final View v){
		Print printer = new Print();
		try {
			printer.openPrinter(Print.DEVTYPE_TCP, MPOSApplication.getPrinterIp(), 0, 1000);
			Builder builder = new Builder(MPOSApplication.getPrinterName(), Builder.MODEL_ANK, 
				MPOSApplication.getContext());
			if(MPOSApplication.getPrinterFont().equals("a")){
				builder.addTextFont(Builder.FONT_A);
			}else if(MPOSApplication.getPrinterFont().equals("b")){
				builder.addTextFont(Builder.FONT_B);
			}
			String printText = MPOSApplication.getContext().getString(R.string.print_test_text).replaceAll("\\*", " ");
			builder.addTextAlign(Builder.ALIGN_CENTER);
			builder.addTextSize(1, 1);
			builder.addText(printText);
			builder.addFeedUnit(30);
			builder.addCut(Builder.CUT_FEED);

			// send builder data
			int[] status = new int[1];
			int[] battery = new int[1];
			try {
				printer.sendData(builder, 10000, status, battery);
			} catch (EposException e) {
				e.printStackTrace();
			}
			if (builder != null) {
				builder.clearCommandBuffer();
			}
		} catch (EposException e) {
			switch(e.getErrorStatus()){
			case EposException.ERR_CONNECT:
				MPOSUtil.makeToask(SettingsActivity.this, e.getMessage());
				break;
			default :
				MPOSUtil.makeToask(SettingsActivity.this, SettingsActivity.this.getString(R.string.not_found_printer));
			}
		}	
	}
}
