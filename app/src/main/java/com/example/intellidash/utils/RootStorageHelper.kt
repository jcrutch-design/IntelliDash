package com.example.intellidash.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.InputStream

object RootStorageHelper {
    private const val TAG = "RootStorageHelper"

    /**
     * Scans for media files in common locations using root access.
     * This is designed to be exhaustive and start automatically.
     */
    fun scanMediaFiles(): List<String> {
        val paths = listOf(
            "/sdcard/DCIM",
            "/sdcard/Pictures",
            "/sdcard/Download",
            "/sdcard/Movies",
            "/sdcard/Documents",
            "/sdcard/Android/media/com.whatsapp",
            "/sdcard/Android/media/org.telegram.messenger",
            "/sdcard/Telegram",
            "/sdcard/WhatsApp/Media"
        )
        // Use a single find command for efficiency. 
        // 2>/dev/null hides permission denied errors for specific subfolders.
        val command = "find ${paths.joinToString(" ")} -type f \\( -iname '*.jpg' -o -iname '*.jpeg' -o -iname '*.png' -o -iname '*.webp' \\) 2>/dev/null"
        val output = executeRootCommand(command)
        
        if (output.isEmpty()) {
            Log.d(TAG, "No media files found during scan.")
            return emptyList()
        }
        
        return output.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    /**
     * Reads a media file using root access and downscales it to 512x512 for inference.
     */
    fun readMediaFile(path: String): Bitmap? {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat \"$path\""))
            val inputStream: InputStream = process.inputStream
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            // We can't easily reset an InputStream from a process, so we might need to read it once or use inSampleSize carefully
            // However, cat output can be decoded directly.
            // But since we want OOM protection, let's decode to a smaller size if possible.
            // For root access 'cat', we might just read everything and then scale.
            
            val bytes = inputStream.readBytes()
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            
            bitmap?.let {
                val scaled = Bitmap.createScaledBitmap(it, 512, 512, true)
                if (scaled != it) {
                    it.recycle()
                }
                scaled
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read media file at $path", e)
            null
        }
    }

    private fun executeRootCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val result = process.inputStream.bufferedReader().readText()
            process.waitFor()
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error executing root command: $command", e)
            ""
        }
    }
}
