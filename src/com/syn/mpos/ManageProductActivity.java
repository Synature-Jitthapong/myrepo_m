package com.syn.mpos;

import java.util.List;

import com.syn.mpos.database.Products;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ManageProductActivity extends Activity{

	private int mProductDeptId;
	private Products mProduct;
	private Formatter mFormat;
	private List<Products.ProductDept> mPdLst;
	private List<Products.Product> mPLst;
	private Products.ProductDept mProDept;
	private ProductDeptAdapter mPdAdapter;
	private ProductAdapter mPAdapter;
	private ListView mLvProductDept;
	private ListView mLvProduct;
	private TextView mTxtDeptName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_product);
		
		mTxtDeptName = (TextView) findViewById(R.id.textView1);
		mLvProductDept = (ListView) findViewById(R.id.listView1);
		mLvProduct = (ListView) findViewById(R.id.listView2);
		
		mProduct = new Products(this);
		mFormat = new Formatter(this);
		
		mPdLst = mProduct.listProductDept();
		mPdAdapter = new ProductDeptAdapter();
		mLvProductDept.setAdapter(mPdAdapter);
		
		showProduct(0);
	}

	void showProduct(int position){
		mLvProductDept.setItemChecked(position, true);

		mProDept = (Products.ProductDept) mLvProductDept.getItemAtPosition(position);
		mProductDeptId = mProDept.getProductDeptId();
		mPLst = mProduct.listProduct(mProductDeptId);
		mPAdapter = new ProductAdapter();
		mLvProduct.setAdapter(mPAdapter);
		mTxtDeptName.setText(mProDept.getProductDeptName());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_manage_product, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemAddProDept:
			addProductDept();
			return true;
		case R.id.itemAddPro:
			addProduct();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	void addProduct(){
		LayoutInflater inflater = (LayoutInflater)
				ManageProductActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View proView = inflater.inflate(R.layout.edit_product_dialog, null);
		final EditText txtProCode = (EditText) proView.findViewById(R.id.editText1);
		final EditText txtProName = (EditText) proView.findViewById(R.id.editText2);
		final EditText txtProPrice = (EditText) proView.findViewById(R.id.editText3);
		
		new AlertDialog.Builder(ManageProductActivity.this)
		.setTitle(mProDept.getProductDeptName())
		.setView(proView)
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		})
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(!txtProCode.getText().toString().isEmpty() &&
						!txtProName.getText().toString().isEmpty() &&
						!txtProPrice.getText().toString().isEmpty()){

					float price = 0.0f;
					try {
						price = Float.parseFloat(txtProPrice.getText().toString());
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					int proId = mProduct.insertProducts(mProductDeptId, txtProCode.getText().toString(), 
							txtProCode.getText().toString(), txtProName.getText().toString(), price);
					mPLst.add(mProduct.getProduct(proId));
					mPAdapter.notifyDataSetChanged();
				}
			}
		}).show();
	}
	
	void addProductDept(){
		LayoutInflater inflater = (LayoutInflater)
				ManageProductActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View deptView = inflater.inflate(R.layout.edit_productdept_dialog, null);
			final EditText txtDeptCode = (EditText) deptView.findViewById(R.id.editText1);
			final EditText txtDeptName = (EditText) deptView.findViewById(R.id.editText2);
			
			new AlertDialog.Builder(ManageProductActivity.this)
			.setTitle(R.string.add)
			.setView(deptView)
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			})
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(!txtDeptCode.getText().toString().isEmpty() &&
							!txtDeptName.getText().toString().isEmpty()){
						int deptId = mProduct.insertProductDept(txtDeptCode.getText().toString(), 
								txtDeptName.getText().toString());
						mPdLst.add(mProduct.getProductDept(deptId));
						mPdAdapter.notifyDataSetChanged();
					}
				}
			}).show();
	}
	
	class ProductAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mPLst.size();
		}

		@Override
		public Products.Product getItem(int position) {
			return mPLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final LayoutInflater inflater = (LayoutInflater)
					ManageProductActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.manage_product_template, null);
			TextView tvName = (TextView) convertView.findViewById(R.id.textView1);
			TextView tvPrice = (TextView) convertView.findViewById(R.id.textView2);
			tvName.setText(mPLst.get(position).getProductCode() + ":");
			tvName.append(mPLst.get(position).getProductName());
			tvPrice.setText(mFormat.currencyFormat(mPLst.get(position).getProductPrice()));
			
			convertView.setOnLongClickListener(new OnLongClickListener(){

				@Override
				public boolean onLongClick(View v) {
					v = inflater.inflate(R.layout.edit_product_dialog, null);
					final EditText txtCode = (EditText) v.findViewById(R.id.editText1);
					final EditText txtName = (EditText) v.findViewById(R.id.editText2);
					final EditText txtPrice = (EditText) v.findViewById(R.id.editText3);
					
					txtCode.setSelectAllOnFocus(true);
					txtName.setSelectAllOnFocus(true);
					txtPrice.setSelectAllOnFocus(true);
					txtCode.setText(mPLst.get(position).getProductCode());
					txtName.setText(mPLst.get(position).getProductName());
					txtPrice.setText(mFormat.currencyFormat(mPLst.get(position).getProductPrice()));
					
					new AlertDialog.Builder(ManageProductActivity.this)
					.setTitle(mPLst.get(position).getProductName())
					.setView(v)
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					})
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(!txtCode.getText().toString().isEmpty() &&
									!txtName.getText().toString().isEmpty() &&
									!txtPrice.getText().toString().isEmpty()){
								float price = 0.0f;
								try {
									price = Float.parseFloat(txtPrice.getText().toString());
								} catch (NumberFormatException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								Products.Product p = mPLst.get(position);
								p.setProductCode(txtCode.getText().toString());
								p.setProductName(txtName.getText().toString());
								p.setProductPrice(price);
								
								mProduct.updateProduct(p.getProductId(), 
										p.getProductCode(), 
										p.getProductName(), 
										p.getProductPrice());
								
								mPAdapter.notifyDataSetChanged();	
							}
						}
					}).show();
					return true;
				}
			});
			return convertView;
		}
		
	}
	
	class ProductDeptAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mPdLst.size();
		}

		@Override
		public Products.ProductDept getItem(int position) {
			return mPdLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final LayoutInflater inflater = (LayoutInflater)
					ManageProductActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.manage_product_dept_template, null);
			TextView tvName = (TextView) convertView.findViewById(R.id.textView1);
			tvName.setText(mPdLst.get(position).getProductDeptCode() + ":");
			tvName.append(mPdLst.get(position).getProductDeptName());
			
			convertView.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					showProduct(position);
				}
				
			});
			
			convertView.setOnLongClickListener(new OnLongClickListener(){

				@Override
				public boolean onLongClick(View v) {
					v = inflater.inflate(R.layout.edit_productdept_dialog, null);
					final EditText txtDeptCode = (EditText) v.findViewById(R.id.editText1);
					final EditText txtDeptName = (EditText) v.findViewById(R.id.editText2);
					
					txtDeptCode.setSelectAllOnFocus(true);
					txtDeptName.setSelectAllOnFocus(true);
					txtDeptCode.setText(mPdLst.get(position).getProductDeptCode());
					txtDeptName.setText(mPdLst.get(position).getProductDeptName());
					
					new AlertDialog.Builder(ManageProductActivity.this)
					.setTitle(mPdLst.get(position).getProductDeptName())
					.setView(v)
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					})
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(!txtDeptCode.getText().toString().isEmpty() &&
									!txtDeptName.getText().toString().isEmpty()){
								Products.ProductDept pd = mPdLst.get(position);
								pd.setProductDeptCode(txtDeptCode.getText().toString());
								pd.setProductDeptName(txtDeptName.getText().toString());
								
								mProduct.updateProductDept(pd.getProductDeptId(), 
										pd.getProductDeptCode(), 
										pd.getProductDeptName());
							}
						}
					}).show();
					return true;
				}
				
			});
			return convertView;
		}
		
	}
}
