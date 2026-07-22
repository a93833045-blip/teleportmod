package net.teleportmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import org.lwjgl.glfw.GLFW;

public class TeleportLogic {
    private static long lH = 0, lR = 0, lS = 0, lB = 0, lW = 0;
    private static boolean pH=false, pR=false, pW=false, pS=false, pB=false, pL=false, pRi=false, pU=false, pD=false, pE=false, pEsc=false;
    private static long bPressTime = 0;
    private static long wPressTime = 0; // Время фиксации нажатия кнопки P

    public static void runCmd(MinecraftClient client, String cmd) {
        if (client == null) return;
        String cleanedCmd = cmd.trim();
        if (cleanedCmd.startsWith("/")) { 
            cleanedCmd = cleanedCmd.substring(1); 
        }
        final String finalCmd = cleanedCmd;
        
        client.execute(() -> {
            if (client.getNetworkHandler() != null) {
                client.getNetworkHandler().sendPacket(new CommandExecutionC2SPacket(finalCmd));
            }
        });
    }

    public static void processInputs(MinecraftClient client, long now, long win) {
        // === ЖЕЛЕЗОБЕТОННЫЙ ЩИТ ОТ ЧАТА, СУНДУКОВ И ИНВЕНТАРЯ ===
        if (client.currentScreen != null) {
            pH = false; pR = false; pS = false; pW = false; pB = false;
            return;
        }

        // === 1. ЗАЖАТИЕ КЛАВИШИ М (3 СЕКУНДЫ) ===
        boolean isMPressed = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_M) == GLFW.GLFW_PRESS;
        if (isMPressed && !TeleportData.isConfigMenuOpen && !TeleportData.isWarpMenuOpen && !TeleportData.isPlayerMenuOpen) {
            if (TeleportData.mPressStartTimestamp == 0) TeleportData.mPressStartTimestamp = now;
            if (now - TeleportData.mPressStartTimestamp > 3000) { TeleportData.isConfigMenuOpen = true; TeleportData.mPressStartTimestamp = 0; }
        } else if (!isMPressed) TeleportData.mPressStartTimestamp = 0;

