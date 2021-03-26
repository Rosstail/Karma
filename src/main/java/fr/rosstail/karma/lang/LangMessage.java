package fr.rosstail.karma.lang;

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
    SELF_DEFENDING_OFF("self-defending-off"),
    SELF_DEFENDING_ON("self-defending-on"),
    TOO_FEW_ARGUMENTS("too-few-arguments");

    private final String text;

    LangMessage(String text) {
        this.text = text;
    }

    String getText() {
        return this.text;
    }
}