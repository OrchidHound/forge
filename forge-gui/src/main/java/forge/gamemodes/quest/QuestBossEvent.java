package forge.gamemodes.quest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import forge.gamemodes.quest.data.QuestPreferences.DifficultyPrefs;
import forge.gamemodes.quest.io.QuestDuelReader;
import forge.localinstance.properties.ForgeConstants;
import forge.model.FModel;
import forge.util.MyRandom;

public class QuestBossEvent extends QuestEventDuel {

    private final boolean finalBoss;
    private final int bossNumber;

    public QuestBossEvent(boolean finalBoss, int bossNumber, QuestEventDifficulty targetDifficulty) {
        super();
        this.finalBoss = finalBoss;
        this.bossNumber = bossNumber;
        setDifficulty(targetDifficulty);
        setShowDifficulty(false);
        if (finalBoss) {
            setTitle("The Final Boss");
            setDescription("The ultimate challenge! Defeat the Final Boss to complete your quest run!\n"
                    + "Starting life: 20"
                    + "  |  Starting hand: 7");
        } else {
            setTitle("Boss " + bossNumber + " of 6");
            setDescription("A powerful Boss stands in your way! Defeat the Boss to continue your quest.\n"
                    + "Starting life: 20"
                    + "  |  Starting hand: 7");
        }
    }

    public boolean isFinalBoss() {
        return finalBoss;
    }

    public int getBossNumber() {
        return bossNumber;
    }

    public static List<QuestEventDuel> createBossEncounterList(QuestEventDuelManagerInterface manager) {
        final int regularEventsPlayed = FModel.getQuest().getAchievements().getRegularEventsPlayed();
        final int difficulty = FModel.getQuest().getAchievements().getDifficulty();
        final int frequency = FModel.getQuestPreferences().getPrefInt(DifficultyPrefs.BOSS_ENCOUNTER_FREQUENCY, difficulty);
        final int bossNumber = regularEventsPlayed / frequency;
        final boolean isFinalBoss = bossNumber >= 6;

        // Scale difficulty by boss number
        final QuestEventDifficulty targetDifficulty;
        if (isFinalBoss) {
            targetDifficulty = QuestEventDifficulty.EXPERT;
        } else if (bossNumber <= 2) {
            targetDifficulty = QuestEventDifficulty.MEDIUM;
        } else if (bossNumber <= 4) {
            targetDifficulty = QuestEventDifficulty.HARD;
        } else {
            targetDifficulty = QuestEventDifficulty.EXPERT;
        }

        QuestBossEvent bossEvent = new QuestBossEvent(isFinalBoss, bossNumber, targetDifficulty);

        final List<String> usedDecks = FModel.getQuest().getAchievements().getUsedBossDeckNames();

        // Build pool from the dedicated bosses folder
        final List<QuestEventDuel> fullPool = new ArrayList<>();
        final File bossDir = new File(ForgeConstants.DEFAULT_BOSS_DUELS_DIR);
        if (bossDir.exists() && bossDir.isDirectory()) {
            final QuestDuelReader reader = new QuestDuelReader(bossDir);
            for (QuestEventDuel d : reader.readAll().values()) {
                if (d.getEventDeck() == null || d.getEventDeck().getMain().isEmpty()) {
                    continue;
                }
                if (isFinalBoss != d.isFinalBossDeck()) {
                    continue;
                }
                if (!isFinalBoss && d.getDifficulty() != targetDifficulty) {
                    continue;
                }
                fullPool.add(d);
            }
        }

        // Prefer unused decks; fall back to full pool if all have been used
        List<QuestEventDuel> bossPool = new ArrayList<>();
        for (QuestEventDuel d : fullPool) {
            if (!usedDecks.contains(d.getEventDeck().getName())) {
                bossPool.add(d);
            }
        }
        if (bossPool.isEmpty()) {
            bossPool = fullPool;
        }

        QuestEventDuel bossSource = null;
        if (!bossPool.isEmpty()) {
            bossSource = bossPool.get(MyRandom.getRandom().nextInt(bossPool.size()));
        } else {
            // Fallback: pick from the strongest tier of regular duels
            for (QuestEventDifficulty diff : new QuestEventDifficulty[]{
                    QuestEventDifficulty.EXPERT, QuestEventDifficulty.HARD,
                    QuestEventDifficulty.MEDIUM, QuestEventDifficulty.EASY}) {
                List<QuestEventDuel> available = new ArrayList<>();
                for (QuestEventDuel d : manager.getDuels(diff)) {
                    if (d.getEventDeck() != null && !d.getEventDeck().getMain().isEmpty()) {
                        available.add(d);
                    }
                }
                if (!available.isEmpty()) {
                    bossSource = available.get(MyRandom.getRandom().nextInt(available.size()));
                    break;
                }
            }
            if (bossSource == null) {
                for (QuestEventDuel d : manager.getAllDuels()) {
                    if (d.getEventDeck() != null && !d.getEventDeck().getMain().isEmpty()) {
                        bossSource = d;
                        break;
                    }
                }
            }
        }

        if (bossSource != null) {
            bossEvent.setEventDeck(bossSource.getEventDeck());
            bossEvent.setProfile(bossSource.getProfile());
            bossEvent.setIconImageKey(bossSource.getIconImageKey());
            final String opponentName = bossSource.getOpponentName() != null
                    ? bossSource.getOpponentName() : bossSource.getTitle();
            bossEvent.setOpponentName(opponentName);
            FModel.getQuest().getAchievements().addUsedBossDeckName(bossSource.getEventDeck().getName());
            FModel.getQuest().save();
        }

        return Collections.singletonList(bossEvent);
    }
}
