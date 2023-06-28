package com.rosstail.karma.lang;

public enum PlaceholderList {
    PLAYER_KARMA("%player_karma%", "%karma_player_karma%"),
    PLAYER_PREVIOUS_KARMA("%player_previous_karma%", "%karma_player_previous_karma%");

    private final String local;
    private final String papi;

    PlaceholderList(String local, String papi) {
        this.local = local;
        this.papi = papi;
    }

    public String getLocal() {
        return local;
    }

    public String getPapi() {
        return papi;
    }
}
