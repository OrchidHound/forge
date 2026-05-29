package forge.gamemodes.quest;

public enum QuestPlaneswalker {
    NONE("None", ""),
    CHANDRA("Chandra", "Minimum deck size is reduced to 30 cards."),
    ELSPETH("Elspeth", "Start each duel with 1 free mulligan."),
    JACE("Jace", "Start each duel with +1 card in hand and +1 maximum hand size.");

    private final String displayName;
    private final String description;

    QuestPlaneswalker(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
