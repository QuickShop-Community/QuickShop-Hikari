package com.ghostchu.quickshop.util.holder;

import com.ghostchu.quickshop.database.bean.IsolatedScanResult;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class DatabaseStatusHolder {
    private Status status;
    private IsolatedScanResult<Long> dataIds;
    private IsolatedScanResult<Long> shopIds;
    private long reportGeneratedAt;


    public enum Status {
        GOOD,
        MAINTENANCE_REQUIRED,
    }
}



