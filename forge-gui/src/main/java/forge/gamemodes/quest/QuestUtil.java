/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.gamemodes.quest;

import com.google.common.collect.ImmutableMap;
import forge.LobbyPlayer;
import forge.card.CardEdition;
import forge.card.CardRules;
import forge.deck.Deck;
import forge.game.GameRules;
import forge.game.GameType;
import forge.game.player.RegisteredPlayer;
import forge.gamemodes.match.HostedMatch;
import forge.gamemodes.quest.bazaar.IQuestBazaarItem;
import forge.gamemodes.quest.bazaar.QuestItemType;
import forge.gamemodes.quest.bazaar.QuestPetController;
import forge.gamemodes.quest.data.DeckConstructionRules;
import forge.gamemodes.quest.data.QuestAchievements;
import forge.gamemodes.quest.data.QuestAssets;
import forge.gamemodes.quest.data.QuestPreferences;
import forge.gui.FThreads;
import forge.gui.GuiBase;
import forge.gui.interfaces.IButton;
import forge.gui.interfaces.IGuiGame;
import forge.gui.util.SGuiChoose;
import forge.gui.util.SOptionPane;
import forge.deck.io.DeckSerializer;
import forge.item.IPaperCard;
import forge.item.PaperCard;
import forge.item.PaperCardPredicates;
import forge.util.ItemPool;
import forge.util.MyRandom;
import forge.item.PaperToken;
import forge.localinstance.properties.ForgeConstants;
import forge.localinstance.properties.ForgePreferences.FPref;
import forge.localinstance.skin.FSkinProp;
import forge.model.FModel;
import forge.player.GamePlayerUtil;
import forge.util.Localizer;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * <p>
 * QuestUtil class.
 * </p>
 * MODEL - Static utility methods to help with minor tasks around Quest.
 *
 * @author Forge
 * @version $Id$
 */
public class QuestUtil {
    private static final DecimalFormat CREDITS_FORMATTER = new DecimalFormat("#,###");
    public static String formatCredits(long credits) {
        return CREDITS_FORMATTER.format(credits);
    }

    /**
     * <p>
     * getComputerStartingCards.
     * </p>
     * Returns new card instances of extra AI cards in play at start of event.
     *
     * @param qe
     *            a {@link forge.gamemodes.quest.QuestEvent} object.
     * @return a {@link java.util.List} object.
     */
    public static List<IPaperCard> getComputerStartingCards(final QuestEvent qe) {
        final List<IPaperCard> list = new ArrayList<>();

        for (final String s : qe.getAiExtraCards()) {
            list.add(QuestUtil.readExtraCard(s));
        }

        return list;
    }

    /**
     * <p>
     * getHumanStartingCards.
     * </p>
     * Returns list of current plant/pet configuration only.
     * @param qc
     *            a {@link forge.gamemodes.quest.QuestController} object.
     * @return a {@link java.util.List} object.
     */
    public static List<IPaperCard> getHumanStartingCards(final QuestController qc) {
        final List<IPaperCard> list = new ArrayList<>();

        for (int iSlot = 0; iSlot < QuestController.MAX_PET_SLOTS; iSlot++) {
            final String petName = qc.getSelectedPet(iSlot);
            final QuestPetController pet = qc.getPetsStorage().getPet(petName);
            if (pet != null) {
                final IPaperCard c = pet.getPetCard(qc.getAssets());
                if (c != null) {
                    list.add(c);
                }
            }
        }

        return list;
    }

    /**
     * <p>
     * getHumanStartingCards.
     * </p>
     * Returns new card instances of extra human cards, including current
     * plant/pet configuration, and cards in play at start of quest.
     *
     * @param qc
     *            a {@link forge.gamemodes.quest.QuestController} object.
     * @param qe
     *            a {@link forge.gamemodes.quest.QuestEvent} object.
     * @return a {@link java.util.List} object.
     */
    public static List<IPaperCard> getHumanStartingCards(final QuestController qc, final QuestEvent qe) {
        final List<IPaperCard> list = QuestUtil.getHumanStartingCards(qc);
        for (final String s : qe.getHumanExtraCards()) {
            list.add(QuestUtil.readExtraCard(s));
        }
        return list;
    }

    /**
     * <p>
     * createToken.
     * </p>
     * Creates a card instance for token defined by property string.
     *
     * @param s
     *            Properties string of token
     *            (TOKEN;W;1;1;sheep;type;type;type...)
     * @return token Card
     */
    public static PaperToken createToken(final String s) {
        final String[] properties = s.split(";", 6);

        final List<String> script = new ArrayList<>();
        script.add("Name:" + properties[4]);
        script.add("Colors:" + properties[1]);
        script.add("PT:"+ properties[2] + "/" + properties[3]);
        script.add("Types:" + properties[5].replace(';', ' '));
        script.add("Oracle:"); // tokens don't have texts yet
        final String fileName = PaperToken.makeTokenFileName(properties[1], properties[2], properties[3], properties[4]);
        return new PaperToken(CardRules.fromScript(script), CardEdition.UNKNOWN, fileName, "", IPaperCard.NO_ARTIST_NAME);
    }

