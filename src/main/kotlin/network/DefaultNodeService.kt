package network

import block.BlockMapper
import chain.BlockAppended
import chain.BlockRejected
import chain.ChainManager
import chain.ChainReplaced
import mempool.Added
import mempool.Duplicate
import mempool.MempoolManager
import mempool.Rejected
import miner.MineResult
import miner.MinedBlock
import miner.MinerService
import network.broadcast.BroadcastService
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
import network.results.AcceptedBlock
import network.results.RejectedBlockSubmission
import network.results.RejectedSubmission
import network.results.SubmitBlockResultResult
import network.results.SubmitTransactionResult
import transactions.TransactionMapper
import wallets.factory.EthersWalletFactory
import validators.block.BlockValidator
import validators.transactions.TransactionValidator

private fun parseInitialChainOrNull(initialChain: List<BlockDto>): List<block.Block>? {
    val parsed = mutableListOf<block.Block>()
    initialChain.forEach { dto ->
        val mapped = BlockMapper.fromDto(dto) ?: return null
        parsed.add(mapped)
    }

    return parsed
}

class DefaultNodeService(
    private val baseUrl: String,
    private val address: String,
    private val publicKey: String,
    transactionValidator: TransactionValidator,
    private val mempoolManager: MempoolManager,
    private val broadcastService: BroadcastService,
    initialChain: List<BlockDto> = emptyList(),
    initialPeers: Set<String> = emptySet()
) : NodeService {

    private val blockValidator = BlockValidator(transactionValidator)
    private val chainManager = ChainManager(
        blockValidator = blockValidator,
        initialChain = initialChain.takeIf { it.isNotEmpty() }?.let(::parseInitialChainOrNull)
    )
    private val minerService = MinerService(
        chainManager = chainManager,
        mempoolManager = mempoolManager,
        minerAddress = address,
    )
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
                length = chainManager.all().size,
                latestHash = chainManager.latest()?.getHash() ?: "GENESIS"
            ),
            peers = PeersCount(
                count = peers.size
            )
        )
    }

    override fun getChain(): ChainResponse {
        return ChainResponse(
            status = "ok",
            chain = chainManager.all().map(BlockMapper::toDto),
            length = chainManager.all().size
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

    override fun replaceChainFromBootstrap(remoteChain: List<BlockDto>): Boolean {
        val candidate = parseInitialChainOrNull(remoteChain) ?: run {
            return false
        }

        return when (chainManager.replaceIfLongerValid(candidate)) {
            is ChainReplaced -> {
                val txIds = candidate.flatMap { it.getTransactions().map { transaction -> transaction.getId() } }
                if (txIds.isNotEmpty()) {
                    mempoolManager.removeByIds(txIds)
                }

                true
            }

            else -> false
        }
    }

    override fun addPeers(peers: List<String>) {
        this.peers.addAll(peers.filter { it != baseUrl })
    }

    override fun submitTransaction(transactionDto: TransactionDto): SubmitTransactionResult {
        if (transactionDto.type.uppercase() != "TRANSFER") {
            return RejectedSubmission(
                code = "INVALID_TRANSACTION",
                message = "Only TRANSFER transactions are accepted in /transactions"
            )
        }

        val transaction = TransactionMapper.fromDto(transactionDto) ?: run {
            return RejectedSubmission(
                code = "INVALID_TRANSACTION",
                message = "Could not parse transaction"
            )
        }

        return when (val result = mempoolManager.addTransaction(transaction)) {
            is Added -> {
                broadcastService.broadcastTransaction(
                    peers = peers.filter { it != baseUrl },
                    tx = transactionDto
                )

                val autoMineResult = minerService.tryAutoMine()
                if (autoMineResult is MinedBlock) {
                    broadcastService.broadcastBlock(
                        peers = peers.filter { it != baseUrl },
                        block = BlockMapper.toDto(autoMineResult.block)
                    )
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

    override fun submitBlock(blockDto: BlockDto): SubmitBlockResultResult {
        val block = BlockMapper.fromDto(blockDto) ?: run {
            return RejectedBlockSubmission(
                code = "INVALID_BLOCK",
                message = "Could not parse block"
            )
        }

        if (chainManager.all().any { it.getHash() == block.getHash() }) {
            return RejectedBlockSubmission(
                code = "DUPLICATE_BLOCK",
                message = "Block already exists in chain"
            )
        }

        return when (val result = chainManager.addBlock(block)) {
            is BlockAppended -> {
                mempoolManager.removeByIds(block.getTransactions().map { it.getId() })
                broadcastService.broadcastBlock(
                    peers = peers.filter { it != baseUrl },
                    block = blockDto
                )
                AcceptedBlock(chainLength = result.chainLength)
            }

            is BlockRejected -> RejectedBlockSubmission(
                code = "INVALID_BLOCK",
                message = result.errors.joinToString("; ")
            )

        }
    }

    override fun mine(trigger: String): MineResult {
        return when (trigger) {
            "auto" -> minerService.tryAutoMine()
            else -> minerService.mineManual()
        }
    }
}