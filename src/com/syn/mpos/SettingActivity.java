package com.syn.mpos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.syn.mpos.database.Setting;
import com.syn.mpos.database.Shop;

import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SettingActivity extends Activity {
	private static Setting mSetting;
	private static Shop mShop;
	private static String mDeviceCode;
	private static Setting.Connection mConn;
	private static int mSettingPosition;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		mSetting = new Setting(this);
		mShop = new Shop(this);
		mConn = mSetting.getConnection();
		mConn.setFullUrl(mConn.getProtocal() + mConn.getAddress() + 
				"/" + mConn.getBackoffice() + "/" + mConn.getService());
		mDeviceCode = Secure.getString(this.getContentResolver(),
				Secure.ANDROID_ID);
		
		Intent intent = getIntent();
		mSettingPosition = intent.getIntExtra("settingPosition", 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_setting, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.itemClose:
			finish();
			return true;
		default :
		return super.onOptionsItemSelected(item);
		}
	}

	public static class SettingCategoryFragment extends ListFragment{
		String[] settings = {"Connection", "Sync", "Printer"};
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
			setListAdapter(new ArrayAdapter<String>(getActivity(), 
					android.R.layout.simple_list_item_activated_1, settings));
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			
			showSetting(mSettingPosition);
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			showSetting(position);
		}
		
		private void showSetting(int position){
			getListView().setItemChecked(position, true);
			
            FragmentTransaction ft = null;
			switch(position){
			case 0:
	            ConnectionSettingFragment cf = new ConnectionSettingFragment();
	            ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.details, cf);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
				break;
			case 1:
	            SyncSettingFragment sf = new SyncSettingFragment();
	            ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.details, sf);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
				break;
			case 2:
	            PrinterDiscoverFragment pf = new PrinterDiscoverFragment();
				ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.details, pf);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
				break;
			}
		}
	}

	
	public static class SyncSettingFragment extends Fragment{
		private List<Setting.SyncItem> mSyncLst;
		private SyncAdapter mSyncAdapter;
		private Formatter mFormat;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mFormat = new Formatter(getActivity());
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
			mSyncLst = new ArrayList<Setting.SyncItem>();
			Setting.SyncItem syncItem = new Setting.SyncItem();
			syncItem.setSyncItemId(1);
			syncItem.setSyncStatus(0);
			syncItem.setSyncItemName(getActivity().getString(R.string.sync_product));
			mSyncLst.add(syncItem);
			
			syncItem = new Setting.SyncItem();
			syncItem.setSyncItemId(2);
			syncItem.setSyncStatus(0);
			syncItem.setSyncItemName(getActivity().getString(R.string.sync_sale));
			mSyncLst.add(syncItem);

			syncItem = new Setting.SyncItem();
			syncItem.setSyncItemId(3);
			syncItem.setSyncStatus(0);
			syncItem.setSyncItemName(getActivity().getString(R.string.sync_stock));
			mSyncLst.add(syncItem);
			
			mSyncAdapter = new SyncAdapter();
			final ListView lvSync = (ListView) getActivity().findViewById(R.id.lvSync);
			lvSync.setAdapter(mSyncAdapter);
			
			lvSync.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View v,
						final int position, long id) {
					final Setting.SyncItem syncItem = (Setting.SyncItem) 
							parent.getItemAtPosition(position);
					
					MPOSService mposService = new MPOSService(getActivity(),mConn);
					
					switch(syncItem.getSyncItemId()){
					case 1:
						mposService.loadProductData(mShop.getShopProperty().getShopID(), 
								mDeviceCode, new MPOSService.OnServiceProcessListener() {
									
									@Override
									public void onSuccess() {
										
										mSetting.addSyncItem(syncItem.getSyncItemId(), 
												true, syncItem.getSyncItemName(), 1, 
												new Date().getTime());
										
										syncItem.setSyncStatus(1);
										syncItem.setSyncTime(new Date().getTime());
										
										mSyncLst.set(position, syncItem);
										mSyncAdapter.notifyDataSetChanged();
										
										new AlertDialog.Builder(getActivity())
										.setTitle(R.string.sync_product)
										.setIcon(android.R.drawable.ic_dialog_alert)
										.setMessage(R.string.update_data_success)
										.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
												
											}
										}).show();
									}
									
									@Override
									public void onError(String mesg) {
										
										mSetting.addSyncItem(syncItem.getSyncItemId(), 
												true, syncItem.getSyncItemName(), -1, 
												new Date().getTime());
										
										syncItem.setSyncStatus(-1);
										syncItem.setSyncTime(new Date().getTime());
										
										mSyncLst.set(position, syncItem);
										mSyncAdapter.notifyDataSetChanged();
										
										new AlertDialog.Builder(getActivity())
										.setTitle(R.string.sync_product)
										.setIcon(android.R.drawable.ic_dialog_alert)
										.setMessage(mesg)
										.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
												
											}
										}).show();
									}
								});
						break;
					case 2:
						break;
					case 3:
						break;
					}
				}
				
			});
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.sync_setting_fragment, container, false);
		}
		
		class SyncAdapter extends BaseAdapter{

			@Override
			public int getCount() {
				return mSyncLst.size();
			}

			@Override
			public Setting.SyncItem getItem(int position) {
				return mSyncLst.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(final int position, View convertView, ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater)
						getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				
				convertView = inflater.inflate(R.layout.sync_item_template, null);
				TextView tvSyncName = (TextView) convertView.findViewById(R.id.textView1);
				TextView tvSyncSummary = (TextView) convertView.findViewById(R.id.textView2);
				ImageView imgSyncStatus = (ImageView) convertView.findViewById(R.id.imageView1);
				
				tvSyncName.setText(mSyncLst.get(position).getSyncItemName());
				if(mSyncLst.get(position).getSyncStatus() == 1){
					tvSyncSummary.setText(mFormat.dateTimeFormat(new Date(mSyncLst.get(position).getSyncTime())));
					imgSyncStatus.setImageResource(R.drawable.ic_action_check);
				}else if(mSyncLst.get(position).getSyncStatus() == -1){
					tvSyncSummary.setText(R.string.sync_fail);
					imgSyncStatus.setImageResource(R.drawable.ic_action_alerts_and_states_error);
				}else{
					tvSyncSummary.setText(R.string.not_sync);
					imgSyncStatus.setImageResource(R.drawable.ic_action_alerts_and_states_error);
				}
				return convertView;
			}
			
		}
	}
	
	public static class ConnectionSettingFragment extends Fragment{
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

		    //setHasOptionsMenu(true);
			
			final EditText txtAddress = (EditText) getActivity().findViewById(R.id.txtAddress);
			final EditText txtBackoffice = (EditText) getActivity().findViewById(R.id.txtBackoffice);
			final Button btnSave = (Button) getActivity().findViewById(R.id.btnSave);
			TextView tvDeviceCode = (TextView) getActivity().findViewById(R.id.tvDeviceCode);
			tvDeviceCode.setText(mDeviceCode);
			
			txtAddress.setText(mConn.getAddress());
			txtBackoffice.setText(mConn.getBackoffice());
			
			btnSave.setOnClickListener(new OnClickListener(){
		
				@Override
				public void onClick(View v) {
					String addr = txtAddress.getText().toString();
					String backoffice = txtBackoffice.getText().toString();
					
					if(!addr.isEmpty() && !backoffice.isEmpty()){
						mSetting.addConnSetting(addr, backoffice);
						btnSave.setText(R.string.save_success);
						btnSave.setEnabled(false);
						
						mConn = mSetting.getConnection();
						mConn.setFullUrl(mConn.getProtocal() + mConn.getAddress() + 
								"/" + mConn.getBackoffice() + "/" + mConn.getService());
						
						MPOSService mposService = new MPOSService(getActivity(), mConn);
						mposService.loadShopData(mDeviceCode, new MPOSService.OnServiceProcessListener() {
							
							@Override
							public void onSuccess() {
								new AlertDialog.Builder(getActivity())
								.setTitle(R.string.update_data)
								.setMessage(R.string.update_data_success)
								.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										
									}
								}).show();
							}
							
							@Override
							public void onError(String mesg) {
								new AlertDialog.Builder(getActivity())
								.setTitle(R.string.update_data)
								.setMessage(mesg)
								.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										
									}
								}).show();
								btnSave.setEnabled(true);
							}
						});
					}else{
						if(addr.isEmpty()){
							new AlertDialog.Builder(getActivity())
							.setTitle(R.string.setting)
							.setMessage(R.string.enter_ipaddress)
							.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									txtAddress.requestFocus();
								}
							}).show();
						}else if(backoffice.isEmpty()){
							new AlertDialog.Builder(getActivity())
							.setTitle(R.string.setting)
							.setMessage(R.string.enter_backoffice)
							.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									txtBackoffice.requestFocus();
								}
							}).show();
						}
					}
				}
				
			});
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.conn_setting_fragment, container, false);
		}

	}
}
