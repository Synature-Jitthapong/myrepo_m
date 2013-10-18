package com.syn.mpos.database;

public class MPOSSql {
	public static final String TB_CONN_SETTING = 
			" CREATE TABLE conn_setting (" +
			" address TEXT, " +
			" backoffice TEXT " +
			"); ";

	public static final String TB_SYNC_ITEM = 
			" CREATE TABLE sync_item (" +
			" sync_item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			" sync_enable INTEGER DEFAULT 1, " +
			" sync_item_name TEXT, " +
			" sync_time TEXT, " +
			" sync_already INTEGER DEFAULT 0 " +
			");";
	
	public static final String TB_PRINTER_SETTING = 
			" CREATE TABLE printer_setting (" +
			" printer_ip TEXT " +
			");";
	
	public static final String TB_SHOP = 
			" CREATE TABLE shop ( " +
			" shop_id INTEGER, " +
			" shop_code TEXT, " +
			" shop_name TEXT, " +
			" shop_type INTEGER, " +
			" fast_food_type INTEGER, " +
			" table_type INTEGER, " +
			" vat_type INTEGER, " +
			" service_charge REAL, " +
			" service_charge_type INTEGER, " +
			" open_hour TEXT, " +
			" close_hour TEXT, " +
			" calculate_service_charge_when_freebill INTEGER, " +
			" company_name TEXT, " +
			" company_address_1 TEXT, " +
			" company_address_2 TEXT, " +
			" company_city TEXT, " +
			" company_province INTEGER, " +
			" company_zip_code TEXT, " +
			" company_tel TEXT, " +
			" company_fax TEXT, " +
			" company_tax_id TEXT, " +
			" company_register_id TEXT, " +
			" company_vat REAL " +
			" );";
	
	public static final String TB_COMPUTER = 
			" CREATE TABLE computer ( " +
			" computer_id INTEGER PRIMARY KEY, " +
			" computer_name TEXT, " +
			" device_code TEXT, " +
			" registration_number TEXT" +
			");";

	public static final String TB_PROGRAM_FEATURE = 
			" CREATE TABLE program_feature ( " +
			" feature_id INTEGER PRIMARY KEY, " +
			" feature_name TEXT, " +
			" feature_value INTEGER DEFAULT 1, " +
			" feature_text TEXT, " +
			" feature_desc TEXT " +
			");";
	
	public static final String TB_STAFF_PERMISS = 
			" CREATE TABLE staff_permission ( " +
			" staff_role_id INTEGER DEFAULT 0, " +
			" permission_item_id INTEGER DEFAULT 0 " +
			" );";
	
	public static final String TB_STAFF = 
			" CREATE TABLE staffs ( " +
			" staff_id INTEGER PRIMARY KEY, " +
			" staff_code TEXT, " +
			" staff_name TEXT, " +
			" staff_password TEXT " +
			");";
	
	public static final String TB_PROPERTY =
			" CREATE TABLE property ( " + 
			" currency_symbol TEXT DEFAULT '$', " +
			" currency_code TEXT DEFAULT 'USD', " +
			" currency_name TEXT, " +
			" currency_format TEXT DEFAULT '#,##0.##', " +
			" qty_format TEXT DEFAULT '#,##0.##', " +
			" date_format TEXT DEFAULT 'yyyy/MM/dd', " +
			" time_format TEXT DEFAULT 'HH:mm:ss' " +
			" );";
	
	public static final String TB_PROVINCE =
			" CREATE TABLE provinces ( " +
			" province_id INTEGER PRIMARY KEY, " +
			" province_name TEXT" +
			");";
	
	public static final String TB_LANG = 
			" CREATE TABLE language ( " +
			" lang_id INTEGER PRIMARY KEY DEFAULT 1, " +
			" lang_name TEXT, " +
			" lang_code TEXT DEFAULT 'en', " +
			" is_default INTEGER DEFAULT 0" +
			");";

