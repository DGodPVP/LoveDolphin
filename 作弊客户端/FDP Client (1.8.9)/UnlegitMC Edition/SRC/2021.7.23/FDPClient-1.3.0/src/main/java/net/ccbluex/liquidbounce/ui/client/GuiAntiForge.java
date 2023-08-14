/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.special.AntiForge;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiAntiForge extends GuiScreen {

    private final GuiScreen prevGui;

    private GuiButton enabledButton;
    private GuiButton fmlButton;
    private GuiButton proxyButton;
    private GuiButton payloadButton;

    public GuiAntiForge(final GuiScreen prevGui) {
        this.prevGui = prevGui;
    }

    @Override
    public void initGui() {
        buttonList.add(enabledButton = new GuiButton(1, width / 2 - 100, height / 4 + 35, "Button"));
        buttonList.add(fmlButton = new GuiButton(2, width / 2 - 100, height / 4 + 50 + 25, "Button"));
        buttonList.add(proxyButton = new GuiButton(3, width / 2 - 100, height / 4 + 50 + 25 * 2, "Button"));
        buttonList.add(payloadButton = new GuiButton(4, width / 2 - 100, height / 4 + 50 + 25 * 3, "Button"));

        buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 55 + 25 * 4 + 5, "Back"));

        updateButtonStat();
    }

    private void updateButtonStat(){
        enabledButton.displayString="Enabled: "+(AntiForge.enabled?"§aOn":"§cOff");
        fmlButton.displayString="FML Brand: "+(AntiForge.blockFML?"§aOn":"§cOff");
        proxyButton.displayString="FML Proxy Packets: "+(AntiForge.blockProxyPacket?"§aOn":"§cOff");
        payloadButton.displayString="Payload Packets: "+(AntiForge.blockPayloadPackets?"§aOn":"§cOff");
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch(button.id) {
            case 1:
                AntiForge.enabled = !AntiForge.enabled;
                break;
            case 2:
                AntiForge.blockFML = !AntiForge.blockFML;
                break;
            case 3:
                AntiForge.blockProxyPacket = !AntiForge.blockProxyPacket;
                break;
            case 4:
                AntiForge.blockPayloadPackets = !AntiForge.blockPayloadPackets;
                break;
            case 0:
                mc.displayGuiScreen(prevGui);
                break;
        }

        updateButtonStat();
        if(button.id!=0) {
            LiquidBounce.configManager.save(true,false);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground(0);
        Fonts.fontBold40.drawCenteredString("AntiForge", (int) (width / 2F), (int) (height / 8F + 5F), 4673984, true);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if(Keyboard.KEY_ESCAPE == keyCode) {
            mc.displayGuiScreen(prevGui);
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }
}