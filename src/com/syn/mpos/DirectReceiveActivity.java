package com.syn.mpos;

import java.util.ArrayList;
import java.util.List;
import com.syn.mpos.inventory.MPOSReceiveStock;
import com.syn.mpos.inventory.MPOSStockDocument;
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
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

public class DirectReceiveActivity extends Activity {
	
	private MPOSReceiveStock mReceiveStock;
	private com.syn.mpos.database.MenuItem menuItem;
	private List<MenuGroups.MenuItem> menuLst;
	private ResultAdapter mResultAdapter;
	private List<Document.DocDetail> docDetailLst;
	private int mDocumentId;
	private int mShopId;
	private int mStaffId;
	private Context mContext;
	
	private SearchView mSearchView;
	private PopupWindow mPopup;
	private View mPopSearch;
	private ListView mListViewResult;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inv_direct_receive);
		mContext = DirectReceiveActivity.this;
		
		Intent intent = getIntent();
		mShopId = intent.getIntExtra("shopId", 0);
		mStaffId = intent.getIntExtra("staffId", 0);
		if(mShopId == 0 || mStaffId == 0)
			finish();
		
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

		mSearchView = (SearchView) menu.findItem(R.id.itemSearch)
				.getActionView();
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
	public void onWindowFocusChanged(boolean hasFocus) {
		mPopup.dismiss();
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
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
					mPopup.dismiss();
			}
			
		});
		
		mPopup = new PopupWindow(mContext);
		mPopup.setContentView(mPopSearch);
		mPopup.setWidth(300);
		mPopup.setHeight(300);
//		mPopup.setFocusable(true);
	}
	
	private void showPopup() {
		mPopup.showAsDropDown(mSearchView);
	}
	
	private void init(){
		mReceiveStock = new MPOSReceiveStock(mContext);
				
		mDocumentId = mReceiveStock.getCurrentDocument(mShopId, 
				MPOSStockDocument.DIRECT_RECEIVE_DOC);
		if(mDocumentId > 0){
			// load document
			//docDetailLst = mDocument.listAllDocDetail(mDocumentId, mShopId);
		}else{
			// create new document
			mDocumentId = mReceiveStock.createDocument(mShopId, MPOSStockDocument.DIRECT_RECEIVE_DOC, mStaffId);
		}
	}
	
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
}
