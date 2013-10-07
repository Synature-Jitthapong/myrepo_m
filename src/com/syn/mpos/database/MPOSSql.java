package com.syn.mpos.database;

public class MPOSSql {
	public static final String TB_SHOP = 
			" CREATE TABLE shop_property ( " +
			" shop_id  INTEGER, " +
			" shop_code  TEXT, " +
			" shop_name  TEXT, " +
			" shop_type  INTEGER, " +
			" fast_food_type  INTEGER, " +
			" table_type  INTEGER, " +
			" vat_type  INTEGER, " +
			" service_charge  REAL, " +
			" service_charge_type  INTEGER, " +
			" open_hour  TEXT, " +
			" close_hour  TEXT, " +
			" calculate_service_charge_when_freebill  INTEGER, " +
			" company_name  TEXT, " +
			" company_address_1  TEXT, " +
			" company_address_2  TEXT, " +
			" company_city  TEXT, " +
			" company_province  INTEGER, " +
			" company_zip_code  TEXT, " +
			" company_tel  TEXT, " +
			" company_fax  TEXT, " +
			" company_tax_id  TEXT, " +
			" company_register_id  TEXT, " +
			" company_vat  REAL " +
			" );";
	
	public static final String TB_PROGRAM_FEATURE = 
			" CREATE TABLE program_feature ( " +
			" feature_id  INTEGER, " +
			" feature_name  TEXT, " +
			" feature_value  INTEGER NOT NULL DEFAULT 1, " +
			" feature_text  TEXT, " +
			" feature_desc  TEXT, " +
			" PRIMARY KEY (feature_id) " +
			" );";
	
	public static final String TB_STAFF_PERMISS = 
			" CREATE TABLE staff_permission ( " +
			" staff_role_id  INTEGER NOT NULL DEFAULT 0, " +
			" permission_item_id  INTEGER NOT NULL DEFAULT 0 " +
			" );";
	
	public static final String TB_STAFF = 
			" CREATE TABLE staffs ( " +
			" staff_id  INTEGER NOT NULL DEFAULT 0, " +
			" staff_code  TEXT, " +
			" staff_name  TEXT, " +
			" staff_password  TEXT, " +
			" PRIMARY KEY (staff_id) " +
			" );";
	
	public static final String TB_COMP = 
			" CREATE TABLE computer_property ( " +
			" computer_id  INTEGER NOT NULL DEFAULT 0, " +
			" computer_name  TEXT, " +
			" device_code  TEXT, " +
			" registration_number  TEXT, " +
			" PRIMARY KEY (computer_id ASC) " +
			" );";
	
	public static final String TB_GB_PROP =
			" CREATE TABLE global_property ( " + 
			" currency_symbol  TEXT, " +
			" currency_code  TEXT, " +
			" currency_name  TEXT, " +
			" currency_format  TEXT, " +
			" date_format  TEXT, " +
			" time_format  TEXT, " +
			" qty_format  TEXT " +
			" );";
	
	public static final String TB_PROVINCE =
			" CREATE TABLE provinces ( " +
			" province_id  INTEGER NOT NULL, " +
			" province_name  TEXT NOT NULL, " +
			" PRIMARY KEY (province_id) " +
			" );";
	
	public static final String TB_LANG = 
			" CREATE TABLE language ( " +
			" lang_id  INTEGER NOT NULL DEFAULT 1, " +
			" lang_name  TEXT, " +
			" lang_code  TEXT NOT NULL DEFAULT en, " +
			" is_default  INTEGER NOT NULL DEFAULT 0, " +
			" PRIMARY KEY (lang_id) " +
			" );";

	
	public static final String TB_BANK = 
			" CREATE TABLE bank_name ( " +
			" bank_name_id  INTEGER NOT NULL, " +
			" bank_name  TEXT NOT NULL, " +
			" PRIMARY KEY (bank_name_id) " +
			" );";
	
