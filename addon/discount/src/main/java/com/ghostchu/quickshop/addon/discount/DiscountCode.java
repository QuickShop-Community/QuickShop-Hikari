package com.ghostchu.quickshop.addon.discount;

import com.ghostchu.quickshop.addon.discount.type.ApplicableType;
import com.ghostchu.quickshop.addon.discount.type.CodeType;
import com.ghostchu.quickshop.addon.discount.type.RateType;
import com.ghostchu.quickshop.api.localization.text.TextManager;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CalculateUtil;
import com.ghostchu.quickshop.util.JsonUtil;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DiscountCode {
    private final String code;
    private final UUID owner;
    private final Map<UUID, Integer> usages;
    private final Set<Long> shopScope;
    private CodeType codeType;
    private DiscountRate rate;
    private int maxUsage;
    private double threshold;

    private long expiredTime;

    public DiscountCode(@NotNull UUID owner, @NotNull String code, @NotNull CodeType codeType, @NotNull DiscountRate rate, int maxUsage, @NotNull Map<UUID, Integer> usages, @NotNull Set<Long> shopScope, double threshold, long expiredTime) {
        this.owner = owner;
        this.code = code;
        this.codeType = codeType;
        this.rate = rate;
        this.maxUsage = maxUsage;
        this.usages = usages;
        this.shopScope = shopScope;
        this.threshold = threshold;
        this.expiredTime = expiredTime;
    }

    public DiscountCode(@NotNull UUID owner, @NotNull String code, @NotNull CodeType codeType, @NotNull DiscountRate rate, int maxUsage, double threshold, long expiredTime) {
        this.owner = owner;
        this.code = code;
        this.codeType = codeType;
        this.rate = rate;
        this.maxUsage = maxUsage;
        this.threshold = threshold;
        this.usages = new HashMap<>();
        this.shopScope = new HashSet<>();
        this.expiredTime = expiredTime;
    }

    @Nullable
    public static DiscountRate toDiscountRate(@NotNull String str) {
        DiscountRate discountRate;
        try {
            if (str.endsWith("%")) {
                double v = Double.parseDouble(StringUtils.substringBeforeLast(str, "%"));
                v = v / 100d;
                if (v <= 0d || v >= 1d) return null;
                discountRate = new PercentageDiscountRate(v);
            } else {
                double v = Double.parseDouble(str);
                if (v <= 0d) return null;
                discountRate = new FixedDiscountRate(v);
            }
            return discountRate;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    public static DiscountCode fromString(@NotNull String string) {
        DiscountCodeData data = JsonUtil.getGson().fromJson(string, DiscountCodeData.class);
        if (data == null) return null;
        DiscountRate rate;
        if (data.getRateType() == RateType.PERCENTAGE) {
            rate = JsonUtil.getGson().fromJson(data.getRate(), PercentageDiscountRate.class);
        } else {
            rate = JsonUtil.getGson().fromJson(data.getRate(), FixedDiscountRate.class);
        }
        return new DiscountCode(data.getOwner(),
                data.getCode(),
                data.getCodeType(),
                rate,
                data.getMaxUsage(),
                data.getUsages(),
                data.getShopScope(),
                data.getThreshold(),
                data.getExpire());
    }

    @NotNull
    public DiscountRate getRate() {
        return rate;
    }

    public void setRate(@NotNull DiscountRate rate) {
        this.rate = rate;
    }

    public long getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(long expiredTime) {
        this.expiredTime = expiredTime;
    }

    @NotNull
    public UUID getOwner() {
        return owner;
    }

    @NotNull
    public String getCode() {
        return code;
    }

    public int getMaxUsage() {
        return maxUsage;
    }

    public void setMaxUsage(int maxUsage) {
        this.maxUsage = maxUsage;
    }

    public boolean addShopToScope(@NotNull Shop shop) {
        if (codeType != CodeType.SPECIFIC_SHOPS)
            throw new IllegalStateException("Cannot add shop to code scope, because this code is not a specific shop code.");
        return this.shopScope.add(shop.getShopId());
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    @NotNull
    public Map<UUID, Integer> getUsages() {
        return usages;
    }

    public double apply(@NotNull UUID player, double total) {
        if (isExpired())
            return total;
        if (total < threshold)
            return total;
        if (getRemainsUsage(player) - 1 < 0) {
            return total;
        }
        if (usages.containsKey(player)) {
            int usage = usages.get(player);
            usages.put(player, usage + 1);
        } else {
            usages.put(player, 1);
        }
        return rate.apply(total);
    }

    public boolean isExpired() {
        if (expiredTime == -1)
            return false;
        return System.currentTimeMillis() > expiredTime;
    }

    @NotNull
    public CodeType getCodeType() {
        return codeType;
    }

    public void setCodeType(CodeType codeType) {
        this.codeType = codeType;
    }

    public Set<Long> getShopScope() {
        return shopScope;
    }

    public int getRemainsUsage(UUID user) {
        if (maxUsage == -1)
            return Integer.MAX_VALUE;
        if (!usages.containsKey(user))
            return maxUsage;
        return Math.max(maxUsage - usages.get(user), 0);
    }

    @NotNull
    public ApplicableType applicableShop(@NotNull UUID uuid, @NotNull Shop shop) {
        // permission test
        if (!shop.playerAuthorize(uuid, Main.instance, "discount_code_use")) {
            return ApplicableType.NO_PERMISSION;
        }
        if (isExpired())
            return ApplicableType.EXPIRED;
        int usage = 0;
        if (usages.containsKey(uuid)) {
            usage = usages.get(uuid);
        }
        if (usage + 1 > maxUsage) {
            return ApplicableType.REACHED_THE_LIMIT;
        }
        ApplicableType type = switch (codeType) {
            case SPECIFIC_SHOPS -> {
                if (shopScope.contains(shop.getShopId())) {
                    yield ApplicableType.APPLICABLE;
                } else {
                    yield ApplicableType.NOT_APPLICABLE;
                }
            }
            case PLAYER_ALL_SHOPS -> {
                if (shop.getOwner().equals(owner)) {
                    yield ApplicableType.APPLICABLE;
                } else {
                    yield ApplicableType.NOT_APPLICABLE;
                }
            }
            case SERVER_ALL_SHOPS -> ApplicableType.APPLICABLE;
        };
        if (type != ApplicableType.APPLICABLE) return type;
        if (this.threshold != -1) {
            return ApplicableType.APPLICABLE_WITH_THRESHOLD;
        }
        return ApplicableType.APPLICABLE;
    }

    @NotNull
    public String saveToString() {
        DiscountCodeData data = new DiscountCodeData(
                owner,
                code,
                codeType,
                rate instanceof PercentageDiscountRate ? RateType.PERCENTAGE : RateType.FIXED,
                JsonUtil.getGson().toJson(rate),
                expiredTime,
                threshold,
                maxUsage,
                usages,
                shopScope
        );
        return JsonUtil.getGson().toJson(data);
    }

    public interface DiscountRate {
        /**
         * Apply the discount to the price
         *
         * @param price price
         * @return discounted price
         */
        double apply(double price);

        @NotNull
        Component format(@NotNull CommandSender sender, @NotNull TextManager textManager);
    }

    static class FixedDiscountRate implements DiscountRate {
        private final double rate;

        public FixedDiscountRate(double rate) {
            this.rate = rate;
        }

        @Override
        public double apply(double price) {
            return Math.max(0.0d, price - rate);
        }

        @Override
        public @NotNull Component format(@NotNull CommandSender sender, @NotNull TextManager textManager) {
            return textManager.of(sender, "addon.discount.fixed-off", String.format("%.2f", rate)).forLocale();
        }
    }

    static class PercentageDiscountRate implements DiscountRate {
        private final double percent;

        public PercentageDiscountRate(double percent) {
            if (percent > 1.0d) {
                throw new IllegalArgumentException("The percent must be less than 1.0");
            }
            if (percent < 0.0d) {
                throw new IllegalArgumentException("The percent must be bigger then 0.0");
            }
            this.percent = percent;
        }

        @Override
        public double apply(double price) {
            return CalculateUtil.multiply(price, percent);
        }

        @Override
        public @NotNull Component format(@NotNull CommandSender sender, @NotNull TextManager textManager) {
            return textManager.of(sender, "addon.discount.percentage-off", CalculateUtil.multiply(percent, 100)).forLocale();
        }
    }
}
