package com.syn.mpos;

public abstract class PrinterUtility {
	
	public static final int HORIZONTAL_MAX_SPACE = 45;
	public static final int QTY_MAX_SPACE = 12;
	
	protected static String createHorizontalSpace(int usedSpace){
		StringBuilder space = new StringBuilder();
		if(usedSpace > HORIZONTAL_MAX_SPACE){
			usedSpace = usedSpace - 2;
		}
		for(int i = usedSpace; i <= HORIZONTAL_MAX_SPACE; i++){
			space.append(" ");
		}
		return space.toString();
	}
	
	protected static String createQtySpace(int usedSpace){
		StringBuilder space = new StringBuilder();
		if(usedSpace > QTY_MAX_SPACE){
			usedSpace = usedSpace - 2;
		}
		for(int i = usedSpace; i <= QTY_MAX_SPACE; i++){
			space.append(" ");
		}
		return space.toString();
	}
}
