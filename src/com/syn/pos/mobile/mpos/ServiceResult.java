package com.syn.pos.mobile.mpos;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;
import com.j1tth4.mobile.core.util.JSONUtil;
import com.syn.pos.mobile.model.WebServiceResult;

public class ServiceResult {
	public static WebServiceResult getResult(String json){
		Type type = new TypeToken<WebServiceResult>() {}.getType();
		JSONUtil jsonUtil = new JSONUtil();
		return (WebServiceResult) jsonUtil.toObject(type, json);
	}
}
