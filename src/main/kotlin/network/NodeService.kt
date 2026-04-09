package network

import network.dto.BlockDto
import network.dto.ChainResponse
import network.dto.CreateWalletResponse
import network.dto.PeersResponse
import network.dto.RegisterPeerResponse
import network.dto.StatusResponse
import network.dto.TransactionDto
import network.results.MineResult
import network.results.ReceiveBlockResult
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
    fun receiveBlock(blockDto: BlockDto): ReceiveBlockResult
    fun mine(trigger: String = "manual"): MineResult

    fun createWallet(): CreateWalletResponse
}
