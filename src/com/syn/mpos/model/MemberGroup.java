package com.syn.mpos.model;

public class MemberGroup {
	private int memberGroupId;
	private String memberGroupCode;
	private String memberGroupName;
	
	public String getMemberGroupCode() {
		return memberGroupCode;
	}
	public void setMemberGroupCode(String memberGroupCode) {
		this.memberGroupCode = memberGroupCode;
	}
	public int getMemberGroupId() {
		return memberGroupId;
	}
	public void setMemberGroupId(int memberGroupId) {
		this.memberGroupId = memberGroupId;
	}
	public String getMemberGroupName() {
		return memberGroupName;
	}
	public void setMemberGroupName(String memberGroupName) {
		this.memberGroupName = memberGroupName;
	}
}
