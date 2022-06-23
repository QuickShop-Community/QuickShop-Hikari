package com.ghostchu.quickshop.database.bean;

import lombok.Data;

import java.util.List;

@Data
public class IsolatedScanResult<T> {
    private final List<T> total;
    private final List<T> isolated;
}
