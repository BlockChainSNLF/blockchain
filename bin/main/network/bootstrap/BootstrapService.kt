package network.bootstrap

import kotlinx.coroutines.runBlocking
import network.NodeService
import network.client.PeerClient

class BootstrapService(
    private val peerClient: PeerClient,
    private val nodeService: NodeService,
    private val myUrl: String,
    private val seedPeers: List<String>
) {
    fun bootstrap() = runBlocking {
        if (seedPeers.isEmpty()) {
            println("No seed peers configured. Starting standalone.")
            return@runBlocking
        }

        for (seed in seedPeers) {
            try {
                println("Trying seed $seed")

                val status = peerClient.getStatus(seed)
                println("Seed reachable: ${status.node.url}")

                val remoteChain = peerClient.getChain(seed)
                nodeService.replaceChainFromBootstrap(remoteChain.chain)

                val registerResponse = peerClient.registerAtPeer(seed, myUrl)
                nodeService.addPeers(registerResponse.peers + seed)

                println("Bootstrap complete via $seed")
                return@runBlocking
            } catch (e: Exception) {
                println("Failed to bootstrap from $seed: ${e.message}")
            }
        }

        println("Could not bootstrap from any seed. Starting standalone.")
    }
}