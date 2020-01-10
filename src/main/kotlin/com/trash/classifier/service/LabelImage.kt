package com.trash.classifier.service

import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.util.Arrays
import org.tensorflow.Graph
import org.tensorflow.Session
import org.tensorflow.Tensor

/** Sample use of the TensorFlow Java API to label images using a pre-trained model.  */
object LabelImage {
    fun constructAndExecuteGraphToNormalizeImage(imageBytes: ByteArray?): Tensor<java.lang.Float> {
        Graph().use { g ->
            val b = GraphBuilder(g)
            // Some constants specific to the pre-trained model at:
            // https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip
            //
            // - The model was trained with images scaled to 224x224 pixels.
            // - The colors, represented as R, G, B in 1-byte each were converted to
            //   float using (value - Mean)/Scale.
            val H = 224
            val W = 224
//            val mean = 117f
//            val scale = 1f

            val mean = 117f;
            val scale = 255f;

            // Since the graph is being constructed once per execution here, we can use a constant for the
            // input image. If the graph were to be re-used for multiple input images, a placeholder would
            // have been more appropriate.
            val input = b.constant("Placeholder", imageBytes)
            val output = b.div(
                b.sub(
                    b.resizeBilinear(
                        b.expandDims(
                            b.cast(b.decodeJpeg(input, 3), java.lang.Float::class.java),
                            b.constant("make_batch", 0)
                        ),
                        b.constant("size", intArrayOf(H, W))
                    ),
                    b.constant("mean", mean)
                ),
                b.constant("scale", scale)
            )
            Session(g).use { s ->
                // Generally, there may be multiple output tensors, all of them must be closed to prevent resource leaks.
                return s.runner().fetch(output.op().name()).run()[0].expect(java.lang.Float::class.java)
            }
        }
    }

    fun executeInceptionGraph(graphDef: ByteArray?, image: Tensor<java.lang.Float>): FloatArray {
        Graph().use { g ->
            g.importGraphDef(graphDef!!)
            Session(g).use { s ->
                s.runner().feed("Placeholder", image).fetch("final_result").run()[0].expect(java.lang.Float::class.java).use { result ->
                    val rshape = result.shape()
                    if (result.numDimensions() != 2 || rshape[0] != 1L) {
                        throw RuntimeException(
                            String.format(
                                "Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s",
                                Arrays.toString(rshape)
                            )
                        )
                    }
                    val nlabels = rshape[1].toInt()
                    return result.copyTo(Array(1) { FloatArray(nlabels) })[0]
                }
            }// Generally, there may be multiple output tensors, all of them must be closed to prevent resource leaks.
        }
    }

    fun maxIndex(probabilities: FloatArray): Int {
        var best = 0
        for (i in 1 until probabilities.size) {
            if (probabilities[i] > probabilities[best]) {
                best = i
            }
        }
        return best
    }

    private fun readAllBytesOrExit(path: Path): ByteArray? {
        try {
            return Files.readAllBytes(path)
        } catch (e: IOException) {
            System.err.println("Failed to read [" + path + "]: " + e.message)
            System.exit(1)
        }

        return null
    }

    private fun readAllLinesOrExit(path: Path): List<String>? {
        try {
            return Files.readAllLines(path, Charset.forName("UTF-8"))
        } catch (e: IOException) {
            System.err.println("Failed to read [" + path + "]: " + e.message)
            System.exit(0)
        }

        return null
    }
}