    /**
     * <p>
     * readExtraCard.
     * </p>
     * Creates single card for a string read from unique event properties.
     *
     * @param name
     *            the name
     * @return the card
     */
    public static IPaperCard readExtraCard(final String name) {
        // Token card creation
        IPaperCard tempcard;
        if (name.startsWith("TOKEN")) {
            tempcard = QuestUtil.createToken(name);
            return tempcard;
        }
        // Standard card creation
        return FModel.getMagicDb().getCommonCards().getCardFromEditions(name);
    }

    public static void travelWorld() {
        final Localizer localizer = Localizer.getInstance();
        if (!checkActiveQuest(localizer.getMessage("lblTravelBetweenWorlds"))) {
            return;
        }
        final List<QuestWorld> worlds = new ArrayList<>();
        final QuestController qCtrl = FModel.getQuest();

        for (final QuestWorld qw : FModel.getWorlds()) {
            if (qCtrl.getWorld() != qw) {
                worlds.add(qw);
            }
        }

        if (worlds.size() < 1) {
            SOptionPane.showErrorDialog("There are currently no worlds you can travel to\nin this version of Forge.", "No Worlds");
            return;
        }

        final String setPrompt = localizer.getMessage("lblWhereDoYouWishToTravel");
        final QuestWorld newWorld = SGuiChoose.oneOrNone(setPrompt, worlds);

        if (worlds.indexOf(newWorld) < 0) {
            return;
        }

        if (qCtrl.getWorld() != newWorld) {
            boolean needRemove = false;
            if (nextChallengeInWins() < 1 && !qCtrl.getAchievements().getCurrentChallenges().isEmpty()) {
                needRemove = true;

                if (!SOptionPane.showConfirmDialog(
                        localizer.getMessage("lblUncompleteChallengesDesc"),
                        localizer.getMessage("lblUncompleteChallengesWarning"))) {
                    return;
                }
            }

            if (needRemove) {
                // Remove current challenges.
                while (nextChallengeInWins() == 0) {
                    qCtrl.getAchievements().addChallengesPlayed();
                }

                qCtrl.getAchievements().getCurrentChallenges().clear();
            }

            qCtrl.setWorld(newWorld);
            qCtrl.resetDuelsManager();
            qCtrl.resetChallengesManager();
            // Note that the following can be (ab)used to re-randomize your opponents by travelling to a different
            // world and back. To prevent this, simply delete the following line that randomizes DuelsManager.
            // (OTOH, you can 'swap' opponents even more easily  by simply selecting a different quest data file and
            // then re-selecting your current quest data file.)
            qCtrl.getDuelsManager().randomizeOpponents();
            qCtrl.getCards().clearShopList();
            qCtrl.save();
        }
    }

    private static QuestEvent event;
    private static QuestEventDraft draftEvent;
    private static boolean halfLifeHandicapActive = false;

    public static boolean isHalfLifeHandicapActive() {
        return halfLifeHandicapActive;
    }

    public static void setHalfLifeHandicapActive(boolean active) {
        halfLifeHandicapActive = active;
    }

    /**
     * <p>
     * nextChallengeInWins.
     * </p>
     *
     * @return a int.
     */
    public static int nextChallengeInWins() {
        final QuestController qData = FModel.getQuest();
        final int challengesPlayed = qData.getAchievements().getChallengesPlayed();

        final int wins = qData.getAchievements().getDuelWins();
        final int turnsToUnlock = FModel.getQuest().getTurnsToUnlockChallenge();
        final int delta;

        // First challenge unlocks after minimum wins reached.
        if (wins < 2 * turnsToUnlock) {
            delta = 2 * turnsToUnlock - wins;
        }
        else {
            // More than enough wins
            if (wins / turnsToUnlock > challengesPlayed) {
                delta = 0;
            }
            // This part takes the "unlimited challenge" bug into account;
            // a player could have an inflated challengesPlayed value.
            // Added 09-2012, can be removed after a while.
            else if (wins < challengesPlayed * turnsToUnlock) {
                delta = (challengesPlayed * turnsToUnlock - wins) + turnsToUnlock;
            }
            // Data OK, but not enough wins yet (default).
            else {
                delta = turnsToUnlock - wins % turnsToUnlock;
            }
        }

        return (delta > 0) ? delta : 0;
    }

