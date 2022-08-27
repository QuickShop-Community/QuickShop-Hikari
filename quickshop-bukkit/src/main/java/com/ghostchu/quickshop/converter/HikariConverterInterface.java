package com.ghostchu.quickshop.converter;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.UUID;

public interface HikariConverterInterface {

    /**
     * Start for backing up
     *
     * @param actionId Action Identifier for this upgrade operation.
     * @param folder   The target folder for backup.
     * @throws Exception Backup fails.
     */
    void backup(@NotNull UUID actionId, @NotNull File folder) throws Exception;

    /**
     * Returns empty for ready, any elements inside will mark as not ready and will be post to users.
     *
     * @return The element about not ready.
     * @throws Exception Any exception throws will mark as unready and will show to users.
     */
    @NotNull
    List<Component> checkReady() throws Exception;

    /**
     * Start the migrating
     *
     * @param actionId Action Identifier for this upgrade operation.
     * @throws IllegalStateException Not ready.
     * @throws Exception             Migrate operation fails.
     */
    void migrate(@NotNull UUID actionId) throws Exception;
}
