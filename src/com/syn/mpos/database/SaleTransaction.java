package com.syn.mpos.database;

import java.util.List;

public class SaleTransaction {
    public class POSData_SaleTransaction
    {
        public SaleData_SessionInfo xSessionInfo;
        public List<SaleData_SaleTransaction> xArySaleTransaction;
    }

    public class SaleData_SessionInfo
    {
        public SaleTable_Session xTableSession;
        public SaleTable_SessionEndDay xTableSessionEndDay;
    }

    public class SaleTable_Session
    {
        public int iSessionID;
        public int iComputerID;
        public int iShopID;
        public String dtSessionDate;
        public String dtOpenSessionDateTime;
        public String dtCloseSessionDateTime;
        public Double fOpenSessionAmount;
        public Double fCloseSessionAmount;
        public int iIsEndDaySession;
    }

    public class SaleTable_SessionEndDay
    {
        public String dtSessionDate;
        public String dtEndDayDateTime;
        public int iTotalQtyReceipt;
        public Double fTotalAmountReceipt;
    }
    
    public class SaleData_SaleTransaction
    {
        public SaleTable_OrderTransaction xOrderTransaction;
        public List<SaleTable_OrderDetail> xAryOrderDetail;
        public List<SaleTable_OrderPromotion> xAryOrderPromotion;
        public List<SaleTable_PaymentDetail> xAryPaymentDetail;
    }

    public class SaleTable_OrderTransaction
    {
        public int iTransactionID;
        public int iComputerID;
        public int iShopID;
        public int iOpenStaffID;
        public String dtOpenTime;
        public String dtCloseTime;
        public int iSaleMode;
        public String szQueueName;
        public int iNoCust;
        public int iDocType;
        public int iTransactionStatusID;
        public int iReceiptYear;
        public int iReceiptMonth;
        public int iReceiptID;
        public String szReceiptNo;
        public String dtSaleDate;
        public Double fTransVAT;
        public Double fServiceCharge;
        public Double fServiceChargeVAT;
        public Double fTransactionVatable;
        public Double fVatPercent;
        public Double fServiceChargePercent;
        public int iIsCalcServiceCharge;
        public int iSessionID;
        public int iVoidStaffID;
        public String szVoidReason;
        public String dtVoidTime;
        public int iMemberID;
        public String szTransactionNote;
    }
    
    public class SaleTable_OrderDetail
    {
        public int iOrderDetailID;
        public int iTransactionID;
        public int iComputerID;
        public int iShopID;
        public int iProductID;
        public int iProductTypeID;
        public int iSaleMode;
        public Double fQty;
        public Double fPricePerUnit;
        public Double fRetailPrice;
        public Double fSalePrice;
        public Double fTotalVatAmount;
        public Double fMemberDiscountAmount;
        public Double fPriceDiscountAmount;
        public int iParentOrderDetailID;
    }

    public class SaleTable_OrderPromotion
    {
        public int iOrderDetailID;
        public int iTransactionID;
        public int iComputerID;
        public int iShopID;
        public int iDiscountTypeID;
        public int iPromotionID;
        public Double fDiscountPrice;
        public Double fPriceAfterDiscount;
    }

    public class SaleTable_PaymentDetail
    {
        public int iPaymentDetailID;
        public int iTransactionID;
        public int iComputerID;
        public int iShopID;
        public int iPayTypeID;
        public Double fPayAmount;
        public String szCreditCardNo;
        public int iExpireMonth;
        public int iExpireYear;
        public int iBankNameID;
        public int iCreditCardType;
        public Double fPaymentVat;
        public String szRemark;
    }
}
