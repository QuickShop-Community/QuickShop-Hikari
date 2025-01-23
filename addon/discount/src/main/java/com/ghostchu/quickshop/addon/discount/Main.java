package com.ghostchu.quickshop.addon.discount;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.discount.command.DiscountCommand;
import com.ghostchu.quickshop.addon.discount.listener.MainListener;
import com.ghostchu.quickshop.api.command.CommandContainer;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

public final class Main extends JavaPlugin implements Listener {

  static Main instance;
  private final DiscountStatusManager discountStatusManager = new DiscountStatusManager();
  private QuickShop plugin;
  private DiscountCodeManager codeManager;

  @Override
  public void onLoad() {

    instance = this;
  }

  @Override
  public void onDisable() {

    codeManager.saveDatabase();
    HandlerList.unregisterAll((Plugin)this);
  }

  @Override
  public void onEnable() {

    saveDefaultConfig();
    plugin = QuickShop.getInstance();
    try {
      codeManager = new DiscountCodeManager(this);
    } catch(IOException e) {
      getLogger().log(Level.WARNING, "Unable to init discount code manager.", e);
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }
    getLogger().info("Registering the per shop permissions...");
    plugin.getShopPermissionManager().registerPermission(BuiltInShopPermissionGroup.ADMINISTRATOR.getNamespacedNode(), this, "discount_code_create");
    plugin.getShopPermissionManager().registerPermission(BuiltInShopPermissionGroup.ADMINISTRATOR.getNamespacedNode(), this, "discount_code_use");
    plugin.getShopPermissionManager().registerPermission(BuiltInShopPermissionGroup.EVERYONE.getNamespacedNode(), this, "discount_code_use");
    plugin.getShopPermissionManager().registerPermission(BuiltInShopPermissionGroup.STAFF.getNamespacedNode(), this, "discount_code_use");
    QuickShop.folia().getImpl().runTimerAsync(()->codeManager.cleanExpiredCodes(), 1L, 20 * 60 * 30);
    QuickShop.folia().getImpl().runTimerAsync(()->codeManager.saveDatabase(), 1L, 20 * 60 * 15);
    getLogger().info("Registering the listeners...");
    Bukkit.getPluginManager().registerEvents(this, this);
    Bukkit.getPluginManager().registerEvents(new MainListener(this), this);
    plugin.getCommandManager().registerCmd(
            CommandContainer
                    .builder()
                    .prefix("discount")
                    .description((locale)->plugin.text().of("addon.discount.commands.discount.description").forLocale(locale))
                    .permission("quickshopaddon.discount.use")
                    .executor(new DiscountCommand(this, plugin))
                    .build());
  }

  public DiscountStatusManager getStatusManager() {

    return discountStatusManager;
  }

  public DiscountCodeManager getCodeManager() {

    return codeManager;
  }
}
