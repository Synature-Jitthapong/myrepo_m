package com.syn.mpos;

import java.util.List;

import com.j1tth4.mobile.util.ImageLoader;
import com.syn.mpos.database.Products;
import com.syn.mpos.database.Setting;

import android.os.Bundle;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SearchProductActivity extends Activity {

	private List<Products.Product> mProductLst;
	private ResultAdapter mResultAdapter;
	
	private ListView mLvResult;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_product);
		mLvResult = (ListView) findViewById(R.id.lvSearchResult);
		
		// Get the intent, verify the action and get the query
	    Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      doSearch(query);
	    }
	}

	private void doSearch(String query){
		mProductLst = GlobalVar.sProduct.listProduct(query);
		mResultAdapter = new ResultAdapter();
		mLvResult.setAdapter(mResultAdapter);
	}
	
	// search result adapter
	private class ResultAdapter extends BaseAdapter {
		private ImageLoader imgLoader;

		public ResultAdapter() {
			imgLoader = new ImageLoader(SearchProductActivity.this,
					R.drawable.no_food, GlobalVar.IMG_DIR);
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
		public View getView(int position, View convertView, ViewGroup parent) {
			Products.Product p = mProductLst.get(position);
			ViewHolder holder;

			LayoutInflater inflater = (LayoutInflater) SearchProductActivity.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

			imgLoader.displayImage(Setting.mMenuImageUrl + p.getImgUrl(),
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
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_product, menu);
		return true;
	}

}
