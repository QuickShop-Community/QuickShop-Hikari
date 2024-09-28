package com.ghostchu.quickshop.addon.reremakemigrator.migratecomponent;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.reremakemigrator.Main;
import com.ghostchu.quickshop.util.ProgressMonitor;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TranslationMigrateComponent extends AbstractMigrateComponent {

  public TranslationMigrateComponent(final Main main, final QuickShop hikari, final org.maxgamer.quickshop.QuickShop reremake, final CommandSender sender) {

    super(main, hikari, reremake, sender);
  }

  @Override
  public boolean migrate() {

    text("modules.translation.start-migrate").send();
    final File reremakeOverrideFolder = new File(getReremake().getDataFolder(), "overrides");
    if(!reremakeOverrideFolder.exists()) {
      return true;
    }
    final File[] fileTree = reremakeOverrideFolder.listFiles();
    if(fileTree == null) {
      getHikari().logger().warn("Skipping translation migrate (no file tree)");
      return true;
    }

    for(final File langFolder : new ProgressMonitor<>(fileTree, (triple)->text("modules.translation.migrate-entry", triple.getRight().getName(), triple.getLeft(), triple.getMiddle()))) {
      final File langJsonFile = new File(langFolder, "messages.json");
      if(!langJsonFile.exists()) {
        continue;
      }
      try {
        migrateFile(langFolder, langJsonFile);
      } catch(Exception exception) {
        getHikari().logger().warn("Failed migrate lang file " + langJsonFile.getName() + ".", exception);
      }
    }
    return true;
  }

  private void migrateFile(final File langFolder, final File langJsonFile) throws IOException {

    final File hikariOverrideFolder = new File(getHikari().getDataFolder(), "overrides");
    final File targetLangFolder = new File(hikariOverrideFolder, langFolder.getName());
    if(!targetLangFolder.exists()) {
      if(!targetLangFolder.mkdirs()) {
        throw new IOException("Failed to create lang " + langFolder.getName() + " directory.");
      }
    }
    final JsonConfiguration jsonConfiguration = JsonConfiguration.loadConfiguration(langJsonFile);
    YamlConfiguration yamlConfiguration = new YamlConfiguration();
    final File targetLangFile = new File(targetLangFolder, langJsonFile.getName().replace(".json", ".yml"));
    if(targetLangFile.exists()) {
      yamlConfiguration = YamlConfiguration.loadConfiguration(targetLangFile);
    }
    getHikari().logger().info("Migrating the Reremake language file: " + langJsonFile.getAbsolutePath() + "...");
    final Set<String> keys = jsonConfiguration.getKeys(true);
    text("modules.translation.copy-values", targetLangFolder.getName(), keys.size()).send();
    for(final String key : new ProgressMonitor<>(keys, (triple)->text("modules.translation.copying-value", triple.getRight(), triple.getLeft(), triple.getMiddle()).send())) {
      if("_comment".equals(key)) {
        continue;
      }
      if(jsonConfiguration.isConfigurationSection(key)) {
        continue;
      }
      if(jsonConfiguration.isString(key)) {
        final String originalValue = jsonConfiguration.getString(key);
        final Component component = MineDown.parse(originalValue).compact();
        final String convertedValue = MiniMessage.miniMessage().serialize(component);
        yamlConfiguration.set(key, convertedValue);
      } else {
        if(jsonConfiguration.isList(key)) {
          final List<String> list = jsonConfiguration.getStringList(key);
          final List<String> convert = new ArrayList<>();
          list.forEach(s->{
            final Component component = MineDown.parse(s).compact();
            final String convertedValue = MiniMessage.miniMessage().serialize(component);
            convert.add(convertedValue);
          });
          yamlConfiguration.set(key, convert);
        } else {
          yamlConfiguration.set(key, jsonConfiguration.get(key));
        }
      }
    }
    if(!targetLangFile.exists()) {
      if(!targetLangFile.createNewFile()) {
        getHikari().logger().warn("Couldn't save converted data to file: file creation failed");
        return;
      }
    }
    yamlConfiguration.save(targetLangFile);
  }
}
