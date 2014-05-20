package com.syn.mpos;

public abstract class PrinterUtility {
	public static final int HORIZONTAL_MAX_SPACE = 45;
	
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
}
