package com.syn.mpos.database;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class ProductProvider extends ContentProvider {
	public static final String AUTHORITY = "com.syn.mpos.ProductProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + Products.TB_PRODUCT);
	
	public static final String SUGGEST_COLUMNS[] = {
		BaseColumns._ID,
		SearchManager.SUGGEST_COLUMN_INTENT_DATA,
		//SearchManager.SUGGEST_COLUMN_ICON_1,
		SearchManager.SUGGEST_COLUMN_TEXT_1
	};
	
	private Products mProduct;
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getType(Uri uri) {
		throw new IllegalArgumentException("Unknown URL " + uri);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean onCreate() {
		mProduct = new Products(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		String query = "";
		try {
			query = selectionArgs[0];
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MatrixCursor matrixCursor = new MatrixCursor(SUGGEST_COLUMNS);
		mProduct.open();
		Cursor cursor = mProduct.queryProduct(Products.ALL_PRODUCT_COLS, 
				Products.COL_PRODUCT_NAME + " LIKE '" + query + "%'", null);
		if(cursor.moveToFirst()){ 
			do{
				matrixCursor.addRow(new Object[]{
						cursor.getInt(cursor.getColumnIndex(Products.COL_PRODUCT_ID)),
						cursor.getInt(cursor.getColumnIndex(Products.COL_PRODUCT_ID)),
						//cursor.getString(cursor.getColumnIndex(Products.COL_IMG_URL)),
						cursor.getString(cursor.getColumnIndex(Products.COL_PRODUCT_NAME))
				});
			}while(cursor.moveToNext());
		}
		cursor.close();
		mProduct.close();
		return matrixCursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

}
