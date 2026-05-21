package forge.screens.home.quest;

import forge.gui.framework.ICDoc;

public enum CSubmenuRelics implements ICDoc {
    SINGLETON_INSTANCE;

    @Override
    public void register() {
    }

    @Override
    public void initialize() {
    }

    @Override
    public void update() {
        VSubmenuRelics.SINGLETON_INSTANCE.update();
    }
}
