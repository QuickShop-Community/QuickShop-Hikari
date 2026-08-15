package com.ghostchu.quickshop.api.event;

/**
 * Trigger while a calendar data was changed by Calendar Watcher. Useful for quickshop simple
 * scheduler.
 */

public class CalendarEvent extends AbstractQSEvent {

  private CalendarTriggerType calendarTriggerType;

  public CalendarEvent(final CalendarTriggerType calendarTriggerType) {

    this.calendarTriggerType = calendarTriggerType;
  }

  public enum CalendarTriggerType {
    NOTHING_CHANGED, SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof CalendarEvent)) return false;
    final CalendarEvent other = (CalendarEvent)o;
    if(!other.canEqual((Object)this)) return false;
    if(!super.equals(o)) return false;
    final Object thisCalendarTriggerType = this.getCalendarTriggerType();
    final Object otherCalendarTriggerType = other.getCalendarTriggerType();
    if(thisCalendarTriggerType == null? otherCalendarTriggerType != null : !thisCalendarTriggerType.equals(otherCalendarTriggerType)) {
      return false;
    }
    return true;
  }

  protected boolean canEqual(final Object other) {

    return other instanceof CalendarEvent;
  }

  @Override
  public int hashCode() {

    final int PRIME = 59;
    int result = super.hashCode();
    final Object calendarTriggerTypeValue = this.getCalendarTriggerType();
    result = result * PRIME + (calendarTriggerTypeValue == null? 43 : calendarTriggerTypeValue.hashCode());
    return result;
  }

  public CalendarTriggerType getCalendarTriggerType() {

    return this.calendarTriggerType;
  }

  public void setCalendarTriggerType(final CalendarTriggerType calendarTriggerType) {

    this.calendarTriggerType = calendarTriggerType;
  }

  @Override
  public String toString() {

    return "CalendarEvent(calendarTriggerType=" + this.getCalendarTriggerType() + ")";
  }
}
