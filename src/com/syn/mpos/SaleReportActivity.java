package com.syn.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import com.syn.mpos.database.MPOSDatabase;
import com.syn.mpos.database.PaymentDetail;
import com.syn.mpos.database.Reporting;
import com.syn.mpos.database.Transaction;
import com.syn.pos.Payment;
import com.syn.pos.Report;

import android.os.Bundle;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Gravity;
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
	private LinearLayout mBillHeader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sale_report);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		mLayoutBillReport = (LinearLayout) findViewById(R.id.billLayout);
		mLayoutProductReport = (LinearLayout) findViewById(R.id.productLayout);
		mBillHeader = (LinearLayout) findViewById(R.id.billHeader);
		
		mLvReport = (ListView) findViewById(R.id.lvReport);
		mLvReportProduct = (ExpandableListView) findViewById(R.id.lvReportProduct);
		
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
		mReporting = new Reporting(MPOSApplication.getWriteDatabase(), mDateFrom, mDateTo);
		mReport = mReporting.getSaleReportByBill();
		mBillReportAdapter.notifyDataSetChanged();
	}
	
	private void genProductReport(){
		mReporting = new Reporting(MPOSApplication.getWriteDatabase(), mDateFrom, mDateTo);
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
			TextView tvSummTotalCash = (TextView) findViewById(R.id.tvSummTotalCash);
			TextView tvSummTotalCredit = (TextView) findViewById(R.id.tvSummTotalCredit);
			TextView tvSummTotalPay = (TextView) findViewById(R.id.tvSummTotalPay);
			
			double totalPrice = 0.0f;
			double totalDiscount = 0.0f;
			double totalSub = 0.0f;
			double totalVatable = 0.0f;
			double totalVat = 0.0f;
			double totalPay = 0.0f;
			double totalCash = 0.0f;
			double totalCredit = 0.0f;
			
			PaymentDetail payment = new PaymentDetail(MPOSApplication.getReadDatabase());
			for(Report.ReportDetail reportDetail : mReport.reportDetail){
				if(reportDetail.getTransStatus() != Transaction.TRANS_STATUS_VOID){
					totalPrice += reportDetail.getTotalPrice();
					totalDiscount += reportDetail.getDiscount();
					totalSub += reportDetail.getSubTotal();
					totalVatable += reportDetail.getVatable();
					totalVat += reportDetail.getTotalVat();
					totalCash += mReporting.getTotalPayByPayType(reportDetail.getTransactionId(), 
							reportDetail.getComputerId(), PaymentDetail.PAY_TYPE_CASH);
					totalCredit += mReporting.getTotalPayByPayType(reportDetail.getTransactionId(), 
							reportDetail.getComputerId(), PaymentDetail.PAY_TYPE_CREDIT);
					totalPay += payment.getTotalPaid(reportDetail.getTransactionId(), 
							reportDetail.getComputerId());
				}
			}
			tvSummTotalPrice.setText(MPOSApplication.getGlobalProperty().currencyFormat(totalPrice));
			tvSummDiscount.setText(MPOSApplication.getGlobalProperty().currencyFormat(totalDiscount));
			tvSummSubTotal.setText(MPOSApplication.getGlobalProperty().currencyFormat(totalSub));
			tvSummVatable.setText(MPOSApplication.getGlobalProperty().currencyFormat(totalVatable));
			tvSummTotalVat.setText(MPOSApplication.getGlobalProperty().currencyFormat(totalVat));
			tvSummTotalPay.setText(MPOSApplication.getGlobalProperty().currencyFormat(totalPay));
			tvSummTotalCash.setText(MPOSApplication.getGlobalProperty().currencyFormat(totalCash));
			tvSummTotalCredit.setText(MPOSApplication.getGlobalProperty().currencyFormat(totalCredit));
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
		mBtnDateFrom.setText(MPOSApplication.getGlobalProperty().dateFormat(mCalendar.getTime()));
		mBtnDateTo.setText(MPOSApplication.getGlobalProperty().dateFormat(mCalendar.getTime()));
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
			
			tvSummProQty.setText(MPOSApplication.getGlobalProperty().qtyFormat(summProduct.getQty()));
			tvSummProQtyPercent.setText(MPOSApplication.getGlobalProperty().qtyFormat(summProduct.getQty() / summProduct.getQty() * 100));
			tvSummProSubTotal.setText(MPOSApplication.getGlobalProperty().currencyFormat(summProduct.getSubTotal()));
			tvSummProSubTotalPercent.setText(MPOSApplication.getGlobalProperty().qtyFormat(summProduct.getSubTotal() / summProduct.getSubTotal() * 100));
			tvSummProDiscount.setText(MPOSApplication.getGlobalProperty().currencyFormat(summProduct.getDiscount()));
			tvSummProTotalPrice.setText(MPOSApplication.getGlobalProperty().currencyFormat(summProduct.getTotalPrice()));
			tvSummProTotalPricePercent.setText(MPOSApplication.getGlobalProperty().qtyFormat(summProduct.getTotalPrice() / summProduct.getTotalPrice() * 100));
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
				convertView = mInflater.inflate(R.layout.sale_report_by_product_template, parent, false);
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
			holder.tvProductPrice.setGravity(Gravity.RIGHT);
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
			holder.tvProductPrice.setText(MPOSApplication.getGlobalProperty().currencyFormat(
					reportDetail.getPricePerUnit()));
			holder.tvQty.setText(MPOSApplication.getGlobalProperty().qtyFormat(
					reportDetail.getQty()));
			holder.tvQtyPercent.setText(MPOSApplication.getGlobalProperty().currencyFormat(
					reportDetail.getQtyPercent()));
			holder.tvSubTotal.setText(MPOSApplication.getGlobalProperty().currencyFormat(
					reportDetail.getSubTotal()));
			holder.tvSubTotalPercent.setText(MPOSApplication.getGlobalProperty().currencyFormat(
					reportDetail.getSubTotalPercent()));
			holder.tvDiscount.setText(MPOSApplication.getGlobalProperty().currencyFormat(
					reportDetail.getDiscount()));
			holder.tvTotalPrice.setText(MPOSApplication.getGlobalProperty().currencyFormat(
					reportDetail.getTotalPrice()));
			holder.tvTotalPricePercent.setText(MPOSApplication.getGlobalProperty().currencyFormat(
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
			tvGroupHeader.setTextAppearance(SaleReportActivity.this, R.style.ColumnHeader);
			tvGroupHeader.setPadding(8, 4, 4, 4);
			tvGroupHeader.setTextSize(32);
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
		private PaymentDetail mPayment;
		
		public BillReportAdapter(){
			mPayment = new PaymentDetail(MPOSApplication.getWriteDatabase());
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
				convertView = mInflater.inflate(R.layout.sale_report_by_bill_template, null);
				holder = new ViewHolder();
				holder.imgSendStatus = (ImageView) convertView.findViewById(R.id.imgSendStatus);
				holder.tvReceipt = (TextView) convertView.findViewById(R.id.tvReceipt);
				holder.tvTotalPrice = (TextView) convertView.findViewById(R.id.tvTotalPrice);
				holder.tvDiscount = (TextView) convertView.findViewById(R.id.tvTotalDisc);
				holder.tvSubTotal = (TextView) convertView.findViewById(R.id.tvSubTotal);
				holder.tvTotalSale = (TextView) convertView.findViewById(R.id.tvTotalSale);
				holder.tvVatable = (TextView) convertView.findViewById(R.id.tvVatable);
				holder.tvTotalVat = (TextView) convertView.findViewById(R.id.tvTotalVat);
				holder.tvCash = (TextView) convertView.findViewById(R.id.tvCash);
				holder.tvCredit = (TextView) convertView.findViewById(R.id.tvCredit);
				holder.tvTotalPayment = (TextView) convertView.findViewById(R.id.tvTotalPayment);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			Report.ReportDetail report = mReport.reportDetail.get(position);
			double vatable = report.getVatable();
			double totalVat = report.getTotalVat();
			double totalPrice = report.getTotalPrice();
			double totalDiscount = report.getDiscount();
			double subTotal = report.getSubTotal();
			double cash = report.getCash();
			double credit = report.getCredit();
			//double voucheer = report.get
			double totalPay = cash + credit;
			
			holder.tvReceipt.setText(report.getReceiptNo());
			holder.tvReceipt.setSelected(true);
			holder.tvTotalPrice.setText(MPOSApplication.getGlobalProperty().currencyFormat(totalPrice));
			holder.tvDiscount.setText(MPOSApplication.getGlobalProperty().currencyFormat(totalDiscount));
			holder.tvSubTotal.setText(MPOSApplication.getGlobalProperty().currencyFormat(subTotal));
			holder.tvVatable.setText(MPOSApplication.getGlobalProperty().currencyFormat(vatable));
			holder.tvTotalVat.setText(MPOSApplication.getGlobalProperty().currencyFormat(totalVat));
			holder.tvTotalPayment.setText(MPOSApplication.getGlobalProperty().currencyFormat(totalPay));
			
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
			
			holder.tvCash.setText(MPOSApplication.getGlobalProperty().currencyFormat(cash));
			holder.tvCredit.setText(MPOSApplication.getGlobalProperty().currencyFormat(credit));
			
			if(report.getSendStatus() == MPOSDatabase.ALREADY_SEND){
				holder.imgSendStatus.setImageResource(R.drawable.ic_action_accept);
			}else{
				holder.imgSendStatus.setImageResource(R.drawable.ic_action_warning);
			}
			if(report.getTransStatus() == Transaction.TRANS_STATUS_VOID){
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
			TextView tvTotalSale;
			TextView tvVatable;
			TextView tvTotalVat;
			TextView tvCash;
			TextView tvCredit;
			TextView tvVoucher;
			TextView tvTotalPayment;
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
					
					mBtnDateFrom.setText(MPOSApplication.getGlobalProperty().dateFormat(mCalendar.getTime()));
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
					
					mBtnDateTo.setText(MPOSApplication.getGlobalProperty().dateFormat(mCalendar.getTime()));
				}
			});
			dialogFragment.show(getFragmentManager(), "Condition");
			break;
		}
	}
}
