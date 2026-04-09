package transactions

import network.dto.TransactionDto

object TransactionMapper {

    fun fromDto(dto: TransactionDto): Transaction {
        return when (dto.type) {
            "TRANSFER" -> TransferTransaction(
                id = dto.id,
                from = dto.from,
                to = dto.to,
                amount = dto.amount,
                timestamp = dto.timestamp,
                publicKey = dto.publicKey,
                signature = dto.signature
            )
            "COINBASE" -> CoinbaseTransaction(
                id = dto.id,
                to = dto.to,
                amount = dto.amount,
                timestamp = dto.timestamp
            )
            else -> throw IllegalArgumentException("Unknown transaction type: ${dto.type}")
        }
    }

    fun toDto(tx: Transaction): TransactionDto {
        return TransactionDto(
            id = tx.getId(),
            type = when (tx) {
                is TransferTransaction -> "TRANSFER"
                is CoinbaseTransaction -> "COINBASE"
            },
            from = tx.getFrom(),
            to = tx.getTo(),
            amount = tx.getAmount(),
            timestamp = tx.getTimestamp(),
            publicKey = tx.getPublicKey(),
            signature = tx.getSignature()
        )
    }
}
