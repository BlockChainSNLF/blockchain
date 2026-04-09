package transactions

import network.dto.TransactionDto

object TransactionMapper {
    fun fromDto(dto: TransactionDto): Transaction {
        return TransferTransaction(
            id = dto.id,
            from = dto.from,
            to = dto.to,
            amount = dto.amount,
            timestamp = dto.timestamp,
            publicKey = dto.publicKey,
            signature = dto.signature
        )
    }
}