	public static final String TB_CREDIT = 
			" CREATE TABLE creditcard_type ( " +
			" creditcard_type_id  INTEGER NOT NULL, " +
			" creditcard_type_name  TEXT NOT NULL, " +
			" PRIMARY KEY (creditcard_type_id) " +
			" );";
	
	public static final String TB_PAY_TYPE = 
			" CREATE TABLE pay_type ( " + 
			" pay_type_id  INTEGER NOT NULL, " +
			" pay_type_code  TEXT NOT NULL, " +
			" pay_type_name  TEXT NOT NULL, " +
			" PRIMARY KEY (pay_type_id) " +
			" );";
	
	public static final String TB_PAY_DETAIL = 
			" CREATE TABLE payment_detail ( " +
			" pay_detail_id  INTEGER NOT NULL, " +
			" transaction_id  INTEGER NOT NULL, " +
			" computer_id  INTEGER NOT NULL, " +
			" pay_type_id  INTEGER NOT NULL, " +
			" pay_amount  REAL NOT NULL, " +
			" credit_card_no  TEXT, " +
			" expire_month  INTEGER, " +
			" expire_year  INTEGER, " +
			" bank_id  INTEGER, " +
			" credit_card_type  INTEGER, " +
			" remark  TEXT, " +
			" PRIMARY KEY (pay_detail_id ASC, transaction_id ASC, computer_id ASC) " +
			" );";
	
	public static final String TB_SESSION = 
			" CREATE TABLE session ( " +
			" session_id  INTEGER NOT NULL, " +
			" computer_id  INTEGER NOT NULL, " +
			" shop_id  INTEGER NOT NULL, " +
			" open_staff_id  INTEGER NOT NULL, " +
			" close_staff_id  INTEGER, " +
			" session_date  TEXT, " +
			" open_date_time  TEXT, " +
			" close_date_time  TEXT, " +
			" open_amount  REAL, " +
			" close_amount  REAL, " +
			" is_endday  INTEGER, " +
			" PRIMARY KEY (session_id, computer_id, shop_id) " +
			" );";
	
	public static final String TB_SESSION_END =
			" CREATE TABLE session_end_day ( " +
			" session_date  TEXT NOT NULL, " +
			" endday_date_time  TEXT, " +
			" total_qty_receipt  INTEGER, " +
			" total_amount_receipt  REAL, " +
			" is_send_to_hq  INTEGER, " +
			" send_to_hq_date_time  TEXT, " +
			" PRIMARY KEY (session_date) " +
			" );";
	
	public static final String TB_PRO_GROUP = 
			" CREATE TABLE product_group ( " +
			" product_group_id  INTEGER NOT NULL, " +
			" product_group_code  TEXT, " +
			" product_group_name  TEXT, " +
			" is_comment  INTEGER NOT NULL DEFAULT 0, " +
			" product_group_type  INTEGER NOT NULL DEFAULT 0, " +
			" product_group_ordering  INTEGER NOT NULL DEFAULT 0, " +
			" updatedate  TEXT, " +
			" PRIMARY KEY (product_group_id) " +
			" );";
	
	public static final String TB_PRO_DEPT = 
			" CREATE TABLE product_dept ( " +
			" product_dept_id  INTEGER NOT NULL, " +
			" product_group_id  INTEGER NOT NULL, " +
			" product_dept_code  TEXT, " +
			" product_dept_name  TEXT, " +
			" product_dept_ordering  INTEGER NOT NULL DEFAULT 0, " +
			" updatedate  TEXT, " +
			" PRIMARY KEY (product_dept_id) " +
			" );";

