package com.syn.mpos;

import java.util.ArrayList;
import java.util.List;

import com.j1tth4.mobile.util.ImageLoader;
import com.syn.mpos.database.Products;
import com.syn.mpos.database.Setting;
import com.syn.mpos.inventory.MPOSReceiveStock;
import com.syn.mpos.inventory.MPOSStockDocument;
import com.syn.mpos.inventory.StockProduct;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class DirectReceiveActivity extends Activity implements
		OnActionExpandListener, OnEditorActionListener {

	private MPOSReceiveStock mReceiveStock;
	private Formatter mFormat;
	private Products mProduct;
	private Setting mSetting;
	private Setting.Connection mConn;
	private List<Products.Product> mProductLst;
	private ResultAdapter mResultAdapter;
	private ReceiveStockAdapter mStockAdapter;
	private List<StockProduct> mStockLst;
	private StockProduct mStock;
	private int mPosition = -1;
	private int mDocumentId;
	private int mShopId;
	private int mStaffId;

	private MenuItem mItemSearch;
	private MenuItem mItemInput;
	private SearchView mSearchView;
	private PopupWindow mPopup;
	private View mPopSearch;
	private ListView mListViewStock;
	private ListView mListViewResult;
	private TextView mTvItemName;
	private EditText mTxtReceiveQty;
	private EditText mTxtReceivePrice;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_direct_receive);

		Intent intent = getIntent();
		mShopId = intent.getIntExtra("shopId", 0);
		mStaffId = intent.getIntExtra("staffId", 0);
		if (mShopId == 0 || mStaffId == 0)
			finish();

		mListViewStock = (ListView) findViewById(R.id.listView1);
		setupPopSearch();
	}

	@Override
	protected void onResume() {
		init();
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_stock_receive, menu);
		
		mItemInput = menu.findItem(R.id.itemReceiveInput);
		mTvItemName = (TextView) mItemInput.getActionView().findViewById(R.id.textView1);
		mTxtReceiveQty = (EditText) mItemInput.getActionView().findViewById(R.id.editText1);
		mTxtReceivePrice = (EditText) mItemInput.getActionView().findViewById(R.id.editText2);
		mTxtReceiveQty.setSelectAllOnFocus(true);
		mTxtReceivePrice.setSelectAllOnFocus(true);
		mTxtReceiveQty.setOnEditorActionListener(this);
		mTxtReceivePrice.setOnEditorActionListener(this);
		
		mItemSearch = menu.findItem(R.id.itemSearch);
		mSearchView = (SearchView) mItemSearch.getActionView();
		mSearchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextChange(String query) {
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String query) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
                
				mProductLst = mProduct.listProduct(query);
				if(mProductLst.size() > 0){
					mResultAdapter.notifyDataSetChanged();
				}else{
					new AlertDialog.Builder(DirectReceiveActivity.this)
					.setTitle(R.string.search)
					.setMessage(R.string.not_found_item)
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					})
					.show();
				}
				return true;
			}

		});

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemConfirm:
			confirm();
			return true;
		case R.id.itemCancel:
			cancel();
			return true;
		case R.id.itemClose:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	void clearActionInput(){
		mTvItemName.setText(null);
		mTxtReceiveQty.setText(null);
		mTxtReceivePrice.setText(null);
		
		mItemInput.setVisible(false);
	}
	
	private void setupPopSearch() {
		mProduct = new Products(this);
		mProductLst = new ArrayList<Products.Product>();
		mResultAdapter = new ResultAdapter();

		LayoutInflater inflater = LayoutInflater.from(DirectReceiveActivity.this);
		mPopSearch = inflater.inflate(R.layout.popup_list, null);
		mListViewResult = (ListView) mPopSearch.findViewById(R.id.listView1);
		mListViewResult.setAdapter(mResultAdapter);
		mListViewResult.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Products.Product p = (Products.Product) parent
						.getItemAtPosition(position);

				addSelectedProduct(p.getProductId(), 1, p.getProductPrice(), 0);
				clearActionInput();
				
				mPopup.dismiss();
				if(mItemSearch.isActionViewExpanded())
					mItemSearch.collapseActionView();
			}

		});

		mPopup = new PopupWindow(DirectReceiveActivity.this);
		mPopup.setContentView(mPopSearch);
		mPopup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		mPopup.setFocusable(true);
	}

	private void showPopup() {
		if (!mPopup.isShowing())
			mPopup.showAsDropDown(mSearchView);
	}

	@Override
	protected void onDestroy() {
		mReceiveStock.clearDocument();
		super.onDestroy();
	}

	private void init() {
		mSetting = new Setting(this);
		mConn = mSetting.getConnection();
		
		mSetting.setMenuImageUrl(mConn.getProtocal() + mConn.getAddress() + "/" + 
				mConn.getBackoffice() + "/Resources/Shop/MenuImage/");

		mFormat = new Formatter(DirectReceiveActivity.this);
		mReceiveStock = new MPOSReceiveStock(DirectReceiveActivity.this);
		mStockLst = new ArrayList<StockProduct>();
		mStockAdapter = new ReceiveStockAdapter();
		mListViewStock.setAdapter(mStockAdapter);
		mListViewStock.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				mPosition = position;
				mStock = (StockProduct) parent.getItemAtPosition(position);

				if(mItemSearch.isActionViewExpanded())
					mItemSearch.collapseActionView();
				
				mItemInput.setVisible(true);
				
				mTvItemName.setText(mStock.getName());
				mTxtReceiveQty.setText(mFormat.qtyFormat(mStock.getReceive()));
				mTxtReceivePrice.setText(mFormat.currencyFormat(mStock.getUnitPrice()));
				mTxtReceiveQty.selectAll();
				mTxtReceiveQty.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mTxtReceiveQty,
                        InputMethodManager.SHOW_IMPLICIT);
			}
			
		});
		
		mDocumentId = mReceiveStock.getCurrentDocument(mShopId,
				MPOSStockDocument.DIRECT_RECEIVE_DOC);
		if (mDocumentId == 0) {
			mDocumentId = mReceiveStock.createDocument(mShopId,
					MPOSStockDocument.DIRECT_RECEIVE_DOC, mStaffId);
			
			mReceiveStock.saveDocument(mDocumentId, mShopId, mStaffId,
					"Save Receive");
		}
		mStockLst = mReceiveStock.listStock(mDocumentId, mShopId);
		mStockAdapter.notifyDataSetChanged();
	}

	private void updateStock(){
		mReceiveStock.updateDocumentDetail(mStock.getId(),
				mDocumentId, mShopId, mStock.getProId(),
				mStock.getReceive(), mStock.getUnitPrice(),
				mStock.getTaxType(), "");	
	}
	
	private void addSelectedProduct(int materialId, float materialQty,
			float materialPrice, int taxType) {
		mReceiveStock.addDocumentDetail(mDocumentId, mShopId, materialId,
				materialQty, 0, materialPrice, taxType, "");

		mStockLst = mReceiveStock.listStock(mDocumentId, mShopId);
		mStockAdapter.notifyDataSetChanged();
	}

	// search result adapter
	private class ResultAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private ImageLoader imgLoader;

		public ResultAdapter() {
			inflater = LayoutInflater.from(DirectReceiveActivity.this);

			imgLoader = new ImageLoader(DirectReceiveActivity.this, R.drawable.no_food,
					"mpos_img");
		}

		@Override
		public int getCount() {
			return mProductLst != null ? mProductLst.size() : 0;
		}

		@Override
		public Products.Product getItem(int position) {
			return mProductLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public void notifyDataSetChanged() {
			showPopup();
			super.notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Products.Product p = mProductLst.get(position);
			ViewHolder holder;

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.search_product_template, null);
				holder = new ViewHolder();
				holder.img = (ImageView) convertView.findViewById(R.id.imageView1);
				holder.tvCode = (TextView) convertView.findViewById(R.id.tvCode);
				holder.tvName = (TextView) convertView.findViewById(R.id.tvName);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			imgLoader.displayImage(
					mSetting.getMenuImageUrl() + p.getPicName(),
					holder.img);
			holder.tvCode.setText(p.getProductCode());
			holder.tvName.setText(p.getProductName());

			return convertView;
		}

		private class ViewHolder {
			ImageView img;
			TextView tvCode;
			TextView tvName;
		}
	}

	// stock adapter
	private class ReceiveStockAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		public ReceiveStockAdapter() {
			inflater = LayoutInflater.from(DirectReceiveActivity.this);
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			final StockProduct stock = mStockLst.get(position);
			View rowView = convertView;
			
			rowView = inflater.inflate(R.layout.receive_stock_template,null);
			
			TextView tvNo = (TextView) rowView.findViewById(R.id.tvNo);
			TextView tvCode = (TextView) rowView.findViewById(R.id.tvCode);
			TextView tvName = (TextView) rowView.findViewById(R.id.tvName);
			TextView tvQty = (TextView) rowView.findViewById(R.id.tvQty);
			TextView tvPrice = (TextView) rowView.findViewById(R.id.tvPrice);
			Button btnDelete = (Button) rowView.findViewById(R.id.button1);
			
			tvNo.setText(Integer.toString(position + 1));
			tvCode.setText(stock.getCode());
			tvName.setText(stock.getName());
			tvQty.setText(mFormat.qtyFormat(stock.getReceive()));
			tvPrice.setText(mFormat.currencyFormat(stock.getUnitPrice()));

			btnDelete.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					new AlertDialog.Builder(DirectReceiveActivity.this)
					.setTitle(R.string.delete)
					.setMessage(R.string.confirm_delete_item)
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					})
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mReceiveStock.deleteDocumentDetail(stock.getId(), mDocumentId, mShopId);
							mStockLst.remove(position);
							mStockAdapter.notifyDataSetChanged();
							clearActionInput();
						}
					}).show();
				}
				
			});
			return rowView;
		}
	}

	@Override
	public boolean onMenuItemActionCollapse(MenuItem item) {
		if (mPopup.isShowing())
			mPopup.dismiss();
		return true;
	}

	@Override
	public boolean onMenuItemActionExpand(MenuItem item) {
		if (mPopup.isShowing())
			mPopup.dismiss();
		return true;
	}

	public void confirm() {
		if(mStockLst.size() > 0){

			new AlertDialog.Builder(DirectReceiveActivity.this)
					.setTitle(R.string.confirm)
					.setMessage(R.string.confirm_stock_receive)
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
	
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
	
								}
							})
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
	
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									if (mReceiveStock.approveDocument(mDocumentId,
											mShopId, mStaffId, "")) {
										
										new AlertDialog.Builder(DirectReceiveActivity.this)
										.setTitle(R.string.confirm)
										.setMessage(R.string.confirm_success)
										.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
												finish();
											}
										}).show();
									}
								}
							}).show();
		}
	}

	public void cancel() {
		final EditText txtRemark = new EditText(DirectReceiveActivity.this);
		txtRemark.setHint(R.string.remark);

		new AlertDialog.Builder(DirectReceiveActivity.this)
				.setTitle(android.R.string.cancel)
				.setMessage(R.string.confirm_cancel)
				.setView(txtRemark)
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						})
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String remark = txtRemark.getText().toString();

								if (mReceiveStock.cancelDocument(mDocumentId,
										mShopId, mStaffId, remark)) {
									
									new AlertDialog.Builder(DirectReceiveActivity.this)
									.setIcon(android.R.drawable.ic_dialog_alert)
									.setTitle(android.R.string.cancel)
									.setMessage(R.string.cancel_success)
									.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											finish();
										}
									}).show();
								}
							}
						}).show();
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if(EditorInfo.IME_ACTION_DONE == actionId){
			float qty = mStock.getReceive();
			float price = mStock.getUnitPrice();
			
			if(v.getId() == R.id.editText1){
				try {
					qty = Float.parseFloat(v.getText().toString());
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if(v.getId() == R.id.editText2){
				try {
					price = Float.parseFloat(v.getText().toString());
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			mStock.setReceive(qty);
			mStock.setUnitPrice(price);
			
			updateStock();
			mStockLst.set(mPosition, mStock);
			mStockAdapter.notifyDataSetChanged();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			
			clearActionInput();
            
			return true;
		}
		return false;
	}
}
