package com.syn.mpos;

import java.util.ArrayList;
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
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class StockCountActivity extends Activity {
	private Formatter mFormat;
	private MPOSStockCount mStockCount;
	private int mStcDocId;
	private List<StockMaterial> mStockLst;
	private StockAdapter mStockAdapter;
	private Calendar mCalendar;
	private int mStaffId;
	private int mShopId;

	private ListView mLvStock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_count);

		Intent intent = getIntent();
		mStaffId = intent.getIntExtra("staffId", 0);
		mShopId = intent.getIntExtra("shopId", 0);
		if (mStaffId == 0 || mShopId == 0)
			finish();

		mLvStock = (ListView) findViewById(R.id.listView1);

		init();
	}

	private void init() {
		mCalendar = Calendar.getInstance();
		Calendar dateFrom = new GregorianCalendar(mCalendar.get(Calendar.YEAR),
				mCalendar.get(Calendar.MONTH), 1);
		mFormat = new Formatter(StockCountActivity.this);
		mStockCount = new MPOSStockCount(StockCountActivity.this, dateFrom.getTimeInMillis(),
				mCalendar.getTimeInMillis());
		mStockLst = new ArrayList<StockMaterial>();
		mStockAdapter = new StockAdapter();
		mLvStock.setAdapter(mStockAdapter);
		mLvStock.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				clearFocus(v);
				return false;
			}

		});

		mStcDocId = mStockCount.getCurrentDocument(mShopId,
				MPOSStockDocument.DAILY_DOC);

		if (mStcDocId == 0) {
			mStcDocId = mStockCount.createDocument(mShopId,
					MPOSStockDocument.DAILY_DOC, mStaffId);

			mStockLst = mStockCount.listStock();
			new SaveStockCountTask().execute();
		} else {
			mStockLst = mStockCount.listStock(mStcDocId, mShopId);
			mStockAdapter.notifyDataSetChanged();
		}
	}
	
	private void clearFocus(final View v){
		v.clearFocus();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		clearFocus(getCurrentFocus());
		return super.onTouchEvent(event);
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
			txtRemark = new EditText(StockCountActivity.this);
			new AlertDialog.Builder(StockCountActivity.this)
					.setView(txtRemark)
					.setTitle(R.string.confirm)
					.setMessage(R.string.confirm_stock_count)
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									hideKeyboard();
								}
							})
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mStockCount.confirmStock(mStcDocId,
											mShopId, mStaffId, txtRemark
													.getText().toString());

									new AlertDialog.Builder(StockCountActivity.this)
											.setIcon(
													android.R.drawable.ic_dialog_info)
											.setTitle(R.string.confirm)
											.setMessage(
													R.string.confirm_success)
											.setNeutralButton(
													R.string.close,
													new DialogInterface.OnClickListener() {

														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {
															hideKeyboard();
															finish();
														}
													}).show();
								}
							}).show();
			return true;
		case R.id.itemClose:
			finish();
			return true;
		case R.id.itemCancel:
			new AlertDialog.Builder(StockCountActivity.this)
					.setTitle(android.R.string.cancel)
					.setMessage(R.string.confirm_cancel_stock_count)
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
									if (mStockCount.cancelDocument(mStcDocId,
											mShopId, mStaffId, "test cancel")) {
										finish();
									} else {
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
			inflater = LayoutInflater.from(StockCountActivity.this);
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
			View rowView = convertView;

			rowView = inflater.inflate(R.layout.stock_count_template, null);
			TextView tvItemNo = (TextView) rowView.findViewById(R.id.tvItemNo);
			TextView tvItemCode = (TextView) rowView
					.findViewById(R.id.tvItemCode);
			TextView tvItemName = (TextView) rowView
					.findViewById(R.id.tvItemName);
			TextView tvItemCurrQty = (TextView) rowView
					.findViewById(R.id.tvItemCurrQty);
			EditText txtItemQty = (EditText) rowView
					.findViewById(R.id.txtItemQty);
			final TextView tvItemDiff = (TextView) rowView
					.findViewById(R.id.tvItemDiff);
			TextView tvItemUnit = (TextView) rowView
					.findViewById(R.id.tvItemUnit);

			txtItemQty.setSelectAllOnFocus(true);

			final float currQty = stock.getCurrQty();
			final float countQty = stock.getCountQty();
			float diffQty = countQty - currQty;

			tvItemNo.setText(Integer.toString(position + 1));
			tvItemCode.setText(stock.getCode());
			tvItemName.setText(stock.getName());
			tvItemCurrQty.setText(mFormat.qtyFormat(currQty));
			txtItemQty.setText(mFormat.qtyFormat(countQty));
			tvItemDiff.setText(mFormat.qtyFormat(diffQty));
			tvItemUnit.setText("unit");

			txtItemQty.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					EditText txtQty = (EditText) v;

					if (!hasFocus) {
						float enterCount = 0.0f;

						try {
							enterCount = Float.parseFloat(txtQty.getText()
									.toString());
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						mStockCount.updateDocumentDetail(stock.getId(),
								mStcDocId, mShopId, stock.getMatId(),
								enterCount, stock.getPricePerUnit(), "");

						stock.setCountQty(enterCount);
						tvItemDiff.setText(mFormat.qtyFormat(enterCount
								- currQty));
					}
				}
			});

			if (position % 2 == 0)
				rowView.setBackgroundResource(R.color.smoke_white);
			else
				rowView.setBackgroundResource(R.color.light_gray);

			return rowView;
		}

	}

	private class SaveStockCountTask extends AsyncTask<Void, Void, Boolean> {

		private ProgressDialog progress;

		public SaveStockCountTask() {
			progress = new ProgressDialog(StockCountActivity.this);
			progress.setCancelable(false);
			TextView tvProgress = new TextView(StockCountActivity.this);
			tvProgress.setText(R.string.progress);
			progress.setMessage(tvProgress.getText().toString());
		}

		@Override
		protected void onPostExecute(Boolean isSuccess) {
			if (progress.isShowing())
				progress.dismiss();

			if (isSuccess) {
				mStockLst = mStockCount.listStock(mStcDocId, mShopId);
				mStockAdapter.notifyDataSetChanged();
			} else {
				Util.alert(StockCountActivity.this, android.R.drawable.ic_dialog_alert,
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
			return mStockCount.saveStock(mStcDocId, mShopId, mStaffId, "",
					mStockLst);
		}

	}

	private void hideKeyboard() {
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
}
