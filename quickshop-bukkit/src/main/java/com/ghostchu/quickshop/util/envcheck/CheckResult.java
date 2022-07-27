package com.ghostchu.quickshop.util.envcheck;

public enum CheckResult {
    SKIPPED("Skip"), PASSED("Pass"), WARNING("Warning"), STOP_WORKING("Error"), DISABLE_PLUGIN("Fatal");
    private final String display;

    CheckResult(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
