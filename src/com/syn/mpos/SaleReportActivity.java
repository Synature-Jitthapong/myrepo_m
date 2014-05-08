package com.syn.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import com.syn.mpos.database.GlobalPropertyDataSource;
import com.syn.mpos.database.MPOSDatabase;
import com.syn.mpos.database.TransactionDataSource;
import com.syn.mpos.database.PaymentDetailDataSource;
import com.syn.mpos.database.Reporting;
import com.syn.pos.Payment;
import com.syn.pos.Report;

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
	//private static final String TAG = "SaleReportActivity";
	public static final int BILL_REPORT = 1;
	public static final int PRODUCT_REPORT = 2;

	private static GlobalPropertyDataSource sGlobal;
	private Report mReport;
	private Reporting mReporting;
	private BillReportAdapter mBillReportAdapter;
	private ProductReportAdapter mProductReportAdapter;
	private Calendar mCalendar;
	private long mDateFrom;
	private long mDateTo;
	
	private MenuItem mConditionItem;
	private Button mBtnDateFrom;
	private Button mBtnDateTo;
	private Button mBtnCreateReport;
	private ListView mLvReport;
	private ExpandableListView mLvReportProduct;
	private MenuItem mReportTypeItem;
	private Spinner mSpReportType;
	private LinearLayout mLayoutBillReport;
	private LinearLayout mLayoutProductReport;
	//private LinearLayout mBillHeader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sale_report);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		mLayoutBillReport = (LinearLayout) findViewById(R.id.billLayout);
		mLayoutProductReport = (LinearLayout) findViewById(R.id.productLayout);
		//mBillHeader = (LinearLayout) findViewById(R.id.billHeader);
		
		mLvReport = (ListView) findViewById(R.id.lvReport);
		mLvReportProduct = (ExpandableListView) findViewById(R.id.lvReportProduct);
		
		sGlobal = new GlobalPropertyDataSource(SaleReportActivity.this);
		Calendar c = Calendar.getInstance();
		mCalendar = new GregorianCalendar(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		mDateFrom = mCalendar.getTimeInMillis();
		mDateTo = mCalendar.getTimeInMillis();
		
		mReport = new Report();
		mBillReportAdapter = new BillReportAdapter();
		mProductReportAdapter = new ProductReportAdapter();
		mLvReport.setAdapter(mBillReportAdapter);
		mLvReportProduct.setAdapter(mProductReportAdapter);
		mLvReportProduct.setGroupIndicator(null);
	}

	private void genBillReport(){
		mReporting = new Reporting(SaleReportActivity.this, mDateFrom, mDateTo);
		mReport = mReporting.getSaleReportByBill();
		mBillReportAdapter.notifyDataSetChanged();
	}
	
	private void genProductReport(){
		mReporting = new Reporting(SaleReportActivity.this, mDateFrom, mDateTo);
		mReport = mReporting.getProductDataReport();
		mProductReportAdapter.notifyDataSetChanged();
	}
	
	private void getBillReportSummary(){
		if(mReport != null){
			TextView tvSummTotalPrice = (TextView) findViewById(R.id.tvSummTotalPrice);
			TextView tvSummDiscount = (TextView) findViewById(R.id.tvSummDiscount);
			TextView tvSummSubTotal = (TextView) findViewById(R.id.tvSummSubTotal);
			TextView tvSummVatable = (TextView) findViewById(R.id.tvSummVatable);
			TextView tvSummTotalVat = (TextView) findViewById(R.id.tvSummTotalVat);
			TextView tvSummTotalPay = (TextView) findViewById(R.id.tvSummTotalPay);
			
			double totalPrice = 0.0f;
			double totalDiscount = 0.0f;
			double totalSub = 0.0f;
			double totalVatable = 0.0f;
			double totalVat = 0.0f;
			double totalPay = 0.0f;
			
			PaymentDetailDataSource payment = new PaymentDetailDataSource(SaleReportActivity.this);
			for(Report.ReportDetail reportDetail : mReport.reportDetail){
				if(reportDetail.getTransStatus() != TransactionDataSource.TRANS_STATUS_VOID){
					totalPrice += reportDetail.getTotalPrice();
					totalDiscount += reportDetail.getDiscount();
					totalSub += reportDetail.getSubTotal();
					totalVatable += reportDetail.getVatable();
					totalVat += reportDetail.getTotalVat();
					totalPay += payment.getTotalPaid(reportDetail.getTransactionId());
				}
			}
			tvSummTotalPrice.setText(sGlobal.currencyFormat(totalPrice));
			tvSummDiscount.setText(sGlobal.currencyFormat(totalDiscount));
			tvSummSubTotal.setText(sGlobal.currencyFormat(totalSub));
			tvSummVatable.setText(sGlobal.currencyFormat(totalVatable));
			tvSummTotalVat.setText(sGlobal.currencyFormat(totalVat));
			tvSummTotalPay.setText(sGlobal.currencyFormat(totalPay));
		}
	}
	
	private class ReportTypeAdapter extends BaseAdapter{
		private List<HashMap<Integer, String>> reportType;
		
		public ReportTypeAdapter(){
			reportType = new ArrayList<HashMap<Integer, String>>();

			HashMap<Integer, String> type = new HashMap<Integer, String>();
			type.put(1, SaleReportActivity.this.getString(R.string.sale_report_by_bill));
			reportType.add(type);
			type = new HashMap<Integer, String>();
			type.put(2, SaleReportActivity.this.getString(R.string.sale_report_by_product));
			reportType.add(type);
		}
		
		@Override
		public int getCount() {
			return reportType.size();
		}

		@Override
		public HashMap<Integer, String> getItem(int position) {
			return reportType.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			HashMap<Integer, String> type = reportType.get(position);
			LayoutInflater inflater = (LayoutInflater)
					SaleReportActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			convertView = inflater.inflate(R.layout.text_view, null);
			TextView textView = (TextView)convertView;
			textView.setText(type.get(position + 1));
			return convertView;
		}
		
	}
	
	private void switchReportType(int type){
		switch(type){
		case 1:
			mLayoutBillReport.setVisibility(View.VISIBLE);
			mLayoutProductReport.setVisibility(View.GONE);
			mBtnCreateReport.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					genBillReport();
				}
				
			});
			break;
		case 2:
			mLayoutBillReport.setVisibility(View.GONE);
			mLayoutProductReport.setVisibility(View.VISIBLE);
			mBtnCreateReport.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					genProductReport();
				}
				
			});			
			break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_sale_report, menu);
		mReportTypeItem = (MenuItem) menu.findItem(R.id.itemReportType);
		mConditionItem = (MenuItem) menu.findItem(R.id.itemDateCondition);
		mSpReportType = (Spinner) mReportTypeItem.getActionView().findViewById(R.id.spinner1);
		mBtnDateFrom = (Button) mConditionItem.getActionView().findViewById(R.id.btnDateFrom);
		mBtnDateTo = (Button) mConditionItem.getActionView().findViewById(R.id.btnDateTo);
		mBtnCreateReport = (Button) ((MenuItem) menu.findItem(R.id.itemCreateReport)).getActionView();
		mBtnCreateReport.setText(R.string.create_report);
		mBtnDateFrom.setText(sGlobal.dateFormat(mCalendar.getTime()));
		mBtnDateTo.setText(sGlobal.dateFormat(mCalendar.getTime()));
		mBtnDateFrom.setOnClickListener(this);
		mBtnDateTo.setOnClickListener(this);
		mSpReportType.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				switchReportType(position + 1);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		mSpReportType.setAdapter(new ReportTypeAdapter());
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
		Report.ReportDetail summProduct = mReporting.getProductSummaryAll();
		if(summProduct != null){
			TextView tvSummProQty = (TextView) findViewById(R.id.tvSummProQty);
			TextView tvSummProQtyPercent = (TextView) findViewById(R.id.tvSummProQtyPercent);
			TextView tvSummProSubTotal = (TextView) findViewById(R.id.tvSummProSubTotal);
			TextView tvSummProSubTotalPercent = (TextView) findViewById(R.id.tvSummProSubTotalPercent);
			TextView tvSummProDiscount = (TextView) findViewById(R.id.tvSummProDiscount);
			TextView tvSummProTotalPrice = (TextView) findViewById(R.id.tvSummProTotalPrice);
			TextView tvSummProTotalPricePercent = (TextView) findViewById(R.id.tvSummProTotalPricePercent);
			
			tvSummProQty.setText(sGlobal.qtyFormat(summProduct.getQty()));
			tvSummProQtyPercent.setText(sGlobal.qtyFormat(summProduct.getQty() / summProduct.getQty() * 100));
			tvSummProSubTotal.setText(sGlobal.currencyFormat(summProduct.getSubTotal()));
			tvSummProSubTotalPercent.setText(sGlobal.qtyFormat(summProduct.getSubTotal() / summProduct.getSubTotal() * 100));
			tvSummProDiscount.setText(sGlobal.currencyFormat(summProduct.getDiscount()));
			tvSummProTotalPrice.setText(sGlobal.currencyFormat(summProduct.getTotalPrice()));
			tvSummProTotalPricePercent.setText(sGlobal.qtyFormat(summProduct.getTotalPrice() / summProduct.getTotalPrice() * 100));
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
			return mReport.groupOfProductLst.get(groupPosition).reportDetail.get(childPosition);
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
					mReport.groupOfProductLst.get(groupPosition).reportDetail.get(childPosition);
			setText(holder, childPosition, reportDetail);
			
			if(reportDetail.getProductName().equals(Reporting.SUMM_DEPT)){
				setSummary(holder, mReport.groupOfProductLst.get(groupPosition).getProductDeptName());
			}else if(reportDetail.getProductName().equals(Reporting.SUMM_GROUP)){
				setSummary(holder, mReport.groupOfProductLst.get(groupPosition).getProductGroupName());
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
			holder.tvProductPrice.setText(sGlobal.currencyFormat(
					reportDetail.getPricePerUnit()));
			holder.tvQty.setText(sGlobal.qtyFormat(
					reportDetail.getQty()));
			holder.tvQtyPercent.setText(sGlobal.currencyFormat(
					reportDetail.getQtyPercent()));
			holder.tvSubTotal.setText(sGlobal.currencyFormat(
					reportDetail.getSubTotal()));
			holder.tvSubTotalPercent.setText(sGlobal.currencyFormat(
					reportDetail.getSubTotalPercent()));
			holder.tvDiscount.setText(sGlobal.currencyFormat(
					reportDetail.getDiscount()));
			holder.tvTotalPrice.setText(sGlobal.currencyFormat(
					reportDetail.getTotalPrice()));
			holder.tvTotalPricePercent.setText(sGlobal.currencyFormat(
					reportDetail.getTotalPricePercent()));
			holder.tvVatType.setText(reportDetail.getVat());
		}
		
		@Override
		public int getChildrenCount(int groupPosition) {
			int count = 0;
			if(mReport != null)
			{
				if(mReport.groupOfProductLst != null){
					if(mReport.groupOfProductLst.get(groupPosition).reportDetail != null){
						count = mReport.groupOfProductLst.get(groupPosition).reportDetail.size();
					}
				}
			}
			return count;
		}

		@Override
		public Report.GroupOfProduct getGroup(int groupPosition) {
			return mReport.groupOfProductLst.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			int count = 0;
			if(mReport != null){
				if(mReport.groupOfProductLst != null){
					count = mReport.groupOfProductLst.size();
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
			
			TextView tvGroupHeader = new TextView(SaleReportActivity.this);
			tvGroupHeader.setTextAppearance(SaleReportActivity.this, R.style.HeaderText);
			tvGroupHeader.setPadding(8, 4, 4, 4);
			tvGroupHeader.setTextSize(28);
			tvGroupHeader.setText(mReport.groupOfProductLst.get(groupPosition).getProductGroupName() + ":" +
					mReport.groupOfProductLst.get(groupPosition).getProductDeptName());
			return tvGroupHeader;
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
	
	static class ProductReportViewHolder{
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
			return mReport != null ? mReport.reportDetail.size() : 0;
		}

		@Override
		public Report.ReportDetail getItem(int position) {
			return mReport.reportDetail.get(position);
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
			
			final Report.ReportDetail report = mReport.reportDetail.get(position);
			double vatable = report.getVatable();
			double totalVat = report.getTotalVat();
			double totalPrice = report.getTotalPrice();
			double totalDiscount = report.getDiscount();
			double subTotal = report.getSubTotal();
			double totalPay = report.getTotalPayment();
			
			holder.tvReceipt.setText(report.getReceiptNo());
			holder.tvReceipt.setSelected(true);
			holder.tvTotalPrice.setText(sGlobal.currencyFormat(totalPrice));
			holder.tvDiscount.setText(sGlobal.currencyFormat(totalDiscount));
			holder.tvSubTotal.setText(sGlobal.currencyFormat(subTotal));
			holder.tvVatable.setText(sGlobal.currencyFormat(vatable));
			holder.tvTotalVat.setText(sGlobal.currencyFormat(totalVat));
			holder.tvTotalPayment.setText(sGlobal.currencyFormat(totalPay));
			
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
			if(report.getTransStatus() == TransactionDataSource.TRANS_STATUS_VOID){
				holder.tvReceipt.setTextColor(Color.RED);
				holder.tvReceipt.setPaintFlags(holder.tvReceipt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
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
		
		private PaymentDetailDataSource mPayment;
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
			
			mPayment = new PaymentDetailDataSource(getActivity());
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
				tvRight.setText(sGlobal.currencyFormat(payment.getPayAmount()));
				
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
					mDateFrom = mCalendar.getTimeInMillis();
					
					mBtnDateFrom.setText(sGlobal.dateFormat(mCalendar.getTime()));
				}
			});
			dialogFragment.show(getFragmentManager(), "Condition");
			break;
		case R.id.btnDateTo:
			dialogFragment = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
				
				@Override
				public void onSetDate(long date) {
					mCalendar.setTimeInMillis(date);
					mDateTo = mCalendar.getTimeInMillis();
					
					mBtnDateTo.setText(sGlobal.dateFormat(mCalendar.getTime()));
				}
			});
			dialogFragment.show(getFragmentManager(), "Condition");
			break;
		}
	}
}
