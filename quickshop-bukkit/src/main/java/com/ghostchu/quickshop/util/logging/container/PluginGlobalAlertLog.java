package com.ghostchu.quickshop.util.logging.container;

import lombok.Data;

@Data
public class PluginGlobalAlertLog {

  private static int v = 1;
  private String content;

  public PluginGlobalAlertLog(final String content) {

    this.content = content;
  }
}
