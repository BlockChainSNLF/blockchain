package network

import consensus.Appended
import consensus.ConsensusEngine
import consensus.Mined
import consensus.MiningFailed
import consensus.RejectedBlock
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
import network.results.BlockAccepted
import network.results.BlockRejected
import network.results.MineFailed
import network.results.MineResult
import network.results.MinedSuccessfully
import network.results.ReceiveBlockResult
import network.results.RejectedSubmission
import network.results.SubmitTransactionResult
import transactions.TransactionMapper
import wallets.factory.EthersWalletFactory
import common.NetworkConfig

class DefaultNodeService(
    private val baseUrl: String,
    private val address: String,
    private val publicKey: String,
    private val mempoolManager: MempoolManager,
    private val consensusEngine: ConsensusEngine,
    initialPeers: Set<String> = emptySet()
) : NodeService {

    private val peers: MutableSet<String> = initialPeers.toMutableSet()

    override fun health(): Map<String, String> = mapOf("status" to "ok")

    override fun getStatus(): StatusResponse = StatusResponse(
        status = "ok",
        node = NodeDto(url = baseUrl, address = address, publicKey = publicKey),
        chain = ChainDataDto(
            length = consensusEngine.chainLength(),
            latestHash = consensusEngine.latestHash()
        ),
        peers = PeersCount(count = peers.size)
    )

    override fun getChain(): ChainResponse = ChainResponse(
        status = "ok",
        chain = consensusEngine.getChain(),
        length = consensusEngine.chainLength()
    )

    override fun getPeers(): PeersResponse = PeersResponse(
        status = "ok",
        peers = peers.toList(),
        count = peers.size
    )

    override fun registerPeer(url: String): RegisterPeerResponse {
        if (url != baseUrl) peers.add(url)
        return RegisterPeerResponse(
            status = "ok",
            registered = url,
            peers = (peers + baseUrl).toList()
        )
    }

    override fun replaceChainFromBootstrap(remoteChain: List<BlockDto>) {
        consensusEngine.initFromBootstrap(remoteChain)
    }

    override fun addPeers(peers: List<String>) {
        this.peers.addAll(peers.filter { it != baseUrl })
    }

    override fun submitTransaction(transactionDto: TransactionDto): SubmitTransactionResult {
        val transaction = try {
            TransactionMapper.fromDto(transactionDto)
        } catch (_: Exception) {
            return RejectedSubmission("INVALID_TRANSACTION", "Could not parse transaction")
        }

        return when (val result = mempoolManager.addTransaction(transaction)) {
            is Added -> {
                // Trigger automático al llegar al threshold
                if (mempoolManager.get().all().size >= NetworkConfig.AUTO_MINE_THRESHOLD) {
                    mine(trigger = "auto")
                }
                Accepted(transaction.getId())
            }
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

    override fun receiveBlock(blockDto: BlockDto): ReceiveBlockResult =
        when (val result = consensusEngine.appendBlock(blockDto)) {
            is Appended     -> BlockAccepted(result.chainLength)
            is RejectedBlock -> BlockRejected(result.reasons)
        }

    override fun mine(trigger: String): MineResult =
        when (val result = consensusEngine.mineBlock(trigger)) {
            is Mined        -> MinedSuccessfully(block = result.block, trigger = result.trigger)
            is MiningFailed -> MineFailed(result.reason)
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
