package network.broadcast

import kotlinx.coroutines.runBlocking
import network.normalizePeerUrls
import network.client.PeerClient
import network.dto.BlockDto
import network.dto.TransactionDto

class BroadcastService(
    private val peerClient: PeerClient
) {
    fun broadcastBlock(peers: List<String>, block: BlockDto) = runBlocking {
        normalizePeerUrls(peers).forEach { peer ->
            runCatching {
                peerClient.sendBlock(peer, block)
            }.onFailure { error ->
                println("Failed to broadcast block ${block.hash} to $peer: ${error.message}")
            }
        }
    }

    fun broadcastTransaction(peers: List<String>, tx: TransactionDto) = runBlocking {
        normalizePeerUrls(peers).forEach { peer ->
            runCatching {
                peerClient.sendTransaction(peer, tx)
            }.onFailure { error ->
                println("Failed to broadcast transaction ${tx.id} to $peer: ${error.message}")
            }
        }
    }
}