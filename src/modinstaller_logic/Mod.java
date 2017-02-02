package modinstaller_logic;

import javafx.scene.control.TreeItem;

/**
 * Created by noah on 2/1/17.
 */
public class Mod {
    public final String name;
    public final String zipLink;
    private Boolean activated;
    public ModPack modPack;
    public final TreeItem<String> node;

    public Mod(String name, String zipLink, Boolean activated) {
        this.name = name;
        this.zipLink = zipLink;
        this.activated = activated;
        this.modPack = null;
        node = new TreeItem<>(name);
    }

    public void toggleActivation() {
        activated = !activated;
    }

    public Boolean isActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }
}
