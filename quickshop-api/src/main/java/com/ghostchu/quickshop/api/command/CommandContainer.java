package com.ghostchu.quickshop.api.command;

import com.ghostchu.quickshop.api.CommonUtil;
import com.ghostchu.quickshop.api.QuickShopAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Builds a CommandContainer that can be registered into CommandManager.
 *
 * @author Ghost_chu
 */
public class CommandContainer {

  @NotNull
  private CommandHandler<?> executor;

  private boolean hidden; // Hide from help, tabcomplete
  /*
    E.g you can use the command when having quickshop.removeall.self or quickshop.removeall.others permission
  */
  private List<String> selectivePermissions;
  private List<String> permissions; // E.g quickshop.unlimited
  @NotNull
  private String prefix; // E.g /quickshop <prefix>
  @Nullable
  private Function<@NotNull String, @Nullable Component> description; // Will show in the /quickshop help, provide an arg that pass a player locale code

  private boolean disabled; //Set command is disabled or not.
  @Nullable
  private Supplier<Boolean> disabledSupplier; //Set command is disabled or not.
  @Nullable
  private Supplier<Component> disablePlaceholder; //Set the text shown if command disabled
  @Nullable
  private Function<@Nullable CommandSender, @NotNull Component> disableCallback; //Set the callback that should return a text to shown

  private Class<?> executorType;

  /**
   * Gets the text should be shown while command was disabled.
   *
   * @param sender the sender
   *
   * @return the text
   */
  @NotNull
  public final Component getDisableText(@NotNull final CommandSender sender) {

    if(this.getDisableCallback() != null) {
      return this.getDisableCallback().apply(sender);
    } else if(this.getDisablePlaceholder() != null && !CommonUtil.isEmptyComponent(this.getDisablePlaceholder().get())) {
      return this.getDisablePlaceholder().get();
    } else {
      return Component.empty().color(NamedTextColor.GRAY).append(QuickShopAPI.getInstance().getTextManager().of(sender, "command.feature-not-enabled").forLocale());
    }
  }

  @ApiStatus.Internal
  @NotNull
  public Class<?> getExecutorType() {

    if(executorType == null) {
      bakeExecutorType();
    }
    return executorType;
  }

  @ApiStatus.Internal
  public void bakeExecutorType() {

    for(final Method declaredMethod : getExecutor().getClass().getMethods()) {
      if("onCommand".equals(declaredMethod.getName()) || "onTabComplete".equals(declaredMethod.getName())) {
        if(declaredMethod.getParameterCount() != 3 || declaredMethod.isSynthetic() || declaredMethod.isBridge()) {
          continue;
        }
        executorType = declaredMethod.getParameterTypes()[0];
        return;
      }
    }
    executorType = Object.class;
  }

  CommandContainer(@NotNull final CommandHandler<?> executor, final boolean hidden, final List<String> selectivePermissions, final List<String> permissions, @NotNull final String prefix, @Nullable final Function<@NotNull String, @Nullable Component> description, final boolean disabled, @Nullable final Supplier<Boolean> disabledSupplier, @Nullable final Supplier<Component> disablePlaceholder, @Nullable final Function<@Nullable CommandSender, @NotNull Component> disableCallback, final Class<?> executorType) {

    if(executor == null) {
      throw new NullPointerException("executor is marked non-null but is null");
  }
    if(prefix == null) {
      throw new NullPointerException("prefix is marked non-null but is null");
    }
    this.executor = executor;
    this.hidden = hidden;
    this.selectivePermissions = selectivePermissions;
    this.permissions = permissions;
    this.prefix = prefix;
    this.description = description;
    this.disabled = disabled;
    this.disabledSupplier = disabledSupplier;
    this.disablePlaceholder = disablePlaceholder;
    this.disableCallback = disableCallback;
    this.executorType = executorType;
  }

  public static class CommandContainerBuilder {

    private CommandHandler<?> executor;
    private boolean hidden;
    private java.util.ArrayList<String> selectivePermissions;
    private java.util.ArrayList<String> permissions;
    private String prefix;
    private Function<@NotNull String, @Nullable Component> description;
    private boolean disabled;
    private Supplier<Boolean> disabledSupplier;
    private Supplier<Component> disablePlaceholder;
    private Function<@Nullable CommandSender, @NotNull Component> disableCallback;
    private Class<?> executorType;

    CommandContainerBuilder() {

    }

