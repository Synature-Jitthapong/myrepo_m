package com.synature.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.synature.mpos.database.model.Comment;
import com.synature.mpos.database.model.CommentGroup;
import com.synature.mpos.database.table.MenuFixCommentTable;
import com.synature.mpos.database.table.ProductGroupTable;
import com.synature.mpos.database.table.ProductTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class MenuComment extends MPOSDatabase{

	public MenuComment(Context context) {
		super(context);
	}

	/**
	 * @return List<Comment> 
	 */
	public List<Comment> listMenuComment(){
		List<Comment> mcl = new ArrayList<Comment>();
		Cursor cursor = getReadableDatabase().query(
				ProductTable.TABLE_PRODUCT, 
				new String[]{
						ProductTable.COLUMN_PRODUCT_ID,
						ProductTable.COLUMN_PRODUCT_NAME,
						ProductTable.COLUMN_PRODUCT_PRICE
				}, 
				COLUMN_DELETED + "=?", 
				new String[]{
						String.valueOf(NOT_DELETE)
				}, null, null, COLUMN_ORDERING + ", " + ProductTable.COLUMN_PRODUCT_NAME);
		if(cursor.moveToFirst()){
			do{
				Comment cm = new Comment();
				cm.setCommentId(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID)));
				cm.setCommentName(cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME)));
				cm.setCommentPrice(cursor.getDouble(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_PRICE)));
				mcl.add(cm);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return mcl;
	}
	
	/**
	 * @param groupId
	 * @return List<Comment>
	 */
	public List<Comment> listMenuComment(int groupId){
		List<Comment> mcl = new ArrayList<Comment>();
		Cursor cursor = getReadableDatabase().query(
				ProductTable.TABLE_PRODUCT, 
				new String[]{
						ProductTable.COLUMN_PRODUCT_ID,
						ProductTable.COLUMN_PRODUCT_NAME,
						ProductTable.COLUMN_PRODUCT_PRICE
				}, 
				ProductGroupTable.COLUMN_PRODUCT_GROUP_ID + "=?"
				+ " AND " + COLUMN_DELETED + "=?", 
				new String[]{
						String.valueOf(groupId),
						String.valueOf(NOT_DELETE)
				}, null, null, COLUMN_ORDERING + ", " + ProductTable.COLUMN_PRODUCT_NAME);
		if(cursor.moveToFirst()){
			do{
				Comment cm = new Comment();
				cm.setCommentId(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID)));
				cm.setCommentName(cursor.getString(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_NAME)));
				cm.setCommentPrice(cursor.getDouble(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_PRICE)));
				mcl.add(cm);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return mcl;
	}
	
	/**
	 * @return List<CommentGroup> 
	 */
	public List<CommentGroup> listMenuCommentGroup(){
		List<CommentGroup> cgl = new ArrayList<CommentGroup>();
		Cursor cursor = getReadableDatabase().query(
				ProductGroupTable.TABLE_PRODUCT_GROUP, 
				new String[]{
					ProductGroupTable.COLUMN_PRODUCT_GROUP_ID,
					ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME
				}, 
				ProductGroupTable.COLUMN_IS_COMMENT + "=?"
				+ " AND " + COLUMN_DELETED + "=?", 
				new String[]{
						String.valueOf(1),
						String.valueOf(Products.NOT_DELETE)
				}, null, null, null);
		if(cursor.moveToFirst()){
			do{
				CommentGroup cg = new CommentGroup();
				cg.setCommentGroupId(cursor.getInt(cursor.getColumnIndex(ProductGroupTable.COLUMN_PRODUCT_GROUP_ID)));
				cg.setCommentGroupName(cursor.getString(cursor.getColumnIndex(ProductGroupTable.COLUMN_PRODUCT_GROUP_NAME)));
				cgl.add(cg);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return cgl;
	}
	
	/**
	 * @param cfl
	 */
	public void insertMenuFixComment(List<com.synature.pos.MenuFixComment> cfl){
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(MenuFixCommentTable.TABLE_MENU_FIX_COMMENT, null, null);
			for(com.synature.pos.MenuFixComment cf : cfl){
				ContentValues cv = new ContentValues();
				cv.put(ProductTable.COLUMN_PRODUCT_ID, cf.getMID());
				cv.put(MenuFixCommentTable.COLUMN_COMMENT_ID, cf.getMCOMID());
				getWritableDatabase().insertOrThrow(MenuFixCommentTable.TABLE_MENU_FIX_COMMENT, 
						null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally {
			getWritableDatabase().endTransaction();
		}
	}
}
