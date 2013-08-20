package com.syn.mpos.model;

import java.util.List;

public class ProductGroups {
	
	private List<ProductGroup> ProductGroup;
	private List<ProductDept> ProductDept;
	private List<Products> Products;
	private List<ProductComponent> ProductComponent;
	private List<PComponentGroup> PComponentGroup;
	private List<PComponentSet> PComponentSet;
	private List<CommentProduct> CommentProduct;
	private List<SaleMode> SaleMode;
	private List<QuestionGroup> QuestionGroup;
	private List<QuestionDetail> QuestionDetail;
	private List<AnswerOption> AnswerOption;
	
	public List<AnswerOption> getAnswerOption() {
		return AnswerOption;
	}

	public void setAnswerOption(List<AnswerOption> answerOption) {
		AnswerOption = answerOption;
	}

	public List<Products> getProducts() {
		return Products;
	}

	public void setProducts(List<Products> products) {
		Products = products;
	}

	public List<SaleMode> getSaleMode() {
		return SaleMode;
	}

	public void setSaleMode(List<SaleMode> saleMode) {
		SaleMode = saleMode;
	}

	public List<QuestionGroup> getQuestionGroup() {
		return QuestionGroup;
	}

	public void setQuestionGroup(List<QuestionGroup> questionGroup) {
		QuestionGroup = questionGroup;
	}

	public List<QuestionDetail> getQuestionDetail() {
		return QuestionDetail;
	}

	public void setQuestionDetail(List<QuestionDetail> questionDetail) {
		QuestionDetail = questionDetail;
	}

	public List<CommentProduct> getCommentProduct() {
		return CommentProduct;
	}

	public void setCommentProduct(List<CommentProduct> commentProduct) {
		CommentProduct = commentProduct;
	}

	public List<PComponentGroup> getPComponentGroup() {
		return PComponentGroup;
	}

	public void setPComponentGroup(List<PComponentGroup> pComponentGroup) {
		PComponentGroup = pComponentGroup;
	}

	public List<PComponentSet> getPComponentSet() {
		return PComponentSet;
	}

	public void setPComponentSet(List<PComponentSet> pComponentSet) {
		PComponentSet = pComponentSet;
	}

	public List<ProductGroup> getProductGroup() {
		return ProductGroup;
	}

	public void setProductGroup(List<ProductGroup> productGroup) {
		this.ProductGroup = productGroup;
	}

	public List<ProductDept> getProductDept() {
		return ProductDept;
	}

	public void setProductDept(List<ProductDept> productDept) {
		ProductDept = productDept;
	}

	public List<Products> getProduct() {
		return Products;
	}

	public void setProduct(List<Products> product) {
		Products = product;
	}

	public List<ProductComponent> getProductComponent() {
		return ProductComponent;
	}

	public void setProductComponent(List<ProductComponent> productComponent) {
		ProductComponent = productComponent;
	}

	public static class AnswerOption{
		private int AnswerID;
        private int QuestionID;
        private String AnswerName;
        
        public AnswerOption(int ansId, int quesId, String ansName){
        	this.AnswerID = ansId;
        	this.QuestionID = quesId;
        	this.AnswerName = ansName;
        }
        
		public int getAnswerID() {
			return AnswerID;
		}
		public void setAnswerID(int answerID) {
			AnswerID = answerID;
		}
		public int getQuestionID() {
			return QuestionID;
		}
		public void setQuestionID(int questionID) {
			QuestionID = questionID;
		}
		public String getAnswerName() {
			return AnswerName;
		}
		public void setAnswerName(String answerName) {
			AnswerName = answerName;
		}
	}
	
	public static class QuestionAnswerData
    {
        private int iQuestionID;
        private int iAnsOptionID;
        private float fAnsValue;
        private String szAnsText;

		public int getiQuestionID() {
			return iQuestionID;
		}
		public void setiQuestionID(int iQuestionID) {
			this.iQuestionID = iQuestionID;
		}
		public int getiAnsOptionID() {
			return iAnsOptionID;
		}
		public void setiAnsOptionID(int iAnsOptionID) {
			this.iAnsOptionID = iAnsOptionID;
		}
		public float getfAnsValue() {
			return fAnsValue;
		}
		public void setfAnsValue(float fAnsValue) {
			this.fAnsValue = fAnsValue;
		}
		public String getSzAnsText() {
			return szAnsText;
		}
		public void setSzAnsText(String szAnsText) {
			this.szAnsText = szAnsText;
		}
    }

