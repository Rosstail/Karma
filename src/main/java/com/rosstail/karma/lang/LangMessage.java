package com.rosstail.karma.lang;

public enum LangMessage {
    PLUGIN_PREFIX("prefix"),

    COMMANDS_BY_PLAYER_ONLY("commands.by-player-only"),
    COMMANDS_PERMISSION_DENIED("commands.permission-denied"),
    COMMANDS_WRONG_VALUE("commands.wrong-value"),
    COMMANDS_WRONG_COMMAND("commands.wrong-command"),
    COMMANDS_PLAYER_DOES_NOT_EXIST("commands.player-does-not-exist"),
    COMMANDS_PLAYER_NO_DATA("commands.player-no-data"),

    COMMANDS_HELP_HEADER("commands.help.header"),
    COMMANDS_HELP_LINE("commands.help.line"),

    COMMANDS_CHECK_DESC("commands.check.desc"),
    COMMANDS_CHECK_SELF_DESC("commands.check.self.desc"),
    COMMANDS_CHECK_SELF_RESULT("commands.check.self.result"),
    COMMANDS_CHECK_OTHER_DESC("commands.check.other.desc"),
    COMMANDS_CHECK_OTHER_RESULT("commands.check.other.result"),

    COMMANDS_EDIT_DESC("commands.edit.desc"),

    COMMANDS_EDIT_PLAYER_DESC("commands.edit.player.desc"),
    COMMANDS_EDIT_PLAYER_DISCONNECTED("commands.edit.player.disconnected-player"),

    COMMANDS_EDIT_PLAYER_KARMA_DESC("commands.edit.player.karma.desc"),
    COMMANDS_EDIT_PLAYER_KARMA_SET_DESC("commands.edit.player.karma.set.desc"),
    COMMANDS_EDIT_PLAYER_KARMA_SET_RESULT("commands.edit.player.karma.set.result"),
    COMMANDS_EDIT_PLAYER_KARMA_ADD_DESC("commands.edit.player.karma.add.desc"),
    COMMANDS_EDIT_PLAYER_KARMA_ADD_RESULT("commands.edit.player.karma.add.result"),
    COMMANDS_EDIT_PLAYER_KARMA_REMOVE_DESC("commands.edit.player.karma.remove.desc"),
    COMMANDS_EDIT_PLAYER_KARMA_REMOVE_RESULT("commands.edit.player.karma.remove.result"),
    COMMANDS_EDIT_PLAYER_KARMA_RESET_DESC("commands.edit.player.karma.reset.desc"),
    COMMANDS_EDIT_PLAYER_KARMA_RESET_RESULT("commands.edit.player.karma.reset.result"),

    COMMANDS_EDIT_PLAYER_TIER_DESC("commands.edit.player.tier.desc"),
    COMMANDS_EDIT_PLAYER_TIER_RESULT("commands.edit.player.tier.result"),
    COMMANDS_EDIT_PLAYER_TIER_SET_DESC("commands.edit.player.tier.set.desc"),
    COMMANDS_EDIT_PLAYER_TIER_SET_RESULT("commands.edit.player.tier.set.result"),

    COMMANDS_EDIT_PLAYER_WANTED_DESC("commands.edit.player.wanted.desc"),
    COMMANDS_EDIT_PLAYER_WANTED_RESULT("commands.edit.player.wanted.result"),
    COMMANDS_EDIT_PLAYER_WANTED_SET_DESC("commands.edit.player.wanted.set.desc"),
    COMMANDS_EDIT_PLAYER_WANTED_SET_RESULT("commands.edit.player.wanted.set.result"),
    COMMANDS_EDIT_PLAYER_WANTED_ADD_DESC("commands.edit.player.wanted.add.desc"),
    COMMANDS_EDIT_PLAYER_WANTED_ADD_RESULT("commands.edit.player.wanted.add.result"),
    COMMANDS_EDIT_PLAYER_WANTED_REMOVE_DESC("commands.edit.player.wanted.remove.desc"),
    COMMANDS_EDIT_PLAYER_WANTED_REMOVE_RESULT("commands.edit.player.wanted.remove.result"),
    COMMANDS_EDIT_PLAYER_WANTED_RESET_DESC("commands.edit.player.wanted.reset.desc"),
    COMMANDS_EDIT_PLAYER_WANTED_RESET_RESULT("commands.edit.player.wanted.reset.result"),