    private static void updatePlantAndPetForView(final IVQuestStats view, final QuestController qCtrl) {
        final Localizer localizer = Localizer.getInstance();
        for (int iSlot = 0; iSlot < QuestController.MAX_PET_SLOTS; iSlot++) {
            final List<QuestPetController> petList = qCtrl.getPetsStorage().getAvaliablePets(iSlot, qCtrl.getAssets());
            final String currentPetName = qCtrl.getSelectedPet(iSlot);

            if (iSlot == 0) { // Plant visiblity
                if (petList.isEmpty()) {
                    view.getCbPlant().setVisible(false);
                }
                else {
                    view.getCbPlant().setVisible(true);
                    view.getCbPlant().setSelected(currentPetName != null);
                }
            }
            if (iSlot == 1) {
                view.getCbxPet().removeAllItems();

                // Pet list visibility
                if (!petList.isEmpty()) {
                    view.getCbxPet().setVisible(true);
                    view.getCbxPet().addItem(localizer.getMessage("lblDontSummonAPet"));

                    for (final QuestPetController pet : petList) {
                        final String name = localizer.getMessage("lblSummon").replace("%n","\"" + pet.getName() + "\"");
                        view.getCbxPet().addItem(name);
                        if (pet.getName().equals(currentPetName)) {
                            view.getCbxPet().setSelectedItem(name);
                        }
                    }
                } else {
                    view.getCbxPet().setVisible(false);
                }
            }
        }

        view.getCbxMatchLength().removeAllItems();
        boolean activeCharms = false;
        StringBuilder matchLength = new StringBuilder();
        matchLength.append(localizer.getMessage("lblMatchBestof")).append(" ").append(qCtrl.getMatchLength());
        if (qCtrl.getAssets().hasItem(QuestItemType.CHARM_VIM)) {
            view.getCbxMatchLength().addItem(localizer.getMessage("lblMatchBestOf1"));
            activeCharms = true;
        }
        view.getCbxMatchLength().addItem(localizer.getMessage("lblMatchBestOf3"));
        if (qCtrl.getAssets().hasItem(QuestItemType.CHARM)) {
            view.getCbxMatchLength().addItem(localizer.getMessage("lblMatchBestOf5"));
            activeCharms = true;
        }
        view.getCbxMatchLength().setSelectedItem(matchLength.toString());
        view.getCbxMatchLength().setVisible(activeCharms);

        if (view.isChallengesView()) {
            view.getLblZep().setVisible(qCtrl.getAssets().hasItem(QuestItemType.ZEPPELIN));
            if (qCtrl.getAssets().getItemLevel(QuestItemType.ZEPPELIN) == 2) {
                view.getLblZep().setEnabled(false);
                view.getLblZep().setTextColor(128, 128, 128);
            }
            else {
                view.getLblZep().setEnabled(true);
                view.getLblZep().setImage(FSkinProp.CLR_TEXT);
            }
        }
        else {
            view.getLblZep().setVisible(false);
        }
    }

    /**
     * Updates all quest info in a view, using
     * retrieval methods dictated in IVQuestStats.<br>
     * - Stats<br>
     * - Pets<br>
     * - Current deck info<br>
     * - "Challenge In" info<br>
     *
     * @param view0 {@link forge.gamemodes.quest.IVQuestStats}
     */
    public static void updateQuestView(final IVQuestStats view0) {
        final QuestController qCtrl = FModel.getQuest();
        final QuestAchievements qA = qCtrl.getAchievements();
        final QuestAssets qS = qCtrl.getAssets();

        if (qA == null) { return; }

        // Fantasy UI display
        view0.getLblNextChallengeInWins().setVisible(true);
        view0.getBtnBazaar().setVisible(true);
        view0.getLblLife().setVisible(true);

        final Localizer localizer = Localizer.getInstance();

        // Stats panel
        view0.getLblCredits().setText(localizer.getMessage("lblCredits") + ": " + QuestUtil.formatCredits(qS.getCredits()));
        view0.getLblLife().setText(localizer.getMessage("lblLife") + ": " + qS.getLife(qCtrl.getMode()));
        view0.getLblWins().setText(localizer.getMessage("lblWins") + ": " + qA.getWin());
        view0.getLblLosses().setText(localizer.getMessage("lblLosses") + ": " + qA.getLost());
        view0.getLblWorld().setText(localizer.getMessage("lblWorld") +": "+ (qCtrl.getWorld() == null ? " (" + localizer.getMessage("lblNone") + ")" : qCtrl.getWorld()));

        // Show or hide the set unlocking button

        view0.getBtnUnlock().setVisible(qCtrl.getUnlocksTokens() > 0 && qCtrl.getWorldFormat() == null);

        // Challenge in wins
        final int num = nextChallengeInWins();
        final String str;
        if (num == 0) {
            str = localizer.getMessage("lblnextChallengeInWins0");
        }
        else if (num == 1) {
            str = localizer.getMessage("lblnextChallengeInWins1");
        }
        else {
            str =localizer.getMessage("lblnextChallengeInWins2").replace("%n","\"" + num + "\"");
        }

        view0.getLblNextChallengeInWins().setText(str);

        if (view0.allowHtml()) {
            view0.getLblWinStreak().setText(
                    "<html>" + localizer.getMessage("lblWinStreak") + ": " + qA.getWinStreakCurrent()
                    + "<br>&nbsp; (" + localizer.getMessage("lblBest") + ": " + qA.getWinStreakBest() + ")</html>");
        }
        else {
            view0.getLblWinStreak().setText(
                    localizer.getMessage("lblWinStreak") +": " + qA.getWinStreakCurrent()
                    + " (" + localizer.getMessage("lblBest") + ": " + qA.getWinStreakBest() + ")");
        }

        // Current deck message
        final IButton lblCurrentDeck = view0.getLblCurrentDeck();
        if (getCurrentDeck() == null) {
            lblCurrentDeck.setTextColor(204, 0, 0);
            lblCurrentDeck.setText(localizer.getMessage("lblBuildAndSelectaDeck"));
        }
        else {
            lblCurrentDeck.setImage(FSkinProp.CLR_TEXT);
            lblCurrentDeck.setText(localizer.getMessage("lblCurrentDeck").replace("%n","\"" + getCurrentDeck().getName() + "\"."));
        }

        // Start panel: pet, plant, zep.
        if (qCtrl.getMode() == QuestMode.Fantasy) {
            updatePlantAndPetForView(view0, qCtrl);
        }
        else {
            // Classic mode display changes
            view0.getCbxPet().setVisible(false);
            view0.getCbPlant().setVisible(false);
            view0.getCbxMatchLength().setVisible(false);
            view0.getLblZep().setVisible(false);
            view0.getLblNextChallengeInWins().setVisible(false);
            view0.getBtnBazaar().setVisible(false);
            view0.getLblLife().setVisible(false);
        }
    }

