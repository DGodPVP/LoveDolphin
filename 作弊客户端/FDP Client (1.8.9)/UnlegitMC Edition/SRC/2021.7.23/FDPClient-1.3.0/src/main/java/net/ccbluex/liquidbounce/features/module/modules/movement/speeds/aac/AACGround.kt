/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

class AACGround : SpeedMode("AACGround") {
    override fun onUpdate() {
        if (!MovementUtils.isMoving()) return

        mc.timer.timerSpeed =
            (LiquidBounce.moduleManager.getModule(Speed::class.java) as Speed?)!!.aacGroundTimerValue.get()

        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }
}