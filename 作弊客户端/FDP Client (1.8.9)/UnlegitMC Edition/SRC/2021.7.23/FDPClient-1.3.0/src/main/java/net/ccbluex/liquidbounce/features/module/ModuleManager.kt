/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.ClientUtils
import org.lwjgl.input.Keyboard
import org.reflections.Reflections
import java.util.*

class ModuleManager : Listenable {

    val modules = TreeSet<Module> { module1, module2 -> module1.name.compareTo(module2.name) }
    private val moduleClassMap = hashMapOf<Class<*>, Module>()

    var pendingBindModule: Module?=null

    init {
        LiquidBounce.eventManager.registerListener(this)
    }

    /**
     * Register all modules
     */
    fun registerModules() {
        ClientUtils.getLogger().info("[ModuleManager] Loading modules...")

        Reflections("${this.javaClass.`package`.name}.modules")
            .getSubTypesOf(Module::class.java).forEach(this::registerModule)

        modules.forEach{ it.onInitialize() }

        ClientUtils.getLogger().info("[ModuleManager] Loaded ${modules.size} modules.")
    }

    /**
     * Register [module]
     */
    fun registerModule(module: Module) {
        modules += module
        moduleClassMap[module.javaClass] = module

        if(module.moduleCommand) {
            generateCommand(module)
        }

        LiquidBounce.eventManager.registerListener(module)
    }

    /**
     * Register [moduleClass]
     */
    private fun registerModule(moduleClass: Class<out Module>) {
        try {
            registerModule(moduleClass.newInstance())
        } catch (e: IllegalAccessException) {
            // this module is a kotlin object
            moduleClass.declaredFields.forEach {
                if(it.name.equals("INSTANCE")){
                    registerModule(it.get(null) as Module)
                    return@forEach
                }
            }
        } catch (e: Throwable){
            ClientUtils.getLogger().error("Failed to load module: ${moduleClass.name} (${e.javaClass.name}: ${e.message})")
        }
    }

    /**
     * Unregister module
     */
    fun unregisterModule(module: Module) {
        modules.remove(module)
        moduleClassMap.remove(module::class.java)
        LiquidBounce.eventManager.unregisterListener(module)
    }

    /**
     * Generate command for [module]
     */
    internal fun generateCommand(module: Module) {
        val values = module.values

        if (values.isEmpty())
            return

        LiquidBounce.commandManager.registerCommand(ModuleCommand(module, values))
    }

    /**
     * Get module by [moduleClass]
     */
    fun <T : Module> getModule(moduleClass: Class<T>): T {
        return moduleClassMap[moduleClass] as T
    }

    operator fun <T : Module> get(clazz: Class<T>) = getModule(clazz)

    /**
     * Get module by [moduleName]
     */
    fun getModule(moduleName: String?) = modules.find { it.name.equals(moduleName, ignoreCase = true) }

    fun getKeyBind(key: Int) = modules.filter { it.keyBind == key }

    /**
     * Module related events
     */

    /**
     * Handle incoming key presses
     */
    @EventTarget
    private fun onKey(event: KeyEvent){
        if(pendingBindModule==null) {
            modules.filter { it.keyBind == event.key }.forEach { it.toggle() }
        }else{
            pendingBindModule!!.keyBind=event.key
            ClientUtils.displayAlert("Bound module §a§l${pendingBindModule!!.name}§3 to key §a§l${Keyboard.getKeyName(event.key)}§3.")
            LiquidBounce.hud.addNotification(Notification("KeyBind","Bound ${pendingBindModule!!.name} to ${Keyboard.getKeyName(event.key)}.", NotifyType.INFO))
            pendingBindModule=null
        }
    }

    override fun handleEvents() = true
}
