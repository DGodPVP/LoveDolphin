/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.module.modules.misc.AutoAbuse
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.font.Fonts

class ReloadCommand : Command("reload", arrayOf("configreload")) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        chat("Reloading...")
        chat("§c§lReloading commands...")
        LiquidBounce.commandManager = CommandManager()
        LiquidBounce.commandManager.registerCommands()
        LiquidBounce.isStarting = true
        LiquidBounce.isLoadingConfig = true
        LiquidBounce.scriptManager.disableScripts()
        LiquidBounce.scriptManager.unloadScripts()
        for(module in LiquidBounce.moduleManager.modules)
            LiquidBounce.moduleManager.generateCommand(module)
        chat("§c§lReloading scripts...")
        LiquidBounce.scriptManager.loadScripts()
        LiquidBounce.scriptManager.enableScripts()
        chat("§c§lReloading fonts...")
        Fonts.loadFonts()
        chat("§c§lReloading modules...")
        LiquidBounce.configManager.load(LiquidBounce.configManager.nowConfig,false)
        AutoAbuse.loadFile()
        chat("§c§lReloading accounts...")
        LiquidBounce.fileManager.loadConfig(LiquidBounce.fileManager.accountsConfig)
        chat("§c§lReloading friends...")
        LiquidBounce.fileManager.loadConfig(LiquidBounce.fileManager.friendsConfig)
        chat("§c§lReloading xray...")
        LiquidBounce.fileManager.loadConfig(LiquidBounce.fileManager.xrayConfig)
        chat("§c§lReloading HUD...")
        LiquidBounce.fileManager.loadConfig(LiquidBounce.fileManager.hudConfig)
        chat("§c§lReloading ClickGUI...")
        LiquidBounce.clickGui = ClickGui()
        LiquidBounce.fileManager.loadConfig(LiquidBounce.fileManager.clickGuiConfig)
        chat("Reloaded.")
        LiquidBounce.isStarting = false
        LiquidBounce.isLoadingConfig = false
    }
}
