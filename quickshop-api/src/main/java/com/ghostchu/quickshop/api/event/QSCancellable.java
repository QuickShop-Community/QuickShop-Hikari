package com.ghostchu.quickshop.api.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.Nullable;

/**
 * Parent class for all cancellable events.
 */
public interface QSCancellable extends Cancellable {

  @Nullable
  Component getCancelReason();

  default void setCancelled(final boolean cancel, @Nullable final String reason) throws IllegalStateException {

    if(reason == null) {

      setCancelled(cancel, (Component)null);
      return;
    }
    setCancelled(cancel, LegacyComponentSerializer.legacySection().deserialize(reason));
  }

  void setCancelled(boolean cancel, @Nullable Component reason) throws IllegalStateException;

  @Override
  @Deprecated
  default void setCancelled(final boolean cancel) throws IllegalStateException {

    setCancelled(cancel, (Component)null);
  }
}
