package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.FontValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat
import kotlin.math.roundToInt

@ElementInfo(name = "Targets", single = true)
class Targets : Element(-46.0,-40.0,1F,Side(Side.Horizontal.MIDDLE,Side.Vertical.MIDDLE)) {
    private val modeValue = ListValue("Mode", arrayOf("Novoline","Astolfo","Liquid","Flux","Fancy"), "Novoline")
    private val switchModeValue = ListValue("SwitchMode", arrayOf("Slide","Zoom"), "Slide")
    private val animSpeedValue = IntegerValue("AnimSpeed",10,5,20)
    private val switchAnimSpeedValue = IntegerValue("SwitchAnimSpeed",20,5,40)
    private val fontValue = FontValue("Font", Fonts.font40)

    private var prevTarget:EntityLivingBase?=null
    private var lastHealth=20F
    private var lastChangeHealth=20F
    private var changeTime=System.currentTimeMillis()
    private var displayPercent=0.0
    private var lastUpdate = System.currentTimeMillis()
    private val decimalFormat = DecimalFormat("0.0")

    override fun drawElement(partialTicks: Float): Border? {
        var target=LiquidBounce.combatManager.target
        val time=System.currentTimeMillis()
        val pct = (time - lastUpdate) / (switchAnimSpeedValue.get()*50.0)
        lastUpdate=System.currentTimeMillis()

        if (mc.currentScreen is GuiHudDesigner) {
            target=mc.thePlayer
        }
        if (target != null) {
            prevTarget = target
        }
        prevTarget ?: return getTBorder()

        if (target!=null) {
            if (displayPercent < 1) {
                displayPercent += pct
            }
            if (displayPercent > 1) {
                displayPercent = 1.0
            }
        } else {
            if (displayPercent > 0) {
                displayPercent -= pct
            }
            if (displayPercent < 0) {
                displayPercent = 0.0
                prevTarget=null
                return getTBorder()
            }
        }

        if(prevTarget!!.health!=lastHealth){
            lastChangeHealth=lastHealth
            lastHealth=prevTarget!!.health
            changeTime=time
        }
        val nowAnimHP=if((time-(animSpeedValue.get()*50))<changeTime){
            prevTarget!!.health+(lastChangeHealth-prevTarget!!.health)*(1-((time-changeTime)/(animSpeedValue.get()*50F)))
        }else{
            prevTarget!!.health
        }

        when(switchModeValue.get().toLowerCase()){
            "zoom" -> {
                val border=getTBorder() ?: return null
                GL11.glScaled(displayPercent,displayPercent,displayPercent)
                GL11.glTranslatef(((border.x2 * 0.5f * (1-displayPercent))/displayPercent).toFloat(), ((border.y2 * 0.5f * (1-displayPercent))/displayPercent).toFloat(), 0f)
            }
            "slide" -> {
                val percent=EaseUtils.easeInQuint(1-displayPercent)
                val xAxis=ScaledResolution(mc).scaledWidth-renderX
                GL11.glTranslated(xAxis*percent,0.0,0.0)
            }
        }

        when(modeValue.get().toLowerCase()){
            "novoline" -> drawNovo(prevTarget!!,nowAnimHP)
            "astolfo" -> drawAstolfo(prevTarget!!,nowAnimHP)
            "liquid" -> drawLiquid(prevTarget!!,nowAnimHP)
            "flux" -> drawFlux(prevTarget!!,nowAnimHP)
            "fancy" -> drawFancy(prevTarget!!,nowAnimHP)
        }

        return getTBorder()
    }

    private fun drawAstolfo(target: EntityLivingBase, nowAnimHP: Float){
        val font=fontValue.get()
        val color=RenderUtils.skyRainbow(1,1F,0.9F,5.0)
        val hpPct=nowAnimHP/target.maxHealth

        RenderUtils.drawRect(0F,0F, 140F, 60F, Color(0,0,0,110).rgb)

        // health rect
        RenderUtils.drawRect(3F, 55F, 137F, 58F,ColorUtils.reAlpha(color,100).rgb)
        RenderUtils.drawRect(3F,55F,3+(hpPct*134F),58F,color.rgb)
        RenderUtils.drawEntityOnScreen(18,46,20,target)

        font.drawStringWithShadow(target.name, 37F, 6F, -1)
        GL11.glPushMatrix()
        GL11.glScalef(2F,2F,2F)
        font.drawString("${target.health.roundToInt()} ❤", 19,9, color.rgb)
        GL11.glPopMatrix()
    }

    private fun drawNovo(target: EntityLivingBase, nowAnimHP: Float){
        val font=fontValue.get()
        val color=ColorUtils.healthColor(target.health,target.maxHealth)
        val darkColor=ColorUtils.darker(color,0.6F)
        val hpPos=33F+((target.health / target.maxHealth * 10000).roundToInt() / 100)

        RenderUtils.drawRect(0F,0F, 140F, 40F, Color(40,40,40).rgb)
        font.drawString(target.name, 33, 5, Color.WHITE.rgb)
        RenderUtils.drawEntityOnScreen(20, 35, 15, target)
        RenderUtils.drawRect(hpPos, 18F, 33F + ((nowAnimHP / target.maxHealth * 10000).roundToInt() / 100), 25F, darkColor)
        RenderUtils.drawRect(33F, 18F, hpPos, 25F, color)
        font.drawString("❤", 33, 30, Color.RED.rgb)
        font.drawString(decimalFormat.format(target.health), 43, 30, Color.WHITE.rgb)
    }

