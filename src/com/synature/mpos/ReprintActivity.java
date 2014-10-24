package com.synature.mpos;

import java.util.List;

import com.synature.mpos.database.SessionDao;
import com.synature.mpos.database.TransactionDao;
import com.synature.mpos.database.model.OrderTransaction;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class ReprintActivity extends Activity {
	
	private TransactionDao mOrders;
	
	private ReprintTransAdapter mTransAdapter;
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
		
		mLvTrans = (ListView) findViewById(R.id.listView1);

		mOrders = new TransactionDao(this);
		SessionDao sess = new SessionDao(this);

		mTransAdapter = new ReprintTransAdapter(ReprintActivity.this, 
				mOrders.listSuccessTransaction(sess.getLastSessionDate()));
		mLvTrans.setAdapter(mTransAdapter);
		mLvTrans.setSelection(mTransAdapter.getCount() - 1);
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
				holder.btnPrint = (ImageButton) convertView.findViewById(R.id.btnPrint);
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
			ImageButton btnPrint;
		}
	}

	private class Reprint extends PrintReceipt{
		
		public int mTransactionId;
		private ImageButton mBtnPrint;
		
		public Reprint(int transactionId, ImageButton refBtnPrint) {
			super(ReprintActivity.this);
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
}
