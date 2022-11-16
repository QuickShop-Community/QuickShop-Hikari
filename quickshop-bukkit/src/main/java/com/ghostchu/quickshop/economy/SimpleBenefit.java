package com.ghostchu.quickshop.economy;

import com.ghostchu.quickshop.api.economy.Benefit;
import com.ghostchu.quickshop.util.JsonUtil;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SimpleBenefit implements Benefit {
    private final Map<UUID, Double> benefits;

    public SimpleBenefit() {
        this.benefits = new HashMap<>();
    }

    public SimpleBenefit(Map<UUID, Double> benefits) {
        this.benefits = benefits;
    }

    @NotNull
    public static SimpleBenefit deserialize(@Nullable String json) {
        if (StringUtils.isEmpty(json)) return new SimpleBenefit();
        Map<UUID, Double> map = JsonUtil.regular().fromJson(json, new TypeToken<Map<UUID, Double>>() {
        }.getType());
        return new SimpleBenefit(map);
    }

    @Override
    public boolean isEmpty() {
        return benefits.isEmpty();
    }

    @Override
    public double getOverflow(double newAdded) {
        double sum = 0d;
        for (Double value : benefits.values()) {
            sum += value;
        }
        sum += newAdded;
        if (sum <= 1.0d) return 0.0d;
        return sum - 1d;
    }

    @Override
    public void addBenefit(@NotNull UUID player, double benefit) throws BenefitOverflowException, BenefitExistsException {
        double overflow = getOverflow(benefit);
        if (overflow != 0) {
            throw new BenefitOverflowException(overflow);
        }
        if (benefits.containsKey(player))
            throw new BenefitExistsException();
        benefits.put(player, benefit);
    }

    @Override
    public void removeBenefit(@NotNull UUID player) {
        benefits.remove(player);
    }

    @Override
    public @NotNull Map<UUID, Double> getRegistry() {
        return new HashMap<>(this.benefits);
    }

    @NotNull
    @Override
    public String serialize() {
        return JsonUtil.regular().toJson(this.benefits);
    }
}
