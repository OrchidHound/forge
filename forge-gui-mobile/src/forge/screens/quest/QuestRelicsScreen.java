package forge.screens.quest;

import java.util.List;

import forge.Forge;
import forge.assets.FSkinFont;
import forge.gamemodes.quest.QuestRelicType;
import forge.model.FModel;
import forge.screens.FScreen;
import forge.toolbox.FDisplayObject;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FScrollPane;

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
                    float h = ((FLabel) obj).getAutoSizeBounds().height;
                    obj.setBounds(x, y, w, h);
                    y += h + PADDING;
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
                        .text(relic.getDescription())
                        .font(FSkinFont.get(13))
                        .build());
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
