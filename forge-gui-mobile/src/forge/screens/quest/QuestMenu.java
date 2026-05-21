package forge.screens.quest;

import java.io.File;
import java.io.IOException;

import forge.Forge;
import forge.assets.FSkinImage;
import forge.deck.FDeckEditor;
import forge.gamemodes.quest.IVQuestStats;
import forge.gamemodes.quest.QuestSpellShop;
import forge.gamemodes.quest.QuestUtil;
import forge.gamemodes.quest.data.QuestPreferences.QPref;
import forge.gamemodes.quest.io.QuestDataIO;
import forge.gui.FThreads;
import forge.gui.interfaces.IButton;
import forge.gui.interfaces.ICheckBox;
import forge.gui.interfaces.IComboBox;
import forge.localinstance.properties.ForgeConstants;
import forge.menu.FMenuItem;
import forge.menu.FPopupMenu;
import forge.model.FModel;
import forge.screens.FScreen;
import forge.screens.LoadingOverlay;
import forge.screens.home.HomeScreen;
import forge.screens.home.LoadGameMenu.LoadGameScreen;
import forge.screens.home.NewGameMenu.NewGameScreen;
import forge.util.ThreadUtil;

public class QuestMenu extends FPopupMenu implements IVQuestStats {

    private static final QuestMenu questMenu = new QuestMenu();
    private static final QuestBazaarScreen bazaarScreen = new QuestBazaarScreen();
    private static final QuestChallengesScreen challengesScreen = new QuestChallengesScreen();
    private static final QuestDecksScreen decksScreen = new QuestDecksScreen();
    private static final QuestDuelsScreen duelsScreen = new QuestDuelsScreen();
    private static final QuestPrefsScreen prefsScreen = new QuestPrefsScreen();
    private static final QuestSpellShopScreen spellShopScreen = new QuestSpellShopScreen();
    private static final QuestStatsScreen statsScreen = new QuestStatsScreen();
    private static final QuestTournamentsScreen tournamentsScreen = new QuestTournamentsScreen();

    private static final FMenuItem duelsItem = new FMenuItem(Forge.getLocalizer().getMessage("lblDuels"), FSkinImage.QUEST_BIG_SWORD, event -> setCurrentScreen(duelsScreen));
    private static final FMenuItem challengesItem = new FMenuItem(Forge.getLocalizer().getMessage("lblChallenges"), FSkinImage.QUEST_HEART, event -> setCurrentScreen(challengesScreen));
    private static final FMenuItem tournamentsItem = new FMenuItem(Forge.getLocalizer().getMessage("lblTournaments"), FSkinImage.QUEST_BIG_SHIELD, event -> setCurrentScreen(tournamentsScreen));
    private static final FMenuItem decksItem = new FMenuItem(Forge.getLocalizer().getMessage("lblQuestDecks"), FSkinImage.QUEST_BIG_BAG, event -> setCurrentScreen(decksScreen));
    private static final FMenuItem spellShopItem = new FMenuItem(Forge.getLocalizer().getMessage("lblSpellShop"), FSkinImage.QUEST_BOOK, event -> setCurrentScreen(spellShopScreen));
    private static final FMenuItem bazaarItem = new FMenuItem(Forge.getLocalizer().getMessage("lblBazaar"), FSkinImage.QUEST_BOTTLES, event -> setCurrentScreen(bazaarScreen));
    private static final FMenuItem statsItem = new FMenuItem(Forge.getLocalizer().getMessage("lblStatistics"), FSkinImage.MENU_STATS, event -> setCurrentScreen(statsScreen));
    private static final FMenuItem unlockSetsItem = new FMenuItem(Forge.getLocalizer().getMessage("btnUnlockSets"), FSkinImage.QUEST_MAP, event -> {
        //invoke in background thread so prompts can work
        ThreadUtil.invokeInGameThread(() -> {
            QuestUtil.chooseAndUnlockEdition();
            FThreads.invokeInEdtLater(QuestMenu::updateCurrentQuestScreen);
        });
    });
    private static final FMenuItem travelItem = new FMenuItem(Forge.getLocalizer().getMessage("btnTravel"), FSkinImage.QUEST_MAP, event -> {
        //invoke in background thread so prompts can work
        ThreadUtil.invokeInGameThread(() -> {
            QuestUtil.travelWorld();
            FThreads.invokeInEdtLater(QuestMenu::updateCurrentQuestScreen);
        });
    });
    private static final FMenuItem prefsItem = new FMenuItem(Forge.getLocalizer().getMessage("Preferences"), Forge.hdbuttons ? FSkinImage.HDPREFERENCE : FSkinImage.SETTINGS, event -> setCurrentScreen(prefsScreen));

