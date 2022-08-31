package com.ghostchu.quickshop.util.reporter.error;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.GameVersion;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.google.common.collect.Lists;
import com.rollbar.notifier.Rollbar;
import com.rollbar.notifier.config.Config;
import com.rollbar.notifier.config.ConfigBuilder;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidPluginException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class RollbarErrorReporter {
    //private volatile static String bootPaste = null;
    private final Rollbar rollbar;
    private final List<String> reported = new ArrayList<>(5);
    private final List<Class<?>> ignoredException = Lists.newArrayList(IOException.class
            , OutOfMemoryError.class
            , ProtocolException.class
            , InvalidPluginException.class
            , UnsupportedClassVersionError.class
            , LinkageError.class);
    private final QuickShop plugin;
    private final QuickShopExceptionFilter quickShopExceptionFilter;
    private final GlobalExceptionFilter serverExceptionFilter;
    private final LinkedBlockingQueue<ErrorBundle> reportQueue = new LinkedBlockingQueue<>();
    private boolean disable;
    private boolean tempDisable;
    //private final GlobalExceptionFilter globalExceptionFilter;
    @Getter
    private volatile boolean enabled;


    public RollbarErrorReporter(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        Config config = ConfigBuilder.withAccessToken("aeace9eab9e042dfb43d97d39728e19c")
                .environment(Util.isDevEdition() ? "development" : "production")
                .platform(plugin.getServer().getVersion())
                .codeVersion(QuickShop.getVersion())
                .handleUncaughtErrors(false)
                .build();
        this.rollbar = Rollbar.init(config);

        quickShopExceptionFilter = new QuickShopExceptionFilter(plugin.getLogger().getFilter());
        plugin.getLogger().setFilter(quickShopExceptionFilter); // Redirect log request passthrough our error catcher.

        serverExceptionFilter = new GlobalExceptionFilter(plugin.getLogger().getFilter());
        plugin.getServer().getLogger().setFilter(serverExceptionFilter);

        Log.debug("Rollbar error reporter success loaded.");
        enabled = true;

        Thread asyncErrorReportThread = new Thread(() -> {
            ErrorBundle errorBundle;
            while (enabled) {
                try {
                    errorBundle = reportQueue.take();
                    Log.debug("Sending error: " + errorBundle.getThrowable().getMessage()
                            + " with context: " + CommonUtil.array2String(errorBundle.getContext()));
                    sendError0(errorBundle.getThrowable(), errorBundle.getContext());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        asyncErrorReportThread.setDaemon(true);
        asyncErrorReportThread.start();
    }

    /**
     * Dupe report check
     *
     * @param throwable Throws
     * @return dupecated
     */
    public boolean canReport(@NotNull Throwable throwable) {
        if (!enabled) {
            return false;
        }
        if (plugin.getUpdateWatcher() == null) {
            return false;
        }
        try {
            if (!plugin.getNexusManager().isLatest()) { // We only receive latest reports.
                return false;
            }
        } catch (Exception exception) {
            Log.debug("Cannot to check reportable: " + exception.getMessage());
            return false;
        }
        if (!GameVersion.get(plugin.getPlatform().getMinecraftVersion()).isCoreSupports()) { // Ignore errors if user install quickshop on unsupported
            // version.
            return false;
        }

        PossiblyLevel possiblyLevel = checkWasCauseByQS(throwable);
        if (possiblyLevel != PossiblyLevel.CONFIRM) {
            return false;
        }
        if (throwable.getMessage().startsWith("#")) {
            return false;
        }
        StackTraceElement stackTraceElement;
        if (throwable.getStackTrace().length < 3) {
            stackTraceElement = throwable.getStackTrace()[1];
        } else {
            stackTraceElement = throwable.getStackTrace()[2];
        }
        if (stackTraceElement.getClassName().contains("com.ghostchu.quickshop.util.reporter.error")) {
            ignoreThrows();
            plugin.getLogger().log(Level.WARNING, "Uncaught exception in ErrorRollbar", throwable);
            resetIgnores();
            return false;
        }
        String text =
                stackTraceElement.getClassName()
                        + "#"
                        + stackTraceElement.getMethodName()
                        + "#"
                        + stackTraceElement.getLineNumber();
        if (!reported.contains(text)) {
            reported.add(text);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check a throw is cause by QS
     *
     * @param throwable Throws
     * @return Cause or not
     */
    public PossiblyLevel checkWasCauseByQS(@Nullable Throwable throwable) {
        if (throwable == null) {
            return PossiblyLevel.IMPOSSIBLE;
        }
        if (throwable.getMessage() == null) {
            return PossiblyLevel.IMPOSSIBLE;
        }
        if (throwable.getMessage().contains("Could not pass event")) {
            if (throwable.getMessage().contains("QuickShop")) {
                return PossiblyLevel.CONFIRM;
            } else {
                return PossiblyLevel.IMPOSSIBLE;
            }
        }
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
        }

        StackTraceElement[] stackTraceElements = throwable.getStackTrace();

        if (stackTraceElements.length == 0) {
            return PossiblyLevel.IMPOSSIBLE;
        }

        if (stackTraceElements[0].getClassName().contains("com.ghostchu.quickshop") && stackTraceElements[1].getClassName().contains("com.ghostchu.quickshop")) {
            return PossiblyLevel.CONFIRM;
        }

        long errorCount = Arrays.stream(stackTraceElements)
                .limit(3)
                .filter(stackTraceElement -> stackTraceElement.getClassName().contains("com.ghostchu.quickshop"))
                .count();

        if (errorCount > 0) {
            return PossiblyLevel.MAYBE;
        } else if (throwable.getCause() != null) {
            return checkWasCauseByQS(throwable.getCause());
        }
        return PossiblyLevel.IMPOSSIBLE;
    }

    /**
     * Set ignore throw. It will unlocked after accept a throw
     */
    public void ignoreThrow() {
        tempDisable = true;
    }

    /**
     * Set ignore throws. It will unlocked after called method resetIgnores.
     */
    public void ignoreThrows() {
        disable = true;
    }

    /**
     * Send a error to Sentry
     *
     * @param throwable Throws
     * @param context   BreadCrumb
     */
    public void sendError(@NotNull Throwable throwable, @NotNull String... context) {
        this.reportQueue.offer(new ErrorBundle(throwable, context));
    }

    /**
     * Reset ignore throw(s).
     */
    public void resetIgnores() {
        tempDisable = false;
        disable = false;
    }

    private void sendError0(@NotNull Throwable throwable, @NotNull String... context) {
        if (Bukkit.isPrimaryThread()) {
            plugin.getLogger().warning("Cannot send error on primary thread (I/O blocking). This error has been discard.");
            return;
        }
        try {
            if (plugin.getBootError() != null) {
                return; // Don't report any errors if boot failed.
            }
            if (tempDisable) {
                this.tempDisable = false;
                return;
            }
            if (disable) {
                return;
            }
            if (!enabled) {
                return;
            }

            if (!canReport(throwable)) {
                return;
            }
            if (ignoredException.contains(throwable.getClass())) {
                return;
            }
            if (throwable.getCause() != null) {
                if (ignoredException.contains(throwable.getCause().getClass())) {
                    return;
                }
            }
            this.rollbar.error(throwable, this.makeMapping(), throwable.getMessage());
            plugin
                    .getLogger()
                    .warning(
                            "A exception was thrown, QuickShop already caught this exception and reported it. This error will only shown once before next restart.");
            plugin.getLogger().warning("====QuickShop Error Report BEGIN===");
            plugin.getLogger().warning("Description: " + throwable.getMessage());
            plugin.getLogger().warning("Server   ID: " + plugin.getServerUniqueID());
            plugin.getLogger().warning("Exception  : ");
            ignoreThrows();
            throwable.printStackTrace();
            resetIgnores();
            plugin.getLogger().warning("====QuickShop Error Report E N D===");
            Log.debug(throwable.getMessage());
            Arrays.stream(throwable.getStackTrace()).forEach(a -> Log.debug(a.getClassName() + "." + a.getMethodName() + ":" + a.getLineNumber()));
            if (Util.isDevMode()) {
                throwable.printStackTrace();
            }
        } catch (Exception th) {
            ignoreThrow();
            plugin.getLogger().log(Level.WARNING, "Something going wrong when automatic report errors, please submit this error on Issue Tracker", th);
        }
    }

    private Map<String, Object> makeMapping() {
        Map<String, Object> dataMapping = new LinkedHashMap<>();
        dataMapping.put("system_os", System.getProperty("os.name"));
        dataMapping.put("system_arch", System.getProperty("os.arch"));
        dataMapping.put("system_version", System.getProperty("os.version"));
        dataMapping.put("system_cores", String.valueOf(Runtime.getRuntime().availableProcessors()));
        dataMapping.put("server_build", plugin.getServer().getVersion());
        dataMapping.put("server_java", String.valueOf(System.getProperty("java.version")));
        dataMapping.put("server_players", plugin.getServer().getOnlinePlayers().size() + "/" + plugin.getServer().getMaxPlayers());
        dataMapping.put("server_onlinemode", String.valueOf(plugin.getServer().getOnlineMode()));
        dataMapping.put("server_bukkitversion", plugin.getServer().getVersion());
        dataMapping.put("user", QuickShop.getInstance().getServerUniqueID().toString());
        return dataMapping;
    }

    public void unregister() {
        enabled = false;
        plugin.getLogger().setFilter(quickShopExceptionFilter.preFilter);
        plugin.getServer().getLogger().setFilter(serverExceptionFilter.preFilter);
        //Logger.getGlobal().setFilter(globalExceptionFilter.preFilter);
    }

    @Data
    static class ErrorBundle {
        private final Throwable throwable;
        private final String[] context;

        public ErrorBundle(Throwable throwable, String[] context) {
            this.throwable = throwable;
            this.context = context;
        }
    }

//    private String getPluginInfo() {
//        StringBuilder buffer = new StringBuilder();
//        for (Plugin bPlugin : plugin.getServer().getPluginManager().getPlugins()) {
//            buffer
//                    .append("\t")
//                    .append(bPlugin.getName())
//                    .append("@")
//                    .append(bPlugin.isEnabled() ? "Enabled" : "Disabled")
//                    .append("\n");
//        }
//        return buffer.toString();
//    }

    class GlobalExceptionFilter implements Filter {

        @Nullable
        @Getter
        private final Filter preFilter;

        GlobalExceptionFilter(@Nullable Filter preFilter) {
            this.preFilter = preFilter;
        }

        /**
         * Check if a given log record should be published.
         *
         * @param rec a LogRecord
         * @return true if the log record should be published.
         */
        @Override
        public boolean isLoggable(@NotNull LogRecord rec) {
            if (!enabled) {
                return defaultValue(rec);
            }
            Level level = rec.getLevel();
            if (level != Level.WARNING && level != Level.SEVERE) {
                return defaultValue(rec);
            }
            if (rec.getThrown() == null) {
                return defaultValue(rec);
            }
            if (Util.isDevMode()) {
                sendError(rec.getThrown(), rec.getMessage());
                return defaultValue(rec);
            } else {
                sendError(rec.getThrown(), rec.getMessage());
                PossiblyLevel possiblyLevel = checkWasCauseByQS(rec.getThrown());
                if (possiblyLevel == PossiblyLevel.IMPOSSIBLE) {
                    return true;
                }
                if (possiblyLevel == PossiblyLevel.MAYBE) {
                    plugin.getLogger().warning("This seems not a QuickShop error. If you have any question, you should ask QuickShop developer.");
                    return true;
                }
                return false;
            }
        }

        private boolean defaultValue(LogRecord rec) {
            return preFilter == null || preFilter.isLoggable(rec);
        }

    }

    class QuickShopExceptionFilter implements Filter {
        @Nullable
        @Getter
        private final Filter preFilter;

        QuickShopExceptionFilter(@Nullable Filter preFilter) {
            this.preFilter = preFilter;
        }

        /**
         * Check if a given log record should be published.
         *
         * @param rec a LogRecord
         * @return true if the log record should be published.
         */
        @Override
        public boolean isLoggable(@NotNull LogRecord rec) {
            if (!enabled) {
                return defaultValue(rec);
            }
            Level level = rec.getLevel();
            if (level != Level.WARNING && level != Level.SEVERE) {
                return defaultValue(rec);
            }
            if (rec.getThrown() == null) {
                return defaultValue(rec);
            }
            if (Util.isDevMode()) {
                sendError(rec.getThrown(), rec.getMessage());
                return defaultValue(rec);
            } else {
                sendError(rec.getThrown(), rec.getMessage());
                PossiblyLevel possiblyLevel = checkWasCauseByQS(rec.getThrown());
                if (possiblyLevel == PossiblyLevel.IMPOSSIBLE) {
                    return true;
                }
                if (possiblyLevel == PossiblyLevel.MAYBE) {
                    plugin.getLogger().warning("This seems not a QuickShop error. If you have any question, you may can ask QuickShop developer but don't except any solution.");
                    return true;
                }
                return false;
            }
        }

        private boolean defaultValue(LogRecord rec) {
            return preFilter == null || preFilter.isLoggable(rec);
        }

    }

    enum PossiblyLevel {
        CONFIRM,
        MAYBE,
        IMPOSSIBLE
    }
}
