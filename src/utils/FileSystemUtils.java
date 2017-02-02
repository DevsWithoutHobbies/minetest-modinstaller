package utils;

import java.io.File;

/**
 * Created by noah on 2/2/17.
 */
public class FileSystemUtils {
    public static void deleteFile(File element) {
        if (!element.exists()) return;
        if (element.isDirectory()) {
            for (File sub : element.listFiles()) {
                deleteFile(sub);
            }
        }
        element.delete();
    }

    public static String sep() {
        if (OSValidator.isWindows()) return "\\";
        else return "/";
    }
}
