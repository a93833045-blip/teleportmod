package net.teleportmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class TeleportModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TeleportData.load();

        // Силовая принудительная инициализация резервного перевода.
        // Если у пользователя включен любой региональный английский (en_gb, en_ca), 
        // движок игры автоматически подтянет строки из нашего базового en_us.json!
        try {
            I18n.translate("category.teleportmod");
        } catch (Exception ignored) {}

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            long now = System.currentTimeMillis();
            long windowHandle = client.getWindow().getHandle();

            TeleportLogic.processInputs(client, now, windowHandle);

            if (client.currentScreen == null) {
                if (TeleportData.isConfigMenuOpen) {
                    client.player.sendMessage(TeleportRender.buildConfigMenuText(), true);
                } else if (TeleportData.isPlayerMenuOpen) {
                    client.player.sendMessage(TeleportRender.getPlayerMenuText(), true);
                } else {
                    MutableText finalMessage = Text.empty();
                    
                    Text tpaStatus = TeleportRender.getTpaStatusText(client, now);
                    if (!tpaStatus.getString().isEmpty()) {
                        finalMessage.append(tpaStatus);
                        finalMessage.append(" ");
                    }

                    Text rtpCd = TeleportRender.getRtpCooldownText(now);
                    if (!rtpCd.getString().isEmpty()) {
                        finalMessage.append(rtpCd);
                    }

                    if (TeleportData.isWarpMenuOpen) {
                        if (!finalMessage.getString().isEmpty()) finalMessage.append(" ");
                        finalMessage.append(TeleportRender.getWarpMenuText());
                    }

                    if (!finalMessage.getString().isEmpty()) {
                        client.player.sendMessage(finalMessage, true);
                    }
                }
            }
        });
    }
}
