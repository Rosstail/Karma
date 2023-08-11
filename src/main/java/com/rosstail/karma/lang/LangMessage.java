package com.rosstail.karma.lang;

public enum LangMessage {
    PLUGIN_PREFIX("prefix", true),

    COMMANDS_BY_PLAYER_ONLY("commands.by-player-only", false),
    COMMANDS_PERMISSION_DENIED("commands.permission-denied", false),
    COMMANDS_INSERT_PLAYER_NAME("commands.insert-player-name", false),
    COMMANDS_WRONG_VALUE("commands.wrong-value", false),
    COMMANDS_WRONG_COMMAND("commands.wrong-command", false),
    COMMANDS_PLAYER_DOES_NOT_EXIST("commands.player-does-not-exist", false),
    COMMANDS_PLAYER_NO_DATA("commands.player-no-data", false),

    COMMANDS_HELP_HEADER("commands.help.header", false),

    COMMANDS_HELP_LINE("commands.help.line", false),

    COMMANDS_CHECK_DESC("commands.check.desc", false),
    COMMANDS_CHECK_SELF_DESC("commands.check.self.desc", false),
    COMMANDS_CHECK_SELF_RESULT("commands.check.self.result", false),
    COMMANDS_CHECK_OTHER_DESC("commands.check.other.desc", false),
    COMMANDS_CHECK_OTHER_RESULT("commands.check.other.result", false),

    COMMANDS_EDIT_DESC("commands.edit.desc", false),

    COMMANDS_EDIT_PLAYER_DESC("commands.edit.player.desc", false),
    COMMANDS_EDIT_PLAYER_DISCONNECTED("commands.edit.player.disconnected-player", false),
    COMMANDS_EDIT_PLAYER_NO_DATA("commands.edit.player.player-no-data", false),
    COMMANDS_EDIT_PLAYER_OUT_OF_BOUNDS("commands.edit.player.out-of-bounds", false),

    COMMANDS_EDIT_PLAYER_KARMA_DESC("commands.edit.player.karma.desc", false),
    COMMANDS_EDIT_PLAYER_KARMA_SET_DESC("commands.edit.player.karma.set.desc", false),
    COMMANDS_EDIT_PLAYER_KARMA_SET_RESULT("commands.edit.player.karma.set.result", false),
    COMMANDS_EDIT_PLAYER_KARMA_ADD_DESC("commands.edit.player.karma.add.desc", false),
    COMMANDS_EDIT_PLAYER_KARMA_ADD_RESULT("commands.edit.player.karma.add.result", false),
    COMMANDS_EDIT_PLAYER_KARMA_REMOVE_DESC("commands.edit.player.karma.remove.desc", false),
    COMMANDS_EDIT_PLAYER_KARMA_REMOVE_RESULT("commands.edit.player.karma.remove.result", false),
    COMMANDS_EDIT_PLAYER_KARMA_RESET_DESC("commands.edit.player.karma.reset.desc", false),
    COMMANDS_EDIT_PLAYER_KARMA_RESET_RESULT("commands.edit.player.karma.reset.result", false),

    COMMANDS_EDIT_PLAYER_TIER_DESC("commands.edit.player.tier.desc", false),
    COMMANDS_EDIT_PLAYER_TIER_SET_DESC("commands.edit.player.tier.set.desc", false),
    COMMANDS_EDIT_PLAYER_TIER_SET_RESULT("commands.edit.player.tier.set.result", false),

    COMMANDS_EDIT_PLAYER_WANTED_DESC("commands.edit.player.wanted.desc", false),
    COMMANDS_EDIT_PLAYER_WANTED_SET_DESC("commands.edit.player.wanted.set.desc", false),
    COMMANDS_EDIT_PLAYER_WANTED_SET_RESULT("commands.edit.player.wanted.set.result", false),
    COMMANDS_EDIT_PLAYER_WANTED_ADD_DESC("commands.edit.player.wanted.add.desc", false),
    COMMANDS_EDIT_PLAYER_WANTED_ADD_RESULT("commands.edit.player.wanted.add.result", false),
    COMMANDS_EDIT_PLAYER_WANTED_REMOVE_DESC("commands.edit.player.wanted.remove.desc", false),
    COMMANDS_EDIT_PLAYER_WANTED_REMOVE_RESULT("commands.edit.player.wanted.remove.result", false),
    COMMANDS_EDIT_PLAYER_WANTED_RESET_DESC("commands.edit.player.wanted.reset.desc", false),
    COMMANDS_EDIT_PLAYER_WANTED_RESET_RESULT("commands.edit.player.wanted.reset.result", false),


