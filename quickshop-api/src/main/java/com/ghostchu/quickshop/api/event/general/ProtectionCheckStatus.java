package com.ghostchu.quickshop.api.event.general;

/**
 * The status about protection check current status.
 */
public enum ProtectionCheckStatus {
  BEGIN(0),
  END(1);

  final int statusCode;

  ProtectionCheckStatus(final int statusCode) {

    this.statusCode = statusCode;
  }
}
