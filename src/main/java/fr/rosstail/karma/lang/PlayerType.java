package fr.rosstail.karma.lang;

public enum PlayerType {
    player("player"),
    attacker("attacker"),
    victim("victim");

    private final String id;

    PlayerType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
