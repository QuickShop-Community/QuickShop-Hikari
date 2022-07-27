package com.ghostchu.quickshop.util.economyformatter;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.ghostchu.simplereloadlib.Reloadable;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuiltInEconomyFormatter implements Reloadable {
    private static final Map<String, String> CURRENCY_SYMBOL_MAPPING = new HashMap<>();
    private final QuickShop plugin;
    private boolean useDecimalFormat;
    private boolean currencySymbolOnRight;

    public BuiltInEconomyFormatter(QuickShop plugin) {
        this.plugin = plugin;
        reloadModule();
        plugin.getReloadManager().register(this);
    }

    @Override
    public ReloadResult reloadModule() {
        CURRENCY_SYMBOL_MAPPING.clear();
        this.useDecimalFormat = plugin.getConfig().getBoolean("use-decimal-format", false);
        this.currencySymbolOnRight = plugin.getConfig().getBoolean("shop.currency-symbol-on-right", false);
        List<String> symbols = plugin.getConfig().getStringList("shop.alternate-currency-symbol-list");
        symbols.forEach(entry -> {
            String[] splits = entry.split(";", 2);
            if (splits.length < 2) {
                plugin.getLogger().warning("Invalid entry in alternate-currency-symbol-list: " + entry);
            }
            CURRENCY_SYMBOL_MAPPING.put(splits[0], splits[1]);
        });
        return new ReloadResult(ReloadStatus.SUCCESS, "Reload successfully.", null);
    }


    public String getInternalFormat(double amount, @Nullable String currency) {
        if (StringUtils.isEmpty(currency)) {
            Log.debug("Format: Currency is null");
            String formatted = useDecimalFormat ? MsgUtil.decimalFormat(amount) : Double.toString(amount);
            return currencySymbolOnRight ? formatted + plugin.getConfig().getString("shop.alternate-currency-symbol", "$") : plugin.getConfig().getString("shop.alternate-currency-symbol", "$") + formatted;
        } else {
            Log.debug("Format: Currency is: [" + currency + "]");
            String formatted = useDecimalFormat ? MsgUtil.decimalFormat(amount) : Double.toString(amount);
            String symbol = CURRENCY_SYMBOL_MAPPING.getOrDefault(currency, currency);
            return currencySymbolOnRight ? formatted + symbol : symbol + formatted;
        }
    }
}
