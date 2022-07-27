package com.ghostchu.quickshop.api.database;

public enum ShopOperationEnum {
    /**
     * @deprecated No longer use PURCHASE
     */
    @Deprecated
    PURCHASE,
    PURCHASE_SELLING_SHOP,
    PURCHASE_BUYING_SHOP,
    CREATE,
    DELETE,
    ONGOING_FEE
}
