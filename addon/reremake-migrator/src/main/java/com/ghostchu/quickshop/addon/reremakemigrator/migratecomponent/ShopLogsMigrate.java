package com.ghostchu.quickshop.addon.reremakemigrator.migratecomponent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.reremakemigrator.Main;
import com.ghostchu.quickshop.api.database.ShopMetricRecord;
import com.ghostchu.quickshop.api.database.ShopOperationEnum;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

public class ShopLogsMigrate extends AbstractMigrateComponent {
    private final String template = "[2023-11-11 19:35:43.502] ";
    DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    public ShopLogsMigrate(Main main, QuickShop hikari, org.maxgamer.quickshop.QuickShop reremake, CommandSender sender) {
        super(main, hikari, reremake, sender);
    }

    @Override
    public boolean migrate() {
        text("modules.shop-logs.start-migrate").send();
        File logsFile = new File(getReremake().getDataFolder(), "appended-qs.log");
        File filteredFile = new File(getReremake().getDataFolder(), "filtered-qs.log");
        try (PrintWriter printWriter = new PrintWriter(logsFile, StandardCharsets.UTF_8)) {
            logsFile.createNewFile();
            filteredFile.createNewFile();
            appendFiles(getReremake().getDataFolder(), printWriter);
            printWriter.flush();
            migrateAppendedLogs(logsFile, filteredFile);
            readAndFormatEntire(filteredFile);
            importToDatabase(filteredFile);
        } catch (Exception ex) {
            getHikari().logger().warn("Failed to migrate logs", ex);
            return true;
        }
        return true;
    }

    private void importToDatabase(File filteredFile) throws IOException {
        long sumLines = countLines(filteredFile);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filteredFile));
        try (bufferedReader) {
            String cursor = null;
            long count = 0;
            try {
                while ((cursor = bufferedReader.readLine()) != null) {
                    count++;
                    text("modules.shop-logs.import-entry", "<ENTRY TOO  LONG>", count, sumLines);
                    DatedLogEntry entry = JsonUtil.standard().fromJson(new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8), DatedLogEntry.class);
                    Date date = entry.getDate();
                    JsonObject jObj = JsonUtil.readObject(entry.getContent());
                    if (jObj.has("shop") && jObj.has("type") && jObj.has("trader")
                            && jObj.has("itemStack") && jObj.has("amount")
                            && jObj.has("balance")
                            && jObj.has("tax")) {
                        try {
                            importThePurchaseRecord(date, jObj);
                        } catch (Exception e) {
                            getHikari().logger().warn("Error on importing to database", e);
                        }
                        continue;
                    }
//                if (jObj.has("creator") && jObj.has("shop") && jObj.has("location")) {
//                    importTheCreationRecord(date, jObj);
//                    continue;
//                }
                    getHikari().logger().warn("Invalid record {}, skipping", entry);
                }
            } catch (Exception e) {
                getHikari().logger().warn("Parse the log failed, cursor [{}]", cursor, e);
            }
        }
    }

    private long countLines(File filteredFile) throws FileNotFoundException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filteredFile));
        return bufferedReader.lines().count();
    }

