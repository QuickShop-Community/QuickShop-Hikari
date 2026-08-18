package com.ghostchu.quickshop.util;

import java.util.Objects;
import java.util.UUID;

public class Profile {

  private final UUID uniqueId;
  private final String name;

  public Profile(final UUID uniqueId, final String name) {

    this.uniqueId = uniqueId;
    this.name = name;
  }

  public UUID getUniqueId() {

    return this.uniqueId;
  }

  public String getName() {

    return this.name;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof Profile)) return false;
    final Profile other = (Profile)o;
    return Objects.equals(this.getUniqueId(), other.getUniqueId())
           && Objects.equals(this.getName(), other.getName());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getUniqueId(), this.getName());
  }

  @Override
  public String toString() {

    return "Profile(uniqueId=" + this.getUniqueId() + ", name=" + this.getName() + ")";
  }
}
