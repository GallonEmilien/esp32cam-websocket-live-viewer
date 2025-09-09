package fr.gallonemilien.websocketcam.utils

import java.net.URI

object UriUtils {
    fun getQueryParams(uri: URI?): Map<String, String> {
        if (uri?.query.isNullOrEmpty()) return emptyMap()
        return uri.query.split("&")
            .mapNotNull {
                val parts = it.split("=")
                if (parts.isNotEmpty()) parts[0] to (parts.getOrNull(1) ?: "") else null
            }
            .toMap()
    }
}