	public static final String TB_BANK = 
			" CREATE TABLE bank_name ( " +
			" bank_name_id INTEGER PRIMARY KEY, " +
			" bank_name TEXT" +
			");";
	
	public static final String TB_CREDIT = 
			" CREATE TABLE creditcard_type ( " +
			" creditcard_type_id INTEGER PRIMARY KEY, " +
			" creditcard_type_name TEXT" +
			");";
	
	public static final String TB_PAY_TYPE = 
			" CREATE TABLE pay_type ( " + 
			" pay_type_id INTEGER PRIMARY KEY, " +
			" pay_type_code TEXT, " +
			" pay_type_name TEXT" +
			");";
	
	public static final String TB_PAY_DETAIL = 
			" CREATE TABLE payment_detail ( " +
			" pay_detail_id INTEGER, " +
			" transaction_id INTEGER, " +
			" computer_id INTEGER, " +
			" pay_type_id INTEGER DEFAULT 1, " +
			" pay_amount REAL DEFAULT 0, " +
			" credit_card_no TEXT, " +
			" expire_month INTEGER, " +
			" expire_year INTEGER, " +
			" bank_id INTEGER, " +
			" credit_card_type INTEGER, " +
			" remark TEXT, " +
			" PRIMARY KEY (pay_detail_id, transaction_id, computer_id) " +
			");";
	
	public static final String TB_SESSION = 
			" CREATE TABLE session ( " +
			" session_id INTEGER, " +
			" computer_id INTEGER, " +
			" shop_id INTEGER, " +
			" open_staff_id INTEGER, " +
			" close_staff_id INTEGER, " +
			" session_date TEXT, " +
			" open_date_time TEXT, " +
			" close_date_time TEXT, " +
			" open_amount REAL, " +
			" close_amount REAL, " +
			" is_endday INTEGER, " +
			" PRIMARY KEY (session_id, computer_id) " +
			");";
	
	public static final String TB_SESSION_END =
			" CREATE TABLE session_end_day ( " +
			" session_date TEXT PRIMARY KEY, " +
			" endday_date_time TEXT, " +
			" total_qty_receipt INTEGER, " +
			" total_amount_receipt REAL, " +
			" is_send_to_hq INTEGER, " +
			" send_to_hq_date_time TEXT" +
			");";
	
	public static final String TB_PRO_GROUP = 
			" CREATE TABLE product_group ( " +
			" product_group_id INTEGER PRIMARY KEY, " +
			" product_group_code TEXT, " +
			" product_group_name TEXT, " +
			" product_group_type INTEGER DEFAULT 0, " +
			" is_comment INTEGER DEFAULT 0, " +
			" create_from_device INTEGER DEFAULT 0, " +
			" product_group_ordering INTEGER DEFAULT 0" +
			");";
	
	public static final String TB_PRO_DEPT = 
			" CREATE TABLE product_dept ( " +
			" product_dept_id INTEGER PRIMARY KEY, " +
			" product_group_id INTEGER, " +
			" product_dept_code TEXT, " +
			" product_dept_name TEXT, " +
			" create_from_device INTEGER DEFAULT 0, " +
			" product_dept_ordering INTEGER DEFAULT 0" +
			");";

	public static final String TB_PRODUCT = 
			" CREATE TABLE products ( " +
			" product_id INTEGER PRIMARY KEY, " +
			" product_dept_id INTEGER, " +
			" product_group_id INTEGER, " +
			" product_code TEXT, " +
			" product_barcode TEXT, " +
			" product_name TEXT, " +
			" product_desc TEXT, " +
			" product_type_id INTEGER DEFAULT 0, " +
			" product_price REAL DEFAULT 0, " +
			" product_unit_name TEXT, " +
			" discount_allow INTEGER DEFAULT 1, " +
			" vat_type INTEGER DEFAULT 1, " +
			" vat_rate REAL DEFAULT 0, " +
			" has_service_charge INTEGER DEFAULT 0, " +
			" activated INTEGER DEFAULT 1, " +
			" is_out_of_stock INTEGER DEFAULT 0, " +
			" create_from_device INTEGER DEFAULT 0, " +
			" pic_name TEXT," +
			" product_ordering INTEGER " +
			");";