        // === 2. НАВИГАЦИЯ ВНУТРИ НАСТРОЕК (М) ===
        if (TeleportData.isConfigMenuOpen) {
            boolean esc = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS;
            if (esc && !pEsc) { TeleportData.cancel(); } pEsc = esc;

            if (TeleportData.isWaitingForBind) {
                for (int i = 32; i <= 348; i++) {
                    if (i == GLFW.GLFW_KEY_ENTER || i == GLFW.GLFW_KEY_ESCAPE) continue; 
                    if (GLFW.glfwGetKey(win, i) == GLFW.GLFW_PRESS) {
                        if (TeleportData.configSelectRow == 0) TeleportData.tempKeyHome = i;
                        if (TeleportData.configSelectRow == 1) TeleportData.tempKeyRtp = i;
                        if (TeleportData.configSelectRow == 2) TeleportData.tempKeySpawn = i;
                        if (TeleportData.configSelectRow == 3) TeleportData.tempKeyWarp = i;
                        if (TeleportData.configSelectRow == 4) TeleportData.tempKeyTpa = i;
                        TeleportData.isWaitingForBind = false; break;
                    }
                }
                return;
            }

            boolean up = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS;
            if (up && !pU) { TeleportData.configSelectRow = (TeleportData.configSelectRow - 1 < 0) ? TeleportData.MAX_CONFIG_ROWS : TeleportData.configSelectRow - 1; } pU = up;
            boolean down = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS;
            if (down && !pD) { TeleportData.configSelectRow = (TeleportData.configSelectRow + 1 > TeleportData.MAX_CONFIG_ROWS) ? 0 : TeleportData.configSelectRow + 1; } pD = down;

            if (TeleportData.configSelectRow == 5) {
                boolean left = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS;
                if (left && !pL) TeleportData.tempCooldownDurationSec = Math.max(10, TeleportData.tempCooldownDurationSec - 10); pL = left;
                boolean right = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS;
                if (right && !pRi) TeleportData.tempCooldownDurationSec = Math.min(1000, TeleportData.tempCooldownDurationSec + 10); pRi = right;
            }
            if (TeleportData.configSelectRow == 6) {
                boolean left = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS; 
                if (left && !pL) { TeleportData.tempTpMode = !TeleportData.tempTpMode; } pL = left;
                boolean right = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS;
                if (right && !pRi) { TeleportData.tempTpMode = !TeleportData.tempTpMode; } pRi = right;
            }
            if (TeleportData.configSelectRow == 8) {
                boolean left = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS;
                if (left && !pL) TeleportData.deleteWarpIndex = (TeleportData.deleteWarpIndex - 1 < 0) ? TeleportData.tempWarpList.size() - 1 : TeleportData.deleteWarpIndex - 1; pL = left;
                boolean right = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS;
                if (right && !pRi) TeleportData.deleteWarpIndex = (TeleportData.deleteWarpIndex + 1 >= TeleportData.tempWarpList.size()) ? 0 : TeleportData.deleteWarpIndex + 1; pRi = right;
            }

            boolean enter = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS;
            if (!enter && pE) { 
                if (TeleportData.configSelectRow <= 4) { TeleportData.isWaitingForBind = true; }
                else if (TeleportData.configSelectRow == 7) TeleportData.addWarpFromClipboard(win);
                else if (TeleportData.configSelectRow == 8) { if(!TeleportData.tempWarpList.isEmpty()) { TeleportData.tempWarpList.remove(TeleportData.deleteWarpIndex); TeleportData.deleteWarpIndex = 0; } }
                else if (TeleportData.configSelectRow == 9) TeleportData.addPlayerFromClipboard(win);
                else if (TeleportData.configSelectRow == 10) TeleportData.clearSelectedPlayer();
                else if (TeleportData.configSelectRow == 11) { TeleportRender.printInstruction(client); }
                else if (TeleportData.configSelectRow == 12) TeleportData.save();
                else if (TeleportData.configSelectRow == 13) TeleportData.cancel();
            } pE = enter;
            return;
        }
        // === 3. НАВИГАЦИЯ В СЕЛЕКТОРАХ СТРЕЛКАМИ ===
        if (TeleportData.isWarpMenuOpen) {
            boolean up = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS;
            if (up && !pU) TeleportData.warpIndex = (TeleportData.warpIndex - 1 < 0) ? TeleportData.warpList.size() - 1 : TeleportData.warpIndex - 1; pU = up;
            boolean down = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS;
            if (down && !pD) TeleportData.warpIndex = (TeleportData.warpIndex + 1 >= TeleportData.warpList.size()) ? 0 : TeleportData.warpIndex + 1; pD = down;
            
            // Проверка сброса зажатия P прямо во время открытого меню варпов
            boolean kw = GLFW.glfwGetKey(win, TeleportData.keyWarp) == GLFW.GLFW_PRESS;
            if (!kw && pW) {
                if (!TeleportData.warpList.isEmpty()) {
                    if (TeleportData.warpIndex >= TeleportData.warpList.size()) TeleportData.warpIndex = 0;
                    runCmd(client, "warp " + TeleportData.warpList.get(TeleportData.warpIndex));
                }
                TeleportData.isWarpMenuOpen = false;
            }
            pW = kw;
            return; 
        }
        if (TeleportData.isPlayerMenuOpen) {
            boolean up = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS;
            if (up && !pU) { TeleportData.playerIndex = (TeleportData.playerIndex - 1 < 0) ? TeleportData.playersTab.size() - 1 : TeleportData.playerIndex - 1; if(!TeleportData.playersTab.get(0).equals("No_Players")) TeleportData.selectedPlayer = TeleportData.playersTab.get(TeleportData.playerIndex); } pU = up;
            boolean down = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS;
            if (down && !pD) { TeleportData.playerIndex = (TeleportData.playerIndex + 1 >= TeleportData.playersTab.size()) ? 0 : TeleportData.playerIndex + 1; if(!TeleportData.playersTab.get(0).equals("No_Players")) TeleportData.selectedPlayer = TeleportData.playersTab.get(TeleportData.playerIndex); } pD = down;
            
            boolean kt = GLFW.glfwGetKey(win, TeleportData.keyTpa) == GLFW.GLFW_PRESS;
            if (!kt && pB) {
                if (!TeleportData.playersTab.isEmpty() && !TeleportData.playersTab.get(0).equals("No_Players")) { TeleportData.selectedPlayer = TeleportData.playersTab.get(TeleportData.playerIndex); } else { TeleportData.selectedPlayer = ""; }
                TeleportData.saveSilent(); TeleportData.isPlayerMenuOpen = false; TeleportData.statusShowUntil = 0;
            }
            pB = kt;
            return; 
        }

