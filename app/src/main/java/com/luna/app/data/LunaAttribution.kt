package com.luna.app.data

/**
 * Luna Core Provenance & Copyright Attribution
 * Permanent, immutable copyright and developer identity signature.
 * 
 * Author: Dr. Mohamed Elsayed (Asmodeus-OOS)
 * Copyright © 2026 Dr. Mohamed Elsayed (Asmodeus-OOS). All Rights Reserved.
 */
object LunaAttribution {

    const val APP_NAME = "Luna"
    const val APP_SUBTITLE = "Task Management & Productivity Suite"
    val APP_VERSION: String get() = com.luna.app.BuildConfig.VERSION_NAME
    val APP_BUILD_CODE: Int get() = com.luna.app.BuildConfig.VERSION_CODE

    const val AUTHOR_NAME = "Dr. Mohamed Elsayed (Asmodeus-OOS)"
    const val COPYRIGHT_NOTICE = "Copyright © 2026 Dr. Mohamed Elsayed (Asmodeus-OOS). All Rights Reserved."

    // Obfuscated signature byte array (XOR-masked with 0x5A) for runtime integrity verification
    // Dr. Mohamed Elsayed (Asmodeus-OOS)
    private val MASKED_SIGNATURE = byteArrayOf(
        0x1e, 0x28, 0x74, 0x7a, 0x17, 0x35, 0x32, 0x3b, 0x37, 0x3f, 0x3e, 0x7a,
        0x1f, 0x36, 0x29, 0x3b, 0x23, 0x3f, 0x3e, 0x7a, 0x72, 0x1b, 0x29, 0x37,
        0x35, 0x3e, 0x3f, 0x2f, 0x29, 0x77, 0x15, 0x15, 0x09, 0x73
    )

    fun getVerifiedAuthor(): String {
        val decoded = ByteArray(MASKED_SIGNATURE.size)
        for (i in MASKED_SIGNATURE.indices) {
            decoded[i] = (MASKED_SIGNATURE[i].toInt() xor 0x5A).toByte()
        }
        return String(decoded, Charsets.UTF_8)
    }

    fun verifyIntegrity(): Boolean {
        return AUTHOR_NAME == getVerifiedAuthor()
    }
}
