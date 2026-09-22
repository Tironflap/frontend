package com.tironflap.frontend.data.hash

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Public-hash style lookup.
 *
 * In production this would load No-Intro / Redump / libretro DAT files
 * or query an online API. For now we ship a small built-in map and
 * treat unknown hashes as "unverified" while still applying junk filters.
 *
 * CRC32 keys are uppercase hex.
 */
@Singleton
class HashDatabase @Inject constructor() {

    data class HashEntry(
        val name: String,
        val systemId: String,
        val region: String? = null
    )

    // Small sample of well-known CRCs – expand later via DAT import
    private val known: Map<String, HashEntry> = mapOf(
        // These are placeholders; real DATs will replace this
    )

    fun lookup(crc32: String?): HashEntry? {
        if (crc32.isNullOrBlank()) return null
        return known[crc32.uppercase()]
    }

    fun isKnown(crc32: String?): Boolean = lookup(crc32) != null
}
