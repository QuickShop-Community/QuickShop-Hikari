package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import org.apache.commons.text.StringEscapeUtils;
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
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        this.os = System.getProperty("os.name");
        this.arch = System.getProperty("os.arch");
        this.version = System.getProperty("os.version");
        this.cores = String.valueOf(Runtime.getRuntime().availableProcessors());
        this.javaVersion = System.getProperty("java.version") + " (" + System.getProperty("java.vendor.version") + ")";
        this.javaImplName = runtimeMxBean.getVmName();
        this.inputArgs = Util.list2String(runtimeMxBean.getInputArguments());
        this.systemProperties = runtimeMxBean.getSystemProperties().keySet().stream()
                .map(key -> StringEscapeUtils.escapeHtml4(key + "=" + runtimeMxBean.getSystemProperties().get(key)))
                .collect(Collectors.joining("<br/>"));
    }


    @Override
    public @NotNull String getTitle() {
        return "System Information";
    }

    @NotNull
    private String buildContent() {
        HTMLTable table = new HTMLTable(2, true);
        table.insert("OS", os);
        table.insert("Arch", arch);
        table.insert("Version", version);
        table.insert("Cores", cores);
        table.insert("Java Version", javaVersion);
        table.insert("JVM Name", javaImplName);
        table.insert("Input Arguments", inputArgs);
        String propertiesContent = """
                <details>
                  <summary>System Properties (Click to open/close)</summary>
                  {properties}
                </details>
                """;
        if (Util.parsePackageProperly("includeProperties").asBoolean()) {
            table.insert("System Properties", propertiesContent.replace("{properties}", systemProperties));
        }
        return table.render();
    }


    @Override
    public @NotNull String genBody() {
        return buildContent();
    }
}
