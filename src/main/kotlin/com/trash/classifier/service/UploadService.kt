package com.trash.classifier.service


import com.trash.classifier.entity.TrashCategories
import com.trash.classifier.exceptions.BadRequestException
import io.javalin.Javalin
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import java.io.File
import io.javalin.http.Context
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.InputStream
import java.lang.IllegalArgumentException


private val logger = KotlinLogging.logger {}

enum class FileExtension(val extension: String) {
    JPG(".jpg"),
    PNG(".png"),
    GIF(".gif");

    companion object {
        fun validateType(extension: String, contentType: String) {
            values().firstOrNull {
                extension == it.extension
            } ?: throw BadRequestException("Invalid file format, extension = $extension , contentType = $contentType")
        }
    }
}

data class SuccessGuessPhotoResponse(val accuracy: Double, val trashCategory: TrashCategories)

class UploadService(val model: ByteArray, val labels: List<String>) {

    init {
        val app = Javalin.create{
            it.enableCorsForAllOrigins()
        }.start(7000)
        app.get("/") { ctx -> ctx.result("Hello World") }
        app.post("/upload") { ctx -> fileUploads(ctx) }
        app.post("/guess-photo") { ctx -> guessPhoto(ctx) }
        app.post("/teach-machine") { ctx -> teachMachine(ctx) }
    }

    private fun fileUploads(ctx: Context) {
        try {
            val file = ctx.uploadedFile("file") ?: throw BadRequestException("file is mandatory in the request")

            val content = file!!.content
            val fileName = file!!.filename

            FileExtension.validateType(file.extension, file.contentType)

            uploadFileTo(content, "/tmp/$fileName")
        } catch (ex: BadRequestException) {
            logger.error { "Bad Request: ${ex.message}" }
            throw ex
        } catch (ex: Exception) {
            logger.error { "Error uploading a file" }
            throw ex
        }
    }

    private fun guessPhoto(ctx: Context) {
        try {
            val file = ctx.uploadedFile("file") ?: throw BadRequestException("file is mandatory in the request")

            ctx.json(getProbability(file.content.readBytes()))
        } catch (ex: BadRequestException) {
            logger.error { "Bad Request: ${ex.message}" }
            throw ex
        } catch (ex: Exception) {
            logger.error { "Error guessing a photo" }
            throw ex
        }
    }

    private fun teachMachine(ctx: Context) {
        try {
            val file = ctx.uploadedFile("file") ?: throw BadRequestException("file is mandatory in the request")
            val category = ctx.formParam("category") ?: throw BadRequestException("category is mandatory in the request")
            val validCategory = TrashCategories.values().find { it.name == category } ?: throw BadRequestException("Category from request: $category does not exist. Valid categories are: ${TrashCategories.values().toList()}")

            val content = file!!.content
            val fileName = file!!.filename

            uploadFileTo(content, "/tmp/$validCategory/$fileName")

        } catch (ex: BadRequestException) {
            logger.error { "Bad Request: ${ex.message}" }
            throw ex
        } catch ( ex: IllegalArgumentException) {

        } catch (ex: Exception) {
            logger.error { "Error guessing a photo, sorry about that" }
            throw ex
        }
    }

    private fun uploadFileTo(file: InputStream, path: String ) {
        FileUtils.copyInputStreamToFile(file, File(path))
    }

    private fun getProbability(imageBytes: ByteArray): List<SuccessGuessPhotoResponse> {
        LabelImage.constructAndExecuteGraphToNormalizeImage(imageBytes).use { image ->
            val labelProbabilities = LabelImage.executeInceptionGraph(model, image)

            return labelProbabilities.withIndex().map { (index, probability) ->
                println(
                    String.format(
                        "-MATCH: %s (%.2f%% likely)",
                        labels[index],
                        probability * 100f
                    )
                )
                SuccessGuessPhotoResponse(probability * 100f.toDouble(), TrashCategories.valueOf(labels[index].toUpperCase()))
            }.sortedByDescending{ it.accuracy }
        }
    }
}