    /** @return {@link forge.deck.Deck} */
    public static Deck getCurrentDeck() {
        Deck d = null;

        if (FModel.getQuest().getAssets() != null) {
            d = FModel.getQuest().getMyDecks().get(
                    FModel.getQuest().getCurrentDeck());
        }

        return d;
    }

    /** Updates the current selected quest event, used when game is started.
     * @param event0 {@link forge.gamemodes.quest.QuestEvent}
     */
    public static void setEvent(final QuestEvent event0) {
        event = event0;
    }

    public static void setDraftEvent(final QuestEventDraft event0) {
        draftEvent = event0;
    }

    public static QuestEventDraft getDraftEvent() {
        return draftEvent;
    }

    public static boolean checkActiveQuest(final String location) {
        final Localizer localizer = Localizer.getInstance();

        final QuestController qc = FModel.getQuest();
        if (qc == null || qc.getAssets() == null) {
            final String msg = localizer.getMessage("PleaseCreateAQuestBefore").replace("%n",location);
            SOptionPane.showErrorDialog(msg, localizer.getMessage("lblNoQuest"));
            System.out.println(msg);
            return false;
        }
        return true;
    }

    /** */
    public static void showSpellShop() {
        final Localizer localizer = Localizer.getInstance();
        if (!checkActiveQuest(localizer.getMessage("lblVisitTheSpellShop"))) {
            return;
        }
        GuiBase.getInterface().showSpellShop();
    }

    public static void triggerSpecialShop() {
        QuestSpellShop.specialShopPool = FModel.getQuest().getCards().generateSpecialShopList();
        QuestSpellShop.specialShopActive = true;
    }

    /**
     * Offers the player a chance to sacrifice a random non-land card from their deck.
     * Handles the confirmation dialog and card removal only. Caller is responsible for
     * generating and displaying rewards via {@link #generateCardExchangeReward()}.
     *
     * @return the removed PaperCard, or null if the player declined or no eligible cards exist
     */
    public static PaperCard performCardExchangeEvent() {
        final Deck currentDeck = getCurrentDeck();
        if (currentDeck == null) { return null; }

        final List<PaperCard> deckCards = currentDeck.getMain().toFlatList();
        final List<PaperCard> nonLandCards = new ArrayList<>();
        for (final PaperCard card : deckCards) {
            if (!card.getRules().getType().isLand()) {
                nonLandCards.add(card);
            }
        }
        if (nonLandCards.isEmpty()) { return null; }

        final PaperCard toRemove = nonLandCards.get(MyRandom.getRandom().nextInt(nonLandCards.size()));

        final boolean accepted = SOptionPane.showConfirmDialog(
            "A mysterious force stirs!\n\nYou may sacrifice " + toRemove.getName()
                + " from your collection in exchange for powerful rewards.\n\nDo you wish to proceed?",
            "Card Exchange Offer");
        if (!accepted) { return null; }

        FModel.getQuest().getCards().removeCard(toRemove, 1);
        return toRemove;
    }

    /**
     * Generates the reward for the card exchange event: either 10 random rare/mythic cards
     * or 2 random cards from the MTGO Vintage Cube, decided at random. Adds them to the
     * quest card pool and returns them for display.
     */
    public static List<PaperCard> generateCardExchangeReward() {
        final List<PaperCard> reward;
        if (MyRandom.getRandom().nextFloat() < 0.5f) {
            reward = FModel.getQuest().getCards().addRandomCards(10, PaperCardPredicates.IS_RARE_OR_MYTHIC);
        } else {
            reward = loadVintageCubeCards(2);
            FModel.getQuest().getCards().addAllCards(reward);
        }
        return reward;
    }

    private static List<PaperCard> loadVintageCubeCards(final int count) {
        final File cubeFile = new File(ForgeConstants.DECK_CUBE_DIR + "MTGO Vintage Cube 2025-12.dck");
        if (!cubeFile.exists()) { return new ArrayList<>(); }

        final Deck cubeDeck = DeckSerializer.fromFile(cubeFile);
        if (cubeDeck == null) { return new ArrayList<>(); }

        final List<PaperCard> allCards = cubeDeck.getMain().toFlatList();
        Collections.shuffle(allCards, MyRandom.getRandom());
        return new ArrayList<>(allCards.subList(0, Math.min(count, allCards.size())));
    }

