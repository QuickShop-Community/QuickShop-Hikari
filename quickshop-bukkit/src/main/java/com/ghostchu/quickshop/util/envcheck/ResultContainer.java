package com.ghostchu.quickshop.util.envcheck;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class ResultContainer {

  private final CheckResult result;
  private String resultMessage;

  public ResultContainer(@NotNull final CheckResult result, @Nullable final String resultMessage) {

    this.result = result;
    this.resultMessage = resultMessage;
    if(StringUtils.isEmpty(this.resultMessage)) {
      this.resultMessage = "null";
    }
  }

}

