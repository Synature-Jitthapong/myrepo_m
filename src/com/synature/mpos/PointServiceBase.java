package com.synature.mpos;

import java.lang.reflect.Type;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.synature.connection.Ksoap2WebServiceTask;

public abstract class PointServiceBase extends Ksoap2WebServiceTask{
	
	public static final String NAME_SPACE = "http://tempuri.org/";
	
	public static final String MERCHANT_PARAM = "szMerchantCode";
	public static final String CARD_TAG_CODE_PARAM = "szCardTagCode";
	public static final String REQUEST_ID_PARAM = "szRequestID";
	public static final String JSON_REDEEM_ITEMS_PARAM = "szJSonListRedeemItems";
	
	public static final int RESPONSE_SUCCESS = 0;
	public static final int RESPONSE_ERROR = -1;
	
	public PointServiceBase(Context c, String method) {
		super(c, NAME_SPACE, method, Utils.getConnectionTimeOut(c));
	}

	public Result toResultObject(String json){
		Gson gson = new Gson();
		Type type = new TypeToken<Result>(){}.getType();
		Result result = gson.fromJson(json, type);
		return result;
	}
	
	public MemberInfo toMemberInfoObject(String json){
		Gson gson = new Gson();
		Type type = new TypeToken<MemberInfo>(){}.getType();
		MemberInfo member = gson.fromJson(json, type);
		return member;
	}
	
	public static class Result{
		private String szRequestID;
		private int iResultID;
		private String szResultData;
		private int iExtraInteger;
		private String szExtraString;
		public String getSzRequestID() {
			return szRequestID;
		}
		public void setSzRequestID(String szRequestID) {
			this.szRequestID = szRequestID;
		}
		public int getiResultID() {
			return iResultID;
		}
		public void setiResultID(int iResultID) {
			this.iResultID = iResultID;
		}
		public String getSzResultData() {
			return szResultData;
		}
		public void setSzResultData(String szResultData) {
			this.szResultData = szResultData;
		}
		public int getiExtraInteger() {
			return iExtraInteger;
		}
		public void setiExtraInteger(int iExtraInteger) {
			this.iExtraInteger = iExtraInteger;
		}
		public String getSzExtraString() {
			return szExtraString;
		}
		public void setSzExtraString(String szExtraString) {
			this.szExtraString = szExtraString;
		}
	}
	
	public static class MemberInfo{
		private String szCardTagCode;
	    private int iCardStatus;
	    private String szCardRemark;
	    private String szCardNo;
	    private double iCurrentCardPoint;
	    private String szFirstName;
	    private String szLastName;
	    private String szNickName;
	    private String szMobileNo;
	    private String szIDCardNo;
		public String getSzCardTagCode() {
			return szCardTagCode;
		}
		public void setSzCardTagCode(String szCardTagCode) {
			this.szCardTagCode = szCardTagCode;
		}
		public int getiCardStatus() {
			return iCardStatus;
		}
		public void setiCardStatus(int iCardStatus) {
			this.iCardStatus = iCardStatus;
		}
		public String getSzCardRemark() {
			return szCardRemark;
		}
		public void setSzCardRemark(String szCardRemark) {
			this.szCardRemark = szCardRemark;
		}
		public String getSzCardNo() {
			return szCardNo;
		}
		public void setSzCardNo(String szCardNo) {
			this.szCardNo = szCardNo;
		}
		public double getiCurrentCardPoint() {
			return iCurrentCardPoint;
		}
		public void setiCurrentCardPoint(double iCurrentCardPoint) {
			this.iCurrentCardPoint = iCurrentCardPoint;
		}
		public String getSzFirstName() {
			return szFirstName;
		}
		public void setSzFirstName(String szFirstName) {
			this.szFirstName = szFirstName;
		}
		public String getSzLastName() {
			return szLastName;
		}
		public void setSzLastName(String szLastName) {
			this.szLastName = szLastName;
		}
		public String getSzNickName() {
			return szNickName;
		}
		public void setSzNickName(String szNickName) {
			this.szNickName = szNickName;
		}
		public String getSzMobileNo() {
			return szMobileNo;
		}
		public void setSzMobileNo(String szMobileNo) {
			this.szMobileNo = szMobileNo;
		}
		public String getSzIDCardNo() {
			return szIDCardNo;
		}
		public void setSzIDCardNo(String szIDCardNo) {
			this.szIDCardNo = szIDCardNo;
		}
	}
}