    private static final FPopupMenu testMenu = new FPopupMenu() {
        @Override
        protected void buildMenu() {
            addItem(new FMenuItem("Trigger Boss", FSkinImage.QUEST_BIG_SWORD, event -> {
                if (FModel.getQuest().getAchievements() != null) {
                    FModel.getQuest().getAchievements().debugTriggerBoss();
                    FModel.getQuest().save();
                    FThreads.invokeInEdtLater(QuestMenu::updateCurrentQuestScreen);
                }
            }));
            addItem(new FMenuItem("Trigger Final Boss", FSkinImage.QUEST_BIG_SWORD, event -> {
                if (FModel.getQuest().getAchievements() != null) {
                    FModel.getQuest().getAchievements().debugTriggerFinalBoss();
                    FModel.getQuest().save();
                    FThreads.invokeInEdtLater(QuestMenu::updateCurrentQuestScreen);
                }
            }));
            addItem(new FMenuItem("Reset Quest Run", FSkinImage.QUEST_BIG_SWORD, event -> {
                if (FModel.getQuest().getAchievements() != null) {
                    FModel.getQuest().getAchievements().debugResetQuestRun();
                    FModel.getQuest().save();
                    FThreads.invokeInEdtLater(QuestMenu::updateCurrentQuestScreen);
                }
            }));
            addItem(new FMenuItem("Add 10000 Credits", FSkinImage.QUEST_GOLD, event -> {
                if (FModel.getQuest().getAssets() != null) {
                    FModel.getQuest().getAssets().addCredits(10000);
                    FModel.getQuest().save();
                    FThreads.invokeInEdtLater(QuestMenu::updateCurrentQuestScreen);
                }
            }));
            addItem(new FMenuItem("Trigger Card Duplication", FSkinImage.QUEST_COIN, event -> {
                ThreadUtil.invokeInGameThread(() -> {
                    QuestUtil.performCardDuplication();
                    FModel.getQuest().save();
                    FThreads.invokeInEdtLater(QuestMenu::updateCurrentQuestScreen);
                });
            }));
            addItem(new FMenuItem("Trigger Special Shop", FSkinImage.QUEST_BOOK, event -> {
                ThreadUtil.invokeInGameThread(() -> {
                    QuestUtil.triggerSpecialShop();
                    FThreads.invokeInEdtLater(QuestMenu::showSpellShop);
                });
            }));
            addItem(new FMenuItem("Trigger Card Exchange", FSkinImage.QUEST_COIN, event -> {
                ThreadUtil.invokeInGameThread(() -> {
                    forge.item.PaperCard removed = QuestUtil.performCardExchangeEvent();
                    if (removed != null) {
                        java.util.List<forge.item.PaperCard> reward = QuestUtil.generateCardExchangeReward();
                        FModel.getQuest().save();
                        if (!reward.isEmpty()) {
                            forge.gui.util.SGuiChoose.reveal("In return, you received:", reward);
                        }
                    }
                    FThreads.invokeInEdtLater(QuestMenu::updateCurrentQuestScreen);
                });
            }));
            addItem(new FMenuItem("Trigger Half-Life Gambit", FSkinImage.QUEST_LIFE, event -> {
                ThreadUtil.invokeInGameThread(() -> {
                    forge.item.PaperCard card = QuestUtil.triggerHalfLifeGambitTest();
                    if (card != null) {
                        FModel.getQuest().save();
                        forge.gui.util.SGuiChoose.reveal("Half-Life Gambit Victory! You've earned a duplicate:", java.util.Collections.singletonList(card));
                    }
                    FThreads.invokeInEdtLater(QuestMenu::updateCurrentQuestScreen);
                });
            }));
            addItem(new FMenuItem("Trigger Alchemy Table", FSkinImage.QUEST_BOTTLES, event -> {
                ThreadUtil.invokeInGameThread(() -> {
                    java.util.List<forge.item.PaperCard> reward = QuestUtil.triggerAlchemyTableTest();
                    if (!reward.isEmpty()) {
                        FModel.getQuest().save();
                        forge.gui.util.SGuiChoose.reveal("The Alchemy Table has transformed your cards! You received:", reward);
                    }
                    FThreads.invokeInEdtLater(QuestMenu::updateCurrentQuestScreen);
                });
            }));
            addItem(new FMenuItem("Reset Special Shop", FSkinImage.QUEST_BOOK, event -> {
                QuestSpellShop.clearSpecialShop();
                FThreads.invokeInEdtLater(QuestMenu::updateCurrentQuestScreen);
            }));
        }
    };
    private static final FMenuItem testMenuItem = new FMenuItem("[Test]", FSkinImage.QUEST_BIG_SWORD, event ->
            testMenu.show(event.getSource(), 0, event.getSource().getHeight()));

