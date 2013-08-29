package com.syn.mpos;

import java.util.List;

import com.syn.mpos.R;
import com.syn.mpos.db.MPOSTransaction;
import com.syn.mpos.model.OrderTransaction;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class DiscountActivity extends Activity {
	private static final String TAG = "DiscountActivity";
	private Context context;
	private Formatter format;
	private MPOSTransaction mposTrans;
	private boolean isEdited = false;

	private List<OrderTransaction.OrderDetail> orderLst;
	private TableLayout tbLayoutDiscount;
	private TextView tvSubTotal;
	private TextView tvTotalDiscount;
	private TextView tvTotalVat;
	private TextView tvTotalPrice;
	private TableRow tbRowVat;

	private int transactionId;
	private int computerId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_discount);

		context = DiscountActivity.this;

		tbLayoutDiscount = (TableLayout) findViewById(R.id.tableLayoutDiscount);
		tvSubTotal = (TextView) findViewById(R.id.textViewDisSubTotal);
		tvTotalDiscount = (TextView) findViewById(R.id.textViewDisDiscount);
		tvTotalPrice = (TextView) findViewById(R.id.textViewDisTotal);
		tbRowVat = (TableRow) findViewById(R.id.tbRowVat);

		Intent intent = getIntent();
		transactionId = intent.getIntExtra("transactionId", 0);
		computerId = intent.getIntExtra("computerId", 0);

		if (transactionId != 0 && computerId != 0) {
			format = new Formatter(DiscountActivity.this);
			mposTrans = new MPOSTransaction(DiscountActivity.this);

			loadOrder();
			summaryPrice();
		} else {
			exit();
		}
	}

	private float calculateDiscount(int orderDetailId, int vatType, float totalPrice, float discount) {
		isEdited = true;

		float totalPriceAfterDiscount = totalPrice - discount;
		mposTrans.discountEatchProduct(orderDetailId, transactionId,
				computerId, vatType, totalPrice, discount);

		summaryPrice();
		return totalPriceAfterDiscount;
	}

	private void loadOrder() {
		if (mposTrans.copyOrderToTmp(transactionId, computerId)) {
			orderLst = mposTrans.listAllOrdersTmp(transactionId, computerId);

			LayoutInflater inflater = LayoutInflater.from(context);
			for (int i = 0; i < orderLst.size(); i++) {
				View v = inflater.inflate(R.layout.discount_template, null);
				TextView tvDiscountNo = (TextView) v
						.findViewById(R.id.textViewDisNo);
				TextView tvDiscountProName = (TextView) v
						.findViewById(R.id.textViewDisProName);
				TextView tvDiscountProAmount = (TextView) v
						.findViewById(R.id.textViewDisProAmount);
				TextView tvDiscountProPrice = (TextView) v
						.findViewById(R.id.textViewDisProPrice);
				final EditText txtDisPrice = (EditText) v
						.findViewById(R.id.editTextDisPrice);
				final TextView tvDisSalePrice = (TextView) v
						.findViewById(R.id.textViewDisSalePrice);
				final TextView tvProductPrice = (TextView) v
						.findViewById(R.id.tvProductPrice);

				final OrderTransaction.OrderDetail order = orderLst.get(i);

				tvDiscountNo.setText(Integer.toString(i + 1));
				tvDiscountProName.setText(order.getProductName());
				tvDiscountProAmount.setText(format.qtyFormat(order
						.getProductAmount()));
				tvProductPrice.setText(format.currencyFormat(order
						.getProductPrice()));
				tvDiscountProPrice.setText(format.currencyFormat(order
						.getTotalPrice()));
				tvDisSalePrice.setText(format.currencyFormat(order
						.getSalePrice()));
				txtDisPrice.setText(format.currencyFormat(order
						.getEachProductDiscount()));
				txtDisPrice.setSelectAllOnFocus(true);

				// select at first row
				if (i == 0)
					txtDisPrice.setSelection(i);

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
											&& order.getTotalPrice() >= discount) {
										float salePrice = calculateDiscount(
												order.getOrderDetailId(),
												order.getVatType(),
												order.getTotalPrice(),
												discount);

										tvDisSalePrice.setText(format
												.currencyFormat(salePrice));
									} else {
										txtDisPrice.setText(format.currencyFormat(order
												.getEachProductDiscount()));
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
									&& order.getTotalPrice() >= discount) {
								float salePrice = calculateDiscount(
										order.getOrderDetailId(),
										order.getVatType(),
										order.getTotalPrice(), 
										discount);

								tvDisSalePrice.setText(format
										.currencyFormat(salePrice));
							} else {
								txtDisPrice.setText(format.currencyFormat(order
										.getEachProductDiscount()));
							}
							return true;
						}
						return false;
					}

				});

				tbLayoutDiscount.addView(v);
			}
		}

	}

	private void summaryPrice() {
		OrderTransaction.OrderDetail orderDetail = mposTrans
				.getSummaryTmp(transactionId);

		float subTotal = orderDetail.getProductPrice();
		float totalDiscount = orderDetail.getEachProductDiscount();
		float totalPrice = subTotal - totalDiscount;
		
		if(orderDetail.getVatExclude() > 0)
			tbRowVat.setVisibility(View.VISIBLE);
		else
			tbRowVat.setVisibility(View.GONE);
		
		tvSubTotal.setText(format.currencyFormat(subTotal));
		tvTotalDiscount.setText(format.currencyFormat(totalDiscount));
		tvTotalPrice.setText(format.currencyFormat(totalPrice));
	}

	private void exit() {
		DiscountActivity.this.finish();
	}

	public void okClicked(final View v) {
		if (mposTrans.confirmDiscount(transactionId, computerId))
			exit();
	}

	public void cancelClicked(final View v) {
		if (isEdited) {
			new AlertDialog.Builder(context)
					.setTitle(R.string.information)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setMessage(R.string.confirm_cancel)
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mposTrans.cancelDiscount(transactionId,
											computerId);
									exit();
								}
							})
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mposTrans.confirmDiscount(transactionId,
											computerId);
									exit();
								}
							}).show();
		} else {
			exit();
		}
	}
}