	public static class QuestionDetail{
		private int QuestionID;
		private String QuestionName;
		private int QuestionGroupID;
		private int QuestionTypeID;
		private int IsRequired;
		private int Ordering;
		private float questionValue;
		private int questionOptId;
		private int optionId;
		private boolean requireSelect;
		
		public boolean isRequireSelect() {
			return requireSelect;
		}
		public void setRequireSelect(boolean requireSelect) {
			this.requireSelect = requireSelect;
		}
		public int getOptionId() {
			return optionId;
		}
		public void setOptionId(int optionId) {
			this.optionId = optionId;
		}
		public float getQuestionValue() {
			return questionValue;
		}
		public void setQuestionValue(float questionValue) {
			this.questionValue = questionValue;
		}
		public int getQuestionOptId() {
			return questionOptId;
		}
		public void setQuestionOptId(int questionOptId) {
			this.questionOptId = questionOptId;
		}
		public int getQuestionID() {
			return QuestionID;
		}
		public void setQuestionID(int questionID) {
			QuestionID = questionID;
		}
		public String getQuestionName() {
			return QuestionName;
		}
		public void setQuestionName(String questionName) {
			QuestionName = questionName;
		}
		public int getQuestionGroupID() {
			return QuestionGroupID;
		}
		public void setQuestionGroupID(int questionGroupID) {
			QuestionGroupID = questionGroupID;
		}
		public int getQuestionTypeID() {
			return QuestionTypeID;
		}
		public void setQuestionTypeID(int questionTypeID) {
			QuestionTypeID = questionTypeID;
		}
		public int getIsRequired() {
			return IsRequired;
		}
		public void setIsRequired(int isRequired) {
			IsRequired = isRequired;
		}
		public int getOrdering() {
			return Ordering;
		}
		public void setOrdering(int ordering) {
			Ordering = ordering;
		}
	}
	
	public static class QuestionGroup{
		private int QuestionGroupID;
		private String QuestionGroupName;
		private int Ordering;
		
		public int getQuestionGroupID() {
			return QuestionGroupID;
		}
		public void setQuestionGroupID(int questionGroupID) {
			QuestionGroupID = questionGroupID;
		}
		public String getQuestionGroupName() {
			return QuestionGroupName;
		}
		public void setQuestionGroupName(String questionGroupName) {
			QuestionGroupName = questionGroupName;
		}
		public int getOrdering() {
			return Ordering;
		}
		public void setOrdering(int ordering) {
			Ordering = ordering;
		}
	}
	
	public static class SaleMode{
		private int SaleModeID;
		private String SaleModeName;
		private int PositionPrefix;
		private String PrefixText;
		private String PrefixQueue;
		private boolean isSelected;
		
		public boolean isSelected() {
			return isSelected;
		}
		public void setSelected(boolean isSelected) {
			this.isSelected = isSelected;
		}
		public int getSaleModeID() {
			return SaleModeID;
		}
		public void setSaleModeID(int saleModeID) {
			SaleModeID = saleModeID;
		}
		public String getSaleModeName() {
			return SaleModeName;
		}
		public void setSaleModeName(String saleModeName) {
			SaleModeName = saleModeName;
		}
		public int getPositionPrefix() {
			return PositionPrefix;
		}
		public void setPositionPrefix(int positionPrefix) {
			PositionPrefix = positionPrefix;
		}
		public String getPrefixText() {
			return PrefixText;
		}
		public void setPrefixText(String prefixText) {
			PrefixText = prefixText;
		}
		public String getPrefixQueue() {
			return PrefixQueue;
		}
		public void setPrefixQueue(String prefixQueue) {
			PrefixQueue = prefixQueue;
		}
	}
	
	public static class ProductGroup {
		private int ProductGroupID;
		private String ProductGroupCode;
		private String ProductGroupName;
		private int ProductGroupOrdering;
		private int IsComment;
		private int ProductGroupType;
		private String UpdateDate;

		public void setProductGroupName(String productGroupName) {
			this.ProductGroupName = productGroupName;
		}

