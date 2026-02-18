package com.synapse.social.studioasinc.feature.profile.profile.components

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.system.measureNanoTime

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class QRCodeBenchmarkTest {

    @Test
    fun benchmarkQRCodeGeneration() {
        val content = "https://example.com/profile/testuser"
        val size = 512
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)

        // Warmup
        generateOld(bitMatrix, size)
        generateNew(bitMatrix, size)

        val iterations = 10
        var totalTimeOld = 0L
        var totalTimeNew = 0L

        for (i in 0 until iterations) {
            totalTimeOld += measureNanoTime {
                generateOld(bitMatrix, size)
            }
            totalTimeNew += measureNanoTime {
                generateNew(bitMatrix, size)
            }
        }

        val avgOld = totalTimeOld / iterations
        val avgNew = totalTimeNew / iterations

        println("Average time (Old): ${avgOld / 1_000_000.0} ms")
        println("Average time (New): ${avgNew / 1_000_000.0} ms")
        println("Improvement: ${avgOld.toDouble() / avgNew.toDouble()}x")

        // Verify correctness
        val bitmapOld = generateOld(bitMatrix, size)
        val bitmapNew = generateNew(bitMatrix, size)

        for (x in 0 until size) {
            for (y in 0 until size) {
                assertEquals("Pixel at $x, $y differs", bitmapOld.getPixel(x, y), bitmapNew.getPixel(x, y))
            }
        }
    }

    private fun generateOld(bitMatrix: com.google.zxing.common.BitMatrix, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    private fun generateNew(bitMatrix: com.google.zxing.common.BitMatrix, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            for (x in 0 until size) {
                pixels[y * size + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
        return bitmap
    }
}
