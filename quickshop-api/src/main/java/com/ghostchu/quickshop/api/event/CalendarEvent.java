package com.ghostchu.quickshop.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Trigger while a calendar data was changed by Calendar Watcher.
 * Useful for quickshop simple scheduler.
 */
@EqualsAndHashCode(callSuper = true)
@Data

public class CalendarEvent extends AbstractQSEvent {
    private CalendarTriggerType calendarTriggerType;

    public CalendarEvent(CalendarTriggerType calendarTriggerType) {
        this.calendarTriggerType = calendarTriggerType;
    }

    public enum CalendarTriggerType {
        NOTHING_CHANGED, SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR
    }
}
