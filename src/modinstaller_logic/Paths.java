package modinstaller_logic;

import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import utils.OSValidator;
import utils.Utils;

import java.io.*;
import java.nio.charset.Charset;

import static utils.FileSystemUtils.sep;

/**
 * Created by noah on 2/2/17.
 */
public class Paths {
    private static String modsPath = "";
    private static Window window;

    public static String getConfigDir() {
        if (OSValidator.isWindows()) return System.getenv("APPDATA") + "\\minetest_modinstaller";
        else if (OSValidator.isMac()) return System.getProperty("user.home") + "/Library/Application Support/Minetest Modinstaller";
        else return System.getProperty("user.home") + "/.minetest_modinstaller";
    }

    public static File getConfigFile() {
        return new File(getConfigDir() + sep() + "config.txt");
    }

    private static File getPathConfigFile() {
        return new File(getConfigDir() + sep() + "modsPath.txt");
    }

    private static String getDefaultMinetestPath() {
        if (OSValidator.isWindows()) return System.getenv("APPDATA") + "\\minetest";
        else if (OSValidator.isMac()) return System.getProperty("user.home") + "/Library/Application Support/minetest";
        else return System.getProperty("user.home") + "/.minetest";
    }

    public static String getModsPath() {
        return modsPath;
    }

    public static void setWindow(Window newWindow) {
        window = newWindow;
    }

    public static Boolean loadModsPath(Boolean forceUserChoose) {
        Utils.buildDirectory(new File(getConfigDir()));

        File pathConfigFile = getPathConfigFile();
        if (forceUserChoose || !pathConfigFile.exists()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Choose install directory");
            alert.setContentText("Please select the minetest install directory. You can change it later.");
            alert.showAndWait();
            DirectoryChooser ds = new DirectoryChooser();
            ds.setTitle("Choose your minetest install directory");
            File defaultModsDirectory = new File(getDefaultMinetestPath());
            if (defaultModsDirectory.exists())
                ds.setInitialDirectory(defaultModsDirectory);
            File newDirectory = ds.showDialog(window);
            if (newDirectory == null) return false;
            setModsPath(newDirectory.getAbsolutePath() + sep() + "mods");
            return true;
        } else {
            try (
                    InputStream fis = new FileInputStream(pathConfigFile.getAbsoluteFile());
                    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                    BufferedReader br = new BufferedReader(isr)
            ) {
                modsPath = br.readLine();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private static void setModsPath(String newPath) {
        try {
            modsPath = newPath;
            FileWriter fw = new FileWriter(getPathConfigFile().getAbsoluteFile());
            fw.write(newPath);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
