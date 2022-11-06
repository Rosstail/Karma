package com.rosstail.karma.lang;

public enum LangMessage {

    BY_PLAYER_ONLY("by-player-only"),
    SET_KARMA("edit.set-karma"),
    ADD_KARMA("edit.add-karma"),
    REMOVE_KARMA("edit.remove-karma"),
    RESET_KARMA("edit.reset-karma"),
    DISCONNECTED("disconnected"),
    WRONG_VALUE("wrong-value"),
    CHECK_OTHER_KARMA("check.other"),
    CHECK_OWN_KARMA("check.own"),
    HELP_HEADER("help.header"),
    HELP_CALCULATE("help.list.calculate"),
    HELP_CHECK("help.list.check"),
    HELP_EDIT("help.list.edit.default"),
    HELP_EDIT_SET("help.list.edit.set"),
    HELP_EDIT_ADD("help.list.edit.add"),
    HELP_EDIT_REMOVE("help.list.edit.remove"),
    HELP_EDIT_RESET("help.list.edit.reset"),
    HELP_WANTED("help.list.wanted.default"),
    HELP_WANTED_CHECK("help.list.wanted.check"),
    HELP_WANTED_EDIT("help.list.wanted.edit.default"),
    HELP_WANTED_EDIT_SET("help.list.wanted.edit.set"),
    HELP_WANTED_EDIT_ADD("help.list.wanted.edit.add"),
    HELP_WANTED_EDIT_REMOVE("help.list.wanted.edit.remove"),
    HELP_WANTED_EDIT_RESET("help.list.wanted.edit.reset"),
    HELP_RELOAD("help.list.reload"),
    HELP_SAVE("help.list.save"),
    HELP_SHOP("help.list.shop"),
    PERMISSION_DENIED("permission-denied"),
    TIER_CHANGE("tier-change"),
    CALCULATION("calculation"),
    TOO_FEW_ARGUMENTS("too-few-arguments"),
    SAVED_DATA("saved-data"),
    PVP_HIT_KARMA_INCREASE("pvp.hit-karma-increase"),
    PVP_HIT_KARMA_UNCHANGED("pvp.hit-karma-unchanged"),
    PVP_HIT_KARMA_DECREASE("pvp.hit-karma-decrease"),
    PVP_KILL_KARMA_INCREASE("pvp.kill-karma-increase"),
    PVP_KILL_KARMA_UNCHANGED("pvp.kill-karma-unchanged"),
    PVP_KILL_KARMA_DECREASE("pvp.kill-karma-decrease"),
    SET_WANTED("wanted.edit.set-wanted"),
    ADD_WANTED("wanted.edit.add-wanted"),
    REMOVE_WANTED("wanted.edit.remove-wanted"),
    RESET_WANTED("wanted.edit.reset-wanted"),
    WANTED_OWN_CHECK("wanted.check.own"),
    WANTED_OTHER_CHECK("wanted.check.other"),
    WANTED_ENTER("wanted.enter"),
    WANTED_REFRESH("wanted.refresh"),
    WANTED_CONNECT_REFRESH("wanted.connect-refresh"),
    WANTED_EXIT("wanted.exit"),
    SHOP_HEADER("shop.list.header"),
    SHOP_LINE("shop.list.line"),
    SHOP_SUCCESS("shop.success"),
    SHOP_FAILURE("shop.failure"),
    SHOP_NOT_EXIST("shop.not-exist"),
    STATUS_INNOCENT("wanted.status.innocent"),
    STATUS_INNOCENT_SHORT("wanted.status.innocent-short"),
    STATUS_WANTED("wanted.status.wanted"),
    STATUS_WANTED_SHORT("wanted.status.wanted-short"),
    CONFIG_RELOAD("reload");

    private final String text;

    LangMessage(String text) {
        this.text = text;
    }

    String getText() {
        return this.text;
    }
}