		public int getProductGroupOrdering() {
			return ProductGroupOrdering;
		}

		public void setProductGroupOrdering(int productGroupOrdering) {
			this.ProductGroupOrdering = productGroupOrdering;
		}

		public int getIsComment() {
			return IsComment;
		}

		public void setIsComment(int isComment) {
			this.IsComment = isComment;
		}

		public int getProductGroupType() {
			return ProductGroupType;
		}

		public void setProductGroupType(int productGroupType) {
			this.ProductGroupType = productGroupType;
		}

		public String getUpdateDate() {
			return UpdateDate;
		}

		public void setUpdateDate(String updateDate) {
			this.UpdateDate = updateDate;
		}

		public int getProductGroupId() {
			return ProductGroupID;
		}

		public void setProductGroupId(int productGroupId) {
			this.ProductGroupID = productGroupId;
		}

		public String getProductGroupCode() {
			return ProductGroupCode;
		}

		public void setProductGroupCode(String productGroupCode) {
			this.ProductGroupCode = productGroupCode;
		}

		public String getProductGroupName() {
			return ProductGroupName;
		}
	}

	public static class ProductDept {
		private int ProductDeptID;
		private int ProductGroupID;
		private String ProductDeptCode;
		private String ProductDeptName;
		private int ProductDeptOrdering;
		private String UpdateDate;
		
		public int getProductDeptOrdering() {
			return ProductDeptOrdering;
		}
		
		public void setProductDeptOrdering(int productDeptOrdering) {
			ProductDeptOrdering = productDeptOrdering;
		}
		public int getProductDeptID() {
			return ProductDeptID;
		}
		public void setProductDeptID(int productDeptID) {
			ProductDeptID = productDeptID;
		}
		public int getProductGroupID() {
			return ProductGroupID;
		}
		public void setProductGroupID(int productGroupID) {
			ProductGroupID = productGroupID;
		}
		public String getProductDeptCode() {
			return ProductDeptCode;
		}
		public void setProductDeptCode(String productDeptCode) {
			ProductDeptCode = productDeptCode;
		}
		public String getProductDeptName() {
			return ProductDeptName;
		}
		public void setProductDeptName(String productDeptName) {
			ProductDeptName = productDeptName;
		}
		public String getUpdateDate() {
			return UpdateDate;
		}
		public void setUpdateDate(String updateDate) {
			UpdateDate = updateDate;
		}
	}

	public static class CommentProduct{
		private int ProductID;
		private int CommentID;
		public int getProductID() {
			return ProductID;
		}
		public void setProductID(int productID) {
			ProductID = productID;
		}
		public int getCommentID() {
			return CommentID;
		}
		public void setCommentID(int commentID) {
			CommentID = commentID;
		}
	}
	
