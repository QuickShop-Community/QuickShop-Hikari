package com.ghostchu.quickshop.converter;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HikariConverter {
    @Getter
    private final QuickShop plugin;
    @Getter
    private final Logger logger = Logger.getLogger("HikariConverter");
    @Getter
    private final List<HikariConverterInterface> converters = new ArrayList<>();

    public HikariConverter(QuickShop plugin) {
        this.plugin = plugin;
    }

    public void upgrade() {
        int selectedVersion = plugin.getConfig().getInt("config-version");
        if (selectedVersion >= 1000) {
            Log.debug("Skipping configuration upgrading (newer than 1k)...");
            return;
        }

        logger.info("Please wait, configuring converter...");
        this.converters.clear();
        this.converters.add(new HikariConfigConverter(this));
        this.converters.add(new HikariDatabaseConverter(this));
        logger.info("Before executing the converter, we're checking for make sure everything getting ready for converting...");
        logger.info("This may need a while...");
        List<Component> errors = new ArrayList<>();

        for (HikariConverterInterface converter : this.converters) {
            try {
                errors.addAll(converter.checkReady());
            } catch (Exception e) {
                plugin.logger().error("Something went wrong when checking ready: " + e.getMessage() + ", converter: " + converter.getClass().getSimpleName(), e);
                errors.add(Component.text("Something went wrong: " + e.getMessage()));
            }
        }

        if (!errors.isEmpty()) {
            logger.info("There are some errors need to resolve before continue converting:");

            for (Component error : errors) {
                logger.info(LegacyComponentSerializer.legacySection().serialize(Component.text("- ").append(error)));
            }
            logger.info("");
            logger.info("You must resolve all errors above to continue the converting!");
            halt();
        }
        UUID uuid = UUID.randomUUID();
        logger.info("This converting process unique id is: " + uuid + ".");
        logger.info("All checks passed! We're creating simple backup for you... (it's really simple and you still need keep your backups before migrating.)");
        File backupFolder = new File(plugin.getDataFolder(), "backup-" + uuid);
        if (!backupFolder.exists() && !backupFolder.mkdirs()) {
            logger.severe("Failed to create backup storage folder, please check your data folder permission!");
            halt();
        }
        for (HikariConverterInterface converter : this.converters) {
            try {
                converter.backup(uuid, backupFolder);
            } catch (Exception exception) {
                logger.log(Level.SEVERE, "Fatal exception occurred when backing up: " + exception.getMessage() + ", converter: " + converter.getClass().getSimpleName(), exception);
                logger.warning("Do you still want continue forcing converting? Create a 'continue.txt' in QuickShop data folder to continue, or close the server and ask the support.");
                requireContinue();
            }
        }
        logger.info("Warning! High Risk Operation alert!");
        logger.info("!! BACKUP YOUR DATA BEFORE CONTINUE !!");
        logger.info("");
        logger.info("QuickShop need upgrade your database and configuration to continue.");
        logger.info("If you don't backup your data, You will have chance will lose all your data.");
        logger.info("If you hadn't backup your data yet, please close the server and backup immediately.");
        logger.info("**Include your map, datafolder, plugins, database, EVERYTHING!**");
        logger.info("");
        logger.info("Perfect! Everything seems get ready for converting, Do you want start the converting right now?");
        logger.info("Once you confirm, the converting process will start, and you can't stop it.");
        logger.info("Create a 'continue.txt' in QuickShop data folder to start the converting!");
        requireContinue();
        logger.info("Starting converting...");
        logger.info("");
        logger.info("");
        logger.info("");
        logger.info("QuickShop Hikari - DataConverter 1.0 by Ghost_chu");
        logger.info("Please do not stop the server while converting, or you may lose all your data!");
        logger.info("");
        logger.info("");
        logger.info("");

        try {
            Thread.sleep(3000); // Give user a chance to kill server before we really start converting
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }

        for (HikariConverterInterface converter : this.converters) {
            logger.info("POST: " + converter.getClass().getName());
            try {
                converter.migrate(uuid);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Unrecoverable error occurred when converting: " + e.getMessage() + ", converter: " + converter.getClass().getSimpleName() + ", this converter works has been interrupted!", e);
                logger.warning("Do you still want continue forcing converting? Create a 'continue.txt' in QuickShop data folder to continue, or close the server and ask the support.");
                requireContinue();
            }
        }
        logger.info("Saving configuration...");
        // End everything
        plugin.getConfig().set("config-version", 1000); // Apollo first version
        plugin.getJavaPlugin().saveConfig();
        plugin.getJavaPlugin().reloadConfig();
        logger.info("Awesome! Seems you data already get ready for Hikari!");
    }

    private void halt() {
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            Runtime.getRuntime().halt(0);
        }
    }

    private void requireContinue() {
        while (true) {
            File file = new File(plugin.getDataFolder(), "continue.txt");
            if (file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
