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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
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

	private void clearFocus(final View v){
		v.clearFocus();
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		clearFocus(getCurrentFocus());
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		clearFocus(getCurrentFocus());
		
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
		menuItem = new com.syn.mpos.database.MenuItem(DirectReceiveActivity.this);
		menuLst = new ArrayList<MenuGroups.MenuItem>();
		mResultAdapter = new ResultAdapter();

		LayoutInflater inflater = LayoutInflater.from(DirectReceiveActivity.this);
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

		mPopup = new PopupWindow(DirectReceiveActivity.this);
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

		mFormat = new Formatter(DirectReceiveActivity.this);
		mReceiveStock = new MPOSReceiveStock(DirectReceiveActivity.this);
		mStockLst = new ArrayList<StockMaterial>();
		mStockAdapter = new ReceiveStockAdapter();
		mListViewStock.setAdapter(mStockAdapter);
		mListViewStock.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				clearFocus(v);
				return false;
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
			inflater = LayoutInflater.from(DirectReceiveActivity.this);

			imgLoader = new ImageLoader(DirectReceiveActivity.this, R.drawable.no_food,
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
			inflater = LayoutInflater.from(DirectReceiveActivity.this);
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			final StockMaterial stock = mStockLst.get(position);
			View rowView = convertView;
			
			rowView = inflater.inflate(R.layout.receive_stock_template,null);
			
			TextView tvNo = (TextView) rowView.findViewById(R.id.tvNo);
			TextView tvCode = (TextView) rowView.findViewById(R.id.tvCode);
			TextView tvName = (TextView) rowView.findViewById(R.id.tvName);
			EditText txtQty = (EditText) rowView.findViewById(R.id.txtQty);
			EditText txtPrice = (EditText) rowView.findViewById(R.id.txtPrice);
			txtQty.setSelectAllOnFocus(true);
			txtPrice.setSelectAllOnFocus(true);
			RadioGroup rdoTaxType = (RadioGroup) rowView.findViewById(R.id.rdoTaxType);
			ImageView imgBtnDelete = (ImageView) rowView.findViewById(R.id.imgDel);
			
			tvNo.setText(Integer.toString(position + 1));
			tvCode.setText(stock.getCode());
			tvName.setText(stock.getName());
			txtQty.setText(mFormat.qtyFormat(stock.getCurrQty()));
			txtPrice.setText(mFormat.currencyFormat(stock.getPricePerUnit()));

			switch(stock.getTaxType()){
			case 0:
				rdoTaxType.check(R.id.rdoNoVat);
				break;
			case 1:
				rdoTaxType.check(R.id.rdoIncludeVat);
				break;
			case 2:
				rdoTaxType.check(R.id.rdoExcludeVat);
				break;
			}
			
			rdoTaxType.setOnCheckedChangeListener(new OnCheckedChangeListener(){
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
			
			txtQty.setOnFocusChangeListener(new OnFocusChangeListener(){

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					EditText txtQty = (EditText) v;
					if(!hasFocus){
						try {
							stock.setCurrQty(Float.parseFloat(txtQty.getText().toString()));
							updateStock(stock);
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
			});
			
			txtPrice.setOnFocusChangeListener(new OnFocusChangeListener(){

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					EditText txtPrice = (EditText) v;
					if(!hasFocus){
						try {
							stock.setPricePerUnit(Float.parseFloat(txtPrice.getText().toString()));
							updateStock(stock);
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
			});
			
			imgBtnDelete.setOnClickListener(new OnClickListener(){

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

	@Override
	public void onSaveClick(final View v) {
		
	}

	@Override
	public void onConfirmClick(View v) {
		if(mStockLst.size() > 0){
			final EditText txtRemark = new EditText(DirectReceiveActivity.this);
			txtRemark.setHint(R.string.remark);
	
			new AlertDialog.Builder(DirectReceiveActivity.this)
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

	@Override
	public void onCancelClick(View v) {
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

	private void hideKeyboard(){
		getWindow().setSoftInputMode(
			      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
}
