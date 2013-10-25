package com.syn.mpos;

import java.util.List;

import com.syn.mpos.database.Setting;
import com.syn.mpos.database.Sync;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import android.widget.ListView;

public class SettingActivity extends Activity {
	private static Setting mSetting;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		mSetting = new Setting(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.setting, menu);
		return true;
	}

	public static class SettingCategoryFragment extends ListFragment{
		String[] settings = {"Connection", "Sync", "Printer"};
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
			setListAdapter(new ArrayAdapter<String>(getActivity(), 
					android.R.layout.simple_list_item_activated_1, settings));
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			
			showSetting(0);
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
		private List<Sync.SyncItem> mSyncLst;
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
			Sync sync = new Sync(getActivity());
			mSyncLst = sync.listSync();
			
			ListView lvSync = (ListView) getActivity().findViewById(R.id.lvSync);
			lvSync.setAdapter(new ArrayAdapter<Sync.SyncItem>(getActivity(), 
					android.R.layout.simple_list_item_activated_1, mSyncLst));
			
			lvSync.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
					
				}
				
			});
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.sync_setting_fragment, container, false);
		}
	}
	
	public static class ConnectionSettingFragment extends Fragment{
		private Setting.Connection mConn;
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

		    //setHasOptionsMenu(true);
		    
			mConn = mSetting.getConnection();
			
			final EditText txtAddress = (EditText) getActivity().findViewById(R.id.txtAddress);
			final EditText txtBackoffice = (EditText) getActivity().findViewById(R.id.txtBackoffice);
			final Button btnSave = (Button) getActivity().findViewById(R.id.btnSave);
			
			txtAddress.setText(mConn.getAddress());
			txtBackoffice.setText(mConn.getBackoffice());
			
			btnSave.setOnClickListener(new OnClickListener(){
		
				@Override
				public void onClick(View v) {
					String addr = txtAddress.getText().toString();
					String backoffice = txtBackoffice.getText().toString();
					
					if(!addr.isEmpty() && !backoffice.isEmpty()){
						mSetting.addConnSetting(addr, backoffice);
						btnSave.setText("Save successfully");
						btnSave.setEnabled(false);
					}
				}
				
			});
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.conn_setting_fragment, container, false);
		}

//		@Override
//		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//			inflater.inflate(R.menu.action_confirm, menu);
//			super.onCreateOptionsMenu(menu, inflater);
//		}
//
//		@Override
//		public boolean onOptionsItemSelected(MenuItem item) {
//			// TODO Auto-generated method stub
//			return super.onOptionsItemSelected(item);
//		}
//		
	}
}
