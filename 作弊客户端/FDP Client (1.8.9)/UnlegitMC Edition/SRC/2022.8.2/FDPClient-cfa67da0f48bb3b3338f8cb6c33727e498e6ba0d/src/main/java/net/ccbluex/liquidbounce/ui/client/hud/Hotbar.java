package net.ccbluex.liquidbounce.ui.client.hud;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.font.FontLoaders;
import net.ccbluex.liquidbounce.utils.render.BlurUtils;
import net.ccbluex.liquidbounce.utils.render.ColorManager;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class Hotbar {
    public static void render(ScaledResolution sr, int itemX) {
        RenderUtils.quickDrawRect(0, sr.getScaledHeight() - 23, sr.getScaledWidth(), sr.getScaledHeight(), new Color(0, 0, 0, 100));
        RenderUtils.quickDrawRect(itemX, sr.getScaledHeight() - 23, itemX + 22, sr.getScaledHeight(), new Color(0, 0, 0, 100));
        BlurUtils.INSTANCE.draw(
                0f,
                (sr.getScaledHeight() - 23),
                sr.getScaledWidth(),
                sr.getScaledHeight(),
                10f
        );
        RenderUtils.quickDrawRect(itemX, sr.getScaledHeight() - 23, itemX + 22, sr.getScaledHeight() - 21, new Color(0, 165, 255));
        RenderUtils.quickDrawRect(0, sr.getScaledHeight() - 23, 2, sr.getScaledHeight(), ColorManager.astolfoRainbow(0, 0, 0));
        FontLoaders.C16.DisplayFonts(FontLoaders.C14, LiquidBounce.CLIENT_NAME + " " + LiquidBounce.CLIENT_VERSION, 7, sr.getScaledHeight() - 18, new Color(255, 255, 255).getRGB());

        FontLoaders.C16.DisplayFonts(FontLoaders.C14, "Language：" + Minecraft.getMinecraft().gameSettings.language, 7, sr.getScaledHeight() - 10, new Color(255, 255, 255).getRGB());
    }
}