	public static final String TB_PRODUCT = 
			" CREATE TABLE products ( " +
			" product_id  INTEGER NOT NULL, " +
			" product_dept_id  INTEGER NOT NULL, " +
			" product_group_id  INTEGER NOT NULL DEFAULT 0, " +
			" product_code  TEXT, " +
			" product_bar_code  TEXT, " +
			" product_type_id  INTEGER NOT NULL DEFAULT 0, " +
			" product_price  REAL NOT NULL DEFAULT 0.0, " +
			" product_unit_name  TEXT, " +
			" product_desc  TEXT, " +
			" discount_allow  INTEGER DEFAULT 0, " +
			" vat_type  INTEGER NOT NULL DEFAULT 0, " +
			" vat_rate  REAL NOT NULL DEFAULT 0.0, " +
			" has_service_charge  INTEGER NOT NULL DEFAULT 0, " +
			" activate  INTEGER NOT NULL DEFAULT 1, " +
			" is_out_of_stock  INTEGER NOT NULL DEFAULT 0, " +
			" sale_mode_1  INTEGER NOT NULL DEFAULT 0, " +
			" product_price_1  REAL NOT NULL DEFAULT 0.0, " +
			" sale_mode_2  INTEGER NOT NULL DEFAULT 0, " +
			" product_price_2  REAL NOT NULL DEFAULT 0.0, " +
			" sale_mode_3  INTEGER NOT NULL DEFAULT 0, " +
			" product_price_3  REAL NOT NULL DEFAULT 0.0, " +
			" sale_mode_4  INTEGER NOT NULL DEFAULT 0, " +
			" product_price_4  REAL NOT NULL DEFAULT 0.0, " +
			" sale_mode_5  INTEGER NOT NULL DEFAULT 0, " +
			" product_price_5  REAL NOT NULL DEFAULT 0.0, " +
			" updatedate  TEXT, " +
			" PRIMARY KEY (product_id) " +
			" );";
	
	public static final String TB_PCOMP_GROUP = 
			" CREATE TABLE p_component_group ( " +
			" p_group_id  INTEGER NOT NULL DEFAULT 0, " +
			" product_id  INTEGER NOT NULL DEFAULT 0, " +
			" sale_mode  INTEGER NOT NULL DEFAULT 1, " +
			" set_group_no  INTEGER NOT NULL DEFAULT 0, " +
			" set_group_name  TEXT NOT NULL, " +
			" require_amount  INTEGER NOT NULL DEFAULT 0 " +
			" );";
	
	public static final String TB_PCOMP_SET = 
			" CREATE TABLE p_component_set ( " +
			" p_group_id  INTEGER NOT NULL DEFAULT 0, " +
			" product_id  INTEGER NOT NULL DEFAULT 0, " +
			" sale_mode  INTEGER NOT NULL DEFAULT 1, " +
			" child_product_id  INTEGER NOT NULL DEFAULT 0, " +
			" child_product_amount  REAL NOT NULL, " +
			" flexible_product_price  REAL NOT NULL DEFAULT 0, " +
			" flexible_include_price  INTEGER NOT NULL DEFAULT 0 " +
			" );";
	
	public static final String TB_PRO_COMP = 
			" CREATE TABLE product_component ( " +
			" p_group_id  INTEGER NOT NULL DEFAULT 0, " +
			" product_id  INTEGER NOT NULL DEFAULT 0, " +
			" sale_mode  INTEGER NOT NULL DEFAULT 0, " +
			" child_product_id  INTEGER NOT NULL DEFAULT 0, " +
			" child_product_amount  REAL NOT NULL, " +
			" flexible_product_price  REAL NOT NULL, " +
			" flexible_include_price  INTEGER NOT NULL, " +
			" PRIMARY KEY (p_group_id, product_id, sale_mode, child_product_id) " +
			" );";
	
	public static final String TB_COM_PRO =
			" CREATE TABLE comment_product ( " +
			" product_id  INTEGER NOT NULL DEFAULT 0, " +
			" comment_id  INTEGER NOT NULL DEFAULT 0 " +
			" );";
	
	public static final String TB_SALE_MODE = 
			" CREATE TABLE sale_mode ( " +
			" sale_mode_id  INTEGER, " +
			" sale_mode_name  TEXT, " +
			" positon_prefix  INTEGER NOT NULL DEFAULT 0, " +
			" prefix_text  TEXT, " +
			" prefix_queue  TEXT, " +
			" PRIMARY KEY (sale_mode_id ASC) " +
			" );";
			
