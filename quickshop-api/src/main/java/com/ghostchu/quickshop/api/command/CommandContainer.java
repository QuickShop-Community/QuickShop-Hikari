package com.ghostchu.quickshop.api.command;

import com.ghostchu.quickshop.api.CommonUtil;
import com.ghostchu.quickshop.api.QuickShopAPI;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Builds a CommandContainer that can be registered into CommandManager.
 *
 * @author Ghost_chu
 */
@Data
@Builder
public class CommandContainer {
    @NotNull
    private CommandHandler<?> executor;

    private boolean hidden; // Hide from help, tabcomplete
    /*
      E.g you can use the command when having quickshop.removeall.self or quickshop.removeall.others permission
    */
    @Singular
    private List<String> selectivePermissions;
    @Singular
    private List<String> permissions; // E.g quickshop.unlimited
    @NotNull
    private String prefix; // E.g /qs <prefix>
    @Nullable
    private Function<@NotNull String, @Nullable Component> description; // Will show in the /qs help, provide an arg that pass a player locale code

    private boolean disabled; //Set command is disabled or not.
    @Nullable
    private Supplier<Boolean> disabledSupplier; //Set command is disabled or not.
    @Nullable
    private Supplier<Component> disablePlaceholder; //Set the text shown if command disabled
    @Nullable
    private Function<@Nullable CommandSender, @NotNull Component> disableCallback; //Set the callback that should return a text to shown

    private Class<?> executorType;

    @ApiStatus.Internal
    @NotNull
    public Class<?> getExecutorType() {
        if (executorType == null) {
            bakeExecutorType();
        }
        return executorType;
    }

    @ApiStatus.Internal
    public void bakeExecutorType() {
        for (Method declaredMethod : getExecutor().getClass().getMethods()) {
            if ("onCommand".equals(declaredMethod.getName()) || "onTabComplete".equals(declaredMethod.getName())) {
                if (declaredMethod.getParameterCount() != 3 || declaredMethod.isSynthetic() || declaredMethod.isBridge()) {
                    continue;
                }
                executorType = declaredMethod.getParameterTypes()[0];
                return;
            }
        }
        executorType = Object.class;
    }

    /**
     * Gets the text should be shown while command was disabled.
     *
     * @param sender the sender
     * @return the text
     */
    public final @NotNull Component getDisableText(@NotNull CommandSender sender) {
        if (this.getDisableCallback() != null) {
            return this.getDisableCallback().apply(sender);
        } else if (this.getDisablePlaceholder() != null && !CommonUtil.isEmptyComponent(this.getDisablePlaceholder().get())) {
            return this.getDisablePlaceholder().get();
        } else {
            return Component.empty().color(NamedTextColor.GRAY).append(QuickShopAPI.getInstance().getTextManager().of(sender, "command.feature-not-enabled").forLocale());
        }
    }
}
