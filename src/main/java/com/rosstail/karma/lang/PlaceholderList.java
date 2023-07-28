package com.rosstail.karma.lang;

public enum PlaceholderList {
    PLAYER_KARMA("player_karma"),
    PLAYER_PREVIOUS_KARMA("player_previous_karma");

    private final String value;

    PlaceholderList(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
