package com.ghostchu.quickshop.util.envcheck;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@AllArgsConstructor
@Data
public class ResultReport {
    private final CheckResult finalResult;
    private final Map<EnvCheckEntry, ResultContainer> results;
}
