package com.rosstail.karma.events;

public enum Cause {

    HIT("hit"),
    KILL("kill");

    private final String text;

    Cause(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
