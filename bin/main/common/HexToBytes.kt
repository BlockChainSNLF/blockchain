package common

fun hexToBytes(hex: String): ByteArray {
        val cleanHex = hex.removePrefix("0x")
        require(cleanHex.length % 2 == 0) { "Invalid hex string" }

        return cleanHex.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
