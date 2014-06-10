package com.synature.mpos.seconddisplay;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.synature.mpos.dao.Formater;
import com.synature.mpos.dao.MPOSOrderTransaction;
import com.synature.pos.SecondDisplayProperty;
import com.synature.pos.SecondDisplayProperty.clsSecDisplayItemData;
import com.synature.pos.SecondDisplayProperty.clsSecDisplay_DetailItem;
import com.synature.pos.SecondDisplayProperty.clsSecDisplay_TransSummary;

public class SecondDisplayJSON {
	/**
	 * @param format
	 * @param trans
	 * @param orderDetailLst
	 * @param transSummLst
	 * @param grandTotal
	 * @return JSON String
	 */
	public static String genDisplayItem(Formater format, MPOSOrderTransaction trans, 
			List<MPOSOrderTransaction.MPOSOrderDetail> orderDetailLst, 
			List<clsSecDisplay_TransSummary> transSummLst, String grandTotal){
		Gson gson = new Gson();
		clsSecDisplayItemData displayData = 
				new clsSecDisplayItemData();
		List<clsSecDisplay_DetailItem> itemLst = 
				new ArrayList<clsSecDisplay_DetailItem>();
		for(MPOSOrderTransaction.MPOSOrderDetail orderDetail : orderDetailLst){
			clsSecDisplay_DetailItem item = new clsSecDisplay_DetailItem();
			item.szItemName = orderDetail.getProductName();
			item.szItemQty = format.qtyFormat(orderDetail.getQty());
			item.szItemTotalPrice = format.currencyFormat(orderDetail.getTotalRetailPrice());
			item.szImageUrl = "";
			itemLst.add(item);
		}
		displayData.szGrandTotalPrice = grandTotal;
		displayData.xListDetailItems = itemLst;
		displayData.xListTransSummarys = transSummLst;
		return gson.toJson(displayData);
	}
	
	/**
	 * @param shopName
	 * @param staffName
	 * @return JSON String
	 */
	public static String genInitDisplay(String shopName, String staffName){
		Gson gson = new Gson();
		SecondDisplayProperty.clsSecDisplayInitial init 
			= new SecondDisplayProperty.clsSecDisplayInitial();
		init.szShopName = shopName;
		init.szStaffName = staffName;
		return gson.toJson(init);
	}
}
