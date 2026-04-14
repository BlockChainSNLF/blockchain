package network.bootstrap

import kotlinx.coroutines.runBlocking
import network.NodeService
import network.client.PeerClient
import network.dto.ChainResponse

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

        val discoveredPeers = linkedSetOf<String>()
        val responsiveSeeds = mutableListOf<String>()
        val chainCandidates = linkedMapOf<String, ChainResponse>()

        for (seed in seedPeers) {
            try {
                println("Discovering peers from $seed")

                val peersResponse = peerClient.getPeers(seed)
                responsiveSeeds.add(seed)
                discoveredPeers.add(seed)
                discoveredPeers.addAll(peersResponse.peers)
            } catch (e: Exception) {
                println("Failed to discover peers from $seed: ${e.message}")
            }
        }

        if (responsiveSeeds.isEmpty()) {
            println("Could not bootstrap from any seed. Starting standalone.")
            return@runBlocking
        }

        for (peer in discoveredPeers.filter { it != myUrl }) {
            try {
                println("Fetching chain from $peer")
                chainCandidates[peer] = peerClient.getChain(peer)
            } catch (e: Exception) {
                println("Failed to fetch chain from $peer: ${e.message}")
            }
        }

        chainCandidates.maxByOrNull { it.value.length }?.let { (peer, chainResponse) ->
            if (!nodeService.replaceChainFromBootstrap(chainResponse.chain)) {
                println("Remote chain from $peer was not accepted")
            }
        }

        val registrationTarget = responsiveSeeds.firstOrNull()
        if (registrationTarget != null) {
            try {
                val registerResponse = peerClient.registerAtPeer(registrationTarget, myUrl)
                nodeService.addPeers((discoveredPeers + registerResponse.peers + registrationTarget).filter { it != myUrl })
            } catch (e: Exception) {
                println("Failed to register at $registrationTarget: ${e.message}")
                nodeService.addPeers(discoveredPeers.filter { it != myUrl })
            }
        } else {
            nodeService.addPeers(discoveredPeers.filter { it != myUrl })
        }

        println("Bootstrap complete")
    }
}