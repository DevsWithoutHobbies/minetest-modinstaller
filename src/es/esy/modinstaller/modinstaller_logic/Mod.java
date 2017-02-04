package es.esy.modinstaller.modinstaller_logic;

import javafx.scene.control.TreeItem;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

/**
 * Created by noah on 2/1/17.
 */
public class Mod {
    public final String name;
    private final String description;
    public final String zipLink;
    public final String zipPath;
    public Boolean isLib;
    public String urlData;
    private Boolean activated;
    public ModPack modPack;


    public final TreeItem<String> node;

    public Mod(String dataString) {
        String[] parts = dataString.split(":::");

        this.name = parts[0];
        this.zipLink = parts[1];
        this.zipPath = parts[2];
        this.isLib = Objects.equals(parts[3], "1");
        this.description = parts[6];

        try {
            this.urlData = URLEncoder.encode(dataString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            this.urlData = "";
            e.printStackTrace();
        }
        this.activated = false;
        this.modPack = null;
        node = new TreeItem<>(name);
    }

    public Boolean matchesSearchString(String searchString) {
        return name.toLowerCase().contains(searchString.toLowerCase()) || description.toLowerCase().contains(searchString.toLowerCase());
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
