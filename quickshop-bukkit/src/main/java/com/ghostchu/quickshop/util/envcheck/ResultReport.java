package com.ghostchu.quickshop.util.envcheck;

import java.util.Map;
import java.util.Objects;

public class ResultReport {

  private final CheckResult finalResult;
  private final Map<EnvCheckEntry, ResultContainer> results;

  public ResultReport(final CheckResult finalResult, final Map<EnvCheckEntry, ResultContainer> results) {

    this.finalResult = finalResult;
    this.results = results;
  }

  public CheckResult getFinalResult() {

    return this.finalResult;
  }

  public Map<EnvCheckEntry, ResultContainer> getResults() {

    return this.results;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof ResultReport)) return false;
    final ResultReport other = (ResultReport)o;
    return Objects.equals(this.getFinalResult(), other.getFinalResult())
           && Objects.equals(this.getResults(), other.getResults());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getFinalResult(), this.getResults());
  }

  @Override
  public String toString() {

    return "ResultReport(finalResult=" + this.getFinalResult() + ", results=" + this.getResults() + ")";
  }
}
