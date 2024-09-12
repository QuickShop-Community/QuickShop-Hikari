package com.ghostchu.quickshop.database.bean;

import com.ghostchu.quickshop.api.database.bean.DataRecord;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.PlayerFinder;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.obj.QUserImpl;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class SimpleDataRecord implements DataRecord {
    private final QUser owner;
    private final String item;
    private final String name;
    private final int type;
    private final String currency;
    private final double price;
    private final boolean unlimited;
    private final boolean hologram;
    private final QUser taxAccount;
    private final String permissions;
    private final String extra;
    private final String inventoryWrapper;
    private final String inventorySymbolLink;
    private final Date createTime;

    private final String benefit;

    public SimpleDataRecord(QUser owner, String item, String name, int type, String currency, double price, boolean unlimited, boolean hologram, QUser taxAccount, String permissions, String extra, String inventoryWrapper, String inventorySymbolLink, Date createTime, String benefit) {
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
        this.benefit = benefit;
    }

    public SimpleDataRecord(PlayerFinder finder, ResultSet set) throws SQLException {
        this.owner = QUserImpl.deserialize(finder, set.getString("owner"), QuickExecutor.getSecondaryProfileIoExecutor());
        this.item = set.getString("item");
        this.name = set.getString("name");
        this.type = set.getInt("type");
        this.currency = set.getString("currency");
        this.price = set.getDouble("price");
        this.unlimited = set.getBoolean("unlimited");
        this.hologram = set.getBoolean("hologram");
        String taxAccountString = set.getString("tax_account");
        this.taxAccount = taxAccountString == null ? null : QUserImpl.deserialize(finder, taxAccountString, QuickExecutor.getSecondaryProfileIoExecutor());
        this.permissions = set.getString("permissions");
        this.extra = set.getString("extra");
        this.inventorySymbolLink = set.getString("inv_symbol_link");
        this.inventoryWrapper = set.getString("inv_wrapper");
        this.createTime = set.getTimestamp("create_time");
        this.benefit = set.getString("benefit");
    }

    @NotNull
    public Map<String, Object> generateLookupParams() {
        Map<String, Object> map = new HashMap<>(generateParams());
        map.remove("create_time");
        return map;
    }

    @NotNull
    public Map<String, Object> generateParams() {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("owner", owner.serialize());
        map.put("item", item);
        map.put("name", name);
        map.put("type", type);
        map.put("currency", currency);
        map.put("price", price);
        map.put("unlimited", unlimited);
        map.put("hologram", hologram);
        if (taxAccount != null) {
            map.put("tax_account", taxAccount.serialize());
        } else {
            map.put("tax_account", null);
        }
        map.put("permissions", permissions);
        map.put("extra", extra);
        map.put("inv_wrapper", inventoryWrapper);
        map.put("inv_symbol_link", inventorySymbolLink);
        map.put("create_time", createTime);
        map.put("benefit", benefit);
        return map;
    }

    @Override
    public @NotNull Date getCreateTime() {
        return createTime;
    }

    @Override
    public String getCurrency() {
        return currency;
    }

    @Override
    public @NotNull String getExtra() {
        return extra;
    }

    @Override
    public @NotNull String getInventorySymbolLink() {
        return inventorySymbolLink;
    }

    @Override
    public @NotNull String getInventoryWrapper() {
        return inventoryWrapper;
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
    public @NotNull QUser getOwner() {
        return owner;
    }

    @Override
    public @NotNull String getPermissions() {
        return permissions;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public QUser getTaxAccount() {
        return taxAccount;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public boolean isHologram() {
        return hologram;
    }

    @Override
    public boolean isUnlimited() {
        return unlimited;
    }

    @Override
    public @NotNull String getBenefit() {
        return benefit;
    }
}
