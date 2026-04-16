package app

import io.github.cdimascio.dotenv.dotenv
import network.normalizePeerUrl

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
            val seedPeersPort = (
                dotenv["SEED_PEER_PORT"]
                    ?: dotenv["SEED_PEERS_PORT"]
                    ?: port.toString()
                ).toInt()

            val seedPeersRaw = dotenv["SEED_PEERS"]
            val seedPeers = if (seedPeersRaw.isNullOrBlank()) {
                listOf("")
            } else {
                seedPeersRaw
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .map { peer -> normalizePeerUrl(peer) ?: "http://${peer.trim().trimEnd('/')}:$seedPeersPort" }
                    .distinct()
            }

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