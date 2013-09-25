package com.syn.mpos;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import com.syn.mpos.inventory.MPOSStockCount;
import com.syn.mpos.inventory.MPOSStockDocument;
import com.syn.mpos.inventory.StockMaterial;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class StockCountActivity extends Activity {

	private Context mContext;
	private Formatter mFormat;
	private MPOSStockCount mStockCount;
	private int mDocumentId;
	private List<StockMaterial> mStockLst;
	private StockAdapter mStockAdapter;
	private Calendar mCalendar;
	private int mStaffId;
	private int mShopId;

	private ListView lvStock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_count);
		mContext = StockCountActivity.this;

		Intent intent = getIntent();
		mStaffId = intent.getIntExtra("staffId", 0);
		mShopId = intent.getIntExtra("shopId", 0);
		if (mStaffId == 0 || mShopId == 0)
			finish();

		lvStock = (ListView) findViewById(R.id.listView1);

		init();
	}

	private void init() {
		mCalendar = Calendar.getInstance();
		Calendar dateFrom = new GregorianCalendar(mCalendar.get(Calendar.YEAR),
				mCalendar.get(Calendar.MONTH), 1);
		mFormat = new Formatter(mContext);
		mStockCount = new MPOSStockCount(mContext, dateFrom.getTimeInMillis(),
				mCalendar.getTimeInMillis());

		mDocumentId = mStockCount.getCurrentDocument(mShopId,
				MPOSStockDocument.DAILY_DOC);
		if (mDocumentId == 0) {
			mDocumentId = mStockCount.createDocument(mShopId,
					MPOSStockDocument.DAILY_DOC, mStaffId);
			mStockLst = mStockCount.listStock();
			new SaveStockCountTask().execute();
		} else {
			mStockLst = mStockCount.listStock(mDocumentId, mShopId);
		}

		mStockAdapter = new StockAdapter();
		lvStock.setAdapter(mStockAdapter);
	}

	@Override
	protected void onDestroy() {
		mStockCount.clearDocument();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_confirm, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		final EditText txtRemark;

		switch (item.getItemId()) {
		case R.id.itemConfirm:
			txtRemark = new EditText(mContext);
			new AlertDialog.Builder(mContext)
					.setView(txtRemark)
					.setTitle(R.string.confirm)
					.setMessage(R.string.confirm_stock_count)
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

								}
							})
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mStockCount.confirmStock(mDocumentId,
											mShopId, mStaffId, txtRemark
													.getText().toString());
								}
							}).show();
			return true;
		case R.id.itemClose:
			finish();
			return true;
		case R.id.itemCancel:
			new AlertDialog.Builder(mContext)
			.setTitle(R.string.confirm)
			.setMessage(R.string.confirm_stock_count)
			.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {

						}
					})
			.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							if(mStockCount.cancelDocument(mDocumentId, mShopId, mStaffId, "test cancel")){
								finish();
							}else{
								// do alert error
							}
						}
					}).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class StockAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		public StockAdapter() {
			inflater = LayoutInflater.from(mContext);
		}

		@Override
		public int getCount() {
			return mStockLst != null ? mStockLst.size() : 0;
		}

		@Override
		public StockMaterial getItem(int position) {
			return mStockLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final StockMaterial stock = mStockLst.get(position);
			final ViewHolder holder;

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.stock_count_template,
						null);
				holder = new ViewHolder();
				holder.tvItemNo = (TextView) convertView
						.findViewById(R.id.tvItemNo);
				holder.tvItemCode = (TextView) convertView
						.findViewById(R.id.tvItemCode);
				holder.tvItemName = (TextView) convertView
						.findViewById(R.id.tvItemName);
				holder.tvItemCurrQty = (TextView) convertView
						.findViewById(R.id.tvItemCurrQty);
				holder.txtItemQty = (EditText) convertView
						.findViewById(R.id.txtItemQty);
				holder.tvItemDiff = (TextView) convertView
						.findViewById(R.id.tvItemDiff);
				holder.tvItemUnit = (TextView) convertView
						.findViewById(R.id.tvItemUnit);
				holder.txtItemQty.setSelectAllOnFocus(true);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final float currQty = stock.getCurrQty();
			final float countQty = stock.getCountQty();
			float diffQty = countQty - currQty;

			holder.tvItemNo.setText(Integer.toString(position + 1));
			holder.tvItemCode.setText(stock.getCode());
			holder.tvItemName.setText(stock.getName());
			holder.tvItemCurrQty.setText(mFormat.qtyFormat(currQty));
			holder.txtItemQty.setText(mFormat.qtyFormat(countQty));
			holder.tvItemDiff.setText(mFormat.qtyFormat(diffQty));
			holder.tvItemUnit.setText("unit");
			holder.txtItemQty.clearFocus();
			
			holder.txtItemQty.setOnKeyListener(new OnKeyListener(){

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if(event.getAction() == KeyEvent.ACTION_UP){
						float enterCount = 0.0f;
						EditText txtQty = (EditText) v;
						
						try {
							enterCount = Float.parseFloat(txtQty.getText().toString());
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	
						stock.setCountQty(enterCount);
						holder.tvItemDiff.setText(mFormat.qtyFormat(enterCount - currQty));
					}
					return false;
				}
				
			});
		
			return convertView;
		}

		private class ViewHolder {
			TextView tvItemNo;
			TextView tvItemCode;
			TextView tvItemName;
			TextView tvItemCurrQty;
			EditText txtItemQty;
			TextView tvItemDiff;
			TextView tvItemUnit;
		}
	}

	private class SaveStockCountTask extends AsyncTask<Void, Void, Boolean> {

		private ProgressDialog progress;

		public SaveStockCountTask() {
			progress = new ProgressDialog(mContext);
			progress.setCancelable(false);
			TextView tvProgress = new TextView(mContext);
			tvProgress.setText(R.string.progress);
			progress.setMessage(tvProgress.getText().toString());
		}

		@Override
		protected void onPostExecute(Boolean isSuccess) {
			if (progress.isShowing())
				progress.dismiss();

			if (!isSuccess) {
				Util.alert(mContext, android.R.drawable.ic_dialog_alert,
						R.string.error, R.string.error_save_stock);
			}
			super.onPostExecute(isSuccess);
		}

		@Override
		protected void onPreExecute() {
			progress.show();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			return mStockCount.saveStock(mDocumentId, mShopId, mStaffId, "",
					mStockLst);
		}

	}
}
