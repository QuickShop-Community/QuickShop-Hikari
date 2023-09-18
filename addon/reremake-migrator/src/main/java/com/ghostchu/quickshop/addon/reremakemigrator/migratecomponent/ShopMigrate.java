package com.ghostchu.quickshop.addon.reremakemigrator.migratecomponent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.reremakemigrator.Main;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.economy.SimpleBenefit;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.shop.ContainerShop;
import com.ghostchu.quickshop.shop.inventory.BukkitInventoryWrapper;
import com.ghostchu.quickshop.util.ProgressMonitor;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.performance.BatchBukkitExecutor;
import com.google.common.io.Files;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.maxgamer.quickshop.api.shop.Shop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ShopMigrate extends AbstractMigrateComponent {
    private final boolean override;
    private final CommandSender sender;

    public ShopMigrate(Main main, QuickShop hikari, org.maxgamer.quickshop.QuickShop reremake, CommandSender sender, boolean overrideExists) {
        super(main, hikari, reremake);
        this.sender = sender;
        this.override = overrideExists;
    }

    @Override
    public boolean migrate() {
        final AtomicBoolean success = new AtomicBoolean(true);
        final AtomicInteger c = new AtomicInteger(0);
        List<Shop> allShops = getReremake().getShopManager().getAllShops();
        List<ContainerShop> preparedShops = new ArrayList<>();
        getHikari().text().of(sender, "addon.reremake-migrator.modules.shop.start-migrate", allShops.size()).send();
        BatchBukkitExecutor<Shop> batchBukkitExecutor = new BatchBukkitExecutor<>();
        batchBukkitExecutor.addTasks(allShops);
        batchBukkitExecutor.startHandle(getHikari().getJavaPlugin(), reremakeShop -> {
            getHikari().text().of(sender, "addon.reremake-migrator.modules.shop.migrate-entry", reremakeShop.toString(), c.incrementAndGet(), allShops.size()).send();
            try {
                Location shopLoc = reremakeShop.getLocation();
                com.ghostchu.quickshop.api.shop.Shop hikariShop = getHikari().getShopManager().getShop(shopLoc,true);
                if (hikariShop != null) {
                    if (!override) {
                        getHikari().text().of(sender, "addon.reremake-migrator.modules.shop.conflict", "SKIPPING").send();
                        return;
                    } else {
                        getHikari().text().of(sender, "addon.reremake-migrator.modules.shop.conflict", "OVERWRITING").send();
                        getHikari().getShopManager().deleteShop(hikariShop);
                    }
                }
                BlockState block = shopLoc.getBlock().getState();
                if (!(block instanceof Container container)) {
                    getHikari().logger().warn("Shop Invalid: Shop block not a valid Container, failed to create InventoryHolder.");
                    return;
                }
                ContainerShop hikariRawShop = new ContainerShop(
                        getHikari(),
                        -1,
                        shopLoc,
                        reremakeShop.getPrice(),
                        reremakeShop.getItem(),
                        QUserImpl.createSync(getHikari().getPlayerFinder(), reremakeShop.getOwner()),
                        reremakeShop.isUnlimited(),
                        ShopType.fromID(reremakeShop.getShopType().toID()),
                        getReremakeShopExtra(reremakeShop),
                        reremakeShop.getCurrency(),
                        reremakeShop.isDisableDisplay(),
                        reremakeShop.getTaxAccountActual() == null ? null : QUserImpl.createSync(getHikari().getPlayerFinder(), reremakeShop.getTaxAccountActual()),
                        getHikari().getJavaPlugin().getName(),
                        getHikari().getInventoryWrapperManager().mklink(new BukkitInventoryWrapper(container.getInventory())),
                        null,
                        Collections.emptyMap(),
                        new SimpleBenefit()
                );
                hikariRawShop.setDirty();
                preparedShops.add(hikariRawShop);
            } catch (Exception e) {
                getHikari().logger().warn("Failed to migrate shop " + reremakeShop, e);
            }
        }).thenAcceptAsync(a -> {
            unloadAndMoveAwayReremake();
            registerHikariShops(preparedShops);
            saveHikariShops();
        }, QuickExecutor.getCommonExecutor()).exceptionally((error) -> {
            getHikari().logger().warn("Error while migrating shops", error);
            success.set(false);
            return null;
        }).join();
        return success.get();
    }

    private YamlConfiguration getReremakeShopExtra(Shop reremakeShop){
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.loadFromString(reremakeShop.saveExtraToYaml());
        } catch (InvalidConfigurationException ignored) {}
        return configuration;
    }

    private void saveHikariShops() {
        CompletableFuture<?>[] shopsToSaveFuture = getHikari().getShopManager().getAllShops().stream().filter(com.ghostchu.quickshop.api.shop.Shop::isDirty)
                .map(com.ghostchu.quickshop.api.shop.Shop::update)
                .toArray(CompletableFuture[]::new);
        getHikari().text().of(sender, "addon.reremake-migrator.modules.shop.saving-shops", shopsToSaveFuture.length).send();
        CompletableFuture.allOf(shopsToSaveFuture)
                .thenAcceptAsync((v) -> {
                    if (shopsToSaveFuture.length != 0) {
                        Log.debug("Saved " + shopsToSaveFuture.length + " shops in background.");
                    }
                }, QuickExecutor.getShopSaveExecutor())
                .exceptionally(e -> {
                    getHikari().logger().warn("Error while saving shops", e);
                    return null;
                }).join();
    }

    private void registerHikariShops(List<ContainerShop> preparedShops) {
        for (com.ghostchu.quickshop.api.shop.Shop shop : new ProgressMonitor<>(preparedShops, triple -> getHikari().text().of(sender, "addon.reremake-migrator.modules.shop.register-entry", triple.getRight(), triple.getLeft(), triple.getMiddle()).send())) {
            getHikari().getShopManager().registerShop(shop, true).join();
            shop.setDirty();
        }
    }

    private void unloadAndMoveAwayReremake() {
        getHikari().text().of(sender, "addon.reremake-migrator.modules.shop.unloading-reremake").send();
        Bukkit.getPluginManager().disablePlugin(getReremake());
        File reremakeDataDirectory = new File(getHikari().getDataFolder(), "QuickShop");
        try {
            Files.move(reremakeDataDirectory, new File(getHikari().getDataFolder(), "QuickShop.migrated"));
        } catch (IOException e) {
            getHikari().logger().warn("Failed to move QuickShop-Reremake data directory, it may cause issues. You should manually move it to another location.");
        }
    }
}
