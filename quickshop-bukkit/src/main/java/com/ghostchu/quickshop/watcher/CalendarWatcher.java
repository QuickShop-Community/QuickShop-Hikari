package com.ghostchu.quickshop.watcher;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.CalendarEvent;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 * CalendarWatcher check-call calendar events related stuffs
 *
 * @author Ghost_chu
 */
public class CalendarWatcher implements Runnable {

  @Getter
  private final File calendarFile = new File(Util.getCacheFolder(), "calendar.cache");
  @Getter
  private final YamlConfiguration configuration;
  private final QuickShop plugin;
  private WrappedTask task;

  public CalendarWatcher(final QuickShop plugin) {

    this.plugin = plugin;
    if(!calendarFile.exists()) {
      try {
        calendarFile.createNewFile();
      } catch(IOException ioException) {
        plugin.logger().warn("Cannot create calendar cache file at {}, scheduled tasks may cannot or execute wrongly!", calendarFile.getAbsolutePath(), ioException);
      }
    }
    configuration = YamlConfiguration.loadConfiguration(calendarFile);
  }

  /**
   * The action to be performed by this timer task.
   */
  @Override
  public void run() {

    final CalendarEvent.CalendarTriggerType type = getAndUpdate();
    Util.mainThreadRun(()->new CalendarEvent(type).callEvent());
  }

  public CalendarEvent.CalendarTriggerType getAndUpdate() {

    final Calendar c = Calendar.getInstance();
    CalendarEvent.CalendarTriggerType type = CalendarEvent.CalendarTriggerType.NOTHING_CHANGED;
    final int secondRecord = configuration.getInt("second");
    final int minuteRecord = configuration.getInt("minute");
    final int hourRecord = configuration.getInt("hour");
    final int dayRecord = configuration.getInt("day");
    final int weekRecord = configuration.getInt("week");
    final int monthRecord = configuration.getInt("month");
    final int yearRecord = configuration.getInt("year");
    final int secondNow = c.get(Calendar.SECOND);
    final int minuteNow = c.get(Calendar.MINUTE);
    final int hourNow = c.get(Calendar.HOUR_OF_DAY);
    final int dayNow = c.get(Calendar.DAY_OF_MONTH);
    final int weekNow = c.get(Calendar.WEEK_OF_MONTH);
    final int monthNow = c.get(Calendar.MONTH);
    final int yearNow = c.get(Calendar.YEAR);
    if(secondNow != secondRecord) {
      type = CalendarEvent.CalendarTriggerType.SECOND;
    }
    if(minuteNow != minuteRecord) {
      type = CalendarEvent.CalendarTriggerType.MINUTE;
    }
    if(hourNow != hourRecord) {
      type = CalendarEvent.CalendarTriggerType.HOUR;
    }
    if(dayNow != dayRecord) {
      type = CalendarEvent.CalendarTriggerType.DAY;
    }
    if(weekNow != weekRecord) {
      type = CalendarEvent.CalendarTriggerType.WEEK;
    }
    if(monthNow != monthRecord) {
      type = CalendarEvent.CalendarTriggerType.MONTH;
    }
    if(yearNow != yearRecord) {
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


    if(type.ordinal() >= CalendarEvent.CalendarTriggerType.HOUR.ordinal()) {
      save();
    }

    return type;
  }

  public void save() {

    try {
      configuration.save(calendarFile);
    } catch(IOException ioException) {
      plugin.logger().warn("Cannot save calendar cache file at {}, scheduled tasks may cannot or execute wrongly!", calendarFile.getAbsolutePath(), ioException);
    }
  }

  public void start() {

    task = QuickShop.folia().getImpl().runTimerAsync(this, 20, 20);
  }

  public void stop() {

    save();
    try {
      if(task != null && !task.isCancelled()) {
        task.cancel();
      }
    } catch(IllegalStateException ex) {
      Log.debug("Task already cancelled " + ex.getMessage());
    }
  }
}
