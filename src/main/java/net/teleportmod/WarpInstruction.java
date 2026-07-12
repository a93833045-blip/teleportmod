package net.teleportmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;

public class WarpInstruction {
    public static void print(MinecraftClient client) {
        if (client.player == null) return;
        
        // Нативно запрашиваем перевод каждой строчки из активного файла локализации игры
        for (int i = 1; i <= 13; i++) {
            String translationKey = "ins.line" + i;
            client.player.sendMessage(Text.literal(I18n.translate(translationKey)), false);
        }
    }
}