	public static class Products{
		private int ProductID;
		private int ProductDeptID;
		private int ProductGroupID;
		private String ProductCode;
		private String ProductBarCode;
		private int ProductTypeID;
		private float ProductPricePerUnit;
		private String ProductUnitName;
		private String ProductDesc;
		private int DiscountAllow;
		private int PrinterID;
		private int PrintGroup;
		private int VatType;
		private float VatRate;
		private int HasServiceCharge;
		private int Activate;
		private int IsOutOfStock;
		private int SaleMode1;
		private float ProductPricePerUnit1;
		private int SaleMode2;
		private float ProductPricePerUnit2;
		private int SaleMode3;
		private float ProductPricePerUnit3;
		private int SaleMode4;
		private float ProductPricePerUnit4;
		private int SaleMode5;
		private float ProductPricePerUnit5;
		private String UpdateDate;
		
		
		public float getProductPricePerUnit1() {
			return ProductPricePerUnit1;
		}
		public void setProductPricePerUnit1(float productPricePerUnit1) {
			ProductPricePerUnit1 = productPricePerUnit1;
		}
		public float getProductPricePerUnit2() {
			return ProductPricePerUnit2;
		}
		public void setProductPricePerUnit2(float productPricePerUnit2) {
			ProductPricePerUnit2 = productPricePerUnit2;
		}
		public float getProductPricePerUnit3() {
			return ProductPricePerUnit3;
		}
		public void setProductPricePerUnit3(float productPricePerUnit3) {
			ProductPricePerUnit3 = productPricePerUnit3;
		}
		public float getProductPricePerUnit4() {
			return ProductPricePerUnit4;
		}
		public void setProductPricePerUnit4(float productPricePerUnit4) {
			ProductPricePerUnit4 = productPricePerUnit4;
		}
		public float getProductPricePerUnit5() {
			return ProductPricePerUnit5;
		}
		public void setProductPricePerUnit5(float productPricePerUnit5) {
			ProductPricePerUnit5 = productPricePerUnit5;
		}
		public void setProductPricePerUnit(float productPricePerUnit) {
			ProductPricePerUnit = productPricePerUnit;
		}
		public void setVatRate(float vatRate) {
			VatRate = vatRate;
		}
		public int getProductID() {
			return ProductID;
		}
		public void setProductID(int productID) {
			ProductID = productID;
		}
		public int getActivate() {
			return Activate;
		}
		public void setActivate(int activate) {
			Activate = activate;
		}
		public int getProductDeptID() {
			return ProductDeptID;
		}
		public void setProductDeptID(int productDeptID) {
			ProductDeptID = productDeptID;
		}
		public int getProductGroupID() {
			return ProductGroupID;
		}
		public void setProductGroupID(int productGroupID) {
			ProductGroupID = productGroupID;
		}
		public String getProductCode() {
			return ProductCode;
		}
		public void setProductCode(String productCode) {
			ProductCode = productCode;
		}
		public String getProductBarCode() {
			return ProductBarCode;
		}
		public void setProductBarCode(String productBarCode) {
			ProductBarCode = productBarCode;
		}
		public int getProductTypeID() {
			return ProductTypeID;
		}
		public void setProductTypeID(int productTypeID) {
			ProductTypeID = productTypeID;
		}
		
		public float getProductPricePerUnit() {
			return ProductPricePerUnit;
		}
		public String getProductUnitName() {
			return ProductUnitName;
		}
		public void setProductUnitName(String productUnitName) {
			ProductUnitName = productUnitName;
		}
		public String getProductDesc() {
			return ProductDesc;
		}
		public void setProductDesc(String productDesc) {
			ProductDesc = productDesc;
		}
		public int getDiscountAllow() {
			return DiscountAllow;
		}
		public void setDiscountAllow(int discountAllow) {
			DiscountAllow = discountAllow;
		}
		public int getPrinterID() {
			return PrinterID;
		}
		public void setPrinterID(int printerID) {
			PrinterID = printerID;
		}
		public int getPrintGroup() {
			return PrintGroup;
		}
		public void setPrintGroup(int printGroup) {
			PrintGroup = printGroup;
		}
		public int getVatType() {
			return VatType;
		}
		public void setVatType(int vatType) {
			VatType = vatType;
		}
		public float getVatRate() {
			return VatRate;
		}
		public int getHasServiceCharge() {
			return HasServiceCharge;
		}
		public void setHasServiceCharge(int hasServiceCharge) {
			HasServiceCharge = hasServiceCharge;
		}
		public int getIsOutOfStock() {
			return IsOutOfStock;
		}
		public void setIsOutOfStock(int isOutOfStock) {
			IsOutOfStock = isOutOfStock;
		}
		public int getSaleMode1() {
			return SaleMode1;
		}
		public void setSaleMode1(int saleMode1) {
			SaleMode1 = saleMode1;
		}
		public int getSaleMode2() {
			return SaleMode2;
		}
		public void setSaleMode2(int saleMode2) {
			SaleMode2 = saleMode2;
		}
		public int getSaleMode3() {
			return SaleMode3;
		}
		public void setSaleMode3(int saleMode3) {
			SaleMode3 = saleMode3;
		}
		public int getSaleMode4() {
			return SaleMode4;
		}
		public void setSaleMode4(int saleMode4) {
			SaleMode4 = saleMode4;
		}
		public int getSaleMode5() {
			return SaleMode5;
		}
		public void setSaleMode5(int saleMode5) {
			SaleMode5 = saleMode5;
		}
		public String getUpdateDate() {
			return UpdateDate;
		}
		public void setUpdateDate(String updateDate) {
			UpdateDate = updateDate;
		}
	}

