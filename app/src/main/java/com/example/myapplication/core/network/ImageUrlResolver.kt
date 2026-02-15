package com.example.myapplication.core.network

object ImageUrlResolver {
    private const val EMULATOR_BASE_URL = "http://10.0.2.2:8080"

    fun resolve(url: String?): String? {
        if (url.isNullOrBlank()) return null

        val raw = url.trim()
        if (raw.startsWith("/")) {
            return "$EMULATOR_BASE_URL$raw"
        }
        if (raw.startsWith("uploads/")) {
            return "$EMULATOR_BASE_URL/$raw"
        }

        var normalized = raw
            .replace("://localhost:", "://10.0.2.2:")
            .replace("://127.0.0.1:", "://10.0.2.2:")

        // Temporary strategy: force Qiniu CDN links to HTTP until custom domain + proper TLS is ready.
        if (normalized.startsWith("https://") && normalized.contains(".clouddn.com")) {
            normalized = "http://" + normalized.removePrefix("https://")
        }

        return normalized
    }
}