    COMMANDS_SHOP_HEADER("commands.shop.header"),
    COMMANDS_SHOP_LINE("commands.shop.line"),
    COMMANDS_SHOP_NOT_EXIST("commands.shop.not-exist"),
    COMMANDS_SHOP_BUY_DESC("commands.shop.buy.desc"),
    COMMANDS_SHOP_BUY_SUCCESS("commands.shop.buy.success"),
    COMMANDS_SHOP_BUY_FAILURE("commands.shop.buy.failure"),


    COMMANDS_RELOAD_DESC("commands.reload.desc"),
    COMMANDS_RELOAD_RESULT("commands.reload.result"),

    COMMANDS_SAVE_DESC("commands.save.desc"),
    COMMANDS_SAVE_RESULT("commands.save.result"),

    COMMANDS_CALCULATE_DESC("commands.calculate.desc"),
    COMMANDS_CALCULATE_RESULT("commands.calculate.result"),


    STORAGE_TYPE("storage.type"),
    KARMA_ON_CHANGE("karma.on-change"),
    TIER_ON_CHANGE("tier.on-change"),

    FIGHT_PVP_ON_ASSAULT("fight.pvp.on-assault"),
    FIGHT_PVP_ON_DEFEND("fight.pvp.on-defend"),
    FIGHT_PVP_HIT_ON_KARMA_GAIN("fight.pvp.hit.on-karma-gain"),
    FIGHT_PVP_HIT_ON_KARMA_UNCHANGED("fight.pvp.hit.on-karma-unchanged"),
    FIGHT_PVP_HIT_ON_KARMA_LOSS("fight.pvp.hit.on-karma-loss"),
    FIGHT_PVP_KILL_ON_KARMA_GAIN("fight.pvp.kill.on-karma-gain"),
    FIGHT_PVP_KILL_ON_KARMA_UNCHANGED("fight.pvp.kill.on-karma-unchanged"),
    FIGHT_PVP_KILL_ON_KARMA_LOSS("fight.pvp.kill.on-karma-loss"),

    FIGHT_PVE_HIT_ON_KARMA_GAIN("fight.pve.hit.on-karma-gain"),
    FIGHT_PVE_HIT_ON_KARMA_UNCHANGED("fight.pve.hit.on-karma-unchanged"),
    FIGHT_PVE_HIT_ON_KARMA_LOSS("fight.pve.hit.on-karma-loss"),
    FIGHT_PVE_KILL_ON_KARMA_GAIN("fight.pve.kill.on-karma-gain"),
    FIGHT_PVE_KILL_ON_KARMA_UNCHANGED("fight.pve.kill.on-karma-unchanged"),
    FIGHT_PVE_KILL_ON_KARMA_LOSS("fight.pve.kill.on-karma-loss"),

    WANTED_STATUS_INNOCENT("wanted.status.innocent"),
    WANTED_STATUS_INNOCENT_SHORT("wanted.status.innocent.short"),
    WANTED_STATUS_WANTED("wanted.status.wanted"),
    WANTED_STATUS_WANTED_SHORT("wanted.status.wanted.short"),
    WANTED_EVENT_ON_ENTER("wanted.event.wanted.on-enter"),
    WANTED_EVENT_ON_REFRESH("wanted.event.wanted.on-refresh"),
    WANTED_EVENT_ON_EXIT("wanted.event.wanted.on-exit"),
    ;

    private final String text;
    private final String displayText;

    LangMessage(String text) {
        this.text = text;
        this.displayText = AdaptMessage.getAdaptMessage().adaptMessage(this.text);
    }

    String getText() {
        return this.text;
    }

    public String getDisplayText() {
        return displayText;
    }
}