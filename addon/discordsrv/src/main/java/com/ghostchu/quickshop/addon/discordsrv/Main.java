package com.ghostchu.quickshop.addon.discordsrv;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.ShopSuccessPurchaseEvent;
import com.ghostchu.quickshop.util.Util;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.commands.PluginSlashCommand;
import github.scarsz.discordsrv.api.commands.SlashCommandProvider;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;

public final class Main extends JavaPlugin implements Listener, SlashCommandProvider {
    private QuickShop plugin;
    private MessageFactory factory;

    @Override
    public void onEnable() {
        plugin = QuickShop.getInstance();
        factory = new MessageFactory(plugin);
        Bukkit.getPluginManager().registerEvents(this, this);
        DiscordSRV.api.subscribe(this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
        DiscordSRV.api.unsubscribe(this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPurchase(ShopSuccessPurchaseEvent event) {
        if (event.getShop().isUnlimited() && plugin.getConfig().getBoolean("shop.ignore-unlimited-shop-messages")) {
            return;
        }
        Util.asyncThreadRun(() -> {
            // Build discord message
            MessageEmbed embed;
            if (event.getShop().isSelling()) {
                embed = factory.somebodyBoughtItemsToYourShop(event.getShop(), event.getPurchaser(), event.getShop().getLocation().getWorld(), event.getAmount(), event.getBalance(), event.getTax());
            } else {
                embed = factory.somebodySellItemsToYourShop(event.getShop(), event.getPurchaser(), event.getShop().getLocation().getWorld(), event.getAmount(), event.getBalance(), event.getTax());
            }
            sendDiscordDM(event.getShop().getOwner(), embed);
            if (event.getShop().isSelling()) {
                if (event.getShop().getRemainingStock() == 0) {
                    sendDiscordDM(event.getShop().getOwner(), factory.runOutOfStock(event.getShop()));
                }
            } else {
                if (event.getShop().getRemainingSpace() == 0) {
                    sendDiscordDM(event.getShop().getOwner(), factory.runOutOfSpace(event.getShop()));
                }
            }
        });
    }

    public void sendDiscordDM(UUID receiver, Message msg) {
        String discordId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(receiver);
        if (discordId == null) {
            return;
        }
        User user = DiscordUtil.getJda().getUserById(discordId);
        if (user == null) {
            return;
        }
        user.openPrivateChannel().queue((channel) -> channel.sendMessage(msg).queue());
    }

    public void sendDiscordDM(UUID receiver, MessageEmbed msg) {
        String discordId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(receiver);
        if (discordId == null) {
            return;
        }
        User user = DiscordUtil.getJda().getUserById(discordId);
        if (user == null) {
            return;
        }
        user.openPrivateChannel().queue((channel) -> channel.sendMessageEmbeds(msg).queue());
    }


    @Override
    public Set<PluginSlashCommand> getSlashCommands() {
        return null;
    }
}
