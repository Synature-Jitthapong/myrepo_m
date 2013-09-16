package com.syn.mpos;

import java.util.List;

import com.syn.mpos.inventory.MPOSStockDocument;
import com.syn.pos.inventory.Document;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

public class InvDirectReceiveActivity extends Activity {
	
	private MPOSStockDocument mDocument;
	private List<Document.DocDetail> docDetailLst;
	private int mDocumentId;
	private int mShopId;
	private int mStaffId;
	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inv_direct_receive);
		mContext = InvDirectReceiveActivity.this;
		
		Intent intent = getIntent();
		mShopId = intent.getIntExtra("shopId", 0);
		mStaffId = intent.getIntExtra("staffId", 0);
		if(mShopId == 0 || mStaffId == 0)
			finish();
	}

	@Override
	protected void onResume() {
		init();
		super.onResume();
	}

	private void init(){
		mDocument = new MPOSStockDocument(mContext);
		mDocumentId = mDocument.getCurrentDocument(mShopId, 
				MPOSStockDocument.DIRECT_RECEIVE_DOC);
		if(mDocumentId > 0){
			// load document
			docDetailLst = mDocument.listAllDocDetail(mDocumentId, mShopId);
		}else{
			// create new document
			mDocumentId = mDocument.createDocument(mShopId, MPOSStockDocument.DIRECT_RECEIVE_DOC, mStaffId);
		}
	}
	
	public void popReceiveClicked(final View v){
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View addProView = inflater.inflate(R.layout.add_product_layout, null);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setView(addProView);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
			
		});
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
			
		});
		dialog.show();
	}
}
