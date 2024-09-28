package com.ghostchu.quickshop.compatibility.openinv;

import com.ghostchu.quickshop.api.command.CommandContainer;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.lishid.openinv.IOpenInv;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public final class Main extends CompatibilityModule implements Listener {

  public IOpenInv openInv;
  public OpenInvInventoryManager manager;

  public OpenInvInventoryManager getManager() {

    return manager;
  }

  public IOpenInv getOpenInv() {

    return openInv;
  }

  @Override
  public void onLoad() {

    super.onLoad();
    openInv = (IOpenInv)Bukkit.getPluginManager().getPlugin("OpenInv");
    manager = new OpenInvInventoryManager(openInv, this);
    getApi().getInventoryWrapperRegistry().register(this, manager);
  }

  @Override
  public void onEnable() {

    super.onEnable();
    getApi().getCommandManager().registerCmd(CommandContainer.builder().prefix("echest").permission("quickshop.echest").description((locale)->LegacyComponentSerializer.legacySection().deserialize(getConfig().getString("messages.description"))).executor(new OpenInvCommand(this)).build());
  }

  @Override
  public void init() {
    // There no init stuffs need to do
  }
}
