package com.example.thesis3

import android.os.Build
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

const val fileName = "/java/com.example.thesis3/text.txt"
val path: String = Path(fileName).absolutePathString()
fun write(msg: String) {
    val myfile = File(path)
    myfile.writeText(msg)
}