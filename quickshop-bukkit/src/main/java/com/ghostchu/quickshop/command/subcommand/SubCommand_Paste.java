package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.paste.Paste;
import com.ghostchu.quickshop.util.paste.PasteGenerator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class SubCommand_Paste implements CommandHandler<CommandSender> {

    private static final List<String> warningPluginList = List.of(
            "ConsoleSpamFix",
            "ConsoleFilter",
            "LogFilter"
    );
    private final QuickShop plugin;

    public SubCommand_Paste(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        // do actions
        Util.asyncThreadRun(() -> {
            for (String s : warningPluginList) {
                if (Bukkit.getPluginManager().getPlugin(s) != null) {
                    if (parser.getArgs().stream().noneMatch(str -> str.contains("--force"))) {
                        plugin.text().of(sender, "paste-warning-plugin-find", s).send();
                        return;
                    }
                }
            }
            if (parser.getArgs().stream().anyMatch(str -> str.contains("file"))) {
                pasteToLocalFile(sender);
                return;
            }
            plugin.text().of(sender, "paste-uploading").send();
            if (!pasteToPastebin(sender)) {
                plugin.text().of(sender, "paste-upload-failed-local").send();
                pasteToLocalFile(sender);
            }
        });
    }

    private boolean pasteToLocalFile(@NotNull CommandSender sender) {
        File file = new File(plugin.getDataFolder(), "paste");
        file.mkdirs();
        file = new File(file, "paste-" + UUID.randomUUID().toString().replace("-", "") + ".html");
        final String string = new PasteGenerator(sender).render();
        try {
            boolean createResult = file.createNewFile();
            Log.debug("Create paste file: " + file.getCanonicalPath() + " " + createResult);
            try (FileWriter fwriter = new FileWriter(file)) {
                fwriter.write(string);
                fwriter.flush();
            }
            plugin.text().of(sender, "paste-created-local", file.getAbsolutePath()).send();
            return true;
        } catch (IOException e) {
            if (plugin.getSentryErrorReporter() != null) {
                plugin.getSentryErrorReporter().ignoreThrow();
            }
            plugin.logger().warn("Failed to save paste locally! The content will be send to the console", e);
            plugin.text().of("paste-created-local-failed").send();
            return false;
        }
    }

    private boolean pasteToPastebin(@NotNull CommandSender sender) {
        final String string = Paste.paste(new PasteGenerator(sender).render());
        if (string != null) {
            String url = "https://ghost-chu.github.io/quickshop-hikari-paste-viewer/?remote=" + URLEncoder.encode(string, StandardCharsets.UTF_8);
            Component component = plugin.text().of(sender, "paste-created", url).forLocale();
            component = component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, url));
            MsgUtil.sendDirectMessage(sender, component);
            if ("zh_cn".equalsIgnoreCase(MsgUtil.getDefaultGameLanguageCode()) || Locale.getDefault().equals(Locale.CHINA)) {
                plugin.text().of(sender, "paste-451").send();
            }
            return true;
        }
        return false;
    }


}
