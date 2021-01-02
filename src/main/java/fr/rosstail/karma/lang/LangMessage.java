package fr.rosstail.karma.lang;

public enum LangMessage {

    BY_PLAYER_ONLY("by-player-only"),
    DISCONNECTED_PLAYER("disconnected-player"),
    CREATING_PLAYER_DATA_FOLDER("creating-playerdata-folder"),
    CREATING_PLAYER("creating-player"),
    PERMISSION_DENIED("permission-denied"),
    CHECK_ALL_STATS("check-all-stats"),
    NO_CLASS("no-class"),
    WRONG_VALUE("wrong-value"),
    TOO_FEW_ARGUMENTS("too-few-arguments"),
    CLASS_LEVEL_UP("class-level-up"),
    CLASS_CHOICE_GUI("class-choice-gui"),
    CLASS_GENERAL_INFO("class-general-info"),
    CLASS_REQUREMENTS_INFO("class-requirement-info"),
    CLASS_BASE_INFO("class-base-info"),
    CLASS_MAXIMUM_INFO("class-max-info"),
    CLASS_GROWTH_INFO("class-growth-info");
    private final String id;

    LangMessage(String id) {
        this.id = id;
    }

    String getId() {
        return id;
    }
}