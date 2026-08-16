package com.ghostchu.quickshop.util.logging.container;

import java.util.Objects;

public class PluginGlobalAlertLog {

  private static int v = 1;
  private String content;

  public PluginGlobalAlertLog(final String content) {

    this.content = content;
  }

  public String getContent() {

    return this.content;
  }

  public void setContent(final String content) {

    this.content = content;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof PluginGlobalAlertLog)) return false;
    final PluginGlobalAlertLog other = (PluginGlobalAlertLog)o;
    return Objects.equals(this.getContent(), other.getContent());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getContent());
  }

  @Override
  public String toString() {

    return "PluginGlobalAlertLog(content=" + this.getContent() + ")";
  }
}
