package com.ghostchu.quickshop.addon.displaycontrol.bean;

public enum ClientType {
    UNDISCOVERED,
    BEDROCK_EDITION_PLAYER_GEYSER,
    BEDROCK_EDITION_PLAYER_FLOODGATE,
    JAVA_EDITION_PLAYER;

    public boolean isBedrockType() {
        return this == BEDROCK_EDITION_PLAYER_FLOODGATE || this == BEDROCK_EDITION_PLAYER_GEYSER;
    }

    public boolean isWaitingDiscover() {
        return this == UNDISCOVERED;
    }
}