	public static final String TB_MENU_DEPT = 
			" CREATE TABLE menu_dept ( " +
			" menu_dept_id  INTEGER NOT NULL DEFAULT 0, " +
			" menu_group_id  INTEGER, " +
			" menu_dept_name_0  TEXT, " +
			" menu_dept_name_1  TEXT, " +
			" menu_dept_name_2  TEXT, " +
			" menu_dept_name_3  TEXT, " +
			" menu_dept_ordering  INTEGER NOT NULL DEFAULT 0, " +
			" updatedate  TEXT, " +
			" activate INTEGER, " +
			" PRIMARY KEY (menu_dept_id) " +
			" );";
	
	public static final String TB_MENU_GROUP = 
			" CREATE TABLE menu_group ( " +
			" menu_group_id  INTEGER NOT NULL, " +
			" menu_group_name_0  TEXT, " +
			" menu_group_name_1  TEXT, " +
			" menu_group_name_2  TEXT, " +
			" menu_group_name_3  TEXT, " +
			" menu_group_type_id  INTEGER NOT NULL DEFAULT 0, " +
			" menu_group_ordering  INTEGER NOT NULL DEFAULT 0, " +
			" updatedate  TEXT, " +
			" activate INTEGER, " +
			" PRIMARY KEY (menu_group_id) " +
			" ); ";

	
	public static final String TB_MENU = 
			" CREATE TABLE menu_item ( " +
			" menu_item_id  INTEGER NOT NULL, " +
			" product_id  INTEGER NOT NULL DEFAULT 0, " +
			" menu_dept_id  INTEGER NOT NULL DEFAULT 0, " +
			" menu_group_id  INTEGER NOT NULL DEFAULT 0, " +
			" menu_name_0  TEXT, " +
			" menu_name_1  TEXT, " +
			" menu_name_2  TEXT, " +
			" menu_name_3  TEXT, " +
			" menu_desc_0  TEXT, " +
			" menu_desc_1  TEXT, " +
			" menu_desc_2  TEXT, " +
			" menu_desc_3  TEXT, " +
			" menu_long_desc_0  TEXT, " +
			" menu_long_desc_1  TEXT, " +
			" menu_long_desc_2  TEXT, " +
			" menu_long_desc_3  TEXT, " +
			" menu_short_name_0  TEXT, " +
			" menu_short_name_1  TEXT, " +
			" menu_short_name_2  TEXT, " +
			" menu_short_name_3  TEXT, " +
			" menu_image_link  TEXT, " +
			" menu_ordering  INTEGER NOT NULL DEFAULT 0, " +
			" updatedate  TEXT, " +
			" menu_activate  INTEGER NOT NULL DEFAULT 0 " +
			" );";
	
	public static final String TB_MENU_FIX_COMM = 
			" CREATE TABLE menu_fixcomment ( " +
			" menuitem_id  INTEGER NOT NULL DEFAULT 0, " +
			" menucomment_id  INTEGER NOT NULL DEFAULT 0 " +
			" ); ";
	
	public static final String TB_MENU_COMM_GROUP = 
			" CREATE TABLE menu_comment_group ( " +
			" menu_comment_group_id  INTEGER NOT NULL, " +
			" menu_comment_group_name_0  TEXT, " +
			" menu_comment_group_name_1  TEXT, " +
			" updatedate  TEXT " +
			" );";
	
	public static final String TB_MENU_COMM =
			" CREATE TABLE menu_comment ( " +
			" menu_comment_id  INTEGER NOT NULL, " +
			" menu_comment_group_id  INTEGER NOT NULL DEFAULT 0, " +
			" menu_comment_name_0  TEXT, " +
			" menu_comment_name_1  TEXT, " +
			" menu_comment_name_2  TEXT, " +
			" menu_comment_name_3  TEXT, " +
			" menu_comment_ordering  INTEGER, " +
			" updatedate  TEXT, " +
			" PRIMARY KEY (menu_comment_id) " +
			" );";
	