    private fun drawLiquid(target: EntityLivingBase, easingHealth: Float){
        val width = (38 + target.name.let(Fonts.font40::getStringWidth))
            .coerceAtLeast(118)
            .toFloat()
        // Draw rect box
        RenderUtils.drawBorderedRect(0F, 0F, width, 36F, 3F, Color.BLACK.rgb, Color.BLACK.rgb)

        // Damage animation
        if (easingHealth > target.health)
            RenderUtils.drawRect(0F, 34F, (easingHealth / target.maxHealth) * width,
                36F, Color(252, 185, 65).rgb)

        // Health bar
        RenderUtils.drawRect(0F, 34F, (target.health / target.maxHealth) * width,
            36F, Color(252, 96, 66).rgb)

        // Heal animation
        if (easingHealth < target.health)
            RenderUtils.drawRect((easingHealth / target.maxHealth) * width, 34F,
                (target.health / target.maxHealth) * width, 36F, Color(44, 201, 144).rgb)


        target.name.let { Fonts.font40.drawString(it, 36, 3, 0xffffff) }
        Fonts.font35.drawString("Distance: ${decimalFormat.format(mc.thePlayer.getDistanceToEntityBox(target))}", 36, 15, 0xffffff)

        // Draw info
        val playerInfo = mc.netHandler.getPlayerInfo(target.uniqueID)
        if (playerInfo != null) {
            Fonts.font35.drawString("Ping: ${playerInfo.responseTime.coerceAtLeast(0)}",
                36, 24, 0xffffff)

            // Draw head
            RenderUtils.drawHead(playerInfo.locationSkin, 2, 2, 30, 30)
        }
    }

    private fun drawFancy(target: EntityLivingBase, easingHealth: Float){
        val width = (48 + 9 + target.name.let(Fonts.fontBold40::getStringWidth))
            .coerceAtLeast(100)
            .toFloat()

        RenderUtils.drawRect(0f,0f,width,48+6f,Color(0,0,0,150))
        RenderUtils.drawRect(0f,0f,width,2f,Color(56,245,200))

        val playerInfo = mc.netHandler.getPlayerInfo(target.uniqueID)
        if (playerInfo != null) {
            RenderUtils.drawHead(playerInfo.locationSkin, 2, 4, 48, 48)
        }

        GL11.glTranslatef(48+3+3f,7f,0f)

        Fonts.fontBold40.drawString(target.name,0f,0f,Color.WHITE.rgb)
    }

    private fun drawFlux(target: EntityLivingBase, nowAnimHP: Float){
        val width = (38 + target.name.let(Fonts.font40::getStringWidth))
            .coerceAtLeast(70)
            .toFloat()

        // draw background
        RenderUtils.drawRect(0F, 0F, width,34F,Color(40,40,40).rgb)
        RenderUtils.drawRect(2F, 22F, width-2F, 24F, Color.BLACK.rgb)
        RenderUtils.drawRect(2F, 28F, width-2F, 30F, Color.BLACK.rgb)

        // draw bars
        RenderUtils.drawRect(2F, 22F, 2+(nowAnimHP / target.maxHealth) * (width-4), 24F, Color(231,182,0).rgb)
        RenderUtils.drawRect(2F, 22F, 2+(target.health / target.maxHealth) * (width-4), 24F, Color(0, 224, 84).rgb)
        RenderUtils.drawRect(2F, 28F, 2+(target.totalArmorValue / 20F) * (width-4), 30F, Color(77, 128, 255).rgb)

        // draw text
        Fonts.font40.drawString(target.name,22,3,Color.WHITE.rgb)
        GL11.glPushMatrix()
        GL11.glScaled(0.7,0.7,0.7)
        Fonts.font35.drawString("Health: ${decimalFormat.format(target.health)}",22/0.7F,(4+Fonts.font40.height)/0.7F,Color.WHITE.rgb)
        GL11.glPopMatrix()

        // Draw head
        val playerInfo = mc.netHandler.getPlayerInfo(target.uniqueID)
        if (playerInfo != null) {
            RenderUtils.drawHead(playerInfo.locationSkin, 2,2,16,16)
        }
    }

    private fun getTBorder():Border?{
        return when(modeValue.get().toLowerCase()){
            "novoline" -> Border(0F,0F,140F,40F)
            "astolfo" -> Border(0F,0F,140F,60F)
            "liquid" -> Border(0F,0F
                ,(38 + mc.thePlayer.name.let(Fonts.font40::getStringWidth)).coerceAtLeast(118).toFloat(),36F)
            "flux" -> Border(0F,0F,(38 + mc.thePlayer.name.let(Fonts.font40::getStringWidth))
                .coerceAtLeast(70)
                .toFloat(),34F)
            "fancy" -> Border(0F,0F,140F,40F)
            else -> null
        }
    }
}