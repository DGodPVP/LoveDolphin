/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.potion.Potion

@ModuleInfo(name = "Regen", description = "Regenerates your health much faster.", category = ModuleCategory.PLAYER)
class Regen : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Vanilla", "OldSpartan", "NewSpartan", "AAC4NoFire"), "Vanilla")
    private val healthValue = IntegerValue("Health", 18, 0, 20)
    private val foodValue = IntegerValue("Food", 18, 0, 20)
    private val speedValue = IntegerValue("Speed", 100, 1, 100)
    private val noAirValue = BoolValue("NoAir", false)
    private val potionEffectValue = BoolValue("PotionEffect", false)

    private var resetTimer = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (resetTimer)
            mc.timer.timerSpeed = 1F
        resetTimer = false

        if ((!noAirValue.get() || mc.thePlayer.onGround) && !mc.thePlayer.capabilities.isCreativeMode &&
            mc.thePlayer.foodStats.foodLevel > foodValue.get() && mc.thePlayer.isEntityAlive && mc.thePlayer.health < healthValue.get()) {
            if(potionEffectValue.get() && !mc.thePlayer.isPotionActive(Potion.regeneration))
                return

            when (modeValue.get().toLowerCase()) {
                "vanilla" -> {
                    repeat(speedValue.get()) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                    }
                }
                
                "aac4nofire" -> {
                    if(mc.thePlayer.isBurning && mc.thePlayer.ticksExisted%10==0) {
                        repeat(35) {
                            mc.netHandler.addToSendQueue(C03PacketPlayer(true))
                        }
                    }
                }
                
                "newspartan" -> {
                    if(mc.thePlayer.ticksExisted%4==0){
                        mc.timer.timerSpeed = 0.95F
                        resetTimer = true
                        repeat(8) {
                            mc.netHandler.addToSendQueue(C03PacketPlayer(true))
                        }
                    }else{
                        mc.netHandler.addToSendQueue(C03PacketPlayer(false))
                        mc.netHandler.addToSendQueue(C03PacketPlayer(true))
                    }
                }

                "oldspartan" -> {
                    if (MovementUtils.isMoving() || !mc.thePlayer.onGround)
                        return

                    repeat(9) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                    }

                    mc.timer.timerSpeed = 0.45F
                    resetTimer = true
                }
            }
        }
    }
    override val tag: String
        get() = modeValue.get()
}
