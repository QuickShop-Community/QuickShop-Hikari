package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.MsgUtil;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SubCommand_About implements CommandHandler<CommandSender> {
    private final QuickShop plugin;
    private final LegacyComponentSerializer serializer;

    public SubCommand_About(QuickShop plugin) {
        this.plugin = plugin;
        this.serializer = LegacyComponentSerializer.legacySection();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        MsgUtil.sendDirectMessage(sender, serializer.deserialize(ChatColor.AQUA + "QuickShop " + ChatColor.YELLOW + QuickShop.getInstance().getFork()));
        MsgUtil.sendDirectMessage(sender, serializer.deserialize(ChatColor.AQUA
                + "Version "
                + ChatColor.YELLOW
                + ">> "
                + ChatColor.GREEN
                + QuickShop.getInstance().getVersion()));
        if (plugin.getBuildInfo().getGitInfo().getBranch().toUpperCase().contains("ORIGIN/LTS")) {
            MsgUtil.sendDirectMessage(sender, serializer.deserialize(
                    ChatColor.AQUA
                            + "Release "
                            + ChatColor.YELLOW
                            + ">> "
                            + ChatColor.GREEN
                            + serializer.serialize(plugin.text().of(sender, "updatenotify.label.lts").forLocale())));
        } else if (plugin.getBuildInfo().getGitInfo().getBranch().toUpperCase().contains("ORIGIN/RELEASE")) {
            MsgUtil.sendDirectMessage(sender,
                    serializer.deserialize(ChatColor.AQUA
                            + "Release "
                            + ChatColor.YELLOW
                            + ">> "
                            + ChatColor.GREEN
                            + serializer.serialize(plugin.text().of(sender, "updatenotify.label.stable").forLocale())));
        } else {
            MsgUtil.sendDirectMessage(sender,
                    serializer.deserialize(ChatColor.AQUA
                            + "Release "
                            + ChatColor.YELLOW
                            + ">> "
                            + ChatColor.GREEN
                            + serializer.serialize(plugin.text().of(sender, "updatenotify.label.unstable").forLocale())));
        }
        MsgUtil.sendDirectMessage(sender,
                serializer.deserialize(ChatColor.AQUA
                        + "Developers "
                        + ChatColor.YELLOW
                        + ">> "
                        + ChatColor.GREEN
                        + CommonUtil.list2String(plugin.getJavaPlugin().getDescription().getAuthors())));
        MsgUtil.sendDirectMessage(sender, serializer.deserialize(ChatColor.GOLD + "Powered by Community"));
        MsgUtil.sendDirectMessage(sender, serializer.deserialize(ChatColor.RED + "Made with ❤"));
//        Util.asyncThreadRun(() -> {
//            try {
//                HttpResponse<String> resp = Unirest.get("https://quickshop-kofi-proxy.ghostchu.workers.dev/").asString();
//                if (resp.isSuccess()) {
//                    MsgUtil.sendDirectMessage(sender, serializer.deserialize(ChatColor.GOLD + "Special thanks to those who support to QuickShop-Hikari on Ko-fi :)"));
//                    List<KoFiDTO> dto = JsonUtil.getGson().fromJson(resp.getBody(), new TypeToken<List<KoFiDTO>>() {
//                    }.getType());
//                    Component message = Component.empty();
//                    boolean first = true;
//                    for (KoFiDTO record : dto) {
//                        if(!first){
//                            message = message.append(Component.text(", ", NamedTextColor.GOLD));
//                        }
//                        message = message.append(Component.text(record.getName(), NamedTextColor.GOLD));
//                        first = false;
//                    }
//                }else{
//                    Log.debug("Failed to retrieve Ko-fi list: " + resp.getStatus());
//                }
//            } catch (Exception err) {
//                Log.debug("Failed to retrieve Ko-fi list: " + err.getMessage());
//            }
//        });


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
