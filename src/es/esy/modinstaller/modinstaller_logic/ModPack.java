package es.esy.modinstaller.modinstaller_logic;

import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by noah on 2/1/17.
 */
public class ModPack {
    public final String name;
    private final List<Mod> mods;
    public final TreeItem<String> node;

    public ModPack(String name) {
        this.name = name;
        this.node = new TreeItem<>(name);
        mods = new ArrayList<>();
    }

    public void toggleActivation() {
        int activationLevel = getActivationLevel();
        if (activationLevel == 2) this.setActivated(false);
        else this.setActivated(true);
    }

    public void addMod(Mod mod) {
        if (mod != null) {
            mod.modPack = this;
            mods.add(mod);
            mods.sort(Comparator.comparing(a -> a.name));
        }
    }

    public TreeItem<String> getNode(String searchString, Boolean showLibs) {
        node.getChildren().clear();
        final Boolean includeAll = name.toLowerCase().contains(searchString.toLowerCase());
        for (Mod mod : mods) {
            if ((includeAll || mod.matchesSearchString(searchString)) && (showLibs || !mod.isLib))
                node.getChildren().add(mod.node);
        }
        if (node.getChildren().size() > 0)
            return node;
        else
            return null;
    }

    public int getActivationLevel() {
        int activeMods = 0;
        for (Mod mod : mods) {
            if (mod.isActivated()) activeMods++;
        }
        if (activeMods == mods.size()) return 2;
        else if (activeMods == 0) return 0;
        else return 1;
    }

    private void setActivated(Boolean activated) {
        for (Mod mod : mods) {
            mod.setActivated(activated);
        }
    }
}
