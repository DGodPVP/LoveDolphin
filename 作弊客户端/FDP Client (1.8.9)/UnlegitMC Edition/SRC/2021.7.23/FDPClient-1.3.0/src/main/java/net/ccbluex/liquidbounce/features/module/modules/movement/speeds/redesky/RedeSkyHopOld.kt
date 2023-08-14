package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.redesky

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class RedeSkyHopOld: SpeedMode("RedeSkyHopOld") {
    override fun onMotion() {
        if(MovementUtils.isMoving()){
            mc.thePlayer.isSprinting = true
            mc.timer.timerSpeed = 1F
            if (mc.thePlayer.onGround) {
                val speedModule=LiquidBounce.moduleManager.getModule(Speed::class.java) as Speed

                mc.thePlayer.motionY=speedModule.redeSkyHeight.get().toDouble()
                MovementUtils.strafe(MovementUtils.getSpeed()
                        +speedModule.redeSkyHopGSpeed.get())
                mc.timer.timerSpeed = speedModule.redeSkyHopTimer.get()
            }
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1F
    }
}