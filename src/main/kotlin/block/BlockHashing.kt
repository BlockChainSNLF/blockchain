package block

import java.security.MessageDigest

object BlockHashing {
    fun canonicalPayload(
        index: Int,
        timestamp: Long,
        previousHash: String,
        nonce: Long,
        transactionIds: List<String>,
    ): String {
        return "$index|$timestamp|$previousHash|$nonce|${transactionIds.joinToString(",")}"
    }

    fun hashHex(payload: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun recompute(block: Block): String {
        val txIds = block.getTransactions().map { it.getId() }
        val payload = canonicalPayload(
            index = block.getIndex(),
            timestamp = block.getTimestamp(),
            previousHash = block.getPreviousHash(),
            nonce = block.getNonce(),
            transactionIds = txIds,
        )

        return hashHex(payload)
    }
}

