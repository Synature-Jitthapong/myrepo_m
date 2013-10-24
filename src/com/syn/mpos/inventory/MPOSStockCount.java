package com.syn.mpos.inventory;

import java.util.ArrayList;
import java.util.List;

import com.syn.pos.inventory.Document;

import android.content.Context;
import android.database.Cursor;

public class MPOSStockCount extends MPOSStockDocument {

	public MPOSStockCount(Context context) {
		super(context);
	}
	
	/**
	 * constructor for create summary stock
	 * @param context
	 * @param dateFrom
	 * @param dateTo
	 */
	public MPOSStockCount(Context context, long dateFrom, long dateTo){
		super(context);
		calculateStock(dateFrom, dateTo);
	}

	/**
	 * 
	 * @param documentId
	 * @param shopId
	 * @param staffId
	 * @param remark
	 * @return
	 */
	public boolean confirmStock(int documentId, int shopId, int staffId, String remark){
		boolean isSuccess = false;
		isSuccess = approveDocument(documentId, shopId, staffId, remark);

		List<Document.DocDetail> docDetailLst;
		docDetailLst = listProductQtyLessThanCurrStock(documentId, shopId);
		if (docDetailLst.size() > 0) {
			isSuccess = createAdjReduceFromDailyDoc(shopId, documentId, shopId,
					staffId, docDetailLst);
		}
		docDetailLst = listProductQtyMoreThanCurrStock(documentId, shopId);
		if (docDetailLst.size() > 0) {
			isSuccess = createAdjAddFromDailyDoc(shopId, documentId, shopId,
					staffId, docDetailLst);
		}
		return isSuccess;
	}
	
	/**
	 * 
	 * @param shopId
	 * @param refDocumentId
	 * @param refShopId
	 * @param staffId
	 * @param docDetailLst
	 * @return
	 */
	private boolean createAdjReduceFromDailyDoc(int shopId, int refDocumentId,
			int refShopId, int staffId, List<Document.DocDetail> docDetailLst) {
		boolean isSuccess = false;
		int documentId = createDocument(shopId, refDocumentId, refShopId,
				super.DAILY_REDUCE_DOC, staffId);
		if(documentId > 0){
			for(Document.DocDetail docDetail : docDetailLst){
				float productQty = docDetail.getProductQty() < 0 ? 
						docDetail.getProductQty() * -1 : docDetail.getProductQty();
				
				addDocumentDetail(documentId, shopId, docDetail.getProductId(), 
						productQty, 0, 0, "");
				isSuccess = true;
			}
			isSuccess = approveDocument(documentId, shopId, staffId, "adjust from daily count");
		}
		return isSuccess;
	}
	
	/**
	 * 
	 * @param shopId
	 * @param refDocumentId
	 * @param refShopId
	 * @param staffId
	 * @param docDetailLst
	 * @return
	 */
	private boolean createAdjAddFromDailyDoc(int shopId, int refDocumentId,
			int refShopId, int staffId, List<Document.DocDetail> docDetailLst) {
		boolean isSuccess = false;
		int documentId = createDocument(shopId, refDocumentId, refShopId,
				super.DAILY_ADD_DOC, staffId);
		if(documentId > 0){
			for(Document.DocDetail docDetail : docDetailLst){
				addDocumentDetail(documentId, shopId, docDetail.getProductId(), 
						docDetail.getProductQty(), 0, 0, "");
				isSuccess = true;
			}
			isSuccess = approveDocument(documentId, shopId, staffId, "adjust from daily count");
		}
		return isSuccess;
	}
	
	/**
	 * 
	 * @param documentId
	 * @param shopId
	 * @return
	 */
	private List<Document.DocDetail> listProductQtyLessThanCurrStock(int documentId, int shopId){
		List<Document.DocDetail> docDetailLst = 
				new ArrayList<Document.DocDetail>();
		String strSql = "SELECT document_id, " +
				" shop_id, " +
				" product_id, " +
				" product_qty - product_balance AS product_qty " +
				" FROM docdetail " +
				" WHERE document_id=" + documentId +
				" AND shop_id=" + shopId +
				" AND product_qty - product_balance < 0";
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				Document.DocDetail docDetail = new Document.DocDetail();
				docDetail.setDocumentId(cursor.getInt(cursor.getColumnIndex("document_id")));
				docDetail.setShopId(cursor.getInt(cursor.getColumnIndex("shop_id")));
				docDetail.setProductId(cursor.getInt(cursor.getColumnIndex("product_id")));
				docDetail.setProductQty(cursor.getFloat(cursor.getColumnIndex("product_qty")));
				docDetailLst.add(docDetail);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mDbHelper.close();
		return docDetailLst;
	}
	
	/**
	 * 
	 * @param documentId
	 * @param shopId
	 * @return
	 */
	private List<Document.DocDetail> listProductQtyMoreThanCurrStock(int documentId, int shopId){
		List<Document.DocDetail> docDetailLst = 
				new ArrayList<Document.DocDetail>();
		String strSql = "SELECT document_id, " +
				" shop_id, " +
				" product_id," +
				" product_qty - product_balance AS product_qty " +
				" FROM docdetail " +
				" WHERE document_id=" + documentId +
				" AND shop_id=" + shopId +
				" AND product_qty - product_balance > 0";
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				Document.DocDetail docDetail = new Document.DocDetail();
				docDetail.setDocumentId(cursor.getInt(cursor.getColumnIndex("document_id")));
				docDetail.setShopId(cursor.getInt(cursor.getColumnIndex("shop_id")));
				docDetail.setProductId(cursor.getInt(cursor.getColumnIndex("product_id")));
				docDetail.setProductQty(cursor.getFloat(cursor.getColumnIndex("product_qty")));
				docDetailLst.add(docDetail);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mDbHelper.close();
		return docDetailLst;
	}
	
