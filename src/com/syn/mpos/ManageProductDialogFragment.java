package com.syn.mpos;

import java.util.List;

import com.syn.mpos.database.Products;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ManageProductDialogFragment extends DialogFragment implements OnClickListener{

	private static OnManageProductDismissListener listener;
	private int mProductDeptId;
	private Products mProduct;
	private List<Products.ProductDept> mPdLst;
	private List<Products.Product> mPLst;
	private ArrayAdapter<Products.Product> mPAdapter;
	private ArrayAdapter<Products.ProductDept> mPdAdapter;
	private ListView mLvProductDept;
	private ListView mLvProduct;
	private EditText mTxtProductDeptCode;
	private EditText mTxtProductDeptName;
	private EditText mTxtProductCode;
	private EditText mTxtProductName;
	private EditText mTxtProductPrice;
	
	private Button mBtnAddProductDept;
	private Button mBtnAddProduct;
	
	public static ManageProductDialogFragment newInstance(){
		ManageProductDialogFragment f = 
				new ManageProductDialogFragment();
		
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mProduct = new Products(getActivity());
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		listener.onDismiss();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof OnManageProductDismissListener){
			listener = (OnManageProductDismissListener) activity;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(mPdLst.size() > 0){
			showProduct(0);
		}
		getDialog().setTitle(R.string.manage_product);
		getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, 
				WindowManager.LayoutParams.MATCH_PARENT);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.manage_product_fragment, container, false); 
		
		mTxtProductDeptCode = (EditText) view.findViewById(R.id.editText1);
		mTxtProductDeptName = (EditText) view.findViewById(R.id.editText2);
		mTxtProductCode = (EditText) view.findViewById(R.id.editText3);
		mTxtProductName = (EditText) view.findViewById(R.id.editText4);
		mTxtProductPrice = (EditText) view.findViewById(R.id.editText5);
		mLvProductDept = (ListView) view.findViewById(R.id.listView1);
		mLvProduct = (ListView) view.findViewById(R.id.listView2);
		mBtnAddProductDept = (Button) view.findViewById(R.id.button1);
		mBtnAddProduct = (Button) view.findViewById(R.id.button2);
		
		mBtnAddProductDept.setOnClickListener(this);
		mBtnAddProduct.setOnClickListener(this);
		mLvProductDept.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {

				showProduct(position);
			}
			
		});
		mLvProduct.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		mPdLst = mProduct.listProductDept();
		mPdAdapter = new ArrayAdapter<Products.ProductDept>(getActivity(),
				android.R.layout.simple_list_item_activated_1, mPdLst);
		mLvProductDept.setAdapter(mPdAdapter);
		
		return view;
	}

	void showProduct(int position){
		mLvProductDept.setItemChecked(position, true);

		Products.ProductDept pd = (Products.ProductDept) mLvProductDept.getItemAtPosition(position);
		mProductDeptId = pd.getProductDeptId();
		mPLst = mProduct.listProduct(mProductDeptId);
		mPAdapter = new ArrayAdapter<Products.Product>(getActivity(),
				android.R.layout.simple_list_item_activated_1, mPLst);
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
			}
			break;
		}
	}
	
	public static interface OnManageProductDismissListener{
		void onDismiss();
	}
}