	public static final String TB_MEMBER_GROUP = 
			" CREATE TABLE member_group ( " +
			" member_group_id INTEGER, " +
			" shop_id INTEGER, " +
			" member_group_code TEXT, " +
			" member_group_name TEXT, " +
			" PRIMARY KEY (member_group_id, shop_id) " +
			" ); ";
	
	public static final String TB_MEMBER = 
			" CREATE TABLE members ( " +
			" member_id  INTEGER, " +
			" shop_id  INTEGER, " +
			" member_group_id  INTEGER, " +
			" member_code  TEXT, " +
			" member_gender  INTEGER, " +
			" member_firstname  TEXT, " +
			" member_lastname  TEXT, " +
			" member_address1  TEXT, " +
			" member_address2  TEXT, " +
			" member_city  TEXT, " +
			" member_province  INTEGER, " +
			" member_zipcode  TEXT, " +
			" member_tel  TEXT, " +
			" member_mobile  TEXT, " +
			" member_fax  TEXT, " +
			" member_email  TEXT, " +
			" member_birthday  INTEGER, " +
			" member_expiredate  INTEGER, " +
			" update_by  INTEGER, " +
			" update_date  INTEGER, " +
			" insert_at_shop_id  INTEGER, " +
			" remark TEXT, " +
			" PRIMARY KEY (member_id ASC, shop_id ASC) " +
			" );";

	public static final String TB_TRANS = 
			" CREATE TABLE order_transaction ( " +
			" transaction_id  INTEGER, " +
			" computer_id  INTEGER, " +
			" shop_id  INTEGER, " +
			" open_time  TEXT, " +
			" open_staff_id  INTEGER, " +
			" paid_time  TEXT, " +
			" paid_staff_id  INTEGER, " +
			" close_time  TEXT, " +
			" transaction_status_id  INTEGER, " +
			" document_type_id  INTEGER, " +
			" receipt_year  INTEGER, " +
			" receipt_month  INTEGER, " +
			" receipt_id  INTEGER, " +
			" sale_date  TEXT, " +
			" session_id  INTEGER, " +
			" void_staff_id  INTEGER, " +
			" void_reason  TEXT, " +
			" void_time  TEXT, " +
			" member_id  INTEGER, " +
			" transaction_vat  REAL, " +
			" transaction_exclude_vat  REAL, " +
			" transaction_vatable  REAL, " +
			" service_charge  REAL, " +
			" service_charge_vat  REAL, " +
			" remark  TEXT, " +
			" transaction_note  TEXT, " +
			" PRIMARY KEY (transaction_id ASC, computer_id ASC) " +
			" );";

	public static final String TB_ORDER = 
			" CREATE TABLE order_detail ( " +
			" order_detail_id  INTEGER, " +
			" transaction_id  INTEGER, " +
			" computer_id  INTEGER, " +
			" product_id  INTEGER, " +
			" product_type_id  INTEGER, " +
			" product_name  TEXT, " +
			" qty  REAL, " +
			" price_per_unit  REAL, " +
			" total_retail_price  REAL, " +
			" total_sale_price  REAL, " +
			" vat_type  INTEGER, " +
			" vat  REAL, " +
			" member_discount  REAL, " +
			" price_discount  REAL, " +
			" PRIMARY KEY (order_detail_id ASC, transaction_id ASC, computer_id ASC) " +
			" );";
	
	public static final String TB_ORDER_TMP = 
			" CREATE TABLE order_detail_tmp ( " +
			" order_detail_id  INTEGER, " +
			" transaction_id  INTEGER, " +
			" computer_id  INTEGER, " +
			" product_id  INTEGER, " +
			" product_type_id  INTEGER, " +
			" product_name  TEXT, " +
			" qty  REAL, " +
			" price_per_unit  REAL, " +
			" total_retail_price  REAL, " +
			" total_sale_price  REAL, " +
			" vat_type  INTEGER, " +
			" vat  REAL, " +
			" member_discount  REAL, " +
			" price_discount  REAL, " +
			" PRIMARY KEY (order_detail_id ASC, transaction_id ASC, computer_id ASC) " +
			" );";
	
