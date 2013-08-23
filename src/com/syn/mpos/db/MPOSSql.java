package com.syn.mpos.db;

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
			" staff_code  TEXT(20), " +
			" staff_name  TEXT(100), " +
			" staff_password  TEXT(100), " +
			" PRIMARY KEY (staff_id) " +
			" );";
	
	public static final String TB_COMP = 
			" CREATE TABLE computer_property ( " +
			" computer_id  INTEGER NOT NULL DEFAULT 0, " +
			" computer_name  TEXT(50), " +
			" device_code  TEXT(50), " +
			" registration_number  TEXT(50), " +
			" PRIMARY KEY (computer_id ASC) " +
			" );";
	
	public static final String TB_GB_PROP =
			" CREATE TABLE global_property ( " + 
			" currency_symbol  TEXT(10), " +
			" currency_code  TEXT(10), " +
			" currency_name  TEXT(20), " +
			" currency_format  TEXT(20), " +
			" date_format  TEXT(20), " +
			" time_format  TEXT(20), " +
			" qty_format  TEXT(20) " +
			" );";
	
	public static final String TB_PROVINCE =
			" CREATE TABLE province ( " +
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
			" pay_amount  REAL(18,4) NOT NULL, " +
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
			" session_date  TEXT NOT NULL, " +
			" open_date_time  TEXT NOT NULL, " +
			" close_date_time  TEXT NOT NULL, " +
			" open_amount  REAL(18,4) NOT NULL, " +
			" close_amount  REAL(18,4) NOT NULL, " +
			" is_endday  INTEGER NOT NULL, " +
			" PRIMARY KEY (session_id, computer_id, shop_id) " +
			" );";
	
	public static final String TB_SESSION_END =
			" CREATE TABLE session_end_day ( " +
			" session_date  TEXT NOT NULL, " +
			" endday_date_time  TEXT NOT NULL, " +
			" total_qty_receipt  INTEGER NOT NULL, " +
			" total_amount_receipt  REAL(18,4) NOT NULL, " +
			" is_send_to_hq  INTEGER NOT NULL, " +
			" send_to_hq_date_time  TEXT NOT NULL, " +
			" PRIMARY KEY (session_date) " +
			" );";
	
	public static final String TB_PRO_GROUP = 
			" CREATE TABLE product_group ( " +
			" product_group_id  INTEGER NOT NULL, " +
			" product_group_code  TEXT(30), " +
			" product_group_name  TEXT(50), " +
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
			" product_dept_code  TEXT(30), " +
			" product_dept_name  TEXT(50), " +
			" product_dept_ordering  INTEGER NOT NULL DEFAULT 0, " +
			" updatedate  TEXT, " +
			" PRIMARY KEY (product_dept_id) " +
			" );";

	public static final String TB_PRODUCT = 
			" CREATE TABLE products ( " +
			" product_id  INTEGER NOT NULL, " +
			" product_dept_id  INTEGER NOT NULL, " +
			" product_group_id  INTEGER NOT NULL DEFAULT 0, " +
			" product_code  TEXT(30), " +
			" product_bar_code  TEXT(30), " +
			" product_type_id  INTEGER NOT NULL DEFAULT 0, " +
			" product_price  REAL NOT NULL DEFAULT 0.0, " +
			" product_unit_name  TEXT(50), " +
			" product_desc  TEXT(200), " +
			" discount_allow  INTEGER DEFAULT 0, " +
			" vat_type  INTEGER NOT NULL DEFAULT 0, " +
			" vat_rate  REAL(3,2) NOT NULL DEFAULT 0.0, " +
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
			" child_product_amount  REAL(18,4) NOT NULL, " +
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
			" menu_dept_name_0  TEXT(100), " +
			" menu_dept_name_1  TEXT(100), " +
			" menu_dept_name_2  TEXT(100), " +
			" menu_dept_name_3  TEXT(100), " +
			" menu_dept_ordering  INTEGER NOT NULL DEFAULT 0, " +
			" updatedate  TEXT, " +
			" PRIMARY KEY (menu_dept_id) " +
			" );";
	
	public static final String TB_MENU_GROUP = 
			" CREATE TABLE menu_group ( " +
			" menu_group_id  INTEGER NOT NULL, " +
			" menu_group_name_0  TEXT(100), " +
			" menu_group_name_1  TEXT(100), " +
			" menu_group_name_2  TEXT(100), " +
			" menu_group_name_3  TEXT(100), " +
			" menu_group_type_id  INTEGER NOT NULL DEFAULT 0, " +
			" menu_group_ordering  INTEGER NOT NULL DEFAULT 0, " +
			" updatedate  TEXT, " +
			" PRIMARY KEY (menu_group_id) " +
			" ); ";

	
	public static final String TB_MENU = 
			" CREATE TABLE menu_item ( " +
			" menu_item_id  INTEGER NOT NULL, " +
			" product_id  INTEGER NOT NULL DEFAULT 0, " +
			" menu_dept_id  INTEGER NOT NULL DEFAULT 0, " +
			" menu_group_id  INTEGER NOT NULL DEFAULT 0, " +
			" menu_name_0  TEXT(100), " +
			" menu_name_1  TEXT(100), " +
			" menu_name_2  TEXT(100), " +
			" menu_name_3  TEXT(100), " +
			" menu_desc_0  TEXT(100), " +
			" menu_desc_1  TEXT(100), " +
			" menu_desc_2  TEXT(100), " +
			" menu_desc_3  TEXT(100), " +
			" menu_long_desc_0  TEXT(100), " +
			" menu_long_desc_1  TEXT(100), " +
			" menu_long_desc_2  TEXT(100), " +
			" menu_long_desc_3  TEXT(100), " +
			" menu_short_name_0  TEXT(5), " +
			" menu_short_name_1  TEXT(5), " +
			" menu_short_name_2  TEXT(5), " +
			" menu_short_name_3  TEXT(5), " +
			" menu_image_link  TEXT(100), " +
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
			" menu_comment_name_0  TEXT(50), " +
			" menu_comment_name_1  TEXT(50), " +
			" menu_comment_name_2  TEXT(50), " +
			" menu_comment_name_3  TEXT(50), " +
			" menu_comment_ordering  INTEGER, " +
			" updatedate  TEXT, " +
			" PRIMARY KEY (menu_comment_id) " +
			" );";
	
	public static final String TB_MEMBER = 
			" CREATE TABLE members ( " +
			" member_id  INTEGER NOT NULL, " +
			" shop_id  INTEGER NOT NULL, " +
			" member_group_id  INTEGER NOT NULL, " +
			" member_code  TEXT NOT NULL, " +
			" member_gender  INTEGER NOT NULL, " +
			" member_firstname  TEXT NOT NULL, " +
			" member_lastname  TEXT NOT NULL, " +
			" member_address1  TEXT NOT NULL, " +
			" member_address2  TEXT, " +
			" member_city  TEXT NOT NULL, " +
			" member_province  INTEGER NOT NULL, " +
			" member_zipcode  TEXT NOT NULL, " +
			" member_tel  TEXT, " +
			" member_mobile  TEXT, " +
			" member_fax  TEXT, " +
			" member_email  TEXT, " +
			" member_birthday  INTEGER, " +
			" member_expiredate  INTEGER, " +
			" updateby  INTEGER, " +
			" updatedate  INTEGER, " +
			" insert_at_shop_id  INTEGER, " +
			" PRIMARY KEY (member_id ASC, shop_id ASC) " +
			" );";

	public static final String TB_TRANS = 
			" CREATE TABLE order_transaction ( " +
			" transaction_id  INTEGER NOT NULL DEFAULT 0, " +
			" computer_id  INTEGER NOT NULL DEFAULT 0, " +
			" shop_id  INTEGER NOT NULL DEFAULT 0, " +
			" open_time  TEXT, " +
			" open_staff_id  INTEGER NOT NULL DEFAULT 0, " +
			" paid_time  TEXT, " +
			" paid_staff_id  INTEGER NOT NULL DEFAULT 0, " +
			" close_time  TEXT, " +
			" transaction_status_id  INTEGER NOT NULL DEFAULT 1, " +
			" document_type_id  INTEGER NOT NULL DEFAULT 8, " +
			" receipt_year  INTEGER NOT NULL DEFAULT 0, " +
			" receipt_month  INTEGER NOT NULL DEFAULT 0, " +
			" receipt_id  INTEGER NOT NULL DEFAULT 0, " +
			" sale_date  TEXT, " +
			" session_id  INTEGER NOT NULL DEFAULT 0, " +
			" void_staff_id  INTEGER NOT NULL DEFAULT 0, " +
			" void_reason  TEXT(200), " +
			" void_time  TEXT, " +
			" member_id  INTEGER NOT NULL DEFAULT 0, " +
			" transaction_vat  REAL(18,4) NOT NULL DEFAULT 0, " +
			" transaction_exclude_vat  REAL(18,4) NOT NULL DEFAULT 0, " +
			" service_charge  REAL(18,4) NOT NULL DEFAULT 0, " +
			" service_charge_vat  REAL(18,4) NOT NULL DEFAULT 0, " +
			" remark  TEXT, " +
			" PRIMARY KEY (transaction_id ASC, computer_id ASC) " +
			" );";

	public static final String TB_ORDER = 
			" CREATE TABLE order_detail ( " +
			" order_detail_id  INTEGER NOT NULL DEFAULT 0, " +
			" transaction_id  INTEGER NOT NULL DEFAULT 0, " +
			" computer_id  INTEGER NOT NULL DEFAULT 0, " +
			" product_id  INTEGER NOT NULL DEFAULT 0, " +
			" product_name  TEXT NOT NULL, " +
			" sale_mode  INTEGER NOT NULL DEFAULT 1, " +
			" product_amount  REAL(18,4) NOT NULL DEFAULT 0, " +
			" product_price  REAL(18,4) NOT NULL DEFAULT 0, " +
			" sale_price  REAL(18,4) NOT NULL DEFAULT 0, " +
			" total_sale_price  REAL(18,4) NOT NULL DEFAULT 0, " +
			" total_product_price  REAL(18,4) NOT NULL DEFAULT 0, " +
			" vat_type  INTEGER NOT NULL DEFAULT 1, " +
			" vat  REAL(18,4) NOT NULL DEFAULT 0, " +
			" vat_exclude  REAL(18,4) NOT NULL DEFAULT 0, " +
			" service_charge  REAL(18,4) NOT NULL DEFAULT 0, " +
			" service_charge_vat  REAL(18,4) NOT NULL DEFAULT 0, " +
			" member_discount  REAL(18,4) NOT NULL DEFAULT 0, " +
			" each_product_discount  REAL(18,4) NOT NULL DEFAULT 0, " +
			" PRIMARY KEY (order_detail_id ASC, transaction_id ASC, computer_id ASC) " +
			" );";
	
	public static final String TB_ORDER_TMP = 
			" CREATE TABLE order_detail_tmp ( " +
			" order_detail_id  INTEGER NOT NULL DEFAULT 0, " +
			" transaction_id  INTEGER NOT NULL DEFAULT 0, " +
			" computer_id  INTEGER NOT NULL DEFAULT 0, " +
			" product_id  INTEGER NOT NULL DEFAULT 0, " +
			" product_name  TEXT NOT NULL, " +
			" sale_mode  INTEGER NOT NULL DEFAULT 1, " +
			" product_amount  REAL(18,4) NOT NULL DEFAULT 0, " +
			" product_price  REAL(18,4) NOT NULL DEFAULT 0, " +
			" sale_price  REAL(18,4) NOT NULL DEFAULT 0, " +
			" total_sale_price  REAL(18,4) NOT NULL DEFAULT 0, " +
			" total_product_price  REAL(18,4) NOT NULL DEFAULT 0, " +
			" vat_type  INTEGER NOT NULL DEFAULT 1, " +
			" vat  REAL(18,4) NOT NULL DEFAULT 0, " +
			" vat_exclude  REAL(18,4) NOT NULL DEFAULT 0, " +
			" service_charge  REAL(18,4) NOT NULL DEFAULT 0, " +
			" service_charge_vat  REAL(18,4) NOT NULL DEFAULT 0, " +
			" member_discount  REAL(18,4) NOT NULL DEFAULT 0, " +
			" each_product_discount  REAL(18,4) NOT NULL DEFAULT 0, " +
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
			" transaction_id  INTEGER NOT NULL, " +
			" order_detail_id  INTEGER NOT NULL, " +
			" order_set_id  INTEGER NOT NULL, " +
			" menu_comment_id  INTEGER NOT NULL DEFAULT 0, " +
			" p_group_id  INTEGER NOT NULL DEFAULT 0, " +
			" product_id  INTEGER NOT NULL DEFAULT 0, " +
			" product_amount  REAL NOT NULL DEFAULT 0, " +
			" product_price  REAL NOT NULL DEFAULT 0 " +
			" );";
	
	public static final String TB_DOC_TYPE_GROUP = 
			" CREATE TABLE document_type_group ( " +
			" document_type_group_id  INTEGER NOT NULL, " +
			" group_header  TEXT NOT NULL, " +
			" group_ordering  INTEGER NOT NULL " +
			" );";
	
	public static final String TB_DOC_TYPE_GROUP_VAL = 
			" CREATE TABLE document_type_group_value ( " +
			" doc_type_group_id  INTEGER NOT NULL, " +
			" document_type_id  INTEGER NOT NULL, " +
			" PRIMARY KEY (doc_type_group_id ASC, document_type_id ASC) " +
			" );";
	
	public static final String TB_DOC_TYPE = 
			" CREATE TABLE document_type ( " +
			" document_type_id  INTEGER NOT NULL, " +
			" document_type_header  TEXT NOT NULL, " +
			" document_type_name  TEXT NOT NULL, " +
			" movement_in_stock  INTEGER NOT NULL DEFAULT 0, " +
			" PRIMARY KEY (document_type_id) " +
			" );";
	
	public static final String TB_DOC = 
			" CREATE TABLE document ( " +
			" document_id  INTEGER NOT NULL, " +
			" shop_id  INTEGER NOT NULL, " +
			" document_type_id  INTEGER NOT NULL, " +
			" document_no  TEXT NOT NULL, " +
			" document_date  TEXT NOT NULL, " +
			" update_by  INTEGER NOT NULL, " +
			" update_date  TEXT NOT NULL, " +
			" document_status  INTEGER NOT NULL, " +
			" remark  TEXT, " +
			" is_send_to_hq  INTEGER NOT NULL, " +
			" send_to_hq_datetime  TEXT NOT NULL, " +
			" PRIMARY KEY (document_id, shop_id) " +
			" );";

	public static final String TB_DOC_DETAIL = 
			" CREATE TABLE docdetail ( " +
			" docdetail_id  INTEGER NOT NULL, " +
			" document_id  INTEGER NOT NULL, " +
			" shop_id  INTEGER NOT NULL, " +
			" product_id  INTEGER NOT NULL, " +
			" unit_name  TEXT NOT NULL, " +
			" product_qty  REAL(18,4) NOT NULL, " +
			" product_price  REAL(18,4) NOT NULL, " +
			" product_net_price  REAL(18,4) NOT NULL, " +
			" product_tax_type  INTEGER NOT NULL, " +
			" product_tax_price  REAL(18,4) NOT NULL, " +
			" PRIMARY KEY (docdetail_id ASC, document_id ASC, shop_id ASC) " +
			" );";
}