    static {
        statsScreen.addTournamentResultsLabels(tournamentsScreen);
    }

    private static void setCurrentScreen(FScreen screen0) {
        //make it so pressing Back from any screen besides Duels screen always goes to Duels screen
        //and make it so Duels screen always goes back to screen that launched Quest mode
        Forge.openScreen(screen0, Forge.getCurrentScreen() != duelsScreen);
    }

    private static void updateCurrentQuestScreen() {
        if (duelsItem.isSelected()) {
            duelsScreen.update();
        }
        else if (challengesItem.isSelected()) {
            challengesScreen.update();
        }
        else if (tournamentsItem.isSelected()) {
            tournamentsScreen.update();
        }
        else if (decksItem.isSelected()) {
            decksScreen.refreshDecks();
        }
        else if (spellShopItem.isSelected()) {
            spellShopScreen.update();
        }
        else if (statsItem.isSelected()) {
            statsScreen.update();
        }
    }

    static {
        //the first time quest mode is launched, add button for it if in Landscape mode
        if (Forge.isLandscapeMode()) {
            HomeScreen.instance.addButtonForMode("-"+Forge.getLocalizer().getMessage("lblQuestMode"), event -> launchQuestMode(LaunchReason.StartQuestMode, HomeScreen.instance.getQuestCommanderMode()));
        }
    }

    public static QuestMenu getMenu() {
        return questMenu;
    }

    private QuestMenu() {
    }

    public enum LaunchReason {
        StartQuestMode,
        LoadQuest,
        NewQuest
    }

    public static void launchQuestMode(final LaunchReason reason, boolean commanderMode) {
        Forge.lastButtonIndex = 6;
        HomeScreen.instance.updateQuestCommanderMode(commanderMode);
        decksScreen.commanderMode = commanderMode;
        //attempt to load current quest
        final File dirQuests = new File(ForgeConstants.QUEST_SAVE_DIR);
        final String questname = FModel.getQuestPreferences().getPref(QPref.CURRENT_QUEST);
        final File data = new File(dirQuests.getPath(), questname);
        if (data.exists()) {
            LoadingOverlay.show(Forge.getLocalizer().getMessage("lblLoadingCurrentQuest"), true, () -> {
                try {
                    FModel.getQuest().load(QuestDataIO.loadData(data));
                } catch (IOException e) {
                    System.err.printf("Failed to load quest '%s'%n", questname);
                    // Failed to load last quest, don't continue with quest loading stuff
                    return;
                }

                FDeckEditor.DECK_CONTROLLER_QUEST.setRootFolder(FModel.getQuest().getMyDecks());
                FDeckEditor.DECK_CONTROLLER_QUEST_DRAFT.setRootFolder(FModel.getQuest().getDraftDecks());
                if (reason == LaunchReason.StartQuestMode) {
                    if (QuestUtil.getCurrentDeck() == null) {
                        Forge.openScreen(decksScreen); //if quest doesn't have a deck specified, open decks screen by default
                    }
                    else {
                        Forge.openScreen(duelsScreen); //TODO: Consider opening most recent quest view
                    }
                }
                else {
                    duelsScreen.update();
                    challengesScreen.update();
                    tournamentsScreen.update();
                    decksScreen.refreshDecks();
                    Forge.openScreen(duelsScreen);
                    if (reason == LaunchReason.NewQuest) {
                        LoadGameScreen.QuestMode.setAsBackScreen(true);
                    }
                }
                HomeScreen.instance.updateQuestWorld(FModel.getQuest().getWorld() == null ? "" : FModel.getQuest().getWorld().toString());
            });
            return;
        }

        //if current quest can't be loaded, open New Quest or Load Quest screen based on whether a quest exists
        if (dirQuests.exists() && dirQuests.isDirectory() && dirQuests.list().length > 0) {
            LoadGameScreen.QuestMode.open();
        }
        else {
            NewGameScreen.QuestMode.open();
        }
    }