    COMMANDS_SHOP_HEADER("commands.shop.header", false),
    COMMANDS_SHOP_LINE("commands.shop.line", false),
    COMMANDS_SHOP_NOT_EXIST("commands.shop.not-exist", false),
    COMMANDS_SHOP_BUY_DESC("commands.shop.buy.desc", false),
    COMMANDS_SHOP_BUY_SUCCESS("commands.shop.buy.success", false),
    COMMANDS_SHOP_BUY_FAILURE("commands.shop.buy.failure", false),


    COMMANDS_RELOAD_DESC("commands.reload.desc", false),
    COMMANDS_RELOAD_RESULT("commands.reload.result", false),

    COMMANDS_SAVE_DESC("commands.save.desc", false),
    COMMANDS_SAVE_RESULT("commands.save.result", false),

    COMMANDS_EVAL_DESC("commands.eval.desc", false),
    COMMANDS_EVAL_RESULT("commands.eval.result", false),

    PLAYER_ONLINE("player.online", false),
    PLAYER_OFFLINE("player.offline", false),

    STORAGE_TYPE("storage.type", false),

    KARMA_ON_CHANGE("karma.on-change", true),
    TIER_ON_CHANGE("tier.on-change", true),
    TIER_NONE_DISPLAY("tier.none.display", false),
    TIER_NONE_SHORT_DISPLAY("tier.none.short-display", false),


    FIGHT_PVP_HIT_ON_KARMA_GAIN("fight.pvp.hit.on-karma-gain", true),
    FIGHT_PVP_HIT_ON_KARMA_UNCHANGED("fight.pvp.hit.on-karma-unchanged", true),
    FIGHT_PVP_HIT_ON_KARMA_LOSS("fight.pvp.hit.on-karma-loss", true),
    FIGHT_PVP_KILL_ON_KARMA_GAIN("fight.pvp.kill.on-karma-gain", true),
    FIGHT_PVP_KILL_ON_KARMA_UNCHANGED("fight.pvp.kill.on-karma-unchanged", true),
    FIGHT_PVP_KILL_ON_KARMA_LOSS("fight.pvp.kill.on-karma-loss", true),

    FIGHT_PVE_HIT_ON_KARMA_GAIN("fight.pve.hit.on-karma-gain", true),
    FIGHT_PVE_HIT_ON_KARMA_UNCHANGED("fight.pve.hit.on-karma-unchanged", true),
    FIGHT_PVE_HIT_ON_KARMA_LOSS("fight.pve.hit.on-karma-loss", true),
    FIGHT_PVE_KILL_ON_KARMA_GAIN("fight.pve.kill.on-karma-gain", true),
    FIGHT_PVE_KILL_ON_KARMA_UNCHANGED("fight.pve.kill.on-karma-unchanged", true),
    FIGHT_PVE_KILL_ON_KARMA_LOSS("fight.pve.kill.on-karma-loss", true),

    WANTED_STATUS_INNOCENT("wanted.status.innocent.display", false),
    WANTED_STATUS_INNOCENT_SHORT("wanted.status.innocent.short", false),
    WANTED_STATUS_WANTED("wanted.status.wanted.display", false),
    WANTED_STATUS_WANTED_SHORT("wanted.status.wanted.short", false),

    WANTED_EVENT_ON_ENTER("wanted.event.on-enter", true),
    WANTED_EVENT_ON_REFRESH("wanted.event.on-refresh", true),
    WANTED_EVENT_ON_EXIT("wanted.event.on-exit", true),
    FORMAT_DATETIME("format.datetime", false),
    FORMAT_DATETIME_NEVER("format.datetime-never", false),
    FORMAT_COUNTDOWN("format.countdown", false),
    ;

    private final String text;
    private String displayText;
    private final boolean nullable;

    LangMessage(String text, boolean nullable) {
        this.text = text;
        this.nullable = nullable;
    }

    String getText() {
        return this.text;
    }

    public String getDisplayText() {
        return displayText;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }
}