	public static final String TB_MEMBER_GROUP = 
			" CREATE TABLE member_group ( " +
			" member_group_id INTEGER, " +
			" shop_id INTEGER, " +
			" member_group_code TEXT, " +
			" member_group_name TEXT, " +
			" PRIMARY KEY (member_group_id, shop_id) " +
			"); ";
	
	public static final String TB_MEMBER = 
			" CREATE TABLE members ( " +
			" member_id INTEGER, " +
			" shop_id INTEGER, " +
			" member_group_id INTEGER, " +
			" member_code TEXT, " +
			" member_gender INTEGER, " +
			" member_firstname TEXT, " +
			" member_lastname TEXT, " +
			" member_address1 TEXT, " +
			" member_address2 TEXT, " +
			" member_city TEXT, " +
			" member_province INTEGER, " +
			" member_zipcode TEXT, " +
			" member_tel TEXT, " +
			" member_mobile TEXT, " +
			" member_fax TEXT, " +
			" member_email TEXT, " +
			" member_birthday INTEGER, " +
			" member_expiredate INTEGER, " +
			" update_by INTEGER, " +
			" update_date INTEGER, " +
			" insert_at_shop_id INTEGER, " +
			" remark TEXT, " +
			" PRIMARY KEY (member_id, shop_id) " +
			");";

	public static final String TB_TRANS = 
			" CREATE TABLE order_transaction ( " +
			" transaction_id INTEGER, " +
			" computer_id INTEGER, " +
			" shop_id INTEGER, " +
			" open_time TEXT, " +
			" open_staff_id INTEGER, " +
			" paid_time TEXT, " +
			" paid_staff_id INTEGER, " +
			" close_time TEXT, " +
			" transaction_status_id INTEGER DEFAULT 1, " +
			" document_type_id INTEGER, " +
			" receipt_year INTEGER, " +
			" receipt_month INTEGER, " +
			" receipt_id INTEGER, " +
			" sale_date TEXT, " +
			" session_id INTEGER, " +
			" void_staff_id INTEGER, " +
			" void_reason TEXT, " +
			" void_time TEXT, " +
			" member_id INTEGER, " +
			" transaction_vat REAL DEFAULT 0, " +
			" transaction_exclude_vat REAL DEFAULT 0, " +
			" transaction_vatable REAL DEFAULT 0, " +
			" service_charge REAL DEFAULT 0, " +
			" service_charge_vat REAL DEFAULT 0, " +
			" remark TEXT, " +
			" transaction_note TEXT, " +
			" PRIMARY KEY (transaction_id, computer_id) " +
			");";

	public static final String TB_ORDER = 
			" CREATE TABLE order_detail ( " +
			" order_detail_id INTEGER, " +
			" transaction_id INTEGER, " +
			" computer_id INTEGER, " +
			" product_id INTEGER, " +
			" product_type_id INTEGER DEFAULT 1, " +
			" order_qty REAL DEFAULT 1, " +
			" unit_price REAL DEFAULT 0, " +
			" total_retail_price REAL DEFAULT 0, " +
			" total_sale_price REAL DEFAULT 0, " +
			" vat_type INTEGER DEFAULT 1, " +
			" vat REAL DEFAULT 0, " +
			" member_discount REAL DEFAULT 0, " +
			" price_discount REAL DEFAULT 0, " +
			" discount_type INTEGER DEFAULT 1, " +
			" PRIMARY KEY (order_detail_id, transaction_id, computer_id) " +
			");";
	
