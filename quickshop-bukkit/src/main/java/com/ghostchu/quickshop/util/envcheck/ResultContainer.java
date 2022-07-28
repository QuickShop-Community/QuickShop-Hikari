package com.ghostchu.quickshop.util.envcheck;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResultContainer {
    private final CheckResult result;
    private String resultMessage;

    public ResultContainer(@NotNull CheckResult result, @Nullable String resultMessage) {
        this.result = result;
        this.resultMessage = resultMessage;
        if (StringUtils.isEmpty(this.resultMessage)) {
            this.resultMessage = "null";
        }
    }

    public CheckResult getResult() {
        return result;
    }

    public String getResultMessage() {
        return resultMessage;
    }
}

