package network

import network.dto.BlockDto
import network.dto.ChainResponse
import network.dto.CreateWalletResponse
import network.dto.PeersResponse
import network.dto.RegisterPeerResponse
import network.dto.StatusResponse
import network.dto.TransactionDto
import network.dto.TransactionResponse
import network.results.SubmitBlockResultResult
import network.results.SubmitTransactionResult

interface NodeService {
    fun health(): Map<String, String>
    fun getStatus(): StatusResponse
    fun getChain(): ChainResponse
    fun getPeers(): PeersResponse
    fun registerPeer(url: String): RegisterPeerResponse

    fun replaceChainFromBootstrap(remoteChain: List<BlockDto>)
    fun addPeers(peers: List<String>)
    fun submitTransaction(transactionDto: TransactionDto): SubmitTransactionResult
    fun submitBlock(blockDto: BlockDto): SubmitBlockResultResult

    fun createWallet(): CreateWalletResponse
}