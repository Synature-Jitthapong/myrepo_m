package com.syn.mpos;

import java.util.List;

import com.syn.mpos.database.GlobalPropertyDataSource;
import com.syn.mpos.database.ProductsDataSource;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ProductSizeAdapter extends BaseAdapter{
	
	private SQLiteDatabase mSqlite;
	private LayoutInflater mInflater;
	private List<ProductsDataSource.Product> mProLst;
	
	public ProductSizeAdapter(Context c, SQLiteDatabase sqlite, List<ProductsDataSource.Product> proLst){
		mSqlite = sqlite;
		mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mProLst = proLst;
	}
	
	@Override
	public int getCount() {
		return mProLst != null ? mProLst.size() : 0;
	}

	@Override
	public ProductsDataSource.Product getItem(int position) {
		return mProLst.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.product_size_template, null);
			holder = new ViewHolder();
			holder.tvProductName = (TextView) convertView.findViewById(R.id.tvProductName);
			holder.tvProductPrice = (TextView) convertView.findViewById(R.id.tvProductPrice);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		ProductsDataSource.Product p = mProLst.get(position);
		holder.tvProductName.setText(p.getProductName());
		holder.tvProductPrice.setText(GlobalPropertyDataSource.currencyFormat(
				mSqlite, p.getProductPrice()));
		return convertView;
	}

	static class ViewHolder{
		public TextView tvProductName;
		public TextView tvProductPrice;
	}
}
