package com.synature.mpos;

import java.util.ArrayList;
import java.util.List;

import com.synature.mpos.database.GlobalPropertyDao;
import com.synature.mpos.database.MPOSDatabase;
import com.synature.mpos.database.SessionDao;
import com.synature.mpos.database.TransactionDao;
import com.synature.mpos.database.model.OrderTransaction;
import com.synature.mpos.database.table.OrderTransTable;
import com.synature.mpos.point.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class ReprintActivity extends Activity implements OnItemSelectedListener{
	
	private TransactionDao mOrders;
	private GlobalPropertyDao mGlobal;
	private List<OrderTransaction> mOrderTrans;
	
	private ReprintTransAdapter mTransAdapter;
	private Spinner mSpSaleDate;
	private ListView mLvTrans;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
	            WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	    LayoutParams params = getWindow().getAttributes();
	    params.width = 500;
	    params.height= 500;
	    params.alpha = 1.0f;
	    params.dimAmount = 0.5f;
	    getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
		setContentView(R.layout.activity_reprint);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		mSpSaleDate = (Spinner) findViewById(R.id.spSaleDate);
		mLvTrans = (ListView) findViewById(R.id.listView1);

		mOrders = new TransactionDao(this);
		mGlobal = new GlobalPropertyDao(this);

		setupListView();
		
		mSpSaleDate.setAdapter(new SaleDateAdapter(getSaleDate()));
		mSpSaleDate.setOnItemSelectedListener(this);
		mSpSaleDate.setSelection(mSpSaleDate.getCount() - 1);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void setupListView(){
		if(mTransAdapter == null){
			SessionDao sess = new SessionDao(this);
			mOrderTrans = mOrders.listSuccessTransaction(sess.getLastSessionDate());
			mTransAdapter = new ReprintTransAdapter(ReprintActivity.this, mOrderTrans);
			mLvTrans.setAdapter(mTransAdapter);
			mLvTrans.setSelection(mTransAdapter.getCount() - 1);
		}
	}
	
	private List<String> getSaleDate(){
		List<String> saleDate = new ArrayList<String>();
		MPOSDatabase db = new MPOSDatabase(ReprintActivity.this);
		Cursor cursor = db.getReadableDatabase().rawQuery(
				"SELECT " + OrderTransTable.COLUMN_SALE_DATE 
				+ " FROM " + OrderTransTable.TABLE_ORDER_TRANS
				+ " GROUP BY " + OrderTransTable.COLUMN_SALE_DATE, null);
		if(cursor.moveToFirst()){
			do{
				saleDate.add(cursor.getString(0));
			}while(cursor.moveToNext());
		}
		cursor.close();
		return saleDate;
	}
	
	public class SaleDateAdapter extends BaseAdapter{

		private List<String> mSaleDate;
		
		public SaleDateAdapter(List<String> saleDate){
			mSaleDate = saleDate;
		}
		
		@Override
		public int getCount() {
			return mSaleDate != null ? mSaleDate.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			return mSaleDate.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = getLayoutInflater().inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
			}
			CheckedTextView textView = (CheckedTextView) convertView;
			textView.setText(mGlobal.dateFormat(mSaleDate.get(position)));
			return convertView;
		}
		
	}
	
	public class ReprintTransAdapter extends OrderTransactionAdapter{

		public ReprintTransAdapter(Context c, List<OrderTransaction> transLst) {
			super(c, transLst);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final OrderTransaction trans = mTransLst.get(position);
			final ViewHolder holder;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.reprint_trans_item, parent, false);
				holder = new ViewHolder();
				holder.tvNo = (TextView) convertView.findViewById(R.id.tvNo);
				holder.tvReceiptNo = (TextView) convertView.findViewById(R.id.tvReceiptNo);
				holder.btnPrint = (Button) convertView.findViewById(R.id.btnPrint);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tvNo.setText(String.valueOf(position + 1) + ".");
			holder.tvReceiptNo.setText(trans.getReceiptNo());
			holder.btnPrint.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					holder.btnPrint.setEnabled(false);
					new Reprint(trans.getTransactionId(), holder.btnPrint).execute();
				}
				
			});
			return convertView;
		}
		
		public class ViewHolder {
			TextView tvNo;
			TextView tvReceiptNo;
			Button btnPrint;
		}
	}

	private class Reprint extends PrintReceipt{
		
		public int mTransactionId;
		private Button mBtnPrint;
		
		public Reprint(int transactionId, Button refBtnPrint) {
			super(ReprintActivity.this, null);
			mTransactionId = transactionId;
			mBtnPrint = refBtnPrint;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			if(Utils.isInternalPrinterSetting(ReprintActivity.this)){
				WintecPrinter wtPrinter = new WintecPrinter(ReprintActivity.this);
				wtPrinter.createTextForPrintReceipt(mTransactionId, true, false);
				wtPrinter.print();
			}else{
				EPSONPrinter epPrinter = new EPSONPrinter(ReprintActivity.this);	
				epPrinter.createTextForPrintReceipt(mTransactionId, true, false);
				epPrinter.print();
			}
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					mBtnPrint.setEnabled(true);
				}
				
			});
			return null;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		String saleDate = parent.getItemAtPosition(position).toString();
		mOrderTrans = mOrders.listSuccessTransaction(saleDate);
		mTransAdapter = new ReprintTransAdapter(ReprintActivity.this, mOrderTrans);
		mLvTrans.setAdapter(mTransAdapter);
		mLvTrans.setSelection(mTransAdapter.getCount() - 1);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}
}
