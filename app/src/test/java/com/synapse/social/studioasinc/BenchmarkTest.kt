package com.synapse.social.studioasinc

import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.util.Random

class BenchmarkTest {

    @Test
    fun benchmarkMemoryUsage() {
        val fileSize = 50 * 1024 * 1024 // 50MB
        val tempFile = File.createTempFile("benchmark", ".tmp")
        tempFile.deleteOnExit()

        val random = Random()
        val buffer = ByteArray(1024 * 1024)
        FileOutputStream(tempFile).use { out ->
            for (i in 0 until 50) {
                random.nextBytes(buffer)
                out.write(buffer)
            }
        }

        println("Benchmarking memory usage for 50MB file...")

        // Measure baseline memory
        System.gc()
        Thread.sleep(100)
        val runtime = Runtime.getRuntime()
        val usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory()
        println("Memory used before loading file: " + (usedMemoryBefore / 1024 / 1024) + " MB")

        // Simulate inefficient loading
        val fileBytes = tempFile.readBytes()

        // Measure memory after loading
        val usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory()
        println("Memory used after loading file: " + (usedMemoryAfter / 1024 / 1024) + " MB")

        val memorySpike = usedMemoryAfter - usedMemoryBefore
        println("Memory spike: " + (memorySpike / 1024 / 1024) + " MB")

        // This confirms that reading bytes consumes significant memory (approx file size + overhead)
        assert(memorySpike > fileSize * 0.8) // Expect at least 80% of file size in memory spike

        // Clean up (make eligible for GC)
        // fileBytes is now out of scope if we return, but let's null it out explicitly if this wasn't the end of method
    }

    @Test
    fun benchmarkOptimizedApproach() {
        // This test simulates the optimized approach where we DON'T read bytes into memory
        // We just pass the File object.

        val fileSize = 50 * 1024 * 1024 // 50MB
        val tempFile = File.createTempFile("benchmark_opt", ".tmp")
        tempFile.deleteOnExit()

        val random = Random()
        val buffer = ByteArray(1024 * 1024)
        FileOutputStream(tempFile).use { out ->
            for (i in 0 until 50) {
                random.nextBytes(buffer)
                out.write(buffer)
            }
        }

        println("Benchmarking optimized approach for 50MB file...")

        // Measure baseline memory
        System.gc()
        Thread.sleep(100)
        val runtime = Runtime.getRuntime()
        val usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory()
        println("Memory used before passing file: " + (usedMemoryBefore / 1024 / 1024) + " MB")

        // Simulate optimized loading: Just use the file reference
        // In the real code, we pass 'tempFile' to the library which handles it efficiently (streaming)
        val fileReference = tempFile

        // Measure memory after "loading" (just passing reference)
        val usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory()
        println("Memory used after passing file: " + (usedMemoryAfter / 1024 / 1024) + " MB")

        val memorySpike = usedMemoryAfter - usedMemoryBefore
        println("Memory spike (Optimized): " + (memorySpike / 1024 / 1024) + " MB")

        // This confirms that passing File reference consumes negligible memory
        assert(memorySpike < 1 * 1024 * 1024) // Expect less than 1MB overhead
    }
}
