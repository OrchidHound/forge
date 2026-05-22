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

    public QuestBossEvent(boolean finalBoss, int bossNumber) {
        super();
        this.finalBoss = finalBoss;
        this.bossNumber = bossNumber;
        setDifficulty(QuestEventDifficulty.EXPERT);
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

        QuestBossEvent bossEvent = new QuestBossEvent(isFinalBoss, bossNumber);

        // Build pool from the dedicated bosses folder
        final List<QuestEventDuel> bossPool = new ArrayList<>();
        final File bossDir = new File(ForgeConstants.DEFAULT_BOSS_DUELS_DIR);
        if (bossDir.exists() && bossDir.isDirectory()) {
            final QuestDuelReader reader = new QuestDuelReader(bossDir);
            for (QuestEventDuel d : reader.readAll().values()) {
                if (d.getEventDeck() == null || d.getEventDeck().getMain().isEmpty()) {
                    continue;
                }
                if (isFinalBoss && !d.isFinalBossDeck()) {
                    continue;
                }
                if (!isFinalBoss && d.isFinalBossDeck()) {
                    continue;
                }
                bossPool.add(d);
            }
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
        }

        return Collections.singletonList(bossEvent);
    }
}
