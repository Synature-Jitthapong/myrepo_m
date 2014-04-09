package com.syn.mpos;

import java.util.List;

import com.syn.mpos.datasource.GlobalProperty;
import com.syn.mpos.datasource.Products;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ProductSizeAdapter extends BaseAdapter{
	private Context mContext;
	private List<Products.Product> mProLst;
	
	public ProductSizeAdapter(Context c, List<Products.Product> proLst){
		mContext = c;
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
		Products.Product p = mProLst.get(position);
		LayoutInflater inflater = (LayoutInflater)
				mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewHolder holder;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.product_size_template, null);
			holder = new ViewHolder();
			holder.tvProductName = (TextView) convertView.findViewById(R.id.tvProductName);
			holder.tvProductPrice = (TextView) convertView.findViewById(R.id.tvProductPrice);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tvProductName.setText(p.getProductName());
		holder.tvProductPrice.setText(MPOSApplication.getGlobalProperty().currencyFormat(p.getProductPrice()));
		return convertView;
	}

	static class ViewHolder{
		public TextView tvProductName;
		public TextView tvProductPrice;
	}
}