    public CommandContainer.CommandContainerBuilder executor(@NotNull final CommandHandler<?> executor) {

      if(executor == null) {
        throw new NullPointerException("executor is marked non-null but is null");
      }
      this.executor = executor;
      return this;
    }

    public CommandContainer.CommandContainerBuilder hidden(final boolean hidden) {

      this.hidden = hidden;
      return this;
    }

    public CommandContainer.CommandContainerBuilder selectivePermission(final String selectivePermission) {

      if(this.selectivePermissions == null) {
        this.selectivePermissions = new java.util.ArrayList<String>();
      }
      this.selectivePermissions.add(selectivePermission);
      return this;
    }

    public CommandContainer.CommandContainerBuilder selectivePermissions(final java.util.Collection<? extends String> selectivePermissions) {

      if(selectivePermissions == null) {
        throw new NullPointerException("selectivePermissions cannot be null");
      }
      if(this.selectivePermissions == null) {
        this.selectivePermissions = new java.util.ArrayList<String>();
      }
      this.selectivePermissions.addAll(selectivePermissions);
      return this;
    }

    public CommandContainer.CommandContainerBuilder clearSelectivePermissions() {

      if(this.selectivePermissions != null) this.selectivePermissions.clear();
      return this;
    }

    public CommandContainer.CommandContainerBuilder permission(final String permission) {

      if(this.permissions == null) this.permissions = new java.util.ArrayList<String>();
      this.permissions.add(permission);
      return this;
    }

    public CommandContainer.CommandContainerBuilder permissions(final java.util.Collection<? extends String> permissions) {

      if(permissions == null) {
        throw new NullPointerException("permissions cannot be null");
      }
      if(this.permissions == null) this.permissions = new java.util.ArrayList<String>();
      this.permissions.addAll(permissions);
      return this;
    }

    public CommandContainer.CommandContainerBuilder clearPermissions() {

      if(this.permissions != null) this.permissions.clear();
      return this;
    }

    public CommandContainer.CommandContainerBuilder prefix(@NotNull final String prefix) {

      if(prefix == null) {
        throw new NullPointerException("prefix is marked non-null but is null");
      }
      this.prefix = prefix;
      return this;
    }

    public CommandContainer.CommandContainerBuilder description(@Nullable final Function<@NotNull String, @Nullable Component> description) {

      this.description = description;
      return this;
    }

    public CommandContainer.CommandContainerBuilder disabled(final boolean disabled) {

      this.disabled = disabled;
      return this;
    }

    public CommandContainer.CommandContainerBuilder disabledSupplier(@Nullable final Supplier<Boolean> disabledSupplier) {

      this.disabledSupplier = disabledSupplier;
      return this;
    }

    public CommandContainer.CommandContainerBuilder disablePlaceholder(@Nullable final Supplier<Component> disablePlaceholder) {

      this.disablePlaceholder = disablePlaceholder;
      return this;
    }

    public CommandContainer.CommandContainerBuilder disableCallback(@Nullable final Function<@Nullable CommandSender, @NotNull Component> disableCallback) {

      this.disableCallback = disableCallback;
      return this;
    }

    public CommandContainer.CommandContainerBuilder executorType(final Class<?> executorType) {

      this.executorType = executorType;
      return this;
    }

