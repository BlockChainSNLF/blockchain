package network.broadcast

import kotlinx.coroutines.runBlocking
import network.client.PeerClient
import network.dto.BlockDto
import network.dto.TransactionDto

class BroadcastService(
    private val peerClient: PeerClient
) {
    fun broadcastBlock(peers: List<String>, block: BlockDto) = runBlocking {
        peers.forEach { peer ->
            runCatching {
                peerClient.sendBlock(peer, block)
            }
        }
    }

    fun broadcastTransaction(peers: List<String>, tx: TransactionDto) = runBlocking {
        peers.forEach { peer ->
            runCatching {
                peerClient.sendTransaction(peer, tx)
            }
        }
    }
}