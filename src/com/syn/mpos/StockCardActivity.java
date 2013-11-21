package com.syn.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.syn.mpos.inventory.MPOSStockCard;
import com.syn.mpos.inventory.StockProduct;

import android.os.Bundle;
import android.app.Activity;
import android.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class StockCardActivity extends Activity implements OnClickListener{
	private Formatter mFormat;
	private MPOSStockCard mStockCard;
	private List<StockProduct> mStockLst;
	private StockCardAdapter mStockCardAdapter;
	private Calendar mCalendarFrom;
	private Calendar mCalendarTo;
	private long mDateFrom;
	private long mDateTo;
	
	private Button btnDateFrom;
	private Button btnDateTo;
	private ListView lvStock;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_card);
		
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
		lvStock = (ListView) findViewById(R.id.listView1);
		
		init();
	}

	private void init(){
		mStockCard = new MPOSStockCard(StockCardActivity.this);
		mFormat = new Formatter(StockCardActivity.this);
		mCalendarFrom = Calendar.getInstance();
		mCalendarTo = Calendar.getInstance();
		mCalendarFrom = new GregorianCalendar(mCalendarFrom.get(Calendar.YEAR), 
				mCalendarFrom.get(Calendar.MONTH), 1);
		mDateFrom = mCalendarFrom.getTimeInMillis();
		mDateTo = mCalendarTo.getTimeInMillis();
		
		mStockLst = new ArrayList<StockProduct>();
		mStockCardAdapter = new StockCardAdapter();
		lvStock.setAdapter(mStockCardAdapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_stock_card, menu);
		MenuItem menuItem = (MenuItem) menu.findItem(R.id.itemDateCondition);
		btnDateFrom = (Button) menuItem.getActionView().findViewById(R.id.btnDateFrom);
		btnDateTo = (Button) menuItem.getActionView().findViewById(R.id.btnDateTo);
		btnDateFrom.setText(mFormat.dateFormat(mCalendarFrom.getTime()));
		btnDateTo.setText(mFormat.dateFormat(mCalendarTo.getTime()));
		btnDateFrom.setOnClickListener(this);
		btnDateTo.setOnClickListener(this);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.itemViewStock:
			viewStock();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onDateFromClick() {
		DialogFragment dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
			
			@Override
			public void onSetDate(long date) {
				mCalendarFrom.setTimeInMillis(date);
				mDateFrom = mCalendarFrom.getTimeInMillis();
				
				btnDateFrom.setText(mFormat.dateFormat(mCalendarFrom.getTime()));
			}
		});
		dialogFragment.show(getFragmentManager(), "Condition");
	}

	public void onDateToClick() {
		DialogFragment dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
			
			@Override
			public void onSetDate(long date) {
				mCalendarTo.setTimeInMillis(date);
				mDateTo = mCalendarTo.getTimeInMillis();
				
				btnDateTo.setText(mFormat.dateFormat(mCalendarTo.getTime()));
			}
		});
		dialogFragment.show(getFragmentManager(), "Condition");
	}

	public void viewStock() {
		mStockLst = mStockCard.listStock(mDateFrom, mDateTo);
		mStockCardAdapter.notifyDataSetChanged();
	}
	
	private class StockCardAdapter extends BaseAdapter{

		private LayoutInflater inflater;
		
		public StockCardAdapter(){
			inflater = LayoutInflater.from(StockCardActivity.this);
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
		public View getView(int position, View convertView, ViewGroup parent) {
			StockProduct stock = mStockLst.get(position);
			ViewHolder holder;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.stock_card_template, null);
				holder = new ViewHolder();
				holder.tvNo = (TextView) convertView.findViewById(R.id.tvNo);
				holder.tvCode = (TextView) convertView.findViewById(R.id.tvCode);
				holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
				holder.tvInitial = (TextView) convertView.findViewById(R.id.tvInit);
				holder.tvReceive = (TextView) convertView.findViewById(R.id.tvReceive);
				holder.tvSale = (TextView) convertView.findViewById(R.id.tvSale);
				holder.tvEndding = (TextView) convertView.findViewById(R.id.tvEndding);
				holder.tvVariance = (TextView) convertView.findViewById(R.id.tvVariance);
				holder.tvSummary = (TextView) convertView.findViewById(R.id.tvSummary);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			float init = stock.getInit();;
			float receive = stock.getReceive();
			float sale = stock.getSale();
			float endding = init + receive + sale;
			float variance = stock.getVariance();
			float summary = endding + variance;
			
			holder.tvNo.setText(Integer.toString(position + 1));
			holder.tvCode.setText(stock.getCode());
			holder.tvName.setText(stock.getName());
			holder.tvInitial.setText(mFormat.qtyFormat(init));
			holder.tvReceive.setText(mFormat.qtyFormat(receive));
			holder.tvSale.setText(mFormat.qtyFormat(sale));
			holder.tvEndding.setText(mFormat.qtyFormat(endding));
			holder.tvVariance.setText(mFormat.qtyFormat(variance));
			holder.tvSummary.setText(mFormat.qtyFormat(summary));
			
			if(position % 2 == 0)
				convertView.setBackgroundResource(R.color.smoke_white);
			else
				convertView.setBackgroundResource(R.color.light_gray);
				
			return convertView;
		}
		
		private class ViewHolder{
			TextView tvNo;
			TextView tvCode;
			TextView tvName;
			TextView tvInitial;
			TextView tvReceive;
			TextView tvSale;
			TextView tvEndding;
			TextView tvVariance;
			TextView tvSummary;
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnDateFrom:
			onDateFromClick();
			break;
		case R.id.btnDateTo:
			onDateToClick();
			break;
		}
	}
}
