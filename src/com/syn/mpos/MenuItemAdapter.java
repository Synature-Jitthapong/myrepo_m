package com.syn.mpos;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.j1tth4.mobile.util.ImageLoader;
import com.syn.mpos.database.GlobalProperty;
import com.syn.mpos.database.Products;

public class MenuItemAdapter extends BaseAdapter{
	
	private SQLiteDatabase mSqlite;
	private ImageLoader mImgLoader;
	private List<Products.Product> mProductLst;
	private LayoutInflater mInflater;
	
	public MenuItemAdapter(Context c, SQLiteDatabase sqlite, List<Products.Product> productLst){
		mSqlite = sqlite;
		mProductLst = productLst;
		mImgLoader = new ImageLoader(c, R.drawable.default_image,
				MPOSApplication.IMG_DIR, ImageLoader.IMAGE_SIZE.MEDIUM);
		mInflater =
				(LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return mProductLst.size();
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
		final Products.Product p = mProductLst.get(position);
		final ViewHolder holder;
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.menu_template, null);
			holder = new ViewHolder();
			holder.tvMenu = (TextView) convertView.findViewById(R.id.textViewMenuName);
			holder.tvPrice = (TextView) convertView.findViewById(R.id.textViewMenuPrice);
			holder.imgMenu = (ImageView) convertView.findViewById(R.id.imageViewMenu);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.tvMenu.setText(p.getProductName());
		if(p.getProductPrice() < 0)
			holder.tvPrice.setVisibility(View.INVISIBLE);
		else
			holder.tvPrice.setText(GlobalProperty.currencyFormat(mSqlite, p.getProductPrice()));

		new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				try {
					mImgLoader.displayImage(MPOSApplication.getImageUrl() + p.getImgUrl(), holder.imgMenu);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}, 500);
		return convertView;
	}
	
	public static class ViewHolder{
		ImageView imgMenu;
		TextView tvMenu;
		TextView tvPrice;
	}
}
