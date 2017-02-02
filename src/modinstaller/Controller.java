package modinstaller;

import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.web.WebView;
import modinstaller_logic.Mod;
import modinstaller_logic.ModPack;
import utils.Utils;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static modinstaller_logic.Paths.*;
import static utils.FileSystemUtils.deleteFile;
import static utils.FileSystemUtils.sep;


public class Controller implements Initializable {
    public Button install_btn;
    public TreeView<String> tree_view;
    public WebView web_view;

    private final List<Mod> modList;
    private final List<ModPack> modPackList;

    private final Image iconGreen = new Image(getClass().getResourceAsStream("/img/green.png"));
    private final Image iconYellow = new Image(getClass().getResourceAsStream("/img/yellow.png"));
    private final Image iconGray = new Image(getClass().getResourceAsStream("/img/gray.png"));


    public Controller() {
        this.modList = new ArrayList<>();
        this.modPackList = new ArrayList<>();
    }

    public void install() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Install Mods");
        alert.setHeaderText("Important information");
        alert.setContentText("This tool will download and install software. Make sure that you trust the authors of the selected mods. Please notice that this mod installer doesn't remove any mods, which aren't indexed by this mod-installer. To uninstall these mods you have to delete them manually.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            saveActivatedMods();

            String modsPath = getModsPath();
            String modInstallerPath = getMinetestDir() + sep() + "mod_installer_data";
            String tmpModsPath = getMinetestDir() + sep() + "mod_installer_data" + sep() + "data";

            Utils.buildDirectory(new File(modsPath));
            Utils.buildDirectory(new File(modInstallerPath));
            Utils.buildDirectory(new File(tmpModsPath));

            List<Mod> toInstall = new ArrayList<>();

            for (Mod mod : modList) {
                if (mod.isActivated()) toInstall.add(mod);

                // delete old version if exists
                deleteFile(getModDirectory(mod));
                if (mod.modPack != null) {
                    deleteFile(getModPackDirectory(mod.modPack));
                }
            }

            List<String> manualInstallRequired = new ArrayList<>();
            List<String> manualInstallOptional = new ArrayList<>();
            int i = 0;
            while (i < toInstall.size()) {
                // get mod
                Mod mod = toInstall.get(i);

                // create temporary download directory
                File tmpModFile = new File(tmpModsPath + sep() + mod.name);
                Utils.buildDirectory(tmpModFile);

                // get final location
                File modFile = getModDirectory(mod);

                // generate modpack.txt if required
                if (mod.modPack != null) {
                    Utils.buildDirectory(getModPackDirectory(mod.modPack));
                    createModpackTXT(mod.modPack);
                }

                try {
                    // unpack in temporary directory
                    Utils.unpackArchive(new URL(mod.zipLink), tmpModFile);

                    //get all directories
                    File[] directories = tmpModFile.listFiles(File::isDirectory);

                    // move first folder to final location
                    if (directories != null && directories.length > 0) {
                        Files.move(directories[0].toPath(), modFile.toPath());
                        File dependenciesFile = new File(modFile + sep() + "depends.txt");
                        if (dependenciesFile.exists()) {
                            String line;
                            try (
                                    InputStream fis = new FileInputStream(dependenciesFile.getAbsoluteFile());
                                    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                                    BufferedReader br = new BufferedReader(isr)
                            ) {
                                while ((line = br.readLine()) != null) {
                                    String name = line;
                                    Boolean optional = false;
                                    if (line.lastIndexOf("?") == line.length() - 1) {
                                        name = line.substring(0, line.length() - 1);
                                        optional = true;
                                    }
                                    Mod m = getModByName(name);
                                    if (m == null) {
                                        if (!optional && !manualInstallRequired.contains(name))
                                            manualInstallRequired.add(name);
                                        else if (optional && !manualInstallOptional.contains(name))
                                            manualInstallOptional.add(name);
                                    } else {
                                        if (!toInstall.contains(m))
                                            toInstall.add(m);
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // remove temporary download directory
                deleteFile(tmpModFile);

                i++;
            }

            if (manualInstallOptional.size() > 0 || manualInstallRequired.size() > 0) {
                Alert alertManInstall = new Alert(Alert.AlertType.WARNING);
                alertManInstall.setTitle("Manual install mods");
                alertManInstall.setHeaderText("Important information");

                String textRequired = manualInstallRequired.size() > 0 ? "You will need to install the following " +
                        "mods manually, because they aren't indexed by this mod-installer:\n" +
                        manualInstallRequired : "";

                List<String> manualInstallOptionalFiltered = manualInstallOptional.stream().filter(n -> !manualInstallRequired.contains(n)).collect(Collectors.toList());

                String textOptional = manualInstallOptionalFiltered.size() > 0 ? "The following mods aren't indexed by this " +
                        "mod-installer but are optional dependencies of at least one mod. You can install " +
                        "them manually:\n" + manualInstallOptionalFiltered : "";

                if (textRequired.length() > 0 && textOptional.length() > 0) {
                    alertManInstall.setContentText(textRequired + "\n\n" + textOptional);
                } else {
                    alertManInstall.setContentText(textRequired + textOptional);
                }

                alertManInstall.show();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        long startTime = System.nanoTime();
        try {
            loadMods();
            loadModPacks();
            loadActivatedMods();
        } catch (IOException e) {
            e.printStackTrace();
        }
        resetTreeView();
        updateImages();
        System.out.println("Init: " + (System.nanoTime() - startTime) / 1000000000.0 + "s");
    }

    private Mod getModByName(String name) {
        for (Mod mod : modList) {
            if (Objects.equals(mod.name, name)) return mod;
        }
        return null;
    }

    private void loadMods() throws IOException {
        URL mod_data = new URL("https://raw.githubusercontent.com/DevsWithoutHobbies/minetest-modinstaller-data/master/index");
        BufferedReader in = new BufferedReader(new InputStreamReader(mod_data.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            String[] parts = inputLine.split(":::");
            modList.add(new Mod(parts[0], parts[1], false));
        }
        in.close();

        modList.sort((a, b) -> b.name.compareTo(a.name));
    }

    private void loadModPacks() throws IOException {
        URL mod_data = new URL("https://raw.githubusercontent.com/DevsWithoutHobbies/minetest-modinstaller-data/master/pack-index");
        BufferedReader in = new BufferedReader(new InputStreamReader(mod_data.openStream()));


        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            String[] parts = inputLine.split(":::");

            ModPack mp = new ModPack(parts[0]);

            for (int i = 1; i < parts.length; i++) {
                Mod mod = getModByName(parts[i]);
                mp.addMod(mod);
            }
            modPackList.add(mp);
        }

        in.close();

        modPackList.sort((a, b) -> b.name.compareTo(a.name));
    }

    private File getConfigFile() {
        return new File(getMinetestDir() + sep() + "mod_installer_data" + sep() + "config.txt");
    }

    private void loadActivatedMods() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            String line;
            try (
                    InputStream fis = new FileInputStream(configFile.getAbsoluteFile());
                    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                    BufferedReader br = new BufferedReader(isr)
            ) {
                while ((line = br.readLine()) != null) {
                    Mod m = getModByName(line);
                    if (m != null) {
                        m.setActivated(true);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveActivatedMods() {
        try {
            FileWriter fw = new FileWriter(getConfigFile().getAbsoluteFile());

            for (Mod mod : modList) {
                if (mod.isActivated()) fw.write(mod.name + "\n");
            }

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void updateImages() {
        for (ModPack modPack : modPackList) {
            int activationLevel = modPack.getActivationLevel();
            if (activationLevel == 0) modPack.node.setGraphic(new ImageView(iconGray));
            else if (activationLevel == 1) modPack.node.setGraphic(new ImageView(iconYellow));
            else modPack.node.setGraphic(new ImageView(iconGreen));
        }

        int activeModCount = 0;
        for (Mod mod : modList) {
            if (mod.isActivated()) {
                activeModCount++;
                mod.node.setGraphic(new ImageView(iconGreen));
            } else {
                mod.node.setGraphic(new ImageView(iconGray));
            }
        }

        if (activeModCount == 0) tree_view.getRoot().setGraphic(new ImageView(iconGray));
        else if (activeModCount == modList.size()) tree_view.getRoot().setGraphic(new ImageView(iconGreen));
        else tree_view.getRoot().setGraphic(new ImageView(iconYellow));
    }

    private void resetTreeView() {

        TreeItem<String> root = new TreeItem<>("All");

        root.setExpanded(true);

        for (ModPack modPack : modPackList) {
            root.getChildren().add(modPack.node);
        }

        for (Mod mod : modList) {
            if (mod.modPack == null)
                root.getChildren().add(mod.node);
        }

        tree_view.setRoot(root);

        tree_view.setOnMouseClicked(mouseEvent -> {
            TreeItem<String> currentItemSelected = tree_view.getSelectionModel().getSelectedItem();
            if (currentItemSelected != null) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    for (Mod mod : modList) {
                        if (currentItemSelected == mod.node) {
                            showModInfo(mod.name);
                        }
                    }
                }
                if (mouseEvent.getButton() == MouseButton.SECONDARY || mouseEvent.getClickCount() == 2) {
                    if (currentItemSelected == tree_view.getRoot()) {
                        toggleAll();
                        updateImages();
                        return;
                    }
                    for (ModPack modPack : modPackList) {
                        if (currentItemSelected == modPack.node) {
                            modPack.toggleActivation();
                            updateImages();
                            return;
                        }
                    }
                    for (Mod mod : modList) {
                        if (currentItemSelected == mod.node) {
                            mod.toggleActivation();
                            updateImages();
                            return;
                        }
                    }
                }
            }
        });
    }

    private void toggleAll() {
        int activeModCount = 0;
        for (Mod mod : modList) {
            if (mod.isActivated()) activeModCount++;
        }
        Boolean newStatus = activeModCount < modList.size();
        for (Mod mod : modList) {
            mod.setActivated(newStatus);
        }
    }


    private void showModInfo(String modName) {
        web_view.getEngine().load("https://www.google.de/#q=" + modName);

    }
}
