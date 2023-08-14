package net.ccbluex.liquidbounce.utils.render

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO


object SmoothImageUtils {

    fun applyAntialiasing(image: BufferedImage): BufferedImage {
        val antialiasedImage = BufferedImage(image.width + 2, image.height + 2, BufferedImage.TYPE_INT_ARGB)
        val g = antialiasedImage.createGraphics() as Graphics2D
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.drawImage(image, 1, 1, null)
        g.dispose()
        return antialiasedImage
    }

}