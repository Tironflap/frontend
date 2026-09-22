package com.tironflap.frontend.data.hash

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStream
import java.util.zip.CRC32
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RomHasher @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Compute CRC32 of a file (standard for No-Intro / Redump style DBs).
     * Returns uppercase 8-char hex, or null on failure.
     */
    fun crc32(uri: Uri, maxBytes: Long = 64L * 1024 * 1024): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                crc32(input, maxBytes)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun crc32(input: InputStream, maxBytes: Long = 64L * 1024 * 1024): String {
        val crc = CRC32()
        val buffer = ByteArray(8192)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read <= 0) break
            crc.update(buffer, 0, read)
            total += read
            if (total >= maxBytes) break // avoid hashing huge ISOs fully on first pass
        }
        return "%08X".format(crc.value)
    }
}