	/**
	 * 
	 * @param documentId
	 * @param shopId
	 * @param stockLst
	 * @return
	 */
	public boolean saveStock(int documentId, int shopId, int staffId,
			String remark, List<StockProduct> stockLst) {
		boolean isSuccess = false;
		
		deleteDocumentDetail(documentId, shopId);
		for (StockProduct stock : stockLst) {
			float countQty = stock.getCurrQty() > 0 ? stock.getCurrQty() : 0;
			try {
				addDocumentDetail(documentId, shopId, stock.getProId(),
						countQty, stock.getCurrQty(), 0, "");
				isSuccess = true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(isSuccess){
			isSuccess = saveDocument(documentId, shopId, staffId, remark);
		}
		return isSuccess;
	}

	public List<StockProduct> listStock(){
		List<StockProduct> stockLst = 
				new ArrayList<StockProduct>();
		
		String strSql = " SELECT a.id, " +
				" a.qty, " +
				" b.product_id, " +
				" b.product_code, " +
				" b.product_name " +
				" FROM stock_tmp a " +
				" LEFT JOIN products b " +
				" ON a.id=b.product_id " +
				" WHERE b.activated=1 ";
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				StockProduct mat = new StockProduct();
				mat.setProId(cursor.getInt(cursor.getColumnIndex("product_id")));
				mat.setCode(cursor.getString(cursor.getColumnIndex("product_code")));
				mat.setName(cursor.getString(cursor.getColumnIndex("product_name")));
				mat.setCurrQty(cursor.getFloat(cursor.getColumnIndex("qty"))); 
				mat.setCountQty(cursor.getFloat(cursor.getColumnIndex("qty")));
				stockLst.add(mat);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mDbHelper.close();
		
		return stockLst;
	}
	
	/**
	 * 
	 * @param documentId
	 * @param shopId
	 * @return
	 */
	public List<StockProduct> listStock(int documentId, int shopId) {
		List<StockProduct> stockLst = 
				new ArrayList<StockProduct>();
		
		String strSql = "SELECT b.docdetail_id, " +
				" b.product_id, " +
				" c.product_code, " +
				" c.product_name, " +
				" b.product_qty AS countQty, " +
				" d.qty AS currQty " +
				" FROM document a " +
				" LEFT JOIN docdetail b " +
				" ON a.document_id=b.document_id " +
				" AND a.shop_id=b.shop_id " +
				" LEFT JOIN products c " +
				" ON b.product_id=c.product_id " +
				" LEFT JOIN stock_tmp d " +
				" ON b.product_id=d.id " +
				" WHERE a.document_id=" + documentId +
				" AND a.shop_id=" + shopId +
				" AND c.activated=1 ";
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				StockProduct mat = new StockProduct();
				mat.setId(cursor.getInt(cursor.getColumnIndex("docdetail_id")));
				mat.setProId(cursor.getInt(cursor.getColumnIndex("product_id")));
				mat.setCode(cursor.getString(cursor.getColumnIndex("product_code")));
				mat.setName(cursor.getString(cursor.getColumnIndex("product_name")));
				mat.setCurrQty(cursor.getFloat(cursor.getColumnIndex("currQty")));
				mat.setCountQty(cursor.getFloat(cursor.getColumnIndex("countQty")));
				stockLst.add(mat);
			}while(cursor.moveToNext());
		}
		cursor.close();
		mDbHelper.close();
		return stockLst;
	}
	
	/**
	 * calculate current stock
	 * @param dateFrom
	 * @param dateTo
	 * @return
	 */
	protected boolean calculateStock(long dateFrom, long dateTo){
		boolean isSuccess = false;
		if(createStockTmp()){
			String strSql = "INSERT INTO stock_tmp " +
					" SELECT p.product_id, " +
					" (SELECT SUM(b.product_qty * c.movement_in_stock) " +
					" FROM document a " +
					" LEFT JOIN docdetail b " +
					" ON a.document_id=b.document_id " +
					" AND a.shop_id=b.shop_id " +
					" LEFT JOIN document_type c " +
					" ON a.document_type_id=c.document_type_id " +
					" WHERE p.product_id=b.product_id " +
					" AND a.document_date >=" + dateFrom + 
					" AND a.document_date <=" + dateTo + 
					" AND a.document_status=2 " +
					" AND c.movement_in_stock != 0 " +
					" GROUP BY b.product_id ) " +
					" FROM products p " +
					" WHERE p.activated=1";
			
			mDbHelper.open();
			isSuccess = mDbHelper.execSQL(strSql);
			mDbHelper.close();
		}
		return isSuccess;
	}

	/**
	 * create stock temp table
	 * @return
	 */
	protected boolean createStockTmp(){
		boolean isSuccess = false;
		String strSql = " CREATE TABLE stock_tmp ( " +
				" id  INTEGER, " +
				" qty REAL DEFAULT 0);";
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL("DROP TABLE IF EXISTS stock_tmp");
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		return isSuccess;
	}
}
