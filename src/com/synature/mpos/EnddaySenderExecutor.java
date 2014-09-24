package com.synature.mpos;

import java.util.Iterator;
import java.util.List;

import com.synature.mpos.database.table.SessionDetailTable;
import com.synature.util.Logger;

import android.content.Context;
import android.database.SQLException;
import android.text.TextUtils;

public class EnddaySenderExecutor extends EnddayBase{
	
	public EnddaySenderExecutor(Context context, int shopId, 
			int computerId, int staffId, WebServiceWorkingListener listener) {
		super(context, shopId, computerId, staffId, listener);
	}

	public void execute() {
		List<String> sessLst = mSession.listSessionEnddayNotSend();
		final Iterator<String> it = sessLst.iterator();
		while (it.hasNext()) {
			final String sessionDate = it.next();
			final String json = generateEnddaySale(sessionDate);
			if (!TextUtils.isEmpty(json)) {
				new EndDaySaleSender(mContext, mShopId, mComputerId,
						mStaffId, json, new WebServiceWorkingListener() {

							@Override
							public void onError(String mesg) {
								setFailStatus(sessionDate);
								Utils.logServerResponse(mContext, " Send endday fail " + mesg);
								mListener.onError(mesg);
							}

							@Override
							public void onPreExecute() {
								Logger.appendLog(mContext, Utils.LOG_PATH, 
										Utils.LOG_FILE_NAME, "Start send endday " + json);
							}

							@Override
							public void onPostExecute() {
								try {
									setSuccessStatus(sessionDate);
									// log json sale if send to server success
									JSONSaleLogFile.appendEnddaySale(mContext, sessionDate, json);
									if (!it.hasNext()) {
										Logger.appendLog(mContext, Utils.LOG_PATH, 
												Utils.LOG_FILE_NAME, "Send endday successfully");
										mListener.onPostExecute();
									}
								} catch (SQLException e) {
									Logger.appendLog(mContext, Utils.LOG_PATH, Utils.LOG_FILE_NAME,
											" Send endday sale error "
													+ SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL
													+ " : " + e.getMessage());
								}
							}

							@Override
							public void onProgressUpdate(int value) {
							}
					}).execute(Utils.getFullUrl(mContext));
			}
		}
	}

}
