package com.syn.mpos.database;

import java.util.ArrayList;
import java.util.List;

import com.syn.mpos.database.ProductsDataSource.Product;
import com.syn.mpos.database.ProductsDataSource.ProductComponentGroup;
import com.syn.pos.OrderTransaction;

public class MPOSOrderTransaction extends OrderTransaction{
	
	public static class MPOSOrderDetail extends OrderDetail{
		private double vatExclude;

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
