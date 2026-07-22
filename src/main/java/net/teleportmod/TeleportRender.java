package net.teleportmod;

import org.lwjgl.glfw.GLFW;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TeleportRender {

    /**
     * Рендерит Portal-Style оверлей выбора варпов по JSON-ключу action.warp.
     * Автоматически скармливает игре плейсхолдеры %s и %d на уровне движка.
     */
    public static Text getWarpMenuText() {
        if (TeleportData.warpList.isEmpty()) {
            return Text.translatable("menu.empty").formatted(Formatting.RED);
        }
        if (TeleportData.warpIndex >= TeleportData.warpList.size()) TeleportData.warpIndex = 0;
        
        // Нативная передача аргументов: (Имя варпа, Текущий номер, Всего варпов)
        return Text.translatable(
            "action.warp", 
            TeleportData.warpList.get(TeleportData.warpIndex), 
            Integer.valueOf(TeleportData.warpIndex + 1), 
            Integer.valueOf(TeleportData.warpList.size())
        );
    }

    /**
     * Рендерит оверлей быстрого выбора игроков из ТАБа.
     */
    public static Text getPlayerMenuText() {
        if (TeleportData.playersTab.isEmpty() || TeleportData.playersTab.get(0).equals("No_Players")) {
            return Text.translatable("menu.empty").formatted(Formatting.RED);
        }
        if (TeleportData.playerIndex >= TeleportData.playersTab.size()) TeleportData.playerIndex = 0;
        
        MutableText widget = Text.literal("[ВЫБОР ИГРОКА] ").formatted(Formatting.DARK_PURPLE, Formatting.BOLD);
        widget.append(Text.literal("(Вверх/Вниз) ").formatted(Formatting.YELLOW));
        widget.append(Text.literal("> ").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        widget.append(Text.literal(TeleportData.playersTab.get(TeleportData.playerIndex)).formatted(Formatting.GREEN, Formatting.BOLD));
        widget.append(Text.literal(" < ").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        
        String counter = "(" + (TeleportData.playerIndex + 1) + "/" + TeleportData.playersTab.size() + ")";
        return widget.append(Text.literal(counter).formatted(Formatting.DARK_GRAY));
    }

    /**
     * Рендерит трёхцветный светофор подсказки состояния /tpa или /tp запроса.
     * Загорается строго на 3 секунды при одиночном клике или после отправки.
     */
    public static Text getTpaStatusText(MinecraftClient client, long now) {
        if (now > TeleportData.statusShowUntil) return Text.empty();
        
        if (TeleportData.selectedPlayer == null || TeleportData.selectedPlayer.isEmpty()) {
            if (TeleportData.isPlayerMenuOpen) {
                return Text.literal("Игрок не выбран").formatted(Formatting.RED);
            }
            return Text.empty();
        }

        Formatting color = Formatting.RED; // Офлайн (По умолчанию красным)
        
        if (now - TeleportData.tpaRequestTimestamp < 15000) {
            color = Formatting.YELLOW; // Запрос только что отправлен (Жёлтый приоритет)
        } else if (client.getNetworkHandler() != null) {
            for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
                String name = TeleportData.getCleanPlayerName(entry);
                if (name.equalsIgnoreCase(TeleportData.selectedPlayer)) { 
                    color = Formatting.GREEN; // Цель найдена в онлайне ТАБа (Зелёный)
                    break; 
                } 
            }
        }
        
        String cmdPrefix = TeleportData.isTpMode ? "/tp " : "/tpa ";
        return Text.literal(cmdPrefix + TeleportData.selectedPlayer).formatted(color);
    }
    /**
     * Рендерит градиентную шкалу отката RTP по JSON-ключам.
     */
    public static Text getRtpCooldownText(long now) {
        MutableText component = Text.empty();
        if (now < TeleportData.cooldownEndTimestamp) {
            long remainingMs = TeleportData.cooldownEndTimestamp - now;
            long secondsLeft = (remainingMs + 999) / 1000;
            
            // Запрашиваем нативный перевод отката, передавая оставшиеся секунды в %d
            Text cdText = Text.translatable("action.rtp.cd", secondsLeft);
            double percentage = (double) remainingMs / (TeleportData.cooldownDurationSec * 1000);
            
            // Алгоритм мягкой смены цвета в зависимости от остатка времени
            Formatting color = Formatting.GREEN;
            if (percentage > 0.75) color = Formatting.DARK_RED;
            else if (percentage > 0.50) color = Formatting.GOLD;
            else if (percentage > 0.25) color = Formatting.YELLOW;
            
            component.append(cdText.copy().formatted(color));
        } else if (TeleportData.cooldownEndTimestamp > 0) {
            // Кулдаун иссяк — запускаем 3-секундный таймер надписи "Телепорт готов!"
            TeleportData.hideReadyNotificationTimestamp = now + 3000;
            TeleportData.cooldownEndTimestamp = 0;
        }

        if (now < TeleportData.hideReadyNotificationTimestamp && TeleportData.cooldownEndTimestamp == 0) {
            component.append(Text.translatable("action.rtp.ready").formatted(Formatting.GREEN));
        }
        return component;
    }

    /**
     * Полностью переписанный на MutableText и translatable-компоненты менеджер настроек.
     * На ходу адаптирует интерфейс под русскую и английскую раскладки.
     */
    public static Text buildConfigMenuText() {
        MutableText menu = Text.empty();
        
        // Подгружаем заголовок "[НАСТРОЙКА] " или "[SETTINGS] "
        menu.append(Text.translatable("menu.config").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        menu.append(" "); 
        
        if (TeleportData.isWaitingForBind) {
            return menu.append(Text.translatable("menu.waitkey").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        }
        
        MutableText rowText = Text.empty().formatted(Formatting.YELLOW);
        rowText.append("§e> "); // Стрелочка активной строки выбора
        
        switch (TeleportData.configSelectRow) {
            case 0: rowText.append(Text.translatable("menu.home")).append(Text.literal(getKeyName(TeleportData.tempKeyHome)).formatted(Formatting.WHITE)); break;
            case 1: rowText.append(Text.translatable("menu.rtp")).append(Text.literal(getKeyName(TeleportData.tempKeyRtp)).formatted(Formatting.WHITE)); break;
            case 2: rowText.append(Text.translatable("menu.spawn")).append(Text.literal(getKeyName(TeleportData.tempKeySpawn)).formatted(Formatting.WHITE)); break;
            case 3: rowText.append(Text.translatable("menu.warp")).append(Text.literal(getKeyName(TeleportData.tempKeyWarp)).formatted(Formatting.WHITE)); break;
            case 4: rowText.append(Text.translatable("menu.tpa")).append(Text.literal(getKeyName(TeleportData.tempKeyTpa)).formatted(Formatting.WHITE)); break;
            case 5: 
                rowText.append(Text.translatable("menu.cooldown"))
                       .append(Text.literal(Long.toString(TeleportData.tempCooldownDurationSec)).formatted(Formatting.WHITE))
                       .append(Text.translatable("menu.sec")); 
                break;
            case 6: 
                String modeName = TeleportData.tempTpMode ? "/tp (Admin)" : "/tpa (Request)";
                rowText.append(Text.translatable("menu.mode")).append(Text.literal(modeName).formatted(Formatting.WHITE)); 
                break;
            case 7: rowText.append(Text.translatable("menu.add")); break;
            case 8:
                rowText.append(Text.translatable("menu.delete"));
                if (TeleportData.tempWarpList.isEmpty()) {
                    rowText.append(Text.translatable("menu.empty"));
                } else {
                    if (TeleportData.deleteWarpIndex >= TeleportData.tempWarpList.size()) TeleportData.deleteWarpIndex = TeleportData.tempWarpList.size() - 1;
                    rowText.append(Text.literal(TeleportData.tempWarpList.get(TeleportData.deleteWarpIndex)).formatted(Formatting.WHITE));
                }
                rowText.append(" ] <");
                break;
            case 9: rowText.append(Text.translatable("menu.addplayer")); break;
            case 10: rowText.append(Text.translatable("menu.clearplayer")); break;
            case 11: rowText.append(Text.translatable("menu.instruction")); break;
            case 12: rowText.append(Text.translatable("menu.save")); break;
            case 13: rowText.append(Text.translatable("menu.cancel")); break;
        }
        
        if (TeleportData.configSelectRow != 8) {
            rowText.append(" <");
        }
        
        menu.append(rowText);
        
        // Порядковый счётчик строк "(1/14)" в самом конце
        String counter = " §8(" + (TeleportData.configSelectRow + 1) + "/14)";
        menu.append(Text.literal(counter).formatted(Formatting.DARK_GRAY));
        
        return menu;
    }

    @Deprecated
    public static String buildConfigString() {
        return "";
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

    /**
     * Построчно выгружает монументальную цветную инструкцию из 18 строк в чат игрока.
     */
    public static void printInstruction(MinecraftClient client) {
        if (client.player == null) return;
        for (int i = 1; i <= 18; i++) {
            Text msg = Text.translatable("ins.line" + i);
            if (!msg.getString().equals("ins.line" + i)) { 
                client.player.sendMessage(msg, false); 
            }
        }
    }
}
