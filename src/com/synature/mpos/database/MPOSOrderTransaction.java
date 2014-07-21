package com.synature.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.synature.mpos.database.Products.Product;
import com.synature.mpos.database.Products.ProductComponentGroup;
import com.synature.pos.OrderTransaction;

public class MPOSOrderTransaction extends OrderTransaction{
	
	/**
	 * @author j1tth4
	 * for display order with order set detail
	 */
	public static class MPOSOrderDetail extends OrderDetail{
		private List<OrderSet.OrderSetDetail> orderSetDetailLst = 
				new ArrayList<OrderSet.OrderSetDetail>();
		private List<MenuComment.Comment> orderCommentLst =
				new ArrayList<MenuComment.Comment>();
		private double vatExclude;
		private int productTypeId;
		private int priceOrPercent;
		private int promotionPriceGroupId;
		private int promotionTypeId;
		private String promotionName;
		private String orderComment;

		public List<MenuComment.Comment> getOrderCommentLst() {
			return orderCommentLst;
		}

		public void setOrderCommentLst(List<MenuComment.Comment> orderCommentLst) {
			this.orderCommentLst = orderCommentLst;
		}

		public List<OrderSet.OrderSetDetail> getOrderSetDetailLst() {
			return orderSetDetailLst;
		}

		public void setOrderSetDetailLst(List<OrderSet.OrderSetDetail> orderSetDetailLst) {
			this.orderSetDetailLst = orderSetDetailLst;
		}

		public int getPromotionPriceGroupId() {
			return promotionPriceGroupId;
		}

		public void setPromotionPriceGroupId(int promotionPriceGroupId) {
			this.promotionPriceGroupId = promotionPriceGroupId;
		}

		public int getPriceOrPercent() {
			return priceOrPercent;
		}

		public void setPriceOrPercent(int priceOrPercent) {
			this.priceOrPercent = priceOrPercent;
		}

		public int getPromotionTypeId() {
			return promotionTypeId;
		}

		public void setPromotionTypeId(int promotionTypeId) {
			this.promotionTypeId = promotionTypeId;
		}

		public String getPromotionName() {
			return promotionName;
		}

		public void setPromotionName(String promotionName) {
			this.promotionName = promotionName;
		}

		public String getOrderComment() {
			return orderComment;
		}

		public void setOrderComment(String orderComment) {
			this.orderComment = orderComment;
		}

		public int getProductTypeId() {
			return productTypeId;
		}

		public void setProductTypeId(int productTypeId) {
			this.productTypeId = productTypeId;
		}

		public double getVatExclude() {
			return vatExclude;
		}

		public void setVatExclude(double vatExclude) {
			this.vatExclude = vatExclude;
		}
	}
	
	/**
	 * @author j1tth4
	 * for display orderset 
	 */
	public static class OrderSet extends ProductComponentGroup{
		private List<OrderSetDetail> orderSetDetailLst = 
				new ArrayList<OrderSetDetail>();
		
		private int transactionId;
		private int orderDetailId;
		
		public List<OrderSetDetail> getOrderSetDetailLst() {
			return orderSetDetailLst;
		}
		public int getTransactionId() {
			return transactionId;
		}
		public void setTransactionId(int transactionId) {
			this.transactionId = transactionId;
		}
		public int getOrderDetailId() {
			return orderDetailId;
		}
		public void setOrderDetailId(int orderDetailId) {
			this.orderDetailId = orderDetailId;
		}
		
		/**
		 * @author j1tth4
		 * for display order set
		 */
		public static class OrderSetDetail extends Product{
			private int orderSetId;
			private double orderSetQty;
			
			public double getOrderSetQty() {
				return orderSetQty;
			}

			public void setOrderSetQty(double orderSetQty) {
				this.orderSetQty = orderSetQty;
			}

			public int getOrderSetId() {
				return orderSetId;
			}

			public void setOrderSetId(int orderSetId) {
				this.orderSetId = orderSetId;
			}
		}
	}
}
