package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.features.module.modules.misc.Teams
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S3FPacketCustomPayload

@ModuleInfo(name = "AutoReport", description = "Auto report players", category = ModuleCategory.PLAYER)
class AutoReport : Module() {
    private val modeValue=ListValue("Mode", arrayOf("Hit","All"),"Hit")
    private val commandValue=TextValue("Command","/reportar %name%")
    private val tipValue=BoolValue("Tip",true)
    private val allDelayValue=IntegerValue("AllDelay",500,0,1000)
    private val blockBooksValue=BoolValue("BlockBooks",false) // 绕过Hypixel /report举报弹出书

    private val reported=mutableListOf<String>()
    private val delayTimer=MSTimer()

    override fun onEnable() {
        reported.clear()
    }

    @EventTarget
    fun onAttack(event: AttackEvent){
        val entity=event.targetEntity ?: return
        if(isTarget(entity)){
            doReport(entity as EntityPlayer)
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(modeValue.get().equals("All",true)&&delayTimer.hasTimePassed(allDelayValue.get().toLong())){
            for(entity in mc.theWorld.loadedEntityList){
                if(isTarget(entity)){
                    if(doReport(entity as EntityPlayer)&&allDelayValue.get()!=0)
                        break
                }
            }
            delayTimer.reset()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        if(blockBooksValue.get()&&event.packet is S3FPacketCustomPayload)
            event.cancelEvent()
    }

    fun doReport(player: EntityPlayer):Boolean{
        val name=player.name

        // pass this if reported
        if(reported.contains(name))
            return false

        reported.add(name)
        mc.thePlayer.sendChatMessage(commandValue.get().replace("%name%",name))
        if(tipValue.get()){
            chat("$name reported!")
        }
        return true
    }

    private fun isTarget(entity: Entity):Boolean{
        if(entity is EntityPlayer){
            if(entity == mc.thePlayer)
                return false

            if (AntiBot.isBot(entity))
                return false

            if (EntityUtils.isFriend(entity))
                return false

            if (entity.isSpectator)
                return false

            val teams = LiquidBounce.moduleManager.getModule(Teams::class.java) as Teams
            return !teams.state || !teams.isInYourTeam(entity)
        }

        return false
    }

    override val tag: String
        get() = modeValue.get()
}