package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.PackageUtil;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.stream.Collectors;

public class SystemInfoItem implements SubPasteItem {

  private final String os;
  private final String arch;
  private final String version;
  private final String cores;
  private final String javaVersion;
  private final String javaImplName;
  private final String inputArgs;
  private final String systemProperties;

  public SystemInfoItem() {

    final RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    this.os = System.getProperty("os.name");
    this.arch = System.getProperty("os.arch");
    this.version = System.getProperty("os.version");
    this.cores = String.valueOf(Runtime.getRuntime().availableProcessors());
    this.javaVersion = System.getProperty("java.version") + " (" + System.getProperty("java.vendor.version") + ")";
    this.javaImplName = runtimeMxBean.getVmName();
    this.inputArgs = CommonUtil.list2String(runtimeMxBean.getInputArguments());
    this.systemProperties = runtimeMxBean.getSystemProperties().keySet().stream()
            .map(key->key + "=" + runtimeMxBean.getSystemProperties().get(key))
            .collect(Collectors.joining("<br/>"));
  }

  @Override
  public @NotNull String genBody() {

    return buildContent();
  }

  @Override
  public @NotNull String getTitle() {

    return "System Information";
  }

  @NotNull
  private String buildContent() {

    final HTMLTable table = new HTMLTable(2, true);
    table.insert("OS", os);
    table.insert("Arch", arch);
    table.insert("Version", version);
    table.insert("Cores", cores);
    table.insert("Java Version", javaVersion);
    table.insert("JVM Name", javaImplName);
    table.insert("Input Arguments", inputArgs);
    final String propertiesContent = """
                               <details>
                                 <summary>System Properties (Click to open/close)</summary>
                                 {properties}
                               </details>
                               """;
    if(PackageUtil.parsePackageProperly("includeProperties").asBoolean()) {
      table.insert("System Properties", propertiesContent.replace("{properties}", systemProperties));
    }
    return table.render();
  }
}
