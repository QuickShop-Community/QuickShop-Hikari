package com.ghostchu.quickshop.watcher;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.CalendarEvent;
import com.ghostchu.quickshop.util.Util;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;

/**
 * CalendarWatcher check-call calendar events related stuffs
 *
 * @author Ghost_chu
 */
public class CalendarWatcher extends BukkitRunnable {
    @Getter
    private final File calendarFile = new File(Util.getCacheFolder(), "calendar.cache");
    @Getter
    private final YamlConfiguration configuration;
    private final QuickShop plugin;
    private BukkitTask task;

    public CalendarWatcher(QuickShop plugin) {
        this.plugin = plugin;
        if (!calendarFile.exists()) {
            try {
                calendarFile.createNewFile();
            } catch (IOException ioException) {
                plugin.getLogger().log(Level.WARNING, "Cannot create calendar cache file at " + calendarFile.getAbsolutePath() + ", scheduled tasks may cannot or execute wrongly!", ioException);
            }
        }
        configuration = YamlConfiguration.loadConfiguration(calendarFile);
    }

    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {
        CalendarEvent.CalendarTriggerType type = getAndUpdate();
        Util.mainThreadRun(() -> new CalendarEvent(type).callEvent());
    }

    public CalendarEvent.CalendarTriggerType getAndUpdate() {
        Calendar c = Calendar.getInstance();
        CalendarEvent.CalendarTriggerType type = CalendarEvent.CalendarTriggerType.NOTHING_CHANGED;
        int secondRecord = configuration.getInt("second");
        int minuteRecord = configuration.getInt("minute");
        int hourRecord = configuration.getInt("hour");
        int dayRecord = configuration.getInt("day");
        int weekRecord = configuration.getInt("week");
        int monthRecord = configuration.getInt("month");
        int yearRecord = configuration.getInt("year");
        int secondNow = c.get(Calendar.SECOND);
        int minuteNow = c.get(Calendar.MINUTE);
        int hourNow = c.get(Calendar.HOUR_OF_DAY);
        int dayNow = c.get(Calendar.DAY_OF_MONTH);
        int weekNow = c.get(Calendar.WEEK_OF_MONTH);
        int monthNow = c.get(Calendar.MONTH);
        int yearNow = c.get(Calendar.YEAR);
        if (secondNow != secondRecord) {
            type = CalendarEvent.CalendarTriggerType.SECOND;
        }
        if (minuteNow != minuteRecord) {
            type = CalendarEvent.CalendarTriggerType.MINUTE;
        }
        if (hourNow != hourRecord) {
            type = CalendarEvent.CalendarTriggerType.HOUR;
        }
        if (dayNow != dayRecord) {
            type = CalendarEvent.CalendarTriggerType.DAY;
        }
        if (weekNow != weekRecord) {
            type = CalendarEvent.CalendarTriggerType.WEEK;
        }
        if (monthNow != monthRecord) {
            type = CalendarEvent.CalendarTriggerType.MONTH;
        }
        if (yearNow != yearRecord) {
            type = CalendarEvent.CalendarTriggerType.YEAR;
        }


        configuration.set("second", secondNow);
        configuration.set("minute", minuteNow);
        configuration.set("hour", hourNow);
        configuration.set("day", dayNow);
        configuration.set("week", weekNow);
        configuration.set("month", monthNow);
        configuration.set("year", yearNow);
        //We can use ordinal() to check we need update or not
        //If we need update every minute, use type.ordinal() >= MINUTE


        if (type.ordinal() >= CalendarEvent.CalendarTriggerType.HOUR.ordinal()) {
            save();
        }

        return type;
    }

    public void start() {
        task = this.runTaskTimerAsynchronously(plugin, 20, 20);
    }

    public void stop() {
        save();
        try {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        } catch (IllegalStateException ignored) {
        }
    }

    public void save() {
        try {
            configuration.save(calendarFile);
        } catch (IOException ioException) {
            plugin.getLogger().log(Level.WARNING, "Cannot save calendar cache file at " + calendarFile.getAbsolutePath() + ", scheduled tasks may cannot or execute wrongly!", ioException);
        }
    }
}
