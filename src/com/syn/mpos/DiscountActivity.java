package com.syn.mpos;

import java.util.List;

import com.syn.mpos.R;
import com.syn.mpos.db.MPOSOrder;
import com.syn.mpos.model.OrderTransaction;
import com.syn.pos.Order;

import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class DiscountActivity extends Activity implements OnConfirmClickListener{
	private static final String TAG = "DiscountActivity";
	private Context mContext;
	private int mTransactionId;
	private int mComputerId;
	private Formatter mFormat;
	private Order mOrder;
	private boolean mIsEdited = false;

	private List<OrderTransaction.OrderDetail> mOrderLst;
	private TableLayout mTbLayoutDiscount;
	private TextView mTvSubTotal;
	private TextView mTvTotalDiscount;
	private TextView mTvVat;
	private TextView mTvTotalPrice;
	private TableRow mTbRowVat;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_discount);
		mContext = DiscountActivity.this;
		
		ActionBar actionBar = getActionBar();
		actionBar.setCustomView(R.layout.confirm_button);
	    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
	            | ActionBar.DISPLAY_SHOW_HOME);

		mTbLayoutDiscount = (TableLayout) findViewById(R.id.tableLayoutDiscount);
		mTvSubTotal = (TextView) findViewById(R.id.textViewDisSubTotal);
		mTvTotalDiscount = (TextView) findViewById(R.id.textViewDisDiscount);
		mTvTotalPrice = (TextView) findViewById(R.id.textViewDisTotal);
		mTbRowVat = (TableRow) findViewById(R.id.tbRowVat);
		mTvVat = (TextView) findViewById(R.id.tvVat);

		Intent intent = getIntent();
		mTransactionId = intent.getIntExtra("transactionId", 0);
		mComputerId = intent.getIntExtra("computerId", 0);

		if (mTransactionId != 0 && mComputerId != 0) {
			mFormat = new Formatter(mContext);
			mOrder = new MPOSOrder(mContext);
			
			loadOrder();
			summary();
		} else {
			exit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

	private float calculateDiscount(int orderDetailId, int vatType, float totalPrice, float discount) {
		mIsEdited = true;

		float totalPriceAfterDiscount = totalPrice - discount;
		mOrder.discountEatchProduct(orderDetailId, mTransactionId,
				mComputerId, vatType, totalPrice, discount);

		summary();
		return totalPriceAfterDiscount;
	}

	private void loadOrder() {
		if (mOrder.copyOrderToTmp(mTransactionId, mComputerId)) {
			mOrderLst = mOrder.listAllOrdersTmp(mTransactionId, mComputerId);

			LayoutInflater inflater = LayoutInflater.from(mContext);
			for (int i = 0; i < mOrderLst.size(); i++) {
				View v = inflater.inflate(R.layout.discount_template, null);
				TextView tvDiscountNo = (TextView) v.findViewById(R.id.textViewDisNo);
				TextView tvDiscountProName = (TextView) v.findViewById(R.id.textViewDisProName);
				TextView tvDiscountProAmount = (TextView) v.findViewById(R.id.textViewDisProAmount);
				TextView tvDiscountProPrice = (TextView) v.findViewById(R.id.textViewDisProPrice);
				final EditText txtDisPrice = (EditText) v.findViewById(R.id.editTextDisPrice);
				final TextView tvDisSalePrice = (TextView) v.findViewById(R.id.textViewDisSalePrice);
				final TextView tvProductPrice = (TextView) v.findViewById(R.id.tvProductPrice);

				final OrderTransaction.OrderDetail order = mOrderLst.get(i);
				tvDiscountNo.setText(Integer.toString(i + 1));
				tvDiscountProName.setText(order.getProductName());
				tvDiscountProAmount.setText(mFormat.qtyFormat(order.getQty()));
				tvProductPrice.setText(mFormat.currencyFormat(order.getPricePerUnit()));
				tvDiscountProPrice.setText(mFormat.currencyFormat(order.getTotalRetailPrice()));
				tvDisSalePrice.setText(mFormat.currencyFormat(order.getTotalSalePrice()));
				txtDisPrice.setText(mFormat.currencyFormat(order.getPriceDiscount()));
				txtDisPrice.setSelectAllOnFocus(true);

				// on focus change event
				txtDisPrice
						.setOnFocusChangeListener(new OnFocusChangeListener() {

							@Override
							public void onFocusChange(View v, boolean hasFocus) {
								if (!hasFocus) {
									float discount = 0.0f;
									try {
										discount = Float.parseFloat(txtDisPrice
												.getText().toString());
									} catch (NumberFormatException e) {
										e.printStackTrace();
									}

									if (discount >= 0
											&& order.getTotalRetailPrice() >= discount) {
										float salePrice = calculateDiscount(
												order.getOrderDetailId(),
												order.getVatType(),
												order.getTotalRetailPrice(),
												discount);

										tvDisSalePrice.setText(mFormat
												.currencyFormat(salePrice));
									} else {
										txtDisPrice.setText(mFormat.currencyFormat(order.getPriceDiscount()));
									}
								}
							}

						});

				// on enter event
				txtDisPrice.setOnKeyListener(new OnKeyListener() {

					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						if ((event.getAction() == KeyEvent.ACTION_DOWN)
								&& keyCode == KeyEvent.KEYCODE_ENTER) {
							float discount = 0.0f;
							try {
								discount = Float.parseFloat(txtDisPrice
										.getText().toString());
							} catch (NumberFormatException e) {
								e.printStackTrace();
							}

							if (discount >= 0
									&& order.getTotalRetailPrice() >= discount) {
								float salePrice = calculateDiscount(
										order.getOrderDetailId(),
										order.getVatType(),
										order.getTotalRetailPrice(), 
										discount);

								tvDisSalePrice.setText(mFormat
										.currencyFormat(salePrice));
							} else {
								txtDisPrice.setText(mFormat.currencyFormat(order
										.getPriceDiscount()));
							}
							return true;
						}
						return false;
					}

				});

				mTbLayoutDiscount.addView(v);
			}
		}

	}

	private void summary() {
		OrderTransaction.OrderDetail orderDetail = 
				mOrder.getSummaryTmp(mTransactionId, mComputerId);

		float subTotal = orderDetail.getTotalRetailPrice();
		float vat = orderDetail.getVat();
		float totalSalePrice = orderDetail.getTotalSalePrice() + vat;
		float totalDiscount = orderDetail.getPriceDiscount() + orderDetail.getMemberDiscount();
		
		if(orderDetail.getVat() > 0)
			mTbRowVat.setVisibility(View.VISIBLE);
		else
			mTbRowVat.setVisibility(View.GONE);
		
		mTvSubTotal.setText(mFormat.currencyFormat(subTotal));
		mTvTotalDiscount.setText(mFormat.currencyFormat(totalDiscount));
		mTvVat.setText(mFormat.currencyFormat(orderDetail.getVat()));
		mTvTotalPrice.setText(mFormat.currencyFormat(totalSalePrice));
	}

	private void exit() {
		DiscountActivity.this.finish();
	}

	@Override
	public void onOkClick(View v) {
		if (mOrder.confirmDiscount(mTransactionId, mComputerId))
			exit();
	}

	@Override
	public void onCancelClick(View v) {
		if (mIsEdited) {
			new AlertDialog.Builder(mContext)
					.setTitle(R.string.information)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setMessage(R.string.confirm_cancel)
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mOrder.cancelDiscount(mTransactionId,
											mComputerId);
									exit();
								}
							})
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mOrder.confirmDiscount(mTransactionId,
											mComputerId);
									exit();
								}
							}).show();
		} else {
			exit();
		}
	}
}