    /**
     * Test helper: shows the half-life gambit dialog using the player's current quest life,
     * and if accepted awards the duplicate card immediately. Returns the awarded card or null.
     */
    public static PaperCard triggerHalfLifeGambitTest() {
        final QuestController qData = FModel.getQuest();
        final int regularLife = qData.getAssets().getLife(qData.getMode());
        final int halfLife = regularLife / 2;
        final boolean accepted = SOptionPane.showConfirmDialog(
            "A challenger's gambit!\n\nWould you like to start this duel with " + halfLife
                + " life instead of " + regularLife + "?\n\n"
                + "Win despite the handicap and you'll receive a duplicate of a card of your choice.",
            "Half-Life Gambit");
        if (!accepted) { return null; }
        final List<PaperCard> collection = qData.getCards().getCardpool().toFlatList();
        if (collection.isEmpty()) { return null; }
        collection.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        final PaperCard card = GuiBase.getInterface().chooseCard(
                "Choose a Card to Duplicate",
                "Select a card from your collection to receive a duplicate copy.",
                collection);
        if (card == null) { return null; }
        qData.getCards().addSingleCard(card, 1);
        return card;
    }

    /** */
    public static void showBazaar() {
        final Localizer localizer = Localizer.getInstance();
        if (!checkActiveQuest(localizer.getMessage("lblVisitTheBazaar"))) {
            return;
        }
        GuiBase.getInterface().showBazaar();
    }

    /** */
    public static void chooseAndUnlockEdition() {
        final Localizer localizer = Localizer.getInstance();
        if (!checkActiveQuest(localizer.getMessage("lblUnlockEditions"))) {
            return;
        }
        final QuestController qData = FModel.getQuest();
        final ImmutablePair<CardEdition, Integer> toUnlock = QuestUtilUnlockSets.chooseSetToUnlock(qData, false, null);
        if (toUnlock == null) {
            return;
        }

        final CardEdition unlocked = toUnlock.left;
        qData.getAssets().subtractCredits(toUnlock.right);
        SOptionPane.showMessageDialog(localizer.getMessage("lblUnlocked").replace("%n",unlocked.getName()),
                localizer.getMessage("titleUnlocked").replace("%n",unlocked.getName()), null);

        QuestUtilUnlockSets.doUnlock(qData, unlocked);
    }

    public static void startGame() {
        if (canStartGame()) {
            finishStartingGame();
        }
    }

    public static void finishStartingGame() {
        QuestSpellShop.clearSpecialShop();
        final QuestController qData = FModel.getQuest();

        FThreads.invokeInBackgroundThread(() -> {
            qData.getDuelsManager().randomizeOpponents();
            qData.setCurrentEvent(event);
            qData.save();

            int extraLifeHuman = 0;
            Integer lifeHuman = null;
            boolean useBazaar = true;
            Boolean forceAnte = null;

            //Generate a life modifier based on this quest's variant as held in the Quest Controller's DeckConstructionRules
            int variantLifeModifier = 0;

            switch(FModel.getQuest().getDeckConstructionRules()){
                case Default: break;
                case Commander: variantLifeModifier = 20; break;
            }

            int lifeAI = 20 + variantLifeModifier;

            if (event instanceof QuestEventChallenge) {
                final QuestEventChallenge qc = ((QuestEventChallenge) event);
                lifeAI = qc.getAILife();
                lifeHuman = qc.getHumanLife();

                if (qData.getAssets().hasItem(QuestItemType.ZEPPELIN)) {
                    extraLifeHuman = 3;
                }

                useBazaar = qc.isUseBazaar();
                forceAnte = qc.isForceAnte();
            }

            final RegisteredPlayer humanStart = getRegisteredPlayerByVariant(getDeckForNewGame());

            final RegisteredPlayer aiStart = getRegisteredPlayerByVariant(event.getEventDeck());


            if (lifeHuman != null) {
                humanStart.setStartingLife(lifeHuman);
            } else {
                humanStart.setStartingLife(qData.getAssets().getLife(qData.getMode()) + extraLifeHuman);
            }

            // Huge Mitts relic: bonus starting hand size
            final int hugeMittsCount = qData.getAssets().getRelicCount(QuestRelicType.HUGE_MITTS);
            if (hugeMittsCount > 0) {
                humanStart.setStartingHand(humanStart.getStartingHand() + hugeMittsCount);
            }

            // Jace planeswalker: +1 starting hand and +1 max hand size
            if (qData.getPlaneswalker() == QuestPlaneswalker.JACE) {
                humanStart.setStartingHand(humanStart.getStartingHand() + 1);
            }

            // Half-life gambit offer
            halfLifeHandicapActive = false;
            float halfLifeGambitChance = FModel.getQuestPreferences().getPrefInt(QuestPreferences.QPref.HALF_LIFE_GAMBIT_CHANCE) / 1000f;
            if (qData.getAssets().hasItem(QuestItemType.LUCKY_MOX)) {
                halfLifeGambitChance *= 1.5f;
            }
            if (MyRandom.getRandom().nextFloat() < halfLifeGambitChance) {
                final int regularLife = humanStart.getStartingLife();
                final int halfLife = regularLife / 2;
                final boolean accepted = SOptionPane.showConfirmDialog(
                    "A challenger's gambit!\n\nWould you like to start this duel with " + halfLife
                        + " life instead of " + regularLife + "?\n\n"
                        + "Win despite the handicap and you'll receive a duplicate of a card of your choice.",
                    "Half-Life Gambit");
                if (accepted) {
                    humanStart.setStartingLife(halfLife);
                    halfLifeHandicapActive = true;
                }
            }

            if (useBazaar) {
                humanStart.addExtraCardsOnBattlefield(QuestUtil.getHumanStartingCards(qData, event));
                aiStart.setStartingLife(lifeAI);
                aiStart.addExtraCardsOnBattlefield(QuestUtil.getComputerStartingCards(event));
            }

            final List<RegisteredPlayer> starter = new ArrayList<>();
            starter.add(humanStart.setPlayer(GamePlayerUtil.getQuestPlayer()));

            final LobbyPlayer aiPlayer = GamePlayerUtil.createAiPlayer(event.getOpponentName() == null ? event.getTitle() : event.getOpponentName(), event.getProfile());
            starter.add(aiStart.setPlayer(aiPlayer));

            final boolean useRandomFoil = FModel.getPreferences().getPrefBoolean(FPref.UI_RANDOM_FOIL);
            for (final RegisteredPlayer rp : starter) {
                rp.setRandomFoil(useRandomFoil);
            }
            boolean useAnte = FModel.getPreferences().getPrefBoolean(FPref.UI_ANTE);
            final boolean matchAnteRarity = FModel.getPreferences().getPrefBoolean(FPref.UI_ANTE_MATCH_RARITY);
            if (forceAnte != null) {
                useAnte = forceAnte;
            }
            final GameRules rules = new GameRules(GameType.Quest);
            rules.setPlayForAnte(useAnte);
            rules.setMatchAnteRarity(matchAnteRarity);
            rules.setGamesPerMatch(qData.getMatchLength());
            rules.setOrderCombatants(FModel.getPreferences().getPrefBoolean(FPref.LEGACY_ORDER_COMBATANTS));
            rules.setUseGrayText(FModel.getPreferences().getPrefBoolean(FPref.UI_GRAY_INACTIVE_TEXT));

            final TreeSet<GameType> variant = new TreeSet<>();
            if(FModel.getQuest().getDeckConstructionRules() == DeckConstructionRules.Commander){
                variant.add(GameType.Commander);
            }

            final HostedMatch hostedMatch = GuiBase.getInterface().hostMatch();
            final IGuiGame gui = GuiBase.getInterface().getNewGuiGame();
            gui.setPlayerAvatar(aiPlayer, event);
            FThreads.invokeInEdtNowOrLater(() -> hostedMatch.startMatch(rules, variant, starter, ImmutableMap.of(humanStart, gui), null));
        });
    }

