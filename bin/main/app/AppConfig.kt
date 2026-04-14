package app

import io.github.cdimascio.dotenv.dotenv

data class AppConfig(
    val bindHost: String,
    val advertisedHost: String,
    val port: Int,
    val seedPeers: List<String>,
    val seedPeersPort: Int,
    val address: String,
    val publicKey: String
) {
    val baseUrl: String
        get() = "http://$advertisedHost:$port"

    companion object {
        fun fromEnv(): AppConfig {
            val dotenv = dotenv()

            val bindHost = dotenv["BIND_HOST"] ?: "0.0.0.0"
            val advertisedHost = dotenv["HOST"] ?: "127.0.0.1"
            val port = (dotenv["PORT"] ?: "8080").toInt()
            val seedPeersPort = (dotenv["SEED_PEERS_PORT"] ?: port.toString()).toInt()

            val seedPeers = dotenv["SEED_PEERS"]
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?.map {
                    val normalized = it.trim().trimEnd('/')

                    when {
                        normalized.contains("://") -> normalized
                        normalized.count { character -> character == ':' } == 1 && normalized.substringAfterLast(':').all(Char::isDigit) -> "http://$normalized"
                        else -> "http://$normalized:$seedPeersPort"
                    }
                }
                ?: emptyList()

            val address = dotenv["NODE_ADDRESS"] ?: "0xABC123"
            val publicKey = dotenv["NODE_PUBLIC_KEY"] ?: "PUBLIC_KEY"

            return AppConfig(
                bindHost = bindHost,
                advertisedHost = advertisedHost,
                port = port,
                seedPeers = seedPeers,
                seedPeersPort = seedPeersPort,
                address = address,
                publicKey = publicKey
            )
        }
    }
}