/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.event.ClientShutdownEvent
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.special.AntiForge
import net.ccbluex.liquidbounce.features.special.CombatManager
import net.ccbluex.liquidbounce.features.special.PacketFixer
import net.ccbluex.liquidbounce.features.special.ServerSpoof
import net.ccbluex.liquidbounce.features.special.macro.MacroManager
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.file.MetricsLite
import net.ccbluex.liquidbounce.file.config.ConfigManager
import net.ccbluex.liquidbounce.script.ScriptManager
import net.ccbluex.liquidbounce.script.remapper.Remapper
import net.ccbluex.liquidbounce.ui.click.ClickGuiManager
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.keybind.KeyBindManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.other.TipSoundManager
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.misc.HttpUtils
import net.ccbluex.liquidbounce.utils.misc.betterfps.BetterFPSCore
import net.minecraft.util.ResourceLocation
import org.apache.commons.io.IOUtils

object LiquidBounce {

    // Client information
    const val CLIENT_NAME = "FDPClient"
    const val COLORED_NAME = "§c§lFDP§6§lClient"
    @JvmField
    val CLIENT_VERSION: String
    const val CLIENT_CREATOR = "CCBlueX & UnlegitMC"
    const val MINECRAFT_VERSION = "1.8.9"

    val enableUpdateAlert: Boolean
    var isStarting = true
    var isLoadingConfig = true

    // Managers
    lateinit var moduleManager: ModuleManager
    lateinit var commandManager: CommandManager
    lateinit var eventManager: EventManager
    lateinit var fileManager: FileManager
    lateinit var scriptManager: ScriptManager
    lateinit var tipSoundManager: TipSoundManager
    lateinit var combatManager: CombatManager
    lateinit var macroManager: MacroManager
    lateinit var configManager: ConfigManager

    // HUD & ClickGUI & KeybindManager
    lateinit var hud: HUD
    lateinit var clickGui: ClickGui
    lateinit var keyBindManager: KeyBindManager
    lateinit var clickGuiManager: ClickGuiManager

    lateinit var metricsLite: MetricsLite

    // Update information
    var latestVersion = ""
    lateinit var updatelog: JsonArray
    var website = "null"
    var updateMessage="Press \"Download\" button to download the latest version!"
    var displayedUpdateScreen=false

    // Menu Background
    var background: ResourceLocation? = null

    // Better FPS
    lateinit var betterFPSCore: BetterFPSCore

    init {
        val commitId=LiquidBounce::class.java.classLoader.getResourceAsStream("FDP_GIT_COMMIT_ID")
        CLIENT_VERSION=if (commitId==null){
            "v1.3.0"
        }else{
            val str=IOUtils.toString(commitId,"utf-8").replace("\n","")
            "git-"+(str.substring(0, 7.coerceAtMost(str.length)))
        }
        enableUpdateAlert=commitId==null
    }

    /***
     * do things that need long time async
     */
    fun initClient(){
        betterFPSCore = BetterFPSCore()
        isStarting = true

        updatelog=JsonArray()

        // check update
        Thread {
            val get = HttpUtils.get("https://fdp.liulihaocai.workers.dev/")

            val jsonObj = JsonParser()
                .parse(get).asJsonObject

            latestVersion = jsonObj.get("version").asString
            website = jsonObj.get("website").asString
            updatelog = jsonObj.getAsJsonArray("updatelog")
            if(jsonObj.has("updatemsg"))
                updateMessage=jsonObj.get("updatemsg").asString

            if(latestVersion== CLIENT_VERSION || !enableUpdateAlert)
                latestVersion = ""
        }.start()
    }

    /***
     * Execute if client will be started
     */
    fun startClient() {
        ClientUtils.logInfo("Starting $CLIENT_NAME $CLIENT_VERSION, by $CLIENT_CREATOR")
        val startTime=System.currentTimeMillis()

        // Create file manager
        fileManager = FileManager()
        configManager = ConfigManager()

        // Crate event manager
        eventManager = EventManager()

        // Register listeners
        eventManager.registerListener(RotationUtils())
        eventManager.registerListener(AntiForge())
        eventManager.registerListener(InventoryUtils())
        eventManager.registerListener(ServerSpoof())

        // Create command manager
        commandManager = CommandManager()

        // ClickGui Manager
        clickGuiManager = ClickGuiManager()

        macroManager = MacroManager()
        eventManager.registerListener(macroManager)

        // Load client fonts
        Fonts.loadFonts()

        // Setup module manager and register modules
        moduleManager = ModuleManager()
        moduleManager.registerModules()

        // Remapper
        try {
            Remapper.loadSrg()

            // ScriptManager
            scriptManager = ScriptManager()
            scriptManager.loadScripts()
            scriptManager.enableScripts()
        } catch (throwable: Throwable) {
            ClientUtils.getLogger().error("Failed to load scripts.", throwable)
        }

        // Register commands
        commandManager.registerCommands()

        tipSoundManager = TipSoundManager()

        // Load configs
        configManager.loadLegacySupport()
        configManager.loadConfigSet()

        // ClickGUI
        clickGui = ClickGui()
        fileManager.loadConfigs(fileManager.clickGuiConfig, fileManager.accountsConfig, fileManager.friendsConfig, fileManager.xrayConfig)

        // Init All after load modules
        clickGuiManager.initAll()

        // KeyBindManager
        keyBindManager=KeyBindManager()

        // Set HUD
        hud = HUD.createDefault()
        fileManager.loadConfig(fileManager.hudConfig)

        // Disable optifine fastrender
        ClientUtils.disableFastRender()

        // bstats.org user count display
        metricsLite=MetricsLite(11076)

        combatManager=CombatManager()
        eventManager.registerListener(combatManager)

        eventManager.registerListener(PacketFixer())

        // Set is starting status
        isStarting = false
        isLoadingConfig=false

        ClientUtils.logInfo("$CLIENT_NAME $CLIENT_VERSION started in ${(System.currentTimeMillis()-startTime)}ms!")
    }

    /**
     * Execute if client will be stopped
     */
    fun stopClient() {
        // Call client shutdown
        eventManager.callEvent(ClientShutdownEvent())

        // Save all available configs
        configManager.save(true,true)
        fileManager.saveAllConfigs()
    }

}
