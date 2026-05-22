package forge.screens.quest;

import java.util.List;

import forge.Forge;
import forge.assets.FSkinFont;
import forge.gamemodes.quest.QuestRelicType;
import forge.model.FModel;
import forge.screens.FScreen;
import forge.toolbox.FComboBox;
import forge.toolbox.FDisplayObject;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FScrollPane;
import forge.util.Utils;

public class QuestRelicsScreen extends FScreen {
    private static final float PADDING = FOptionPane.PADDING;

    private final FScrollPane scroller = add(new FScrollPane() {
        @Override
        protected ScrollBounds layoutAndGetScrollBounds(float visibleWidth, float visibleHeight) {
            float x = PADDING;
            float y = PADDING;
            float w = visibleWidth - 2 * PADDING;
            for (FDisplayObject obj : getChildren()) {
                if (obj.isVisible()) {
                    float h;
                    if (obj instanceof FLabel) {
                        h = ((FLabel) obj).getAutoSizeBounds().height;
                    } else {
                        h = obj.getHeight() > 0 ? obj.getHeight() : Utils.AVG_FINGER_HEIGHT;
                    }
                    obj.setBounds(x, y, w, h);
                    y += obj.getHeight() + PADDING;
                }
            }
            return new ScrollBounds(visibleWidth, y);
        }
    });

    public QuestRelicsScreen() {
        super(Forge.getLocalizer().getMessage("lblRelics"), QuestMenu.getMenu());
    }

    @Override
    public void onActivate() {
        update();
    }

    public void update() {
        scroller.clear();

        final List<QuestRelicType> relics = FModel.getQuest().getAssets().getRelics();
        if (relics.isEmpty()) {
            scroller.add(new FLabel.Builder()
                    .text(Forge.getLocalizer().getMessage("lblNoRelics"))
                    .font(FSkinFont.get(14))
                    .build());
        } else {
            for (final QuestRelicType relic : relics) {
                scroller.add(new FLabel.Builder()
                        .text(relic.getDisplayName())
                        .font(FSkinFont.get(18))
                        .build());
                scroller.add(new FLabel.Builder()
                        .text(relic.getRarity())
                        .font(FSkinFont.get(12))
                        .build());
                scroller.add(new FLabel.Builder()
                        .text(relic.getDescription())
                        .font(FSkinFont.get(13))
                        .build());

                if (relic == QuestRelicType.MOOD_RING) {
                    scroller.add(new FLabel.Builder()
                            .text("Active Color:")
                            .font(FSkinFont.get(13))
                            .build());
                    final FComboBox<String> cbxColor = scroller.add(new FComboBox<>());
                    for (final String c : new String[]{"Off", "White", "Blue", "Black", "Red", "Green"}) {
                        cbxColor.addItem(c);
                    }
                    final String saved = FModel.getQuest().getAssets().getRelicData(QuestRelicType.MOOD_RING);
                    cbxColor.setSelectedItem(saved != null && !saved.isEmpty() ? saved : "Off");
                    cbxColor.setDropDownChangeHandler(e -> {
                        final String sel = cbxColor.getSelectedItem();
                        FModel.getQuest().getAssets().setRelicData(QuestRelicType.MOOD_RING, "Off".equals(sel) ? "" : sel);
                        FModel.getQuest().save();
                    });
                }
            }
        }

        scroller.revalidate();
        setHeaderCaption(FModel.getQuest().getName() + " - " + Forge.getLocalizer().getMessage("lblRelics"));
    }

    @Override
    protected void doLayout(float startY, float width, float height) {
        scroller.setBounds(0, startY, width, height - startY);
    }
}
