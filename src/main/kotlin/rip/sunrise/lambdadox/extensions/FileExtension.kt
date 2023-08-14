package rip.sunrise.lambdadox.extensions

import java.io.File
import java.util.zip.ZipInputStream

fun File.getFilesFromZip(): List<DataPair> {
    val entries = mutableListOf<DataPair>()
    ZipInputStream(this.inputStream()).use {
        while (true) {
            val entry = it.nextEntry ?: return entries
            entries.add(DataPair(entry.name, it.readAllBytes()))
        }
    }
}

data class DataPair(val name: String, val data: ByteArray)