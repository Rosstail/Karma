package com.rosstail.karma.events;

public enum Reasons {

    HIT("hit"),
    KILL("kill");

    private final String text;

    Reasons(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
