package network

fun normalizePeerUrl(rawUrl: String): String? {
    val trimmed = rawUrl.trim().trimEnd('/')
    if (trimmed.isBlank()) {
        return null
    }

    return when {
        trimmed.contains("://") -> trimmed
        trimmed.count { it == ':' } == 1 && trimmed.substringAfterLast(':').all(Char::isDigit) -> "http://$trimmed"
        else -> null
    }
}

fun normalizePeerUrls(rawUrls: Collection<String>): List<String> {
    return rawUrls.mapNotNull(::normalizePeerUrl).distinct()
}

