package fr.rosstail.karma.lang;

/**
 * List of all messages identifiers
 */
public enum LangMessage {

    BY_PLAYER_ONLY("by-player-only"),
    CREATING_PLAYER_DATA_FOLDER("creating-playerdata-folder"),
    CREATING_PLAYER("creating-player"),
    DISCONNECTED_PLAYER("disconnected-player"),
    CHECK_OWN_KARMA("check-own-karma"),
    CHECK_OTHER_KARMA("check-other-karma"),
    SET_KARMA("set-karma"),
    ADD_KARMA("add-karma"),
    REMOVE_KARMA("remove-karma"),
    RESET_KARMA("reset-karma"),
    TIER_CHANGE("tier-change"),
    SELF_DEFENDING_OFF("self-defending-off"),
    SELF_DEFENDING_ON("self-defending-on"),
    PERMISSION_DENIED("permission-denied"),
    WRONG_VALUE("wrong-value");

    private final String id;

    LangMessage(String id) {
        this.id = id;
    }

    String getId() {
        return id;
    }
}
