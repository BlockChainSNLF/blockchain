package miner

import app.NetworkParams
import block.Block
import block.BlockHashing
import block.PreBlock
import chain.BlockAppended
import chain.BlockRejected
import chain.ChainManager
import mempool.MempoolManager
import transactions.CoinbaseTransaction
import transactions.Transaction
import java.util.UUID

class MinerService(
    private val chainManager: ChainManager,
    private val mempoolManager: MempoolManager,
    private val minerAddress: String,
    private val nowProvider: () -> Long = { System.currentTimeMillis() },
    private val reward: Int = NetworkParams.BLOCK_REWARD,
    private val difficultyPrefix: String = NetworkParams.DIFFICULTY_PREFIX,
    private val autoMineThreshold: Int = NetworkParams.AUTO_MINE_THRESHOLD,
) {
    fun mineManual(): MineResult = mine(trigger = "manual")

    fun tryAutoMine(): MineResult {
        val pendingTransfers = mempoolManager.get().all()
        if (pendingTransfers.size < autoMineThreshold) {
            return NotMined("auto mine threshold not reached")
        }

        return mine(trigger = "auto")
    }

    private fun mine(trigger: String): MineResult {
        while (true) {
            val tip = chainManager.latest()
            val candidate = mineCandidate(tip)

            when (val addResult = chainManager.addBlock(candidate)) {
                is BlockAppended -> {
                    val minedTransferIds = candidate.getTransactions()
                        .filterNot { it is CoinbaseTransaction }
                        .map { it.getId() }

                    if (minedTransferIds.isNotEmpty()) {
                        mempoolManager.removeByIds(minedTransferIds)
                    }

                    return MinedBlock(
                        block = addResult.block,
                        chainLength = addResult.chainLength,
                        trigger = trigger,
                    )
                }

                is BlockRejected -> continue
            }
        }
    }

    private fun mineCandidate(tip: Block?): Block {
        val timestamp = nextTimestamp(tip)

        return if (tip == null) {
            mineBlock(
                index = 0,
                timestamp = timestamp,
                previousHash = "0",
                transactions = emptyList(),
            )
        } else {
            val pendingTransfers = mempoolManager.get().all()
            val coinbase = CoinbaseTransaction(
                id = UUID.randomUUID().toString(),
                to = minerAddress,
                amount = reward,
                timestamp = timestamp,
            )

            mineBlock(
                index = tip.getIndex() + 1,
                timestamp = timestamp,
                previousHash = tip.getHash(),
                transactions = listOf(coinbase) + pendingTransfers,
            )
        }
    }

    private fun mineBlock(
        index: Int,
        timestamp: Long,
        previousHash: String,
        transactions: List<Transaction>,
    ): Block {
        var nonce = 0L

        while (true) {
            val payload = BlockHashing.canonicalPayload(
                index = index,
                timestamp = timestamp,
                previousHash = previousHash,
                nonce = nonce,
                transactionIds = transactions.map { it.getId() },
            )

            val hash = BlockHashing.hashHex(payload)
            if (hash.startsWith(difficultyPrefix)) {
                val preBlock = PreBlock(
                    index = index,
                    timestamp = timestamp,
                    transactions = transactions,
                    previousHash = previousHash,
                    nonce = nonce,
                )

                return Block(preBlock = preBlock, hash = hash)
            }

            nonce++
        }
    }

    private fun nextTimestamp(previous: Block?): Long {
        val now = nowProvider()

        if (previous == null) {
            return if (now > 0L) now else 1L
        }

        val minimum = previous.getTimestamp() + 1
        return if (now > minimum) now else minimum
    }
}