    public CommandContainer build() {

      java.util.List<String> selectivePermissions;
      switch(this.selectivePermissions == null? 0 : this.selectivePermissions.size()) {
        case 0:
          selectivePermissions = java.util.Collections.emptyList();
          break;
        case 1:
          selectivePermissions = java.util.Collections.singletonList(this.selectivePermissions.get(0));
          break;
        default:
          selectivePermissions = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.selectivePermissions));
      }
      java.util.List<String> permissions;
      switch(this.permissions == null? 0 : this.permissions.size()) {
        case 0:
          permissions = java.util.Collections.emptyList();
          break;
        case 1:
          permissions = java.util.Collections.singletonList(this.permissions.get(0));
          break;
        default:
          permissions = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.permissions));
      }
      return new CommandContainer(this.executor, this.hidden, selectivePermissions, permissions, this.prefix, this.description, this.disabled, this.disabledSupplier, this.disablePlaceholder, this.disableCallback, this.executorType);
    }

    @Override
    public String toString() {

      return "CommandContainer.CommandContainerBuilder(executor=" + this.executor + ", hidden=" + this.hidden + ", selectivePermissions=" + this.selectivePermissions + ", permissions=" + this.permissions + ", prefix=" + this.prefix + ", description=" + this.description + ", disabled=" + this.disabled + ", disabledSupplier=" + this.disabledSupplier + ", disablePlaceholder=" + this.disablePlaceholder + ", disableCallback=" + this.disableCallback + ", executorType=" + this.executorType + ")";
    }
  }

  public static CommandContainer.CommandContainerBuilder builder() {

    return new CommandContainer.CommandContainerBuilder();
  }

  @NotNull
  public CommandHandler<?> getExecutor() {

    return this.executor;
  }

  public boolean isHidden() {

    return this.hidden;
  }

  public List<String> getSelectivePermissions() {

    return this.selectivePermissions;
  }

  public List<String> getPermissions() {

    return this.permissions;
  }

  @NotNull
  public String getPrefix() {

    return this.prefix;
  }

  @Nullable
  public Function<@NotNull String, @Nullable Component> getDescription() {

    return this.description;
  }

  public boolean isDisabled() {

    return this.disabled;
  }

  @Nullable
  public Supplier<Boolean> getDisabledSupplier() {

    return this.disabledSupplier;
  }

  @Nullable
  public Supplier<Component> getDisablePlaceholder() {

    return this.disablePlaceholder;
  }

  @Nullable
  public Function<@Nullable CommandSender, @NotNull Component> getDisableCallback() {

    return this.disableCallback;
  }

  public void setExecutor(@NotNull final CommandHandler<?> executor) {

    if(executor == null) {
      throw new NullPointerException("executor is marked non-null but is null");
    }
    this.executor = executor;
  }

  public void setHidden(final boolean hidden) {

    this.hidden = hidden;
  }

  public void setSelectivePermissions(final List<String> selectivePermissions) {

    this.selectivePermissions = selectivePermissions;
  }

  public void setPermissions(final List<String> permissions) {

    this.permissions = permissions;
  }

  public void setPrefix(@NotNull final String prefix) {

    if(prefix == null) {
      throw new NullPointerException("prefix is marked non-null but is null");
    }
    this.prefix = prefix;
  }

  public void setDescription(@Nullable final Function<@NotNull String, @Nullable Component> description) {

    this.description = description;
  }

  public void setDisabled(final boolean disabled) {

    this.disabled = disabled;
  }

  public void setDisabledSupplier(@Nullable final Supplier<Boolean> disabledSupplier) {

    this.disabledSupplier = disabledSupplier;
  }

  public void setDisablePlaceholder(@Nullable final Supplier<Component> disablePlaceholder) {

    this.disablePlaceholder = disablePlaceholder;
  }

  public void setDisableCallback(@Nullable final Function<@Nullable CommandSender, @NotNull Component> disableCallback) {

    this.disableCallback = disableCallback;
  }

  public void setExecutorType(final Class<?> executorType) {

    this.executorType = executorType;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof CommandContainer)) return false;
    final CommandContainer other = (CommandContainer)o;
    return this.isHidden() == other.isHidden()
           && this.isDisabled() == other.isDisabled()
           && Objects.equals(this.getExecutor(), other.getExecutor())
           && Objects.equals(this.getSelectivePermissions(), other.getSelectivePermissions())
           && Objects.equals(this.getPermissions(), other.getPermissions())
           && Objects.equals(this.getPrefix(), other.getPrefix())
           && Objects.equals(this.getDescription(), other.getDescription())
           && Objects.equals(this.getDisabledSupplier(), other.getDisabledSupplier())
           && Objects.equals(this.getDisablePlaceholder(), other.getDisablePlaceholder())
           && Objects.equals(this.getDisableCallback(), other.getDisableCallback())
           && Objects.equals(this.getExecutorType(), other.getExecutorType());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.isHidden(), this.isDisabled(), this.getExecutor(), this.getSelectivePermissions(), this.getPermissions(), this.getPrefix(), this.getDescription(), this.getDisabledSupplier(), this.getDisablePlaceholder(), this.getDisableCallback(), this.getExecutorType());
  }

  @Override
  public String toString() {

    return "CommandContainer(executor=" + this.getExecutor() + ", hidden=" + this.isHidden() + ", selectivePermissions=" + this.getSelectivePermissions() + ", permissions=" + this.getPermissions() + ", prefix=" + this.getPrefix() + ", description=" + this.getDescription() + ", disabled=" + this.isDisabled() + ", disabledSupplier=" + this.getDisabledSupplier() + ", disablePlaceholder=" + this.getDisablePlaceholder() + ", disableCallback=" + this.getDisableCallback() + ", executorType=" + this.getExecutorType() + ")";
  }
}
