package transactions

import app.NetworkParams
import network.dto.TransactionDto

object TransactionMapper {
    private const val ZERO_64 = "0000000000000000000000000000000000000000000000000000000000000000"

    fun fromDto(dto: TransactionDto): Transaction? {
        return when (dto.type.uppercase()) {
            "TRANSFER" -> TransferTransaction(
                id = dto.id,
                from = dto.from,
                to = dto.to,
                amount = dto.amount,
                timestamp = dto.timestamp,
                publicKey = dto.publicKey,
                signature = dto.signature
            )

            "COINBASE" -> {
                if (dto.from != "SYSTEM") return null
                if (dto.amount != NetworkParams.BLOCK_REWARD) return null
                if (dto.publicKey != ZERO_64) return null
                if (dto.signature != ZERO_64) return null

                CoinbaseTransaction(
                    id = dto.id,
                    to = dto.to,
                    amount = dto.amount,
                    timestamp = dto.timestamp
                )
            }

            else -> null
        }
    }
}