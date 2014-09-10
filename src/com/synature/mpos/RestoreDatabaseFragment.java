package com.synature.mpos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.synature.mpos.database.FormaterDao;
import com.synature.mpos.database.MPOSDatabase;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RestoreDatabaseFragment extends DialogFragment{

	private FormaterDao mFormat;
	private DatabaseInfo mDbInfo;
	private List<DatabaseInfo> mDbInfoLst;
	private DatabaseListAdapter mAdapter;
	
	private ListView mLvDatabase;
	
	public static RestoreDatabaseFragment newInstance(){
		RestoreDatabaseFragment f = new RestoreDatabaseFragment();
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFormat = new FormaterDao(getActivity());
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View content = inflater.inflate(R.layout.database_listview, null);
		mLvDatabase = (ListView) content;
		setupDatabaseListViewAdapter();
		mLvDatabase.setOnItemClickListener(mOnItemClickListener);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.restore_db);
		builder.setView(content);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		builder.setPositiveButton(android.R.string.ok, null);
		final AlertDialog d = builder.create();
		d.show();
		d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(mDbInfo != null){
					restoreDatabase(mDbInfo.getFileName());
					mDbInfo = null;
					d.dismiss();
				}else{
					new AlertDialog.Builder(getActivity())
					.setTitle(R.string.restore_db)
					.setMessage(R.string.please_select)
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).show();
				}
			}
			
		});
		return d;
	}

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int pos,
				long id) {
			mDbInfo = (DatabaseInfo) parent.getItemAtPosition(pos);
		}
		
	};
	
	private void restoreDatabase(String dbFileName){
		String dbName = MPOSDatabase.MPOSOpenHelper.DB_NAME;
		File sd = Environment.getExternalStorageDirectory();
		FileChannel source = null;
		FileChannel destination = null;
		File dbPath = getActivity().getDatabasePath(dbName);
		File sdPath = new File(sd, Utils.BACKUP_DB_PATH);
		if(!sdPath.exists())
			sdPath.mkdirs();
		try {
			source = new FileInputStream(sdPath + File.separator + dbFileName).getChannel();
			destination = new FileOutputStream(dbPath).getChannel();
			destination.transferFrom(source, 0, source.size());
			source.close();
			destination.close();
			Utils.makeToask(getActivity(), getActivity().getString(R.string.restore_db_success));
		} catch (IOException e) {
			e.printStackTrace();
			Utils.makeToask(getActivity(), e.getLocalizedMessage());
		}
	}
	
	private void setupDatabaseListViewAdapter(){
		if(mAdapter == null){
			listDatabaseInfo();
			mAdapter = new DatabaseListAdapter();
			mLvDatabase.setAdapter(mAdapter);
		}
		mAdapter.notifyDataSetChanged();
	}
	
	private void listDatabaseInfo(){
		File sd = Environment.getExternalStorageDirectory();
		File backupPath = new File(sd, Utils.BACKUP_DB_PATH);
		File[] files = backupPath.listFiles();
		if(files != null){
			mDbInfoLst = new ArrayList<DatabaseInfo>();
			for(File file : files){
				DatabaseInfo dbInfo = new DatabaseInfo();
				dbInfo.setDbName(file.getName());
				dbInfo.setFileName(file.getName());
				dbInfo.setDbSize(DecimalFormat.getInstance().format(file.length()));
				mDbInfoLst.add(dbInfo);
			}
		}
	}
	
	private class DatabaseListAdapter extends BaseAdapter{
		
		private LayoutInflater mInflater = getActivity().getLayoutInflater();
		
		@Override
		public int getCount() {
			return mDbInfoLst != null ? mDbInfoLst.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			return mDbInfoLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.database_list_item, parent, false);
				holder.tvDbName = (TextView) convertView.findViewById(R.id.tvDbName);
				holder.tvDbSize = (TextView) convertView.findViewById(R.id.tvDbSize);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			DatabaseInfo dbInfo = mDbInfoLst.get(position);
			String fileName = dbInfo.getFileName();
			try {
				fileName = mFormat.dateTimeFormat(dbInfo.getDbName());
			} catch (Exception e) {
			}
			holder.tvDbName.setText(fileName);
			holder.tvDbSize.setText(dbInfo.getDbSize());
			return convertView;
		}
		
		private class ViewHolder{
			TextView tvDbName;
			TextView tvDbSize;
		}
	}
	
	private class DatabaseInfo{
		private String dbName;
		private String fileName;
		private String dbSize;
		public String getDbName() {
			return dbName;
		}
		public void setDbName(String dbName) {
			this.dbName = dbName;
		}
		public String getDbSize() {
			return dbSize;
		}
		public void setDbSize(String dbSize) {
			this.dbSize = dbSize;
		}
		public String getFileName() {
			return fileName;
		}
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
	}
}
