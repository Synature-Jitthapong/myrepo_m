package com.syn.mpos;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import com.syn.mpos.inventory.MPOSStockDocument;
import com.syn.pos.inventory.Document;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class StockCountActivity extends Activity {
	
	private Context mContext;
	private Formatter mFormat;
	private MPOSStockDocument mStock;
	private int mDocumentId;
	private List<HashMap<String, String>> mStockLst;
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
		if(mStaffId == 0 || mShopId == 0)
			finish();
		
		lvStock = (ListView) findViewById(R.id.listView1);
		
		init();
	}
	
	private void init(){
		mCalendar = Calendar.getInstance();
		mFormat = new Formatter(mContext);
		mStock = new MPOSStockDocument(mContext);
		mDocumentId = mStock.getCurrentDocument(mShopId, MPOSStockDocument.DAILY_DOC);
		if(mDocumentId == 0)
			mDocumentId = mStock.createDocument(mShopId, MPOSStockDocument.DAILY_DOC, mStaffId);
		
		Calendar dateFrom = new GregorianCalendar(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), 1);
		
		mStockLst = mStock.listStock(dateFrom.getTimeInMillis(), mCalendar.getTimeInMillis());
		mStockAdapter = new StockAdapter();
		lvStock.setAdapter(mStockAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.action_confirm, menu);
		return true;
	}

	private class StockAdapter extends BaseAdapter{
		
		private LayoutInflater inflater;
		
		public StockAdapter(){
			inflater = LayoutInflater.from(mContext);
		}
		
		@Override
		public int getCount() {
			return mStockLst != null ? mStockLst.size() : 0;
		}

		@Override
		public HashMap<String, String> getItem(int position) {
			return mStockLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			HashMap<String, String> stock = mStockLst.get(position);
			ViewHolder holder = null;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.stock_count_template, null);
				holder = new ViewHolder();
				holder.tvItemNo = (TextView) convertView.findViewById(R.id.tvItemNo);
				holder.tvItemCode = (TextView) convertView.findViewById(R.id.tvItemCode);
				holder.tvItemName = (TextView) convertView.findViewById(R.id.tvItemName);
				holder.tvItemCurrQty = (TextView) convertView.findViewById(R.id.tvItemCurrQty);
				holder.txtItemQty = (EditText) convertView.findViewById(R.id.txtItemQty);
				holder.tvItemDiff = (TextView) convertView.findViewById(R.id.tvItemDiff);
				holder.tvItemUnit = (TextView) convertView.findViewById(R.id.tvItemUnit);
				
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tvItemNo.setText(Integer.toString(position + 1));
			holder.tvItemCode.setText(stock.get("productCode"));
			holder.tvItemName.setText(stock.get("productName"));
			holder.tvItemCurrQty.setText(stock.get("currQty"));
			holder.txtItemQty.setText(stock.get("countQty"));
			
			return convertView;
		}
		
		private class ViewHolder{
			TextView tvItemNo;
			TextView tvItemCode;
			TextView tvItemName;
			TextView tvItemCurrQty;
			EditText txtItemQty;
			TextView tvItemDiff;
			TextView tvItemUnit;
		}
	}
}