	public static final String TB_ORDER_TMP = 
			" CREATE TABLE order_detail_tmp ( " +
			" order_detail_id INTEGER, " +
			" transaction_id INTEGER, " +
			" computer_id INTEGER, " +
			" product_id INTEGER, " +
			" product_type_id INTEGER DEFAULT 1, " +
			" order_qty REAL DEFAULT 1, " +
			" unit_price REAL DEFAULT 0, " +
			" total_retail_price REAL DEFAULT 0, " +
			" total_sale_price REAL DEFAULT 0, " +
			" vat_type INTEGER DEFAULT 1, " +
			" vat REAL DEFAULT 0, " +
			" member_discount REAL DEFAULT 0, " +
			" price_discount REAL DEFAULT 0, " +
			" discount_type INTEGER DEFAULT 1, " +
			" PRIMARY KEY (order_detail_id, transaction_id, computer_id) " +
			");";
	
	public static final String TB_DOC_TYPE_GROUP = 
			" CREATE TABLE document_type_group ( " +
			" document_type_group_id INTEGER, " +
			" group_header TEXT, " +
			" group_ordering INTEGER" +
			" );";
	
	public static final String TB_DOC_TYPE_GROUP_VAL = 
			" CREATE TABLE document_type_group_value ( " +
			" doc_type_group_id INTEGER, " +
			" document_type_id INTEGER" +
			" );";
	
	public static final String TB_DOC_TYPE = 
			" CREATE TABLE document_type ( " +
			" document_type_id INTEGER, " +
			" document_type_header TEXT, " +
			" document_type_name TEXT, " +
			" movement_in_stock INTEGER " +
			" );";
	
	public static final String TB_DOC = 
			" CREATE TABLE document ( " +
			" document_id INTEGER, " +
			" shop_id INTEGER, " +
			" ref_document_id INTEGER, " +
			" ref_shop_id INTEGER, " +
			" document_type_id INTEGER, " +
			" document_no TEXT, " +
			" document_date TEXT, " +
			" document_year INTEGER, " +
			" document_month INTEGER, " +
			" update_by INTEGER, " +
			" update_date TEXT, " +
			" document_status INTEGER DEFAULT 1, " +
			" remark TEXT, " +
			" is_send_to_hq INTEGER DEFAULT 0, " +
			" send_to_hq_datetime TEXT, " +
			" PRIMARY KEY (document_id, shop_id) " +
			");";

	public static final String TB_DOC_DETAIL = 
			" CREATE TABLE docdetail ( " +
			" docdetail_id  INTEGER, " +
			" document_id  INTEGER, " +
			" shop_id  INTEGER, " +
			" material_id  INTEGER, " +
			" unit_name  TEXT, " +
			" material_qty  REAL DEFAULT 0, " +
			" material_balance  REAL DEFAULT 0, " +
			" material_price_per_unit  REAL DEFAULT 0, " +
			" material_net_price  REAL DEFAULT 0, " +
			" material_tax_type  INTEGER DEFAULT 1, " +
			" material_tax_price  REAL DEFAULT 0, " +
			" PRIMARY KEY (docdetail_id, document_id, shop_id) " +
			");";
	