    /**
     * Uses the appropriate RegisteredPlayer command for generating a RegisteredPlayer based on this quest's variant as
     * held by the QuestController's DeckConstructionRules.
     * @param deck The deck to generate the RegisteredPlayer with
     * @return A newly made RegisteredPlayer specific to the quest's variant
     */
    private static RegisteredPlayer getRegisteredPlayerByVariant(Deck deck){
        switch (FModel.getQuest().getDeckConstructionRules()) {
            case Default:
                return new RegisteredPlayer(deck);
            case Commander:
                return RegisteredPlayer.forCommander(deck);
        }
        return null;
    }

    private static Deck getDeckForNewGame() {
        Deck deck = null;
        if (event instanceof QuestEventChallenge) {
            // Predefined HumanDeck
            deck = ((QuestEventChallenge) event).getHumanDeck();
        }
        if (deck == null) {
            // If no predefined Deck, use the Player's Deck
            deck = getCurrentDeck();
        }
        return deck;
    }

    /**
     * Checks to see if a game can be started and displays relevant dialogues.
     * @return True if a game can be started.
     */
    public static boolean canStartGame() {
        final Localizer localizer = Localizer.getInstance();
        if (!checkActiveQuest(localizer.getMessage("lblStartADuel")) || null == event) {
            return false;
        }

        final var achievements = FModel.getQuest().getAchievements();
        if (achievements != null && achievements.isQuestRunOver()) {
            SOptionPane.showErrorDialog("Your quest run has ended. You were defeated by a Boss.\nStart a new quest to play again.", "Quest Run Over");
            return false;
        }
        if (achievements != null && achievements.isBossEventPending() && !(event instanceof QuestBossEvent) && !(event instanceof QuestEventChallenge)) {
            SOptionPane.showErrorDialog("A Boss encounter is waiting! You must defeat the Boss before continuing your quest.", "Boss Encounter");
            return false;
        }

        final Deck deck = getDeckForNewGame();
        if (deck == null) {
            final String msg = localizer.getMessage("lblSelectAQuestDeck");
            SOptionPane.showErrorDialog(msg, localizer.getMessage("lblNoDeck"));
            System.out.println(msg);
            return false;
        }

        if (FModel.getPreferences().getPrefBoolean(FPref.ENFORCE_DECK_LEGALITY)) {
            final String errorMessage = getDeckConformanceProblemsBeforeGame(deck);
            if (null != errorMessage) {
                SOptionPane.showErrorDialog(localizer.getMessage("lblInvalidDeckDesc").replace("%n",errorMessage), "Invalid Deck");
                return false;
            }
        }

        return true;
    }

