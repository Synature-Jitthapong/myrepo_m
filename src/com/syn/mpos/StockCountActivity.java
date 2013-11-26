package com.syn.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.syn.mpos.database.inventory.StockCount;
import com.syn.mpos.database.inventory.StockDocument;
import com.syn.mpos.database.inventory.StockProduct;

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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class StockCountActivity extends Activity implements OnEditorActionListener{
	private Formatter mFormat;
	private StockCount mStockCount;
	private int mStcDocId;
	private StockProduct mStockProduct;
	private List<StockProduct> mStockLst;
	private StockAdapter mStockAdapter;
	private Calendar mCalendar;
	private int mPosition = -1;
	private int mStaffId;
	private int mShopId;

	private MenuItem mItemInput;
	private ListView mLvStock;
	private EditText mTxtCountStock;
	private TextView mTvItemName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_count);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        
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
		mStockCount = new StockCount(StockCountActivity.this, dateFrom.getTimeInMillis(),
				mCalendar.getTimeInMillis());
		mStockLst = new ArrayList<StockProduct>();
		mStockAdapter = new StockAdapter();
		mLvStock.setAdapter(mStockAdapter);
		
		mLvStock.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				mPosition = position;
				mStockProduct = (StockProduct) parent.getItemAtPosition(position);
				
				mItemInput.setVisible(true);
				mTvItemName.setText(mStockProduct.getName());
				mTxtCountStock.setText(mFormat.qtyFormat(mStockProduct.getCountQty()));
				mTxtCountStock.selectAll();
				mTxtCountStock.requestFocus();
				
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mTxtCountStock,
                        InputMethodManager.SHOW_IMPLICIT);
			}
		});

		mStcDocId = mStockCount.getCurrentDocument(mShopId,
				StockDocument.DAILY_DOC);

		if (mStcDocId == 0) {
			mStcDocId = mStockCount.createDocument(mShopId,
					StockDocument.DAILY_DOC, mStaffId);

			mStockLst = mStockCount.listStock();
			new SaveStockCountTask().execute();
		} else {
			mStockLst = mStockCount.listStock(mStcDocId, mShopId);
			mStockAdapter.notifyDataSetChanged();
		}
	}
	
	@Override
	protected void onDestroy() {
		mStockCount.clearDocument();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_stockcount, menu);
		mItemInput = menu.findItem(R.id.itemInputNum);
		mTxtCountStock = (EditText) mItemInput.getActionView().findViewById(R.id.editText1);
		mTvItemName = (TextView) mItemInput.getActionView().findViewById(R.id.textView1);
		mTxtCountStock.setOnEditorActionListener(this);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.itemConfirm:
			new AlertDialog.Builder(StockCountActivity.this)
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
									mStockCount.confirmStock(mStcDocId,mShopId, mStaffId, "");

									new AlertDialog.Builder(StockCountActivity.this)
											.setIcon(android.R.drawable.ic_dialog_info)
											.setTitle(R.string.confirm)
											.setMessage(R.string.confirm_success)
											.setNeutralButton(R.string.close,
													new DialogInterface.OnClickListener() {

														@Override
														public void onClick(DialogInterface dialog,
																int which) {
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
		public StockProduct getItem(int position) {
			return mStockLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final StockProduct stock = mStockLst.get(position);
			View rowView = convertView;

			rowView = inflater.inflate(R.layout.stock_count_template, null);
			TextView tvItemNo = (TextView) rowView.findViewById(R.id.tvItemNo);
			TextView tvItemCode = (TextView) rowView.findViewById(R.id.tvItemCode);
			TextView tvItemName = (TextView) rowView.findViewById(R.id.tvItemName);
			TextView tvItemCurrQty = (TextView) rowView.findViewById(R.id.tvItemCurrQty);
			TextView tvCount = (TextView) rowView.findViewById(R.id.tvCount);
			final TextView tvItemDiff = (TextView) rowView.findViewById(R.id.tvItemDiff);
			TextView tvItemUnit = (TextView) rowView.findViewById(R.id.tvItemUnit);

			final float currQty = stock.getCurrQty();
			final float countQty = stock.getCountQty();
			float diffQty = countQty - currQty;

			tvItemNo.setText(Integer.toString(position + 1));
			tvItemCode.setText(stock.getCode());
			tvItemName.setText(stock.getName());
			tvItemCurrQty.setText(mFormat.qtyFormat(currQty));
			tvCount.setText(mFormat.qtyFormat(countQty));
			tvItemDiff.setText(mFormat.qtyFormat(diffQty));
			tvItemUnit.setText("unit");

//			txtItemQty.setOnFocusChangeListener(new OnFocusChangeListener() {
//
//				@Override
//				public void onFocusChange(View v, boolean hasFocus) {
//					EditText txtQty = (EditText) v;
//
//					if (!hasFocus) {
//						float enterCount = 0.0f;
//
//						try {
//							enterCount = Float.parseFloat(txtQty.getText()
//									.toString());
//						} catch (NumberFormatException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//
//						mStockCount.updateDocumentDetail(stock.getId(),
//								mStcDocId, mShopId, stock.getProId(),
//								enterCount, stock.getUnitPrice(), "");
//
//						stock.setCountQty(enterCount);
//						tvItemDiff.setText(mFormat.qtyFormat(enterCount
//								- currQty));
//					}
//				}
//			});
//
//			if (position % 2 == 0)
//				rowView.setBackgroundResource(R.color.smoke_white);
//			else
//				rowView.setBackgroundResource(R.color.light_gray);

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
				new AlertDialog.Builder(getApplicationContext())
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.error)
				.setMessage(R.string.error_save_stock)
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				})
				.show();
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

	void clearActionInput(){
		mTvItemName.setText(null);
		mTxtCountStock.setText(null);
		mItemInput.setVisible(false);
	}
	
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if(EditorInfo.IME_ACTION_DONE == actionId){
			float count = 0.0f;
			try {
				count = Float.parseFloat(v.getText().toString());
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			mStockCount.updateDocumentDetail(mStockProduct.getId(),
					mStcDocId, mShopId, mStockProduct.getProId(),
					count, mStockProduct.getUnitPrice(), "");
			
			StockProduct stock = mStockProduct;
			stock.setCountQty(count);
			mStockLst.set(mPosition, stock);
			mStockAdapter.notifyDataSetChanged();
			
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			
            clearActionInput();
            
			return true;
		}
		return false;
	}
}
