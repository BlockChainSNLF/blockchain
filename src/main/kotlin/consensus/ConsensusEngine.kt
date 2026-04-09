package consensus

import common.NetworkConfig
import mempool.MempoolManager
import network.broadcast.BroadcastService
import network.dto.BlockDto
import transactions.TransactionMapper
import validators.block.BlockValidator
import validators.block.InvalidBlock
import validators.block.ValidBlock
import validators.transactions.TransactionValidator

class ConsensusEngine(
    private val miner: Miner,
    private val transactionValidator: TransactionValidator,
    private val mempoolManager: MempoolManager,
    private val broadcastService: BroadcastService,
    private val getPeers: () -> List<String>,
    initialChain: List<BlockDto> = emptyList()
) {

    // ── estado interno ────────────────────────────────────────────────────────

    private var chain: List<BlockDto> = initialChain
    private var balances: Map<String, Long> = computeBalances(initialChain)

    // ── lectura de estado ─────────────────────────────────────────────────────

    fun getChain(): List<BlockDto> = chain
    fun latestBlock(): BlockDto? = chain.lastOrNull()
    fun getBalances(): Map<String, Long> = balances
    fun chainLength(): Int = chain.size
    fun latestHash(): String = chain.lastOrNull()?.hash ?: "GENESIS"

    // ── genesis ───────────────────────────────────────────────────────────────

    fun mineGenesisBlock() {
        if (chain.isNotEmpty()) return
        val genesis = miner.mine(miner.buildGenesis())
        chain = listOf(genesis)
        balances = emptyMap()
        println("Genesis mined: index=${genesis.index} hash=${genesis.hash}")
    }

    // ── appendBlock: recibe un bloque de un peer ──────────────────────────────

    fun appendBlock(block: BlockDto): AppendBlockResult {
        return when (val result = validate(block, latestBlock())) {
            is InvalidBlock -> RejectedBlock(result.reasons)
            ValidBlock -> {
                chain = chain + block
                balances = computeBalances(chain)
                cleanMempool(block)
                broadcastService.broadcastBlock(getPeers(), block)
                Appended(block = block, chainLength = chain.size)
            }
        }
    }

    // ── replaceChain: longest valid chain ────────────────────────────────────

    fun replaceChain(remoteChain: List<BlockDto>): ReplaceChainResult {
        if (remoteChain.size <= chain.size) return IgnoredTooShort

        val errors = validateFullChain(remoteChain)
        if (errors.isNotEmpty()) return InvalidChain(errors)

        chain = remoteChain
        balances = computeBalances(chain)
        return Replaced
    }

    // ── mineBlock: manual o automático ───────────────────────────────────────

    fun mineBlock(trigger: String): MineBlockResult {
        val pendingTransfers = mempoolManager.get().all()
            .map { TransactionMapper.toDto(it) }

        val candidate = miner.buildCandidate(
            previousBlock = latestBlock(),
            pendingTransfers = pendingTransfers
        )
        val minedBlock = miner.mine(candidate)

        return when (val validation = validate(minedBlock, latestBlock())) {
            is InvalidBlock -> MiningFailed(
                "Mined block failed self-validation: ${validation.reasons.joinToString("; ")}"
            )
            ValidBlock -> {
                chain = chain + minedBlock
                balances = computeBalances(chain)
                cleanMempool(minedBlock)
                broadcastService.broadcastBlock(getPeers(), minedBlock)
                Mined(block = minedBlock, trigger = trigger)
            }
        }
    }

    // ── bootstrap ─────────────────────────────────────────────────────────────

    fun initFromBootstrap(remoteChain: List<BlockDto>) {
        if (remoteChain.size > chain.size) {
            chain = remoteChain
            balances = computeBalances(chain)
        }
    }

    // ── privados ──────────────────────────────────────────────────────────────

    private fun validate(block: BlockDto, previous: BlockDto?) =
        BlockValidator.validate(
            block = block,
            previousBlock = previous,
            transactionValidator = transactionValidator,
            balances = balances
        )

    private fun validateFullChain(remoteChain: List<BlockDto>): List<String> {
        val errors = mutableListOf<String>()
        var running: Map<String, Long> = emptyMap()
        remoteChain.forEachIndexed { i, block ->
            val previous = if (i == 0) null else remoteChain[i - 1]
            when (val r = BlockValidator.validate(block, previous, transactionValidator, running)) {
                is InvalidBlock -> errors.addAll(r.reasons.map { "block[$i]: $it" })
                ValidBlock -> running = applyBlock(running, block)
            }
        }
        return errors
    }

    private fun cleanMempool(block: BlockDto) {
        mempoolManager.removeByIds(block.transactions.map { it.id })
    }

    private fun computeBalances(chain: List<BlockDto>): Map<String, Long> {
        var b: Map<String, Long> = emptyMap()
        chain.forEach { b = applyBlock(b, it) }
        return b
    }

    private fun applyBlock(current: Map<String, Long>, block: BlockDto): Map<String, Long> {
        val updated = current.toMutableMap()
        block.transactions.forEach { tx ->
            when (tx.type) {
                "COINBASE"  -> updated[tx.to]   = (updated[tx.to]   ?: 0L) + tx.amount
                "TRANSFER"  -> {
                    updated[tx.from] = (updated[tx.from] ?: 0L) - tx.amount
                    updated[tx.to]   = (updated[tx.to]   ?: 0L) + tx.amount
                }
            }
        }
        return updated
    }
}