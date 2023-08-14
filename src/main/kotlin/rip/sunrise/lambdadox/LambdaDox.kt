package rip.sunrise.lambdadox

import org.objectweb.asm.ClassReader
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import rip.sunrise.lambdadox.extensions.getFilesFromZip
import java.io.File

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: /path/to/target.jar")
        return
    }

    File(args[0]).getFilesFromZip().forEach { (name, bytes) ->
        if (name.endsWith(".class")) {
            val clazz = ClassReader(bytes).let {
                val node = ClassNode(Opcodes.ASM9)
                it.accept(node, 0)
                node
            }

            clazz.methods
                .filterNot { it.name.startsWith("lambda") }
                .filterNot { it.name == "<init>" }
                .filterNot { it.name == "<clinit>" }
                .forEach { m ->
                    m.instructions
                        .filterIsInstance<InvokeDynamicInsnNode>()
                        .filter { it.bsm.name == "metafactory" }
                        .map { it.bsmArgs[1] as Handle }
                        .firstOrNull { it.name.startsWith("lambda") }
                        ?.also {
                            val capturedName = it.name.split("$")[1]
                            println("${m.name} -> $capturedName")
                        }
                }
        }
    }
}