package fr.rosstail.karma.commands.list;

public enum Commands {

    COMMAND_KARMA("", "karma"),
    COMMAND_KARMA_CALCULATE("calculate", "karma.calculate"),
    COMMAND_KARMA_CHECK("check", "karma.self"),
    COMMAND_KARMA_HELP("help", "karma.help"),
    COMMAND_KARMA_SET("set", "karma.set"),
    COMMAND_KARMA_ADD("add", "karma.add"),
    COMMAND_KARMA_REMOVE("remove", "karma.remove"),
    COMMAND_KARMA_RESET("reset", "karma.reset"),
    COMMAND_KARMA_OTHER("check", "karma.other");

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
