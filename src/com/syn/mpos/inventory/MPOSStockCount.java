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
		docDetailLst = listMaterialQtyLessThanCurrStock(documentId, shopId);
		if (docDetailLst.size() > 0) {
			isSuccess = createAdjReduceFromDailyDoc(shopId, documentId, shopId,
					staffId, docDetailLst);
		}
		docDetailLst = listMaterialQtyMoreThanCurrStock(documentId, shopId);
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
				addDocumentDetail(documentId, shopId, docDetail.getMaterialId(), 
						docDetail.getMaterialQty(), 0, 0, "");
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
				addDocumentDetail(documentId, shopId, docDetail.getMaterialId(), 
						docDetail.getMaterialQty(), 0, 0, "");
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
	private List<Document.DocDetail> listMaterialQtyLessThanCurrStock(int documentId, int shopId){
		List<Document.DocDetail> docDetailLst = 
				new ArrayList<Document.DocDetail>();
		String strSql = "SELECT document_id, shop_id, material_id, " +
				" (material_count_qty - material_qty) * -1 AS material_qty " +
				" FROM docdetail " +
				" WHERE document_id=" + documentId +
				" AND shop_id=" + shopId +
				" AND material_count_qty - material_qty < 0";
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				Document.DocDetail docDetail = new Document.DocDetail();
				docDetail.setDocumentId(cursor.getInt(cursor.getColumnIndex("document_id")));
				docDetail.setShopId(cursor.getInt(cursor.getColumnIndex("shop_id")));
				docDetail.setMaterialId(cursor.getInt(cursor.getColumnIndex("material_id")));
				docDetail.setMaterialQty(cursor.getFloat(cursor.getColumnIndex("material_qty")));
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
	private List<Document.DocDetail> listMaterialQtyMoreThanCurrStock(int documentId, int shopId){
		List<Document.DocDetail> docDetailLst = 
				new ArrayList<Document.DocDetail>();
		String strSql = "SELECT document_id, shop_id, material_id," +
				" material_count_qty + material_qty AS material_qty " +
				" FROM docdetail " +
				" WHERE document_id=" + documentId +
				" AND shop_id=" + shopId +
				" AND material_count_qty - material_qty > 0";
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				Document.DocDetail docDetail = new Document.DocDetail();
				docDetail.setDocumentId(cursor.getInt(cursor.getColumnIndex("document_id")));
				docDetail.setShopId(cursor.getInt(cursor.getColumnIndex("shop_id")));
				docDetail.setMaterialId(cursor.getInt(cursor.getColumnIndex("material_id")));
				docDetail.setMaterialQty(cursor.getFloat(cursor.getColumnIndex("material_qty")));
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
			String remark, List<StockMaterial> stockLst) {
		boolean isSuccess = false;
		
		deleteDocumentDetail(documentId, shopId);
		for (StockMaterial stock : stockLst) {
			try {
				addDocumentDetail(documentId, shopId, stock.getId(),
						stock.getCurrQty(), stock.getCountQty(), 0, "");
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

	public List<StockMaterial> listStock(){
		List<StockMaterial> stockLst = 
				new ArrayList<StockMaterial>();
		
		String strSql = " SELECT a.material_qty, b.product_id, " +
				" b.product_code, c.menu_name_0 " +
				" FROM stock_tmp a " +
				" LEFT JOIN products b " +
				" ON a.material_id=b.product_id " +
				" LEFT JOIN menu_item c " +
				" ON b.product_id=c.product_id " +
				" WHERE b.activate=1 " +
				" AND c.menu_activate=1";
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				StockMaterial mat = new StockMaterial(
						cursor.getInt(cursor.getColumnIndex("product_id")),
						cursor.getString(cursor.getColumnIndex("product_code")),
						cursor.getString(cursor.getColumnIndex("menu_name_0")),
						cursor.getFloat(cursor.getColumnIndex("material_qty")), 
						cursor.getFloat(cursor.getColumnIndex("material_qty")));
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
	public List<StockMaterial> listStock(int documentId, int shopId) {
		List<StockMaterial> stockLst = 
				new ArrayList<StockMaterial>();
		
		String strSql = "SELECT b.material_id, c.product_code, " +
				" d.menu_name_0, e.material_qty, b.material_count_qty " +
				" FROM document a " +
				" LEFT JOIN docdetail b " +
				" ON a.document_id=b.document_id " +
				" AND a.shop_id=b.shop_id " +
				" LEFT JOIN products c " +
				" ON b.material_id=c.product_id " +
				" LEFT JOIN menu_item d " +
				" ON c.product_id=d.product_id " +
				" LEFT JOIN stock_tmp e " +
				" ON b.material_id=e.material_id " +
				" WHERE a.document_id=" + documentId +
				" AND a.shop_id=" + shopId +
				" AND c.activate=1 " +
				" AND d.menu_activate=1";
		
		mDbHelper.open();
		Cursor cursor = mDbHelper.rawQuery(strSql);
		if(cursor.moveToFirst()){
			do{
				StockMaterial mat = new StockMaterial(
						cursor.getInt(cursor.getColumnIndex("material_id")),
						cursor.getString(cursor.getColumnIndex("product_code")),
						cursor.getString(cursor.getColumnIndex("menu_name_0")),
						cursor.getFloat(cursor.getColumnIndex("material_qty")),
						cursor.getFloat(cursor.getColumnIndex("material_count_qty"))
						);
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
					" (SELECT SUM(b.material_qty * c.movement_in_stock) " +
					" FROM document a " +
					" LEFT JOIN docdetail b " +
					" ON a.document_id=b.document_id " +
					" AND a.shop_id=b.shop_id " +
					" LEFT JOIN document_type c " +
					" ON a.document_type_id=c.document_type_id " +
					" WHERE p.product_id=b.material_id " +
					" AND a.document_date >=" + dateFrom + 
					" AND a.document_date <=" + dateTo + 
					" AND a.document_status=2 " +
					" AND c.movement_in_stock != 0 " +
					" GROUP BY b.material_id ) " +
					" FROM products p " +
					" WHERE p.activate=1";
			
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
				" material_id  INTEGER NOT NULL, " +
				" material_qty REAL DEFAULT 0);";
		
		mDbHelper.open();
		isSuccess = mDbHelper.execSQL("DROP TABLE IF EXISTS stock_tmp");
		isSuccess = mDbHelper.execSQL(strSql);
		mDbHelper.close();
		return isSuccess;
	}
}
