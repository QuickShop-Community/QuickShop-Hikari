package com.ghostchu.quickshop.util.privacy;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.metric.MetricDataType;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

public class PrivacyController {

  private final QuickShop plugin;

  public PrivacyController(final QuickShop plugin) {

    this.plugin = plugin;
  }

  private boolean isAllowed(final MetricDataType dataType, final String moduleName, final UUID transaction) {

    final ConfigurationSection section = plugin.getConfig().getConfigurationSection("privacy");
    if(section == null) {
      Log.privacy("[CHECK] Transaction " + transaction + " was declined: `privacy` ROOT section missing.");
      return false;
    }
    final ConfigurationSection dataTypeSection = section.getConfigurationSection("type");
    if(dataTypeSection == null) {
      Log.privacy("[CHECK] Transaction " + transaction + " was declined: `type` section missing.");
      return false;
    }
    final boolean dataTypeResult = dataTypeSection.getBoolean(dataType.name(), false);
    if(!dataTypeResult) {
      Log.privacy("[CHECK] Transaction " + transaction + " was declined: item in `type` section missing.");
      return false;
    }
    final ConfigurationSection moduleSection = section.getConfigurationSection("module");
    if(moduleSection == null) {
      Log.privacy("[CHECK] Transaction " + transaction + " was approved: `module` section not found but " + dataType.name() + " type was explicitly enabled.");
      return true;
    }
    final boolean r = moduleSection.getBoolean(moduleName, true);
    if(r) {
      Log.privacy("[CHECK] Transaction " + transaction + " was approved: module was not explicitly disabled.");
    } else {
      Log.privacy("[CHECK] Transaction " + transaction + " was declined: module was explicitly disabled.");
    }
    return r;
  }

  public void privacyReview(final MetricDataType dataType, final String moduleName, final String reason, final Runnable accepted, final Runnable declined) {

    final UUID privacyTransaction = UUID.randomUUID();
    Log.privacy("[REVIEW] The module [" + moduleName + "] requesting to processing your data off your local machine for usage [" + dataType.name() + "], the reason is: " + reason + ". Transaction Id: " + privacyTransaction);
    if(!isAllowed(dataType, moduleName, privacyTransaction)) {
      Log.privacy("[REVIEW] Declined transaction " + privacyTransaction);
      declined.run();
    } else {
      Log.privacy("[REVIEW] Accepted transaction " + privacyTransaction);
      accepted.run();
    }
  }
}
