package net.teleportmod;

import org.lwjgl.glfw.GLFW;
import net.minecraft.client.resource.language.I18n;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class WarpMenu {
    public static ArrayList<String> LIST = new ArrayList<>(Arrays.asList("Деревня", "Энд", "арена", "колизей", "трасса", "ледяная_трасса"));
    public static int index = 0;
    public static long startTime = 0;
    public static boolean isOpen = false;

    public static boolean isConfigOpen = false;
    public static long mPressStartTime = 0;
    public static int configSelectRow = 0; 
    public static boolean isWaitingForKey = false;

    public static int keyHome = GLFW.GLFW_KEY_O;
    public static int keyRtp = GLFW.GLFW_KEY_R;
    public static int keySpawn = GLFW.GLFW_KEY_C;
    public static int keyWarp = GLFW.GLFW_KEY_P;
    public static long cooldownSec = 20;

    public static int tempHome = keyHome;
    public static int tempRtp = keyRtp;
    public static int tempSpawn = keySpawn;
    public static int tempWarp = keyWarp;
    public static long tempCooldownSec = cooldownSec;
    public static ArrayList<String> tempLIST = new ArrayList<>(LIST);
    public static int deleteSelectIndex = 0;

    private static final File configFile = new File("teleportmod_config.txt");

    public static void navigate(boolean down) {
        if (LIST.isEmpty()) return;
        if (down) index = (index + 1) >= LIST.size() ? 0 : index + 1;
        else index = (index - 1) < 0 ? LIST.size() - 1 : index - 1;
        beep();
    }

    public static void navigateConfig(int direction) {
        configSelectRow += direction;
        if (configSelectRow < 0) configSelectRow = 9;
        if (configSelectRow > 9) configSelectRow = 0;
        beep();
    }

    public static void changeCooldown(boolean increase) {
        if (increase) { tempCooldownSec += 10; if (tempCooldownSec > 1000) tempCooldownSec = 1000; }
        else { tempCooldownSec -= 10; if (tempCooldownSec < 10) tempCooldownSec = 10; }
        beep();
    }

    public static void navigateDeleteSelection(boolean right) {
        if (tempLIST.isEmpty()) return;
        if (right) deleteSelectIndex = (deleteSelectIndex + 1) >= tempLIST.size() ? 0 : deleteSelectIndex + 1;
        else deleteSelectIndex = (deleteSelectIndex - 1) < 0 ? tempLIST.size() - 1 : deleteSelectIndex - 1;
        beep();
    }

    public static void addWarpFromClipboard(long win) {
        try {
            String clipboard = GLFW.glfwGetClipboardString(win);
            if (clipboard != null && !clipboard.trim().isEmpty() && clipboard.length() < 32) {
                String cleanName = clipboard.trim().replace(" ", "_");
                if (!tempLIST.contains(cleanName)) {
                    tempLIST.add(cleanName);
                    deleteSelectIndex = tempLIST.size() - 1;
                    doubleBeep();
                }
            } else { beep(); }
        } catch (Exception e) { beep(); }
    }

    public static void deleteCurrentWarp() {
        if (!tempLIST.isEmpty() && deleteSelectIndex >= 0 && deleteSelectIndex < tempLIST.size()) {
            tempLIST.remove(deleteSelectIndex);
            if (deleteSelectIndex >= tempLIST.size()) deleteSelectIndex = Math.max(0, tempLIST.size() - 1);
            doubleBeep();
        } else { beep(); }
    }

    public static String buildString() {
        if (LIST.isEmpty()) return I18n.translate("menu.empty");
        if (index >= LIST.size()) index = 0;
        return String.format(I18n.translate("action.warp"), LIST.get(index), (index + 1), LIST.size());
    }

    public static String buildConfigString() {
        String prefix = "§d§l" + I18n.translate("menu.config") + " §r";
        if (isWaitingForKey) return prefix + "§d§l" + I18n.translate("menu.waitkey") + "§r";
        
        StringBuilder sb = new StringBuilder(prefix);
        switch (configSelectRow) {
            case 0: sb.append("§e> ").append(I18n.translate("menu.home")).append("§f").append(getKeyName(tempHome)).append(" §e<"); break;
            case 1: sb.append("§e> ").append(I18n.translate("menu.rtp")).append("§f").append(getKeyName(tempRtp)).append(" §e<"); break;
            case 2: sb.append("§e> ").append(I18n.translate("menu.spawn")).append("§f").append(getKeyName(tempSpawn)).append(" §e<"); break;
            case 3: sb.append("§e> ").append(I18n.translate("menu.warp")).append("§f").append(getKeyName(tempWarp)).append(" §e<"); break;
            case 4: sb.append("§6> ").append(I18n.translate("menu.cooldown")).append("§f").append(tempCooldownSec).append(I18n.translate("menu.sec")).append(" §6<"); break;
            case 5: sb.append("§b§l> ").append(I18n.translate("menu.add")).append(" <"); break;
            case 6: 
                sb.append("§c> ").append(I18n.translate("menu.delete"));
                if (tempLIST.isEmpty()) sb.append(I18n.translate("menu.empty"));
                else {
                    if (deleteSelectIndex >= tempLIST.size()) deleteSelectIndex = tempLIST.size() - 1;
                    sb.append(tempLIST.get(deleteSelectIndex));
                }
                sb.append(" ] <");
                break;
            case 7: sb.append("§e§l> ").append(I18n.translate("menu.instruction")).append(" <"); break;
            case 8: sb.append("§a§l> ").append(I18n.translate("menu.save")).append(" <"); break;
            case 9: sb.append("§c§l> ").append(I18n.translate("menu.cancel")).append(" <"); break;
        }
        sb.append(" §8(").append(configSelectRow + 1).append("/10)§r");
        return sb.toString();
    }

    public static String getKeyName(int code) {
        String name = GLFW.glfwGetKeyName(code, 0);
        if (name == null || name.isEmpty()) {
            if (code == GLFW.GLFW_KEY_SPACE) return "SPACE";
            if (code == GLFW.GLFW_KEY_ENTER) return "ENTER";
            return "Key_" + code;
        }
        return name.toUpperCase();
    }

    public static void save() {
        keyHome = tempHome; keyRtp = tempRtp; keySpawn = tempSpawn; keyWarp = tempWarp; 
        cooldownSec = tempCooldownSec; LIST = new ArrayList<>(tempLIST);
        isConfigOpen = false;
        try (PrintWriter writer = new PrintWriter(configFile)) {
            writer.println(keyHome); writer.println(keyRtp); writer.println(keySpawn); writer.println(keyWarp); writer.println(cooldownSec);
            for (String warp : LIST) { writer.println("warp:" + warp); }
        } catch (Exception ignored) {}
        doubleBeep();
    }

    public static void load() {
        if (!configFile.exists()) return;
        try (Scanner scanner = new Scanner(configFile)) {
            if (scanner.hasNextInt()) keyHome = tempHome = scanner.nextInt();
            if (scanner.hasNextInt()) keyRtp = tempRtp = scanner.nextInt();
            if (scanner.hasNextInt()) keySpawn = tempSpawn = scanner.nextInt();
            if (scanner.hasNextInt()) keyWarp = tempWarp = scanner.nextInt();
            if (scanner.hasNextLong()) cooldownSec = tempCooldownSec = scanner.nextLong();
            
            ArrayList<String> loadedWarps = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("warp:")) { loadedWarps.add(line.substring(5)); }
            }
            if (!loadedWarps.isEmpty()) { LIST = new ArrayList<>(loadedWarps); tempLIST = new ArrayList<>(loadedWarps); }
        } catch (Exception ignored) {}
    }

    public static void cancel() {
        tempHome = keyHome; tempRtp = keyRtp; tempSpawn = keySpawn; tempWarp = keyWarp; 
        tempCooldownSec = cooldownSec; tempLIST = new ArrayList<>(LIST);
        isConfigOpen = false;
        beep();
    }

    public static void beep() { try { java.awt.Toolkit.getDefaultToolkit().beep(); } catch (Exception ignored) {} }
    public static void doubleBeep() { try { java.awt.Toolkit.getDefaultToolkit().beep(); Thread.sleep(80); java.awt.Toolkit.getDefaultToolkit().beep(); } catch (Exception ignored) {} }
}
