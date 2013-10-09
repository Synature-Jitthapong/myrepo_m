package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.j1tth4.mobile.sqlite.SQLiteHelper;
import com.syn.pos.MemberGroup;

public class Member extends Util {
	private SQLiteHelper mDbHelper;
	
	public Member(Context context){
		super(context);
		mDbHelper = new MPOSSQLiteHelper(context);
	}
	
	public int getMaxMember(int shopId){
		int memberId = 0;
		String strSql = "SELECT MAX(member_id) " +
				" FROM members " +
				" WHERE shop_id=" + shopId;
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			memberId = cursor.getInt(0);
		}
		cursor.close();
		mDbHelper.close();
		return memberId + 1;
	}
	
	public boolean checkMemberCode(String code){
		boolean isPass = true;
		String strSql = "SELECT member_code " +
				" FROM members " +
				" WHERE member_code='" + code + "' ";
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			isPass = false;
		}
		mDbHelper.close();
		return isPass;
	}
	
	public List<MemberGroup> listMemberGroups(int shopId){
		List<MemberGroup> mgLst = new ArrayList<MemberGroup>();
		
		String strSql = "SELECT * FROM member_group " +
				" WHERE shop_id=" + shopId;
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				MemberGroup mg = new MemberGroup();
				mg.setMemberGroupId(cursor.getInt(cursor.getColumnIndex("member_group_id")));
				mg.setMemberGroupCode(cursor.getString(cursor.getColumnIndex("member_group_code")));
				mg.setMemberGroupName(cursor.getString(cursor.getColumnIndex("member_group_name")));
				
				mgLst.add(mg);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mDbHelper.close();
		return mgLst;
	}
	
	public boolean addMember(String code, String firstName, 
			String lastName, int gender, int groupId, String addr1,
			String addr2, String city, int province, String zipCode,
			String mobile, String tel, String fax, String email,
			long birthDay, long expDate, String remark, int staffId, int shopId){
		boolean isSuccess = false;
		Calendar dateTime = getDateTime();
		int memberId = getMaxMember(shopId);
		
		ContentValues cv = new ContentValues();
		cv.put("member_id", memberId);
		cv.put("shop_id", shopId);
		cv.put("member_group_id", groupId);
		cv.put("member_code", code);
		cv.put("member_gender", gender);
		cv.put("member_firstname", firstName);
		cv.put("member_lastname", lastName);
		cv.put("member_address1", addr1);
		cv.put("member_address2", addr2);
		cv.put("member_city", city);
		cv.put("member_province", province);
		cv.put("member_zipcode", zipCode);
		cv.put("member_mobile", mobile);
		cv.put("member_tel", tel);
		cv.put("member_fax", fax);
		cv.put("member_email", email);
		cv.put("member_birthday", birthDay);
		cv.put("member_expiredate", expDate);
		cv.put("remark", remark);
		cv.put("update_by", staffId);
		cv.put("update_date", dateTime.getTimeInMillis());
		cv.put("insert_at_shop_id", shopId);
		
		mDbHelper.open();
		isSuccess = mDbHelper.insert("members", cv);
		mDbHelper.close();
		return isSuccess;
	}
}