	public static class ProductComponent{
		private int PGroupID;
        private int ProductID;
        private int SaleMode;
        private int ChildProductID;
        private float ChildProductAmount;
        private float FlexibleProductPrice;
        private int FlexibleIncludePrice;
        
		public int getPGroupID() {
			return PGroupID;
		}
		public void setPGroupID(int pGroupID) {
			PGroupID = pGroupID;
		}
		public int getProductID() {
			return ProductID;
		}
		public void setProductID(int productID) {
			ProductID = productID;
		}
		public int getSaleMode() {
			return SaleMode;
		}
		public void setSaleMode(int saleMode) {
			SaleMode = saleMode;
		}
		public int getChildProductID() {
			return ChildProductID;
		}
		public void setChildProductID(int childProductID) {
			ChildProductID = childProductID;
		}
		public float getChildProductAmount() {
			return ChildProductAmount;
		}
		public void setChildProductAmount(float childProductAmount) {
			ChildProductAmount = childProductAmount;
		}
		public float getFlexibleProductPrice() {
			return FlexibleProductPrice;
		}
		public void setFlexibleProductPrice(float flexibleProductPrice) {
			FlexibleProductPrice = flexibleProductPrice;
		}
		public int getFlexibleIncludePrice() {
			return FlexibleIncludePrice;
		}
		public void setFlexibleIncludePrice(int flexibleIncludePrice) {
			FlexibleIncludePrice = flexibleIncludePrice;
		}
	}
	
	public static class PComponentGroup {
		private int PGroupID;
		private int SaleMode;
		private int SetGroupNo;
		private String SetGroupName;
		private float RequireAmount;
		private String MenuImageLink;
		private int totalPCompSet;
		
		public int getTotalPCompSet() {
			return totalPCompSet;
		}
		public void setTotalPCompSet(int totalPCompSet) {
			this.totalPCompSet = totalPCompSet;
		}
		
		public String getMenuImageLink() {
			return MenuImageLink;
		}
		public void setMenuImageLink(String menuImageLink) {
			MenuImageLink = menuImageLink;
		}
		public int getPGroupID() {
			return PGroupID;
		}
		public void setPGroupID(int pGroupID) {
			PGroupID = pGroupID;
		}
		public int getSaleMode() {
			return SaleMode;
		}
		public void setSaleMode(int saleMode) {
			SaleMode = saleMode;
		}
		public int getSetGroupNo() {
			return SetGroupNo;
		}
		public void setSetGroupNo(int setGroupNo) {
			SetGroupNo = setGroupNo;
		}
		public String getSetGroupName() {
			return SetGroupName;
		}
		public void setSetGroupName(String setGroupName) {
			SetGroupName = setGroupName;
		}
		public float getRequireAmount() {
			return RequireAmount;
		}
		public void setRequireAmount(float requireAmount) {
			RequireAmount = requireAmount;
		}
	}
	
	public static class PComponentSet{
		private int PGroupID;
        private int ProductID;
        private int SaleMode;
        private int ChildProductID;
        private float ChildProductAmount;
        private float FlexibleProductPrice;
        private int FlexibleIncludePrice;
        
		public int getPGroupID() {
			return PGroupID;
		}
		public void setPGroupID(int pGroupID) {
			PGroupID = pGroupID;
		}
		public int getProductID() {
			return ProductID;
		}
		public void setProductID(int productID) {
			ProductID = productID;
		}
		public int getSaleMode() {
			return SaleMode;
		}
		public void setSaleMode(int saleMode) {
			SaleMode = saleMode;
		}
		public int getChildProductID() {
			return ChildProductID;
		}
		public void setChildProductID(int childProductID) {
			ChildProductID = childProductID;
		}
		public float getChildProductAmount() {
			return ChildProductAmount;
		}
		public void setChildProductAmount(float childProductAmount) {
			ChildProductAmount = childProductAmount;
		}
		public float getFlexibleProductPrice() {
			return FlexibleProductPrice;
		}
		public void setFlexibleProductPrice(float flexibleProductPrice) {
			FlexibleProductPrice = flexibleProductPrice;
		}
		public int getFlexibleIncludePrice() {
			return FlexibleIncludePrice;
		}
		public void setFlexibleIncludePrice(int flexibleIncludePrice) {
			FlexibleIncludePrice = flexibleIncludePrice;
		}
	}
}
