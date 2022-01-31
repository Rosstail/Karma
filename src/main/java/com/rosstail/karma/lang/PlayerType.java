package com.rosstail.karma.lang;

public enum PlayerType {
    PLAYER("player"),
    ATTACKER("attacker"),
    VICTIM("victim");

    private final String text;
    PlayerType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
