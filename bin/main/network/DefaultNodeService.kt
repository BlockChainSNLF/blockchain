package network

import mempool.Added
import mempool.Duplicate
import mempool.MempoolManager
import mempool.Rejected
import network.dto.BlockDto
import network.dto.ChainDataDto
import network.dto.ChainResponse
import network.dto.CreateWalletResponse
import network.dto.NodeDto
import network.dto.PeersCount
import network.dto.PeersResponse
import network.dto.RegisterPeerResponse
import network.dto.StatusResponse
import network.dto.TransactionDto
import network.dto.WalletDto
import network.results.Accepted
import network.results.RejectedSubmission
import network.results.SubmitTransactionResult
import transactions.TransactionMapper
import wallets.factory.EthersWalletFactory

class DefaultNodeService(
    private val baseUrl: String,
    private val address: String,
    private val publicKey: String,
    private val mempoolManager: MempoolManager,
    initialChain: List<BlockDto> = emptyList(),
    initialPeers: Set<String> = emptySet()
) : NodeService {

    private var chain: List<BlockDto> = initialChain
    private val peers: MutableSet<String> = initialPeers.toMutableSet()

    override fun health(): Map<String, String> {
        return mapOf("status" to "ok")
    }

    override fun getStatus(): StatusResponse {
        return StatusResponse(
            status = "ok",
            node = NodeDto(
                url = baseUrl,
                address = address,
                publicKey = publicKey
            ),
            chain = ChainDataDto(
                length = chain.size,
                latestHash = chain.lastOrNull()?.hash ?: "GENESIS"
            ),
            peers = PeersCount(
                count = peers.size
            )
        )
    }

    override fun getChain(): ChainResponse {
        return ChainResponse(
            status = "ok",
            chain = chain,
            length = chain.size
        )
    }

    override fun getPeers(): PeersResponse {
        return PeersResponse(
            status = "ok",
            peers = peers.toList(),
            count = peers.size
        )
    }

    override fun registerPeer(url: String): RegisterPeerResponse {
        if (url != baseUrl) {
            peers.add(url)
        }

        return RegisterPeerResponse(
            status = "ok",
            registered = url,
            peers = (peers + baseUrl).toList()
        )
    }

    override fun replaceChainFromBootstrap(remoteChain: List<BlockDto>) {
        if (remoteChain.size > chain.size) {
            chain = remoteChain
        }
    }

    override fun addPeers(peers: List<String>) {
        this.peers.addAll(peers.filter { it != baseUrl })
    }

    override fun submitTransaction(transactionDto: TransactionDto): SubmitTransactionResult {


        val transaction = try {
            TransactionMapper.fromDto(transactionDto)
        } catch (_: Exception) {
            return RejectedSubmission(
                code = "INVALID_TRANSACTION",
                message = "Could not parse transaction"
            )
        }


        return when (val result = mempoolManager.addTransaction(transaction)) {
            is Added -> Accepted(transaction.getId())

            Duplicate -> RejectedSubmission(
                "DUPLICATE_TRANSACTION",
                "Transaction already exists in mempool"
            )


            is Rejected -> RejectedSubmission(
                "INVALID_TRANSACTION",
                result.validationResult.messages.joinToString("; ")
            )

        }
    }

    override fun createWallet(): CreateWalletResponse {
        val wallet = EthersWalletFactory.createWallet()

        return CreateWalletResponse(
            status = "ok",
            wallet = WalletDto(
                privateKey = wallet.privateKey,
                publicKey = wallet.publicKey,
                address = wallet.address
            )
        )
    }
}