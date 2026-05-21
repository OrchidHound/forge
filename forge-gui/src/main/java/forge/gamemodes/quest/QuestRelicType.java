package forge.gamemodes.quest;

/**
 * Defines all relics that can be held by the player during a quest run.
 * Relics are unique objects that persistently affect gameplay mechanics.
 */
public enum QuestRelicType {

    ALCHEMY_KIT("Alchemy Kit",
            "After each win, you may offer up to 2 rare/mythic cards from your collection "
            + "in exchange for that many random rare/mythic cards."),

    MOOD_RING("Mood Ring",
            "Tints your fortune with a chosen hue. Select a color to restrict all booster pack "
            + "rewards to cards of that color only. Toggle off at any time in the Relics menu.");

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
