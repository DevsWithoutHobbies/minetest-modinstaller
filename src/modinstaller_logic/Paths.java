package modinstaller_logic;

import utils.OSValidator;

import java.io.File;
import java.io.IOException;

import static utils.FileSystemUtils.sep;

/**
 * Created by noah on 2/2/17.
 */
public class Paths {
    public static String getMinetestDir() {
        if (OSValidator.isWindows()) return System.getenv("APPDATA") + "\\minetest";
        else if (OSValidator.isMac()) return System.getProperty("user.home") + "/Library/Application Support/minetest";
        else return System.getProperty("user.home") + "/.minetest";
    }

    public static String getModsPath() {
        return getMinetestDir() + sep() + "mods";
    }

    public static File getModDirectory(Mod mod) {
        if (mod.modPack == null) {
            //mod is not part of a modPack
            return new File(getModsPath() + sep() + mod.name);
        } else {
            //mod is part of a modPack
            return new File(getModsPath() + sep() + mod.modPack.name + sep() + mod.name);
        }
    }

    public static File getModPackDirectory(ModPack modPack) {
        return new File(getModsPath() + sep() + modPack.name);
    }

    public static void createModpackTXT(ModPack modPack) {
        File modPackFile = new File(getModsPath() + sep() + modPack.name + sep() + "modpack.txt");
        if (!modPackFile.exists()) {
            try {
                if (!modPackFile.createNewFile()) {
                    System.err.println("Unable to create modpack file: " + modPackFile.getAbsolutePath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
