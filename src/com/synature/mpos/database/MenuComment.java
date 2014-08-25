package com.synature.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.synature.mpos.database.model.Comment;
import com.synature.mpos.database.model.CommentGroup;
import com.synature.mpos.database.table.MenuCommentGroupTable;
import com.synature.mpos.database.table.MenuCommentTable;
import com.synature.mpos.database.table.MenuFixCommentTable;
import com.synature.mpos.database.table.ProductTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class MenuComment extends MPOSDatabase{

	public MenuComment(Context context) {
		super(context);
	}

	/**
	 * @return List<Comment> 
	 */
	public List<Comment> listMenuComment(){
		List<Comment> cml = new ArrayList<Comment>();
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT a." + MenuCommentTable.COLUMN_COMMENT_ID + ","
				+ " a." + MenuCommentTable.COLUMN_COMMENT_NAME + ","
				+ " b." + ProductTable.COLUMN_PRODUCT_PRICE
				+ " FROM " + MenuCommentTable.TABLE_MENU_COMMENT + " a "
				+ " LEFT JOIN " + ProductTable.TABLE_PRODUCT + " b "
				+ " ON a." + MenuCommentTable.COLUMN_COMMENT_ID
				+ " =b." + ProductTable.COLUMN_PRODUCT_ID
				+ " ORDER BY a." + COLUMN_ORDERING, null);
		if(cursor.moveToFirst()){
			do{
				Comment cm = new Comment();
				cm.setCommentId(cursor.getInt(
						cursor.getColumnIndex(MenuCommentTable.COLUMN_COMMENT_ID)));
				cm.setCommentName(cursor.getString(
						cursor.getColumnIndex(MenuCommentTable.COLUMN_COMMENT_NAME)));
				cm.setCommentPrice(cursor.getDouble(
						cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_PRICE)));
				cml.add(cm);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return cml;
	}
	
	/**
	 * @param groupId
	 * @return List<Comment>
	 */
	public List<Comment> listMenuComment(int groupId){
		List<Comment> mcl = new ArrayList<Comment>();
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT a." + MenuCommentTable.COLUMN_COMMENT_ID + ","
				+ " a." + MenuCommentTable.COLUMN_COMMENT_NAME + ","
				+ " b." + ProductTable.COLUMN_PRODUCT_PRICE
				+ " FROM " + MenuCommentTable.TABLE_MENU_COMMENT + " a "
				+ " LEFT JOIN " + ProductTable.TABLE_PRODUCT + " b "
				+ " ON a." + MenuCommentTable.COLUMN_COMMENT_ID
				+ " =b." + ProductTable.COLUMN_PRODUCT_ID
				+ " WHERE a." + MenuCommentTable.COLUMN_COMMENT_GROUP_ID + "=?"
				+ " ORDER BY a." + COLUMN_ORDERING, 
				new String[]{
						String.valueOf(groupId)
				});
		if(cursor.moveToFirst()){
			do{
				Comment cm = new Comment();
				cm.setCommentId(cursor.getInt(
						cursor.getColumnIndex(MenuCommentTable.COLUMN_COMMENT_ID)));
				cm.setCommentName(cursor.getString(
						cursor.getColumnIndex(MenuCommentTable.COLUMN_COMMENT_NAME)));
				cm.setCommentPrice(cursor.getDouble(
						cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_PRICE)));
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
				MenuCommentGroupTable.TABLE_MENU_COMMENT_GROUP, 
				new String[]{
					MenuCommentTable.COLUMN_COMMENT_GROUP_ID,
					MenuCommentGroupTable.COLUMN_COMMENT_GROUP_NAME
				}, 
				null, null, null, null, null);
		if(cursor.moveToFirst()){
			do{
				CommentGroup cg = new CommentGroup();
				cg.setCommentGroupId(cursor.getInt(
						cursor.getColumnIndex(MenuCommentTable.COLUMN_COMMENT_GROUP_ID)));
				cg.setCommentGroupName(cursor.getString(
						cursor.getColumnIndex(MenuCommentGroupTable.COLUMN_COMMENT_GROUP_NAME)));
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
				cv.put(MenuCommentTable.COLUMN_COMMENT_ID, cf.getMCOMID());
				getWritableDatabase().insertOrThrow(MenuFixCommentTable.TABLE_MENU_FIX_COMMENT, 
						null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally {
			getWritableDatabase().endTransaction();
		}
	}
	
	/**
	 * @param cgl
	 * @throws SQLException
	 */
	public void insertMenuCommentGroup(List<com.synature.pos.MenuCommentGroup> cgl) throws SQLException{
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(MenuCommentGroupTable.TABLE_MENU_COMMENT_GROUP, null, null);
			for(com.synature.pos.MenuCommentGroup cg : cgl){
				ContentValues cv = new ContentValues();
				cv.put(MenuCommentTable.COLUMN_COMMENT_GROUP_ID, cg.getMCGRID());
				cv.put(MenuCommentGroupTable.COLUMN_COMMENT_GROUP_NAME, cg.getMCGRNAM0());
				getWritableDatabase().insertOrThrow(MenuCommentGroupTable.TABLE_MENU_COMMENT_GROUP, 
						null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally {
			getWritableDatabase().endTransaction();
		}
	}
	
	/**
	 * @param cml
	 * @throws SQLException
	 */
	public void insertMenuComment(List<com.synature.pos.MenuComment> cml) throws SQLException{
		getWritableDatabase().beginTransaction();
		try {
			getWritableDatabase().delete(MenuCommentTable.TABLE_MENU_COMMENT, null, null);
			for(com.synature.pos.MenuComment cm : cml){
				ContentValues cv = new ContentValues();
				cv.put(MenuCommentTable.COLUMN_COMMENT_ID, cm.getMCID());
				cv.put(MenuCommentTable.COLUMN_COMMENT_GROUP_ID, cm.getMCGRID());
				cv.put(MenuCommentTable.COLUMN_COMMENT_NAME, cm.getMCNAM0());
				cv.put(MenuCommentTable.COLUMN_COMMENT_NAME1, cm.getMCNAM1());
				cv.put(MenuCommentTable.COLUMN_COMMENT_NAME2, cm.getMCNAM2());
				cv.put(MenuCommentTable.COLUMN_COMMENT_NAME3, cm.getMCNAM3());
				cv.put(COLUMN_ORDERING, cm.getMCORD());
				getWritableDatabase().insertOrThrow(MenuCommentTable.TABLE_MENU_COMMENT, null, cv);
			}
			getWritableDatabase().setTransactionSuccessful();
		} finally{
			getWritableDatabase().endTransaction();
		}
	}
}