    @Override
    protected void buildMenu() {
        FScreen currentScreen = Forge.getCurrentScreen();
        addItem(duelsItem); duelsItem.setSelected(currentScreen == duelsScreen);
        addItem(challengesItem); challengesItem.setSelected(currentScreen == challengesScreen);
        addItem(tournamentsItem); tournamentsItem.setSelected(currentScreen == tournamentsScreen);
        addItem(decksItem); decksItem.setSelected(currentScreen == decksScreen);
        addItem(spellShopItem); spellShopItem.setSelected(currentScreen == spellShopScreen);
        addItem(bazaarItem); bazaarItem.setSelected(currentScreen == bazaarScreen);
        addItem(unlockSetsItem);
        if(!HomeScreen.instance.getQuestCommanderMode())
            addItem(travelItem);
        addItem(statsItem); statsItem.setSelected(currentScreen == statsScreen);
        addItem(prefsItem); prefsItem.setSelected(currentScreen == prefsScreen);
        addItem(testMenuItem);
    }

    @Override
    public IButton getBtnBazaar() {
        return bazaarItem;
    }

    @Override
    public IButton getBtnSpellShop() {
        return spellShopItem;
    }

    @Override
    public IButton getBtnUnlock() {
        return unlockSetsItem;
    }

    @Override
    public IButton getBtnTravel() {
        return travelItem;
    }

    @Override
    public IButton getLblCredits() {
        return statsScreen.getLblCredits();
    }

    @Override
    public IButton getLblLife() {
        return statsScreen.getLblLife();
    }

    @Override
    public IButton getLblWorld() {
        return statsScreen.getLblWorld();
    }

    @Override
    public IButton getLblWins() {
        return statsScreen.getLblWins();
    }

    @Override
    public IButton getLblLosses() {
        return statsScreen.getLblLosses();
    }

    @Override
    public IButton getLblNextChallengeInWins() {
        return Forge.getCurrentScreen() == challengesScreen ? challengesScreen.getLblNextChallengeInWins() : duelsScreen.getLblNextChallengeInWins();
    }

    @Override
    public IButton getLblCurrentDeck() {
        return Forge.getCurrentScreen() == challengesScreen ? challengesScreen.getLblCurrentDeck() : duelsScreen.getLblCurrentDeck();
    }

    @Override
    public IButton getLblWinStreak() {
        return statsScreen.getLblWinStreak();
    }

    @Override
    public IComboBox<String> getCbxPet() {
        return statsScreen.getCbxPet();
    }

    @Override
    public ICheckBox getCbPlant() {
        return statsScreen.getCbPlant();
    }

    @Override
    public IComboBox<String> getCbxMatchLength() {
        return statsScreen.getCbxMatchLength();
    }

    @Override
    public IButton getLblZep() {
        return statsScreen.getLblZep();
    }

    @Override
    public boolean isChallengesView() {
        return Forge.getCurrentScreen() == challengesScreen || Forge.getCurrentScreen() == statsScreen; //treat stats screen as challenges view so Zeppelin shows up
    }

    @Override
    public boolean allowHtml() {
        return false;
    }

    public static void showSpellShop() {
        Forge.openScreen(spellShopScreen);
    }

    public static void showBazaar() {
        Forge.openScreen(bazaarScreen);
    }
}
