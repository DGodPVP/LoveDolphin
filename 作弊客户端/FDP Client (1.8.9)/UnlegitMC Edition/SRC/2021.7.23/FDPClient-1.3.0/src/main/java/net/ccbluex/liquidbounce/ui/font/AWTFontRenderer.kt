/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.LiquidBounce
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.TextureUtil
import org.apache.commons.io.IOUtils
import org.lwjgl.opengl.GL11
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.imageio.ImageIO

/**
 * Generate new bitmap based font renderer
 */
class AWTFontRenderer(val font: Font) {

    companion object {
        var assumeNonVolatile: Boolean = false
        val activeFontRenderers: ArrayList<AWTFontRenderer> = ArrayList()

        private var gcTicks: Int = 0
        private const val GC_TICKS = 600 // Start garbage collection every 600 frames
        private const val CACHED_FONT_REMOVAL_TIME = 30000 // Remove cached texts after 30s of not being used

        fun garbageCollectionTick() {
            if (gcTicks++ > GC_TICKS) {
                activeFontRenderers.forEach { it.collectGarbage() }

                gcTicks = 0
            }
        }
    }

    private fun collectGarbage() {
        val currentTime = System.currentTimeMillis()

        cachedStrings.filter { currentTime - it.value.lastUsage > CACHED_FONT_REMOVAL_TIME }.forEach {
            GL11.glDeleteLists(it.value.displayList, 1)

            it.value.deleted = true

            cachedStrings.remove(it.key)
        }
    }

    private var fontHeight = -1
    private val charLocations = arrayOfNulls<CharLocation>(255)

    private val cachedStrings: HashMap<String, CachedFont> = HashMap()

    private var textureID = 0
    private var textureWidth = 0
    private var textureHeight = 0
    private val fontMetrics: FontMetrics

    val height: Int
        get() = (fontHeight - 8) / 2

    init {
        val graphics = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).graphics as Graphics2D
        putHints(graphics)
        graphics.font = font
        fontMetrics=graphics.fontMetrics

        loadBitmap()

