package com.ghostchu.quickshop.util.holder;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Result {

    public static final Result SUCCESS = new Result() {
        @Override
        public String getMessage() {
            return "";
        }

        @Override
        public void setMessage(String message) {
        }

        @Override
        public void setResult(boolean result) {
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
    @Setter
    private boolean result = false;
    @Setter
    @Getter
    private String message;
    @Getter
    @Setter
    private String listener;


    public Result() {
    }

    public Result(String message) {
        result = false;
        this.message = message;
    }

    public boolean isSuccess() {
        return result;
    }
}