//    private void importTheCreationRecord(Date date, JsonObject jObj) {
//        UUID creator = UUID.fromString(jObj.getAsJsonObject("creator").getAsString());
//        JsonObject shopObj = jObj.get("shop").getAsJsonObject();
//        JsonObject loc = jObj.get("location").getAsJsonObject();
//        String worldName = loc.get("world").getAsString();
//        int x = loc.get("x").getAsInt();
//        int y = loc.get("y").getAsInt();
//        int z = loc.get("z").getAsInt();
//        World world = Bukkit.getWorld("worldName");
//        if (world == null) {
//            getHikari().logger().warn("Failed to migrate record, world is null.");
//            return;
//        }
//        Shop shop = getHikari().getShopManager().getShop(new Location(world, x, y, z));
//        if (shop == null) {
//            getHikari().logger().warn("Failed to migrate record, shop already no-longer exists.");
//            return;
//        }
//
//    }

    private void importThePurchaseRecord(Date date, JsonObject jObj) throws InvalidConfigurationException {
        int amount = jObj.get("amount").getAsInt();
        double balance = jObj.get("balance").getAsDouble();
        double tax = jObj.get("tax").getAsDouble();
        UUID trader = UUID.fromString(jObj.get("trader").getAsString());
        ShopType type = ShopType.valueOf(jObj.get("type").getAsString());
        JsonObject shop = jObj.get("shop").getAsJsonObject();
        JsonObject pos = shop.get("position").getAsJsonObject();
        World world = Bukkit.getWorld(pos.get("world").getAsString());
        if (world == null) {
            throw new IllegalArgumentException("World " + jObj.get("world").getAsString() + " not exists.");
        }
        int x = pos.get("x").getAsInt();
        int y = pos.get("y").getAsInt();
        int z = pos.get("z").getAsInt();
        Location location = new Location(world, x, y, z);
        Shop shopInstance = getHikari().getShopManager().getShop(location);
        if (shopInstance == null) {
            throw new IllegalArgumentException("Shop at " + location + " not exists anymore");
        }
        ShopMetricRecord shopMetricRecord = new ShopMetricRecord(
                date.getTime(),
                shopInstance.getShopId(),
                type == ShopType.SELLING ? ShopOperationEnum.PURCHASE_SELLING_SHOP : ShopOperationEnum.PURCHASE_BUYING_SHOP,
                balance,
                tax,
                amount,
                QUserImpl.createSync(getHikari().getPlayerFinder(), trader, QuickExecutor.getSecondaryProfileIoExecutor())
        );
        getHikari().getDatabaseHelper().insertMetricRecord(shopMetricRecord);
    }

    private void readAndFormatEntire(File filteredFile) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filteredFile));
        File formattedFile = new File(getReremake().getDataFolder(), "formatted-qs.log");
        formattedFile.createNewFile();
        PrintWriter writer = new PrintWriter(formattedFile, StandardCharsets.UTF_8);
        StringBuilder buffer = new StringBuilder();
        try (bufferedReader; writer) {
            String cursor;
            while ((cursor = bufferedReader.readLine()) != null) {
                if (_isNewLineStart(cursor)) {
                    // Clean buffer
                    if (!buffer.isEmpty()) {
                        DatedLogEntry formatted = _formatLine(buffer.toString());
                        if (formatted != null) {
                            writer.println(Base64.getEncoder().encodeToString(JsonUtil.standard().toJson(formatted)
                                    .getBytes(StandardCharsets.UTF_8)));
                        } else {
                            getHikari().logger().warn("Entry {} is invalid.", cursor);
                        }
                        buffer = new StringBuilder();
                    }
                    buffer.append(cursor);
                } else {
                    buffer.append(cursor);
                }
            }
            writer.flush();
        }
    }

    @Data
    @AllArgsConstructor
    static class DatedLogEntry {
        private Date date;
        private String content;
    }

    @Nullable
    private DatedLogEntry _formatLine(String line) throws DateTimeParseException {
        String json = StringUtils.substringAfter(line, "] ");
        if (!CommonUtil.isJson(json)) return null;
        String dateStr = line.substring(1, template.length() - 1).trim();
        TemporalAccessor accessor = DATETIME_FORMATTER.parse(dateStr);
        return new DatedLogEntry(new Date(accessor.getLong(ChronoField.MILLI_OF_SECOND)), json);
    }

    private boolean _isNewLineStart(String line) {
        if (!line.startsWith("[")) return false;
        if (line.length() < template.length()) return false;
        return line.endsWith("] ");
    }

    private void migrateAppendedLogs(File logsFile, File filteredFile) throws IOException {
        text("modules.shop-logs.filter-history-files").send();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(logsFile));
        PrintWriter writer = new PrintWriter(filteredFile, StandardCharsets.UTF_8);
        long counter = 0;
        try (bufferedReader; writer) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("beforeTrading") && line.contains("player") && line.contains("holding")) {
                    continue;
                }
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                writer.println(line);
                counter++;
            }
        } finally {
            writer.flush();
            logsFile.delete();
            text("modules.shop-logs.filtered-history-files", counter);
        }
    }

    private void appendFiles(File dataFolder, PrintWriter logsFile) throws IOException {
        text("modules.shop-logs.extract-history-files").send();
        File logsSubFolder = new File(dataFolder, "logs");
        if (logsSubFolder.exists()) {
            File[] files = logsSubFolder.listFiles(f -> f.getName().endsWith(".log.gz"));
            if (files != null) {
                for (File file : files) {
                    try (GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(file))) {
                        String content = new String(gzipInputStream.readAllBytes(), StandardCharsets.UTF_8);
                        logsFile.println(content);
                        logsFile.flush();
                    }
                }
            }
        }
        File mainLogFile = new File(dataFolder, "qs.log");
        getHikari().logger().info("Selected main log file: " + mainLogFile.getAbsolutePath());
        if (mainLogFile.exists()) {
            try {
                logsFile.println(Files.readString(mainLogFile.toPath()));
            } catch (MalformedInputException e) {
                getHikari().logger().warn("Encoding determine failed, file not a UTF-8 encoding file, re-read by platform encoding...", e);
                logsFile.println(Files.readString(mainLogFile.toPath(), Charset.defaultCharset()));
            }
        } else {
            throw new IllegalStateException("Main qs.log log file not exists");
        }
    }
}
