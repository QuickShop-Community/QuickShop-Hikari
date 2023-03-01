package com.ghostchu.quickshop.addon.discordsrv.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationSettings {
    private Map<NotificationFeature, Boolean> settings;
}
