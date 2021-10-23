package com.rosstail.karma.lang;

public enum LangMessage {

    BY_PLAYER_ONLY("by-player-only"),
    SET_KARMA("set-karma"),
    ADD_KARMA("add-karma"),
    REMOVE_KARMA("remove-karma"),
    RESET_KARMA("reset-karma"),
    DISCONNECTED("disconnected"),
    WRONG_VALUE("wrong-value"),
    CHECK_OTHER_KARMA("check-other-karma"),
    CHECK_OWN_KARMA("check-own-karma"),
    HELP("help"),
    PERMISSION_DENIED("permission-denied"),
    TIER_CHANGE("tier-change"),
    CALCULATION("calculation"),
    TOO_FEW_ARGUMENTS("too-few-arguments"),
    PVP_HIT_KARMA_INCREASE("pvp.hit-karma-increase"),
    PVP_HIT_KARMA_UNCHANGED("pvp.hit-karma-unchanged"),
    PVP_HIT_KARMA_DECREASE("pvp.hit-karma-decrease"),
    PVP_KILL_KARMA_INCREASE("pvp.kill-karma-increase"),
    PVP_KILL_KARMA_UNCHANGED("pvp.kill-karma-unchanged"),
    PVP_KILL_KARMA_DECREASE("pvp.kill-karma-decrease"),
    WANTED_ENTER("wanted.enter"),
    WANTED_REFRESH("wanted.refresh"),
    WANTED_EXIT("wanted.exit"),
    STATUS_INNOCENT("wanted.status.innocent"),
    STATUS_WANTED("wanted.status.wanted");

    private final String text;

    LangMessage(String text) {
        this.text = text;
    }

    String getText() {
        return this.text;
    }
}