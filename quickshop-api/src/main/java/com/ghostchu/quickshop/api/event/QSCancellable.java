package com.ghostchu.quickshop.api.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.Nullable;

/**
 * Parent class for all cancellable events.
 */
public interface QSCancellable extends Cancellable {
    @Override
    @Deprecated
    default void setCancelled(boolean cancel) {
        setCancelled(cancel, (Component) null);
    }

    default void setCancelled(boolean cancel, @Nullable String reason) {
        if (reason == null) {
            setCancelled(cancel, (Component) null);
            return;
        }
        setCancelled(cancel, LegacyComponentSerializer.legacySection().deserialize(reason));
    }

    void setCancelled(boolean cancel, @Nullable Component reason);

    @Nullable
    Component getCancelReason();
}
