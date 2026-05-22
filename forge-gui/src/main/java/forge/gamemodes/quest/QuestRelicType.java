package forge.gamemodes.quest;

/**
 * Defines all relics that can be held by the player during a quest run.
 * Relics are unique objects that persistently affect gameplay mechanics.
 */
public enum QuestRelicType {

    ALCHEMY_KIT("Alchemy Kit",
            "After each win, you may offer up to 2 rare/mythic cards from your collection "
            + "in exchange for that many random rare/mythic cards.",
            "Rare", false),

    MOOD_RING("Mood Ring",
            "Tints your fortune with a chosen hue. Select a color to restrict all booster pack "
            + "rewards to cards of that color only. Toggle off at any time in the Relics menu.",
            "Rare", false),

    RARE_GOODIES("Rare Goodies",
            "After each won duel, you receive one additional rare or mythic rare card. "
            + "Stacks with multiple copies.",
            "Common", true),

    PREMIUM_PACKS("Premium Packs",
            "Each booster pack reward contains one additional rare or mythic rare card. "
            + "Stacks with multiple copies.",
            "Common", true),

    VICTORS_LAUREL("Victor's Laurel",
            "Increases the bonus credits earned for winning by a specific turn by 50%. "
            + "Stacks with multiple copies.",
            "Uncommon", true),

    HUGE_MITTS("Huge Mitts",
            "Grants a starting hand size bonus of 1. Stacks with multiple copies.",
            "Rare", true);

    private final String displayName;
    private final String description;
    private final String rarity;
    private final boolean allowMultiple;

    QuestRelicType(final String displayName, final String description, final String rarity, final boolean allowMultiple) {
        this.displayName = displayName;
        this.description = description;
        this.rarity = rarity;
        this.allowMultiple = allowMultiple;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getRarity() { return rarity; }
    public boolean allowMultiple() { return allowMultiple; }

    @Override
    public String toString() { return displayName; }
}
