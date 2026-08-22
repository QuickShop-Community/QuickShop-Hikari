package com.ghostchu.quickshop.api.localization.text;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class ProxiedLocale {

  private Locale locale;
  @Nullable
  private String origin;
  private String relative;
  private NumberFormat nf;

  public ProxiedLocale(@Nullable final String origin, final String relative, @NotNull final NumberFormat nf, @NotNull final Locale locale) {

    this.origin = origin;
    this.relative = relative;
    this.nf = nf;
    this.locale = locale;
  }

  public String getLocale() {

    return relative;
  }

  @NotNull
  public NumberFormat getNumberFormat() {

    return nf;
  }

  @NotNull
  public Locale getJavaLocale() {

    return locale;
  }

  @Nullable
  public String getOrigin() {

    return this.origin;
  }

  public String getRelative() {

    return this.relative;
  }

  public NumberFormat getNf() {

    return this.nf;
  }

  public void setLocale(final Locale locale) {

    this.locale = locale;
  }

  public void setOrigin(@Nullable final String origin) {

    this.origin = origin;
  }

  public void setRelative(final String relative) {

    this.relative = relative;
  }

  public void setNf(final NumberFormat nf) {

    this.nf = nf;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof ProxiedLocale)) return false;
    final ProxiedLocale other = (ProxiedLocale)o;
    return Objects.equals(this.getLocale(), other.getLocale())
           && Objects.equals(this.getOrigin(), other.getOrigin())
           && Objects.equals(this.getRelative(), other.getRelative())
           && Objects.equals(this.getNf(), other.getNf());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getLocale(), this.getOrigin(), this.getRelative(), this.getNf());
  }

  @Override
  public String toString() {

    return "ProxiedLocale(locale=" + this.getLocale() + ", origin=" + this.getOrigin() + ", relative=" + this.getRelative() + ", nf=" + this.getNf() + ")";
  }
}