        // === 4. ОБРАБОТКА ДВОЙНЫХ КЛИКОВ МАКРОСОВ ===
        boolean kh = GLFW.glfwGetKey(win, TeleportData.keyHome) == GLFW.GLFW_PRESS;
        if (kh && !pH) {
            if (now - lH < 400) { runCmd(client, "home o"); lH = 0; } else { lH = now; }
        } pH = kh;

        boolean ks = GLFW.glfwGetKey(win, TeleportData.keySpawn) == GLFW.GLFW_PRESS;
        if (ks && !pS) {
            if (now - lS < 400) { runCmd(client, "spawn"); lS = 0; } else { lS = now; }
        } pS = ks;

        boolean kr = GLFW.glfwGetKey(win, TeleportData.keyRtp) == GLFW.GLFW_PRESS;
        if (kr && !pR) {
            if (now - lR < 400) {
                runCmd(client, "rtp"); lR = 0;
                if (now >= TeleportData.cooldownEndTimestamp) { TeleportData.cooldownEndTimestamp = now + (TeleportData.cooldownDurationSec * 1000); TeleportData.hideReadyNotificationTimestamp = 0; }
            } else { lR = now; }
        } pR = kr;

        // === ИСПРАВЛЕННАЯ ПОЛНАЯ МЕХАНИКА ДЛЯ P (ВАРПЫ) ===
        boolean kw = GLFW.glfwGetKey(win, TeleportData.keyWarp) == GLFW.GLFW_PRESS;
        if (kw && !pW) {
            wPressTime = now; // Запоминаем время клика
        } else if (!kw && pW) {
            long holdDuration = now - wPressTime;
            if (now - lW < 400) {
                // Двойное быстрое нажатие: мгновенное ТП в самый ПЕРВЫЙ варп списка
                if (!TeleportData.warpList.isEmpty()) {
                    TeleportData.warpIndex = 0;
                    runCmd(client, "warp " + TeleportData.warpList.get(0));
                }
                lW = 0;
            } else {
                lW = now;
            }
        }
        // Если удерживаем P дольше 300мс — активируем радиальный список варпов
        if (kw && !TeleportData.isWarpMenuOpen && (now - wPressTime > 300)) {
            TeleportData.isWarpMenuOpen = true;
        }
        pW = kw;

        // --- МЕХАНИКА B ---
        boolean kt = GLFW.glfwGetKey(win, TeleportData.keyTpa) == GLFW.GLFW_PRESS;
        if (kt && !pB) { bPressTime = now; }
        else if (!kt && pB) {
            long holdDuration = now - bPressTime;
            if (now - lB < 400) {
                if (TeleportData.selectedPlayer != null && !TeleportData.selectedPlayer.isEmpty()) { 
                    TeleportData.tpaRequestTimestamp = now; TeleportData.statusShowUntil = now + 3000; 
                    String baseCmd = TeleportData.isTpMode ? "tp " : "tpa "; runCmd(client, baseCmd + TeleportData.selectedPlayer); 
                }
                lB = 0;
            } else {
                if (holdDuration < 300) { TeleportData.statusShowUntil = now + 3000; }
                lB = now;
            }
        }
        if (kt && !TeleportData.isPlayerMenuOpen && (now - bPressTime > 300)) { TeleportData.updatePlayerList(client); TeleportData.isPlayerMenuOpen = true; TeleportData.statusShowUntil = 0; }
        pB = kt;
    }
}
