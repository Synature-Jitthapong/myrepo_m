package com.synature.mpos.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.synature.mpos.database.model.Member;
import com.synature.mpos.database.table.MemberTable;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

public class MemberDao extends MPOSDatabase{

	public MemberDao(Context context) {
		super(context);
	}

	public Member getMember(String code){
		Member m = null;
		Cursor cursor = getReadableDatabase().query(MemberTable.TABLE_MEMBER, 
				new String[]{
					MemberTable.COLUMN_MEMBER_ID,
					MemberTable.COLUMN_MEMBER_CODE,
					MemberTable.COLUMN_MEMBER_FIRST_NAME,
					MemberTable.COLUMN_MEMBER_LAST_NAME
				}, 
				MemberTable.COLUMN_MEMBER_CODE + "=?", 
				new String[]{
					code
				}, null, null, null);
		if(cursor.moveToFirst()){
			m = new Member();
			m.setMemberId(cursor.getInt(cursor.getColumnIndex(MemberTable.COLUMN_MEMBER_ID)));
			m.setMemberCode(cursor.getString(cursor.getColumnIndex(MemberTable.COLUMN_MEMBER_CODE)));
			m.setMemberFirstName(cursor.getString(cursor.getColumnIndex(MemberTable.COLUMN_MEMBER_FIRST_NAME)));
			m.setMemberLastName(cursor.getString(cursor.getColumnIndex(MemberTable.COLUMN_MEMBER_LAST_NAME)));
		}
		cursor.close();
		return m;
	}
	
	public void loadMembers() throws IOException {
		Log.d("Member", "Loading member...");
		final Resources resources = mContext.getResources();
		InputStream inputStream = resources
				.openRawResource(com.synature.mpos.R.raw.member);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));

		try {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] strings = TextUtils.split(line, ",");
				addMember(Integer.parseInt(strings[0].trim()), strings[1].trim(),
						strings[2].trim(), strings[3].trim());
			}
		} finally {
			reader.close();
		}
		Log.d("Member", "DONE loading member.");
	}
	
	public int countMember(){
		int total = 0;
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT COUNT(*) FROM " + MemberTable.TABLE_MEMBER, null);
		if(cursor.moveToFirst()){
			total = cursor.getInt(0);
		}
		return total;
	}
	
	private void addMember(int id, String code, String firstName, String lastName){
		ContentValues cv = new ContentValues();
		cv.put(MemberTable.COLUMN_MEMBER_ID, id);
		cv.put(MemberTable.COLUMN_MEMBER_CODE, code);
		cv.put(MemberTable.COLUMN_MEMBER_FIRST_NAME, firstName);
		cv.put(MemberTable.COLUMN_MEMBER_LAST_NAME, lastName);
		getWritableDatabase().insertOrThrow(MemberTable.TABLE_MEMBER, null, cv);
	}
}
