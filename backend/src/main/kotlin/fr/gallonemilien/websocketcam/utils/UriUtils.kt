package fr.gallonemilien.websocketcam.utils

import java.net.URI

object UriUtils {
    fun getQueryParams(uri: URI?): Map<String, String> {
        val query = uri?.query ?: return emptyMap()
        return query.split("&")
            .mapNotNull {
                val parts = it.split("=", limit = 2)
                if (parts.isNotEmpty()) {
                    parts[0] to (parts.getOrNull(1) ?: "")
                } else {
                    null
                }
            }
            .toMap()
    }
}