    public static String getDeckConformanceProblemsBeforeGame(Deck deck){
        // Challenges with fixed decks override conformance settings
        if(event instanceof QuestEventChallenge && ((QuestEventChallenge) event).getHumanDeck() != null)
            return null;

        //Check quest mode's generic deck construction rules: minimum cards in deck, sideboard etc
        String errorMessage = GameType.Quest.getDeckFormat().getDeckConformanceProblem(deck);
        if (errorMessage != null && errorMessage.startsWith("should have at least")) {
            final int grimCount = FModel.getQuest().getAssets().getRelicCount(QuestRelicType.COMPACT_GRIMOIRE);
            int effectiveMin = 40 - grimCount;
            if (FModel.getQuest().getPlaneswalker() == QuestPlaneswalker.CHANDRA) {
                effectiveMin = Math.min(effectiveMin, 30);
            }
            if (deck != null && deck.getMain().countAll() >= effectiveMin) {
                errorMessage = null;
            }
        }
        if (errorMessage != null) return errorMessage;

        //Check for all applicable deck construction rules per this quests's saved DeckConstructionRules enum
        switch(FModel.getQuest().getDeckConstructionRules()){
            case Commander:
                errorMessage = GameType.Commander.getDeckFormat().getDeckConformanceProblem(deck);
                break;
        }
        if(errorMessage != null) return errorMessage;

        //Check for this quest- and World's deck construction rules: allowed sets, banned/restricted cards etc
        if (FModel.getQuestPreferences().getPrefInt(QuestPreferences.QPref.WORLD_RULES_CONFORMANCE) == 1) {
            if(FModel.getQuest().getFormat() != null)
                errorMessage = FModel.getQuest().getFormat().getDeckConformanceProblem(deck);
        }

        return errorMessage;
    }

    /** Duplicate in DeckEditorQuestMenu and
     * probably elsewhere...can streamline at some point
     * (probably shouldn't be here).
     *
     * @param in &emsp; {@link java.lang.String}
     * @return {@link java.lang.String}
     */
    public static String cleanString(final String in) {
        final StringBuilder out = new StringBuilder();
        final char[] c = in.toCharArray();

        for (final char aC : c) {
            if (Character.isLetterOrDigit(aC) || (aC == '-') || (aC == '_') || (aC == ' ')) {
                out.append(aC);
            }
        }

        return out.toString();
    }

    /**
     * Prompts the player to optionally choose a card from their collection to receive a duplicate copy of.
     * Adds the chosen card to the collection. Does not save — caller is responsible for saving.
     *
     * @return the duplicated PaperCard, or null if the player cancelled
     */
    public static PaperCard performCardDuplication() {
        final ItemPool<PaperCard> pool = FModel.getQuest().getCards().getCardpool();
        if (pool.isEmpty()) { return null; }

        final List<PaperCard> cards = new ArrayList<>();
        for (final Map.Entry<PaperCard, Integer> e : pool) {
            cards.add(e.getKey());
        }
        cards.sort(Comparator.comparing(PaperCard::getName));

        final PaperCard chosen = SGuiChoose.oneOrNone("Choose a card from your collection to duplicate (press Cancel to skip):", cards);
        if (chosen != null) {
            FModel.getQuest().getCards().addSingleCard(chosen, 1);
        }
        return chosen;
    }

    /**
     * Performs the alchemy table event: player selects up to 8 rare/mythic cards to exchange
     * for an equal number of random rare/mythic cards. Returns the new cards, or empty list
     * if the player declined or had no eligible cards.
     */
    public static List<PaperCard> performAlchemyTableEvent() {
        final ItemPool<PaperCard> pool = FModel.getQuest().getCards().getCardpool();
        if (pool.isEmpty()) { return Collections.emptyList(); }

        final List<PaperCard> rareMythics = new ArrayList<>();
        for (final Map.Entry<PaperCard, Integer> e : pool) {
            if (PaperCardPredicates.IS_RARE_OR_MYTHIC.test(e.getKey())) {
                rareMythics.add(e.getKey());
            }
        }
        if (rareMythics.isEmpty()) { return Collections.emptyList(); }

        rareMythics.sort(Comparator.comparing(PaperCard::getName));

        final boolean accepted = SOptionPane.showConfirmDialog(
            "The Alchemy Table glows with transformative energy!\n\n"
                + "You may offer up to 8 rare or mythic cards from your collection "
                + "in exchange for an equal number of random rare/mythic cards.\n\n"
                + "Do you wish to approach the table?",
            "Alchemy Table");
        if (!accepted) { return Collections.emptyList(); }

        final List<PaperCard> offered = SGuiChoose.getChoices(
            "Select up to 8 rare/mythic cards to offer (0 to cancel):", 0, 8, rareMythics);
        if (offered == null || offered.isEmpty()) { return Collections.emptyList(); }

        for (final PaperCard card : offered) {
            FModel.getQuest().getCards().removeCard(card, 1);
        }
        final List<PaperCard> reward = FModel.getQuest().getCards()
                .addRandomCards(offered.size(), PaperCardPredicates.IS_RARE_OR_MYTHIC);
        return reward != null ? reward : Collections.emptyList();
    }

