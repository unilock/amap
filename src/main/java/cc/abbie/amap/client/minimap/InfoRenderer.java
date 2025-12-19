package cc.abbie.amap.client.minimap;

import cc.abbie.amap.client.minimap.config.MinimapConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import cc.abbie.amap.client.AMapKeybinds;

public class InfoRenderer {
    public static int getHeight() {
        int lineHeight = Minecraft.getInstance().font.lineHeight;
        int height = 0;

        if (MinimapConfig.INSTANCE.minimap.showCoordinates.value() != MinimapConfig.Minimap.CoordinatesType.DISABLED) {
            height += 2 * lineHeight;
        }
        if (MinimapConfig.INSTANCE.minimap.showBiome.value()) {
            height += 2 * lineHeight;
        }
        if (MinimapConfig.INSTANCE.minimap.showMenuKey.value()) {
            height += lineHeight;
        }

        return height;
    }

    public static void renderInfo(GuiGraphics gui, int centreX, int leftX, int rightX, int y, float partialTick) {
        Minecraft client = Minecraft.getInstance();
        Font font = client.font;

        final int lineHeight = font.lineHeight;
        int yOffset = y;

        if (MinimapConfig.INSTANCE.minimap.showCoordinates.value() != MinimapConfig.Minimap.CoordinatesType.DISABLED) {
            LocalPlayer player = client.player;
            if (player == null) return;

            Vec3 eyePos = player.getEyePosition(partialTick);
            int footPos = (int) player.getPosition(partialTick).y;

            if (MinimapConfig.INSTANCE.minimap.showCoordinates.value() == MinimapConfig.Minimap.CoordinatesType.TYPE1) {
                gui.drawCenteredString(font, String.format("%+d, %+d", Mth.floor(eyePos.x), Mth.floor(eyePos.z)), centreX, yOffset, -1);
                yOffset += lineHeight;
                gui.drawCenteredString(font, String.format("%d", footPos), centreX, yOffset, -1);
                yOffset += lineHeight;
            } else {
                gui.drawCenteredString(font, String.format("%+.2f, %+.2f", eyePos.x, eyePos.z), centreX, yOffset, -1);
                yOffset += lineHeight;
                gui.drawCenteredString(font, String.format("%.2f (%d)", eyePos.y, footPos), centreX, yOffset, -1);
                yOffset += lineHeight;
            }
        }

        if (MinimapConfig.INSTANCE.minimap.showBiome.value()) {
            ClientLevel level = client.level;
            LocalPlayer player = client.player;
            if (level == null || player == null) return;

            ResourceLocation biomeId = level.getBiome(player.blockPosition()).unwrapKey().orElseThrow().location();
            Component biomeName = Component.translatable("biome.%s.%s".formatted(biomeId.getNamespace(), biomeId.getPath()));
            for (FormattedCharSequence infoLine : font.split(biomeName, rightX - leftX)) {
				gui.drawCenteredString(font, infoLine, centreX, yOffset, -1);
				yOffset += lineHeight;
			}
        }

        if (MinimapConfig.INSTANCE.minimap.showMenuKey.value()) {
            Component infoLine = Component.translatable("info.amap.menuKey", AMapKeybinds.OPEN_MINIMAP_CONFIG.getTranslatedKeyMessage());
            int textWidth = font.width(infoLine.getVisualOrderText());
            gui.drawString(font, infoLine, rightX - textWidth, yOffset, -1);
            yOffset += lineHeight;
        }
    }
}
