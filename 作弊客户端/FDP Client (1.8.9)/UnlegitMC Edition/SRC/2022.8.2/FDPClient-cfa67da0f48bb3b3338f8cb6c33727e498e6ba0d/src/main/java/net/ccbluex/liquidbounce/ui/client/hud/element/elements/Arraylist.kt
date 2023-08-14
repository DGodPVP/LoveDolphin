/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.client.hud.element.Side.Horizontal
import net.ccbluex.liquidbounce.ui.client.hud.element.Side.Vertical
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.utils.render.Animation
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorManager
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

/**
 * CustomHUD Arraylist element
 *
 * Shows a list of enabled modules
 */
@ElementInfo(name = "Arraylist", blur = true)
class Arraylist(
    x: Double = 5.0,
    y: Double = 5.0,
    scale: Float = 1F,
    side: Side = Side(Horizontal.RIGHT, Vertical.UP)
) : Element(x, y, scale, side) {

    private val ModeValue = ListValue("Mode", arrayOf("None", "Outline", "FDPNew"), "FDPNew")
    //private val colorRedValue = IntegerValue("Text-R", 0, 0, 255)
    //private val colorGreenValue = IntegerValue("Text-G", 111, 0, 255)
    //private val colorBlueValue = IntegerValue("Text-B", 255, 0, 255)
    //private val tagColorModeValue = ListValue("Tag-Color", arrayOf("Custom", "Random", "Rainbow", "AnotherRainbow", "Slowly", "SkyRainbow", "Astolfo"), "Custom")
    //private val tagColorRedValue = IntegerValue("Tag-R", 195, 0, 255)
    //private val tagColorGreenValue = IntegerValue("Tag-G", 195, 0, 255)
    //private val tagColorBlueValue = IntegerValue("Tag-B", 195, 0, 255)
    //private val astolfoRainbowOffset = IntegerValue("AstolfoRainbowOffset", 5, 1, 20)
    //private val astolfoRainbowIndex = IntegerValue("AstolfoRainbowIndex", 109, 1, 300)
    //private val rectColorModeValue = ListValue("Rect-Color", arrayOf("Custom", "Random", "Rainbow", "AnotherRainbow", "Slowly", "SkyRainbow", "Astolfo"), "Rainbow")
    //private val rectColorRedValue = IntegerValue("Rect-R", 255, 0, 255)
    //private val rectColorGreenValue = IntegerValue("Rect-G", 255, 0, 255)
    //private val rectColorBlueValue = IntegerValue("Rect-B", 255, 0, 255)
    //private val rectColorBlueAlpha = IntegerValue("Rect-Alpha", 255, 0, 255)
    //private val saturationValue = FloatValue("Random-Saturation", 0.9f, 0f, 1f)
    //private val brightnessValue = FloatValue("Random-Brightness", 1f, 0f, 1f)
    private val tagsValue = ListValue("TagsStyle", arrayOf("-", "|", "()", "[]", "<>", "Space", "None"), "Space")
    private val shadow = BoolValue("ShadowText", true)
    private val split = BoolValue("SplitName", false)
    private val slideInAnimation = BoolValue("SlideInAnimation", true)
    private val noRenderModules = BoolValue("NoRenderModules", true)
    //private val backgroundColorModeValue = ListValue("Background-Color", arrayOf("Custom", "Random", "Rainbow", "AnotherRainbow", "Slowly", "SkyRainbow", "Astolfo"), "Custom")
    //private val backgroundColorRedValue = IntegerValue("Background-R", 0, 0, 255)
    //private val backgroundColorGreenValue = IntegerValue("Background-G", 0, 0, 255)
    //private val backgroundColorBlueValue = IntegerValue("Background-B", 0, 0, 255)
    //private val backgroundColorAlphaValue = IntegerValue("Background-Alpha", 100, 0, 255)
    //private val backgroundExpand = IntegerValue("Background-Expand", 2, 0, 10)
    //private val rainbowSpeed = IntegerValue("RainbowSpeed", 1, 1, 10)
    //private val rectValue = ListValue("Rect", arrayOf("None", "Left", "Right", "Outline", "Special", "Top"), "None")
    private val caseValue = ListValue("Case", arrayOf("Upper", "Normal", "Lower"), "Normal")
    //private val spaceValue = FloatValue("Space", 0F, 0F, 5F)
    //private val textHeightValue = FloatValue("TextHeight", 11F, 1F, 20F)
    //private val textYValue = FloatValue("TextY", 2.4F, 0F, 20F)
    private val fontValue = FontValue("Font", Fonts.font32)

    private var x2 = 0
    private var y2 = 0F

    private var modules = emptyList<Module>()
    
    val delay = intArrayOf(0)

    private fun shouldExpect(module: Module): Boolean {
        return noRenderModules.get() && module.category == ModuleCategory.RENDER
    }

    private fun changeCase(inStr: String): String {
        val str = LanguageManager.replace(inStr)
        return when (caseValue.get().lowercase()) {
            "upper" -> str.uppercase()
            "lower" -> str.lowercase()
            else -> str
        }
    }

    private fun getModuleTag(module: Module): String {
        module.tag ?: return ""
        return when (tagsValue.get().lowercase()) {
            "-" -> " - ${module.tag}"
            "|" -> "|${module.tag}"
            "()" -> " (${module.tag})"
            "[]" -> " [${module.tag}]"
            "<>" -> " <${module.tag}>"
            "space" -> " ${module.tag}"
            else -> ""
        }
    }

    private fun getModuleName(module: Module) = if (split.get()) { module.splicedName } else { module.localizedName }

    override fun drawElement(partialTicks: Float): Border? {
        val fontRenderer = fontValue.get()

        for (module in LiquidBounce.moduleManager.modules) {
            if (!module.array || shouldExpect(module) || (!module.state && module.slide == 0F && (module.yPosAnimation == null || module.yPosAnimation!!.state == Animation.EnumAnimationState.STOPPED))) continue

            module.width = fontRenderer.getStringWidth(changeCase(getModuleName(module) + getModuleTag(module)))

            val targetSlide = if (module.state) { module.width.toFloat() } else { 0f }
            if (module.slide != targetSlide) {
                module.slide = targetSlide
            }
        }

        // Draw arraylist
        val textShadow = shadow.get()
        val textSpacer = 11f

        when (ModeValue.get().toLowerCase()) {
            "none" -> {
                modules.forEachIndexed { index, module ->
                    val xPos = -module.slide - 2
                    val a= (if (side.vertical == Vertical.DOWN) index + 1f else index).toFloat()
                    val realYPos = if (slideInAnimation.get() && !module.state) { if (side.vertical == Vertical.DOWN) { 0f } else { -11f } } else { (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) * a }
                    val yPos = module.yPos
                    if (yPos != realYPos) {
                        module.yPos = realYPos
                    }

                    val rectX = xPos -  5
                    blur(rectX - 10, yPos,  -3F, yPos + 11)
                    RenderUtils.drawRect(
                        rectX - 2,
                        yPos,
                        0f,
                        yPos + 11,
                        Color(0,0,0,100)
                    )

                    val mName = changeCase(getModuleName(module))
                    val mTag = changeCase(getModuleTag(module))
                    fontRenderer.drawString(mName, xPos - 3, yPos + 2.4f,
                        ColorManager.astolfoRainbow(0,0,0), textShadow)

                    fontRenderer.drawString(mTag, xPos -  3 + fontRenderer.getStringWidth(mName), yPos + 2.4f,
                        Color(245,245,245,150).rgb, textShadow)
                }
            }
            "outline" -> {
                modules.forEachIndexed { index, module ->
                    val xPos = -module.slide - 2
                    val a= (if (side.vertical == Vertical.DOWN) index + 1f else index).toFloat()
                    var b= (if((index+1)==modules.size) 0f else -modules.get(index+1).slide - 7)
                    val realYPos = if (slideInAnimation.get() && !module.state) { if (side.vertical == Vertical.DOWN) { 0f } else { -11f } } else { (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) * a }
                    val yPos = module.yPos
                    if (yPos != realYPos) {
                        module.yPos = realYPos
                    }

                    val rectX = xPos -  5
                    blur(rectX - 10, yPos,  -3F, yPos + 11)
                    if(index==0) {
                        RenderUtils.drawRect(
                            rectX - 1,
                            yPos - 1,
                            0f,
                            yPos,
                            ColorManager.astolfoRainbow(0, 0, 0)
                        )
                    }
                    RenderUtils.drawRect(
                        0f,
                        yPos-1,
                        1f,
                        yPos+11,
                        ColorManager.astolfoRainbow(0,0,0)
                    )
                    RenderUtils.drawRect(
                        rectX - 1,
                        yPos,
                        0f,
                        yPos + 11,
                        Color(0,0,0,100)
                    )
                    RenderUtils.drawRect(
                        rectX - 1,
                        yPos-1,
                        rectX,
                        yPos + 11,
                        ColorManager.astolfoRainbow(0,0,0)
                    )
                    RenderUtils.drawRect(
                        rectX - 1,
                        yPos+10,
                        if((index+1)==modules.size) 0f else ((-modules.get(index+1).slide - 8)),
                        yPos + 11,
                        ColorManager.astolfoRainbow(0,0,0)
                    )
                    val mName = changeCase(getModuleName(module))
                    val mTag = changeCase(getModuleTag(module))
                    fontRenderer.drawString(mName, xPos - 1, yPos + 2.4f,
                        ColorManager.astolfoRainbow(0,0,0), textShadow)

                    fontRenderer.drawString(mTag, xPos -  1 + fontRenderer.getStringWidth(mName), yPos + 2.4f,
                        Color(245,245,245,150).rgb, textShadow)
                }
            }
            "newfdp" -> {
                modules.forEachIndexed { index, module ->
                    val xPos = -module.slide - 2
                    val a= (if (side.vertical == Vertical.DOWN) index + 1f else index).toFloat()
                    val realYPos = if (slideInAnimation.get() && !module.state) { if (side.vertical == Vertical.DOWN) { 0f } else { -11f } } else { (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) * a }
                    val yPos = module.yPos
                    if (yPos != realYPos) {
                        module.yPos = realYPos
                    }

                    val rectX = xPos -  5
                    blur(rectX - 10, yPos,  -3F, yPos + 11)
                    RenderUtils.drawRect(
                        rectX - 2,
                        yPos,
                        0f,
                        yPos + 11,
                        Color(0,0,0,100)
                    )

                    val mName = changeCase(getModuleName(module))
                    val mTag = changeCase(getModuleTag(module))
                    fontRenderer.drawString(mName, xPos - 3, yPos + 2.4f,
                        ColorManager.astolfoRainbow(0,0,0), textShadow)

                    fontRenderer.drawString(mTag, xPos -  3 + fontRenderer.getStringWidth(mName), yPos + 2.4f,
                        Color(245,245,245,150).rgb, textShadow)
                }
            }
        }

        // Draw border
        if (mc.currentScreen is GuiHudDesigner) {
            x2 = Int.MIN_VALUE

            if (modules.isEmpty()) {
                return if (side.horizontal == Horizontal.LEFT) {
                    Border(0F, -1F, 20F, 20F)
                } else {
                    Border(0F, -1F, -20F, 20F)
                }
            }

            for (module in modules) {
                when (side.horizontal) {
                    Horizontal.RIGHT, Horizontal.MIDDLE -> {
                        val xPos = -module.slide.toInt() - 2
                        if (x2 == Int.MIN_VALUE || xPos < x2) x2 = xPos
                    }
                    Horizontal.LEFT -> {
                        val xPos = module.slide.toInt() + 14
                        if (x2 == Int.MIN_VALUE || xPos > x2) x2 = xPos
                    }
                }
            }
            y2 = ((if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) * modules.size)

            return Border(0F, 0F, x2 - 7F, y2 - if (side.vertical == Vertical.DOWN) 1F else 0F)
        }

        GlStateManager.resetColor()
        return null
    }

    override fun updateElement() {
        modules = LiquidBounce.moduleManager.modules
            .filter { it.array && !shouldExpect(it) && (it.state || it.slide > 0 || !(it.yPosAnimation==null || it.yPosAnimation!!.state==Animation.EnumAnimationState.STOPPED)) }
            .sortedBy { -it.width }
    }

    override fun drawBoarderBlur(blurRadius: Float) {}
}
