/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import org.lwjgl.input.Keyboard

@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ModuleInfo(val name: String, val description: String, val category: ModuleCategory,
                            val keyBind: Int = Keyboard.CHAR_NONE, val canEnable: Boolean = true, val array: Boolean = true,
                            val autoDisable: EnumAutoDisableType = EnumAutoDisableType.NONE, val moduleCommand: Boolean = true,
                            val defaultOn: Boolean = false)

enum class EnumAutoDisableType {
    NONE,
    RESPAWN,
    FLAG,
    GAME_END
}