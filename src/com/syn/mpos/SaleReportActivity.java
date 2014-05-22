package com.syn.mpos;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.j1tth4.exceptionhandler.ExceptionHandler;
import com.syn.mpos.dao.FormatPropertyDao;
import com.syn.mpos.dao.MPOSDatabase;
import com.syn.mpos.dao.PaymentDao;
import com.syn.mpos.dao.Reporting;
import com.syn.mpos.dao.TransactionDao;
import com.syn.pos.Payment;
import com.syn.pos.Report;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class SaleReportActivity extends Activity implements OnClickListener{
	//private static final String TAG = "SaleReportActivity";
	public static final int BILL_REPORT = 1;
	public static final int PRODUCT_REPORT = 2;

	private static FormatPropertyDao sFormat;
	private Report mReport;
	private Reporting mReporting;
	private BillReportAdapter mBillReportAdapter;
	private ProductReportAdapter mProductReportAdapter;
	
	private Calendar mCalendar;
	private String mDateFrom;
	private String mDateTo;
	
	private Button mBtnDateFrom;
	private Button mBtnDateTo;
	private Button mBtnCreateReport;
	private Button mBtnPrint;
	private ListView mLvReport;
	private ProgressBar mProgressReport;
	private ExpandableListView mLvReportProduct;
	private Spinner mSpReportType;
	private LinearLayout mLayoutBillReport;
	private LinearLayout mLayoutProductReport;
	//private LinearLayout mBillHeader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * Register ExceptinHandler for catch error when application crash.
		 */
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, 
				MPOSApplication.LOG_DIR, MPOSApplication.LOG_FILE_NAME));
		
		setContentView(R.layout.activity_sale_report);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		mLayoutBillReport = (LinearLayout) findViewById(R.id.billLayout);
		mLayoutProductReport = (LinearLayout) findViewById(R.id.productLayout);
		mProgressReport = (ProgressBar) findViewById(R.id.progressBarReport);
		//mBillHeader = (LinearLayout) findViewById(R.id.billHeader);
		
		mLvReport = (ListView) findViewById(R.id.lvReport);
		mLvReportProduct = (ExpandableListView) findViewById(R.id.lvReportProduct);
		
		sFormat = new FormatPropertyDao(SaleReportActivity.this);
		Calendar c = Calendar.getInstance();
		mCalendar = new GregorianCalendar(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		mDateFrom = String.valueOf(mCalendar.getTimeInMillis());
		mDateTo = String.valueOf(mCalendar.getTimeInMillis());

		mReporting = new Reporting(SaleReportActivity.this, mDateFrom, mDateTo);
		mReport = new Report();
		mBillReportAdapter = new BillReportAdapter();
		mProductReportAdapter = new ProductReportAdapter();
		mLvReport.setAdapter(mBillReportAdapter);
		mLvReportProduct.setAdapter(mProductReportAdapter);
		mLvReportProduct.setGroupIndicator(null);
	}

	private class LoadBillReportTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected void onPreExecute() {
			mProgressReport.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(Void result) {
			mProgressReport.setVisibility(View.GONE);
			mBillReportAdapter.notifyDataSetChanged();
		}

		@Override
		protected Void doInBackground(Void... params) {
			mReport = mReporting.getSaleReportByBill();
			return null;
		}
		
	}
	
	private class LoadProductReportTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected void onPreExecute() {
			mProgressReport.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(Void result) {
			mProgressReport.setVisibility(View.GONE);
			mProductReportAdapter.notifyDataSetChanged();
		}

		@Override
		protected Void doInBackground(Void... params) {
			mReport = mReporting.getProductDataReport();
			return null;
		}
		
	}
	
	private void getBillReportSummary(){
		Report.ReportDetail summary = mReporting.getBillSummary();
		
		TextView tvSummTotalPrice = (TextView) findViewById(R.id.tvSummTotalPrice);
		TextView tvSummDiscount = (TextView) findViewById(R.id.tvSummDiscount);
		TextView tvSummSubTotal = (TextView) findViewById(R.id.tvSummSubTotal);
		TextView tvSummVatable = (TextView) findViewById(R.id.tvSummVatable);
		TextView tvSummTotalVat = (TextView) findViewById(R.id.tvSummTotalVat);
		TextView tvSummTotalPay = (TextView) findViewById(R.id.tvSummTotalPay);
		
		tvSummTotalPrice.setText(sFormat.currencyFormat(summary.getTotalPrice()));
		tvSummDiscount.setText(sFormat.currencyFormat(summary.getDiscount()));
		tvSummSubTotal.setText(sFormat.currencyFormat(summary.getSubTotal()));
		tvSummVatable.setText(sFormat.currencyFormat(summary.getVatable()));
		tvSummTotalVat.setText(sFormat.currencyFormat(summary.getTotalVat()));
		tvSummTotalPay.setText(sFormat.currencyFormat(summary.getTotalPayment()));
	}
	
	private void switchReportType(int type){
		switch(type){
		case 0:
			mLayoutBillReport.setVisibility(View.VISIBLE);
			mLayoutProductReport.setVisibility(View.GONE);
			mBtnCreateReport.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					new LoadBillReportTask().execute();
				}
				
			});
			mBtnPrint.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
				}
				
			});
			break;
		case 1:
			mLayoutBillReport.setVisibility(View.GONE);
			mLayoutProductReport.setVisibility(View.VISIBLE);
			mBtnCreateReport.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					new LoadProductReportTask().execute();
				}
				
			});			
			mBtnPrint.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					new PrintReport(SaleReportActivity.this, 
							PrintReport.WhatPrint.PRODUCT_REPORT).execute();
				}
				
			});
			break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_sale_report, menu);
		MenuItem itemReportType = (MenuItem) menu.findItem(R.id.itemReportType);
		MenuItem itemCondition = (MenuItem) menu.findItem(R.id.itemDateCondition);
		MenuItem itemCreateReport = (MenuItem) menu.findItem(R.id.itemCreateReport);
		MenuItem itemPrint = (MenuItem) menu.findItem(R.id.itemPrint);
		mSpReportType = (Spinner) itemReportType.getActionView().findViewById(R.id.spinner1);
		mBtnDateFrom = (Button) itemCondition.getActionView().findViewById(R.id.btnDateFrom);
		mBtnDateTo = (Button) itemCondition.getActionView().findViewById(R.id.btnDateTo);
		mBtnCreateReport = (Button) itemCreateReport.getActionView();
		mBtnPrint = (Button) itemPrint.getActionView();
		mBtnCreateReport.setText(R.string.create_report);
		mBtnPrint.setText(R.string.print);
		mBtnDateFrom.setText(sFormat.dateFormat(mCalendar.getTime()));
		mBtnDateTo.setText(sFormat.dateFormat(mCalendar.getTime()));
		mBtnDateFrom.setOnClickListener(this);
		mBtnDateTo.setOnClickListener(this);
		mSpReportType.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				switchReportType(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
			
		});
		String[] reportTypes = {
				getString(R.string.sale_report_by_bill),
				getString(R.string.sale_report_by_product)
		};
		mSpReportType.setAdapter(new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_dropdown_item, reportTypes));
		mSpReportType.setSelection(0);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void summaryProduct(){
		Report.ReportDetail summProduct = mReporting.getProductSummary();
		if(summProduct != null){
			TextView tvSummProQty = (TextView) findViewById(R.id.tvSummProQty);
			TextView tvSummProQtyPercent = (TextView) findViewById(R.id.tvSummProQtyPercent);
			TextView tvSummProSubTotal = (TextView) findViewById(R.id.tvSummProSubTotal);
			TextView tvSummProSubTotalPercent = (TextView) findViewById(R.id.tvSummProSubTotalPercent);
			TextView tvSummProDiscount = (TextView) findViewById(R.id.tvSummProDiscount);
			TextView tvSummProTotalPrice = (TextView) findViewById(R.id.tvSummProTotalPrice);
			TextView tvSummProTotalPricePercent = (TextView) findViewById(R.id.tvSummProTotalPricePercent);
			
			tvSummProQty.setText(sFormat.qtyFormat(summProduct.getQty()));
			tvSummProQtyPercent.setText(sFormat.qtyFormat(summProduct.getQty() / summProduct.getQty() * 100));
			tvSummProSubTotal.setText(sFormat.currencyFormat(summProduct.getSubTotal()));
			tvSummProSubTotalPercent.setText(sFormat.qtyFormat(summProduct.getSubTotal() / summProduct.getSubTotal() * 100));
			tvSummProDiscount.setText(sFormat.currencyFormat(summProduct.getDiscount()));
			tvSummProTotalPrice.setText(sFormat.currencyFormat(summProduct.getTotalPrice()));
			tvSummProTotalPricePercent.setText(sFormat.qtyFormat(summProduct.getTotalPrice() / summProduct.getTotalPrice() * 100));
		}
	}
	
	public class ProductReportAdapter extends BaseExpandableListAdapter{
		
		private LayoutInflater mInflater;
		
		public ProductReportAdapter(){
			mInflater = (LayoutInflater)
					SaleReportActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public Report.ReportDetail getChild(int groupPosition, int childPosition) {
			return mReport.getGroupOfProductLst().get(groupPosition).getReportDetail().get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			
			summaryProduct();
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {	
			ProductReportViewHolder holder;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.product_report_template, parent, false);
				holder = new ProductReportViewHolder();
				holder.tvNo = (TextView) convertView.findViewById(R.id.tvNo);
				holder.tvProductCode = (TextView) convertView.findViewById(R.id.tvProCode);
				holder.tvProductName = (TextView) convertView.findViewById(R.id.tvProName);
				holder.tvProductPrice = (TextView) convertView.findViewById(R.id.tvProPrice);
				holder.tvQty = (TextView) convertView.findViewById(R.id.tvQty);
				holder.tvQtyPercent = (TextView) convertView.findViewById(R.id.tvQtyPercent);
				holder.tvSubTotal = (TextView) convertView.findViewById(R.id.tvSubTotal);
				holder.tvSubTotalPercent = (TextView) convertView.findViewById(R.id.tvSubTotalPercent);
				holder.tvDiscount = (TextView) convertView.findViewById(R.id.tvDiscount);
				holder.tvTotalPrice = (TextView) convertView.findViewById(R.id.tvTotalPrice);
				holder.tvTotalPricePercent = (TextView) convertView.findViewById(R.id.tvTotalPricePercent);
				holder.tvVatType = (TextView) convertView.findViewById(R.id.tvVatType);
				convertView.setTag(holder);
			}else{
				holder = (ProductReportViewHolder) convertView.getTag();
			}

			Report.ReportDetail reportDetail = 
					mReport.getGroupOfProductLst().get(groupPosition).getReportDetail().get(childPosition);
			setText(holder, childPosition, reportDetail);
			
			if(reportDetail.getProductName().equals(Reporting.SUMM_DEPT)){
				setSummary(holder, mReport.getGroupOfProductLst().get(groupPosition).getProductDeptName());
			}else if(reportDetail.getProductName().equals(Reporting.SUMM_GROUP)){
				setSummary(holder, mReport.getGroupOfProductLst().get(groupPosition).getProductGroupName());
			}
			return convertView;
		}
		
		private void setSummary(ProductReportViewHolder holder, String text){
			holder.tvNo.setVisibility(View.GONE);
			holder.tvProductName.setVisibility(View.GONE);
			holder.tvProductCode.setVisibility(View.GONE);
			holder.tvProductPrice.setText(SaleReportActivity.this.getString(R.string.summary) + 
					" " + text);
			holder.tvProductPrice.setLayoutParams(
					new LinearLayout.LayoutParams(0, 
							LayoutParams.WRAP_CONTENT, 2.8f));
		}
		
		private void setText(ProductReportViewHolder holder, int position, 
				Report.ReportDetail reportDetail){
			holder.tvNo.setVisibility(View.VISIBLE);
			holder.tvProductCode.setVisibility(View.VISIBLE);
			holder.tvProductPrice.setVisibility(View.VISIBLE);
			holder.tvProductName.setVisibility(View.VISIBLE);
			holder.tvNo.setLayoutParams(
					new LinearLayout.LayoutParams(0, 
							LayoutParams.WRAP_CONTENT, 0.2f));
			holder.tvProductCode.setLayoutParams(
					new LinearLayout.LayoutParams(0, 
							LayoutParams.WRAP_CONTENT, 0.8f));
			holder.tvProductName.setLayoutParams(
					new LinearLayout.LayoutParams(0, 
							LayoutParams.WRAP_CONTENT, 1f));
			holder.tvProductPrice.setLayoutParams(
					new LinearLayout.LayoutParams(0, 
							LayoutParams.WRAP_CONTENT, 0.8f));
			holder.tvNo.setText(String.valueOf(position + 1) + ".");
			holder.tvProductCode.setText(reportDetail.getProductCode());
			holder.tvProductName.setText(reportDetail.getProductName());
			holder.tvProductPrice.setText(sFormat.currencyFormat(
					reportDetail.getPricePerUnit()));
			holder.tvQty.setText(sFormat.qtyFormat(
					reportDetail.getQty()));
			holder.tvQtyPercent.setText(sFormat.currencyFormat(
					reportDetail.getQtyPercent()));
			holder.tvSubTotal.setText(sFormat.currencyFormat(
					reportDetail.getSubTotal()));
			holder.tvSubTotalPercent.setText(sFormat.currencyFormat(
					reportDetail.getSubTotalPercent()));
			holder.tvDiscount.setText(sFormat.currencyFormat(
					reportDetail.getDiscount()));
			holder.tvTotalPrice.setText(sFormat.currencyFormat(
					reportDetail.getTotalPrice()));
			holder.tvTotalPricePercent.setText(sFormat.currencyFormat(
					reportDetail.getTotalPricePercent()));
			holder.tvVatType.setText(reportDetail.getVat());
		}
		
		@Override
		public int getChildrenCount(int groupPosition) {
			int count = 0;
			if(mReport != null)
			{
				if(mReport.getGroupOfProductLst() != null){
					if(mReport.getGroupOfProductLst().get(groupPosition).getReportDetail() != null){
						count = mReport.getGroupOfProductLst().get(groupPosition).getReportDetail().size();
					}
				}
			}
			return count;
		}

		@Override
		public Report.GroupOfProduct getGroup(int groupPosition) {
			return mReport.getGroupOfProductLst().get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			int count = 0;
			if(mReport != null){
				if(mReport.getGroupOfProductLst() != null){
					count = mReport.getGroupOfProductLst().size();
				}
			}
			return count;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			
			try {
				if(!isExpanded)
					((ExpandableListView) parent).expandGroup(groupPosition);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ProductReportHeaderHolder groupHolder;
			if(convertView == null){
				groupHolder = new ProductReportHeaderHolder();
				groupHolder.tvHeader = new TextView(SaleReportActivity.this);
				groupHolder.tvHeader.setTextAppearance(SaleReportActivity.this, R.style.HeaderText);
				groupHolder.tvHeader.setPadding(8, 4, 4, 4);
				groupHolder.tvHeader.setTextSize(28);
				convertView = groupHolder.tvHeader;
				convertView.setTag(groupHolder);
			}else{
				groupHolder = (ProductReportHeaderHolder) convertView.getTag();
			}
			groupHolder.tvHeader.setText(mReport.getGroupOfProductLst().get(groupPosition).getProductGroupName() + ":" +
					mReport.getGroupOfProductLst().get(groupPosition).getProductDeptName());
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
	
	class ProductReportHeaderHolder{
		TextView tvHeader;
	}
	
	class ProductReportViewHolder{
		TextView tvNo;
		TextView tvProductCode;
		TextView tvProductName;
		TextView tvProductPrice;
		TextView tvQty;
		TextView tvQtyPercent;
		TextView tvSubTotal;
		TextView tvSubTotalPercent;
		TextView tvDiscount;
		TextView tvTotalPrice;
		TextView tvTotalPricePercent;
		TextView tvVatType;
	}
	
	public class BillReportAdapter extends BaseAdapter{
		
		private LayoutInflater mInflater;
		
		public BillReportAdapter(){
			mInflater = (LayoutInflater)
					SaleReportActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return mReport != null ? mReport.getReportDetail().size() : 0;
		}

		@Override
		public Report.ReportDetail getItem(int position) {
			return mReport.getReportDetail().get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();

			getBillReportSummary();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.bill_report_template, null);
				holder = new ViewHolder();
				holder.imgSendStatus = (ImageView) convertView.findViewById(R.id.imgSendStatus);
				holder.tvReceipt = (TextView) convertView.findViewById(R.id.tvReceipt);
				holder.tvTotalPrice = (TextView) convertView.findViewById(R.id.tvTotalPrice);
				holder.tvDiscount = (TextView) convertView.findViewById(R.id.tvTotalDisc);
				holder.tvSubTotal = (TextView) convertView.findViewById(R.id.tvSubTotal);
				holder.tvVatable = (TextView) convertView.findViewById(R.id.tvVatable);
				holder.tvTotalVat = (TextView) convertView.findViewById(R.id.tvTotalVat);
				holder.tvTotalPayment = (TextView) convertView.findViewById(R.id.tvTotalPayment);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			final Report.ReportDetail report = mReport.getReportDetail().get(position);
			double vatable = report.getVatable();
			double totalVat = report.getTotalVat();
			double totalPrice = report.getTotalPrice();
			double totalDiscount = report.getDiscount();
			double subTotal = report.getSubTotal();
			double totalPay = report.getTotalPayment();
			
			holder.tvReceipt.setText(report.getReceiptNo());
			holder.tvReceipt.setSelected(true);
			holder.tvTotalPrice.setText(sFormat.currencyFormat(totalPrice));
			holder.tvDiscount.setText(sFormat.currencyFormat(totalDiscount));
			holder.tvSubTotal.setText(sFormat.currencyFormat(subTotal));
			holder.tvVatable.setText(sFormat.currencyFormat(vatable));
			holder.tvTotalVat.setText(sFormat.currencyFormat(totalVat));
			holder.tvTotalPayment.setText(sFormat.currencyFormat(totalPay));
			
//			List<Payment.PaymentDetail> payTypeLst = 
//					mPayment.listPaymentGroupByType(report.getTransactionId(), report.getComputerId());
//	
//			int idx = 7; // position to add
//			for(Payment.PaymentDetail payType : payTypeLst){
//				TextView tvPayTypeHead = (TextView) mInflater.inflate(R.layout.tv_column_header, null);
//				mBillHeader.addView(tvPayTypeHead, idx);
//				TextView tvPaytype = (TextView) mInflater.inflate(R.layout.tv_column_detail, null);
//				((LinearLayout) convertView).addView(tvPaytype, idx);
//				tvPayTypeHead.setText(payType.getPayTypeName());
//				tvPaytype.setText(MPOSApplication.getGlobalProperty().currencyFormat(payType.getPayAmount()));
//				idx++;
//			}
			
			holder.tvTotalPayment.setTextColor(Color.BLUE);
			holder.tvTotalPayment.setPaintFlags(holder.tvTotalPayment.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			holder.tvTotalPayment.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					PaymentDetailFragment f = 
							PaymentDetailFragment.newInstance(report.getTransactionId());
					f.show(getFragmentManager(), "PaymentDialogFragment");
				}
				
			});
			
			if(report.getSendStatus() == MPOSDatabase.ALREADY_SEND){
				holder.imgSendStatus.setImageResource(R.drawable.ic_action_accept);
			}else{
				holder.imgSendStatus.setImageResource(R.drawable.ic_action_warning);
			}
			if(report.getTransStatus() == TransactionDao.TRANS_STATUS_VOID){
				holder.tvReceipt.setTextColor(Color.RED);
				holder.tvReceipt.setPaintFlags(holder.tvReceipt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			}else{
				holder.tvReceipt.setTextColor(Color.BLACK);
				holder.tvReceipt.setPaintFlags(holder.tvReceipt.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
			}
			return convertView;
		}
		
		class ViewHolder{
			ImageView imgSendStatus;
			TextView tvReceipt;
			TextView tvTotalPrice;
			TextView tvDiscount;
			TextView tvSubTotal;
			TextView tvVatable;
			TextView tvTotalVat;
			TextView tvTotalPayment;
		}
	}

	/*
	 * Payment detail dialog
	 */
	public static class PaymentDetailFragment extends DialogFragment{
		
		private PaymentDao mPayment;
		private List<Payment.PaymentDetail> mPaymentLst;
		private PaymentDetailAdapter mPaymentAdapter;
		
		private int mTransactionId;
		
		private LayoutInflater mInflater;
		
		public static PaymentDetailFragment newInstance(int transactionId){
			PaymentDetailFragment f = new PaymentDetailFragment();
			Bundle b = new Bundle();
			b.putInt("transactionId", transactionId);
			f.setArguments(b);
			return f;
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			mTransactionId = getArguments().getInt("transactionId");
			
			mPayment = new PaymentDao(getActivity());
			mPaymentLst = mPayment.listPaymentGroupByType(mTransactionId);
			mPaymentAdapter = new PaymentDetailAdapter();
			
			mInflater = (LayoutInflater) 
					getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			super.onCreate(savedInstanceState);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			View v = mInflater.inflate(R.layout.listview, null);
			final ListView lv = (ListView) v;
			lv.setAdapter(mPaymentAdapter);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.payment);
			builder.setView(v);
			builder.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					getDialog().dismiss();
				}
			});

			return builder.create();
		}

		private class PaymentDetailAdapter extends BaseAdapter{

			@Override
			public int getCount() {
				return mPaymentLst != null ? mPaymentLst.size() : 0;
			}

			@Override
			public Payment.PaymentDetail getItem(int position) {
				return mPaymentLst.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				convertView = mInflater.inflate(R.layout.template_flex_left_right, null);
				TextView tvLeft = (TextView) convertView.findViewById(R.id.textView1);
				TextView tvRight = (TextView) convertView.findViewById(R.id.textView2);
				
				Payment.PaymentDetail payment = mPaymentLst.get(position);
				
				tvLeft.setText(payment.getPayTypeName());
				tvRight.setText(sFormat.currencyFormat(payment.getPayAmount()));
				
				return convertView;
			}
			
		}
	}
	
	@Override
	public void onClick(View v) {
		DialogFragment dialogFragment;
		
		switch(v.getId()){
		case R.id.btnDateFrom:
			dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
				
				@Override
				public void onSetDate(long date) {
					mCalendar.setTimeInMillis(date);
					mDateFrom = String.valueOf(mCalendar.getTimeInMillis());
					
					mBtnDateFrom.setText(sFormat.dateFormat(mCalendar.getTime()));
					mReporting.setDateFrom(mDateFrom);
				}
			});
			dialogFragment.show(getFragmentManager(), "Condition");
			break;
		case R.id.btnDateTo:
			dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
				
				@Override
				public void onSetDate(long date) {
					mCalendar.setTimeInMillis(date);
					mDateTo = String.valueOf(mCalendar.getTimeInMillis());
					
					mBtnDateTo.setText(sFormat.dateFormat(mCalendar.getTime()));
					mReporting.setDateTo(mDateTo);
				}
			});
			dialogFragment.show(getFragmentManager(), "Condition");
			break;
		}
	}
}
