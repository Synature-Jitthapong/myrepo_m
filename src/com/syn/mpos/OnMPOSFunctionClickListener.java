package com.syn.mpos;

import android.view.View;

public interface OnMPOSFunctionClickListener {
	void onHoldBillClick(final View v);

	void onInventoryClick(final View v);

	void onReportClick(final View v);

	void onUtilityClick(final View v);

	void onSwitchUserClick(final View v);

	void onLogoutClick(final View v);
}
