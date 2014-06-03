package com.syn.mpos.dao;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.dao.Products.Product;
import com.syn.mpos.dao.Products.ProductComponentGroup;
import com.synature.pos.OrderTransaction;

public class MPOSOrderTransaction extends OrderTransaction{
	
	/**
	 * @author j1tth4
	 * for display order with order set detail
	 */
	public static class MPOSOrderDetail extends OrderDetail{
		private List<OrderSet.OrderSetDetail> orderSetDetailLst;
		private double vatExclude;
		private int productTypeId;

		public List<OrderSet.OrderSetDetail> getOrderSetDetailLst() {
			return orderSetDetailLst;
		}

		public void setOrderSetDetailLst(List<OrderSet.OrderSetDetail> orderSetDetailLst) {
			this.orderSetDetailLst = orderSetDetailLst;
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
		public List<OrderSetDetail> mProductLst = 
				new ArrayList<OrderSetDetail>();
		
		private int transactionId;
		private int orderDetailId;
		
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
