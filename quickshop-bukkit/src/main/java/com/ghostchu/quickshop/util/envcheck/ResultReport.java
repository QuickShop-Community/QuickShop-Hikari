package com.ghostchu.quickshop.util.envcheck;

import lombok.Data;

import java.util.Map;

@Data
public class ResultReport {
    private final CheckResult finalResult;
    private final Map<EnvCheckEntry, ResultContainer> results;

    public ResultReport(CheckResult finalResult, Map<EnvCheckEntry, ResultContainer> results) {
        this.finalResult = finalResult;
        this.results = results;
    }
}
