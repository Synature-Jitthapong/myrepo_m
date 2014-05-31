package com.syn.mpos;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.syn.mpos.dao.Formater;
import com.syn.mpos.dao.MPOSDatabase;
import com.syn.mpos.dao.MPOSOrderTransaction;
import com.syn.mpos.dao.PaymentDetail;
import com.syn.mpos.dao.Products;
import com.syn.mpos.dao.Reporting;
import com.syn.mpos.dao.Reporting.SimpleProductData;
import com.syn.mpos.dao.Shop;
import com.syn.mpos.dao.Transaction;
import com.synature.exceptionhandler.ExceptionHandler;
import com.synature.pos.Payment;
import com.synature.pos.Report;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import android.widget.Spinner;
import android.widget.TextView;

public class SaleReportActivity extends Activity implements OnClickListener{

	public static final int REPORT_BY_BILL = 0;
	public static final int REPORT_BY_PRODUCT = 1;
	public static final int REPORT_ENDDAY = 2;
	
	private Formater mFormat;

	private Reporting mReporting;
	
	private int mStaffId;
	
	private Calendar mCalendar;
	private String mDateFrom;
	private String mDateTo;
	
	private TextView mTvTo;
	private Button mBtnDateFrom;
	private Button mBtnDateTo;
	private Spinner mSpReportType;
	
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

		mFormat = new Formater(SaleReportActivity.this);
		Calendar c = Calendar.getInstance();
		mCalendar = new GregorianCalendar(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		mDateFrom = String.valueOf(mCalendar.getTimeInMillis());
		mDateTo = String.valueOf(mCalendar.getTimeInMillis());

		mReporting = new Reporting(SaleReportActivity.this, mDateFrom, mDateTo);
		
		mStaffId = getIntent().getIntExtra("staffId", 0);
		
		if(savedInstanceState == null){
			getFragmentManager().beginTransaction()
				.add(R.id.reportContent, BillReportFragment.getInstance()).commit();
		}
	}
	
