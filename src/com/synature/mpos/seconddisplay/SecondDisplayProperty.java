package com.synature.mpos.seconddisplay;

import java.util.List;

public class SecondDisplayProperty {
	// --- Second Display Class and Property
	public enum eSecDisplayCmdType {
		Initial(0), 
		DisplayItemData(1), 
		ChangePayment(2), 
		ExtraView(11), 
		QuestionView(12), 
		ClearScreen(99);

		private int value;

		eSecDisplayCmdType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}
	}

	public static class clsSecDisplayInitial {
		public eSecDisplayCmdType iCommandTypeID = eSecDisplayCmdType.ClearScreen; // 0
		public String szShopName;
		public String szStaffName;

	}

	public static class clsSecDisplayItemData {
		public eSecDisplayCmdType iCommandTypeID = eSecDisplayCmdType.DisplayItemData;// 1
		public String szGrandTotalPrice;
		public clsSecDisplay_Transaction xTransaction;
		public List<clsSecDisplay_DetailItem> xListDetailItems;
		public List<clsSecDisplay_TransSummary> xListTransSummarys;
	}

	public static class clsSecDisplay_Transaction {
		public String szTransName;
		public int iNoCustomer;
		public String szCustName;
	}

	public static class clsSecDisplay_DetailItem {
		public String szItemName;
		public String szItemQty;
		public String szItemTotalPrice;
		public String szImageUrl;
	}

	public static class clsSecDisplay_TransSummary {
		public String szSumName;
		public String szSumAmount;
	}

	public static class clsSecDisplay_ChangePayment {
		public eSecDisplayCmdType iCommandTypeID = eSecDisplayCmdType.ChangePayment; // 2
		public String szPayAmount;
		public String szCashChangeAmount;

		// ---------------------------------------
		// --- If szMemberCode has data, then show member point
		// --- If this szMemberCode has not data, then hide member point.
		public String szMemberCode;
		public String szMemberName;
		public String szPrevPoint;
		public String szCurrentPoint;
		public String szBillPoint;
	}

	public static class clsSecDisplay_ExtraView {
		public eSecDisplayCmdType iCommandTypeID = eSecDisplayCmdType.ExtraView; // 11
		public String szExtraViewTitle;
		public int iExtraViewType; // 0=Web View, 1=Member Info, 2=Card
		public int iCloseType; // 0=User Close, 1=Delay Time Close,
		public int iCloseSecDelayTime;
		public clsSecDisplay_Extra_WebView xExtraWebView;
		public clsSecDisplay_Extra_MemberView xExtraMemberView;
		public clsSecDisplay_Extra_CardView xExtraCardView;
	}

	public static class clsSecDisplay_Extra_WebView {
		public String szWebURL;
	}

	public static class clsSecDisplay_Extra_MemberView {
		public String szMemberCode;
		public String szMemberName;
		public String szMemberPhone;
		public String szMemberMobile;
		public String szMemberExpireDate;

		public String szCurrentPoint;
	}

	public static class clsSecDisplay_Extra_CardView {

	}

	public static class clsSecDisplay_QuestionView {
		public eSecDisplayCmdType iCommandTypeID = eSecDisplayCmdType.QuestionView; // 12
		public String szQuestionTitle;

		public List<String> szListQuestions;
	}

	public static class clsSecDisplay_ClearScreen {
		public eSecDisplayCmdType iCommandTypeID = eSecDisplayCmdType.ClearScreen; // 99
	}
}
