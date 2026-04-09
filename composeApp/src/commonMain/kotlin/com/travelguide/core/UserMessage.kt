package com.travelguide.core

fun Throwable.toUserMessage(defaultMessage: String = "Проверьте подключение к интернету."): String {
    val raw = message?.trim().orEmpty()
    if (raw.isBlank()) return defaultMessage

    val lower = raw.lowercase()
    return when {
        "failed to connect" in lower ||
        "unable to resolve host" in lower ||
        "failed host lookup" in lower ||
        "network is unreachable" in lower ||
        "connection refused" in lower ||
        "socket closed" in lower ||
        "timeout" in lower ||
        "timed out" in lower ||
        "no address associated with hostname" in lower ||
        "software caused connection abort" in lower ||
        "broken pipe" in lower -> "Проверьте подключение к интернету."

        else -> raw
    }
}
