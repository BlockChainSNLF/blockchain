package consensus

import common.HashUtils
import common.NetworkConfig
import network.dto.BlockDto
import network.dto.TransactionDto
import java.util.UUID

class Miner(
    private val minerAddress: String
) {

    // Bloque genesis: index 0, sin transacciones, previousHash = "0"
    fun buildGenesis(): BlockDto {
        val timestamp = System.currentTimeMillis()
        val nonce = "0"
        val hash = computeHash(0, timestamp, NetworkConfig.GENESIS_PREVIOUS_HASH, nonce, emptyList())
        return BlockDto(
            index = 0,
            timestamp = timestamp,
            transactions = emptyList(),
            previousHash = NetworkConfig.GENESIS_PREVIOUS_HASH,
            hash = hash,
            nonce = nonce
        )
    }

    // Bloque candidato normal: COINBASE primero + transfers del mempool en FIFO
    fun buildCandidate(
        previousBlock: BlockDto?,
        pendingTransfers: List<TransactionDto>
    ): BlockDto {
        val index = (previousBlock?.index ?: -1) + 1
        val previousHash = previousBlock?.hash ?: NetworkConfig.GENESIS_PREVIOUS_HASH
        val timestamp = System.currentTimeMillis()

        val coinbase = TransactionDto(
            id = UUID.randomUUID().toString(),
            type = "COINBASE",
            from = NetworkConfig.COINBASE_FROM,
            to = minerAddress,
            amount = NetworkConfig.BLOCK_REWARD,
            timestamp = timestamp,
            publicKey = NetworkConfig.COINBASE_ZERO,
            signature = NetworkConfig.COINBASE_ZERO
        )

        val transactions = listOf(coinbase) + pendingTransfers
        val nonce = "0"
        val hash = computeHash(index, timestamp, previousHash, nonce, transactions)

        return BlockDto(
            index = index,
            timestamp = timestamp,
            transactions = transactions,
            previousHash = previousHash,
            hash = hash,
            nonce = nonce
        )
    }

    // Proof of Work: itera el nonce hasta que el hash empiece con REQUIRED_PREFIX
    fun mine(candidate: BlockDto): BlockDto {
        var nonce = 0L
        while (true) {
            val nonceStr = nonce.toString()
            val hash = computeHash(
                index = candidate.index,
                timestamp = candidate.timestamp,
                previousHash = candidate.previousHash,
                nonce = nonceStr,
                transactions = candidate.transactions
            )
            if (hash.startsWith(NetworkConfig.REQUIRED_PREFIX)) {
                return candidate.copy(nonce = nonceStr, hash = hash)
            }
            nonce++
        }
    }

    // Formato canónico del TP: index|timestamp|previousHash|nonce|txId1,txId2,...
    private fun computeHash(
        index: Int,
        timestamp: Long,
        previousHash: String,
        nonce: String,
        transactions: List<TransactionDto>
    ): String {
        val txIds = transactions.joinToString(",") { it.id }
        val payload = "$index|$timestamp|$previousHash|$nonce|$txIds"
        return HashUtils.sha256Hex(payload)
    }
}
