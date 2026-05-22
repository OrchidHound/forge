package forge.screens.home.quest;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

import javax.swing.JSeparator;
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
import forge.toolbox.FComboBoxWrapper;
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
            for (int i = 0; i < relics.size(); i++) {
                final QuestRelicType relic = relics.get(i);

                if (i > 0) {
                    pnlRelics.add(new JSeparator(SwingConstants.HORIZONTAL), "w 100%!, h 1px!, gap 0 0 10px 10px");
                }

                final FLabel nameLabel;
                if ("Mythic".equals(relic.getRarity())) {
                    nameLabel = new FLabel.Builder()
                            .text(buildRainbowHtml(relic.getDisplayName()))
                            .fontSize(16)
                            .fontAlign(SwingConstants.CENTER)
                            .build();
                } else {
                    nameLabel = new FLabel.Builder()
                            .text(relic.getDisplayName())
                            .fontStyle(Font.BOLD)
                            .fontSize(16)
                            .fontAlign(SwingConstants.CENTER)
                            .build();
                    nameLabel.setForeground(getRelicNameColor(relic));
                }
                pnlRelics.add(nameLabel, "w 100%!, gap 0 0 8px 4px");

                pnlRelics.add(new FLabel.Builder()
                        .text(relic.getDescription())
                        .fontSize(13)
                        .fontAlign(SwingConstants.LEFT)
                        .build(), "w 100%!, gap 10px 0 0 8px");

                if (relic == QuestRelicType.MOOD_RING) {
                    pnlRelics.add(new FLabel.Builder()
                            .text("Active Color:")
                            .fontSize(13)
                            .fontAlign(SwingConstants.LEFT)
                            .build(), "gap 10px 0 8px 2px");
                    final FComboBoxWrapper<String> cbxColor = new FComboBoxWrapper<>();
                    for (final String c : new String[]{"Off", "White", "Blue", "Black", "Red", "Green"}) {
                        cbxColor.addItem(c);
                    }
                    final String saved = FModel.getQuest().getAssets().getRelicData(QuestRelicType.MOOD_RING);
                    cbxColor.setSelectedItem(saved != null && !saved.isEmpty() ? saved : "Off");
                    cbxColor.addActionListener(e -> {
                        final String sel = (String) cbxColor.getSelectedItem();
                        FModel.getQuest().getAssets().setRelicData(QuestRelicType.MOOD_RING, "Off".equals(sel) ? "" : sel);
                        FModel.getQuest().save();
                    });
                    cbxColor.addTo(pnlRelics, "gap 10px 0 0 8px, h 26px!");
                }
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

    private static Color getRelicNameColor(final QuestRelicType relic) {
        switch (relic.getRarity()) {
            case "Common":   return Color.WHITE;
            case "Uncommon": return new Color(100, 160, 255);
            case "Rare":     return new Color(255, 215, 0);
            default:         return Color.WHITE;
        }
    }

    private static String buildRainbowHtml(final String name) {
        final String[] colors = {"#FF0000", "#FF7F00", "#FFFF00", "#00CC00", "#4488FF", "#9900CC"};
        final StringBuilder html = new StringBuilder("<html><center><b>");
        int ci = 0;
        for (final char c : name.toCharArray()) {
            if (c == ' ') {
                html.append("&nbsp;");
            } else {
                html.append("<font color='").append(colors[ci % colors.length]).append("'>").append(c).append("</font>");
                ci++;
            }
        }
        return html.append("</b></center></html>").toString();
    }
}
