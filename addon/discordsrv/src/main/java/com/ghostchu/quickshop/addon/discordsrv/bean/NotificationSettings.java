package com.ghostchu.quickshop.addon.discordsrv.bean;

import java.util.Map;
import java.util.Objects;

public class NotificationSettings {

  private Map<NotificationFeature, Boolean> settings;

  public NotificationSettings(final Map<NotificationFeature, Boolean> settings) {

    this.settings = settings;
  }

  public NotificationSettings() {

  }

  public Map<NotificationFeature, Boolean> getSettings() {

    return this.settings;
  }

  public void setSettings(final Map<NotificationFeature, Boolean> settings) {

    this.settings = settings;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof NotificationSettings)) return false;
    final NotificationSettings other = (NotificationSettings)o;
    return Objects.equals(this.getSettings(), other.getSettings());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getSettings());
  }

  @Override
  public String toString() {

    return "NotificationSettings(settings=" + this.getSettings() + ")";
  }
}
