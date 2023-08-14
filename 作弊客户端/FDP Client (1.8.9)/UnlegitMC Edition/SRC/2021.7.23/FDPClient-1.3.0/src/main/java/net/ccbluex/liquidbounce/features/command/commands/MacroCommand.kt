package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.special.macro.Macro
import net.ccbluex.liquidbounce.utils.misc.StringUtils
import org.lwjgl.input.Keyboard

class MacroCommand : Command("macro", arrayOf("m")) {
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            val arg1=args[1]
            when(arg1.toLowerCase()){
                "add" -> {
                    if (args.size > 3) {
                        val key = Keyboard.getKeyIndex(args[2].toUpperCase())
                        if(key!=Keyboard.KEY_NONE){
                            var comm=StringUtils.toCompleteString(args, 3)
                            if(!comm.startsWith(".")) comm=".$comm"
                            LiquidBounce.macroManager.macros.add(Macro(key,comm))
                            chat("Bound macro $comm to key ${Keyboard.getKeyName(key)}.")
                        }else{
                            chat("Unknown key to bind macro.")
                        }
                        save()
                    }else{
                        chatSyntax("macro add <key> <macro>")
                    }
                }

                "remove" -> {
                    if (args.size > 2) {
                        if(args[2].startsWith(".")){
                            LiquidBounce.macroManager.macros.filter { it.command == StringUtils.toCompleteString(args, 2) }
                        }else{
                            val key = Keyboard.getKeyIndex(args[2].toUpperCase())
                            LiquidBounce.macroManager.macros.filter { it.key==key }
                        }.forEach {
                            LiquidBounce.macroManager.macros.remove(it)
                            chat("Remove macro ${it.command}.")
                        }
                        save()
                    }else{
                        chatSyntax("macro remove <macro/key>")
                    }
                }

                "list" -> {
                    chat("Macros:")
                    LiquidBounce.macroManager.macros.forEach {
                        chat("key=${Keyboard.getKeyName(it.key)}, command=${it.command}")
                    }
                }

                else -> chatSyntax("macro <add/remove/list>")
            }
            return
        }
        chatSyntax("macro <add/remove/list>")
    }

    private fun save(){
        LiquidBounce.configManager.smartSave()
        playEdit()
    }
}