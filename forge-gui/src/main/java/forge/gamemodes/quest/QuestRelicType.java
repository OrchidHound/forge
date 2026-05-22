package forge.gamemodes.quest;

/**
 * Defines all relics that can be held by the player during a quest run.
 * Relics are unique objects that persistently affect gameplay mechanics.
 */
public enum QuestRelicType {

    ALCHEMY_KIT("Alchemy Kit",
            "After each win, you may offer up to 2 rare/mythic cards from your collection "
            + "in exchange for that many random rare/mythic cards.",
            "Rare", 1),

    MOOD_RING("Mood Ring",
            "Tints your fortune with a chosen hue. Select a color to restrict all booster pack "
            + "rewards to cards of that color only. Toggle off at any time in the Relics menu.",
            "Rare", 1),

    RARE_GOODIES("Rare Goodies",
            "After each won duel, you receive one additional rare or mythic rare card. "
            + "Stacks with multiple copies.",
            "Common", 20),

    PREMIUM_PACKS("Premium Packs",
            "Each booster pack reward contains one additional rare or mythic rare card. "
            + "Stacks with multiple copies.",
            "Common", 20),

    VICTORS_LAUREL("Victor's Laurel",
            "Increases the bonus credits earned for winning by a specific turn by 50%. "
            + "Stacks with multiple copies.",
            "Uncommon", 20),

    HUGE_MITTS("Huge Mitts",
            "Grants a starting hand size bonus of 1. Stacks with multiple copies.",
            "Rare", 20),

    COLLECTORS_LENS("Collector's Lens",
            "The spell shop stocks 2 additional rare or mythic rare cards. "
            + "Stacks with multiple copies.",
            "Uncommon", 20),

    MERCHANTS_FAVOR("Merchant's Favor",
            "The spell shop stocks 6 additional uncommon cards. "
            + "Stacks with multiple copies.",
            "Common", 20),

    BARGAIN_BARREL("Bargain Barrel",
            "The spell shop stocks 12 additional common cards. "
            + "Stacks with multiple copies.",
            "Common", 20),

    DRAFT_PERMIT("Draft Permit",
            "Reduces the cost of re-rolling a tournament from 600 to 250 credits.",
            "Uncommon", 1),

    WANDERING_INVITATION("Wandering Invitation",
            "After each won duel, each copy grants a 5% chance of one additional random tournament "
            + "appearing. Stacks with multiple copies.",
            "Uncommon", 20),

    DRAFTERS_EYE("Drafter's Eye",
            "When you go undefeated in a tournament, you may pick one additional card from the "
            + "drafted set per copy owned. Stacks with multiple copies.",
            "Rare", 20),

    HAGGLERS_COIN("Haggler's Coin",
            "Reduces spell shop card prices by 5% per copy owned. Stacks with multiple copies.",
            "Common", 20),

    ECHOING_SEAL("Echoing Seal",
            "Whenever you acquire a relic other than this one that allows duplicates, you receive "
            + "one additional copy of it per copy of this relic owned. Stacks with multiple copies.",
            "Mythic", 20),

    COLLECTORS_CODEX("Collector's Codex",
            "Upon acquiring this relic, choose any card from the quest pool to add to your collection. "
            + "Stacks with multiple copies.",
            "Rare", 20),

    VICTORY_PURSE("Victory Purse",
            "After each won duel, you receive 50 bonus credits. Stacks with multiple copies.",
            "Common", 20),

    CRUSHING_BLOW("Crushing Blow",
            "After winning a duel by reducing your opponent to -20 or less life, "
            + "you receive 3 bonus rare/mythic cards per copy owned. Stacks with multiple copies.",
            "Common", 20);

    private final String displayName;
    private final String description;
    private final String rarity;
    private final int maxCopies;

    QuestRelicType(final String displayName, final String description, final String rarity, final int maxCopies) {
        this.displayName = displayName;
        this.description = description;
        this.rarity = rarity;
        this.maxCopies = maxCopies;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getRarity() { return rarity; }
    public int getMaxCopies() { return maxCopies; }

    @Override
    public String toString() { return displayName; }
}
