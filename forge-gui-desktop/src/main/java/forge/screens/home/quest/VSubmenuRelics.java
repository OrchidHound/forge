package forge.screens.home.quest;

import java.awt.Font;
import java.util.List;

import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import forge.gamemodes.quest.QuestRelicType;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.model.FModel;
import forge.screens.home.EMenuGroup;
import forge.screens.home.IVSubmenu;
import forge.screens.home.LblHeader;
import forge.screens.home.VHomeUI;
import forge.toolbox.FLabel;
import forge.toolbox.FScrollPanel;
import forge.util.Localizer;
import net.miginfocom.swing.MigLayout;

public enum VSubmenuRelics implements IVSubmenu<CSubmenuRelics> {
    SINGLETON_INSTANCE;

    final Localizer localizer = Localizer.getInstance();

    private DragCell parentCell;
    private final DragTab tab = new DragTab(localizer.getMessage("lblRelics"));
    private final LblHeader lblTitle = new LblHeader(localizer.getMessage("lblRelics"));

    private final FScrollPanel pnlRelics = new FScrollPanel(
            new MigLayout("insets 10, gap 0, wrap"),
            true,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    @Override
    public EMenuGroup getGroupEnum() {
        return EMenuGroup.QUEST;
    }

    @Override
    public String getMenuTitle() {
        return localizer.getMessage("lblRelics");
    }

    @Override
    public EDocID getItemEnum() {
        return EDocID.HOME_QUESTRELICS;
    }

    @Override
    public void populate() {
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().removeAll();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().setLayout(new MigLayout("insets 0, gap 0, wrap"));
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblTitle, "w 98%!, h 40px!, gap 1% 0 15px 15px");
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(pnlRelics, "w 98%!, pushy, growy, gap 1% 0 0 20px");
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().repaintSelf();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().revalidate();
    }

    public void update() {
        pnlRelics.removeAll();

        final List<QuestRelicType> relics = FModel.getQuest().getAssets().getRelics();
        if (relics.isEmpty()) {
            pnlRelics.add(new FLabel.Builder()
                    .text(localizer.getMessage("lblNoRelics"))
                    .fontStyle(Font.ITALIC)
                    .fontSize(14)
                    .fontAlign(SwingConstants.LEFT)
                    .build(), "w 100%!, gap 0 0 5px 5px");
        } else {
            for (final QuestRelicType relic : relics) {
                pnlRelics.add(new FLabel.Builder()
                        .text(relic.getDisplayName())
                        .fontStyle(Font.BOLD)
                        .fontSize(16)
                        .fontAlign(SwingConstants.LEFT)
                        .build(), "w 100%!, gap 0 0 10px 2px");
                pnlRelics.add(new FLabel.Builder()
                        .text(relic.getDescription())
                        .fontSize(13)
                        .fontAlign(SwingConstants.LEFT)
                        .build(), "w 100%!, gap 10px 0 0 8px");
            }
        }

        pnlRelics.revalidate();
        pnlRelics.repaint();
    }

    @Override
    public EDocID getDocumentID() {
        return EDocID.HOME_QUESTRELICS;
    }

    @Override
    public DragTab getTabLabel() {
        return tab;
    }

    @Override
    public CSubmenuRelics getLayoutControl() {
        return CSubmenuRelics.SINGLETON_INSTANCE;
    }

    @Override
    public void setParentCell(final DragCell cell0) {
        this.parentCell = cell0;
    }

    @Override
    public DragCell getParentCell() {
        return parentCell;
    }
}
