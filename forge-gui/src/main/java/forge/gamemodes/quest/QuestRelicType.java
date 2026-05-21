package forge.gamemodes.quest;

/**
 * Defines all relics that can be held by the player during a quest run.
 * Relics are unique objects that persistently affect gameplay mechanics.
 */
public enum QuestRelicType {

    ALCHEMY_KIT("Alchemy Kit",
            "After each win, you may offer up to 2 rare/mythic cards from your collection "
            + "in exchange for that many random rare/mythic cards.");

    private final String displayName;
    private final String description;

    QuestRelicType(final String displayName, final String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    @Override
    public String toString() { return displayName; }
}
