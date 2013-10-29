package com.syn.mpos;

import java.util.List;

import com.syn.mpos.database.Products;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ManageProductActivity extends Activity implements OnClickListener{

	private int mProductDeptId;
	private Products mProduct;
	private Formatter mFormat;
	private List<Products.ProductDept> mPdLst;
	private List<Products.Product> mPLst;
	private ProductDeptAdapter mPdAdapter;
	private ProductAdapter mPAdapter;
	private ListView mLvProductDept;
	private ListView mLvProduct;
	private EditText mTxtProductDeptCode;
	private EditText mTxtProductDeptName;
	private EditText mTxtProductCode;
	private EditText mTxtProductName;
	private EditText mTxtProductPrice;
	
	private Button mBtnAddProductDept;
	private Button mBtnAddProduct;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_product);
		
		mTxtProductDeptCode = (EditText) findViewById(R.id.editText1);
		mTxtProductDeptName = (EditText) findViewById(R.id.editText2);
		mTxtProductCode = (EditText) findViewById(R.id.editText3);
		mTxtProductName = (EditText) findViewById(R.id.editText4);
		mTxtProductPrice = (EditText) findViewById(R.id.editText5);
		mLvProductDept = (ListView) findViewById(R.id.listView1);
		mLvProduct = (ListView) findViewById(R.id.listView2);
		mBtnAddProductDept = (Button) findViewById(R.id.button1);
		mBtnAddProduct = (Button) findViewById(R.id.button2);
		
		mProduct = new Products(this);
		mFormat = new Formatter(this);
		
		mPdLst = mProduct.listProductDept();
		mPdAdapter = new ProductDeptAdapter();
		mLvProductDept.setAdapter(mPdAdapter);
		
		mBtnAddProductDept.setOnClickListener(this);
		mBtnAddProduct.setOnClickListener(this);
	}

	void showProduct(int position){
		mLvProductDept.setItemChecked(position, true);

		Products.ProductDept pd = (Products.ProductDept) mLvProductDept.getItemAtPosition(position);
		mProductDeptId = pd.getProductDeptId();
		mPLst = mProduct.listProduct(mProductDeptId);
		mPAdapter = new ProductAdapter();
		mLvProduct.setAdapter(mPAdapter);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.button1 :
			if(!mTxtProductDeptCode.getText().toString().isEmpty() && 
					!mTxtProductDeptName.getText().toString().isEmpty()){
				int deptId = mProduct.insertProductDept(mTxtProductDeptCode.getText().toString(), 
						mTxtProductDeptName.getText().toString());
				mPdLst.add(mProduct.getProductDept(deptId));
				mPdAdapter.notifyDataSetChanged();
				
				mTxtProductDeptCode.setText(null);
				mTxtProductDeptName.setText(null);
				mTxtProductDeptCode.requestFocus();
			}
			break;
		case R.id.button2:
			if(!mTxtProductCode.getText().toString().isEmpty() &&
					!mTxtProductName.getText().toString().isEmpty() &&
					!mTxtProductPrice.getText().toString().isEmpty()){

				float price = 0.0f;
				try {
					price = Float.parseFloat(mTxtProductPrice.getText().toString());
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				int proId = mProduct.insertProducts(mProductDeptId, mTxtProductCode.getText().toString(), 
						mTxtProductCode.getText().toString(), mTxtProductName.getText().toString(), price);
				mPLst.add(mProduct.getProduct(proId));
				mPAdapter.notifyDataSetChanged();
				
				mTxtProductCode.setText(null);
				mTxtProductName.setText(null);
				mTxtProductPrice.setText(null);
				mTxtProductCode.requestFocus();
			}
			break;
		}
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
