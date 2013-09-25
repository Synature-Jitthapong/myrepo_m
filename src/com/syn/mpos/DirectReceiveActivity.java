package com.syn.mpos;

import java.util.ArrayList;
import java.util.List;

import com.j1tth4.mobile.util.ImageLoader;
import com.syn.mpos.inventory.MPOSReceiveStock;
import com.syn.mpos.inventory.MPOSStockDocument;
import com.syn.mpos.inventory.StockMaterial;
import com.syn.pos.MenuGroups;
import com.syn.pos.Setting;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

public class DirectReceiveActivity extends Activity implements
		OnActionExpandListener, OnConfirmClickListener {

	private MPOSReceiveStock mReceiveStock;
	private Formatter mFormat;
	private com.syn.mpos.database.MenuItem menuItem;
	private SharedPreferences mSharedPref;
	private Setting mSetting;
	private List<MenuGroups.MenuItem> menuLst;
	private ResultAdapter mResultAdapter;
	private ReceiveStockAdapter mStockAdapter;
	private List<StockMaterial> mStockLst;
	private int mDocumentId;
	private int mShopId;
	private int mStaffId;
	private Context mContext;

	private MenuItem mItemSearch;
	private SearchView mSearchView;
	private PopupWindow mPopup;
	private View mPopSearch;
	private ListView mListViewStock;
	private ListView mListViewResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_direct_receive);
		mContext = DirectReceiveActivity.this;

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
		inflater.inflate(R.menu.action_stock_receive, menu);

		mItemSearch = menu.findItem(R.id.itemSearch);
		mSearchView = (SearchView) mItemSearch.getActionView();
		mSearchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String query) {
				menuLst = menuItem.listMenuItem(query);
				mResultAdapter.notifyDataSetChanged();
				return true;
			}

		});

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemConfirm:
			onConfirmClick(item.getActionView());
			return true;
		case R.id.itemCancel:
			onCancelClick(item.getActionView());
			return true;
		case R.id.itemClose:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void setupPopSearch() {
		menuItem = new com.syn.mpos.database.MenuItem(mContext);
		menuLst = new ArrayList<MenuGroups.MenuItem>();
		mResultAdapter = new ResultAdapter();

		LayoutInflater inflater = LayoutInflater.from(mContext);
		mPopSearch = inflater.inflate(R.layout.popup_list, null);
		mListViewResult = (ListView) mPopSearch.findViewById(R.id.listView1);
		mListViewResult.setAdapter(mResultAdapter);
		mListViewResult.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				MenuGroups.MenuItem menuItem = (MenuGroups.MenuItem) parent
						.getItemAtPosition(position);

				addSelectedProduct(menuItem.getProductID(), 1,
						menuItem.getProductPricePerUnit(), 0);
				
				hideKeyboard();
				mPopup.dismiss();
			}

		});

		mPopup = new PopupWindow(mContext);
		mPopup.setContentView(mPopSearch);
		mPopup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		mPopup.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				if (mItemSearch.isActionViewExpanded())
					mItemSearch.collapseActionView();
			}

		});
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
		mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		mSetting = new Setting();
		mSetting.setMenuImageUrl("http://"
				+ mSharedPref.getString("pref_ipaddress", "") + "/"
				+ mSharedPref.getString("pref_webservice", "")
				+ "/Resources/Shop/MenuImage/");

		mFormat = new Formatter(mContext);
		mReceiveStock = new MPOSReceiveStock(mContext);
		mStockLst = new ArrayList<StockMaterial>();
		mStockAdapter = new ReceiveStockAdapter();
		mListViewStock.setAdapter(mStockAdapter);

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

	private void updateStock(StockMaterial stock){
		mReceiveStock.updateDocumentDetail(stock.getId(),
				mDocumentId, mShopId, stock.getMatId(),
				stock.getCurrQty(), stock.getPricePerUnit(),
				stock.getTaxType(), "");	
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
			inflater = LayoutInflater.from(mContext);

			imgLoader = new ImageLoader(mContext, R.drawable.no_food,
					"mpos_img");
		}

		@Override
		public int getCount() {
			return menuLst != null ? menuLst.size() : 0;
		}

		@Override
		public MenuGroups.MenuItem getItem(int position) {
			return menuLst.get(position);
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
			MenuGroups.MenuItem menu = menuLst.get(position);
			ViewHolder holder;

			if (convertView == null) {
				convertView = inflater.inflate(
						R.layout.search_product_template, null);
				holder = new ViewHolder();
				holder.img = (ImageView) convertView
						.findViewById(R.id.imageView1);
				holder.tvCode = (TextView) convertView
						.findViewById(R.id.tvCode);
				holder.tvName = (TextView) convertView
						.findViewById(R.id.tvName);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			imgLoader.displayImage(
					mSetting.getMenuImageUrl() + menu.getMenuImageLink(),
					holder.img);
			holder.tvCode.setText(menu.getProductCode());
			holder.tvName.setText(menu.getMenuName_0());

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
			inflater = LayoutInflater.from(mContext);
		}

		@Override
		public int getCount() {
			return mStockLst != null ? mStockLst.size() : 0;
		}

		@Override
		public StockMaterial getItem(int position) {
			return mStockLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final StockMaterial stock = mStockLst.get(position);
			final ViewHolder holder;

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.receive_stock_template,
						null);
				holder = new ViewHolder();
				holder.tvNo = (TextView) convertView.findViewById(R.id.tvNo);
				holder.tvCode = (TextView) convertView
						.findViewById(R.id.tvCode);
				holder.tvName = (TextView) convertView
						.findViewById(R.id.tvName);
				holder.txtQty = (EditText) convertView
						.findViewById(R.id.txtQty);
				holder.txtPrice = (EditText) convertView
						.findViewById(R.id.txtPrice);
				holder.rdoTaxType = (RadioGroup) convertView
						.findViewById(R.id.rdoTaxType);
				holder.txtPrice.setSelectAllOnFocus(true);
				holder.txtQty.setSelectAllOnFocus(true);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.tvNo.setText(Integer.toString(position + 1));
			holder.tvCode.setText(stock.getCode());
			holder.tvName.setText(stock.getName());
			holder.txtQty.setText(mFormat.qtyFormat(stock.getCurrQty()));
			holder.txtPrice.setText(mFormat.currencyFormat(stock
					.getPricePerUnit()));

			holder.txtQty.clearFocus();
			holder.txtPrice.clearFocus();
			
			switch(stock.getTaxType()){
			case 0:
				holder.rdoTaxType.check(R.id.rdoNoVat);
				break;
			case 1:
				holder.rdoTaxType.check(R.id.rdoIncludeVat);
				break;
			case 2:
				holder.rdoTaxType.check(R.id.rdoExcludeVat);
				break;
			}
			
			holder.rdoTaxType.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					RadioButton rdoTax;
					switch(checkedId){
					case R.id.rdoNoVat:
						rdoTax = (RadioButton) group.findViewById(checkedId);
						if(rdoTax.isChecked())
							stock.setTaxType(0);
						break;
					case R.id.rdoIncludeVat:
						rdoTax = (RadioButton) group.findViewById(checkedId);
						if(rdoTax.isChecked())
							stock.setTaxType(1);
						break;
					case R.id.rdoExcludeVat:
						rdoTax = (RadioButton) group.findViewById(checkedId);
						if(rdoTax.isChecked())
							stock.setTaxType(2);
						break;
					}
					
					updateStock(stock);
				}
			});
			
			holder.txtQty.setOnKeyListener(new OnKeyListener(){

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if(event.getAction() == KeyEvent.ACTION_UP){
						EditText txtQty = (EditText) v;
						try {
							stock.setCurrQty(Float.parseFloat(txtQty.getText().toString()));
							updateStock(stock);
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					return false;
				}
				
			});
			
			holder.txtPrice.setOnKeyListener(new OnKeyListener(){

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if(event.getAction() == KeyEvent.ACTION_UP){
						EditText txtPrice = (EditText) v;
						try {
							stock.setPricePerUnit(Float.parseFloat(txtPrice.getText().toString()));
							updateStock(stock);
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					return false;
				}
				
			});
			return convertView;
		}

		private class ViewHolder {
			TextView tvNo;
			TextView tvCode;
			TextView tvName;
			EditText txtQty;
			EditText txtPrice;
			RadioGroup rdoTaxType;
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

	@Override
	public void onSaveClick(final View v) {
		
	}

	@Override
	public void onConfirmClick(View v) {
		final EditText txtRemark = new EditText(mContext);
		txtRemark.setHint(R.string.remark);

		new AlertDialog.Builder(mContext)
				.setTitle(R.string.confirm)
				.setMessage(R.string.confirm_stock_receive)
				.setView(txtRemark)
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
								String remark = txtRemark.getText().toString();

								if (mReceiveStock.approveDocument(mDocumentId,
										mShopId, mStaffId, remark)) {
									Util.alert(mContext,
											android.R.drawable.ic_dialog_alert,
											R.string.confirm,
											R.string.confirm_success);
									
									init();
								}
							}
						}).show();
	}

	@Override
	public void onCancelClick(View v) {
		final EditText txtRemark = new EditText(mContext);
		txtRemark.setHint(R.string.remark);

		new AlertDialog.Builder(mContext)
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
									Util.alert(mContext,
											android.R.drawable.ic_dialog_alert,
											R.string.confirm,
											R.string.confirm_success);
									
									finish();
								}
							}
						}).show();
	}

	private void hideKeyboard(){
		getWindow().setSoftInputMode(
			      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
}
