package net.ccbluex.liquidbounce.features.module.modules.misc

import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemSkull
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemTool
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import org.apache.commons.io.IOUtils
import java.util.*

/***
 * @author liulihaocai
 * FILHO DA PUTA CLIENT
 */
@ModuleInfo(name = "AuthBypass", description = "Bypass auth when join server.", category = ModuleCategory.MISC)
class AuthBypass : Module(){
    private val delayValue= IntegerValue("Delay",1500,100,5000)

    private var skull:String?=null
    private var type="none"
    private val packets=ArrayList<Packet<INetHandlerPlayServer>>()
    private val clickedSlot=ArrayList<Int>()
    private val timer=MSTimer()
    private val jsonParser=JsonParser()

    private val brLangMap = HashMap<String, String>()

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(packets.isNotEmpty()&&timer.hasTimePassed(delayValue.get().toLong())){
            for(packet in packets){
                mc.netHandler.addToSendQueue(packet)
            }
            packets.clear()
            LiquidBounce.hud.addNotification(Notification(name,"Authenticate bypassed.", NotifyType.INFO))
        }
    }

    override fun onEnable() {
        skull=null
        type="none"
        packets.clear()
        clickedSlot.clear()

        //load locale async
        Thread {
            val localeJson=JsonParser().parse(IOUtils.toString(AuthBypass::class.java.classLoader.getResourceAsStream("br_items.json"),"utf-8")).asJsonObject

            brLangMap.clear()
            for((key,element) in localeJson.entrySet()){
                brLangMap["item.$key"] = element.asString.toLowerCase()
            }
        }.start()
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        val packet=event.packet
        if(packet is S2FPacketSetSlot){
            val slot=packet.func_149173_d()
            val windowId=packet.func_149175_c()
            val item=packet.func_149174_e()
            if(windowId==0 || item==null || type=="none" || clickedSlot.contains(slot)){
                return
            }
            val itemName=item.unlocalizedName

            when(type.toLowerCase()){
                "skull" -> {
                    if(itemName.contains("item.skull.char",ignoreCase = true)){
                        val nbt=item.tagCompound ?: return
                        // val uuid=nbt.get<CompoundTag>("SkullOwner").get<CompoundTag>("Properties").get<ListTag>("textures").get<CompoundTag>(0).get<StringTag>("Value").value
                        val data=process(nbt.getCompoundTag("SkullOwner").getCompoundTag("Properties")
                            .getTagList("textures",NBTTagCompound.NBT_TYPES.indexOf("COMPOUND"))
                            .getCompoundTagAt(0).getString("Value"))
                        if(skull==null){
                            skull=data
                        }else if(skull!=data) {
                            skull = null
                            timer.reset()
                            click(windowId,slot,item)
                        }
                    }
                }

                // special rules lol
                "enchada" -> { // select all
                    click(windowId,slot,item)
                }

                "cabeça" -> { // skulls
                    if(item.item is ItemSkull){
                        click(windowId,slot,item)
                    }
                }

                "ferramenta" -> { // tools
                    if(item.item is ItemTool){
                        click(windowId,slot,item)
                    }
                }

                "comida" -> { // foods
                    if(item.item is ItemFood){
                        click(windowId,slot,item)
                    }
                }

                // the new item check in redesky
                else -> {
                    if(getItemLocalName(item).contains(type)){
                        click(windowId,slot,item)
                    }
                }
            }
        }
        //silent auth xd
        if(packet is S2DPacketOpenWindow){
            val windowName=packet.windowTitle.unformattedText
            if(packet.slotCount==27 && packet.guiId.contains("container",ignoreCase = true)
                && windowName.startsWith("Clique",ignoreCase = true)){
                type = when{
                    windowName.contains("bloco",ignoreCase = true) -> "skull"
                    else -> {
                        val splited=windowName.split(" ")
                        var str=splited[splited.size-1].replace(".","").toLowerCase()
                        if(str.endsWith("s")){
                            str=str.substring(0,str.length-1)
                        }
                        str
                    }
                }
                packets.clear()
                clickedSlot.clear()
                event.cancelEvent()
            }else{
                type="none"
            }
        }
    }

    private fun click(windowId: Int,slot: Int,item: ItemStack){
        clickedSlot.add(slot)
        packets.add(C0EPacketClickWindow(windowId,slot,0,0,item, RandomUtils.nextInt(114,514).toShort()))
    }

    private fun getItemLocalName(item: ItemStack):String {
        return brLangMap[item.unlocalizedName] ?: "null"
    }

    private fun process(data: String):String{
        val jsonObject=jsonParser.parse(String(Base64.getDecoder().decode(data))).asJsonObject
        return jsonObject
            .getAsJsonObject("textures")
            .getAsJsonObject("SKIN")
            .get("url").asString
    }
}