package fr.rosstail.karma.commands;

public enum Commands {

    COMMAND_KARMA("karma", "karma"),
    COMMAND_KARMA_HELP("karma", "karma"),
    COMMAND_KARMA_SET("karma", "karma"),
    COMMAND_KARMA_ADD("karma", "karma"),
    COMMAND_KARMA_REMOVE("karma", "karma"),
    COMMAND_KARMA_RESET("karma", "karma"),
    COMMAND_KARMA_OTHER("karma", "karma");

    private final String command;
    private final String permission;

    Commands(String command, String permission) {
        this.command = command;
        this.permission = permission;
    }

    public String getCommand() {
        return command;
    }

    public String getPermission() {
        return permission;
    }
}
