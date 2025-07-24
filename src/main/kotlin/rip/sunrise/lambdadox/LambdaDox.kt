package rip.sunrise.lambdadox

import org.objectweb.asm.ClassReader
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import rip.sunrise.lambdadox.extensions.getFilesFromZip
import java.io.File
import java.io.PrintWriter

fun main(args: Array<String>) {
    if (args.size != 2) {
        println("Usage: /path/to/target.jar /path/to/output/mappings.txt")
        return
    }

    val mapFile = File(args[1]).absoluteFile
    mapFile.parentFile.mkdirs()
    mapFile.createNewFile()

    val mapStream = PrintWriter(mapFile.outputStream())

    File(args[0]).getFilesFromZip().forEach { (name, bytes) ->
        if (name.endsWith(".class")) {
            val clazz = ClassReader(bytes).let {
                val node = ClassNode(Opcodes.ASM9)
                it.accept(node, 0)
                node
            }

            val mappings = clazz.methods
                .filterNot { it.name.startsWith("lambda") }
                .filterNot { it.name == "<init>" }
                .filterNot { it.name == "<clinit>" }
                .mapNotNull { m ->
                    m.instructions
                        .filterIsInstance<InvokeDynamicInsnNode>()
                        .filter { it.bsm.name == "metafactory" }
                        .map { it.bsmArgs[1] as Handle }
                        .firstOrNull { it.name.startsWith("lambda") }
                        ?.let {
                            m to it.name.split("$")[1]
                        }
                }.toMutableList()
            mappings.removeIf { it.first.name == it.second }

            if (mappings.isEmpty()) return@forEach

            val clazzName = clazz.name.replace("/", ".")
            mapStream.println("$clazzName -> $clazzName:")
            mappings.forEach { (m, deobfuscated) ->
                val returnType = Type.getReturnType(m.desc).className
                val arguments = Type.getArgumentTypes(m.desc).joinToString(",") { it.className }
                mapStream.println("    $returnType ${m.name}($arguments) -> $deobfuscated")
            }
        }
    }

    mapStream.close()
}