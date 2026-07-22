package net.teleportmod;

import org.lwjgl.glfw.GLFW;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

public class TeleportData {
    public static ArrayList<String> warpList = new ArrayList<>(Arrays.asList("Деревня", "Энд", "арена", "колизей", "трасса", "ледяная_трасса"));
    public static ArrayList<String> tempWarpList = new ArrayList<>(warpList);
    public static int warpIndex = 0;
    public static int deleteWarpIndex = 0;
    public static boolean isWarpMenuOpen = false;

    public static ArrayList<String> playersTab = new ArrayList<>();
    public static int playerIndex = 0;
    public static boolean isPlayerMenuOpen = false;
    public static String selectedPlayer = "";
    public static long tpaRequestTimestamp = 0;

    public static long cooldownDurationSec = 20;
    public static long tempCooldownDurationSec = cooldownDurationSec;
    public static long cooldownEndTimestamp = 0;
    public static long hideReadyNotificationTimestamp = 0;

    public static int keyHome = GLFW.GLFW_KEY_O;
    public static int keyRtp = GLFW.GLFW_KEY_R;
    public static int keySpawn = GLFW.GLFW_KEY_C;
    public static int keyWarp = GLFW.GLFW_KEY_P;
    public static int keyTpa = GLFW.GLFW_KEY_B;
    public static boolean isTpMode = false;

    public static int tempKeyHome = keyHome;
    public static int tempKeyRtp = keyRtp;
    public static int tempKeySpawn = keySpawn;
    public static int tempKeyWarp = keyWarp;
    public static int tempKeyTpa = keyTpa;
    public static boolean tempTpMode = isTpMode;

    public static boolean isConfigMenuOpen = false;
    public static int configSelectRow = 0;
    public static boolean isWaitingForBind = false;
    public static long mPressStartTimestamp = 0;
    public static long statusShowUntil = 0;

    private static final File CONFIG_FILE = new File("teleportmod_config.txt");
    public static final int MAX_CONFIG_ROWS = 13;

    public static String cleanName(String raw) {
        if (raw == null) return "";
        String s = raw.replaceAll("§.", "");
        s = s.replaceAll("\\[.*?\\]", "").replaceAll("\\(.*?\\)", "").replaceAll("\\{.*?\\}", "");
        s = s.replaceAll("[:>»«<\\-]", "");
        s = s.replaceAll("[^a-zA-Z0-9_]", "");
        return s.trim();
    }

    public static String getCleanPlayerName(PlayerListEntry entry) {
        Text displayName = entry.getDisplayName();
        if (displayName == null) return cleanName(entry.getProfile().getName());
        String cleaned = cleanName(displayName.getString());
        return cleaned.isEmpty() ? cleanName(entry.getProfile().getName()) : cleaned;
    }

    // === ИСПРАВЛЕНИЕ: ДОБАВЛЕНЫ НЕДОСТАЮЩИЕ МЕТОДЫ РАБОТЫ С БУФЕРОМ И ОЧИСТКИ ===
    public static void addPlayerFromClipboard(long win) {
        try {
            String clip = GLFW.glfwGetClipboardString(win);
            if (clip != null && !clip.trim().isEmpty()) {
                String clean = cleanName(clip);
                if (!clean.isEmpty() && clean.length() <= 16) {
                    selectedPlayer = clean;
                    saveSilent();
                }
            }
        } catch (Exception ignored) {}
    }

    public static void clearSelectedPlayer() {
        selectedPlayer = "";
        saveSilent();
    }

