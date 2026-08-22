package com.ghostchu.quickshop.util.holder;

import java.util.Objects;

public class Result {

  public static final Result SUCCESS = new Result() {
    @Override
    public String getMessage() {

      return "";
    }

    @Override
    public void setMessage(final String message) {

    }

    @Override
    public void setResult(final boolean result) {

    }

    @Override
    public String getListener() {

      return "";
    }

    @Override
    public boolean isSuccess() {

      return true;
    }
  };
  private boolean result = false;
  private String message;
  private String listener;


  public Result() {

  }

  public Result(final String message) {

    result = false;
    this.message = message;
  }

  public boolean isSuccess() {

    return result;
  }

  @Override
  public String toString() {

    return "Result(result=" + this.result + ", message=" + this.getMessage() + ", listener=" + this.getListener() + ")";
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof Result)) return false;
    final Result other = (Result)o;
    return this.result == other.result
           && Objects.equals(this.getMessage(), other.getMessage())
           && Objects.equals(this.getListener(), other.getListener());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.result, this.getMessage(), this.getListener());
  }

  public void setResult(final boolean result) {

    this.result = result;
  }

  public void setMessage(final String message) {

    this.message = message;
  }

  public String getMessage() {

    return this.message;
  }

  public String getListener() {

    return this.listener;
  }

  public void setListener(final String listener) {

    this.listener = listener;
  }
}
