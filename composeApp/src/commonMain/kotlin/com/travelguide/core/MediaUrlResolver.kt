package com.travelguide.core

object MediaUrlResolver {
    fun resolve(raw: String?): String? {
        val value = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null
        if (value.startsWith("http://") || value.startsWith("https://") || value.startsWith("content://")) {
            return value
        }

        val normalized = when {
            value.startsWith("/") -> value
            else -> "/$value"
        }

        return NetworkConfig.BASE_URL.trimEnd('/') + normalized
    }
}
