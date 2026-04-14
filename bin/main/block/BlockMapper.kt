package block

import network.dto.BlockDto
import network.dto.TransactionDto
import transactions.Coinbase
import transactions.Transfer
import transactions.Transaction
import transactions.TransactionMapper

object BlockMapper {
    fun fromDto(dto: BlockDto): Block? {
        val transactions = mutableListOf<Transaction>()
        dto.transactions.forEach { txDto ->
            val tx = TransactionMapper.fromDto(txDto) ?: return null
            transactions.add(tx)
        }

        val preBlock = PreBlock(
            index = dto.index,
            timestamp = dto.timestamp,
            transactions = transactions,
            previousHash = dto.previousHash,
            nonce = dto.nonce,
        )

        return Block(
            preBlock = preBlock,
            hash = dto.hash,
        )
    }

    fun toDto(block: Block): BlockDto {
        return BlockDto(
            index = block.getIndex(),
            timestamp = block.getTimestamp(),
            transactions = block.getTransactions().map { it.toDto() },
            previousHash = block.getPreviousHash(),
            hash = block.getHash(),
            nonce = block.getNonce(),
        )
    }

    private fun Transaction.toDto(): TransactionDto {
        return TransactionDto(
            id = getId(),
            type = when (getType()) {
                Transfer -> "TRANSFER"
                Coinbase -> "COINBASE"
            },
            from = getFrom(),
            to = getTo(),
            amount = getAmount(),
            timestamp = getTimestamp(),
            publicKey = getPublicKey(),
            signature = getSignature(),
        )
    }
}