	public static final String TB_ORDER_COMM =
			" CREATE TABLE order_comment ( " +
			" transaction_id  INTEGER NOT NULL, " +
			" order_detail_id  INTEGER NOT NULL, " +
			" menu_comment_id  INTEGER NOT NULL, " +
			" comment_amount  REAL NOT NULL, " +
			" comment_price  REAL NOT NULL " +
			" );";
	
	public static final String TB_ORDER_SET = 
			" CREATE TABLE order_set ( " +
			" transaction_id  INTEGER NOT NULL, " +
			" order_detail_id  INTEGER NOT NULL DEFAULT 0, " +
			" order_set_id  INTEGER NOT NULL, " +
			" product_id  INTEGER NOT NULL DEFAULT 0, " +
			" p_group_id  INTEGER NOT NULL DEFAULT 0, " +
			" set_group_no  INTEGER NOT NULL DEFAULT 0, " +
			" product_price  REAL NOT NULL DEFAULT 0, " +
			" product_amount  REAL NOT NULL DEFAULT 0, " +
			" remark  TEXT NOT NULL, " +
			" PRIMARY KEY (transaction_id ASC, order_detail_id ASC, order_set_id ASC) " +
			" );";

	public static final String TB_ORDER_SET_COMM = 
			" CREATE TABLE order_set_comment ( " +
			" transaction_id  INTEGER, " +
			" order_detail_id  INTEGER, " +
			" order_set_id  INTEGER, " +
			" menu_comment_id  INTEGER, " +
			" p_group_id  INTEGER, " +
			" product_id  INTEGER, " +
			" product_amount  REAL, " +
			" product_price  REAL " +
			" );";
	
	public static final String TB_DOC_TYPE_GROUP = 
			" CREATE TABLE document_type_group ( " +
			" document_type_group_id  INTEGER, " +
			" group_header  TEXT, " +
			" group_ordering  INTEGER" +
			" );";
	
	public static final String TB_DOC_TYPE_GROUP_VAL = 
			" CREATE TABLE document_type_group_value ( " +
			" doc_type_group_id  INTEGER, " +
			" document_type_id  INTEGER" +
			" );";
	
	public static final String TB_DOC_TYPE = 
			" CREATE TABLE document_type ( " +
			" document_type_id  INTEGER, " +
			" document_type_header  TEXT, " +
			" document_type_name  TEXT, " +
			" movement_in_stock  INTEGER " +
			" );";
	
	public static final String TB_DOC = 
			" CREATE TABLE document ( " +
			" document_id  INTEGER, " +
			" shop_id  INTEGER, " +
			" ref_document_id  INTEGER, " +
			" ref_shop_id  INTEGER, " +
			" document_type_id  INTEGER, " +
			" document_no  TEXT, " +
			" document_date, " +
			" update_by  INTEGER, " +
			" update_date  TEXT, " +
			" document_status  INTEGER, " +
			" remark  TEXT, " +
			" is_send_to_hq  INTEGER, " +
			" send_to_hq_datetime  TEXT," +
			" PRIMARY KEY (document_id ASC, shop_id ASC) " +
			" );";

	public static final String TB_DOC_DETAIL = 
			" CREATE TABLE docdetail ( " +
			" docdetail_id  INTEGER, " +
			" document_id  INTEGER, " +
			" shop_id  INTEGER, " +
			" material_id  INTEGER, " +
			" unit_name  TEXT, " +
			" material_qty  REAL, " +
			" material_balance  REAL, " +
			" material_price_per_unit  REAL, " +
			" material_net_price  REAL, " +
			" material_tax_type  INTEGER, " +
			" material_tax_price  REAL, " +
			" PRIMARY KEY (docdetail_id ASC, document_id ASC, shop_id ASC) " +
			" );";
	
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
