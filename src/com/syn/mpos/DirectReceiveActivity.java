package com.syn.mpos;

import java.util.ArrayList;
import java.util.List;

import com.j1tth4.mobile.util.ImageLoader;
import com.syn.mpos.database.Products;
import com.syn.mpos.database.Setting;
import com.syn.mpos.database.inventory.ReceiveStock;
import com.syn.mpos.database.inventory.StockDocument;
import com.syn.mpos.database.inventory.StockProduct;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
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
	private ReceiveStock mReceiveStock;
	private List<Products.Product> mProductLst;
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

        getActionBar().setDisplayHomeAsUpEnabled(true);
        
		mListViewStock = (ListView) findViewById(R.id.lvStock);
		
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
        	int productId = Integer.parseInt(intent.getDataString());
        	Products.Product p = GlobalVar.sProduct.getProduct(productId);
        	// add selected product
        	addSelectedProduct(productId, 1, p.getProductPrice(), p.getProductUnitName());
        	
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
    
            String query = intent.getStringExtra(SearchManager.QUERY);
            
        }
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		init();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_stock_receive, menu);
		
		mItemInput = menu.findItem(R.id.itemReceiveInput);
		mTvItemName = (TextView) mItemInput.getActionView().findViewById(R.id.textView1);
		mTxtReceiveQty = (EditText) mItemInput.getActionView().findViewById(R.id.txtQty);
		mTxtReceivePrice = (EditText) mItemInput.getActionView().findViewById(R.id.txtPrice);
		mTxtReceiveQty.setSelectAllOnFocus(true);
		mTxtReceivePrice.setSelectAllOnFocus(true);
		mTxtReceiveQty.setOnEditorActionListener(this);
		mTxtReceivePrice.setOnEditorActionListener(this);

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		mItemSearch = menu.findItem(R.id.itemSearch);
		mSearchView = (SearchView) mItemSearch.getActionView();
		mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		mSearchView.setIconifiedByDefault(false);
		mItemSearch.expandActionView();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
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

	@Override
	protected void onDestroy() {
		mReceiveStock.clearDocument();
		super.onDestroy();
	}

	private void init() {
		mReceiveStock = new ReceiveStock(DirectReceiveActivity.this);
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
				mTxtReceiveQty.setText(MPOSApplication.sGlobalVar.qtyFormat(mStock.getReceive()));
				mTxtReceivePrice.setText(MPOSApplication.sGlobalVar.currencyFormat(mStock.getUnitPrice()));
				mTxtReceiveQty.selectAll();
				mTxtReceiveQty.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mTxtReceiveQty,
                        InputMethodManager.SHOW_IMPLICIT);
			}
			
		});
		
		mDocumentId = mReceiveStock.getCurrentDocument(mShopId,
				StockDocument.DIRECT_RECEIVE_DOC);
		if (mDocumentId == 0) {
			mDocumentId = mReceiveStock.createDocument(mShopId,
					StockDocument.DIRECT_RECEIVE_DOC, mStaffId);
			
			mReceiveStock.saveDocument(mDocumentId, mShopId, mStaffId,
					"Save Receive");
		}
		mStockLst = mReceiveStock.listStock(mDocumentId, mShopId);
		mStockAdapter.notifyDataSetChanged();
	}

	private void updateStock(){
		mReceiveStock.updateDocumentDetail(mStock.getId(),
				mDocumentId, mShopId, mStock.getProId(),
				mStock.getReceive(), mStock.getUnitPrice(), "");	
	}
	
	private void addSelectedProduct(int productId, float productAmount,
			float productPrice, String unitName) {
		mReceiveStock.addDocumentDetail(mDocumentId, mShopId, productId,
				productAmount, productPrice, unitName);

		mStockLst = mReceiveStock.listStock(mDocumentId, mShopId);
		mStockAdapter.notifyDataSetChanged();
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
			Button btnDelete = (Button) rowView.findViewById(R.id.btnDelete);
			
			tvNo.setText(Integer.toString(position + 1));
			tvCode.setText(stock.getCode());
			tvName.setText(stock.getName());
			tvQty.setText(MPOSApplication.sGlobalVar.qtyFormat(stock.getReceive()));
			tvPrice.setText(MPOSApplication.sGlobalVar.currencyFormat(stock.getUnitPrice()));

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
		

		return true;
	}

	@Override
	public boolean onMenuItemActionExpand(MenuItem item) {
		

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
			
			if(v.getId() == R.id.txtQty){
				try {
					qty = Float.parseFloat(v.getText().toString());
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if(v.getId() == R.id.txtPrice){
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
