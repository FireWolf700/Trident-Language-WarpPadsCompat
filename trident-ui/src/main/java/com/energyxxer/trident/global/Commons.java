package com.energyxxer.trident.global;

import com.energyxxer.trident.global.temp.projects.Project;
import com.energyxxer.trident.global.temp.projects.ProjectManager;
import com.energyxxer.trident.main.window.TridentWindow;
import com.energyxxer.trident.ui.Tab;
import com.energyxxer.trident.ui.theme.change.ThemeChangeListener;
import com.energyxxer.util.ImageManager;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.util.out.Console;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Commons {

    public static String DEFAULT_CARET_DISPLAY_TEXT = "-:-";

    public static String themeAssetsPath = "light_theme/";

    static {
        ThemeChangeListener.addThemeChangeListener(t -> {
            themeAssetsPath = t.getString("Assets.path","default:light_theme/");
        }, true);
    }

    public static boolean isSpecialCharacter(char ch) {
        return "\b\r\n\t\f\u007F\u001B".contains("" + ch);
    }

    public static void showInExplorer(String path) {
        try {
            if(System.getProperty("os.name").startsWith("Windows")) {
                Runtime.getRuntime().exec("Explorer.exe /select," + path);
            } else if(Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(new File(path).getParentFile());
            } else {
                Debug.log("Couldn't open file '" + path + "': Desktop is not supported", Debug.MessageType.ERROR);
            }
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    private static String getIconPath(String name) {
        return "/assets/icons/" + themeAssetsPath + name + ".png";
    }

    public static BufferedImage getIcon(String name) {
        return ImageManager.load(getIconPath(name));
    }

    public static void updateActiveProject() {
        if(TridentWindow.toolbar != null && TridentWindow.projectExplorer != null)
            TridentWindow.toolbar.setActiveProject(getActiveProject());
    }

    public static Project getActiveProject() {
        Project selected = null;

        Tab selectedTab = TabManager.getSelectedTab();

        List<String> selectedFiles = TridentWindow.projectExplorer.getSelectedFiles();

        if(selectedTab != null && selectedTab.getLinkedProject() != null) {
            selected = selectedTab.getLinkedProject();
        } else if(selectedFiles.size() > 0) {
            selected = ProjectManager.getAssociatedProject(new File(selectedFiles.get(0)));
        }
        return selected;
    }

    public static void compileActive() {
        if(Commons.getActiveProject() == null) return;
        /*Compiler c = new Compiler(Commons.getActiveProject());
        c.setLibrary(Resources.nativeLib);
        c.addProgressListener(TridentWindow::setStatus);
        c.addCompletionListener(() -> {
            TridentWindow.noticeExplorer.setNotices(c.getReport().groupByLabel());
            if(c.getReport().getTotal() > 0) TridentWindow.noticeBoard.open();
            c.getReport().getWarnings().forEach(Console.warn::println);
            c.getReport().getErrors().forEach(Console.err::println);
        });
        c.compile();*/
    }
}
