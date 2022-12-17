package com.ghostchu.quickshop.addon.discount.type;

public enum CodeCreationResponse {
    SUCCESS,
    REGEX_FAILURE,
    CODE_EXISTS,
    INVALID_USAGE,
    INVALID_RATE,
    INVALID_THRESHOLD,
    INVALID_EXPIRE_TIME,
    PERMISSION_DENIED
}
