package com.trash.classifier

import com.trash.classifier.service.UploadService
import java.io.FileInputStream
import java.net.URL

var envVarUrl: String = System.getenv("varname") ?: "/tmp/output_graph.pb"


fun main() {
    UploadService(
        FileInputStream(envVarUrl).readBytes(),
        listOf("cardboard","glass"
                ,"metal"
                ,"paper"
                ,"plastic"
                ,"trash"
        )
    )
}