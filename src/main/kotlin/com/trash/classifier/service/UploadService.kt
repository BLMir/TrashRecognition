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

class UploadService {

    init {
        val app = Javalin.create().start(7000)
        app.get("/") { ctx -> ctx.result("Hello World") }
        app.post("/upload") { ctx -> fileUploads(ctx) }
        app.post("/guess-photo") { ctx -> guessPhoto(ctx) }
    }

    private fun fileUploads(ctx: Context) {
        try {
            val file = ctx.uploadedFile("file") ?: throw BadRequestException("file is mandatory in the request")

            val content = file!!.content
            val fileName = file!!.filename

            FileExtension.validateType(file.extension, file.contentType)

//            val image = ImageIO.read(content)
//            val resized = resize(image, 500, 500)
//            val output = File("/tmp/$fileName")
//            ImageIO.write(resized, "png", output)

            FileUtils.copyInputStreamToFile(content, File("/tmp/$fileName"))
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

    private fun resize(img: BufferedImage, height: Int, width: Int): BufferedImage {
        val tmp: Image = img.getScaledInstance(width, height, Image.SCALE_SMOOTH)
        val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2d: Graphics2D = resized.createGraphics()
        g2d.drawImage(tmp, 0, 0, null)
        g2d.dispose()
        return resized
    }

    private fun getProbability(imageBytes: ByteArray): List<SuccessGuessPhotoResponse> {
        val graphDef = File(ClassLoader.getSystemClassLoader().getResource("output_graph.pb")?.file).readBytes()
        val labels = File(ClassLoader.getSystemClassLoader().getResource("output_labels.txt")?.file).readLines()
        LabelImage.constructAndExecuteGraphToNormalizeImage(imageBytes).use { image ->
            val labelProbabilities = LabelImage.executeInceptionGraph(graphDef, image)

            return labelProbabilities.withIndex().map { (index, probability) ->
                println(
                    String.format(
                        "MATCH: %s (%.2f%% likely)",
                        labels[index],
                        probability * 100f
                    )
                )
                SuccessGuessPhotoResponse(probability * 100f.toDouble(), TrashCategories.valueOf(labels[index].toUpperCase()))
            }.sortedByDescending{ it.accuracy }
        }
    }
}