	/**
	 * Script additional data for testing
	 */
	public static final String[] BANK_SQL = {
		" INSERT INTO `bank_name` VALUES (1, 'กรุงเทพฯ'); ",
		" INSERT INTO `bank_name` VALUES (2, 'กสิกรไทย'); ",
		" INSERT INTO `bank_name` VALUES (3, 'ทหารไทย'); ",
		" INSERT INTO `bank_name` VALUES (4, 'กรุงไทย'); ",
		" INSERT INTO `bank_name` VALUES (5, 'ไทยพาณิชย์'); ",
		" INSERT INTO `bank_name` VALUES (6, 'UOB'); ",
		" INSERT INTO `bank_name` VALUES (7, 'ซิตี้แบงค์'); ",
		" INSERT INTO `bank_name` VALUES (8, 'กรุงศรีอยุธยา'); ",
		" INSERT INTO `bank_name` VALUES (9, 'นครหลวงไทย'); ",
		" INSERT INTO `bank_name` VALUES (10, 'Standard Charter'); ",
		" INSERT INTO `bank_name` VALUES (11, 'HSBC'); ",
		" INSERT INTO `bank_name` VALUES (12, 'ออมสิน'); ",
		" INSERT INTO `bank_name` VALUES (13, 'อิออน'); ",
		" INSERT INTO `bank_name` VALUES (14, 'AIG'); ",
		" INSERT INTO `bank_name` VALUES (15, 'GE Money'); ",
		" INSERT INTO `bank_name` VALUES (16, 'แคปปิตอล โอเค'); ",
		" INSERT INTO `bank_name` VALUES (17, 'BMB'); ",
		" INSERT INTO `bank_name` VALUES (18, 'ASA'); ",
		" INSERT INTO `bank_name` VALUES (19, 'PSCL'); ",
		" INSERT INTO `bank_name` VALUES (20, 'อื่น ๆ'); ",
		" INSERT INTO `bank_name` VALUES (21, 'TDB'); "
	};
	
	public static final String[] CARD_SQL = {
		" INSERT INTO `creditcard_type` VALUES (1, 'VISA'); ",
		" INSERT INTO `creditcard_type` VALUES (2, 'Master'); ",
		" INSERT INTO `creditcard_type` VALUES (3, 'AMEX'); ",
		" INSERT INTO `creditcard_type` VALUES (4, 'DINERS'); ",
		" INSERT INTO `creditcard_type` VALUES (5, 'JCB'); ",
		" INSERT INTO `creditcard_type` VALUES (6, 'VS-GOLD'); ",
		" INSERT INTO `creditcard_type` VALUES (7, 'MC-GOLD');"
	};
	
	public static final String[] DOC_TYPE = {
		" INSERT INTO `document_type` VALUES (7, 'xxxST', 'Monthly Stock Card', 0); ",
		" INSERT INTO `document_type` VALUES (10, 'xxxR', 'Transfer Stock', 1); ",
		" INSERT INTO `document_type` VALUES (18, 'xxxDS', 'Add From Daily Stock', 1); ",
		" INSERT INTO `document_type` VALUES (19, 'xxxDS', 'Reduce From Daily Stock', -1); ",
		" INSERT INTO `document_type` VALUES (20, 'xxxSD', 'Material From Sale Document', -1); ",
		" INSERT INTO `document_type` VALUES (21, 'xxxVD', 'Material From Void Document', 1); ",
		" INSERT INTO `document_type` VALUES (22, 'xxxMS', 'Add From Monthly Stock', 1); ",
		" INSERT INTO `document_type` VALUES (23, 'xxxMS', 'Reduce From Monthly Stock', -1); ",
		" INSERT INTO `document_type` VALUES (24, 'xxxST', 'Daily Stock Card', 0); ",
		" INSERT INTO `document_type` VALUES (39, 'xxxRO', 'Direct Receive Order', 1); ",
	};
	
	public static final String[] MG = {
		" INSERT INTO `member_group` VALUES (1, 4, 'MG1', 'Member Group 1'); ",
		" INSERT INTO `member_group` VALUES (2, 4, 'MG2', 'Member Group 2'); ",
		" INSERT INTO `member_group` VALUES (3, 4, 'MG3', 'Member Group 3'); ",
	};
	
	public static final String[] PROVINCE = {
		" INSERT INTO `provinces` VALUES (1, 'Chiang Mai'); ",
		" INSERT INTO `provinces` VALUES (2, 'Chiang Rai'); ",
		" INSERT INTO `provinces` VALUES (3, 'Kamphaeng Phet'); ",
		" INSERT INTO `provinces` VALUES (4, 'Lampang'); ",
		" INSERT INTO `provinces` VALUES (5, 'Lamphun'); "
	};
}