	private void switchReportType(int type){
		switch(type){
		case REPORT_BY_BILL:
			getFragmentManager().beginTransaction()
			.replace(R.id.reportContent, BillReportFragment.getInstance()).commit();
			if(mBtnDateFrom != null){
				mBtnDateFrom.setVisibility(View.VISIBLE);
				mTvTo.setVisibility(View.VISIBLE);
			}
			setTitle(R.string.sale_report_by_bill);
			break;
		case REPORT_BY_PRODUCT:
			getFragmentManager().beginTransaction()
			.replace(R.id.reportContent, ProductReportFragment.getInstance()).commit();
			if(mBtnDateFrom != null){
				mBtnDateFrom.setVisibility(View.VISIBLE);
				mTvTo.setVisibility(View.VISIBLE);
			}
			setTitle(R.string.sale_report_by_product);
			break;
		case REPORT_ENDDAY:
			getFragmentManager().beginTransaction()
			.replace(R.id.reportContent, EnddayReportFragment.getInstance()).commit();
			if(mBtnDateFrom != null){
				mBtnDateFrom.setVisibility(View.GONE);
				mTvTo.setVisibility(View.GONE);
			}
			setTitle(R.string.endday_report);
			break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_sale_report, menu);
		MenuItem itemReportType = (MenuItem) menu.findItem(R.id.itemReportType);
		MenuItem itemCondition = (MenuItem) menu.findItem(R.id.itemDateCondition);
		mSpReportType = (Spinner) itemReportType.getActionView().findViewById(R.id.spinner1);
		mBtnDateFrom = (Button) itemCondition.getActionView().findViewById(R.id.btnDateFrom);
		mBtnDateTo = (Button) itemCondition.getActionView().findViewById(R.id.btnDateTo);
		mTvTo = (TextView) itemCondition.getActionView().findViewById(R.id.tvTo);
		mBtnDateFrom.setText(mFormat.dateFormat(mCalendar.getTime()));
		mBtnDateTo.setText(mFormat.dateFormat(mCalendar.getTime()));
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
				getString(R.string.sale_report_by_product),
				getString(R.string.endday_report)
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

	private static LinearLayout createRowSummary(Context context, TextView[] tvs){
		LinearLayout row = new LinearLayout(context);
		for(TextView tvSummary : tvs){
			row.addView(tvSummary);
		}
		return row;
	}
	
	private static TextView createTextViewSummary(Context context, 
			String content, LinearLayout.LayoutParams params){
		TextView tvSummary = new TextView(context);
		tvSummary.setText(content);
		tvSummary.setLayoutParams(params);
		tvSummary.setGravity(Gravity.RIGHT);
		tvSummary.setTextAppearance(context, R.style.HeaderText);
		return tvSummary;
	}

	/*
	 * Payment detail dialog
	 */
	public static class PaymentDetailFragment extends DialogFragment{
		
		private PaymentDetail mPayment;
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
			
			mPayment = new PaymentDetail(getActivity());
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
				tvRight.setText(((SaleReportActivity) getActivity()).mFormat.currencyFormat(payment.getPayAmount()));
				
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
					
					mBtnDateFrom.setText(mFormat.dateFormat(mCalendar.getTime()));
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
					
					mBtnDateTo.setText(mFormat.dateFormat(mCalendar.getTime()));
					mReporting.setDateTo(mDateTo);
				}
			});
			dialogFragment.show(getFragmentManager(), "Condition");
			break;
		}
	}
	
	/**
	 * @author j1tth4
	 * Endday Report Fragment
	 */
	public static class EnddayReportFragment extends Fragment{

		private static EnddayReportFragment sInstance;
		
		private Transaction mTrans;
		private PaymentDetail mPayment;
	
		private LinearLayout mEnddayReportFooterContainer;
		private ListView mLvEnddayReport;
		
		public static EnddayReportFragment getInstance(){
			if(sInstance == null){
				sInstance = new EnddayReportFragment();
			}
			return sInstance;
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);
			
			mTrans = new Transaction(getActivity());
			mPayment = new PaymentDetail(getActivity());
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_endday_report, container, false);
			mEnddayReportFooterContainer = (LinearLayout) rootView.findViewById(R.id.enddayReportFooterContainer);
			mLvEnddayReport = (ListView) rootView.findViewById(R.id.lvEnddayReport);
			return rootView;
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			inflater.inflate(R.menu.fragment_sale_report, menu);
			super.onCreateOptionsMenu(menu, inflater);
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			SaleReportActivity activity = (SaleReportActivity) getActivity();
			switch(item.getItemId()){
			case R.id.itemCreateReport:
				createReport();
				return true;
			case R.id.itemPrint:
				new PrintReport(getActivity(), activity.mDateTo, activity.mDateTo, 
						activity.mStaffId, PrintReport.WhatPrint.SUMMARY_SALE).execute();
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}
		
		private void createReport(){
			LayoutInflater inflater = (LayoutInflater)
					getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			SaleReportActivity activity = ((SaleReportActivity) getActivity());
			Shop shop = new Shop(getActivity());
			MPOSOrderTransaction trans = mTrans.getTransaction(activity.mDateTo);
			MPOSOrderTransaction.MPOSOrderDetail sumOrder 
				= mTrans.getSummaryOrderInDay(activity.mDateTo, activity.mDateTo);
			
			mEnddayReportFooterContainer.removeAllViews();
			// add footer
			View totalView = inflater.inflate(R.layout.left_mid_right_template, null);
			((TextView) totalView.findViewById(R.id.tvMid)).setText(getString(R.string.total));
			((TextView) totalView.findViewById(R.id.tvMid)).append(" " +
					((SaleReportActivity) getActivity()).mFormat.qtyFormat(sumOrder.getQty()));
			((TextView) totalView.findViewById(R.id.tvRight)).setText(
					((SaleReportActivity) getActivity()).mFormat.currencyFormat(sumOrder.getTotalRetailPrice()));
			mEnddayReportFooterContainer.addView(totalView);
			
			if(sumOrder.getPriceDiscount() > 0){
				View discountView = inflater.inflate(R.layout.left_mid_right_template, null);
				((TextView) discountView.findViewById(R.id.tvMid)).setText(getString(R.string.discount));
				((TextView) discountView.findViewById(R.id.tvRight)).setText(
						((SaleReportActivity) getActivity()).mFormat.currencyFormat(sumOrder.getPriceDiscount()));
				mEnddayReportFooterContainer.addView(discountView);
			}
			
			View subTotalView2 = inflater.inflate(R.layout.left_mid_right_template, null);
			((TextView) subTotalView2.findViewById(R.id.tvMid)).setText(getString(R.string.sub_total));
			((TextView) subTotalView2.findViewById(R.id.tvRight)).setText(
					((SaleReportActivity) getActivity()).mFormat.currencyFormat(sumOrder.getTotalSalePrice()));
			mEnddayReportFooterContainer.addView(subTotalView2);
			
			if(sumOrder.getVatExclude() > 0){
				View vatExcludeView = inflater.inflate(R.layout.left_mid_right_template, null);
				((TextView) vatExcludeView.findViewById(R.id.tvMid)).setText(getString(R.string.vat_exclude));
				((TextView) vatExcludeView.findViewById(R.id.tvRight)).setText(
						((SaleReportActivity) getActivity()).mFormat.currencyFormat(sumOrder.getVatExclude()));
				mEnddayReportFooterContainer.addView(vatExcludeView);
			}
			
			View totalSaleView = inflater.inflate(R.layout.left_mid_right_template, null);
			((TextView) totalSaleView.findViewById(R.id.tvMid)).setText(getString(R.string.total_sale));
			((TextView) totalSaleView.findViewById(R.id.tvRight)).setText(
					((SaleReportActivity) getActivity()).mFormat.currencyFormat(
							sumOrder.getTotalSalePrice() + sumOrder.getVatExclude()));
			mEnddayReportFooterContainer.addView(totalSaleView);
			
			if(shop.getCompanyVatType() == Products.VAT_TYPE_INCLUDED){
				View vatView = inflater.inflate(R.layout.left_mid_right_template, null);
				((TextView) vatView.findViewById(R.id.tvMid)).setText(getString(R.string.before_vat));
				((TextView) vatView.findViewById(R.id.tvRight)).setText(
						((SaleReportActivity) getActivity()).mFormat.currencyFormat(
								trans.getTransactionVatable() - trans.getTransactionVat()));
				mEnddayReportFooterContainer.addView(vatView);
				vatView = inflater.inflate(R.layout.left_mid_right_template, null);
				((TextView) vatView.findViewById(R.id.tvMid)).setText(getString(R.string.total_vat));
				((TextView) vatView.findViewById(R.id.tvRight)).setText(
						((SaleReportActivity) getActivity()).mFormat.currencyFormat(trans.getTransactionVat()));
				mEnddayReportFooterContainer.addView(vatView);
			}
			
			List<Payment.PaymentDetail> summaryPaymentLst = 
					mPayment.listSummaryPayment(
							mTrans.getSeperateTransactionId(activity.mDateTo));
			if(summaryPaymentLst != null){
				View paymentView = inflater.inflate(R.layout.left_mid_right_template, null);
				((TextView) paymentView.findViewById(R.id.tvMid)).setText(getString(R.string.payment_detail));
				((TextView) paymentView.findViewById(R.id.tvMid)).setPaintFlags(
						((TextView) paymentView.findViewById(R.id.tvMid)).getPaintFlags() |Paint.UNDERLINE_TEXT_FLAG);
				((TextView) paymentView.findViewById(R.id.tvRight)).setText(null);
				mEnddayReportFooterContainer.addView(paymentView);
				for(Payment.PaymentDetail payment : summaryPaymentLst){
					paymentView = inflater.inflate(R.layout.left_mid_right_template, null);
					((TextView) paymentView.findViewById(R.id.tvMid)).setText(payment.getPayTypeName());
					((TextView) paymentView.findViewById(R.id.tvRight)).setText(
							((SaleReportActivity) getActivity()).mFormat.currencyFormat(payment.getPayAmount()));
					mEnddayReportFooterContainer.addView(paymentView);
				}
			}
			
			View totalReceiptView = inflater.inflate(R.layout.left_mid_right_template, null);
			((TextView) totalReceiptView.findViewById(R.id.tvMid)).setText(getString(R.string.total_receipt_in_day));
			((TextView) totalReceiptView.findViewById(R.id.tvRight)).setText(
					String.valueOf(mTrans.getTotalReceipt(activity.mDateTo)));
			mEnddayReportFooterContainer.addView(totalReceiptView);
			
			MPOSOrderTransaction.MPOSOrderDetail sumVoidOrder = 
					mTrans.getSummaryVoidOrderInDay(activity.mDateTo);
			View totalVoidView = inflater.inflate(R.layout.left_mid_right_template, null);
			((TextView) totalVoidView.findViewById(R.id.tvMid)).setText(getString(R.string.void_bill));
			((TextView) totalVoidView.findViewById(R.id.tvMid)).setPaintFlags(
					((TextView) totalVoidView.findViewById(R.id.tvMid)).getPaintFlags() |Paint.UNDERLINE_TEXT_FLAG);
			((TextView) totalVoidView.findViewById(R.id.tvRight)).setText(null);
			mEnddayReportFooterContainer.addView(totalVoidView);
			totalVoidView = inflater.inflate(R.layout.left_mid_right_template, null);
			((TextView) totalVoidView.findViewById(R.id.tvMid)).setText(getString(R.string.void_bill_after_paid));
			((TextView) totalVoidView.findViewById(R.id.tvMid)).append(" " +
					activity.mFormat.qtyFormat(sumVoidOrder.getQty()));
			((TextView) totalVoidView.findViewById(R.id.tvRight)).setText(
					activity.mFormat.currencyFormat(sumVoidOrder.getTotalSalePrice()));
			mEnddayReportFooterContainer.addView(totalVoidView);
			
			loadReportDetail();
		}
		
		private void loadReportDetail(){
			Reporting reporting = new Reporting(getActivity(), 
					((SaleReportActivity) getActivity()).mDateTo, 
					((SaleReportActivity) getActivity()).mDateTo);
			List<SimpleProductData> simpleLst = reporting.listSummaryProductGroupInDay();
			mLvEnddayReport.setAdapter(new EnddayReportAdapter(simpleLst));
		}
		
		private class EnddayReportAdapter extends BaseAdapter{

			private LayoutInflater mInflater;
			private List<SimpleProductData> mSimpleLst;
			
			public EnddayReportAdapter(List<SimpleProductData> simpleLst){
				mInflater = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				mSimpleLst = simpleLst;
			}
			
			@Override
			public int getCount() {
				return mSimpleLst != null ? mSimpleLst.size() : 0;
			}

			@Override
			public SimpleProductData getItem(int position) {
				return mSimpleLst.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder;
				if(convertView == null){
					convertView = mInflater.inflate(R.layout.endday_report_template, null);
					holder = new ViewHolder();
					holder.tvGroupDept = (TextView) convertView.findViewById(R.id.tvGroupDept);
					holder.tvGroupTotalQty = (TextView) convertView.findViewById(R.id.tvGroupTotalQty);
					holder.tvGroupTotalPrice= (TextView) convertView.findViewById(R.id.tvGroupTotalPrice);
					holder.itemContainer = (LinearLayout) convertView.findViewById(R.id.itemContainer);
					convertView.setTag(holder);
				}else{
					holder = (ViewHolder) convertView.getTag();
				}
				
				SimpleProductData simple = mSimpleLst.get(position);
				holder.tvGroupDept.setText(simple.getDeptName());
				holder.tvGroupTotalQty.setText(
						((SaleReportActivity) getActivity()).mFormat.qtyFormat(simple.getDeptTotalQty()));
				holder.tvGroupTotalPrice.setText(
						((SaleReportActivity) getActivity()).mFormat.currencyFormat(simple.getDeptTotalPrice()));
				if(simple.getItemLst() != null){
					holder.itemContainer.removeAllViews();
					for(SimpleProductData.Item item : simple.getItemLst()){
						View bill = mInflater.inflate(R.layout.left_mid_right_template, null);
						((TextView) bill.findViewById(R.id.tvLeft)).setText(item.getItemName());
						((TextView) bill.findViewById(R.id.tvMid)).setText(
								((SaleReportActivity) getActivity()).mFormat.qtyFormat(item.getTotalQty()));
						((TextView) bill.findViewById(R.id.tvRight)).setText(
								((SaleReportActivity) getActivity()).mFormat.currencyFormat(item.getTotalPrice()));
						holder.itemContainer.addView(bill);
					}
				}
				return convertView;
			}
			
			class ViewHolder{
				TextView tvGroupDept;
				TextView tvGroupTotalQty;
				TextView tvGroupTotalPrice;
				LinearLayout itemContainer;
			}
		}
	}
	
	public static class BillReportFragment extends Fragment{

		private static BillReportFragment sInstance;

		private Report mBillReport;
		private BillReportAdapter mBillReportAdapter;
		
		private ListView mLvReport;
		private LinearLayout mBillSumContent;
		
		public static BillReportFragment getInstance(){
			if(sInstance == null){
				sInstance = new BillReportFragment();
			}
			return sInstance;
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch(item.getItemId()){
			case R.id.itemCreateReport:
				new LoadBillReportTask().execute();
				return true;
			case R.id.itemPrint:
				SaleReportActivity activity = ((SaleReportActivity) getActivity());
				new PrintReport(getActivity(), activity.mDateFrom, activity.mDateTo,
						PrintReport.WhatPrint.BILL_REPORT).execute();
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			inflater.inflate(R.menu.fragment_sale_report, menu);
			super.onCreateOptionsMenu(menu, inflater);
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			setHasOptionsMenu(true);
			mBillReport = new Report();
			mBillReportAdapter = new BillReportAdapter();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_bill_report, container, false);

			mBillSumContent = (LinearLayout) rootView.findViewById(R.id.billSummaryContent);
			mLvReport = (ListView) rootView.findViewById(R.id.lvReport);
			mLvReport.setAdapter(mBillReportAdapter);
			return rootView;
		}
		
		public class BillReportAdapter extends BaseAdapter{
			
			private LayoutInflater mInflater;
			
			public BillReportAdapter(){
				mInflater = (LayoutInflater)
						getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}
			
			@Override
			public int getCount() {
				return mBillReport != null ? mBillReport.getReportDetail().size() : 0;
			}

			@Override
			public Report.ReportDetail getItem(int position) {
				return mBillReport.getReportDetail().get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public void notifyDataSetChanged() {
				super.notifyDataSetChanged();

				summaryBill();
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
				
				final Report.ReportDetail report = mBillReport.getReportDetail().get(position);
				double vatable = report.getVatable();
				double totalVat = report.getTotalVat();
				double totalPrice = report.getTotalPrice();
				double totalDiscount = report.getDiscount();
				double subTotal = report.getSubTotal();
				double totalPay = report.getTotalPayment();
				
				holder.tvReceipt.setText(report.getReceiptNo());
				holder.tvReceipt.setSelected(true);
				holder.tvTotalPrice.setText(((SaleReportActivity) getActivity()).mFormat.currencyFormat(totalPrice));
				holder.tvDiscount.setText(((SaleReportActivity) getActivity()).mFormat.currencyFormat(totalDiscount));
				holder.tvSubTotal.setText(((SaleReportActivity) getActivity()).mFormat.currencyFormat(subTotal));
				holder.tvVatable.setText(((SaleReportActivity) getActivity()).mFormat.currencyFormat(vatable));
				holder.tvTotalVat.setText(((SaleReportActivity) getActivity()).mFormat.currencyFormat(totalVat));
				holder.tvTotalPayment.setText(((SaleReportActivity) getActivity()).mFormat.currencyFormat(totalPay));
				
//				List<Payment.PaymentDetail> payTypeLst = 
//						mPayment.listPaymentGroupByType(report.getTransactionId(), report.getComputerId());
	//	
//				int idx = 7; // position to add
//				for(Payment.PaymentDetail payType : payTypeLst){
//					TextView tvPayTypeHead = (TextView) mInflater.inflate(R.layout.tv_column_header, null);
//					mBillHeader.addView(tvPayTypeHead, idx);
//					TextView tvPaytype = (TextView) mInflater.inflate(R.layout.tv_column_detail, null);
//					((LinearLayout) convertView).addView(tvPaytype, idx);
//					tvPayTypeHead.setText(payType.getPayTypeName());
//					tvPaytype.setText(MPOSApplication.getGlobalProperty().currencyFormat(payType.getPayAmount()));
//					idx++;
//				}
				
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
				if(report.getTransStatus() == Transaction.TRANS_STATUS_VOID){
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
		
		private void summaryBill(){
			Report.ReportDetail summary = 
					((SaleReportActivity) getActivity()).mReporting.getBillSummary();
			mBillSumContent.removeAllViews();
			TextView[] tvSummary = {
					createTextViewSummary(getActivity(), getString(R.string.summary), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)),
					createTextViewSummary(getActivity(), ((SaleReportActivity) getActivity()).mFormat.currencyFormat(summary.getTotalPrice()), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f)),
					createTextViewSummary(getActivity(), ((SaleReportActivity) getActivity()).mFormat.currencyFormat(summary.getDiscount()), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f)),
					createTextViewSummary(getActivity(), ((SaleReportActivity) getActivity()).mFormat.currencyFormat(summary.getSubTotal()), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f)),
					createTextViewSummary(getActivity(), ((SaleReportActivity) getActivity()).mFormat.currencyFormat(summary.getVatable()), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f)),
					createTextViewSummary(getActivity(), ((SaleReportActivity) getActivity()).mFormat.currencyFormat(summary.getTotalVat()), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f)),
					createTextViewSummary(getActivity(), ((SaleReportActivity) getActivity()).mFormat.currencyFormat(summary.getTotalPayment()), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f))
			};
			mBillSumContent.addView(createRowSummary(getActivity(), tvSummary));
		}
		
		public class LoadBillReportTask extends AsyncTask<Void, Void, Void>{

			@Override
			protected void onPreExecute() {
			}

			@Override
			protected void onPostExecute(Void result) {
				mBillReportAdapter.notifyDataSetChanged();
			}

			@Override
			protected Void doInBackground(Void... params) {
				mBillReport = ((SaleReportActivity) getActivity()).mReporting.getSaleReportByBill();
				return null;
			}
			
		}
	}
	
	public static class ProductReportFragment extends Fragment{

		private static ProductReportFragment sInstance;

		private Report mReportProduct;
		private ProductReportAdapter mProductReportAdapter;
		
		private LinearLayout mProductSumContent;
		private ExpandableListView mLvReportProduct;
		
		public static ProductReportFragment getInstance(){
			if(sInstance == null){
				sInstance = new ProductReportFragment();
			}
			return sInstance;
		}
		
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			inflater.inflate(R.menu.fragment_sale_report, menu);
			super.onCreateOptionsMenu(menu, inflater);
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch(item.getItemId()){
			case R.id.itemCreateReport:
				new LoadProductReportTask().execute();
				return true;
			case R.id.itemPrint:
				SaleReportActivity activity = ((SaleReportActivity) getActivity());
				new PrintReport(getActivity(), 
						activity.mDateFrom, activity.mDateTo,
						PrintReport.WhatPrint.PRODUCT_REPORT).execute();
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			setHasOptionsMenu(true);
			mReportProduct = new Report();
			mProductReportAdapter = new ProductReportAdapter();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_product_report, container, false);

			mProductSumContent = (LinearLayout) rootView.findViewById(R.id.productSummaryContent);
			mLvReportProduct = (ExpandableListView) rootView.findViewById(R.id.lvReportProduct);

			mLvReportProduct.setAdapter(mProductReportAdapter);
			mLvReportProduct.setGroupIndicator(null);
			return rootView;
		}
		
		public class ProductReportAdapter extends BaseExpandableListAdapter{
			
			private LayoutInflater mInflater;
			
			public ProductReportAdapter(){
				mInflater = (LayoutInflater)
						getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}
			
			@Override
			public Report.ReportDetail getChild(int groupPosition, int childPosition) {
				try {
					return mReportProduct.getGroupOfProductLst().get(groupPosition).
							getReportDetail().get(childPosition);
				} catch (Exception e) {
					return null;
				}
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
						mReportProduct.getGroupOfProductLst().get(groupPosition).getReportDetail().get(childPosition);
				setText(holder, childPosition, reportDetail);
				
				if(reportDetail.getProductName().equals(Reporting.SUMM_DEPT)){
					setSummary(holder, mReportProduct.getGroupOfProductLst().get(groupPosition).getProductDeptName());
				}else if(reportDetail.getProductName().equals(Reporting.SUMM_GROUP)){
					setSummary(holder, mReportProduct.getGroupOfProductLst().get(groupPosition).getProductGroupName());
				}
				return convertView;
			}
			
			private void setSummary(ProductReportViewHolder holder, String text){
				holder.tvNo.setVisibility(View.GONE);
				holder.tvProductName.setVisibility(View.GONE);
				holder.tvProductCode.setVisibility(View.GONE);
				holder.tvProductPrice.setText(getActivity().getString(R.string.summary) + 
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
				holder.tvProductPrice.setText(((SaleReportActivity) getActivity()).mFormat.currencyFormat(
						reportDetail.getPricePerUnit()));
				holder.tvQty.setText(((SaleReportActivity) getActivity()).mFormat.qtyFormat(
						reportDetail.getQty()));
				holder.tvQtyPercent.setText(((SaleReportActivity) getActivity()).mFormat.currencyFormat(
						reportDetail.getQtyPercent()));
				holder.tvSubTotal.setText(((SaleReportActivity) getActivity()).mFormat.currencyFormat(
						reportDetail.getSubTotal()));
				holder.tvSubTotalPercent.setText(((SaleReportActivity) getActivity()).mFormat.currencyFormat(
						reportDetail.getSubTotalPercent()));
				holder.tvDiscount.setText(((SaleReportActivity) getActivity()).mFormat.currencyFormat(
						reportDetail.getDiscount()));
				holder.tvTotalPrice.setText(((SaleReportActivity) getActivity()).mFormat.currencyFormat(
						reportDetail.getTotalPrice()));
				holder.tvTotalPricePercent.setText(((SaleReportActivity) getActivity()).mFormat.currencyFormat(
						reportDetail.getTotalPricePercent()));
				holder.tvVatType.setText(reportDetail.getVat());
			}
			
			@Override
			public int getChildrenCount(int groupPosition) {
				int count = 0;
				if(mReportProduct != null)
				{
					if(mReportProduct.getGroupOfProductLst() != null){
						if(mReportProduct.getGroupOfProductLst().get(groupPosition).getReportDetail() != null){
							count = mReportProduct.getGroupOfProductLst().get(groupPosition).getReportDetail().size();
						}
					}
				}
				return count;
			}

			@Override
			public Report.GroupOfProduct getGroup(int groupPosition) {
				try {
					return mReportProduct.getGroupOfProductLst().get(groupPosition);
				} catch (Exception e) {
					return null;
				}
			}

			@Override
			public int getGroupCount() {
				int count = 0;
				if(mReportProduct != null){
					if(mReportProduct.getGroupOfProductLst() != null){
						count = mReportProduct.getGroupOfProductLst().size();
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
					groupHolder.tvHeader = new TextView(getActivity());
					groupHolder.tvHeader.setTextAppearance(getActivity(), R.style.HeaderText);
					groupHolder.tvHeader.setPadding(8, 4, 4, 4);
					groupHolder.tvHeader.setTextSize(20);
					convertView = groupHolder.tvHeader;
					convertView.setTag(groupHolder);
				}else{
					groupHolder = (ProductReportHeaderHolder) convertView.getTag();
				}
				groupHolder.tvHeader.setText(mReportProduct.getGroupOfProductLst().get(groupPosition).getProductGroupName() + ":" +
						mReportProduct.getGroupOfProductLst().get(groupPosition).getProductDeptName());
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
		}
		
		private void summaryProduct(){
			Report.ReportDetail summProduct = 
					((SaleReportActivity) getActivity()).mReporting.getProductSummary();
			TextView[] tvGrandTotal = {
					createTextViewSummary(getActivity(), getString(R.string.grand_total), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2.8f)),
					createTextViewSummary(getActivity(), ((SaleReportActivity) getActivity()).mFormat.qtyFormat(summProduct.getQty()), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f)),
					createTextViewSummary(getActivity(), ((SaleReportActivity) getActivity()).mFormat.qtyFormat(summProduct.getQty() / summProduct.getQty() * 100), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f)),
					createTextViewSummary(getActivity(), ((SaleReportActivity) getActivity()).mFormat.currencyFormat(summProduct.getSubTotal()), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f)),
					createTextViewSummary(getActivity(), ((SaleReportActivity) getActivity()).mFormat.qtyFormat(summProduct.getSubTotal() / summProduct.getSubTotal() * 100), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f)),
					createTextViewSummary(getActivity(), ((SaleReportActivity) getActivity()).mFormat.currencyFormat(summProduct.getDiscount()), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f)),
					createTextViewSummary(getActivity(), ((SaleReportActivity) getActivity()).mFormat.currencyFormat(summProduct.getTotalPrice()), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f)),
					createTextViewSummary(getActivity(), ((SaleReportActivity) getActivity()).mFormat.qtyFormat(summProduct.getTotalPrice() / summProduct.getTotalPrice() * 100), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f)),
					createTextViewSummary(getActivity(), "", new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f))
			};
			mProductSumContent.removeAllViews();
			mProductSumContent.addView(createRowSummary(getActivity(), tvGrandTotal));
			
			Transaction trans = new Transaction(getActivity());
			MPOSOrderTransaction.MPOSOrderDetail summOrder 
				= trans.getSummaryOrderInDay(((SaleReportActivity) getActivity()).mDateFrom, 
						((SaleReportActivity) getActivity()).mDateTo);	
			
			// total sale
			TextView[] tvSubTotal = {
					createTextViewSummary(getActivity(), getString(R.string.sub_total), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 5.9f)),
					createTextViewSummary(getActivity(), ((SaleReportActivity) getActivity()).mFormat.currencyFormat(summOrder.getTotalRetailPrice()), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f)),
					createTextViewSummary(getActivity(), "", new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f)),
					createTextViewSummary(getActivity(), "", new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f))
			};
			mProductSumContent.addView(createRowSummary(getActivity(), tvSubTotal));
			
			if(summOrder.getPriceDiscount() > 0){
				// total discount
				TextView[] tvTotalDiscount = {
						createTextViewSummary(getActivity(), getString(R.string.discount), new 
								LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 5.9f)),
						createTextViewSummary(getActivity(), ((SaleReportActivity) getActivity()).mFormat.currencyFormat(summOrder.getPriceDiscount()), new 
								LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f)),
						createTextViewSummary(getActivity(), "", new 
								LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f)),
						createTextViewSummary(getActivity(), "", new 
								LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f))
				};
				mProductSumContent.addView(createRowSummary(getActivity(), tvTotalDiscount));
			}
			
			if(summOrder.getVatExclude() > 0){
				Shop shop = new Shop(getActivity());
				// total vatExclude
				TextView[] tvTotalVatExclude = {
						createTextViewSummary(getActivity(), getString(R.string.vat_exclude) + " " +
								NumberFormat.getInstance().format(shop.getCompanyVatRate()) + "%", new 
								LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 5.9f)),
						createTextViewSummary(getActivity(), ((SaleReportActivity) getActivity()).mFormat.currencyFormat(summOrder.getVatExclude()), new 
								LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f)),
						createTextViewSummary(getActivity(), "", new 
								LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f)),
						createTextViewSummary(getActivity(), "", new 
								LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f))
				};
				mProductSumContent.addView(createRowSummary(getActivity(), tvTotalVatExclude));
			}
			
			// total sale
			TextView[] tvTotalSale = {
					createTextViewSummary(getActivity(), getString(R.string.total_sale), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 5.9f)),
					createTextViewSummary(getActivity(), ((SaleReportActivity) getActivity()).mFormat.currencyFormat(summOrder.getTotalSalePrice() + 
							summOrder.getVatExclude()), new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f)),
					createTextViewSummary(getActivity(), "", new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f)),
					createTextViewSummary(getActivity(), "", new 
							LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f))
			};
			mProductSumContent.addView(createRowSummary(getActivity(), tvTotalSale));
		
		}
		
		public class LoadProductReportTask extends AsyncTask<Void, Void, Void>{

			@Override
			protected void onPreExecute() {
			}

			@Override
			protected void onPostExecute(Void result) {
				mProductReportAdapter.notifyDataSetChanged();
			}

			@Override
			protected Void doInBackground(Void... params) {
				mReportProduct = ((SaleReportActivity) getActivity()).mReporting.getProductDataReport();
				return null;
			}
			
		}
	}
}
