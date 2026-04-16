package network.bootstrap

import kotlinx.coroutines.runBlocking
import network.NodeService
import network.client.PeerClient
import network.dto.ChainResponse
import network.normalizePeerUrl
import network.normalizePeerUrls

class BootstrapService(
    private val peerClient: PeerClient,
    private val nodeService: NodeService,
    private val myUrl: String,
    private val seedPeers: List<String>
) {
    fun bootstrap() = runBlocking {
        val normalizedMyUrl = normalizePeerUrl(myUrl)
        val normalizedSeeds = normalizePeerUrls(seedPeers)

        if (normalizedSeeds.isEmpty()) {
            println("No seed peers configured. Starting standalone.")
            return@runBlocking
        }

        val discoveredPeers = linkedSetOf<String>()
        val responsiveSeeds = mutableListOf<String>()
        val chainCandidates = linkedMapOf<String, ChainResponse>()

        for (seed in normalizedSeeds) {
            try {
                println("Discovering peers from $seed")

                val peersResponse = peerClient.getPeers(seed)
                responsiveSeeds.add(seed)
                discoveredPeers.add(seed)
                discoveredPeers.addAll(normalizePeerUrls(peersResponse.peers))
            } catch (e: Exception) {
                println("Failed to discover peers from $seed: ${e.message}")
            }
        }

        if (responsiveSeeds.isEmpty()) {
            println("Could not bootstrap from any seed. Starting standalone.")
            return@runBlocking
        }

        for (peer in discoveredPeers.filter { it != normalizedMyUrl }) {
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
                nodeService.addPeers(
                    normalizePeerUrls(discoveredPeers + registerResponse.peers + registrationTarget)
                        .filter { it != normalizedMyUrl }
                )
            } catch (e: Exception) {
                println("Failed to register at $registrationTarget: ${e.message}")
                nodeService.addPeers(discoveredPeers.filter { it != normalizedMyUrl })
            }
        } else {
            nodeService.addPeers(discoveredPeers.filter { it != normalizedMyUrl })
        }

        println("Bootstrap complete")
    }
}