package com.syn.mpos.model;

import java.util.List;

public class QueueDisplayInfo {
	public List<QueueInfo> xListQueueInfo;
	private String szCurQueueGroupA;
	private String szCurQueueGroupB;
	private String szCurQueueGroupC;

	public String getSzCurQueueGroupA() {
		return szCurQueueGroupA;
	}

	public void setSzCurQueueGroupA(String szCurQueueGroupA) {
		this.szCurQueueGroupA = szCurQueueGroupA;
	}

	public String getSzCurQueueGroupB() {
		return szCurQueueGroupB;
	}

	public void setSzCurQueueGroupB(String szCurQueueGroupB) {
		this.szCurQueueGroupB = szCurQueueGroupB;
	}

	public String getSzCurQueueGroupC() {
		return szCurQueueGroupC;
	}

	public void setSzCurQueueGroupC(String szCurQueueGroupC) {
		this.szCurQueueGroupC = szCurQueueGroupC;
	}

	public static class QueueInfo extends com.syn.mpos.model.QueueInfo {

	}
}