    /**
     * Central relic acquisition method. Adds the relic to the player's assets and
     * triggers any on-acquire effects (e.g. Collector's Codex card pick).
     */
    public static void acquireRelic(final QuestRelicType relic) {
        final QuestController quest = FModel.getQuest();
        final QuestAssets assets = quest.getAssets();

        final int countBefore = assets.getRelicCount(relic);
        assets.addRelic(relic);
        final int copiesAdded = assets.getRelicCount(relic) - countBefore;

        if (relic == QuestRelicType.COLLECTORS_CODEX) {
            final List<PaperCard> pool = quest.getCards().getQuestCardPoolList();
            pool.sort(Comparator.comparing(PaperCard::getName));
            for (int i = 0; i < copiesAdded; i++) {
                final PaperCard chosen = GuiBase.getInterface().chooseCard(
                        "Collector's Codex",
                        "Choose a card from the quest pool to add to your collection:",
                        pool);
                if (chosen != null) {
                    quest.getCards().addSingleCard(chosen, 1);
                }
            }
        }
    }

    /**
     * Offers the player a choice of three randomly selected relics weighted by rarity.
     * Relics already at max copies are excluded. No relic appears twice in the same offer.
     */
    public static void performRelicOffer() {
        final QuestController quest = FModel.getQuest();
        final QuestAssets assets = quest.getAssets();

        // Build candidate pool: relics the player can still acquire
        final List<QuestRelicType> candidates = new ArrayList<>();
        for (final QuestRelicType relic : QuestRelicType.values()) {
            if (assets.getRelicCount(relic) < relic.getMaxCopies()) {
                candidates.add(relic);
            }
        }
        if (candidates.isEmpty()) { return; }

        // Pick up to 3 distinct relics weighted by rarity
        final List<QuestRelicType> offered = new ArrayList<>();
        final List<QuestRelicType> pool = new ArrayList<>(candidates);
        for (int i = 0; i < 3 && !pool.isEmpty(); i++) {
            final QuestRelicType pick = rollRelicByRarity(pool);
            offered.add(pick);
            pool.remove(pick);
        }

        // Build display strings: "[Rarity] Name — Description"
        final List<String> displayStrings = new ArrayList<>();
        for (final QuestRelicType relic : offered) {
            displayStrings.add("[" + relic.getRarity() + "] " + relic.getDisplayName()
                    + " — " + relic.getDescription());
        }

        final String chosen = SGuiChoose.oneOrNone(
                "A relic has appeared! Choose one to add to your collection:", displayStrings);
        if (chosen != null) {
            acquireRelic(offered.get(displayStrings.indexOf(chosen)));
        }
    }

    private static QuestRelicType rollRelicByRarity(final List<QuestRelicType> pool) {
        final float roll = MyRandom.getRandom().nextFloat();
        final String rarity;
        if (roll < 0.65f)      rarity = "Common";
        else if (roll < 0.9f) rarity = "Uncommon";
        else if (roll < 0.99f) rarity = "Rare";
        else                   rarity = "Mythic";

        final List<QuestRelicType> rarityPool = new ArrayList<>();
        for (final QuestRelicType r : pool) {
            if (r.getRarity().equals(rarity)) {
                rarityPool.add(r);
            }
        }
        if (!rarityPool.isEmpty()) {
            return rarityPool.get(MyRandom.getRandom().nextInt(rarityPool.size()));
        }
        // Fallback: pick from any available rarity if none of the target rarity remain
        return pool.get(MyRandom.getRandom().nextInt(pool.size()));
    }

    /**
     * Test helper: directly triggers the alchemy table event.
     */
    public static List<PaperCard> triggerAlchemyTableTest() {
        return performAlchemyTableEvent();
    }

    /**
     * Performs the Alchemy Kit relic exchange: player selects up to 2 rare/mythic cards
     * to trade for an equal number of random rare/mythic cards. Returns the new cards,
     * or empty list if the player declined or had no eligible cards.
     */
    public static List<PaperCard> performAlchemyKitExchange() {
        final ItemPool<PaperCard> pool = FModel.getQuest().getCards().getCardpool();
        if (pool.isEmpty()) { return Collections.emptyList(); }

        final List<PaperCard> rareMythics = new ArrayList<>();
        for (final Map.Entry<PaperCard, Integer> e : pool) {
            if (PaperCardPredicates.IS_RARE_OR_MYTHIC.test(e.getKey())) {
                rareMythics.add(e.getKey());
            }
        }
        if (rareMythics.isEmpty()) { return Collections.emptyList(); }

        rareMythics.sort(Comparator.comparing(PaperCard::getName));

        final List<PaperCard> offered = SGuiChoose.getChoices(
            "Alchemy Kit: Select up to 2 rare/mythic cards to exchange (0 to skip):", 0, 2, rareMythics);
        if (offered == null || offered.isEmpty()) { return Collections.emptyList(); }

        for (final PaperCard card : offered) {
            FModel.getQuest().getCards().removeCard(card, 1);
        }
        final List<PaperCard> reward = FModel.getQuest().getCards()
                .addRandomCards(offered.size(), PaperCardPredicates.IS_RARE_OR_MYTHIC);
        return reward != null ? reward : Collections.emptyList();
    }

    public static void buyQuestItem(final IQuestBazaarItem item) {
        final QuestAssets qA = FModel.getQuest().getAssets();
        final int cost = item.getBuyingPrice(qA);
        if (cost >= 0 && (qA.getCredits() - cost) >= 0) {
            qA.subtractCredits(cost);
            qA.addCredits(item.getSellingPrice(qA));
            item.onPurchase(qA);
            FModel.getQuest().save();
        }
    }

} // QuestUtil
