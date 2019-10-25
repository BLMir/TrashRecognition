package com.trash.classifier

import com.trash.classifier.service.UploadService
import java.io.File

fun main() {
    UploadService(
        File(ClassLoader.getSystemClassLoader().getResource("output_graph.pb")?.file).readBytes(),
        File(ClassLoader.getSystemClassLoader().getResource("output_labels.txt")?.file).readLines()
    )
}