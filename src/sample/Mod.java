package sample;

import javafx.scene.control.TreeItem;

/**
 * Created by noah on 2/1/17.
 */
public class Mod {
    String name;
    String zipLink;
    Boolean activated;
    ModPack modPack;
    TreeItem<String> node;

    Mod(String name, String zipLink, Boolean activated) {
        this.name = name;
        this.zipLink = zipLink;
        this.activated = activated;
        this.modPack = null;
        node = new TreeItem<>(name);
    }

    void toggleActivation() {
        activated = !activated;
    }

    Boolean isActivated() {
        return activated;
    }

    void setActivated(Boolean activated) {
        this.activated = activated;
    }
}
