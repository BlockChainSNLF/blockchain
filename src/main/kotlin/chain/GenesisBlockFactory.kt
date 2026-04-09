package chain

import app.NetworkParams
import block.Block
import block.BlockHashing
import block.PreBlock
import transactions.Transaction

object GenesisBlockFactory {
    private const val GENESIS_TIMESTAMP = 1L

    fun createGenesis(): Block {
        val transactions: List<Transaction> = emptyList()
        var nonce = 0L

        while (true) {
            val payload = BlockHashing.canonicalPayload(
                index = 0,
                timestamp = GENESIS_TIMESTAMP,
                previousHash = "0",
                nonce = nonce,
                transactionIds = emptyList(),
            )

            val hash = BlockHashing.hashHex(payload)
            if (hash.startsWith(NetworkParams.DIFFICULTY_PREFIX)) {
                return Block(
                    preBlock = PreBlock(
                        index = 0,
                        timestamp = GENESIS_TIMESTAMP,
                        transactions = transactions,
                        previousHash = "0",
                        nonce = nonce,
                    ),
                    hash = hash,
                )
            }

            nonce++
        }
    }
}