        activeFontRenderers.add(this)
    }

    /**
     * Allows you to draw a string with the target font
     *
     * @param text  to render
     * @param x     location for target position
     * @param y     location for target position
     * @param color of the text
     */
    fun drawString(text: String, x: Double, y: Double, color: Int) {
        val scale = 0.25
        val reverse = 1 / scale

        GlStateManager.pushMatrix()
        GlStateManager.scale(scale, scale, scale)
        GL11.glTranslated(x * 2F, y * 2.0 - 2.0, 0.0)
        GlStateManager.bindTexture(textureID)

        val red: Float = (color shr 16 and 0xff) / 255F
        val green: Float = (color shr 8 and 0xff) / 255F
        val blue: Float = (color and 0xff) / 255F
        val alpha: Float = (color shr 24 and 0xff) / 255F

        GlStateManager.color(red, green, blue, alpha)

        var currX = 0.0

        val cached: CachedFont? = cachedStrings[text]

        if (cached != null) {
            GL11.glCallList(cached.displayList)

            cached.lastUsage = System.currentTimeMillis()

            GlStateManager.popMatrix()

            return
        }

        var list = -1

        if (assumeNonVolatile) {
            list = GL11.glGenLists(1)

            GL11.glNewList(list, GL11.GL_COMPILE_AND_EXECUTE)
        }

        GL11.glBegin(GL11.GL_QUADS)

        for (char in text.toCharArray()) {
            if (char.toInt() >= charLocations.size) {
                GL11.glEnd()

                // Ugly solution, because floating point numbers, but I think that shouldn't be that much of a problem
                GlStateManager.scale(reverse, reverse, reverse)
                Minecraft.getMinecraft().fontRendererObj.drawString("$char", currX.toFloat() * scale.toFloat() + 1, 2f, color, false)
                currX += Minecraft.getMinecraft().fontRendererObj.getStringWidth("$char") * reverse

                GlStateManager.scale(scale, scale, scale)
                GlStateManager.bindTexture(textureID)
                GlStateManager.color(red, green, blue, alpha)

                GL11.glBegin(GL11.GL_QUADS)
            } else {
                val fontChar = charLocations[char.toInt()] ?: continue

                drawChar(fontChar, currX.toFloat(), 0f)
                currX += fontChar.width - 8.0
            }
        }

        GL11.glEnd()

        if (assumeNonVolatile) {
            cachedStrings[text] = CachedFont(list, System.currentTimeMillis())
            GL11.glEndList()
        }

        GlStateManager.popMatrix()
    }

    /**
     * Draw char from texture to display
     *
     * @param char target font char to render
     * @param x        target positon x to render
     * @param y        target potion y to render
     */
    private fun drawChar(char: CharLocation, x: Float, y: Float) {
        val width = char.width.toFloat()
        val height = char.height.toFloat()
        val srcX = char.x.toFloat()
        val srcY = char.y.toFloat()
        val renderX = srcX / textureWidth
        val renderY = srcY / textureHeight
        val renderWidth = width / textureWidth
        val renderHeight = height / textureHeight

        GL11.glTexCoord2f(renderX, renderY)
        GL11.glVertex2f(x, y)
        GL11.glTexCoord2f(renderX, renderY + renderHeight)
        GL11.glVertex2f(x, y + height)
        GL11.glTexCoord2f(renderX + renderWidth, renderY + renderHeight)
        GL11.glVertex2f(x + width, y + height)
        GL11.glTexCoord2f(renderX + renderWidth, renderY)
        GL11.glVertex2f(x + width, y)
    }

    private fun loadBitmap(){
        val cacheFontDir=File(LiquidBounce.fileManager.cacheDir
            ,"${font.fontName.replace(" ","_").toLowerCase()}${if(font.isBold){"-bold"}else{""}}${if(font.isItalic){"-italic"}else{""}}-${font.size}")
        if(!cacheFontDir.exists()) cacheFontDir.mkdir()
        val jsonFile=File(cacheFontDir,"data.json")
        val imageFile=File(cacheFontDir,"image.png")

        if(!jsonFile.exists() || !imageFile.exists()){
            println("bitmap cache not found! rendering...")
            renderBitmap(jsonFile, imageFile)
        }else{
            println("loading bitmap cache...")
            val json=JsonParser().parse(IOUtils.toString(FileInputStream(jsonFile),"utf-8")).asJsonObject
            textureHeight=json.get("height").asInt
            textureWidth=json.get("width").asInt
            fontHeight=json.get("fheight").asInt
            val chars=json.getAsJsonArray("chars")
            for(jsonElement in chars){
                val charJson=jsonElement.asJsonObject
                charLocations[charJson.get("char").asInt]= CharLocation(charJson)
            }

            val bufferedImage=ImageIO.read(imageFile)
            textureID = TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), bufferedImage, true,
                true)
        }
    }

    /**
     * Render font chars to a bitmap
     */
    private fun renderBitmap(jsonFile: File, imageFile: File) {
        val json=JsonObject()

        val fontImages = arrayOfNulls<BufferedImage>(255)
        var rowHeight = 0
        var charX = 0
        var charY = 0

        val charLocs=JsonArray()
        for (targetChar in 0 until 255) {
            val fontImage = drawCharToImage(targetChar.toChar())
            val fontChar = CharLocation(targetChar, charX, charY, fontImage.width, fontImage.height)
            charLocs.add(fontChar.toJson())

            if (fontChar.height > fontHeight)
                fontHeight = fontChar.height
            if (fontChar.height > rowHeight)
                rowHeight = fontChar.height

            charLocations[targetChar] = fontChar
            fontImages[targetChar] = fontImage

            charX += fontChar.width

            if (charX > 2048) {
                if (charX > textureWidth)
                    textureWidth = charX

                charX = 0
                charY += rowHeight
                rowHeight = 0
            }
        }
        textureHeight = charY + rowHeight

        json.addProperty("height",textureHeight)
        json.addProperty("width",textureWidth)
        json.addProperty("fheight",fontHeight)
        json.add("chars",charLocs)

        IOUtils.write(Gson().toJson(json),FileOutputStream(jsonFile),"utf-8")

        val bufferedImage = BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics2D = bufferedImage.graphics as Graphics2D
        graphics2D.font = font
        graphics2D.color = Color(255, 255, 255, 0)
        graphics2D.fillRect(0, 0, textureWidth, textureHeight)
        graphics2D.color = Color.white

        for (targetChar in 0 until 255)
            if (fontImages[targetChar] != null && charLocations[targetChar] != null)
                graphics2D.drawImage(fontImages[targetChar], charLocations[targetChar]!!.x, charLocations[targetChar]!!.y,
                        null)

        ImageIO.write(bufferedImage,"png",imageFile)

        textureID = TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), bufferedImage, true,
                true)
    }

    /**
     * Draw a char to a buffered image
     *
     * @param ch char to render
     * @return image of the char
     */
    private fun drawCharToImage(ch: Char): BufferedImage {
        var charWidth = fontMetrics.charWidth(ch) + 8
        if (charWidth <= 0)
            charWidth = 7

        var charHeight = fontMetrics.height + 3
        if (charHeight <= 0)
            charHeight = font.size

        val fontImage = BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = fontImage.graphics as Graphics2D
        putHints(graphics)
        graphics.font = font
        graphics.color = Color.WHITE
        graphics.drawString(ch.toString(), 3, 1 + fontMetrics.ascent)

        return fontImage
    }

    private fun putHints(graphics: Graphics2D){
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    }

    /**
     * Calculate the string width of a text
     *
     * @param text for width calculation
     * @return the width of the text
     */
    fun getStringWidth(text: String): Int {
        var width = 0

        for (c in text.toCharArray()) {
            val charCode=c.toInt()
            width += if(c.toInt()<charLocations.size){
                (charLocations[charCode]?.width ?: continue) - 8
            }else{
                ((charLocations['\u0003'.toInt()]?.width ?: continue)*1.5).toInt()
            }
        }

        return width / 2
    }

    /**
     * Data class for saving char location of the font image
     */
    private data class CharLocation(val char: Int, var x: Int, var y: Int, var width: Int, var height: Int){
        constructor(json:JsonObject) : this(json.get("char").asInt,json.get("x").asInt,json.get("y").asInt,json.get("width").asInt,json.get("height").asInt)

        fun toJson():JsonObject{
            val json=JsonObject()

            json.addProperty("char",char)
            json.addProperty("x",x)
            json.addProperty("y",y)
            json.addProperty("width",width)
            json.addProperty("height",height)

            return json
        }
    }
}