package net.teleportmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class TeleportModClient implements ClientModInitializer {
    private static long lH = 0, lR = 0, lW = 0, lS = 0;
    private static boolean prevH = false, prevR = false, prevW = false, prevS = false, prevL = false, prevRi = false, prevU = false, prevD = false, prevE = false, prevEsc = false;
    private static long cdEnd = 0, hideReady = 0;

    @Override
    public void onInitializeClient() {
        WarpMenu.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            long now = System.currentTimeMillis();
            long win = client.getWindow().getHandle();

            // === 1. ЗАЖАТИЕ КЛАВИШИ М (3 СЕКУНДЫ) ===
            boolean isMPressed = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_M) == GLFW.GLFW_PRESS;
            if (isMPressed && client.currentScreen == null && !WarpMenu.isConfigOpen && !WarpMenu.isOpen) {
                if (WarpMenu.mPressStartTime == 0) WarpMenu.mPressStartTime = now;
                if (now - WarpMenu.mPressStartTime > 3000) {
                    WarpMenu.isConfigOpen = true; WarpMenu.mPressStartTime = 0; WarpMenu.doubleBeep();
                }
            } else if (!isMPressed) WarpMenu.mPressStartTime = 0;

            // === 2. НАВИГАЦИЯ ВНУТРИ МЕНЮ НАСТРОЕК (М) ===
            if (WarpMenu.isConfigOpen && client.currentScreen == null) {
                boolean esc = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS;
                if (esc && !prevEsc) { WarpMenu.cancel(); } prevEsc = esc;

                if (WarpMenu.isWaitingForKey) {
                    for (int i = 32; i <= 348; i++) {
                        if (i == GLFW.GLFW_KEY_ENTER || i == GLFW.GLFW_KEY_ESCAPE) continue; 
                        if (GLFW.glfwGetKey(win, i) == GLFW.GLFW_PRESS) {
                            if (WarpMenu.configSelectRow == 0) WarpMenu.tempHome = i;
                            if (WarpMenu.configSelectRow == 1) WarpMenu.tempRtp = i;
                            if (WarpMenu.configSelectRow == 2) WarpMenu.tempSpawn = i;
                            if (WarpMenu.configSelectRow == 3) WarpMenu.tempWarp = i;
                            WarpMenu.isWaitingForKey = false; WarpMenu.beep(); break;
                        }
                    }
                } else if (WarpMenu.isConfigOpen) {
                    boolean up = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS;
                    if (up && !prevU) WarpMenu.navigateConfig(-1); prevU = up;
                    
                    boolean down = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS;
                    if (down && !prevD) WarpMenu.navigateConfig(1); prevD = down;

                    if (WarpMenu.configSelectRow == 4) {
                        boolean left = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS;
                        if (left && !prevL) WarpMenu.changeCooldown(false); prevL = left;
                        boolean right = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS;
                        if (right && !prevRi) WarpMenu.changeCooldown(true); prevRi = right;
                    }
                    
                    if (WarpMenu.configSelectRow == 6) {
                        boolean left = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS;
                        if (left && !prevL) WarpMenu.navigateDeleteSelection(false); prevL = left;
                        boolean right = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS;
                        if (right && !prevRi) WarpMenu.navigateDeleteSelection(true); prevRi = right;
                    }

                    boolean enter = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS;
                    if (!enter && prevE) { 
                        if (WarpMenu.configSelectRow <= 3) { WarpMenu.isWaitingForKey = true; WarpMenu.beep(); }
                        else if (WarpMenu.configSelectRow == 5) WarpMenu.addWarpFromClipboard(win);
                        else if (WarpMenu.configSelectRow == 6) WarpMenu.deleteCurrentWarp();
                        else if (WarpMenu.configSelectRow == 7) { WarpInstruction.print(client); WarpMenu.doubleBeep(); }
                        else if (WarpMenu.configSelectRow == 8) WarpMenu.save();
                        else if (WarpMenu.configSelectRow == 9) WarpMenu.cancel();
                    } prevE = enter;
                }
            }

            // === 3. НАВИГАЦИЯ В МЕНЮ ВАРПОВ (P) ===
            if (WarpMenu.isOpen && client.currentScreen == null) {
                boolean up = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS;
                if (up && !prevU) WarpMenu.navigate(false); prevU = up;
                boolean down = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS;
                if (down && !prevD) WarpMenu.navigate(true); prevD = down;
            }

            // === 4. ВЫВОД ИНТЕРФЕЙСА В ЭКШЕН-БАР ===
            if (client.currentScreen == null) {
                if (WarpMenu.isConfigOpen) {
                    client.player.sendMessage(Text.literal(WarpMenu.buildConfigString()), true);
                } else {
                    StringBuilder message = new StringBuilder();
                    if (now < cdEnd) {
                        long timeLeft = cdEnd - now;
                        long sec = (timeLeft + 999) / 1000;
                        String color = CooldownSplitter.getColor(timeLeft, WarpMenu.cooldownSec);
                        message.append(color).append(String.format(I18n.translate("action.rtp.cd"), sec));
                    } else if (cdEnd > 0) { hideReady = now + 3000; cdEnd = 0; }
                    
                    if (now < hideReady && cdEnd == 0) message.append("§a").append(I18n.translate("action.rtp.ready"));
                    if (WarpMenu.isOpen) {
                        if (message.length() > 0) message.append(" | ");
                        message.append(WarpMenu.buildString());
                    }
                    if (message.length() > 0) client.player.sendMessage(Text.literal(message.toString()), true);
                }
            }

            if (client.currentScreen != null || WarpMenu.isConfigOpen) return;

            // === 5. ОБРАБОТКА ТЕЛЕПОРТОВ (ОТПРАВКА КОМАНД СЕРВЕРУ) ===
            boolean h = GLFW.glfwGetKey(win, WarpMenu.keyHome) == GLFW.GLFW_PRESS;
            if (h && !prevH) {
                if (now - lH < 400 && client.getNetworkHandler() != null) {
                    client.getNetworkHandler().sendPacket(new CommandExecutionC2SPacket("home o"));
                }
                lH = (now - lH < 400) ? 0 : now;
            } prevH = h;

            // ФИКС РАССИНХРОНИЗАЦИИ: Кулдаун больше не блокирует отправку пакета!
            boolean r = GLFW.glfwGetKey(win, WarpMenu.keyRtp) == GLFW.GLFW_PRESS;
            if (r && !prevR) {
                if (now - lR < 400 && client.getNetworkHandler() != null) {
                    client.getNetworkHandler().sendPacket(new CommandExecutionC2SPacket("rtp"));
                    cdEnd = now + (WarpMenu.cooldownSec * 1000); // Обновляем таймер отображения
                    hideReady = 0;
                }
                lR = (now - lR < 400) ? 0 : now;
            } prevR = r;

            boolean c = GLFW.glfwGetKey(win, WarpMenu.keySpawn) == GLFW.GLFW_PRESS;
            if (c && !prevS) {
                if (now - lS < 400 && client.getNetworkHandler() != null) {
                    client.getNetworkHandler().sendPacket(new CommandExecutionC2SPacket("spawn"));
                }
                lS = (now - lS < 400) ? 0 : now;
            } prevS = c;

            boolean w = GLFW.glfwGetKey(win, WarpMenu.keyWarp) == GLFW.GLFW_PRESS;
            if (w && !prevW) WarpMenu.startTime = now;
            if (w && !WarpMenu.isOpen && (now - WarpMenu.startTime > 350)) WarpMenu.isOpen = true;
            if (!w && prevW) {
                if (!WarpMenu.isOpen) {
                    if (now - lW < 400 && client.getNetworkHandler() != null && !WarpMenu.LIST.isEmpty()) {
                        client.getNetworkHandler().sendPacket(new CommandExecutionC2SPacket("warp " + WarpMenu.LIST.get(0)));
                    }
                    lW = (now - lW < 400) ? 0 : now;
                } else {
                    if (client.getNetworkHandler() != null && !WarpMenu.LIST.isEmpty()) {
                        client.getNetworkHandler().sendPacket(new CommandExecutionC2SPacket("warp " + WarpMenu.LIST.get(WarpMenu.index)));
                    }
                    WarpMenu.isOpen = false;
                }
            } prevW = w;
        });
    }
}
