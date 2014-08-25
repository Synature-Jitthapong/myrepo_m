package com.synature.mpos.database.model;

public class Comment{
	private int commentId;
	private String commentName;
	private double commentQty;
	private double commentPrice;
	private boolean isSelected;
	
	public double getCommentQty() {
		return commentQty;
	}
	public void setCommentQty(double commentQty) {
		this.commentQty = commentQty;
	}
	public double getCommentPrice() {
		return commentPrice;
	}
	public void setCommentPrice(double commentPrice) {
		this.commentPrice = commentPrice;
	}
	public boolean isSelected() {
		return isSelected;
	}
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	public int getCommentId() {
		return commentId;
	}
	public void setCommentId(int commentId) {
		this.commentId = commentId;
	}
	public String getCommentName() {
		return commentName;
	}
	public void setCommentName(String commentName) {
		this.commentName = commentName;
	}
}
