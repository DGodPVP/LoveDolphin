/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.client.hud.element.Side.Horizontal
import net.ccbluex.liquidbounce.ui.client.hud.element.Side.Vertical
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

/**
 * CustomHUD Arraylist element
 *
 * Shows a list of enabled modules
 */
@ElementInfo(name = "Arraylist", single = true)
class Arraylist(x: Double = 1.0, y: Double = 2.0, scale: Float = 1F,
                side: Side = Side(Horizontal.RIGHT, Vertical.UP)) : Element(x, y, scale, side) {

    private val colorModeValue = ListValue("Text-Color", arrayOf("Custom", "Random", "Rainbow", "AnotherRainbow", "OtherRainbow", "SkyRainbow"), "SkyRainbow")
    private val colorRedValue = IntegerValue("Text-R", 0, 0, 255)
    private val colorGreenValue = IntegerValue("Text-G", 111, 0, 255)
    private val colorBlueValue = IntegerValue("Text-B", 255, 0, 255)
    private val tagColorModeValue = ListValue("Tag-Color", arrayOf("Custom", "Random", "Rainbow", "AnotherRainbow", "OtherRainbow", "SkyRainbow"), "Custom")
    private val tagColorRedValue = IntegerValue("Tag-R", 255, 0, 255)
    private val tagColorGreenValue = IntegerValue("Tag-G", 255, 0, 255)
    private val tagColorBlueValue = IntegerValue("Tag-B", 255, 0, 255)
    private val rectColorModeValue = ListValue("Rect-Color", arrayOf("Custom", "Random", "Rainbow", "AnotherRainbow", "OtherRainbow", "SkyRainbow"), "Rainbow")
    private val rectColorRedValue = IntegerValue("Rect-R", 255, 0, 255)
    private val rectColorGreenValue = IntegerValue("Rect-G", 255, 0, 255)
    private val rectColorBlueValue = IntegerValue("Rect-B", 255, 0, 255)
    private val rectColorBlueAlpha = IntegerValue("Rect-Alpha", 255, 0, 255)
    private val saturationValue = FloatValue("Random-Saturation", 0.9f, 0f, 1f)
    private val brightnessValue = FloatValue("Random-Brightness", 1f, 0f, 1f)
    private val tags = BoolValue("Tags", true)
    private val shadow = BoolValue("ShadowText", false)
    private val split = BoolValue("SplitName", false)
    private val backgroundColorModeValue = ListValue("Background-Color", arrayOf("Custom", "Random", "Rainbow", "AnotherRainbow", "OtherRainbow", "SkyRainbow"), "Custom")
    private val backgroundColorRedValue = IntegerValue("Background-R", 0, 0, 255)
    private val backgroundColorGreenValue = IntegerValue("Background-G", 0, 0, 255)
    private val backgroundColorBlueValue = IntegerValue("Background-B", 0, 0, 255)
    private val backgroundColorAlphaValue = IntegerValue("Background-Alpha", 0, 0, 255)
    private val backgroundExpand = IntegerValue("Background-Expand",2,0,10)
    private val rainbowSpeed = IntegerValue("RainbowSpeed",10,1,10)
    private val rectValue = ListValue("Rect", arrayOf("None", "Left", "Right", "Outline"), "None")
    private val upperCaseValue = BoolValue("UpperCase", false)
    private val spaceValue = FloatValue("Space", 0F, 0F, 5F)
    private val textHeightValue = FloatValue("TextHeight", 11F, 1F, 20F)
    private val textYValue = FloatValue("TextY", 1F, 0F, 20F)
    private val fontValue = FontValue("Font", Fonts.font40)

    private var x2 = 0
    private var y2 = 0F

    private var modules = emptyList<Module>()

    override fun drawElement(partialTicks: Float): Border? {
        val fontRenderer = fontValue.get()

        AWTFontRenderer.assumeNonVolatile = true

        // Slide animation - update every render
        val delta = RenderUtils.deltaTime

        fun getModuleName(module: Module) = if(split.get()){ module.splicedName }else{ module.name }

        for (module in LiquidBounce.moduleManager.modules) {
            if (!module.array || (!module.state && module.slide == 0F)) continue

            var displayString = getModuleName(module)+if(module.tag!=null){if(tags.get()){" "+module.tag}else{""}}else{""}

            if(upperCaseValue.get()) displayString=displayString.toUpperCase()

            module.width = fontRenderer.getStringWidth(displayString)

            if (module.state) {
                if (module.slide < module.width) {
                    module.slide = AnimationUtils.easeOut(module.slideStep, module.width.toFloat()) * module.width
                    module.slideStep += delta / 4F
                }
            } else if (module.slide > 0) {
                module.slide = AnimationUtils.easeOut(module.slideStep, module.width.toFloat()) * module.width
                module.slideStep -= delta / 4F
            }

            module.slide = module.slide.coerceIn(0F, module.width.toFloat())
            module.slideStep = module.slideStep.coerceIn(0F, module.width.toFloat())
        }

        // Draw arraylist
        val colorMode = colorModeValue.get()
        val rectColorMode = rectColorModeValue.get()
        val backgroundColorMode = backgroundColorModeValue.get()
        val customColor = Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
        val tagCustomColor = Color(tagColorRedValue.get(),tagColorGreenValue.get(),tagColorBlueValue.get())
        val rectCustomColor = Color(rectColorRedValue.get(), rectColorGreenValue.get(), rectColorBlueValue.get(),
                rectColorBlueAlpha.get())
        val space = spaceValue.get()
        val textHeight = textHeightValue.get()
        val textY = textYValue.get()
        val rectMode = rectValue.get()
        val backgroundCustomColor = Color(backgroundColorRedValue.get(), backgroundColorGreenValue.get(),
                backgroundColorBlueValue.get(), backgroundColorAlphaValue.get())
        val textShadow = shadow.get()
        val textSpacer = textHeight + space
        val saturation = saturationValue.get()
        val brightness = brightnessValue.get()

        when (side.horizontal) {
            Horizontal.RIGHT, Horizontal.MIDDLE -> {
                modules.forEachIndexed { index, module ->

                    val xPos = -module.slide - 2
                    val yPos = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) *
                            if (side.vertical == Vertical.DOWN) index + 1 else index
                    val moduleColor = Color.getHSBColor(module.hue, saturation, brightness).rgb

                    val rectX=xPos - if (rectMode.equals("right", true)) 5 else 2
                    RenderUtils.drawRect(
                            rectX-backgroundExpand.get(),
                            yPos,
                            if (rectMode.equals("right", true)) -3F else 0F,
                            yPos + textHeight,
                        when(backgroundColorMode.toLowerCase()) {
                            "rainbow" -> ColorUtils.rainbow(400000000L * index).rgb
                            "random" -> moduleColor
                            "skyrainbow" -> RenderUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get(), rainbowSpeed.get().toDouble()).rgb
                            "otherrainbow" -> RenderUtils.arrayRainbow((index+1)*rainbowSpeed.get()).rgb
                            "anotherrainbow" -> ColorUtils.fade(backgroundCustomColor,100,index+1).rgb
                            else -> backgroundCustomColor.rgb
                        }
                    )

                    val mName=if(upperCaseValue.get()){getModuleName(module).toUpperCase()}else{getModuleName(module)}
                    val mTag=if(module.tag!=null){" "+if(upperCaseValue.get()){module.tag!!.toUpperCase()}else{module.tag}}else{""}
                    fontRenderer.drawString(mName, xPos - if (rectMode.equals("right", true)) 3 else 0, yPos + textY,
                        when(colorMode.toLowerCase()) {
                            "rainbow" -> ColorUtils.rainbow(400000000L * index).rgb
                            "random" -> moduleColor
                            "skyrainbow" -> RenderUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get(), rainbowSpeed.get().toDouble()).rgb
                            "otherrainbow" -> RenderUtils.arrayRainbow((index+1)*rainbowSpeed.get()).rgb
                            "anotherrainbow" -> ColorUtils.fade(customColor,100,index+1).rgb
                            else -> customColor.rgb
                        }, textShadow)
                    if(tags.get()){
                    fontRenderer.drawString(mTag, xPos - (if (rectMode.equals("right", true)) 3 else 0) + fontRenderer.getStringWidth(mName), yPos + textY,
                        ColorUtils.antiColor(when(tagColorModeValue.get().toLowerCase()) {
                            "rainbow" -> ColorUtils.rainbow(400000000L * index)
                            "random" -> Color(moduleColor)
                            "skyrainbow" -> RenderUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get(), rainbowSpeed.get().toDouble())
                            "otherrainbow" -> ColorUtils.antiColor(RenderUtils.arrayRainbow((index+1)*rainbowSpeed.get()))
                            "anotherrainbow" -> ColorUtils.antiColor(ColorUtils.fade(tagCustomColor,100,index+1))
                            else -> ColorUtils.antiColor(tagCustomColor)
                        }).rgb, textShadow)
                    }

                    if (!rectMode.equals("none", true)) {
                        val rectColor = when(rectColorMode.toLowerCase()) {
                            "rainbow" -> ColorUtils.rainbow(400000000L * index).rgb
                            "random" -> moduleColor
                            "skyrainbow" -> RenderUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get(), rainbowSpeed.get().toDouble()).rgb
                            "otherrainbow" -> RenderUtils.arrayRainbow((index+1)*rainbowSpeed.get()).rgb
                            "anotherrainbow" -> ColorUtils.fade(rectCustomColor,100,index+1).rgb
                            else -> rectCustomColor.rgb
                        }

                        when(rectMode.toLowerCase()) {
                            "left" -> RenderUtils.drawRect(xPos - 5, yPos, xPos - 2, yPos + textHeight,
                                    rectColor)
                            "right" -> RenderUtils.drawRect(-3F, yPos, 0F,
                                    yPos + textHeight, rectColor)
                            "outline" -> {
                                RenderUtils.drawRect(-1F, yPos - 1F, 0F,
                                    yPos + textHeight, rectColor)
                                RenderUtils.drawRect(xPos - 3, yPos, xPos - 2, yPos + textHeight,
                                    rectColor)
                                if (module != modules[0]) {
                                    RenderUtils.drawRect(xPos - 3 - (modules[index-1].width - module.width), yPos, xPos - 2, yPos + 1,
                                        rectColor)
                                    if (module == modules[modules.size - 1]) {
                                        RenderUtils.drawRect(xPos - 3, yPos + textHeight, 0.0F, yPos + textHeight + 1,
                                            rectColor)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Horizontal.LEFT -> {
                modules.forEachIndexed { index, module ->
                    val xPos = -(module.width - module.slide) + if (rectMode.equals("left", true)) 5 else 2
                    val yPos = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) *
                            if (side.vertical == Vertical.DOWN) index + 1 else index
                    val moduleColor = Color.getHSBColor(module.hue, saturation, brightness).rgb

                    RenderUtils.drawRect(
                            0F,
                            yPos,
                            xPos + module.width + if (rectMode.equals("right", true)) 5 else 2 + backgroundExpand.get(),
                            yPos + textHeight,
                        when(backgroundColorMode.toLowerCase()) {
                            "rainbow" -> ColorUtils.rainbow(400000000L * index).rgb
                            "random" -> moduleColor
                            "skyrainbow" -> RenderUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get(), rainbowSpeed.get().toDouble()).rgb
                            "otherrainbow" -> RenderUtils.arrayRainbow((index+1)*rainbowSpeed.get()).rgb
                            "anotherrainbow" -> ColorUtils.fade(backgroundCustomColor,100,index+1).rgb
                            else -> backgroundCustomColor.rgb
                        }
                    )

                    val mName=if(upperCaseValue.get()){getModuleName(module).toUpperCase()}else{getModuleName(module)}
                    val mTag=if(module.tag!=null){" "+if(upperCaseValue.get()){module.tag!!.toUpperCase()}else{module.tag}}else{""}
                    fontRenderer.drawString(mName, xPos, yPos + textY, when(colorMode.toLowerCase()) {
                        "rainbow" -> ColorUtils.rainbow(400000000L * index).rgb
                        "random" -> moduleColor
                        "skyrainbow" -> RenderUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get(), rainbowSpeed.get().toDouble()).rgb
                        "otherrainbow" -> RenderUtils.arrayRainbow((index+1)*rainbowSpeed.get()).rgb
                        "anotherrainbow" -> ColorUtils.fade(customColor,100,index+1).rgb
                        else -> customColor.rgb
                    }, textShadow)

                    fontRenderer.drawString(mTag, xPos+fontRenderer.getStringWidth(mName), yPos + textY,
                        ColorUtils.antiColor(when(tagColorModeValue.get().toLowerCase()) {
                            "rainbow" -> ColorUtils.rainbow(400000000L * index)
                            "random" -> Color(moduleColor)
                            "skyrainbow" -> RenderUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get(), rainbowSpeed.get().toDouble())
                            "otherrainbow" -> ColorUtils.antiColor(RenderUtils.arrayRainbow((index+1)*rainbowSpeed.get()))
                            "anotherrainbow" -> ColorUtils.antiColor(ColorUtils.fade(tagCustomColor,100,index+1))
                            else -> ColorUtils.antiColor(tagCustomColor)
                        }).rgb, textShadow)

                    if (!rectMode.equals("none", true)) {
                        val rectColor = when(rectColorMode.toLowerCase()) {
                            "rainbow" -> ColorUtils.rainbow(400000000L * index).rgb
                            "random" -> moduleColor
                            "skyrainbow" -> RenderUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get(), rainbowSpeed.get().toDouble()).rgb
                            "otherrainbow" -> RenderUtils.arrayRainbow((index+1)*rainbowSpeed.get()).rgb
                            "anotherrainbow" -> ColorUtils.fade(rectCustomColor,100,index+1).rgb
                            else -> rectCustomColor.rgb
                        }

                        when {
                            rectMode.equals("left", true) -> RenderUtils.drawRect(0F,
                                    yPos - 1, 3F, yPos + textHeight, rectColor)
                            rectMode.equals("right", true) ->
                                RenderUtils.drawRect(xPos + module.width + 2, yPos, xPos + module.width + 2 + 3,
                                        yPos + textHeight, rectColor)
                        }
                    }
                }
            }
        }

        // Draw border
        if (mc.currentScreen is GuiHudDesigner) {
            x2 = Int.MIN_VALUE

            if (modules.isEmpty()) {
                return if (side.horizontal == Horizontal.LEFT)
                    Border(0F, -1F, 20F, 20F)
                else
                    Border(0F, -1F, -20F, 20F)
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
            y2 = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) * modules.size

            return Border(0F, 0F, x2 - 7F, y2 - if(side.vertical == Vertical.DOWN) 1F else 0F)
        }

        AWTFontRenderer.assumeNonVolatile = false
        GlStateManager.resetColor()
        return null
    }

    override fun updateElement() {
        modules = LiquidBounce.moduleManager.modules
                .filter { it.array && it.slide > 0 }
                .sortedBy { -it.width }
    }
}
