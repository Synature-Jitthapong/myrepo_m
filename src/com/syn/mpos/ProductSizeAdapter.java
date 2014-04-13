package com.syn.mpos;

import java.util.List;

import com.syn.mpos.database.GlobalProperty;
import com.syn.mpos.database.Products;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ProductSizeAdapter extends BaseAdapter{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<Products.Product> mProLst;
	
	public ProductSizeAdapter(Context c, List<Products.Product> proLst){
		mContext = c;
		mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mProLst = proLst;
	}
	
	@Override
	public int getCount() {
		return mProLst != null ? mProLst.size() : 0;
	}

	@Override
	public Products.Product getItem(int position) {
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
		Products.Product p = mProLst.get(position);
		holder.tvProductName.setText(p.getProductName());
		holder.tvProductPrice.setText(GlobalProperty.currencyFormat(mContext, 
				p.getProductPrice()));
		return convertView;
	}

	static class ViewHolder{
		public TextView tvProductName;
		public TextView tvProductPrice;
	}
}
