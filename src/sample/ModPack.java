package sample;

import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by noah on 2/1/17.
 */
public class ModPack {
    String name;
    List<Mod> mods;
    TreeItem<String> node;

    ModPack(String name) {
        this.name = name;
        mods = new ArrayList<>();
        node = new TreeItem<>(name);
//        node.setGraphic();
    }

    void toggleActivation() {
        int activationLevel = getActivationLevel();
        if (activationLevel == 2) this.setActivated(false);
        else this.setActivated(true);
    }

    void addMod(Mod mod) {
        if (mod != null) {
            mod.modPack = this;
            mods.add(mod);
            node.getChildren().add(mod.node);
        }
    }

    int getActivationLevel() {
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
