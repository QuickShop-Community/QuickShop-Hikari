package com.ghostchu.quickshop.database.bean;

import com.ghostchu.quickshop.api.database.bean.DataRecord;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Data
public class SimpleDataRecord implements DataRecord {
    private final UUID owner;
    private final String item;
    private final String name;
    private final int type;
    private final String currency;
    private final double price;
    private final boolean unlimited;
    private final boolean hologram;
    private final UUID taxAccount;
    private final String permissions;
    private final String extra;
    private final String inventoryWrapper;
    private final String inventorySymbolLink;
    private final Date createTime;

    public SimpleDataRecord(UUID owner, String item, String name, int type, String currency, double price, boolean unlimited, boolean hologram, UUID taxAccount, String permissions, String extra, String inventoryWrapper, String inventorySymbolLink, Date createTime) {
        this.owner = owner;
        this.item = item;
        this.name = name;
        this.type = type;
        this.currency = currency;
        this.price = price;
        this.unlimited = unlimited;
        this.hologram = hologram;
        this.taxAccount = taxAccount;
        this.permissions = permissions;
        this.extra = extra;
        this.inventoryWrapper = inventoryWrapper;
        this.inventorySymbolLink = inventorySymbolLink;
        this.createTime = createTime;
    }

    public SimpleDataRecord(ResultSet set) throws SQLException {
        this.owner = UUID.fromString(set.getString("owner"));
        this.item = set.getString("item");
        this.name = set.getString("name");
        this.type = set.getInt("type");
        this.currency = set.getString("currency");
        this.price = set.getDouble("price");
        this.unlimited = set.getBoolean("unlimited");
        this.hologram = set.getBoolean("hologram");
        this.taxAccount = set.getString("tax_account") == null ? null : UUID.fromString(set.getString("tax_account"));
        this.permissions = set.getString("permissions");
        this.extra = set.getString("extra");
        this.inventorySymbolLink = set.getString("inv_symbol_link");
        this.inventoryWrapper = set.getString("inv_wrapper");
        this.createTime = set.getTimestamp("create_time");
    }

    @NotNull
    public Map<String, Object> generateLookupParams() {
        Map<String, Object> map = new HashMap<>(generateParams());
        map.remove("create_time");
        return map;
    }

    @NotNull
    public Map<String, Object> generateParams() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("owner", owner.toString());
        map.put("item", item);
        map.put("name", name);
        map.put("type", type);
        map.put("currency", currency);
        map.put("price", price);
        map.put("unlimited", unlimited);
        map.put("hologram", hologram);
        map.put("tax_account", taxAccount);
        map.put("permissions", permissions);
        map.put("extra", extra);
        map.put("inv_wrapper", inventoryWrapper);
        map.put("inv_symbol_link", inventorySymbolLink);
        map.put("create_time", createTime);
        return map;
    }

    @Override
    public @NotNull UUID getOwner() {
        return owner;
    }

    @Override
    public @NotNull String getItem() {
        return item;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String getCurrency() {
        return currency;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public boolean isUnlimited() {
        return unlimited;
    }

    @Override
    public boolean isHologram() {
        return hologram;
    }

    @Override
    public UUID getTaxAccount() {
        return taxAccount;
    }

    @Override
    public @NotNull String getPermissions() {
        return permissions;
    }

    @Override
    public @NotNull String getInventoryWrapper() {
        return inventoryWrapper;
    }

    @Override
    public @NotNull String getInventorySymbolLink() {
        return inventorySymbolLink;
    }

    @Override
    public @NotNull Date getCreateTime() {
        return createTime;
    }

    @Override
    public @NotNull String getExtra() {
        return extra;
    }
}
