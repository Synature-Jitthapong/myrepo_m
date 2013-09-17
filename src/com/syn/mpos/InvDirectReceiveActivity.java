package com.syn.mpos;

import java.util.List;

import com.syn.mpos.inventory.MPOSStockDocument;
import com.syn.pos.inventory.Document;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class InvDirectReceiveActivity extends Activity {
	
	private MPOSStockDocument mDocument;
	private List<Document.DocDetail> docDetailLst;
	private int mDocumentId;
	private int mShopId;
	private int mStaffId;
	private Context mContext;
	
	private MenuItem menuItem;
	
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.direct_receive, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
	    switch (item.getItemId()) {
		    case R.id.itemSearch:
		    	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private void init(){
		mDocument = new MPOSStockDocument(mContext);
		mDocumentId = mDocument.getCurrentDocument(mShopId, 
				MPOSStockDocument.DIRECT_RECEIVE_DOC);
		if(mDocumentId > 0){
			// load document
			//docDetailLst = mDocument.listAllDocDetail(mDocumentId, mShopId);
		}else{
			// create new document
			mDocumentId = mDocument.createDocument(mShopId, MPOSStockDocument.DIRECT_RECEIVE_DOC, mStaffId);
		}
	}
}
