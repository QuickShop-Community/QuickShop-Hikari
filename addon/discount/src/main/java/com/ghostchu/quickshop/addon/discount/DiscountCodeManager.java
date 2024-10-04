package com.ghostchu.quickshop.addon.discount;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.discount.type.CodeCreationResponse;
import com.ghostchu.quickshop.addon.discount.type.CodeType;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class DiscountCodeManager {

  public static final String NAME_REG_EXP = "[a-zA-Z0-9_]*";
  private final Set<DiscountCode> codes = Collections.synchronizedSet(new HashSet<>());
  private final Pattern namePattern = Pattern.compile(NAME_REG_EXP);
  private final Main main;
  private final File file;
  private YamlConfiguration config;

  public DiscountCodeManager(final Main main) throws IOException {

    this.main = main;
    this.file = new File(main.getDataFolder(), "data.yml");
    initDatabase();
    cleanExpiredCodes();
  }

  private void initDatabase() throws IOException {

    if(!this.file.exists()) {
      this.file.createNewFile();
    }
    this.config = YamlConfiguration.loadConfiguration(this.file);
    this.codes.clear();
    this.codes.addAll(this.config.getStringList("codes").stream()
                              .map(DiscountCode::fromString)
                              .filter(Objects::nonNull)
                              .toList());
  }

  public void cleanExpiredCodes() {

    if(codes.removeIf(DiscountCode::isExpired)) {
      saveDatabase();
    }
  }

  public void saveDatabase() {

    try {
      this.config.set("codes", this.codes.stream().map(DiscountCode::saveToString).toList());
      this.config.save(this.file);
    } catch(IOException e) {
      main.getLogger().log(Level.WARNING, "Couldn't save the player discount codes status into database.", e);
    }
  }

  @Nullable
  public DiscountCode getCode(@NotNull final String code) {

    for(final DiscountCode discountCode : this.codes) {
      if(discountCode.getCode().equalsIgnoreCase(code)) {
        return discountCode;
      }
    }
    return null;
  }

  @NotNull
  public Set<DiscountCode> getCodes() {

    return codes;
  }

  public void removeCode(@NotNull final DiscountCode discountCode) {

    codes.remove(discountCode);
  }

  @NotNull
  public CodeCreationResponse createDiscountCode(@NotNull final CommandSender sender, @NotNull final UUID owner, @NotNull final String code, @NotNull final CodeType codeType, @NotNull final String rate, final int maxUsage, final double threshold, final long expiredTime) {

    if(!namePattern.matcher(code).matches()) {
      return CodeCreationResponse.REGEX_FAILURE;
    }
    if(codes.stream().anyMatch(c->c.getCode().equalsIgnoreCase(code))) {
      return CodeCreationResponse.CODE_EXISTS;
    }
    if(maxUsage != -1 && maxUsage < 1) {
      return CodeCreationResponse.INVALID_USAGE;
    }
    if(threshold != -1 && threshold < 1) {
      return CodeCreationResponse.INVALID_THRESHOLD;
    }
    if(expiredTime != -1 && expiredTime < 1) {
      return CodeCreationResponse.INVALID_EXPIRE_TIME;
    }
    final DiscountCode.DiscountRate discountRate = DiscountCode.toDiscountRate(rate);
    if(discountRate == null) {
      return CodeCreationResponse.INVALID_RATE;
    }
    if(!QuickShop.getPermissionManager().hasPermission(sender, "quickshopaddon.discount.create." + codeType.name().toLowerCase())) {
      return CodeCreationResponse.PERMISSION_DENIED;
    }
    final DiscountCode discountCode = new DiscountCode(owner, code, codeType, discountRate, maxUsage, threshold, expiredTime);
    if(!this.codes.add(discountCode)) {
      return CodeCreationResponse.CODE_EXISTS;
    }
    main.getCodeManager().saveDatabase();
    return CodeCreationResponse.SUCCESS;
  }

}
