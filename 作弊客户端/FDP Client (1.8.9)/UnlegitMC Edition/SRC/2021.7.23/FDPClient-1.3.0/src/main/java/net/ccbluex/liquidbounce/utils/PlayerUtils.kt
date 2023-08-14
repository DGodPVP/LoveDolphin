package net.ccbluex.liquidbounce.utils

object PlayerUtils {
    fun randomUnicode(str: String):String{
        val stringBuilder = StringBuilder()
        for (c in str.toCharArray()){
            if (Math.random()>0.5&&c.toInt() in 33..128){
                stringBuilder.append(Character.toChars(c.toInt() + 65248))
            }else{
                stringBuilder.append(c)
            }
        }
        return stringBuilder.toString()
    }
}