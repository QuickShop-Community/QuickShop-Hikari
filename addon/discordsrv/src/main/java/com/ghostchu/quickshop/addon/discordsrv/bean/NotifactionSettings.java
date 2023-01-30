package com.ghostchu.quickshop.addon.discordsrv.bean;

import java.util.Map;

public record NotifactionSettings(Map<NotifactionFeature, Boolean> settings) {
}
