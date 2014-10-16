package com.synature.mpos;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;

public class SendLogActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature (Window.FEATURE_NO_TITLE);
	    setFinishOnTouchOutside (false);
		setContentView(R.layout.activity_send_log);
	}
	
	public void sendLogClick(final View v){
		sendLogFile();
		finish();
	}
	
	private String extractLogToFile()
	{
	  PackageManager manager = this.getPackageManager();
	  PackageInfo info = null;
	  try {
	    info = manager.getPackageInfo (this.getPackageName(), 0);
	  } catch (NameNotFoundException e2) {
	  }
	  String model = Build.MODEL;
	  if (!model.startsWith(Build.MANUFACTURER))
	    model = Build.MANUFACTURER + " " + model;

	  // Make file name - file must be saved to external storage or it wont be readable by
	  // the email app.
	  String path = Environment.getExternalStorageDirectory() + File.separator + Utils.LOG_PATH + File.separator;
		String fullName = path + "fatal";

	  // Extract to file.
	  File file = new File (fullName);
	  InputStreamReader reader = null;
	  FileWriter writer = null;
	  try
	  {
	    // For Android 4.0 and earlier, you will get all app's log output, so filter it to
	    // mostly limit it to your app's output.  In later versions, the filtering isn't needed.
	    String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
	                  "logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s" :
	                  "logcat -d -v time";

	    // get input stream
	    Process process = Runtime.getRuntime().exec(cmd);
	    reader = new InputStreamReader (process.getInputStream());

	    // write output stream
	    writer = new FileWriter (file);
	    writer.write ("Android version: " +  Build.VERSION.SDK_INT + "\n");
	    writer.write ("Device: " + model + "\n");
	    writer.write ("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");

	    char[] buffer = new char[10000];
	    do 
	    {
	      int n = reader.read (buffer, 0, buffer.length);
	      if (n == -1)
	        break;
	      writer.write (buffer, 0, n);
	    } while (true);

	    reader.close();
	    writer.close();
	  }
	  catch (IOException e)
	  {
	    if (writer != null)
	      try {
	        writer.close();
	      } catch (IOException e1) {
	      }
	    if (reader != null)
	      try {
	        reader.close();
	      } catch (IOException e1) {
	      }

	    // You might want to write a failure message to the log here.
	    return null;
	  }

	  return fullName;
	}
	
	private void sendLogFile ()
	{
	  String fullName = extractLogToFile();
	  if (fullName == null)
	    return;

	  Intent intent = new Intent (Intent.ACTION_SEND);
	  intent.setType ("plain/text");
	  intent.putExtra (Intent.EXTRA_EMAIL, new String[] {"jitthapong@synaturegroup.com"});
	  intent.putExtra (Intent.EXTRA_SUBJECT, "mPOS fatal log");
	  intent.putExtra (Intent.EXTRA_STREAM, Uri.parse ("file://" + fullName));
	  intent.putExtra (Intent.EXTRA_TEXT, "Log file attached."); // do this so some email clients don't complain about empty body.
	  startActivity (intent);
	}
}
