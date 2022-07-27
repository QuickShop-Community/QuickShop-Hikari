package com.ghostchu.quickshop.api.event;

/**
 * The status about protection check current status.
 */
public enum ProtectionCheckStatus {
    BEGIN(0),
    END(1);

    final int statusCode;

    ProtectionCheckStatus(int statusCode) {
        this.statusCode = statusCode;
    }
}
