package com.syn.mpos;

import java.util.ArrayList;
import java.util.List;
import com.syn.mpos.inventory.MPOSReceiveStock;
import com.syn.mpos.inventory.MPOSStockDocument;
import com.syn.mpos.inventory.StockMaterial;
import com.syn.pos.MenuGroups;
import com.syn.pos.inventory.Document;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
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
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Spinner;
import android.widget.TextView;

public class DirectReceiveActivity extends Activity implements OnActionExpandListener {
	
	private MPOSReceiveStock mReceiveStock;
	private Formatter mFormat;
	private com.syn.mpos.database.MenuItem menuItem;
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
		if(mShopId == 0 || mStaffId == 0)
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
		inflater.inflate(R.menu.direct_receive, menu);

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

	private void setupPopSearch(){
		menuItem = new com.syn.mpos.database.MenuItem(mContext);
		menuLst = new ArrayList<MenuGroups.MenuItem>();
		mResultAdapter = new ResultAdapter();
		
		LayoutInflater inflater = LayoutInflater.from(mContext);
		mPopSearch = inflater.inflate(R.layout.popup_list, null);
		mListViewResult = (ListView) mPopSearch.findViewById(R.id.listView1);
		mListViewResult.setAdapter(mResultAdapter);
		mListViewResult.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				MenuGroups.MenuItem menuItem = (MenuGroups.MenuItem) parent
						.getItemAtPosition(position);

				addSelectedProduct(menuItem.getProductID(), 1,
						menuItem.getProductPricePerUnit(), 0);
				mPopup.dismiss();
			}
			
		});
		
		mPopup = new PopupWindow(mContext);
		mPopup.setContentView(mPopSearch);
		mPopup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT); 
		mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		mPopup.setOnDismissListener(new OnDismissListener(){

			@Override
			public void onDismiss() {
				if(mItemSearch.isActionViewExpanded())
					mItemSearch.collapseActionView();
			}
			
		});
		mPopup.setFocusable(true);
	}
	
	private void showPopup() {
		if(!mPopup.isShowing())
			mPopup.showAsDropDown(mSearchView);
	}
	
	@Override
	protected void onDestroy() {
		mReceiveStock.clearDocument();
		super.onDestroy();
	}

	private void init(){
		mFormat = new Formatter(mContext);
		mReceiveStock = new MPOSReceiveStock(mContext);
		mStockLst = new ArrayList<StockMaterial>();
		mStockAdapter = new ReceiveStockAdapter();
		mListViewStock.setAdapter(mStockAdapter);
		
		mDocumentId = mReceiveStock.getCurrentDocument(mShopId, 
				MPOSStockDocument.DIRECT_RECEIVE_DOC);
		if(mDocumentId == 0){
			mDocumentId = mReceiveStock.createDocument(mShopId, MPOSStockDocument.DIRECT_RECEIVE_DOC, mStaffId);	
		}
		mStockLst = mReceiveStock.listStock(mDocumentId, mShopId);
		mStockAdapter.notifyDataSetChanged();
	}
	
	private void addSelectedProduct(int materialId, float materialQty, float materialPrice, 
			int taxType){
		mReceiveStock.addDocumentDetail(mDocumentId, mShopId, materialId, 
				materialQty, 0, materialPrice, taxType, "");

		mStockLst = mReceiveStock.listStock(mDocumentId, mShopId);
		mStockAdapter.notifyDataSetChanged();
	}
	
	// search result adapter
	private class ResultAdapter extends BaseAdapter{

		private LayoutInflater inflater;
		
		public ResultAdapter(){
			inflater = LayoutInflater.from(mContext);
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
			
			if(convertView == null){
				convertView = inflater.inflate(R.layout.search_product_template, null);
				holder = new ViewHolder();
				holder.img = (ImageView) convertView.findViewById(R.id.imageView1);
				holder.tvCode = (TextView) convertView.findViewById(R.id.tvCode);
				holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
				
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tvCode.setText(menu.getProductCode());
			holder.tvName.setText(menu.getMenuName_0());
			
			return convertView;
		}
		
		private class ViewHolder{
			ImageView img;
			TextView tvCode;
			TextView tvName;
		}
	}

	// stock adapter
	private class ReceiveStockAdapter extends BaseAdapter{

		private LayoutInflater inflater;
		
		public ReceiveStockAdapter(){
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
			ViewHolder holder;
			
			if(convertView == null){
				convertView = inflater.inflate(R.layout.receive_stock_template, null);
				holder = new ViewHolder();
				holder.tvNo = (TextView) convertView.findViewById(R.id.tvNo);
				holder.tvCode = (TextView) convertView.findViewById(R.id.tvCode);
				holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
				holder.txtQty = (EditText) convertView.findViewById(R.id.txtQty);
				holder.txtPrice = (EditText) convertView.findViewById(R.id.txtPrice);
				holder.rdoTaxType = (RadioGroup) convertView.findViewById(R.id.rdoTaxType);
				
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tvNo.setText(Integer.toString(position + 1));
			holder.tvCode.setText(stock.getCode());
			holder.tvName.setText(stock.getName());
			holder.txtQty.setText(mFormat.qtyFormat(stock.getCurrQty()));
			holder.txtPrice.setText(mFormat.currencyFormat(stock.getPricePerUnit()));
			holder.txtQty.setOnFocusChangeListener(new OnFocusChangeListener(){

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(hasFocus)
						((EditText) v).selectAll();
				}
				
			});
			
			holder.txtPrice.setOnFocusChangeListener(new OnFocusChangeListener(){

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(hasFocus)
						((EditText) v).selectAll();
				}
				
			});
			
			holder.txtQty.setOnKeyListener(new OnKeyListener(){

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					float qty = stock.getCountQty();
					
					qty = Float.parseFloat(((EditText) v).getText().toString());
					
					mReceiveStock.updateDocumentDetail(stock.getId(), mDocumentId, 
							mShopId, stock.getMatId(), qty, stock.getPricePerUnit(), stock.getTaxType(), "");
					
					return false;
				}
				
			});
			
			holder.txtPrice.setOnKeyListener(new OnKeyListener(){

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					float price = stock.getPricePerUnit();
					
					price = Float.parseFloat(((EditText) v).getText().toString());
					
					mReceiveStock.updateDocumentDetail(stock.getId(), mDocumentId, 
							mShopId, stock.getMatId(), stock.getCurrQty(), price, stock.getTaxType(), "");
					
					return false;
				}
				
			});
			
			switch(stock.getTaxType()){
			case 0:
				holder.rdoTaxType.findViewById(R.id.rdoNoVat).setSelected(true);
				break;
			case 1:
				holder.rdoTaxType.findViewById(R.id.rdoIncludeVat).setSelected(true);
				break;
			case 2:
				holder.rdoTaxType.findViewById(R.id.rdoExcludeVat).setSelected(true);
				break;
			}
			return convertView;
		}
		
		private class ViewHolder{
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
		if(mPopup.isShowing())
			mPopup.dismiss();
		return true;
	}

	@Override
	public boolean onMenuItemActionExpand(MenuItem item) {
		if(mPopup.isShowing())
			mPopup.dismiss();
		return true;
	}
}