    public static void addWarpFromClipboard(long win) {
        try {
            String clipboard = GLFW.glfwGetClipboardString(win);
            if (clipboard != null && !clipboard.trim().isEmpty() && clipboard.length() < 32) {
                String cleanName = clipboard.trim().replace(" ", "_");
                if (!tempWarpList.contains(cleanName)) {
                    tempWarpList.add(cleanName);
                    deleteWarpIndex = tempWarpList.size() - 1;
                }
            }
        } catch (Exception ignored) {}
    }
    public static void updatePlayerList(MinecraftClient client) {
        playersTab.clear();
        if (client.getNetworkHandler() == null) return;
        Collection<PlayerListEntry> entries = client.getNetworkHandler().getPlayerList();
        String myName = client.player != null ? cleanName(client.player.getName().getString()) : "";

        for (PlayerListEntry entry : entries) {
            String name = getCleanPlayerName(entry);
            if (!name.isEmpty() && !name.equalsIgnoreCase(myName) && !playersTab.contains(name)) {
                playersTab.add(name);
            }
        }
        if (playersTab.isEmpty()) { playersTab.add("No_Players"); }
    }

    public static void saveSilent() {
        try (PrintWriter writer = new PrintWriter(CONFIG_FILE)) {
            writer.println(keyHome); writer.println(keyRtp); writer.println(keySpawn); writer.println(keyWarp); writer.println(keyTpa);
            writer.println(cooldownDurationSec); writer.println(isTpMode ? "1" : "0");
            writer.println("selectedPlayer:" + selectedPlayer);
            for (String warp : warpList) { writer.println("warp:" + warp); }
        } catch (Exception ignored) {}
    }

    public static void save() {
        keyHome = tempKeyHome; keyRtp = tempKeyRtp; keySpawn = tempKeySpawn; keyWarp = tempKeyWarp; keyTpa = tempKeyTpa;
        cooldownDurationSec = tempCooldownDurationSec; isTpMode = tempTpMode; warpList = new ArrayList<>(tempWarpList);
        isConfigMenuOpen = false;
        saveSilent();
    }

    public static void cancel() {
        isConfigMenuOpen = false;
        tempKeyHome = keyHome; tempKeyRtp = keyRtp; tempKeySpawn = keySpawn; tempKeyWarp = keyWarp; tempKeyTpa = keyTpa;
        tempCooldownDurationSec = cooldownDurationSec; tempTpMode = isTpMode; tempWarpList = new ArrayList<>(warpList);
    }

    // === ОПЕЧАТКА ИСПРАВЛЕНА: tempSpawn ЗАМЕНЕН НА НАСТОЯЩИЙ tempKeySpawn ===
    public static void load() {
        if (!CONFIG_FILE.exists()) return;
        try (Scanner scanner = new Scanner(CONFIG_FILE)) {
            if (scanner.hasNextInt()) keyHome = tempKeyHome = scanner.nextInt();
            if (scanner.hasNextInt()) keyRtp = tempKeyRtp = scanner.nextInt();
            if (scanner.hasNextInt()) keySpawn = tempKeySpawn = scanner.nextInt();
            if (scanner.hasNextInt()) keyWarp = tempKeyWarp = scanner.nextInt();
            
            if (scanner.hasNextLong()) {
                long val = scanner.nextLong();
                if (val > 300 || val == GLFW.GLFW_KEY_B) {
                    keyTpa = tempKeyTpa = (int) val;
                    if (scanner.hasNextLong()) cooldownDurationSec = tempCooldownDurationSec = scanner.nextLong();
                } else {
                    cooldownDurationSec = tempCooldownDurationSec = val;
                    keyTpa = tempKeyTpa = GLFW.GLFW_KEY_B;
                }
            }
            ArrayList<String> loadedWarps = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.equals("1") || line.equals("0")) { isTpMode = tempTpMode = line.equals("1"); }
                else if (line.startsWith("selectedPlayer:")) { selectedPlayer = line.substring(15); }
                else if (line.startsWith("warp:")) { loadedWarps.add(line.substring(5)); }
                else if (!line.isEmpty() && !line.contains(":") && loadedWarps.isEmpty() && line.length() < 32) { loadedWarps.add(line); }
            }
            if (!loadedWarps.isEmpty()) { warpList = new ArrayList<>(loadedWarps); tempWarpList = new ArrayList<>(loadedWarps); }
        } catch (Exception ignored) {}
    }
}
