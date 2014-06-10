package com.synature.mpos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;

import com.epson.eposprint.BatteryStatusChangeEventListener;
import com.epson.eposprint.StatusChangeEventListener;
import com.epson.epsonio.DevType;
import com.epson.epsonio.EpsonIoException;
import com.epson.epsonio.Finder;
import com.epson.epsonio.IoStatus;

public class PrinterDiscoverFragment extends ListFragment implements Runnable, 
	OnItemClickListener, StatusChangeEventListener, BatteryStatusChangeEventListener {
	public static final int DISCOVERY_INTERVAL = 500;
	private List<HashMap<String, String>> mPrinterLst;
	private SimpleAdapter mPrinterAdapter;
	private ScheduledExecutorService mScheduler;
	private ScheduledFuture<?> mFuture;
	private Handler mHandler;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mPrinterLst = new ArrayList<HashMap<String, String>>();
		mPrinterAdapter = new SimpleAdapter(getActivity(), mPrinterLst, 
				android.R.layout.simple_list_item_1, 
				new String[]{"Printer"}, new int[] {android.R.id.text1});
		
		setListAdapter(mPrinterAdapter);
		getListView().setOnItemClickListener(this);
		
		// start find thread scheduler
		mScheduler = Executors.newSingleThreadScheduledExecutor();
		findStart();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		
	}

	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		//stop find
        if(mFuture != null){
        	mFuture.cancel(false);
            while(!mFuture.isDone()){
                try{
                    Thread.sleep(DISCOVERY_INTERVAL);
                }catch(Exception e){
                    break;
               }
            }
            mFuture = null;
        }
        if(mScheduler != null){
        	mScheduler.shutdown();
        	mScheduler = null;
        }
        //stop old finder
        while(true) {
            try{
                Finder.stop();
                break;
            }catch(EpsonIoException e){
                if(e.getStatus() != IoStatus.ERR_PROCESSING){
                    break;
                }
            }
        }	
	}
	
	// find start/restart
	private void findStart() {
		if (mScheduler == null) {
			return;
		}

		// stop old finder
		while (true) {
			try {
				Finder.stop();
				break;
			} catch (EpsonIoException e) {
				if (e.getStatus() != IoStatus.ERR_PROCESSING) {
					break;
				}
			}
		}

		// stop find thread
		if (mFuture != null) {
			mFuture.cancel(false);
			while (!mFuture.isDone()) {
				try {
					Thread.sleep(DISCOVERY_INTERVAL);
				} catch (Exception e) {
					break;
				}
			}
			mFuture = null;
		}

		// clear list
		mPrinterLst.clear();
		mPrinterAdapter.notifyDataSetChanged();

		// get device type and find
		try {
			Finder.start(getActivity(), DevType.TCP, "255.255.255.255");
		} catch (EpsonIoException e) {
			e.printStackTrace();
		}

		// start thread
		mFuture = mScheduler.scheduleWithFixedDelay(this, 0,
				DISCOVERY_INTERVAL, TimeUnit.MILLISECONDS);
	}

	@Override
	public void onBatteryStatusChangeEvent(final String deviceName, final int bat) {
		getActivity().runOnUiThread(new Runnable(){

			@Override
			public synchronized void run() {
				new AlertDialog.Builder(getActivity())
				.setTitle(R.string.error)
				.setMessage(deviceName + " status: " + bat)
				.show();
			}
			
		});
	}

	@Override
	public void onStatusChangeEvent(final String deviceName, final int status) {
		getActivity().runOnUiThread(new Runnable(){

			@Override
			public synchronized void run() {
				new AlertDialog.Builder(getActivity())
				.setTitle(R.string.error)
				.setMessage(deviceName + " status: " + status)
				.show();
			}
			
		});
	}

	@Override
	public synchronized void run() {
		class UpdateListThread extends Thread {
			String[] list;

			public UpdateListThread(String[] listDevices) {
				list = listDevices;
			}

			@Override
			public void run() {
				if (list == null) {
					if (mPrinterLst.size() > 0) {
						mPrinterLst.clear();
						mPrinterAdapter.notifyDataSetChanged();
					}
				} else if (list.length != mPrinterLst.size()) {
					mPrinterLst.clear();
					for (String name : list) {
						HashMap<String, String> item = new HashMap<String, String>();
						item.put("Address", name);
						mPrinterLst.add(item);
					}
					mPrinterAdapter.notifyDataSetChanged();
				}
			}
		}

		String[] deviceList = null;
		try {
			deviceList = Finder.getResult();
			mHandler.post(new UpdateListThread(deviceList));
		} catch (Exception e) {
			return;
		}
	}
}
