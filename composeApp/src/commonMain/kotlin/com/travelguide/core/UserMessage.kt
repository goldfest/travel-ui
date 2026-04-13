package com.travelguide.core

fun Throwable.toUserMessage(defaultMessage: String = "Проверьте подключение к интернету."): String {
    val raw = message?.trim().orEmpty()
    if (raw.isBlank()) return defaultMessage

    return when {
        isServerUnavailableMessage(raw) -> "Сервер недоступен"
        isInternetUnavailableMessage(raw) -> "Проверьте подключение к интернету."
        else -> raw
    }
}

fun Throwable.isInternetUnavailableIssue(): Boolean = isInternetUnavailableMessage(message)

fun Throwable.isServerUnavailableIssue(): Boolean = isServerUnavailableMessage(message)

private fun isInternetUnavailableMessage(message: String?): Boolean {
    val lower = message?.trim()?.lowercase().orEmpty()
    if (lower.isBlank()) return false

    return "unable to resolve host" in lower ||
        "failed host lookup" in lower ||
        "network is unreachable" in lower ||
        "no address associated with hostname" in lower ||
        "host lookup" in lower ||
        "failed to connect to" in lower && "after" in lower ||
        "software caused connection abort" in lower ||
        "broken pipe" in lower ||
        "socket closed" in lower
}

private fun isServerUnavailableMessage(message: String?): Boolean {
    val lower = message?.trim()?.lowercase().orEmpty()
    if (lower.isBlank()) return false

    return "connection refused" in lower ||
        "connect timed out" in lower ||
        "timeout" in lower ||
        "timed out" in lower ||
        "failed to connect to" in lower ||
        "http 500" in lower ||
        "http 502" in lower ||
        "http 503" in lower ||
        "http 504" in lower ||
        "service unavailable" in lower ||
        "bad gateway" in lower ||
        "gateway timeout" in lower
}
