package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class SubCommand_About implements CommandHandler<CommandSender> {
    private final QuickShop plugin;
    private KofiFetchCache fetchCache;

    public SubCommand_About(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        String forkName = plugin.getFork();
        String version = plugin.getVersion();
        Component releaseType;
        if (plugin.getBuildInfo().getGitInfo().getBranch().toUpperCase().contains("ORIGIN/LTS")) {
            releaseType =  plugin.text().of(sender, "updatenotify.label.lts").forLocale();
        } else if (plugin.getBuildInfo().getGitInfo().getBranch().toUpperCase().contains("ORIGIN/RELEASE")) {
            releaseType = plugin.text().of(sender, "updatenotify.label.stable").forLocale();
        }else{
            releaseType = plugin.text().of(sender, "updatenotify.label.unstable").forLocale();
        }
        String developers = CommonUtil.list2String(plugin.getJavaPlugin().getDescription().getAuthors());
        String languageCode = plugin.text().findRelativeLanguages(sender).getLocale();
        Component localizedStaffs = plugin.text().of(sender, "translation-author").forLocale();
        final Component donationKey = plugin.text().of(sender, "about.invalid-donation-key").forLocale();
        plugin.text().ofList(sender, "about.text", forkName,version,releaseType,developers,languageCode,localizedStaffs,donationKey).send();
        Util.asyncThreadRun(() -> {
            try {
                String json = fetchKofi();
                if (json != null) {
                    plugin.text().of(sender, "about.kofi-thanks").send();
                    List<KoFiDTO> dto = JsonUtil.getGson().fromJson(json, new TypeToken<List<KoFiDTO>>() {
                    }.getType());
                    Component message = Component.empty();
                    boolean first = true;
                    for (KoFiDTO record : dto) {
                        if (!first) {
                            message = message.append(Component.text(", ", NamedTextColor.GRAY));
                        }
                        message = message.append(Component.text(record.getName(), NamedTextColor.GOLD));
                        first = false;
                    }
                    MsgUtil.sendDirectMessage(sender, message);
                }
            } catch (Exception err) {
                Log.debug("Failed to retrieve Ko-fi list: " + err.getMessage());
            }
        });
    }

    @Nullable
    private String fetchKofi() {
        if (this.fetchCache != null && Instant.now().isBefore(this.fetchCache.getInstant())) {
            return this.fetchCache.getJson();
        }
        HttpResponse<String> resp = Unirest.get("https://quickshop-kofi-proxy.ghostchu.workers.dev/").asString();
        if (resp.isSuccess()) {
            this.fetchCache = new KofiFetchCache(Instant.now().plus(12, ChronoUnit.HOURS), resp.getBody());
            return this.fetchCache.getJson();
        } else {
            Log.debug("Failed to retrieve Ko-fi list: " + resp.getStatus());
            return null;
        }
    }

    static class KofiFetchCache {
        private final Instant instant;
        private final String json;

        KofiFetchCache(@NotNull Instant instant, @NotNull String json) {
            this.instant = instant;
            this.json = json;
        }

        public Instant getInstant() {
            return instant;
        }

        public String getJson() {
            return json;
        }
    }

    @NoArgsConstructor
    @Data
    static class KoFiDTO {
        @SerializedName("id")
        private Integer id;
        @SerializedName("time")
        private String time;
        @SerializedName("type")
        private String type;
        @SerializedName("name")
        private String name;
    }
}
